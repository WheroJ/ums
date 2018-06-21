package com.zetavision.panda.ums.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color.BLACK
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.zetavision.panda.ums.R
import java.util.*


/**
 * Created by wheroj on 2018/2/10 12:09.
 * @describe
 */
object QRUtils {

    private fun Create2QR2(urls: String, imageView: ImageView) {
        var mScreenWidth = 0
        val bitmap: Bitmap?
        try {
            /**
             * 获取屏幕信息的区别
             * 只有activity可以使用WindowManager否则应该使用Context.getResources().getDisplayMetrics()来获取。
             * Context.getResources().getDisplayMetrics()依赖于手机系统，获取到的是系统的屏幕信息；
             * WindowManager.getDefaultDisplay().getMetrics(dm)是获取到Activity的实际屏幕信息。
             */
            val context = UIUtils.getContext()
            mScreenWidth = UIUtils.getWinWidth()

            bitmap = createQRImage(urls, mScreenWidth,
                    BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))//自己写的方法

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    //生成二维码图片（不带图片）
    @Throws(WriterException::class)
    fun createQRCode(url: String, widthAndHeight: Int): Bitmap {
        val hints = Hashtable<EncodeHintType, String>()
        hints[EncodeHintType.CHARACTER_SET] =  "utf-8"
        val matrix = MultiFormatWriter().encode(url,
                BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight)

        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)
        //画黑点
        for (y in 0 until height) {
            (0 until width)
                    .filter { matrix.get(it, y) }
                    .forEach {
                        pixels[y * width + it] = BLACK //0xff000000
                    }
        }
        val bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    /**
     * 带图片的二维码
     */
    private fun createQRImage(content: String, heightPix: Int, logoBm: Bitmap?): Bitmap? {
        try {
            // if (content == null || "".equals(content)) {
            // return false;
            // }

            //配置参数
            val hints = HashMap<EncodeHintType, Any>()
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8")
            //容错级别
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            //设置空白边距的宽度
            // hints.put(EncodeHintType.MARGIN, 2); //default is 4

            // 图像数据转换，使用了矩阵转换
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, heightPix, heightPix, hints)
            val pixels = IntArray(heightPix * heightPix)
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (y in 0 until heightPix) {
                for (x in 0 until heightPix) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * heightPix + x] = -0x1000000
                    } else {
                        pixels[y * heightPix + x] = -0x1
                    }
                }
            }

            // 生成二维码图片的格式，使用ARGB_8888
            var bitmap = Bitmap.createBitmap(heightPix, heightPix, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, heightPix, 0, 0, heightPix, heightPix)

            if (logoBm != null) {
                bitmap = addLogo(bitmap, logoBm)
            }

            //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
            return bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * 在二维码中间添加Logo图案
     */
    private fun addLogo(src: Bitmap?, logo: Bitmap?): Bitmap? {
        if (src == null) {
            return null
        }

        if (logo == null) {
            return src
        }

        //获取图片的宽高
        val srcWidth = src.width
        val srcHeight = src.height
        val logoWidth = logo.width
        val logoHeight = logo.height

        if (srcWidth == 0 || srcHeight == 0) {
            return null
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return src
        }

        //logo大小为二维码整体大小的1/5
        val scaleFactor = srcWidth * 1.0f / 5f / logoWidth.toFloat()
        var bitmap: Bitmap? = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap)
            canvas.drawBitmap(src, 0f, 0f, null)
            canvas.scale(scaleFactor, scaleFactor, (srcWidth / 2).toFloat(), (srcHeight / 2).toFloat())
            canvas.drawBitmap(logo, ((srcWidth - logoWidth) / 2).toFloat(), ((srcHeight - logoHeight) / 2).toFloat(), null)

            canvas.save(Canvas.ALL_SAVE_FLAG)
            canvas.restore()
        } catch (e: Exception) {
            bitmap = null
            e.stackTrace
        }

        return bitmap
    }
}