package com.zetavision.panda.ums.base;

import android.app.Fragment;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.fragments.base.BaseFragment;
import com.zetavision.panda.ums.model.Result;
import com.zetavision.panda.ums.model.User;
import com.zetavision.panda.ums.ui.LoginActivity;
import com.zetavision.panda.ums.utils.ActivityCollector;
import com.zetavision.panda.ums.utils.Constant;
import com.zetavision.panda.ums.utils.IntentUtils;
import com.zetavision.panda.ums.utils.SPUtil;
import com.zetavision.panda.ums.utils.ToastUtils;
import com.zetavision.panda.ums.utils.UserPreferences;
import com.zetavision.panda.ums.utils.UserUtils;
import com.zetavision.panda.ums.utils.network.Client;
import com.zetavision.panda.ums.utils.network.RxUtils;
import com.zetavision.panda.ums.utils.network.UmsApi;
import com.zetavision.panda.ums.widget.ViewHeaderBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import butterknife.ButterKnife;

abstract public class BaseActivity extends AppCompatActivity {

    private BaseActivity mContext;
    private boolean hasTitle;
    private Fragment showFragment;
    private LinearLayout llContainer;
    private ViewHeaderBar viewHeadBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        hasTitle = getHasTitle();
        beforeInit();
        setContentView(getLayoutId());
        gainView();

        ButterKnife.bind(this);
        init();
        ActivityCollector.addActivity(this);
    }

    protected ViewHeaderBar getHeader() {
        return viewHeadBar;
    }

    private void gainView() {
        llContainer = findViewById(R.id.fragmentBase_content);
        LinearLayout.LayoutParams LayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        View view = View.inflate(this, getContentLayoutId(), null);
        llContainer.addView(view, LayoutParams);

        viewHeadBar = findViewById(R.id.fragmentBase_header);
        if (hasTitle) {
            viewHeadBar.setOnItemClickListener(new ViewHeaderBar.OnItemClickListener() {
                @Override
                public void onLeftClick() {
                    getThis().onLeftClick();
                }

                @Override
                public void onLogoutClick() {
                    User loginUser = UserUtils.INSTANCE.getCurretnLoginUser();
                    if (loginUser != null) {
                        RxUtils.INSTANCE.acquireString(Client.getApi(UmsApi.class).logout(loginUser.USERNAME)
                                , new RxUtils.DialogListener(getThis()) {
                            @Override
                            public void onResult(@NotNull Result result) {
                                IntentUtils.INSTANCE.goLogout(mContext);
                            }

                            @Override
                            public void onError(@NotNull Throwable e) {
                                super.onError(e);
                                ToastUtils.show(e.getMessage());
                            }
                        });
                    } else {
                        IntentUtils.INSTANCE.goLogout(mContext);
                    }
                }

                @Override
                public void onRightTextClick() {
                    getThis().onRightTextClick();
                }
            });
        } else {
            viewHeadBar.setVisibility(View.GONE);
        }
    }

    protected void onRightTextClick(){}
    /**
     * 默认实现结束当前页
     */
    protected void onLeftClick() {
        finish();
    }



    /**
     * 初始化之前调用，默认空实现
     */
    public void beforeInit() {
        changeAppLanguage();
    }

    /**
     * 默认没有header
     * @return
     */
    protected  boolean getHasTitle() {
        return false;
    }

    private int getLayoutId() {
        return R.layout.fragment_base;
    }

    /**
     * @Title: getContentLayoutId @Description: 设置布局文件 @return @throws
     */
    public abstract int getContentLayoutId();


    /**
     * 替换Fragment
     * @param fragment
     */
    protected void replaceShow(BaseFragment fragment){
        try {
            if (showFragment == null) {
                getFragmentManager().beginTransaction().show(fragment).commitAllowingStateLoss();
                showFragment = fragment;
            } else {
                getFragmentManager().beginTransaction().hide(showFragment).show(fragment).commitAllowingStateLoss();
                showFragment = fragment;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 替换Fragment
     * @param layoutId
     * @param fragment
     */
    protected void replaceShow(Fragment fragment, int layoutId){
        try {
            getFragmentManager().beginTransaction().replace(layoutId, fragment).commitAllowingStateLoss();
            showFragment = fragment;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected BaseActivity getThis() {
        return mContext;
    }

    protected abstract void init();

    public void changeAppLanguage() {
        UserPreferences preferences = new UserPreferences();
        String sta = preferences.getLanguage();
        Locale myLocale = new Locale(sta);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        if (!(this instanceof LoginActivity)) {
            if (preferences.getLanguage().equals(Locale.CHINESE.getLanguage())) {//中文
                RxUtils.INSTANCE.acquireString(Client.getApi(UmsApi.class).setUserLocale(Constant.LANG_CHINA), null);
            } else {//英文
                RxUtils.INSTANCE.acquireString(Client.getApi(UmsApi.class).setUserLocale(Constant.LANG_CHINA), null);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String str) {
        switch (str) {
            case Constant.EVENT_REFRESH_LANGUAGE:
                changeAppLanguage();
                IntentUtils.INSTANCE.reOpenActivity(this);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //从后台运行 转到前台运行
        if (SPUtil.getBoolean(Constant.IS_RUN_BACK, false)) {
            //APP 前台运行重新开启服务
            SPUtil.saveBoolean(Constant.IS_RUN_BACK, false);
            IntentUtils.INSTANCE.startReLoginService();
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        RxUtils.INSTANCE.cancelRequest();
    }

    @Override
    protected void onDestroy() {
        ActivityCollector.removeActivity(this);

        if (ActivityCollector.mActivities.size() == 0) {
            IntentUtils.INSTANCE.clearBuffer(false);
        }
        mContext = null;
        super.onDestroy();
    }
}
