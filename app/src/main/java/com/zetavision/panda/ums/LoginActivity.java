package com.zetavision.panda.ums;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.zetavision.panda.ums.Utils.Api;
import com.zetavision.panda.ums.Utils.LoadingDialog;
import com.zetavision.panda.ums.Utils.UserPreferences;
import com.zetavision.panda.ums.base.BaseActivity;
import com.zetavision.panda.ums.model.Result;
import com.zetavision.panda.ums.model.User;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.login_box) public View login_box;
    @BindView(R.id.username) public EditText username;
    @BindView(R.id.password) public EditText password;

    @Override
    public void onCreateView() {
        setContentView(R.layout.activity_login);
    }

    @OnClick(R.id.login) void login() {

        final UserPreferences preferences = new UserPreferences();
        preferences.clearCookie();

        Api api = new Api(this);
        final LoadingDialog dialog = new LoadingDialog();
        dialog.show(getFragmentManager(), null);
        api.get("login.mobile?USERNAME="+this.username.getText().toString() + "&PASSWORD="+this.password.getText().toString()).subscribe(new Consumer<Result>() {
            @Override
            public void accept(Result result) throws Exception {
                preferences.saveUser(result.getData(User.class));
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, new Action() {
            @Override
            public void run() throws Exception {
                dialog.dismiss();
            }
        });
    }
}
