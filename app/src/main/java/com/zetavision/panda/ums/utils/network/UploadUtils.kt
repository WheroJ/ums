package com.zetavision.panda.ums.utils.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.webkit.MimeTypeMap
import com.zetavision.panda.ums.model.Result
import com.zetavision.panda.ums.utils.*
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.HashMap


/**
 * Created by wheroj on 2018/2/4 22:09.
 * @describe
 */
class UploadUtils {

    companion object {
        const val reqWidth = 600
        const val reqHeight = 600
    }
    private val TAG: String = "UploadUtils"
    private var isStopUpload = false

    fun getIsStopUpload(): Boolean {
        return isStopUpload
    }

    /**
     * 停止上传
     */
    fun stopUpload() {
        isStopUpload = true
    }

    /**
     * 设置可以上传文件
     */
    fun startUpload() {
        isStopUpload = false
    }

    abstract class UploadListener(context: AppCompatActivity? = null): RxUtils.DialogListener(context) {

        private var fileMap = HashMap<String, File>()

        override fun onResult(result: Result) {
            val iterator = fileMap.keys.iterator()
            while (iterator.hasNext()) {
                val oldPath = iterator.next()
                val oldFile = File(oldPath)
                val newFile = fileMap[oldPath]
                oldFile.delete()
                newFile?.delete()
                val newFileAbsolutePath = fileMap[oldPath]?.absolutePath

                if (!TextUtils.isEmpty(newFileAbsolutePath)) {
                    var lastPointIndex = newFileAbsolutePath!!.lastIndexOf(".")
                    if (lastPointIndex != -1 && "mp4" == newFileAbsolutePath.substring(lastPointIndex + 1)) {
                        val lastIndexOf = newFileAbsolutePath.lastIndexOf(File.separator)
                        if (lastIndexOf != -1)
                            FileUtils.deleteAll(newFileAbsolutePath.substring(0, lastIndexOf))
                    }
                }
            }
        }

        fun setUploadFile(fileMap: HashMap<String, File>) {
            this.fileMap = fileMap
        }
    }

    private fun uploadImages(fileMap: HashMap<String, File>, formCode: String, listener: RxUtils.HttpListener) {
        //构建body
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        val iterator = fileMap.keys.iterator()
        while (iterator.hasNext() && !isStopUpload) {
            val oldPath = iterator.next()
            val newFile = fileMap[oldPath]
            val oldFile = File(oldPath)
            if (newFile != null) {
                builder.addFormDataPart("file", oldFile.name
                        , RequestBody.create(MediaType.parse(getMimeType(newFile)), newFile))
            }
        }
        if (!isStopUpload) {
            RxUtils.acquireString(Client.getApi(UmsApi::class.java)
                    .uploadFileBatch("inspect", "form", formCode, builder.build()), listener)
        }
    }

    /**
     * 上传图片到服务器
     * @param listener
     */
    fun uploadImageAndVideo(listener: RxUtils.HttpListener, photoPath: List<String>, formCode: String) {
        Observable.just(photoPath)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map {
                    for (i in photoPath.indices) {
                        val split = photoPath[i].split(";")
                        if (split.size > 1) {
                            val type = split[0]
                            val path = split[1]
                            when (type) {
                                Constant.TAKE_PHOTO -> BitmapUtils.decodeSampledBitmapFromResource(path, reqWidth, reqHeight)
                            }
                        }
                    }

                    return@map photoPath
                }
                .map {
                    val fileMap = HashMap<String, File>()
                    for (i in it.indices) {
                        if (isStopUpload) return@map HashMap<String, File>()

                        val split = it[i].split(";")
                        if (split.size > 1) {
                            val type = split[0]
                            val path = split[1]

                            when (type) {
                                Constant.TAKE_PHOTO -> {
                                    val bitmap = BitmapFactory.decodeFile(path)
                                    if (bitmap != null && bitmap.byteCount > 0) {
                                        var file = File(UIUtils.getCachePath(), "photo_" + System.currentTimeMillis() + ".png")
                                        if (file.exists()) {
                                            file = File(UIUtils.getCachePath(), "photo_" + UUID.randomUUID() + System.currentTimeMillis() + ".png")
                                        }
                                        try {
                                            if (bitmap.compress(Bitmap.CompressFormat.PNG, 40, FileOutputStream(file))) {
                                                fileMap[path] = file
                                                LogPrinter.i("UploadUtils", file.absolutePath)
                                            }
                                        } catch (e: FileNotFoundException) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                                Constant.TAKE_VIDEO -> {
                                    fileMap[path] = File(path)
                                }
                            }
                        }

                    }
                    (listener as? UploadListener)?.setUploadFile(fileMap)
                    return@map fileMap
                }
                .subscribe {
                    uploadImages(it, formCode, listener)
                }
    }

    fun uploadFiles(uploadListener: UploadListener? = null, files: List<String>) {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        val fileMap = HashMap<String, File>()
        files.mapNotNull {
            val file = File(it)
            builder.addFormDataPart("file", file.name
                    , RequestBody.create(MediaType.parse(getMimeType(file)), file))
            fileMap[it] = file
        }
        uploadListener?.setUploadFile(fileMap)
        //TODO 后期需要完善
//        RxUtils.acquireString(Client.getApi(UmsApi::class.java)
//                .uploadFileBatch("inspect", "form", formCode, builder.build()), listener)
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