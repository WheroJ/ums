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
    private int preCheckId = current;


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

//        File file = new File(UIUtils.getCachePath(), "photo_1518060307109.png");
//        File file4 = new File(UIUtils.getCachePath(), "photo_1518060427235.png");
//        File file5 = new File(UIUtils.getCachePath(), "photo_1518060937237.png");
//        File file6 = new File(UIUtils.getCachePath(), "photo_1518061946096.png");
//        File file3 = new File(UIUtils.getCachePath(), "photo_1518062099503.png");
//        File file12 = new File(UIUtils.getCachePath(), "photo_15180603071092.png");
//        File file13 = new File(UIUtils.getCachePath(), "photo_15180604272352.png");
//        File file14 = new File(UIUtils.getCachePath(), "photo_15180609372372.png");
//        File file15 = new File(UIUtils.getCachePath(), "photo_15180619460962.png");
//        File file16 = new File(UIUtils.getCachePath(), "photo_15180620995032.png");
////        File file7 = new File(UIUtils.getCachePath(), "photo_1518062505236.png");
////        File file8 = new File(UIUtils.getCachePath(), "photo_1518069128199.png");
////        File file10 = new File(UIUtils.getCachePath(), "photo_1518070151948.png");
//        File file2 = new File(UIUtils.getCachePath(), "upload.png");
////        File file9 = new File(UIUtils.getCachePath(), "ums.png");
////        File file11 = new File(UIUtils.getCachePath(), "picture.png");
//        System.out.println("地址：" + file2.getAbsolutePath());
//        ArrayList<String> list = new ArrayList<>();
//        list.add(file.getAbsolutePath());
//        list.add(file2.getAbsolutePath());
//        list.add(file3.getAbsolutePath());
//        list.add(file4.getAbsolutePath());
//        list.add(file5.getAbsolutePath());
//        list.add(file6.getAbsolutePath());
////        list.add(file7.getAbsolutePath());
////        list.add(file8.getAbsolutePath());
////        list.add(file9.getAbsolutePath());
////        list.add(file10.getAbsolutePath());
////        list.add(file11.getAbsolutePath());
//        list.add(file12.getAbsolutePath());
//        list.add(file13.getAbsolutePath());
//        list.add(file14.getAbsolutePath());
//        list.add(file15.getAbsolutePath());
//        list.add(file16.getAbsolutePath());
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
//        }, list, "4C1-CA-ADH01-180227-PM-002");
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
        ToastUtils.show(getString(R.string.refuse_permission));
        onChange((LinearLayout) findViewById(preCheckId));
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
            preCheckId = current;
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
