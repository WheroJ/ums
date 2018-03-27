package com.zetavision.panda.ums.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager

/**
 * Created by wheroj on 2018/3/26 16:04.
 * @describe
 */
class ScrollLinearLayoutManager(context:Context, orientation: Int, reverseLayout: Boolean): LinearLayoutManager(context, orientation, reverseLayout){

    private var isScrollEnabled = false

    fun setScrollEnabled(enabled: Boolean) {
        this.isScrollEnabled = enabled
    }
    override fun canScrollVertically(): Boolean {
        return isScrollEnabled&&super.canScrollVertically()
    }

    override fun canScrollHorizontally(): Boolean {
        return isScrollEnabled&&super.canScrollHorizontally()
    }
}