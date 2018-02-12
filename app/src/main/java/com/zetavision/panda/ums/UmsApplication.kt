package com.zetavision.panda.ums

import android.content.Context
import com.zetavision.panda.ums.utils.Constant
import com.zetavision.panda.ums.utils.CrashHandler
import com.zetavision.panda.ums.utils.UIUtils
import com.zetavision.panda.ums.utils.UserPreferences
import org.litepal.LitePalApplication
import java.util.*

/**
 * Created by wheroj on 2018/1/30.
 */
class UmsApplication : LitePalApplication() {

    private lateinit var application: Context
    override fun onCreate() {
        super.onCreate()

        application = this
        UIUtils.init(application)

        if(!Constant.DEBUG) CrashHandler.getInstance().init(application)

        changeAppLanguage()
    }

    fun changeAppLanguage() {
        val preferences = UserPreferences()
        val sta = preferences.language
        val myLocale = Locale(sta)
        val res = resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.locale = myLocale
        res.updateConfiguration(conf, dm)
    }
}