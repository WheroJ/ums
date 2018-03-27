package com.zetavision.panda.ums.ui;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.TextAppearanceSpan;
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
import com.zetavision.panda.ums.utils.SPUtil;
import com.zetavision.panda.ums.utils.ToastUtils;
import com.zetavision.panda.ums.utils.UIUtils;
import com.zetavision.panda.ums.utils.UserPreferences;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
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

    @BindView(R.id.activityMain_tvDownloaded)
    TextView tvDownloaded;
    @BindView(R.id.activityMain_tvInProgress)
    TextView tvInProgress;
    @BindView(R.id.activityMain_tvWaitUpload)
    TextView tvWaitUpload;

    private int current = R.id.download;
    private int preCheckId = current;
    private final int TYPE_DOWN = 1, TYPE_UPLOAD = 2, TYPE_INPROGRESS = 3;

    @Override
    public int getContentLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init() {
        changeContent();

        uploadCrashLog();
    }

    private void uploadCrashLog() {
        ArrayList<String> crashFiles = SPUtil.getObject(Constant.WAIT_UPLOAD_CRASH_LOG, new ArrayList<String>());
        if (!crashFiles.isEmpty()) {
            Intent intent = new Intent();
            intent.setAction(Constant.ACTION_UPLOAD_LOG);
            sendBroadcast(intent);
        }
    }

    private SpannableString getSpannableString(int type, int count) {
        SpannableString content = null;
        int firstEnd = 0;
        switch (type) {
            case TYPE_DOWN:
                content = new SpannableString(getString(R.string.info_downloaded, String.valueOf(count)));
                firstEnd = 3;
                break;
            case TYPE_INPROGRESS:
                content = new SpannableString(getString(R.string.info_inprogress, String.valueOf(count)));
                firstEnd = 1;
                break;
            case TYPE_UPLOAD:
                content = new SpannableString(getString(R.string.info_waitupload, String.valueOf(count)));
                firstEnd = 1;
                break;
        }
        if (content != null) {
            int length = String.valueOf(count).length();
            content.setSpan(new TextAppearanceSpan(this, R.style.text1), 0, firstEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            content.setSpan(new TextAppearanceSpan(this, R.style.text2), firstEnd, firstEnd + length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            content.setSpan(new AbsoluteSizeSpan(UIUtils.dip2px(16)), firstEnd + length, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return content;
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

    private long backPressTimeRecord = 0L;
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
        updateCount();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onMessageEvent(String str) {
        super.onMessageEvent(str);
        if (Constant.UPDATE_DOWN_COUNT.equals(str) || Constant.UPDATE_WAIT_UPLOAD_COUNT.equals(str)) {
            updateCount();
        }
    }

    public void updateCount() {
        List<FormInfoDetail> formInfoDetails = DataSupport.where("isUpload = ?", String.valueOf(FormInfo.WAIT))
                .find(FormInfoDetail.class, true);
//        List<FormInfoDetail> formInfoDetails = DataSupport.findAll(FormInfoDetail.class, true);

        int completeCount = 0, inProgressCount = 0;
        for (int i = 0; i < formInfoDetails.size(); i++) {
            if (formInfoDetails.get(i).form.getStatus().equals(Constant.FORM_STATUS_COMPLETED)) {
                completeCount ++;
            } else if (formInfoDetails.get(i).form.getStatus().equals(Constant.FORM_STATUS_INPROGRESS)) {
                inProgressCount ++;
            }
        }
        tvWaitUpload.setText(getSpannableString(TYPE_UPLOAD, completeCount), TextView.BufferType.SPANNABLE);
        tvInProgress.setText(getSpannableString(TYPE_INPROGRESS, inProgressCount));
        tvUploadCount.setText(String.valueOf(completeCount),  TextView.BufferType.SPANNABLE);

        int downloadedCount = DataSupport.count(FormInfoDetail.class);
        tvDownloaded.setText(getSpannableString(TYPE_DOWN, downloadedCount), TextView.BufferType.SPANNABLE);
    }
}
