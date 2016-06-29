package com.ashokvarma.rxsamples;

import android.app.Application;

import com.ashokvarma.rxsamples.volley.MyVolley;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import timber.log.Timber;

public class SampleApplication
      extends Application {

    private static SampleApplication _instance;
    private RefWatcher _refWatcher;

    public static SampleApplication get() {
        return _instance;
    }

    public static RefWatcher getRefWatcher() {
        return SampleApplication.get()._refWatcher;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        _instance = (SampleApplication) getApplicationContext();
        _refWatcher = LeakCanary.install(this);

        // Initialize Volley
        MyVolley.init(this);

        Timber.plant(new Timber.DebugTree());
    }
}
