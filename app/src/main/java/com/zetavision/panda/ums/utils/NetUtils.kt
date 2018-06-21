package com.zetavision.panda.ums.utils

import android.content.Context
import android.net.ConnectivityManager



/**
 * Created by wheroj on 2018/2/2 17:35.
 * @describe
 */
object NetUtils {
    fun isNetConnect(context: Context):Boolean {
        val manager = UIUtils.getContext().getSystemService(
                Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkinfo = manager.activeNetworkInfo

        return !(networkinfo == null || !networkinfo.isAvailable)

        return true
    }
}