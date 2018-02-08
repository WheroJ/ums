package com.zetavision.panda.ums.widget

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.utils.UIUtils

/**
 * Created by wheroj on 2018/2/6 10:56.
 * @describe
 */
abstract class PopVideoPicture(context: Context): PopupWindow() {

    private var tvCancel: TextView
    private var tvTakePicture: TextView
    private var tvTakeVideo: TextView
    init {
        contentView = View.inflate(context, R.layout.pop_takevideo, null)
        tvCancel = contentView.findViewById(R.id.popTakeVideo_tvCancel)
        tvTakePicture = contentView.findViewById(R.id.popTakeVideo_tvTakePicture)
        tvTakeVideo = contentView.findViewById(R.id.popTakeVideo_tvTakeVideo)

        tvCancel.setOnClickListener {
            dismiss()
        }

        tvTakePicture.setOnClickListener {
            onTakePicture()
        }

        tvTakeVideo.setOnClickListener {
            onTakeVideo()
        }
        initView()
    }

    private fun initView() {
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        setBackgroundDrawable(UIUtils.getContext().resources.getDrawable(R.drawable.transparent))
        this.isTouchable = true
        this.isOutsideTouchable = false
    }

    abstract fun onTakePicture()
    abstract fun onTakeVideo()
}