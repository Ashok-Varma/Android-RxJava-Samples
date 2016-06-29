package com.ashokvarma.rxsamples.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ashokvarma.rxsamples.R;
import com.ashokvarma.rxsamples.RxUtils;
import com.ashokvarma.rxsamples.wiring.LogAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import timber.log.Timber;

import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;

public class TimingDemoFragment
      extends BaseFragment {

    @Bind(R.id.list_threading_log) ListView _logsList;

    private LogAdapter _adapter;
    private List<String> _logs;

    private Subscription _subscription1 = null;
    private Subscription _subscription2 = null;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _setupLogger();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_demo_timing, container, false);
        ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);

        RxUtils.unsubscribeIfNotNull(_subscription1);
        RxUtils.unsubscribeIfNotNull(_subscription2);
    }
// -----------------------------------------------------------------------------------

    @OnClick(R.id.btn_demo_timing_1)
    public void Btn1_RunSingleTaskAfter2s() {
        _log(String.format("A1 [%s] --- BTN click", _getCurrentTimestamp()));

        Observable.timer(2, TimeUnit.SECONDS)//
              //.just(1).delay(2, TimeUnit.SECONDS)//
              .subscribe(new Observer<Long>() {
                  @Override
                  public void onCompleted() {
                      _log(String.format("A1 [%s] XXX COMPLETE", _getCurrentTimestamp()));
                  }

                  @Override
                  public void onError(Throwable e) {
                      Timber.e(e, "something went wrong in TimingDemoFragment example");
                  }

                  @Override
                  public void onNext(Long number) {
                      _log(String.format("A1 [%s]     NEXT", _getCurrentTimestamp()));
                  }
              });
    }

    @OnClick(R.id.btn_demo_timing_2)
    public void Btn2_RunTask_IntervalOf1s() {
        if (_subscription1 != null && !_subscription1.isUnsubscribed()) {
            _subscription1.unsubscribe();
            _log(String.format("B2 [%s] XXX BTN KILLED", _getCurrentTimestamp()));
            return;
        }

        _log(String.format("B2 [%s] --- BTN click", _getCurrentTimestamp()));

        _subscription1 = Observable//
              .interval(1, TimeUnit.SECONDS)//
              .subscribe(new Observer<Long>() {
                  @Override
                  public void onCompleted() {
                      _log(String.format("B2 [%s] XXXX COMPLETE", _getCurrentTimestamp()));
                  }

                  @Override
                  public void onError(Throwable e) {
                      Timber.e(e, "something went wrong in TimingDemoFragment example");
                  }

                  @Override
                  public void onNext(Long number) {
                      _log(String.format("B2 [%s]     NEXT", _getCurrentTimestamp()));
                  }
              });
    }

    @OnClick(R.id.btn_demo_timing_3)
    public void Btn3_RunTask_IntervalOf1s_StartImmediately() {
        if (_subscription2 != null && !_subscription2.isUnsubscribed()) {
            _subscription2.unsubscribe();
            _log(String.format("C3 [%s] XXX BTN KILLED", _getCurrentTimestamp()));
            return;
        }

        _log(String.format("C3 [%s] --- BTN click", _getCurrentTimestamp()));

        _subscription2 = Observable//
              .interval(0, 1, TimeUnit.SECONDS)//
              .subscribe(new Observer<Long>() {
                  @Override
                  public void onCompleted() {
                      _log(String.format("C3 [%s] XXXX COMPLETE", _getCurrentTimestamp()));
                  }

                  @Override
                  public void onError(Throwable e) {
                      Timber.e(e, "something went wrong in TimingDemoFragment example");
                  }

                  @Override
                  public void onNext(Long number) {
                      _log(String.format("C3 [%s]     NEXT", _getCurrentTimestamp()));
                  }
              });
    }

    @OnClick(R.id.btn_demo_timing_4)
    public void Btn4_RunTask5Times_IntervalOf3s() {
        _log(String.format("D4 [%s] --- BTN click", _getCurrentTimestamp()));

        Observable//
              .interval(3, TimeUnit.SECONDS).take(5)//
              .subscribe(new Observer<Long>() {
                  @Override
                  public void onCompleted() {
                      _log(String.format("D4 [%s] XXX COMPLETE", _getCurrentTimestamp()));
                  }

                  @Override
                  public void onError(Throwable e) {
                      Timber.e(e, "something went wrong in TimingDemoFragment example");
                  }

                  @Override
                  public void onNext(Long number) {
                      _log(String.format("D4 [%s]     NEXT", _getCurrentTimestamp()));
                  }
              });
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    @OnClick(R.id.btn_clr)
    public void OnClearLog() {
        _logs = new ArrayList<>();
        _adapter.clear();
    }

    private void _setupLogger() {
        _logs = new ArrayList<>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<String>());
        _logsList.setAdapter(_adapter);
    }

    private void _log(String logMsg) {
        _logs.add(0, String.format(logMsg + " [MainThread: %b]", getMainLooper() == myLooper()));

        // You can only do below stuff on main thread.
        new Handler(getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                _adapter.clear();
                _adapter.addAll(_logs);
            }
        });
    }

    private String _getCurrentTimestamp() {
        return new SimpleDateFormat("k:m:s:S a").format(new Date());
    }

}
