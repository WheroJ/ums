package com.zetavision.panda.ums.base;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;

import com.zetavision.panda.ums.Utils.ActivityCollector;
import com.zetavision.panda.ums.Utils.Constant;
import com.zetavision.panda.ums.Utils.UserPreferences;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import butterknife.ButterKnife;

abstract public class BaseActivity extends Activity {

    private BaseActivity mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCollector.addActivity(this);
        changeAppLanguage();
        onCreateView();
        ButterKnife.bind(this);
        init();
        mContext = this;
    }

    protected BaseActivity getThis() {
        return mContext;
    }

    protected void init() {}

    abstract public void onCreateView();

    public void changeAppLanguage() {
        UserPreferences preferences = new UserPreferences();
        String sta = preferences.getLanguage();
        Locale myLocale = new Locale(sta);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String str) {
        switch (str) {
            case Constant.EVENT_REFRESH_LANGUAGE:
                changeAppLanguage();
                //有待测试
//                onCreate(null);
                finish();
                Intent intent = new Intent(this, this.getClass());
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //建议需要的时候在对应界面开启，基类调用这样会占用资源
//        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
//        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mContext = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
