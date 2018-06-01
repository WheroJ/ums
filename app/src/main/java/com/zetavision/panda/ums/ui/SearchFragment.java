package com.zetavision.panda.ums.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;

import com.google.zxing.Result;
import com.seuic.scanner.DecodeInfo;
import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.fragments.base.CaptureFragment;
import com.zetavision.panda.ums.service.ScanReceiver;
import com.zetavision.panda.ums.utils.IntentUtils;
import com.zetavision.panda.ums.utils.ToastUtils;
import com.zetavision.panda.ums.zxing.ViewfinderView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;

public class SearchFragment extends CaptureFragment {

    @BindView(R.id.switchInput) public View switchInput;
    @BindView(R.id.inputLayout) public View inputLayout;
    @BindView(R.id.device_name) public EditText device_name;
    @BindView(R.id.preview_view) SurfaceView surfaceView;
    @BindView(R.id.finderView) ViewfinderView finderView;

    private final int TYPE_SCAN = 1;
    private final int TYPE_CAMERA = 2;

    /**
     * 使用哪种类型扫描
     */
    private int scanType = -1;
    private ScanReceiver scanReceiver = null;

    /**
     * 默认为点检搜索界面
     */
    private boolean isSpot = true;

    @Override
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        super.handleDecode(rawResult, barcode, scaleFactor);
        if (scanType == TYPE_CAMERA) {
            // 处理扫描结果
            if (!rawResult.getText().equals("")) {
                if (isSpot)
                    IntentUtils.INSTANCE.goSpotCheck(getContext(), rawResult.getText());
                else
                    IntentUtils.INSTANCE.goUpKeep(getContext(), rawResult.getText().trim());
            }
        }
    }

    @OnClick({R.id.switchInput, R.id.switchScan, R.id.switchCamera})
    public void onSwitch(View view) {
        switch (view.getId()) {
            case R.id.switchInput:
                this.inputLayout.setVisibility(View.VISIBLE);
                this.switchInput.setVisibility(View.GONE);
                scanType = -1;
                break;
            case R.id.switchScan:
                this.inputLayout.setVisibility(View.GONE);
                this.switchInput.setVisibility(View.VISIBLE);
                scanType = TYPE_SCAN;
                break;
            case R.id.switchCamera:
                this.inputLayout.setVisibility(View.GONE);
                this.switchInput.setVisibility(View.VISIBLE);
                scanType = TYPE_CAMERA;
                break;
        }
        updateView();
    }

    @OnClick(R.id.ok)
    public void onOk() {
        if(!device_name.getText().toString().equals("")) {
            if (isSpot)
                IntentUtils.INSTANCE.goSpotCheck(getContext(), device_name.getText().toString());
            else
                IntentUtils.INSTANCE.goUpKeep(getContext(), device_name.getText().toString());
        } else {
            ToastUtils.show(getString(R.string.device_name_notnull));
        }
    }

    @Override
    public int getContentLayoutId() {
        return R.layout.fragment_capture;
    }

    @Override
    protected void init() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            isSpot = arguments.getBoolean("isSpot", true);
        }

        if (isSpot)
            getHeader().setTitle(getString(R.string.spotcheck_scan));
        else
            getHeader().setTitle(getString(R.string.maint_scan));

        updateView();

        // 程序打开的时候调用服务，只用调用一次即可，不需要在每个activity中都调用
//        IntentUtils.INSTANCE.startScanService(getActivity());
    }

    private void updateView() {
        if (scanType == TYPE_CAMERA) {
            surfaceView.setVisibility(View.VISIBLE);
            finderView.setVisibility(View.VISIBLE);
//            initCamera(surfaceView.getHolder());
        } else {
            surfaceView.setVisibility(View.INVISIBLE);
            finderView.setVisibility(View.GONE);
//            closeCamera();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        IntentUtils.INSTANCE.unRegisterScanReceiver(scanReceiver, getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);

        scanReceiver = new ScanReceiver();
        IntentUtils.INSTANCE.registerScanReceiver(scanReceiver, getActivity());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverCode(DecodeInfo info) {
        if (scanType == TYPE_SCAN) {
            // 处理扫描结果
            if (!TextUtils.isEmpty(info.barcode)) {
                if (isSpot)
                    IntentUtils.INSTANCE.goSpotCheck(getContext(), info.barcode);
                else
                    IntentUtils.INSTANCE.goUpKeep(getContext(), info.barcode);
            } else {
                ToastUtils.show(R.string.error_code);
            }
        }
    }
}
