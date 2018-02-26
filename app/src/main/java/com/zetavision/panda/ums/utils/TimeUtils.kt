package com.zetavision.panda.ums.utils

import android.text.TextUtils
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
        val hour = minute/60

        return if (hour == 0L) {
            if (minute == 0L) {
                if (second == 0L) {
                    "0".plus("秒")
                } else {
                    second.toString().plus("秒")
                }
            } else {
                minute.toString().plus("分").plus(second).plus("秒")
            }
        } else {
            hour.toString().plus("时").plus(minute).plus("分").plus(second).plus("秒")
        }
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