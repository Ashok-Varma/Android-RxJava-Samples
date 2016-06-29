package com.ashokvarma.rxsamples.rxbus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ashokvarma.rxsamples.R;
import com.ashokvarma.rxsamples.fragments.BaseFragment;

import butterknife.ButterKnife;

public class RxBusDemoFragment
      extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_rxbus_demo, container, false);
        ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().getSupportFragmentManager()
              .beginTransaction()
              .replace(R.id.demo_rxbus_frag_1, new RxBusDemo_TopFragment())
              .replace(R.id.demo_rxbus_frag_2, new RxBusDemo_Bottom3Fragment())
              //.replace(R.id.demo_rxbus_frag_2, new RxBusDemo_Bottom2Fragment())
              //.replace(R.id.demo_rxbus_frag_2, new RxBusDemo_Bottom1Fragment())
              .commit();
    }

    public static class TapEvent {}
}