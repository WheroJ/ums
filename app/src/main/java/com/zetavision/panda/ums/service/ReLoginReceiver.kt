package com.zetavision.panda.ums.service

import android.content.*
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.IBinder
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.utils.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.TimeUnit


/**
 * Created by shopping on 2018/1/23 11:47.
 * https://github.com/wheroj
 */
class ReLoginReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        var action = intent?.action
        when(action) {
            Constant.REGETTOKEN -> getNewToken(context)
            Constant.RELOGINACTION -> reLogin(context)
            Intent.ACTION_CLOSE_SYSTEM_DIALOGS -> monitorKey(intent, context)
            else -> listenerNet(intent, context)
        }
    }

    private fun listenerNet(intent: Intent?, context: Context) {
        if (intent != null) {
            // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
            if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
                //获取联网状态的NetworkInfo对象
                val info = intent
                        .getParcelableExtra<NetworkInfo>(ConnectivityManager.EXTRA_NETWORK_INFO)
                if (info != null) {
                    //如果当前的网络连接成功并且网络连接可用
                    var bindService: UmsService? = null
                    val connection: ServiceConnection = object : ServiceConnection {
                        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                            var binder: UmsService.MyBinder = service as UmsService.MyBinder
                            bindService = binder.service
                        }

                        override fun onServiceDisconnected(name: ComponentName?) {

                        }
                    }
                    IntentUtils.bindService(UIUtils.getContext(), connection)

                    if (NetworkInfo.State.CONNECTED == info.state && info.isAvailable) {
//                        IntentUtils.stopServcie(context)
                        EventBus.getDefault().post(Constant.NET_CONNECT)
//                        bindService?.dispose()
//                        UIUtils.getContext().unbindService(connection)
                        LogPrinter.i("TAG", getConnectionType(info.type) + "连上")
                    } else {
//                        bindService?.addLoginTimeObserver()
                        EventBus.getDefault().post(Constant.NET_DISCONNECT)
//                        IntentUtils.startReLoginService()
                        LogPrinter.i("TAG", getConnectionType(info.type) + "断开")
                    }
                }
            }
        }
    }

    private fun getConnectionType(type: Int): String {
        var connType = ""
        if (type == ConnectivityManager.TYPE_MOBILE) {
            connType = "3G网络数据"
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            connType = "WIFI网络"
        }
        return connType
    }

    private fun monitorKey(intent: Intent?, context: Context) {
        var reason = intent?.getStringExtra(Constant.SYSTEM_DIALOG_REASON_KEY)
        if (reason != null) {
            when (reason) {
                Constant.SYSTEM_DIALOG_REASON_HOME_KEY -> stopService(context)
                Constant.SYSTEM_DIALOG_REASON_LOCK -> stopService(context)
            }
        }
    }

    private fun stopService(context: Context) {
        SPUtil.saveBoolean(Constant.IS_RUN_BACK, true)
        IntentUtils.stopServcie(context)
    }

    private fun reLogin(context: Context) {
        ToastUtils.show(R.string.relogin)
        Observable.timer(3, TimeUnit.SECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    IntentUtils.goReLogin(context)
                })
    }

    private fun getNewToken(context: Context?) {

//        UserHttpUtils.getNewOpenId(TokenUtils.getToken(), object : RxUtils.DefaultListener(){
//            override fun onResult(result: String) {
//                var contentValues = ContentValues()
//                val userEntity = TokenUtils.getCurrentLoginUser()
//                contentValues.put("openId", JsonUtils.getJsonStringObjInKeyValue(result, "openId"))
//                contentValues.put("expireIn", JsonUtils.getIntObjInKeyValue(result, "expireIn"))
//                contentValues.put("tokenSaveTime", System.currentTimeMillis())
//                if (userEntity != null)
//                    DataSupport.updateAll(UserEntity::class.java, contentValues, "(telePhone='" + userEntity.telePhone + "')")
//            }
//
//            override fun onError(e: Throwable) {
//                super.onError(e)
//                if ("-106" == e.message) {
//                    //openId为空
//                    reLogin(context)
//                }
//            }
//        })

        LogPrinter.i("Recervier", "重新獲取token")
    }
}