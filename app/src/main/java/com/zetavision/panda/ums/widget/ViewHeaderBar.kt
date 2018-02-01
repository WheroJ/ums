package com.zetavision.panda.ums.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.utils.UserPreferences

/**
 * Created by wheroj on 2018/1/31 10:13.
 * @describe
 */
class ViewHeaderBar: RelativeLayout {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, -1)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    private lateinit var mTvTitle: TextView
    private lateinit var mTvUserName: TextView
    private lateinit var mIvBack: ImageView
    private lateinit var mIvLogo: ImageView
    private lateinit var mIvLogout: ImageView
    private lateinit var mViewPick: PackUpLeftView
    private fun initView() {
        View.inflate(context, R.layout.view_header, this)

        mTvTitle = findViewById(R.id.header_title)
        mTvUserName = findViewById(R.id.header_username)
        mIvBack = findViewById(R.id.header_back)
        mIvLogo = findViewById(R.id.header_logo)
        mIvLogout = findViewById(R.id.header_logout)
        mViewPick = findViewById(R.id.header_pickup)

        setDefaultRight()
    }

    fun setTitle(title: String) {
        mTvTitle.text = title
    }

    fun setLeftImage(resId: Int) {
        mIvBack.visibility = View.VISIBLE
        mViewPick.visibility = View.GONE

        mIvBack.setImageResource(resId)
        mIvBack.setOnClickListener {
            if (listener != null)
                listener.onLeftClick()
        }
    }

    fun setLeftPickUp(resId: Int) {
        mIvBack.visibility = View.GONE
        mViewPick.visibility = View.VISIBLE

        mViewPick.setOnClickListener {
            if (listener != null)
                listener.onLeftClick()
        }
    }

    fun setRightText(text: String, color: Int) {
        mIvLogo.visibility = View.GONE
        mIvLogout.visibility = View.INVISIBLE

        mTvUserName.text = text
        mTvUserName.setTextColor(resources.getColor(color))
        mTvUserName.visibility = View.VISIBLE
        if (listener != null)
            mTvUserName.setOnClickListener {
                listener.onRightTextClick()
            }
    }

    fun setRightImgage(resId: Int) {
        mIvLogo.visibility = View.GONE
        mIvLogout.visibility = View.VISIBLE
        mTvUserName.visibility = View.GONE

        mIvLogout.setImageResource(resId)
    }

    fun setDefaultRight(text: String? = null) {
        if (!TextUtils.isEmpty(text)) {
            mTvUserName.text = text
        } else {
            var preferences = UserPreferences()
            val user = preferences.user
            if (user != null) {
                mTvUserName.text = (user!!.username)
            }
        }

        mViewPick.setOnClickListener {
            if (listener != null)
                listener.onLeftClick()
        }

        mIvLogout.setOnClickListener {
            if (listener != null)
                listener.onLogoutClick()
        }
    }

    interface OnItemClickListener {
        fun onLeftClick()
        fun onLogoutClick()
        fun onRightTextClick()
    }

    private lateinit var listener: OnItemClickListener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
}