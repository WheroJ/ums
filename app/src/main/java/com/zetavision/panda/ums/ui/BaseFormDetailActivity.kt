package com.zetavision.panda.ums.ui

import com.zetavision.panda.ums.base.BaseActivity
import com.zetavision.panda.ums.model.FormInfoDetail
import com.zetavision.panda.ums.model.Result
import com.zetavision.panda.ums.model.Shift
import com.zetavision.panda.ums.model.Weather
import com.zetavision.panda.ums.utils.network.Client
import com.zetavision.panda.ums.utils.network.UmsApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by wheroj on 2018/2/12 11:19.
 * @describe
 */
abstract class  BaseFormDetailActivity: BaseActivity() {

    protected var formInfoDetail: FormInfoDetail? = null
    protected var weatherList: List<Weather>? = null
    protected var shiftList: List<Shift>? = null

    fun loadWeatherAndShiftInNet() {
        Observable.zip(Client.getApi(UmsApi::class.java).queryWeather(), Client.getApi(UmsApi::class.java).queryShift()
                , BiFunction<ResponseBody, ResponseBody, HashMap<String, Result>> { t1, t2 ->
            var map = HashMap<String, Result>()

            try {
                var resultObject = JSONObject(t1.string())
                val resultWeather = Result()
                resultWeather.returnCode = resultObject.optString("returnCode")
                resultWeather.returnMessage = resultObject.optString("returnMessage")
                resultWeather.returnData = resultObject.optString("returnData")
                map["weather"] = resultWeather

                resultObject = JSONObject(t2.string())
                val resultShift = Result()
                resultShift.returnCode = resultObject.optString("returnCode")
                resultShift.returnMessage = resultObject.optString("returnMessage")
                resultShift.returnData = resultObject.optString("returnData")
                map["shift"] = resultShift
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return@BiFunction map
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { map ->
                    var weather = map["weather"]
                    weatherList = weather?.getList(Weather::class.java)
                    if (weatherList != null) {
                        for (i in weatherList!!.indices) {
                            weatherList!![i].saveOrUpdate("weather='" + weatherList!![i].weather + "'")
                        }
                    }

                    var shift = map["shift"]
                    shiftList = shift?.getList(Shift::class.java)
                    if (shiftList != null) {
                        for (i in shiftList!!.indices) {
                            shiftList!![i].saveOrUpdate("shift='" + shiftList!![i].shift + "'")
                        }
                    }

                    loadLocalData()
                }
    }

    abstract fun loadLocalData()
}