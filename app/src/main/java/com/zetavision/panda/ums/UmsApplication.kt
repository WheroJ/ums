package com.zetavision.panda.ums

import android.content.Context
import com.zetavision.panda.ums.utils.UIUtils
import org.litepal.LitePalApplication

/**
 * Created by wheroj on 2018/1/30.
 */
class UmsApplication : LitePalApplication() {

    private lateinit var application: Context
    override fun onCreate() {
        super.onCreate()

        application = this
        UIUtils.init(application)
    }
}