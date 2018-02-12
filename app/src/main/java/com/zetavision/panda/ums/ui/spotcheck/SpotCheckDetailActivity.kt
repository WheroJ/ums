package com.zetavision.panda.ums.ui.spotcheck

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.adapter.CommonSpinnerAdapter
import com.zetavision.panda.ums.adapter.SpotCheckDetailAdapter
import com.zetavision.panda.ums.base.BaseActivity
import com.zetavision.panda.ums.model.*
import com.zetavision.panda.ums.utils.*
import com.zetavision.panda.ums.utils.network.Client
import com.zetavision.panda.ums.utils.network.RxUtils
import com.zetavision.panda.ums.utils.network.UmsApi
import com.zetavision.panda.ums.utils.network.UploadUtils
import com.zetavision.panda.ums.widget.PopTakePicture
import com.zetavision.panda.ums.widget.PopVideoPicture
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import org.litepal.crud.DataSupport
import java.util.concurrent.TimeUnit

/**
 * Created by wheroj on 2018/2/5 13:48.
 * @describe
 */
class SpotCheckDetailActivity: BaseActivity() {

    private var recyclerView: RecyclerView? = null
    private var formInfoDetail: FormInfoDetail? = null
    private var weatherList: List<Weather>? = null
    private var shiftList: List<Shift>? = null
    private lateinit var maintFormId: String

    private var temperSpinnerAdapter: CommonSpinnerAdapter? = null
    private var classesSpinnerAdapter: CommonSpinnerAdapter? = null
    private  var spotCheckDetailAdapter: SpotCheckDetailAdapter? = null

    private var compositeDisposable = CompositeDisposable()
    override fun getContentLayoutId(): Int {
        return R.layout.activity_spotcheck_detail
    }

    @SuppressLint("WrongViewCast")
    override fun init() {
        header.setLeftImage(R.mipmap.back)
        header.setRightText(getString(R.string.common_savedata), R.color.main_color)

        findViewById<TextView>(R.id.activityFormDetail_tvRemark).text = getString(R.string.spotcheck_remark)

        recyclerView = findViewById(R.id.activitySpotCheckDetail_recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        maintFormId = intent.getStringExtra("maintFormId")
        loadData()
    }

    private fun loadData() {

        if (NetUtils.isNetConnect(`this`)) {
            loadWeatherAndShiftInNet()
        } else {
            weatherList = DataSupport.findAll(Weather::class.java)
            shiftList = DataSupport.findAll(Shift::class.java)
            loadLocalData()
        }
    }

    private fun loadWeatherAndShiftInNet() {
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

    @SuppressLint("StringFormatInvalid")
    private fun loadLocalData() {
        formInfoDetail = DataSupport.where("(formId = '$maintFormId')").findFirst(FormInfoDetail::class.java, true)
        if(formInfoDetail == null) {
            RxUtils.acquireString(Client.getApi(UmsApi::class.java).downloadMaintForm(maintFormId)
                    , object : RxUtils.DialogListener() {
                override fun onResult(result: Result) {
                    val formInfoDetails = result.getList(FormInfoDetail::class.java)
                    if (formInfoDetails != null && formInfoDetails.size > 0) {
                        formInfoDetail = formInfoDetails[0]
                        header.setTitle(resources.getString(R.string.spotcheck_fill, formInfoDetail!!.form.formCode))
                        initView(formInfoDetail)
                        initSpinner(formInfoDetail, weatherList, shiftList)
                    }
                }
            })
        } else {
            initView(formInfoDetail)
            initSpinner(formInfoDetail, weatherList, shiftList)
            header.setTitle(resources.getString(R.string.spotcheck_fill, formInfoDetail!!.form.formCode))
        }
    }

    private fun initSpinner(formInfoDetail: FormInfoDetail?, weatherList: List<Weather>?, shiftList: List<Shift>?) {

        if (formInfoDetail != null) {
            val temperSpinner = findViewById<Spinner>(R.id.activitySpotCheckDetail_temperSpinner)
            val classesSpinner = findViewById<Spinner>(R.id.activitySpotCheckDetail_classesSpinner)

            var weathers: List<String> = arrayListOf()
            if (weatherList != null) {
                weathers = weatherList.indices.map { weatherList[it].description }
            }

            var shifts: List<String> = arrayListOf()
            if (shiftList != null) {
                shifts = shiftList.indices.map { shiftList[it].description }
            }

            if (temperSpinnerAdapter == null) {
                temperSpinnerAdapter = CommonSpinnerAdapter(this)
                temperSpinner.adapter = temperSpinnerAdapter
                temperSpinnerAdapter!!.notifyDataSetChanged(weathers)
                temperSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        formInfoDetail.form.weather = weatherList!![position].weather
                    }
                }
            }

            if (TextUtils.isEmpty(formInfoDetail.form.weather)) {
                temperSpinner.setSelection(0)
            } else {
                var weather = Weather()
                weather.weather = formInfoDetail.form.weather
                val indexOf = weatherList?.indexOf(weather) ?: -1
                if (indexOf in weathers.indices) temperSpinner.setSelection(indexOf)
                else {
                    formInfoDetail.form.weather = weatherList!![0].description
                    temperSpinner.setSelection(0)
                }
            }

            if (classesSpinnerAdapter == null) {
                classesSpinnerAdapter = CommonSpinnerAdapter(this)
                classesSpinner.adapter = classesSpinnerAdapter
                classesSpinnerAdapter!!.notifyDataSetChanged(shifts)
                classesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        formInfoDetail.form.shift = shiftList!![position].shift
                    }
                }
            }
            if (TextUtils.isEmpty(formInfoDetail.form.shift)) {
                classesSpinner.setSelection(0)
            } else {
                var shift = Shift()
                shift.shift = formInfoDetail.form.shift
                val indexOf = shiftList?.indexOf(shift) ?: -1
                if (indexOf in shifts.indices) classesSpinner.setSelection(indexOf)
                else {
                    formInfoDetail.form.shift = shiftList!![0].shift
                    classesSpinner.setSelection(0)
                }
            }
        }
    }

    @SuppressLint("WrongViewCast")
    private fun initView(formInfoDetail: FormInfoDetail?) {

        if (formInfoDetail != null) {
            if (spotCheckDetailAdapter == null) {
                spotCheckDetailAdapter = SpotCheckDetailAdapter(formInfoDetail.formItemList, formInfoDetail.form.status, `this`)
                spotCheckDetailAdapter!!.setOnInnerItemClickListener(object : SpotCheckDetailAdapter.OnInnerItemClickListener{
                    override fun onAddClick(position: Int) {
                        showAddPop(position)
                    }

                    override fun takePicture(position: Int) {
                        showTakePictureDialog(position)
                    }
                })
                recyclerView?.adapter = spotCheckDetailAdapter
            } else {
                spotCheckDetailAdapter!!.updateData(formInfoDetail.form.status)
            }

            findViewById<TextView>(R.id.activitySpotCheckDetail_inspectRouteCode).text = formInfoDetail.form.inspectRouteCode
            findViewById<TextView>(R.id.activitySpotCheckDetail_maintPeroid).text = formInfoDetail.form.inspectPeriodCode
            val etRemark = findViewById<EditText>(R.id.activitySpotCheckDetail_etRemark)
            val tvRemark = findViewById<TextView>(R.id.activitySpotCheckDetail_tvRemark)
            if (Constant.FORM_STATUS_CLOSED == formInfoDetail.form.status
                    || Constant.FORM_STATUS_COMPLETED == formInfoDetail.form.status
                    || Constant.FORM_STATUS_PLANNED == formInfoDetail.form.status) {
                header.setHiddenRight()
                tvRemark.visibility = View.VISIBLE
                etRemark.visibility = View.GONE
            } else {
                header.setRightText(getString(R.string.common_savedata), R.color.main_color)
                tvRemark.visibility = View.GONE
                etRemark.visibility = View.VISIBLE
                etRemark.setText(formInfoDetail.form.fillinRemarks)
                etRemark.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        formInfoDetail.form.fillinRemarks = s.toString()
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    }
                })
            }

            val btnMaintStatus = findViewById<Button>(R.id.activitySpotCheckDetail_btnMaintStatus)
            val tvMaintStatus = findViewById<TextView>(R.id.activitySpotCheckDetail_tvMaintStatus)
            val tvMaintStatusStr = findViewById<TextView>(R.id.activitySpotCheckDetail_tvMaintStatusStr)
            when (formInfoDetail.form.status) {
//                "表单状态：已结束"
                Constant.FORM_STATUS_CLOSED -> tvMaintStatusStr.text = getString(R.string.formstatus).plus(getString(R.string.formstatus_end))
                Constant.FORM_STATUS_PLANNED -> {
//                    "表单状态：已计划"
                    tvMaintStatusStr.text = getString(R.string.formstatus).plus(getString(R.string.formstatus_planed))
                    btnMaintStatus.text = resources.getString(R.string.spotcheck_start)
                    var drawable: Drawable = resources.getDrawable(R.mipmap.start)
//                drawable.bounds = Rect(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                    btnMaintStatus.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                }
                Constant.FORM_STATUS_INPROGRESS -> {
                    compositeDisposable.add(Observable.interval(0, 1, TimeUnit.SECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe {
                                val useTime = TimeUtils.getUseTime(formInfoDetail.form.startTime)
                                tvMaintStatusStr.text = getString(R.string.formstatus).plus(getString(R.string.formstatus_ing)).plus(useTime)
                            })

                    btnMaintStatus.text = resources.getString(R.string.spotcheck_finish)
                    var drawable: Drawable = resources.getDrawable(R.mipmap.done)
                    btnMaintStatus.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                }
                Constant.FORM_STATUS_COMPLETED -> {
                    compositeDisposable.dispose()
                    compositeDisposable.clear()

//                    "表单状态：已完成"
                    tvMaintStatusStr.text = getString(R.string.formstatus).plus(getString(R.string.formstatus_finish))
                    btnMaintStatus.visibility = View.GONE
                    tvMaintStatus.visibility = View.VISIBLE
                }
            }
            btnMaintStatus.setOnClickListener {
                when (formInfoDetail.form.status) {
                    Constant.FORM_STATUS_PLANNED -> {
                        formInfoDetail.form.status = Constant.FORM_STATUS_INPROGRESS
                        formInfoDetail.form.startTime = System.currentTimeMillis() / 1000
                        val user = DataSupport.findLast(User::class.java)
                        if (user != null) formInfoDetail.form.startUser = user.USERNAME
                        formInfoDetail.form.saveOrUpdate("formId='${formInfoDetail.form.formId}'")
                        formInfoDetail.saveOrUpdate("formId='${formInfoDetail.formId}'")
                        initView(formInfoDetail)
                    }
                    Constant.FORM_STATUS_INPROGRESS -> {
//                        formInfoDetail.form.saveOrUpdate("formId='${formInfoDetail.form.formId}'")
//                        formInfoDetail.saveOrUpdate("formId='${formInfoDetail.formId}'")
                        if (checkAndSaveData(true)) {
                            formInfoDetail.form.status = Constant.FORM_STATUS_COMPLETED
                            formInfoDetail.form.completeTime = System.currentTimeMillis() / 1000
                            val user = DataSupport.findLast(User::class.java)
                            if (user != null) formInfoDetail.form.completeUser = user.USERNAME
                            formInfoDetail.form.saveOrUpdate("formId='${formInfoDetail.form.formId}'")
//                            formInfoDetail.saveOrUpdate("formId='${formInfoDetail.formId}'")
                            initView(formInfoDetail)
                        }
                    }
                }
            }
        }
    }

    private fun showAddPop(position: Int) {
        var popupWindow = object: PopVideoPicture(`this`) {
            override fun onTakeVideo() {

            }

            override fun onTakePicture() {
                IntentUtils.loadImgFromCamera(`this`, position, CAMERA_RESULT)
            }
        }
        popupWindow.showAtLocation(recyclerView, Gravity.CENTER, 0, 0)
    }

    private val CAMERA_RESULT = 100
    private val VEDIO_RESULT = 101
    fun showTakePictureDialog(position: Int) {
        var popupWindow = object: PopTakePicture(`this`) {
            override fun onTakePicture() {
                IntentUtils.loadImgFromCamera(`this`, position, CAMERA_RESULT)
            }
        }
        popupWindow.showAtLocation(recyclerView, Gravity.CENTER, 0, 0)
    }

    override fun getHasTitle(): Boolean {
        return true
    }

    override fun onLeftClick() {
        if (formInfoDetail?.form?.status == Constant.FORM_STATUS_INPROGRESS) {
            showDialog()
        }
    }

    private fun showDialog() {
        var builder = AlertDialog.Builder(`this`)
        builder.setMessage(R.string.notice_save_data)
        builder.setNeutralButton(R.string.cancel) { dialog, which ->
            builder.create().dismiss()
        }

        builder.setPositiveButton(R.string.back) { dialog, which ->
            finish()
        }
        builder.create().show()
    }

    override fun onBackPressed() {
        if (formInfoDetail?.form?.status == Constant.FORM_STATUS_INPROGRESS) {
            showDialog()
        } else {
            super.onBackPressed()
        }
    }

    override fun onRightTextClick() {
        checkAndSaveData(false)
    }

    /**
     * 是否进行拍照检测
     */
    private fun checkAndSaveData(checkPhoto: Boolean):Boolean {
        if (checkData(formInfoDetail, checkPhoto)) {
            return if (saveData(formInfoDetail)) {
                ToastUtils.show(R.string.save_success)
//                finish()
                true
            } else {
                ToastUtils.show(R.string.save_fail)
                false
            }
        }
        return false
    }

    private fun checkData(formInfoDetail: FormInfoDetail?, checkDetail: Boolean): Boolean {
        if (formInfoDetail != null) {
            for (i in formInfoDetail.formItemList.indices) {
                val formItem = formInfoDetail.formItemList[i]
                if ("N" == formItem.valueType) {
                    try {
                        if (TextUtils.isEmpty(formItem.presetValue)) {
                            if (!checkDetail) {
                                val findFirst = DataSupport.where("formItemId='${formItem.formItemId}'").findFirst(FormItem::class.java)
                                if (findFirst != null) formItem.presetValue = findFirst.presetValue
                            }
                        } else {
                            val intValue = formItem.presetValue.toFloat()
//                            val lowerLimit = formItem.lowerLimit.toFloat()
//                            val upperLimit = formItem.upperLimit.toFloat()
//                            if (intValue < lowerLimit || intValue > upperLimit) {
//                                ToastUtils.show("序号" + (i + 1) + "设定的值超出了范围")
//                                return false
//                            }
                        }
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                        ToastUtils.show("序号" + (i + 1) + "的取值必须是数字")
                        return false
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                        ToastUtils.show("序号" + (i + 1) + "的取值不能为空")
                        return false
                    }
                }
                if (checkDetail && TextUtils.isEmpty(formItem.presetValue)) {
                    ToastUtils.show("序号" + (i + 1) + "保养数据未录入，请录入数据！")
                    return false
                }

                if (!TextUtils.isEmpty(formItem.remarks)) {
                    if (formItem.remarks.length > Constant.MAX_LEN) {
                        ToastUtils.show("序号" + (i + 1) + "的备注超过200的长度限制")
                        return false
                    }
                }

                if (checkDetail) {
                    if ("Y" == formItem.photoMust) {
                        if (formItem.photoPaths == null || formItem.photoPaths.isEmpty()) {
                            ToastUtils.show("序号" + (i + 1) + "的必须进行现场拍照才能保存")
                            return false
                        }
                    }
                }
            }
            if (!TextUtils.isEmpty(formInfoDetail.form.fillinRemarks)) {
                if (formInfoDetail.form.fillinRemarks.length > Constant.MAX_LEN) {
                    ToastUtils.show("备注超过200的长度限制")
                    return false
                }
            }
            return true
        }
        return false
    }

    private fun saveData(formInfoDetail: FormInfoDetail?): Boolean {
        if (formInfoDetail != null) {
            var isSuccessSave: Boolean
            formInfoDetail.formItemList.indices
                    .map { formInfoDetail.formItemList[it] }
                    .forEach {
                        isSuccessSave = it.saveOrUpdate("(formItemId='${it.formItemId}')")
                        if (!isSuccessSave) return isSuccessSave
                    }

            val formInfo = formInfoDetail.form
            isSuccessSave = formInfo.saveOrUpdate("(formId='${formInfo.formId}')")
            if (!isSuccessSave) return isSuccessSave
            formInfoDetail.isUpload = 1
            return formInfoDetail.saveOrUpdate("(formId='${formInfoDetail.formId}')")
        }
        return false
    }

    override fun onPause() {
        super.onPause()
        compositeDisposable.dispose()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_RESULT && resultCode == RESULT_OK) run {
            if (data != null) {
                var mPhotoPath = data.getStringExtra(Constant.PHOTOPATH)
                val position = data.getIntExtra("position", -1)

                val formItemList = formInfoDetail?.formItemList
                if (formItemList != null && !TextUtils.isEmpty(mPhotoPath)) {
                    if (position != -1 && position in formInfoDetail?.formItemList?.indices!!) {
                        if (formItemList[position].photoPaths == null)
                            formItemList[position].photoPaths = ArrayList()
                        drawTime(mPhotoPath)
                        formItemList[position].photoPaths.add(Constant.TAKE_PHOTO.plus(";").plus(mPhotoPath))

//                        UploadUtils.upload(object : RxUtils.DialogListener(`this`) {
//                            override fun onResult(result: Result) {
//                                ToastUtils.show("上传成功")
//                                if (formItemList[position].photoUrls == null)
//                                    formItemList[position].photoUrls = ArrayList()
//                                formItemList[position].photoUrls.add(result.returnData)
//                                spotCheckDetailAdapter?.notifyDataSetChanged()
//                            }
//                        }, mPhotoPath)
                    }
                }
            }
        }
    }

    /**
     * 右下角绘制日期
     */
    private fun drawTime(photoPath: String) {
        val bitmap = BitmapUtils.decodeSampledBitmapFromResource(photoPath, UploadUtils.reqWidth, UploadUtils.reqHeight)
        var canvas = Canvas(bitmap)
        val currentTime: String = TimeUtils.getCurrentTime()

        var paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.textSize = UIUtils.dip2px(16).toFloat()
        paint.color = resources.getColor(R.color.white)

        val bounds = Rect()
        paint.getTextBounds(currentTime, 0, currentTime.length, bounds)
        val textHeight = paint.fontMetrics.bottom - paint.fontMetrics.top
        val textWidth = bounds.right - bounds.left

        canvas.drawText(currentTime, (bitmap.width - textWidth - UIUtils.dip2px(15)).toFloat()
                , (bitmap.height - UIUtils.dip2px(15)).toFloat(), paint)
    }
}