package com.zetavision.panda.ums.utils.network

import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.webkit.MimeTypeMap
import com.zetavision.panda.ums.model.Result
import com.zetavision.panda.ums.utils.BitmapUtils
import com.zetavision.panda.ums.utils.LogPrinter
import com.zetavision.panda.ums.utils.UIUtils
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList



/**
 * Created by wheroj on 2018/2/4 22:09.
 * @describe
 */
object UploadUtils {

    const val reqWidth = 600
    const val reqHeight = 600

    abstract class UploadListener(context: AppCompatActivity? = null): RxUtils.DialogListener(context) {

        private var fileList = ArrayList<File?>()

        override fun onResult(result: Result) {
            fileList.indices
                    .map { fileList[it] }
                    .filter { it != null && it.exists() }
                    .forEach { it?.delete() }
        }

        fun setUploadFile(files: ArrayList<File?>) {
            this.fileList = files
        }
    }

    private fun uploadImge(fileList: ArrayList<File?>, formCode: String, listener: RxUtils.HttpListener) {
        //构建body
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        fileList.indices
                .filter { fileList[it] != null }
                .forEach {
                    builder.addFormDataPart("file", fileList[it]!!.name
                        , RequestBody.create(MediaType.parse(getMimeType(fileList[it]!!)), fileList[it]!!))
                }
        RxUtils.acquireString(Client.getApi(UmsApi::class.java)
                .uploadFile("inspect", "form", formCode, builder.build()), listener)
    }

    private fun uploadImges(fileList: ArrayList<File?>, formCode: String, listener: RxUtils.HttpListener) {
        //构建body
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        fileList.indices
                .filter { fileList[it] != null }
                .forEach {
                    builder.addFormDataPart("file", fileList[it]!!.name
                        , RequestBody.create(MediaType.parse(getMimeType(fileList[it]!!)), fileList[it]!!))
                }
        RxUtils.acquireString(Client.getApi(UmsApi::class.java)
                .uploadFileBatch("inspect", "form", formCode, builder.build()), listener)
    }

    /**
     * 上传图片到服务器
     * @param listener
     */
    fun upload(listener: RxUtils.HttpListener, photoPath: List<String>, formCode: String) {
        LogPrinter.i("UploadUtils", photoPath.size.toString() + "")
        Observable.just(photoPath)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map {
                    val bitmapList = ArrayList<Bitmap>()
                    photoPath.indices
                            .map {
                                LogPrinter.i("UploadUtils", "path=" + photoPath[it] + "。。。。。。。。。。。。")
                                photoPath[it].split(";") }
                            .map {
                                if (it.size > 1) it[1] else it[0]
                            }
                            .filterNot { TextUtils.isEmpty(it) }
                            .mapNotNullTo(bitmapList) {BitmapUtils.decodeSampledBitmapFromResource(it, reqWidth, reqHeight)}

                    return@map bitmapList
                }
                .map {
                    val fileList = ArrayList<File?>()
                    for (i in it.indices) {
                        val bitmap = it[i]
                        if (bitmap.byteCount > 0) {
                            var file = File(UIUtils.getCachePath(), "photo_" + System.currentTimeMillis() + ".png")
                            if (file.exists()) {
                                file = File(UIUtils.getCachePath(), "photo_" + UUID.randomUUID() + System.currentTimeMillis() + ".png")
                            }
                            try {
                                if (bitmap.compress(Bitmap.CompressFormat.PNG, 40, FileOutputStream(file))) {
                                    fileList.add(file)
                                    LogPrinter.i("UploadUtils", file.absolutePath)
                                }
                            } catch (e: FileNotFoundException) {
                                e.printStackTrace()
                            }
                        }
                    }
                    (listener as? UploadListener)?.setUploadFile(fileList)
                    return@map fileList
                }
                .subscribe {
                    uploadImges(it, formCode, listener)
                }
    }

    private fun getSuffix(file: File?): String? {
        if (file == null || !file.exists() || file.isDirectory) {
            return null
        }
        val fileName = file.name
        if (fileName == "" || fileName.endsWith(".")) {
            return null
        }
        val index = fileName.lastIndexOf(".")
        return if (index != -1) {
            fileName.substring(index + 1).toLowerCase(Locale.US)
        } else {
            null
        }
    }

    private fun getMimeType(file: File): String {
        val suffix = getSuffix(file) ?: return "file/*"
        val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix)
        return if (!TextUtils.isEmpty(type)) {
            type
        } else "file/*"
    }
}