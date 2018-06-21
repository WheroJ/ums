package com.zetavision.panda.ums.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.zetavision.panda.ums.R

/**
 * Created by wheroj on 2018/1/31 10:39.
 * @describe
 */
class PackUpLeftView: LinearLayout {
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, -1)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    private fun initView() {
        View.inflate(context, R.layout.view_pickup, this)
    }
}