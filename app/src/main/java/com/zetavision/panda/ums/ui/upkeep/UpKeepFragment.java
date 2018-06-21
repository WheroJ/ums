package com.zetavision.panda.ums.ui.upkeep;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.seuic.scanner.DecodeInfo;
import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.fragments.base.BaseFragment;
import com.zetavision.panda.ums.service.ScanReceiver;
import com.zetavision.panda.ums.utils.IntentUtils;
import com.zetavision.panda.ums.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;

public class UpKeepFragment extends BaseFragment {

    @BindView(R.id.switchInput) public View switchInput;
    @BindView(R.id.inputLayout) public View inputLayout;
    @BindView(R.id.device_name) public EditText device_name;

    /**
     * 默认处理二维码扫描结果
     */
    private boolean dealDecode = true;
    private ScanReceiver scanReceiver = null;

//    @Override
//    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
//        super.handleDecode(rawResult, barcode, scaleFactor);
//        if (dealDecode) {
//            // 处理扫描结果
//            if (!rawResult.getText().equals("")) {
//                IntentUtils.INSTANCE.goUpKeep(getContext(), rawResult.getText().trim());
//            }
//        }
//    }

    @OnClick({R.id.switchInput, R.id.switchScan})
    public void onSwitch(View view) {
        if (view.getId() == R.id.switchInput) {
            this.inputLayout.setVisibility(View.VISIBLE);
            this.switchInput.setVisibility(View.GONE);
            dealDecode = false;
        }
        if (view.getId() == R.id.switchScan) {
            this.inputLayout.setVisibility(View.GONE);
            this.switchInput.setVisibility(View.VISIBLE);
            dealDecode = true;
        }
    }

    @OnClick(R.id.ok)
    public void onOk() {
        if(!device_name.getText().toString().equals("")) {
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
        getHeader().setTitle(getString(R.string.maint_scan));
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
        if (dealDecode) {
            // 处理扫描结果
            if (!TextUtils.isEmpty(info.barcode))
                IntentUtils.INSTANCE.goUpKeep(getContext(), info.barcode);
            else
                ToastUtils.show(R.string.error_code);
        }
    }
}
