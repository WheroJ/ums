package com.zetavision.panda.ums.utils.network

import com.zetavision.panda.ums.utils.UIUtils
import okhttp3.logging.HttpLoggingInterceptor
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by wheroj on 2018/6/12 15:17.
 * @describe
 */
class LocalLogger: HttpLoggingInterceptor.Logger {
    override fun log(message: String?) {
        val format = SimpleDateFormat("yyyy-MM-dd")
        val calendar = Calendar.getInstance()
        val dateStr = format.format(calendar.time)
        val file = File(UIUtils.getCachePath(), dateStr + "_log.log")
        if (!file.exists()) {
            file.createNewFile()
        }

        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val preFileName = format.format(calendar.time) + "_log.log"
        val preFile = File(UIUtils.getCachePath(), preFileName)
        if (preFile.exists()) {
            preFile.delete()
        }

        val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(file, true)))
        writer.append(message)
        writer.newLine()
        writer.flush()
        writer.close()
    }
}