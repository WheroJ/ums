package com.zetavision.panda.ums.fragments.base;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseFragment extends Fragment{

    protected Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View view = createView(inflater);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    abstract protected  View createView(LayoutInflater inflater);
    protected void init() {}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder!=null) {
            unbinder.unbind();
        }
    }
}
