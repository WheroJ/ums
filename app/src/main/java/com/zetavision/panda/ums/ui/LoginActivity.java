package com.zetavision.panda.ums.ui;

import android.view.View;
import android.widget.EditText;

import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.base.BaseActivity;
import com.zetavision.panda.ums.model.Result;
import com.zetavision.panda.ums.model.User;
import com.zetavision.panda.ums.utils.Constant;
import com.zetavision.panda.ums.utils.IntentUtils;
import com.zetavision.panda.ums.utils.NetUtils;
import com.zetavision.panda.ums.utils.ToastUtils;
import com.zetavision.panda.ums.utils.UserUtils;
import com.zetavision.panda.ums.utils.network.Client;
import com.zetavision.panda.ums.utils.network.RxUtils;
import com.zetavision.panda.ums.utils.network.UmsApi;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.login_box) public View login_box;
    @BindView(R.id.userName) public EditText username;
    @BindView(R.id.password) public EditText password;

    @OnClick(R.id.login) void login() {

//        final UserPreferences preferences = new UserPreferences();
//        preferences.clearBuffer();
//        SPUtil.clearBuffer();
        UmsApi loginApi = Client.getApi(UmsApi.class);
        RxUtils.INSTANCE.acquireString(loginApi.login(username.getText().toString(), password.getText().toString())
                , new RxUtils.DialogListener(this) {
            @Override
            public void onResult(@NotNull Result result) {
                User user = result.getData(User.class);
                UserUtils.INSTANCE.setUserLogin(user);
//                IntentUtils.INSTANCE.startReLoginService();
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

        if (!Constant.RE_LOGIN.equals(getIntent().getStringExtra(Constant.RE_LOGIN))) {
            if (!NetUtils.INSTANCE.isNetConnect(this)) {
                offLineLogin();
            }
        }

//        try {
//            File file = new File("haha.text");
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
//            outputStreamWriter.write("hahahahah");
//            outputStreamWriter.write("yyyyyyyyyyyyyy");
//            outputStreamWriter.flush();
//            outputStreamWriter.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
////        "/data/data/com.zetavision.panda.ums/cache/"
//        File file = new File("init.rc");
//        File file1 = new File("haha.text");
//        UploadUtils.INSTANCE.uploadImge(file, this);
    }

    private void offLineLogin() {
        User user = UserUtils.INSTANCE.getCurretnLoginUser();
        if (user != null) {
            if (!UserUtils.INSTANCE.isTokenOutOfDate()) {
                IntentUtils.INSTANCE.startReLoginService();
                IntentUtils.INSTANCE.goMain(getThis());
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        IntentUtils.INSTANCE.goExit(this);
    }
}
