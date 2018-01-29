package com.zetavision.panda.ums.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.zxing.Result;
import com.zetavision.panda.ums.DianjianActivity;
import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.fragments.base.CaptureFragment;

import butterknife.BindView;
import butterknife.OnClick;

public class DianjianFragment extends CaptureFragment {

    @BindView(R.id.switchInput) public View switchInput;
    @BindView(R.id.inputLayout) public View inputLayout;
    @BindView(R.id.device_name) public EditText device_name;

    @Override
    protected View createView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_capture, null);
    }

    @Override
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        super.handleDecode(rawResult, barcode, scaleFactor);
        // 处理扫描结果
        Intent intent = new Intent(getContext(), DianjianActivity.class);
        intent.putExtra("device_name", rawResult.getText());
        startActivity(intent);
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
            Intent intent = new Intent(getContext(), DianjianActivity.class);
            intent.putExtra("device_name", device_name.getText());
            startActivity(intent);
        }
    }
}
