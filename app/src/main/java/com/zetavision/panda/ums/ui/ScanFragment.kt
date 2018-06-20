package com.zetavision.panda.ums.ui

import android.text.TextUtils
import android.view.View
import com.seuic.scanner.DecodeInfo
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.fragments.base.BaseFragment
import com.zetavision.panda.ums.service.ScanReceiver
import com.zetavision.panda.ums.utils.IntentUtils
import com.zetavision.panda.ums.utils.ToastUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by wheroj on 2018/6/20 16:20.
 * @describe
 */
class ScanFragment: BaseFragment() {

    private var scanReceiver: ScanReceiver? = null

    /**
     * 默认为点检搜索界面
     */
    private var isSpot = true

    override fun getContentLayoutId(): Int {
        return R.layout.fragment_scan
    }

    override fun init() {
        if (arguments != null) {
            isSpot = arguments.getBoolean("isSpot", true)
        }
        if (isSpot)
            header.setTitle(getString(R.string.spotcheck_scan))
        else
            header.setTitle(getString(R.string.maint_scan))

        contentView.findViewById<View>(R.id.switchInput).setOnClickListener {
            val fragment = SearchFragment()
            fragment.arguments = arguments
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContentView, fragment)
                    .commitAllowingStateLoss()
        }
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
        IntentUtils.unRegisterScanReceiver(scanReceiver, activity)
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)

        scanReceiver = ScanReceiver()
        IntentUtils.registerScanReceiver(scanReceiver!!, activity)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiverCode(info: DecodeInfo) {
        // 处理扫描结果
        if (!TextUtils.isEmpty(info.barcode)) {
            if (isSpot)
                IntentUtils.goSpotCheck(context, info.barcode)
            else
                IntentUtils.goUpKeep(context, info.barcode)
        } else {
            ToastUtils.show(R.string.error_code)
        }
    }
}