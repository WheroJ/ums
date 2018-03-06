package com.zetavision.panda.ums.ui;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.base.BaseActivity;
import com.zetavision.panda.ums.model.Result;
import com.zetavision.panda.ums.model.User;
import com.zetavision.panda.ums.utils.AESTool;
import com.zetavision.panda.ums.utils.Constant;
import com.zetavision.panda.ums.utils.IntentUtils;
import com.zetavision.panda.ums.utils.NetUtils;
import com.zetavision.panda.ums.utils.ToastUtils;
import com.zetavision.panda.ums.utils.UserPreferences;
import com.zetavision.panda.ums.utils.UserUtils;
import com.zetavision.panda.ums.utils.network.Client;
import com.zetavision.panda.ums.utils.network.RxUtils;
import com.zetavision.panda.ums.utils.network.UmsApi;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import okhttp3.ResponseBody;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.login_box) public View login_box;
    @BindView(R.id.userName) public EditText username;
    @BindView(R.id.password) public EditText password;
    @BindView(R.id.activityLogin_radioGroup)
    RadioGroup rgLang;

    /**
     * 是否离线登录
     */
    private boolean offLogin = false;

    @OnClick(R.id.login) void login() {
        if (!offLogin) {
            UmsApi loginApi = Client.getApi(UmsApi.class);
            final String pass = password.getText().toString();
            String userName = username.getText().toString();
            Observable<ResponseBody> login = loginApi.login(userName, pass, getLanguage());
            RxUtils.INSTANCE.acquireString(login, new RxUtils.DialogListener(this) {
                @Override
                public void onResult(@NotNull Result result) {
                    User user = result.getData(User.class);
                    user.PASS = AESTool.combineEncrypt(pass);
                    UserUtils.INSTANCE.setUserLogin(user);
                    IntentUtils.INSTANCE.startReLoginService();
                    IntentUtils.INSTANCE.goMain(getThis());
                }

                @Override
                public void onError(@NotNull Throwable e) {
                    super.onError(e);
                    ToastUtils.show(e.getMessage());
                }
            });
        } else {
            String pass = password.getText().toString();
            String userName = username.getText().toString();
            User user = UserUtils.INSTANCE.getUserByName(userName);
            if (user == null) {
                ToastUtils.show(R.string.no_user);
            } else {
                if (TextUtils.isEmpty(pass)) {
                    ToastUtils.show(R.string.pass_notnull);
                } else {
                    String encrypt = AESTool.combineEncrypt(pass);
                    if (encrypt.equals(user.PASS)) {
                        offLineLogin();
                    } else {
                        ToastUtils.show(R.string.pass_error);
                    }
                }
            }
        }
    }



    @Override
    public int getContentLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void init() {

        changeCheckButton();
        rgLang.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.activityLogin_rbCh:
                        setLanguage(Constant.LANG_CHINA);
                        break;
                    case R.id.activityLogin_rbEn:
                        setLanguage(Constant.LANG_ENGLISH);
                        break;
                }
            }
        });
    }

    private void setLanguage(String lang) {
        UserPreferences preferences = new UserPreferences();
        if (Constant.LANG_ENGLISH.equals(lang)) {
            preferences.setLanguage(Locale.ENGLISH.getLanguage());
        } else {//英文
            preferences.setLanguage(Locale.CHINESE.getLanguage());
        }
        EventBus.getDefault().post(Constant.EVENT_REFRESH_LANGUAGE);
    }

    private void changeCheckButton() {
        UserPreferences preferences = new UserPreferences();
        if (Locale.ENGLISH.getLanguage().equals(preferences.getLanguage())) {
            rgLang.check(R.id.activityLogin_rbEn);
        } else {//英文
            rgLang.check(R.id.activityLogin_rbCh);
        }
    }

    private String getLanguage() {
        UserPreferences preferences = new UserPreferences();
        if (Locale.ENGLISH.getLanguage().equals(preferences.getLanguage())) {
            return Constant.LANG_ENGLISH;
        } else {//英文
            return Constant.LANG_CHINA;
        }
    }

    private void offLineLogin() {
        if (!UserUtils.INSTANCE.isTokenOutOfDate()) {
            IntentUtils.INSTANCE.startReLoginService();
            IntentUtils.INSTANCE.goMain(getThis());
        } else {
            ToastUtils.show(R.string.userinfo_outdate);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        IntentUtils.INSTANCE.goExit(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Constant.RE_LOGIN.equals(getIntent().getStringExtra(Constant.RE_LOGIN))) {
            if (!NetUtils.INSTANCE.isNetConnect(this)) {
                offLogin = true;
            } else offLogin = false;
        }
    }
}
