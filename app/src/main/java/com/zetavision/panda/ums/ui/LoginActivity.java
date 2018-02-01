package com.zetavision.panda.ums.ui;

import android.view.View;
import android.widget.EditText;

import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.base.BaseActivity;
import com.zetavision.panda.ums.model.Result;
import com.zetavision.panda.ums.model.User;
import com.zetavision.panda.ums.utils.IntentUtils;
import com.zetavision.panda.ums.utils.SPUtil;
import com.zetavision.panda.ums.utils.ToastUtils;
import com.zetavision.panda.ums.utils.UserPreferences;
import com.zetavision.panda.ums.utils.network.Client;
import com.zetavision.panda.ums.utils.network.RxUtils;
import com.zetavision.panda.ums.utils.network.UmsApi;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.login_box) public View login_box;
    @BindView(R.id.username) public EditText username;
    @BindView(R.id.password) public EditText password;

    @OnClick(R.id.login) void login() {

        final UserPreferences preferences = new UserPreferences();
        preferences.clearCookie();
        SPUtil.clearCookie();

        UmsApi loginApi = Client.getApi(UmsApi.class);
        RxUtils.INSTANCE.acquireString(loginApi.login(username.getText().toString(), password.getText().toString()), new RxUtils.DialogListener(this) {
            @Override
            public void onResult(@NotNull Result result) {
                preferences.saveUser(result.getData(User.class));
                IntentUtils.INSTANCE.goMain(getThis());
            }

            @Override
            public void onError(@NotNull Throwable e) {
                super.onError(e);
                ToastUtils.show(e.getMessage());
            }
        });
    }



    @Override
    public int getContentLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void init() {

    }
}
