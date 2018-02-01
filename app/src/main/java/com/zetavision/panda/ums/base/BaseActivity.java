package com.zetavision.panda.ums.base;

import android.app.Fragment;
import android.content.Intent;
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
import com.zetavision.panda.ums.utils.ActivityCollector;
import com.zetavision.panda.ums.utils.Constant;
import com.zetavision.panda.ums.utils.IntentUtils;
import com.zetavision.panda.ums.utils.UserPreferences;
import com.zetavision.panda.ums.widget.ViewHeaderBar;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
                    IntentUtils.INSTANCE.goLogout(mContext);
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
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        mContext = null;
    }
}
