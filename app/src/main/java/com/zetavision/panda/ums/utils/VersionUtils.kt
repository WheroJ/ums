package com.zetavision.panda.ums.utils

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.TextUtils
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.model.Result
import com.zetavision.panda.ums.utils.network.Client
import com.zetavision.panda.ums.utils.network.RxUtils
import com.zetavision.panda.ums.utils.network.UmsApi
import org.json.JSONObject
import java.io.File


/**
 * Created by wheroj on 2018/6/6 11:11.
 * @describe
 */
class VersionUtils(val mContext: AppCompatActivity) {
    fun checkVersion() {
        val observable = Client.getApi(UmsApi::class.java).checkVersion(UIUtils.getVersionName())
        RxUtils.acquireString(observable, object : RxUtils.DialogListener(mContext) {
            override fun onResult(result: Result) {
                val data = result.returnData
                val jsonObject = JSONObject(data)
                val isNewVersion = jsonObject.optString("isNewVersion")
                if (!TextUtils.equals("Y", isNewVersion)) {
                    val downloadUrl = jsonObject.optString("downloadUrl")
                    val isForcedUpdate = jsonObject.optString("isForcedUpdate")
                    val updateNotice = jsonObject.optString("updateNotice")
                    if (TextUtils.equals(isForcedUpdate, "Y")) {
                        showForceUpdate(String(Base64Util.decode(downloadUrl)), String(Base64Util.decode(updateNotice)))
                    } else showUpdate(String(Base64Util.decode(downloadUrl)), String(Base64Util.decode(updateNotice)))
                }
            }
        })
    }

    private fun showForceUpdate(downloadUrl: String, updateNotice: String) {
        val builder = AlertDialog.Builder(mContext)
        builder.setCancelable(false)
        println("version".plus(downloadUrl.plus("   ===   ").plus(updateNotice)))
        builder.setPositiveButton(R.string.update) { dialog, which ->
            val fileName = "ums_" + UIUtils.getVersionName() + ".apk"
            downloadApk(downloadUrl, fileName)
            IntentUtils.goExit(mContext)
        }

        builder.setMessage(Html.fromHtml(updateNotice))
        builder.setTitle(R.string.version_update)
        builder.setNegativeButton(R.string.exit) { dialog, which ->
            IntentUtils.goExit(mContext)
        }
        builder.create().show()
    }

    private fun showUpdate(downloadUrl: String, updateNotice: String) {
        val builder = AlertDialog.Builder(mContext)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.update) { dialog, which ->
            val fileName = "ums_" + UIUtils.getVersionName() + ".apk"
            downloadApk(downloadUrl, fileName)
        }

        builder.setMessage(Html.fromHtml(updateNotice))
        builder.setTitle(R.string.version_update)
        builder.setNegativeButton(R.string.cancel) { dialog, which ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun downloadApk(downloadUrl: String, fileName: String) {

        val request = DownloadManager.Request(Uri.parse(downloadUrl))

        /*
         * 设置在通知栏是否显示下载通知(下载进度), 有 3 个值可选:
         *    VISIBILITY_VISIBLE:                   下载过程中可见, 下载完后自动消失 (默认)
         *    VISIBILITY_VISIBLE_NOTIFY_COMPLETED:  下载过程中和下载完成后均可见
         *    VISIBILITY_HIDDEN:                    始终不显示通知
         */
        //在通知栏显示下载进度
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        // 设置通知的标题和描述
        request.setTitle("Ums版本更新")
        request.setDescription("对于该请求文件的描述")

        /*
         * 设置允许使用的网络类型, 可选值:
         *     NETWORK_MOBILE:      移动网络
         *     NETWORK_WIFI:        WIFI网络
         *     NETWORK_BLUETOOTH:   蓝牙网络
         * 默认为所有网络都允许
         */
         request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)

        // 添加请求头
//         request.addRequestHeader("User-Agent", "Chrome Mozilla/5.0")

        // 设置下载文件的保存位置
        val saveFile = File(UIUtils.getCachePath(), fileName)
        request.setDestinationUri(Uri.fromFile(saveFile))

        /*
         * 2. 获取下载管理器服务的实例, 添加下载任务
         */
        val manager = mContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // 将下载请求加入下载队列, 返回一个下载ID
        val downloadId = manager.enqueue(request)

        // 如果中途想取消下载, 可以调用remove方法, 根据返回的下载ID取消下载, 取消下载后下载保存的文件将被删除
        // manager.remove(downloadId);
    }
}