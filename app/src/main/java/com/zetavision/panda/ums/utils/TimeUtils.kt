package com.zetavision.panda.ums.utils

import android.text.TextUtils
import com.zetavision.panda.ums.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by wheroj on 2018/2/1 18:25.
 * @describe
 */
object TimeUtils {
    
    fun getUseTime(time: Long): String {
        val currentSecond = System.currentTimeMillis() / 1000 - time
        val minute = currentSecond/60%60
        var second = currentSecond%60
        val hour = currentSecond/60/60

        return " ".plus(if (hour == 0L) {
            if (minute == 0L) {
                if (second == 0L) {
                    "0".plus(UIUtils.getContext().getString(R.string.second))
                } else {
                    second.toString().plus(UIUtils.getContext().getString(R.string.second))
                }
            } else {
                val preferences = UserPreferences()
                if (preferences.language == Locale.CHINESE.language) {//中文
                    minute.toString().plus(UIUtils.getContext().getString(R.string.minute)).plus(second).plus(UIUtils.getContext().getString(R.string.second))
                } else {//英文
                    "0".plus(":").plus(minute).plus(":").plus(second)
                }
            }
        } else {
            val preferences = UserPreferences()
            if (preferences.language == Locale.CHINESE.language) {//中文
                hour.toString().plus(UIUtils.getContext().getString(R.string.hour)).plus(minute).plus(UIUtils.getContext().getString(R.string.minute)).plus(second).plus(UIUtils.getContext().getString(R.string.second))
            } else {//英文
                hour.toString().plus(":").plus(minute).plus(":").plus(second)
            }
        })
    }

    /**
     * @return yyyy-MM-dd HH:mm
     */
    fun getCurrentTime(): String {
        var format = SimpleDateFormat("yyyy-MM-dd HH:mm")
        return format.format(Date(System.currentTimeMillis()))
    }

    /**
     * @param dateStr yyyy-MM-dd
     * @return 当前的秒
     */
    fun getSecond(dateStr: String, format: String = "yyyy-MM-dd"): Long {
        if (TextUtils.isEmpty(dateStr))
            return -1
        var format = SimpleDateFormat(format)
        val date = format.parse(dateStr)
        return date.time/1000
    }

    /**
     * @param dateStr yyyy-MM-dd
     * @return 当前的秒
     */
    fun getSecond(dateStr: String): Long {
        if (TextUtils.isEmpty(dateStr))
            return -1
        val format = SimpleDateFormat("yyyy-MM-dd")
        val date = format.parse(dateStr)
        return date.time/1000
    }
}