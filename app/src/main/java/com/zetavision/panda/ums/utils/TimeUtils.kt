package com.zetavision.panda.ums.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by wheroj on 2018/2/1 18:25.
 * @describe
 */
object TimeUtils {
    
    fun getUseTime(time: Long): String {
        val currentSecond = System.currentTimeMillis() / 1000 - time
        val minute = currentSecond/60
        var second = currentSecond%60
        val hour = minute/60

        return hour.toString().plus("时").plus(minute).plus("分").plus(second).plus("秒")
    }

    /**
     * @return yyyy-MM-dd HH:mm
     */
    fun getCurrentTime(): String {
        var format = SimpleDateFormat("yyyy-MM-dd HH:mm")
        return format.format(Date(System.currentTimeMillis()))
    }
}