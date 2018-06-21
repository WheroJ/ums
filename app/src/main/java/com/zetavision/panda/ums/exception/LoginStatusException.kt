package com.zetavision.panda.ums.exception

import android.text.TextUtils

/**
 * Created by wheroj on 2018/4/24 10:34.
 * @describe
 */
class LoginStatusException(val code: String, msg: String): Exception(msg) {

    fun getIntCode(): Int {
        if (TextUtils.isEmpty(code)) {
            return Int.MIN_VALUE
        }
        return code.toInt()
    }
}