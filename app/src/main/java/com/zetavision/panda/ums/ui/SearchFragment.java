package com.zetavision.panda.ums.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.fragments.base.BaseFragment;
import com.zetavision.panda.ums.utils.IntentUtils;
import com.zetavision.panda.ums.utils.ToastUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class SearchFragment extends BaseFragment {

    @BindView(R.id.device_name) public EditText device_name;

    /**
     * 默认为点检搜索界面
     */
    private boolean isSpot = true;

    @OnClick({R.id.switchScan, R.id.switchCamera})
    public void onSwitch(View view) {
        BaseFragment fragment = null;
        switch (view.getId()) {
            case R.id.switchScan:
                fragment = new ScanFragment();
                break;
            case R.id.switchCamera:
                fragment = new CameraFragment();
                break;
        }
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContentView, fragment)
                .commitAllowingStateLoss();
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

        // 程序打开的时候调用服务，只用调用一次即可，不需要在每个activity中都调用
//        IntentUtils.INSTANCE.startScanService(getActivity());
    }
}
