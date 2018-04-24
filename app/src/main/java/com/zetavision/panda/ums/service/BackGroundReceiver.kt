package com.zetavision.panda.ums.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zetavision.panda.ums.model.Result
import com.zetavision.panda.ums.ui.LoginActivity
import com.zetavision.panda.ums.utils.Constant
import com.zetavision.panda.ums.utils.SPUtil
import com.zetavision.panda.ums.utils.network.UploadUtils
import java.io.File


/**
 * Created by wheroj on 2018/3/12 16:06.
 * @describe
 */
class BackGroundReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val intent2 = Intent(context, LoginActivity::class.java)
            intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent2)
        } else if (intent.action == Constant.ACTION_UPLOAD_LOG) {
//            uploadCrashLog()
        }
    }

    /**
     * 上传崩溃日志
     */
    private fun uploadCrashLog() {
        val crashPaths:ArrayList<String>  = SPUtil.getObject(Constant.WAIT_UPLOAD_CRASH_LOG, ArrayList())
        val uploadUtils = UploadUtils()
        uploadUtils.uploadFiles(object : UploadUtils.UploadListener() {
            override fun onResult(result: Result) {
                super.onResult(result)
                val copyArray = ArrayList<String>()
                copyArray.addAll(crashPaths)
                copyArray.mapNotNull {
                    val file = File(it)
                    if (!file.exists())
                        crashPaths.remove(it)
                }
                SPUtil.saveObject(Constant.WAIT_UPLOAD_CRASH_LOG, crashPaths)
            }
        }, crashPaths)
    }
}
