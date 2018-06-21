package com.zetavision.panda.ums.widget

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Spinner



/**
 * Created by wheroj on 2018/6/19 16:15.
 * @describe
 */
class ClickControlledSpinner : Spinner {

    private var isMoved = false
    private val touchedPoint = Point()


    private var onClickMyListener: OnClickMyListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private var expend = true
    override fun onTouchEvent(event: MotionEvent): Boolean {
//        val x = event.rawX.toInt()
//        val y = event.rawY.toInt()
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                touchedPoint.x = x
//                touchedPoint.y = y
//            }
//            MotionEvent.ACTION_MOVE -> isMoved = true
//            MotionEvent.ACTION_CANCEL,
//            MotionEvent.ACTION_UP ->
//                if (isMoved) {
//                    // 从上向下滑动
//                    when {
//                        y - touchedPoint.y > 20 -> {
//                        }
//                        touchedPoint.y - y > 20 -> {
//                        }
//                        else -> expend = onClick()
//                    }
//                    isMoved = false
//                } else {
//                    expend = onClick()
//                }
//            else -> {}
//
//        }
//        return if(expend) expend else super.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private fun onClick():Boolean{
        if (onClickMyListener != null && isEnabled) {
            return onClickMyListener!!.onClick()
        }
        return false
    }

    /**
     * 注册自定义的点击事件监听
     * Register the click event self-fulfilling listener.
     * @param onClickMyListener
     */
    fun setOnClickMyListener(onClickMyListener: OnClickMyListener) {
        this.onClickMyListener = onClickMyListener
    }

    /**
     * 自定义点击事件监听.
     * Click event self-fulfilling listener.
     * @author Wison Xu
     */
    interface OnClickMyListener {
        /**
         * 点击时触发
         * 警告：该方法在非UI线程中执行
         *
         * Triggers when click event occurs.
         * Warning: this method does not run in UI thread.
         */
        fun onClick(): Boolean
    }
}
