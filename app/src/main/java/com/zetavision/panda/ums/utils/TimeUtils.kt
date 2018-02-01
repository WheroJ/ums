package com.zetavision.panda.ums.utils

/**
 * Created by wheroj on 2018/2/1 18:25.
 * @describe
 */
object TimeUtils {
    
    fun getUseTime(time: Long): String {
        val currentSecond = time - System.currentTimeMillis() / 1000
        val minute = currentSecond%60
        val hour = currentSecond/60
        var second = currentSecond%60

        return hour.toString().plus("时").plus(minute).plus("分").plus(second).plus("秒")
    }
}