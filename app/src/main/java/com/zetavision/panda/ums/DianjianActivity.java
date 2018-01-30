package com.zetavision.panda.ums;


import android.content.Intent;
import android.widget.ListView;

import com.zetavision.panda.ums.base.BaseActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class DianjianActivity extends BaseActivity {

    @BindView(R.id.listView) ListView listView;

    @Override
    public void onCreateView() {
        setContentView(R.layout.activity_dianjian);
    }

    @OnClick(R.id.back) void back() {
        onBackPressed();
    }

    @OnClick(R.id.logout) void logout() {
        Intent login = new Intent(this, LoginActivity.class);
        login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(login);
    }
}