package com.zetavision.panda.ums.ui;

import android.Manifest;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.TextAppearanceSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.base.BaseActivity;
import com.zetavision.panda.ums.model.FormInfo;
import com.zetavision.panda.ums.model.FormInfoDetail;
import com.zetavision.panda.ums.ui.formdownload.DownloadFragment;
import com.zetavision.panda.ums.ui.formup.UploadFragment;
import com.zetavision.panda.ums.utils.Common;
import com.zetavision.panda.ums.utils.Constant;
import com.zetavision.panda.ums.utils.IntentUtils;
import com.zetavision.panda.ums.utils.ToastUtils;
import com.zetavision.panda.ums.utils.UIUtils;
import com.zetavision.panda.ums.utils.UserPreferences;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
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
            case R.id.activityMain_tvInProgress:
                FormListFragment progressFragment = new FormListFragment();
                Bundle bundle = new Bundle();
                bundle.putString("actionType", FormInfo.ACTION_TYPE_P);
                bundle.putString("status", Constant.FORM_STATUS_INPROGRESS);
                progressFragment.setArguments(bundle);
                replaceShow(progressFragment, R.id.content);
                break;
            case R.id.activityMain_tvDownloaded:
                FormListFragment downListFragment = new FormListFragment();
                Bundle bundle2 = new Bundle();
                bundle2.putString("actionType", FormInfo.ACTION_TYPE_P);
                bundle2.putString("status", Constant.FORM_STATUS_ALL);
                downListFragment.setArguments(bundle2);
                replaceShow(downListFragment, R.id.content);
                break;
        }
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    void showMaintFragment() {
        SearchFragment fragment = new SearchFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isSpot", false);
        fragment.setArguments(bundle);
        replaceShow(fragment, R.id.content);
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    void showSpotCheckFragment() {
        replaceShow(new SearchFragment(), R.id.content);
    }

    // 用户拒绝授权回调
    @OnPermissionDenied(Manifest.permission.CAMERA)
    void showDeniedForCamera() {
        ToastUtils.show(getString(R.string.refuse_permission));
        onChange(findViewById(preCheckId));
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

    @OnClick({R.id.download, R.id.upload, R.id.baoyang, R.id.dianjian
            , R.id.activityMain_tvInProgress, R.id.activityMain_tvDownloaded})
    void onChange(View view) {
        if (view.getId() != this.current) {
            View currentView = findViewById(this.current);
            currentView.setBackground(null);
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

        Observable.create(new ObservableOnSubscribe<HashMap<String, Integer>>() {
            @Override
            public void subscribe(ObservableEmitter<HashMap<String, Integer>> emitter) throws Exception {
                List<FormInfoDetail> formInfoDetails = DataSupport.where("isUpload = ?", String.valueOf(FormInfo.WAIT))
                        .find(FormInfoDetail.class, false);
                HashMap<String, Integer> hashMap = new HashMap<>();
                int completeCount = 0, inProgressCount = 0, downloadedCount;

                for (FormInfoDetail formInfoDetail : formInfoDetails) {
                    completeCount += DataSupport.where("formId = ? and status='" + Constant.FORM_STATUS_COMPLETED + "'"
                            , formInfoDetail.formId).count(FormInfo.class);
                    inProgressCount += DataSupport.where("formId = ? and status='" + Constant.FORM_STATUS_INPROGRESS + "'"
                            , formInfoDetail.formId).count(FormInfo.class);
                }
                downloadedCount = DataSupport.count(FormInfoDetail.class);
                hashMap.put("downloadedCount", downloadedCount);
                hashMap.put("completeCount", completeCount);
                hashMap.put("inProgressCount", inProgressCount);
                emitter.onNext(hashMap);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<HashMap<String, Integer>>() {
                    @Override
                    public void accept(HashMap<String, Integer> hashMap) throws Exception {
                        tvWaitUpload.setText(getSpannableString(TYPE_UPLOAD, hashMap.get("completeCount")), TextView.BufferType.SPANNABLE);
                        tvInProgress.setText(getSpannableString(TYPE_INPROGRESS, hashMap.get("inProgressCount")));
                        tvUploadCount.setText(String.valueOf(hashMap.get("completeCount")),  TextView.BufferType.SPANNABLE);
                        tvDownloaded.setText(getSpannableString(TYPE_DOWN, hashMap.get("downloadedCount")), TextView.BufferType.SPANNABLE);
                    }
                });
    }
}
