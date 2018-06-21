package com.zetavision.panda.ums.ui

import android.graphics.Bitmap
import android.view.View
import com.google.zxing.Result
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.fragments.base.CaptureFragment
import com.zetavision.panda.ums.utils.IntentUtils

/**
 * Created by wheroj on 2018/6/20 16:24.
 * @describe
 */
class CameraFragment: CaptureFragment() {

    /**
     * 默认为点检搜索界面
     */
    private var isSpot = true

    override fun handleDecode(rawResult: Result, barcode: Bitmap, scaleFactor: Float) {
        super.handleDecode(rawResult, barcode, scaleFactor)
        // 处理扫描结果
        if (rawResult.text != "") {
            if (isSpot)
                IntentUtils.goSpotCheck(context, rawResult.text)
            else
                IntentUtils.goUpKeep(context, rawResult.text.trim { it <= ' ' })
        }
    }

    override fun getContentLayoutId(): Int {
        return R.layout.fragment_camera
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
}