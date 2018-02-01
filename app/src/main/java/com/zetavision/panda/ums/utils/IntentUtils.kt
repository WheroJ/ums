package com.zetavision.panda.ums.utils

import android.app.Activity
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import com.zetavision.panda.ums.base.BaseActivity
import com.zetavision.panda.ums.service.UmsService
import com.zetavision.panda.ums.ui.LoginActivity
import com.zetavision.panda.ums.ui.MainActivity
import com.zetavision.panda.ums.ui.spotcheck.SpotCheckActivity
import com.zetavision.panda.ums.ui.upkeep.UpKeepActivity
import com.zetavision.panda.ums.ui.upkeep.UpKeepDetailActivity

/**
 * Created by wheroj on 2018/1/30.
 * @describeb
 */
object IntentUtils {

    fun goUpKeep(context: Context, deviceName: String) {
        val intent = Intent(context, UpKeepActivity::class.java)
        intent.putExtra("deviceName", deviceName)
        context.startActivity(intent)
    }

    fun startDownloadService(activity: Activity, connection: ServiceConnection) {
        val intent = Intent(activity, UmsService::class.java)
        activity.bindService(intent, connection, BIND_AUTO_CREATE)
    }

    fun goLogout(context: Context) {
        val login = Intent(context, LoginActivity::class.java)
        login.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(login)
    }

    fun goUpKeepDetail(context: Context, formId: String) {
        val intent = Intent(context, UpKeepDetailActivity::class.java)
        intent.putExtra("maintFormId", formId)
        context.startActivity(intent)
    }

    fun goMain(context: BaseActivity) {
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
        context.finish()
    }

    fun goSpotCheck(context: Context, deviceName: String) {
        val intent = Intent(context, SpotCheckActivity::class.java)
        intent.putExtra("deviceName", deviceName)
        context.startActivity(intent)
    }
}