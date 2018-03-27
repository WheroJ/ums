package com.zetavision.panda.ums.ui

import android.text.TextUtils
import android.widget.ImageView
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.base.BaseActivity
import com.zetavision.panda.ums.utils.BitmapUtils
import com.zetavision.panda.ums.utils.UIUtils

/**
 * Created by wheroj on 2018/3/7 16:45.
 * @describe
 */
class ImageViewerActivity: BaseActivity() {

    override fun getHasTitle(): Boolean {
        return true
    }

    override fun getContentLayoutId(): Int {
        return R.layout.activity_imageviewer
    }

    override fun init() {
        var photoPath = intent.getStringExtra("photoPath")
        if (!TextUtils.isEmpty(photoPath)) {
            val resource = BitmapUtils.decodeSampledBitmapFromResource(photoPath, UIUtils.getWinWidth(), UIUtils.getWinHeight())
            findViewById<ImageView>(R.id.image_viewer).setImageBitmap(resource)
        } else {
            findViewById<ImageView>(R.id.image_viewer).setImageResource(R.mipmap.icon_default_order_big)
        }

        header.setTitle(getString(R.string.photo_detail))
        header.setLeftImage(R.mipmap.back)
        header.setHiddenRight()
    }

}