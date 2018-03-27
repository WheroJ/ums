package com.zetavision.panda.ums.utils

import java.io.File

/**
 * Created by wheroj on 2018/3/14 16:09.
 * @describe
 */
object FileUtils {
    fun deleteAll(path: String) {
        val file = File(path)
        if (file.isDirectory) {
            val listFiles = file.listFiles()
            if (listFiles != null && listFiles.isEmpty()) {
                file.delete()
            } else {
                for (f in listFiles) {
                    deleteAll(f.absolutePath)
                    f.delete()
                }
                file.delete()
            }
        } else if (file.isFile) {
            file.delete()
        }
    }
}