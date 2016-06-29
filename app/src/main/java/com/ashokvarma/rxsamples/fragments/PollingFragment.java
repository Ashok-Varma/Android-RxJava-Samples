package com.ashokvarma.rxsamples.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ashokvarma.rxsamples.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class PollingFragment
      extends BaseFragment {

    private static final int INITIAL_DELAY = 0;
    private static final int POLLING_INTERVAL = 1000;
    private static final int POLL_COUNT = 8;

    @Bind(R.id.list_threading_log) ListView _logsList;

    private LogAdapter _adapter;
    private List<String> _logs;

    private CompositeSubscription _subscriptions;
    private int _counter = 0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _subscriptions = new CompositeSubscription();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _setupLogger();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_polling, container, false);
        ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _subscriptions.unsubscribe();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.btn_start_simple_polling)
    public void onStartSimplePollingClicked() {

        final int pollCount = POLL_COUNT;

        _subscriptions.add(//
              Observable.interval(INITIAL_DELAY, POLLING_INTERVAL, TimeUnit.MILLISECONDS)
                    .map(new Func1<Long, String>() {
                        @Override
                        public String call(Long heartBeat) {
                            return _doNetworkCallAndGetStringResult(heartBeat);
                        }
                    }).take(pollCount)
                    .doOnSubscribe(new Action0() {
                        @Override
                        public void call() {
                            _log(String.format("Start simple polling - %s", _counter));
                        }
                    })
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String taskName) {
                            _log(String.format(Locale.US, "Executing polled task [%s] now time : [xx:%02d]",
                                  taskName, _getSecondHand()));
                        }
                    })
        );
    }

    @OnClick(R.id.btn_start_increasingly_delayed_polling)
    public void onStartIncreasinglyDelayedPolling() {
        _setupLogger();

        final int pollingInterval = POLLING_INTERVAL;
        final int pollCount = POLL_COUNT;

        _log(String.format(Locale.US, "Start increasingly delayed polling now time: [xx:%02d]",
              _getSecondHand()));

        _subscriptions.add(//
              Observable.just(1)
                    .repeatWhen(new RepeatWithDelay(pollCount, pollingInterval))
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object o) {
                            _log(String.format(Locale.US, "Executing polled task now time : [xx:%02d]",
                                  _getSecondHand()));
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable e) {
                            Timber.d(e, "arrrr. Error");
                        }
                    })
        );
    }

    // -----------------------------------------------------------------------------------

    // CAUTION:
    // --------------------------------------
    // THIS notificationHandler class HAS NO BUSINESS BEING non-static
    // I ONLY did this cause i wanted access to the `_log` method from inside here
    // for the purpose of demonstration. In the real world, make it static and LET IT BE!!

    // It's 12am in the morning and i feel lazy dammit !!!

    //public static class RepeatWithDelay
    public class RepeatWithDelay
          implements Func1<Observable<? extends Void>, Observable<?>> {

        private final int _repeatLimit;
        private final int _pollingInterval;
        private int _repeatCount = 1;

        RepeatWithDelay(int repeatLimit, int pollingInterval) {
            _pollingInterval = pollingInterval;
            _repeatLimit = repeatLimit;
        }

        // this is a notificationhandler, all we care about is
        // the emission "type" not emission "content"
        // only onNext triggers a re-subscription

        @Override
        public Observable<?> call(Observable<? extends Void> inputObservable) {

            // it is critical to use inputObservable in the chain for the result
            // ignoring it and doing your own thing will break the sequence

            return inputObservable.flatMap(new Func1<Void, Observable<?>>() {
                @Override
                public Observable<?> call(Void blah) {


                    if (_repeatCount >= _repeatLimit) {
                        // terminate the sequence cause we reached the limit
                        _log("Completing sequence");
                        return Observable.empty();
                    }

                    // since we don't get an input
                    // we store state in this handler to tell us the point of time we're firing
                    _repeatCount++;

                    return Observable.timer(_repeatCount * _pollingInterval,
                          TimeUnit.MILLISECONDS);
                }
            });
        }
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    private String _doNetworkCallAndGetStringResult(long attempt) {
        try {
            if (attempt == 4) {
                // randomly make one event super long so we test that the repeat logic waits
                // and accounts for this.
                Thread.sleep(9000);
            } else {
                Thread.sleep(3000);
            }

        } catch (InterruptedException e) {
            Timber.d("Operation was interrupted");
        }
        _counter++;

        return String.valueOf(_counter);
    }

    private int _getSecondHand() {
        long millis = System.currentTimeMillis();
        return (int) (TimeUnit.MILLISECONDS.toSeconds(millis) -
                      TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    private void _log(String logMsg) {
        if (_isCurrentlyOnMainThread()) {
            _logs.add(0, logMsg + " (main thread) ");
            _adapter.clear();
            _adapter.addAll(_logs);
        } else {
            _logs.add(0, logMsg + " (NOT main thread) ");

            // You can only do below stuff on main thread.
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    _adapter.clear();
                    _adapter.addAll(_logs);
                }
            });
        }
    }

    private void _setupLogger() {
        _logs = new ArrayList<>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<String>());
        _logsList.setAdapter(_adapter);
        _counter = 0;
    }

    private class LogAdapter
          extends ArrayAdapter<String> {

        public LogAdapter(Context context, List<String> logs) {
            super(context, R.layout.item_log, R.id.item_log, logs);
        }
    }
}