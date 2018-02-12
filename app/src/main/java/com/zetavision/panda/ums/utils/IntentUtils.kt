@file:Suppress("UNREACHABLE_CODE")

package com.zetavision.panda.ums.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.provider.MediaStore
import com.zetavision.panda.ums.base.BaseActivity
import com.zetavision.panda.ums.model.FormInfo
import com.zetavision.panda.ums.service.UmsService
import com.zetavision.panda.ums.ui.LoginActivity
import com.zetavision.panda.ums.ui.MainActivity
import com.zetavision.panda.ums.ui.spotcheck.SpotCheckDetailActivity
import com.zetavision.panda.ums.ui.spotcheck.SpotCheckListActivity
import com.zetavision.panda.ums.ui.upkeep.UpKeepDetailActivity
import com.zetavision.panda.ums.ui.upkeep.UpKeepListActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by wheroj on 2018/1/30.
 * @describeb
 */
object IntentUtils {

    fun goUpKeep(context: Context, deviceName: String) {
        val intent = Intent(context, UpKeepListActivity::class.java)
        intent.putExtra("deviceName", deviceName)
        intent.putExtra("actionType", FormInfo.ACTION_TYPE_M)
        context.startActivity(intent)
    }

    fun bindService(context: Context, connection: ServiceConnection) {
        val intent = Intent(context, UmsService::class.java)
        context.bindService(intent, connection, BIND_AUTO_CREATE)
    }

    fun goLogout(context: Context) {
        val login = Intent(context, LoginActivity::class.java)
        login.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        if (context !is Activity) {
            login.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        login.putExtra(Constant.RE_LOGIN, Constant.RE_LOGIN)
        context.startActivity(login)
        IntentUtils.clearBuffer(true)
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
        val intent = Intent(context, SpotCheckListActivity::class.java)
        intent.putExtra("deviceName", deviceName)
        intent.putExtra("actionType", FormInfo.ACTION_TYPE_P)
        context.startActivity(intent)
    }

    fun reOpenActivity(activity: BaseActivity) {
        activity.finish()
        val intent = Intent(activity, activity.javaClass)
        activity.startActivity(intent)
    }

    /**
     * 退出整个应用
     * @param activity
     */
    fun goExit(activity: BaseActivity) {
        IntentUtils.clearBuffer(false)
        activity.finish()
        System.exit(0)
    }

    fun stopServcie(context: Context) {
        val intent = Intent(context, UmsService::class.java)
        context.stopService(intent)
        LogPrinter.i("StopService", "服务被停止了=======")
    }

    /**
     * 开启token判断过期机制的service
     */
    fun startReLoginService() {
        val context = UIUtils.getContext()
        val intent = Intent(context, UmsService::class.java)
        context.startService(intent)
        LogPrinter.i("StartService", "服务被开启了=======")
    }

    fun goReLogin(context: Context?) {
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context!!.startActivity(intent)
        IntentUtils.clearBuffer(true)
    }

    /**
     * 清除缓存
     *
     * 目前需要清理的是：1，UmsService 是否初始化的状态保存
     *
     * @param logout  是否启用退出登录
     */
    fun clearBuffer(logout: Boolean) {
        LogPrinter.i("CLEAR_COOKIE", "清除缓存。。。")
        IntentUtils.stopServcie(UIUtils.getContext())
        if (logout) {
            UserUtils.clearLogin()
            var userPreferences = UserPreferences()
            userPreferences.clearCookie()
        }
    }

    /**
     * 点检详情
     */
    fun goSpotCheckDetail(context: Context, formId: String?) {
        val intent = Intent(context, SpotCheckDetailActivity::class.java)
        intent.putExtra("maintFormId", formId)
        context.startActivity(intent)
    }

    /**
     * 打开照相机照像并获取图片的名字和对应地址
     *
     * @param thisContext
     * @param requestCode 请求码
     * @param position item的位置
     * @return
     */
    fun loadImgFromCamera(thisContext: Activity, position:Int, requestCode: Int): String? {
        // 拍照
        try {
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            val sdcardPath = UIUtils.getExtraStoragePath() ?: return null
            var mPhotoPath = sdcardPath + File.separator + "DCIM/Camera"
            val file = File(mPhotoPath)
            if (!file.exists()) {
                file.mkdirs()
            }
            val mPhotoFile = File(file, getPhotoFileName())
            if (!mPhotoFile.exists()) {
                mPhotoFile.createNewFile()
            }
            mPhotoPath = mPhotoFile.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile))
            intent.putExtra(Constant.PHOTOPATH, mPhotoPath)
            intent.putExtra("position", position)
            thisContext.startActivityForResult(intent, requestCode)
            return mPhotoPath
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }

    }

    /**
     * 用时间戳生成照片名称
     *
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    private fun getPhotoFileName(): String {
        val date = Date(System.currentTimeMillis())
        val dateFormat = SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss")
        return dateFormat.format(date) + ".jpg"
    }
}