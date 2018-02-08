package com.zetavision.panda.ums.utils.network

import android.graphics.Bitmap
import com.zetavision.panda.ums.utils.BitmapUtils
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

/**
 * Created by wheroj on 2018/2/4 22:09.
 * @describe
 */
object UploadUtils {

    val reqWidth = 200
    val reqHeight = 200

    private fun uploadImge(file: File, listener: RxUtils.HttpListener) {
        //构建body
        var requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("name", "image.png")
//                .addFormDataPart("name", "image.png")
//                .addFormDataPart("psd", psd)
//                image/*
                .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("text/plain"), file))
                .build()
        RxUtils.acquireString(Client.getApi(UmsApi::class.java).uploadFile(requestBody), listener)

        /*object :RxUtils.DialogListener(activity) {
            override fun onResult(result: Result) {
                LogPrinter.i("UploadFile", "成功。。。。。")
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                LogPrinter.i("UploadFile", "失敗。。。。" + e.message)
            }
        }*/
    }

    /**
     * 上传图片到服务器
     * @param listener
     * @param bitmap  即将上传的文件
     * @param url  将图片上传到该地址
     */
    fun upload(listener: RxUtils.HttpListener, photoPath: String) {
        val bitmap = BitmapUtils.decodeSampledBitmapFromResource(photoPath, reqWidth, reqHeight)
        if (bitmap != null && bitmap.byteCount > 0) {
            Observable.just(bitmap)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .map {
                        var file = File(UIUtils.getCachePath(), "photo_" + System.currentTimeMillis() + ".png")
                        if (file.exists()) {
                            file = File(UIUtils.getCachePath(), "photo_" + UUID.randomUUID() + System.currentTimeMillis() + ".png")
                        }
                        try {
                            if (it.compress(Bitmap.CompressFormat.PNG, 50, FileOutputStream(file))) {
                                return@map file
                            } else return@map null
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                            return@map null
                        }
                    }.subscribe {
                        t -> if (t != null) uploadImge(t, listener)
                    }
        }

    }
}