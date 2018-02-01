package com.zetavision.panda.ums.utils.network

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import com.zetavision.panda.ums.model.Result
import com.zetavision.panda.ums.utils.Constant
import com.zetavision.panda.ums.utils.LoadingDialog
import com.zetavision.panda.ums.utils.LogPrinter
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import org.json.JSONObject
import java.lang.ref.WeakReference


/**
 * Created by shopping on 2017/12/22 10:46.
 * https://github.com/wheroj
 */
object RxUtils {

    var compositeDisposable = CompositeDisposable()

    abstract class DialogListener(var context: AppCompatActivity? = null) : HttpListener {

        private var weakReference: WeakReference<Activity>? = null
        private var progressDlg: LoadingDialog? = null

        init {
            if (context != null) {
                weakReference = WeakReference<Activity>(context)
                progressDlg = LoadingDialog()
            }

        }

        override fun onError(e: Throwable) {
            if (Constant.DEBUG) {
                LogPrinter.e("onError", e.message)
            }
            progressDlg?.dismiss()
        }

        override fun onComplete() {
            progressDlg?.dismiss()
        }

        override fun onStart(d: Disposable) {
            progressDlg?.show(context?.fragmentManager, null)
        }
    }


    interface HttpListener {
        fun onError(e: Throwable)
        fun onComplete()
        fun onStart(d: Disposable)
        fun onResult(result: Result)
    }

    fun acquireString(observable: Observable<ResponseBody>
                      , httpListener: HttpListener? = null) {
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { t: ResponseBody ->
                    val resultObject = JSONObject(t.string())
                    val result = Result()
                    result.returnCode = resultObject.getInt("returnCode")
                    result.returnMessage = resultObject.getString("returnMessage")
                    result.returnData = resultObject.getString("returnData")
                    return@map result
                }
                .subscribe(object : Observer<Result> {
                    override fun onNext(result: Result) {
                        if (result.returnCode == 0) {
                            httpListener?.onResult(result)
                        } else {
                            httpListener?.onError(Throwable("error-"+result.returnCode + ":" + result.returnMessage))
                        }
                    }

                    override fun onError(e: Throwable) {
                        httpListener?.onError(e)
                    }

                    override fun onComplete() {
                        httpListener?.onComplete()
                    }

                    override fun onSubscribe(d: Disposable) {
                        compositeDisposable.add(d)
                        httpListener?.onStart(d)
                    }
                })
    }

    fun cancelRequest() {
        compositeDisposable.dispose()
        compositeDisposable.clear()
    }
}