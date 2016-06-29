package com.ashokvarma.rxsamples.volley;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.ashokvarma.rxsamples.R;
import com.ashokvarma.rxsamples.fragments.BaseFragment;
import com.ashokvarma.rxsamples.wiring.LogAdapter;

import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class VolleyDemoFragment
      extends BaseFragment {

    public static final String TAG = "VolleyDemoFragment";

    @Bind(R.id.list_threading_log) ListView _logsList;

    private List<String> _logs;
    private LogAdapter _adapter;

    private CompositeSubscription _compositeSubscription = new CompositeSubscription();

    @Override
    public View onCreateView(LayoutInflater inflater,
          @Nullable ViewGroup container,
          @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_volley, container, false);
        ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _setupLogger();
    }

    @Override
    public void onPause() {
        super.onPause();
        _compositeSubscription.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public Observable<JSONObject> newGetRouteData() {
        return Observable.defer(new Func0<Observable<JSONObject>>() {
            @Override
            public Observable<JSONObject> call() {
                try {
                    return Observable.just(getRouteData());
                } catch (InterruptedException | ExecutionException e) {
                    Log.e("routes", e.getMessage());
                    return Observable.error(e);
                }
            }
        });
    }

    @OnClick(R.id.btn_start_operation)
    void startRequest() {
        startVolleyRequest();
    }

    private void startVolleyRequest() {
        _compositeSubscription.add(newGetRouteData().subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(new Observer<JSONObject>() {
                  @Override
                  public void onCompleted() {
                      Log.e(TAG, "onCompleted");
                      Timber.d("----- onCompleted");
                      _log("onCompleted ");
                  }

                  @Override
                  public void onError(Throwable e) {
                      VolleyError cause = (VolleyError) e.getCause();
                      String s = new String(cause.networkResponse.data, Charset.forName("UTF-8"));
                      Log.e(TAG, s);
                      Log.e(TAG, cause.toString());
                      _log("onError " + s);

                  }

                  @Override
                  public void onNext(JSONObject jsonObject) {
                      Log.e(TAG, "onNext " + jsonObject.toString());
                      _log("onNext " + jsonObject.toString());

                  }
              }));
    }

    private JSONObject getRouteData() throws ExecutionException, InterruptedException {
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        String url = "http://www.weather.com/adat/sk/101010100.html";
        final Request.Priority priority = Request.Priority.IMMEDIATE;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, future, future);
        MyVolley.getRequestQueue().add(req);
        return future.get();
    }

    // -----------------------------------------------------------------------------------
    // Methods that help wiring up the example (irrelevant to RxJava)

    private void _setupLogger() {
        _logs = new ArrayList<>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<String>());
        _logsList.setAdapter(_adapter);
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
}
