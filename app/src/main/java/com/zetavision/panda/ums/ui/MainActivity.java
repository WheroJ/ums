package com.zetavision.panda.ums.ui;

import android.Manifest;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.base.BaseActivity;
import com.zetavision.panda.ums.model.FormInfo;
import com.zetavision.panda.ums.model.FormInfoDetail;
import com.zetavision.panda.ums.ui.formdownload.DownloadFragment;
import com.zetavision.panda.ums.ui.formup.UploadFragment;
import com.zetavision.panda.ums.ui.spotcheck.SpotCheckFragment;
import com.zetavision.panda.ums.ui.upkeep.UpKeepFragment;
import com.zetavision.panda.ums.utils.Common;
import com.zetavision.panda.ums.utils.Constant;
import com.zetavision.panda.ums.utils.IntentUtils;
import com.zetavision.panda.ums.utils.ToastUtils;
import com.zetavision.panda.ums.utils.UserPreferences;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends BaseActivity {

    @BindView(R.id.leftLayout) public LinearLayout leftLayout;
    @BindView(R.id.activityMain_tvUploadCount)
    TextView tvUploadCount;

    private int current = R.id.download;

    @Override
    public int getContentLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init() {
        changeContent();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {//应用没有该权限
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {//是否应该继续显示对话框
//                    //之前请求过拒绝了   返回true
//                    //如果用户在过去拒绝了权限请求，并在权限请求系统对话框中选择了 Don't ask again 选项，此方法将返回 false。如果设备规范禁止应用具有该权限，此方法也会返回 false
//                    new AlertDialog.Builder(this).setTitle("申请权限").setMessage("拍照需要申请相机权限，是否允许?").setPositiveButton("取消", null).setNegativeButton("确定", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            //点击确定的时候再次进行权限的申请
//                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
//                        }
//                    }).show();
//                } else {
//                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
//                }
//            }
//        }

//        File file = new File(UIUtils.getCachePath(), "picture.jpg");
//        File file2 = new File(UIUtils.getCachePath(), "upload.png");
//        File file3 = new File(UIUtils.getCachePath(), "ums.jpg");
//        ArrayList<String> list = new ArrayList<>();
//        list.add(file.getAbsolutePath());
//        list.add(file2.getAbsolutePath());
//        list.add(file3.getAbsolutePath());
//        UploadUtils.INSTANCE.upload(new UploadUtils.UploadListener() {
//            @Override
//            public void onResult(@NotNull Result result) {
//                super.onResult(result);
//                LogPrinter.i("UploadFile", "成功。。。。。");
//            }
//
//            @Override
//            public void onError(@NotNull Throwable e) {
//                super.onError(e);
//                LogPrinter.i("UploadFile", "失敗。。。。" + e.getMessage());
//            }
//        }, list);
    }

    private void changeContent() {
        switch (this.current) {
            case R.id.download:
                replaceShow(new DownloadFragment(), R.id.content);
                break;
            case R.id.upload:
                replaceShow(new UploadFragment(), R.id.content);
                break;
            case R.id.baoyang:
                MainActivityPermissionsDispatcher.showMaintFragmentWithPermissionCheck(this);
                break;
            case R.id.dianjian:
                MainActivityPermissionsDispatcher.showSpotCheckFragmentWithPermissionCheck(this);
                break;
        }
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    void showMaintFragment() {
        replaceShow(new UpKeepFragment(), R.id.content);
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    void showSpotCheckFragment() {
        replaceShow(new SpotCheckFragment(), R.id.content);
    }

    // 用户拒绝授权回调
    @OnPermissionDenied(Manifest.permission.CAMERA)
    void showDeniedForCamera() {
        ToastUtils.show("拒绝开起权限");
//        Toast.makeText(this, R.string.permission_camera_denied, Toast.LENGTH_SHORT).show();
    }

    // 用户勾选了“不再提醒”时调用
    @OnNeverAskAgain(Manifest.permission.CAMERA)
    void showNeverAskForCamera() {
        switch (this.current) {
            case R.id.baoyang:
                showMaintFragment();
                break;
            case R.id.dianjian:
                showSpotCheckFragment();
                break;
        }
    }

    @OnClick({R.id.download, R.id.upload, R.id.baoyang, R.id.dianjian}) void onChange(LinearLayout view) {
        if (view.getId() != this.current) {
            LinearLayout preView = findViewById(this.current);
            preView.setBackground(null);
            this.current = view.getId();
            view.setBackgroundColor(getResources().getColor(R.color.main_color));
            changeContent();
        }
    }

    public void onMenu() {
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

    /**
     * 中英文切换
     */
    public void onChangeLanguage() {
        UserPreferences preferences = new UserPreferences();
        if (preferences.getLanguage().equals(Locale.CHINESE.getLanguage())) {//中文
            preferences.setLanguage(Locale.ENGLISH.getLanguage());
        } else {//英文
            preferences.setLanguage(Locale.CHINESE.getLanguage());
        }
        EventBus.getDefault().post(Constant.EVENT_REFRESH_LANGUAGE);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        switch (requestCode) {
//            case 0:
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // Toast.makeText(MainActivity.this,"申请相机权限成功", Toast.LENGTH_LONG).show();
//                } else {
//                    // Toast.makeText(this,"申请相机权限失败",Toast.LENGTH_LONG).show();
//                }
//                break;
//            default:
//                break;
//        }
//    }

    private long backPressTimeRecord = 0l;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - backPressTimeRecord) < 2000) {
                IntentUtils.INSTANCE.goExit(this);
            } else {
                backPressTimeRecord = currentTime;
                ToastUtils.show(R.string.rt_exit);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUnUploadCount();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    public void updateUnUploadCount() {
        List<FormInfoDetail> formInfoDetails = DataSupport.where("isUpload = ?", String.valueOf(FormInfo.WAIT))
                .find(FormInfoDetail.class, true);

        int count = 0;
        for (int i = 0; i < formInfoDetails.size(); i++) {
            if (formInfoDetails.get(i).form.getStatus().equals(Constant.FORM_STATUS_COMPLETED)) {
                count ++;
            }
        }
        tvUploadCount.setText(String.valueOf(count));
    }
}
