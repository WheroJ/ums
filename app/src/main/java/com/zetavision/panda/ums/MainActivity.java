package com.zetavision.panda.ums;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zetavision.panda.ums.Utils.Common;
import com.zetavision.panda.ums.Utils.Constant;
import com.zetavision.panda.ums.Utils.UserPreferences;
import com.zetavision.panda.ums.base.BaseActivity;
import com.zetavision.panda.ums.fragments.BaoyangFragment;
import com.zetavision.panda.ums.fragments.DianjianFragment;
import com.zetavision.panda.ums.fragments.DownloadFragment;
import com.zetavision.panda.ums.fragments.UploadFragment;
import com.zetavision.panda.ums.model.User;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    @BindView(R.id.leftLayout) public LinearLayout leftLayout;
    @BindView(R.id.title) public TextView title;
    @BindView(R.id.username) public TextView username;

    private int current = R.id.download;

    @Override
    public void onCreateView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void init() {
        UserPreferences preferences = new UserPreferences();
        User user = preferences.getUser();
        if (user != null) {
            username.setText(user.getUSERNAME());
        }
        changeContent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {//应用没有该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {//是否应该继续显示对话框
                    //之前请求过拒绝了   返回true
                    //如果用户在过去拒绝了权限请求，并在权限请求系统对话框中选择了 Don't ask again 选项，此方法将返回 false。如果设备规范禁止应用具有该权限，此方法也会返回 false
                    new AlertDialog.Builder(this).setTitle("申请权限").setMessage("拍照需要申请相机权限，是否允许?").setPositiveButton("取消", null).setNegativeButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //点击确定的时候再次进行权限的申请
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
                        }
                    }).show();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
                }
            }
        }
    }

    private void changeContent() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        switch (this.current) {
            case R.id.download:
                transaction.replace(R.id.content, new DownloadFragment());
                break;
            case R.id.upload:
                transaction.replace(R.id.content, new UploadFragment());
                break;
            case R.id.baoyang:
                transaction.replace(R.id.content, new BaoyangFragment());
                break;
            case R.id.dianjian:
                transaction.replace(R.id.content, new DianjianFragment());
                break;
        }
        transaction.commit();
    }

    @OnClick({R.id.download, R.id.upload, R.id.baoyang, R.id.dianjian}) void onChange(LinearLayout view) {
        if (view.getId() != this.current) {
            LinearLayout preView = findViewById(this.current);
            preView.setBackground(null);
            this.current = view.getId();
            TextView textView = (TextView)view.getChildAt(1);
            this.title.setText(textView.getText());
            view.setBackgroundColor(Color.parseColor("#62a7ea"));
            changeContent();
        }
    }

    @OnClick(R.id.logout) void logout() {
        Intent login = new Intent(this, LoginActivity.class);
        login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(login);
    }

    @OnClick(R.id.menu) void onMenu() {
        float start;
        float end;
        if (Common.dip2px(this, 180) == this.leftLayout.getWidth()) {
            start = Common.dip2px(this, 180);
            end = 0;
        } else {
            start = 0;
            end = Common.dip2px(this, 180);
        }
        final ViewGroup.LayoutParams params = leftLayout.getLayoutParams();
        ValueAnimator anim = ValueAnimator.ofFloat(start, end);
        anim.setDuration(300);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                params.width = (int)(float) animation.getAnimatedValue();
                leftLayout.setLayoutParams(params);
            }
        });
        anim.start();
    }

    @OnClick(R.id.username) void onChangeLanguage() {
        // 中英文切换
        UserPreferences preferences = new UserPreferences();
        if (preferences.getLanguage().equals(Locale.CHINESE.getLanguage())) {//中文
            preferences.setLanguage(Locale.ENGLISH.getLanguage());
        } else {//英文
            preferences.setLanguage(Locale.CHINESE.getLanguage());
        }
        EventBus.getDefault().post(Constant.EVENT_REFRESH_LANGUAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Toast.makeText(MainActivity.this,"申请相机权限成功", Toast.LENGTH_LONG).show();
                } else {
                    // Toast.makeText(this,"申请相机权限失败",Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }
}
