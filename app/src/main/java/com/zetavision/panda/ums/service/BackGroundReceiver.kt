package com.zetavision.panda.ums.service

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import android.text.TextUtils
import com.zetavision.panda.ums.model.Result
import com.zetavision.panda.ums.ui.LoginActivity
import com.zetavision.panda.ums.utils.Constant
import com.zetavision.panda.ums.utils.SPUtil
import com.zetavision.panda.ums.utils.UIUtils
import com.zetavision.panda.ums.utils.network.UploadUtils
import java.io.File
import java.util.*


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
            uploadCrashLog()
        } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED == intent.action) {
            // 点击下载进度通知时, 对应的下载ID以数组的方式传递
            val ids = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS)
            System.out.println("ids: " + Arrays.toString(ids))

            val fileName = "ums_" + UIUtils.getVersionName() + ".apk"
            val apkFile = File(UIUtils.getCachePath(), fileName)
            installApk(context, apkFile.absolutePath)

        } else if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action) {
            System.out.println("下载完成")

            /*
             * 获取下载完成对应的下载ID, 这里下载完成指的不是下载成功, 下载失败也算是下载完成,
             * 所以接收到下载完成广播后, 还需要根据 id 手动查询对应下载请求的成功与失败.
             */
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            val downloadFilePath = getDownloadFilePath(context, id)
            if (!TextUtils.isEmpty(downloadFilePath)) {
                installApk(context, downloadFilePath)
            }
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
                    val file = File(UIUtils.getCachePath(), it)
                    if (!file.exists())
                        crashPaths.remove(it)
                }
                SPUtil.saveObject(Constant.WAIT_UPLOAD_CRASH_LOG, crashPaths)
            }
        }, crashPaths)
    }

    private fun getDownloadFilePath(context: Context, downloadId: Long): String {
        // 获取下载管理器服务的实例
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // 创建一个查询对象
        val query = DownloadManager.Query()

        // 根据 下载ID 过滤结果
        query.setFilterById(downloadId)

        // 还可以根据状态过滤结果
        query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL)

        // 执行查询, 返回一个 Cursor (相当于查询数据库)
        val cursor = manager.query(query)

        if (!cursor.moveToFirst()) {
            cursor.close()
            return ""
        }

        // 下载ID
//        val id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID))
        // 下载请求的状态
        val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
        // 下载文件在本地保存的路径（Android 7.0 以后 COLUMN_LOCAL_FILENAME 字段被弃用, 需要用 COLUMN_LOCAL_URI 字段来获取本地文件路径的 Uri）
        val localFileName = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
        } else {
            cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME))
        }
        // 已下载的字节大小
//        val downloadedSoFar = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
        // 下载文件的总字节大小
//        val totalSize = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

        cursor.close()

//        println("下载进度: $downloadedSoFar/$totalSize")

        /*
         * 判断是否下载成功，其中状态 status 的值有 5 种:
         *     DownloadManager.STATUS_SUCCESSFUL:   下载成功
         *     DownloadManager.STATUS_FAILED:       下载失败
         *     DownloadManager.STATUS_PENDING:      等待下载
         *     DownloadManager.STATUS_RUNNING:      正在下载
         *     DownloadManager.STATUS_PAUSED:       下载暂停
         */
        return if (status == DownloadManager.STATUS_SUCCESSFUL) {
            /*
             * 特别注意: 查询获取到的 localFilename 才是下载文件真正的保存路径，在创建
             * 请求时设置的保存路径不一定是最终的保存路径，因为当设置的路径已是存在的文件时，
             * 下载器会自动重命名保存路径，例如: .../demo-1.apk, .../demo-2.apk
             */
            localFileName
        } else ""
    }

    //普通安装
    private fun installApk(context: Context, apkPath: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        //版本在7.0以上是不能直接通过uri访问的
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            val file = File(apkPath)
            // 由于没有在Activity环境下启动Activity,设置下面的标签
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            val apkUri = FileProvider.getUriForFile(context, "com.zetavision.panda.ums.fileprovider", file)
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        } else {
            intent.setDataAndType(Uri.fromFile(File(apkPath)),
                    "application/vnd.android.package-archive")
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}
