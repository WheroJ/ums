package com.zetavision.panda.ums.widget

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.utils.UIUtils

/**
 * Created by wheroj on 2018/2/6 10:56.
 * @describe
 */
abstract class PopTakePicture(context: Context): PopupWindow() {

    private var btnTake: Button
    init {
        contentView = View.inflate(context, R.layout.pop_takepicture, null)
        btnTake = contentView.findViewById(R.id.popTakePicure_btnTake)

        initView()
    }

    private fun initView() {
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        setBackgroundDrawable(UIUtils.getContext().resources.getDrawable(R.drawable.transparent))
        this.isTouchable = true
        this.isOutsideTouchable = false
        btnTake.setOnClickListener {
            onTakePicture()
        }
    }

    abstract fun onTakePicture()
}