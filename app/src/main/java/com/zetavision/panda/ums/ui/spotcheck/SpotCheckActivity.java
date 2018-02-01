package com.zetavision.panda.ums.ui.spotcheck;


import android.widget.ListView;

import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.base.BaseActivity;

import butterknife.BindView;

public class SpotCheckActivity extends BaseActivity {

    @BindView(R.id.listView) ListView listView;

    @Override
    public int getContentLayoutId() {
        return R.layout.activity_dianjian;
    }

    @Override
    protected void init() {

    }
}