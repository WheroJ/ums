package com.zetavision.panda.ums.fragments;

import android.view.LayoutInflater;
import android.view.View;

import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.fragments.base.BaseFragment;

public class UploadFragment extends BaseFragment{

    @Override
    protected View createView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_upload, null);
    }

}
