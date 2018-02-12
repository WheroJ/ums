package com.zetavision.panda.ums.ui.spotcheck;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.EditText;

import com.google.zxing.Result;
import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.fragments.base.CaptureFragment;
import com.zetavision.panda.ums.utils.IntentUtils;
import com.zetavision.panda.ums.utils.ToastUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class SpotCheckFragment extends CaptureFragment {

    @BindView(R.id.switchInput) public View switchInput;
    @BindView(R.id.inputLayout) public View inputLayout;
    @BindView(R.id.device_name) public EditText device_name;

    /**
     * 默认处理二维码扫描结果
     */
    private boolean dealDecode = true;

    @Override
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        super.handleDecode(rawResult, barcode, scaleFactor);
        if (dealDecode) {
            // 处理扫描结果
            if (!rawResult.getText().equals(""))
                IntentUtils.INSTANCE.goSpotCheck(getContext(), rawResult.getText());
        }
    }

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
            IntentUtils.INSTANCE.goSpotCheck(getContext(), device_name.getText().toString());
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
        getHeader().setTitle(getString(R.string.spotcheck_scan));
    }
}
