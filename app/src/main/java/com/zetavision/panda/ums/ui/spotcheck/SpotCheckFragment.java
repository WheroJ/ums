package com.zetavision.panda.ums.ui.spotcheck;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.EditText;

import com.google.zxing.Result;
import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.fragments.base.CaptureFragment;
import com.zetavision.panda.ums.utils.IntentUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class SpotCheckFragment extends CaptureFragment {

    @BindView(R.id.switchInput) public View switchInput;
    @BindView(R.id.inputLayout) public View inputLayout;
    @BindView(R.id.device_name) public EditText device_name;

    @Override
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        super.handleDecode(rawResult, barcode, scaleFactor);
        // 处理扫描结果
        IntentUtils.INSTANCE.goSpotCheck(getContext(), rawResult.getText());
    }

    @OnClick({R.id.switchInput, R.id.switchScan})
    public void onSwitch(View view) {
        if (view.getId() == R.id.switchInput) {
            this.inputLayout.setVisibility(View.VISIBLE);
            this.switchInput.setVisibility(View.GONE);
        }
        if (view.getId() == R.id.switchScan) {
            this.inputLayout.setVisibility(View.GONE);
            this.switchInput.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.ok)
    public void onOk() {
        if(!device_name.getText().toString().equals("")) {
            Intent intent = new Intent(getContext(), SpotCheckActivity.class);
            intent.putExtra("device_name", device_name.getText());
            startActivity(intent);
        }
    }

    @Override
    public int getContentLayoutId() {
        return R.layout.fragment_capture;
    }

    @Override
    protected void init() {
        getHeader().setTitle("点检扫描");
    }
}
