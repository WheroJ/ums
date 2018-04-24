package com.zetavision.panda.ums.ui.spotcheck

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
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
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONException
import org.json.JSONObject
import org.litepal.crud.DataSupport
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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

    private var compositeDisposable: CompositeDisposable? = null
    private var mPhotoPath: String? = null
    private var mPosition: Int = -1
    private var mVideoUri: Uri? = null
    private var addFiles: ArrayList<File> = ArrayList()
    private var delFiles: ArrayList<File> = ArrayList()

    private var TAG: String = "SpotCheck"

    override fun getContentLayoutId(): Int {
        return R.layout.activity_spotcheck_detail
    }

    @SuppressLint("WrongViewCast")
    override fun init() {
        header.setLeftImage(R.mipmap.back)
        header.setRightText(getString(R.string.common_savedata), R.color.main_color)

        recyclerView = findViewById(R.id.activitySpotCheckDetail_recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(`this`, LinearLayoutManager.VERTICAL, false)
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
            val map = HashMap<String, Result>()

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
                    val weather = map["weather"]
                    weatherList = weather?.getList(Weather::class.java)
                    if (weatherList != null) {
                        for (i in weatherList!!.indices) {
                            weatherList!![i].saveOrUpdate("weather='" + weatherList!![i].weather + "'")
                        }
                    }

                    val shift = map["shift"]
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
        val dialog = LoadingDialog()
        val bundle = Bundle()
        bundle.putString(Constant.LOADING_CONTENT_KEY, getString(R.string.loading))
        dialog.arguments = bundle
        dialog.show(fragmentManager, null)
        Observable.create<FormInfoDetail> {
            emitter ->
            formInfoDetail = DataSupport.where("(formId = '$maintFormId')").findFirst(FormInfoDetail::class.java, true)
            emitter.onNext(formInfoDetail!!)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(formInfoDetail == null) {
                        if (NetUtils.isNetConnect(`this`)) {
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
                            ToastUtils.show(R.string.data_exception)
                        }
                    } else {
                        initView(formInfoDetail)
                        initSpinner(formInfoDetail, weatherList, shiftList)
                        header.setTitle(resources.getString(R.string.spotcheck_fill, formInfoDetail!!.form.formCode))
                    }
                    dialog.dismiss()
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
                val weather = Weather()
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
                val shift = Shift()
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
        setAdapter()

        if (formInfoDetail != null) {
            findViewById<TextView>(R.id.activitySpotCheckDetail_inspectRouteCode).text = formInfoDetail.form.inspectRouteCode
            findViewById<TextView>(R.id.activitySpotCheckDetail_maintPeroid).text = formInfoDetail.form.inspectPeriodCode
            if (Constant.FORM_STATUS_CLOSED == formInfoDetail.form.status
                    || Constant.FORM_STATUS_COMPLETED == formInfoDetail.form.status
                    || Constant.FORM_STATUS_PLANNED == formInfoDetail.form.status) {
                header.setHiddenRight()
            } else {
                header.setRightText(getString(R.string.common_savedata), R.color.main_color)
            }

            val btnMaintStatus = findViewById<Button>(R.id.activitySpotCheckDetail_btnMaintStatus)
            val btnSeeSop = findViewById<Button>(R.id.activitySpotCheckDetail_btnSeeSop)
            val tvMaintStatus = findViewById<TextView>(R.id.activitySpotCheckDetail_tvMaintStatus)
            val tvMaintStatusStr = findViewById<TextView>(R.id.activitySpotCheckDetail_tvMaintStatusStr)

            btnSeeSop.setOnClickListener {
                val where = if (FormInfo.ACTION_TYPE_M == formInfoDetail.actionType) {
                    "flowCode = '" + formInfoDetail.form.maintFlowCode + "'"
                } else {
                    "flowCode = '" + formInfoDetail.form.inspectFlowCode + "'"
                }
                val sopMap = DataSupport.where(where).findFirst(SopMap::class.java)
                if (sopMap != null) {
                    OpenFileUtils.Companion.getInstance().openFile(sopMap.sopLocalPath)
                } else {
                    ToastUtils.show(R.string.no_local_data)
                }
            }

            when (formInfoDetail.form.status) {
//                "表单状态：已结束"
                Constant.FORM_STATUS_CLOSED -> tvMaintStatusStr.text = getString(R.string.formstatus).plus(getString(R.string.formstatus_end))
                Constant.FORM_STATUS_PLANNED -> {
//                    "表单状态：已计划"
                    tvMaintStatusStr.text = getString(R.string.formstatus).plus(getString(R.string.formstatus_planed))
                    btnMaintStatus.text = resources.getString(R.string.spotcheck_start)
                    val drawable: Drawable = resources.getDrawable(R.mipmap.start)
//                drawable.bounds = Rect(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                    btnMaintStatus.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                }
                Constant.FORM_STATUS_INPROGRESS -> {
                    compositeDisposable?.dispose()
                    compositeDisposable?.clear()
                    compositeDisposable = CompositeDisposable()
                    compositeDisposable?.add(Observable.interval(0, 1, TimeUnit.SECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe {
                                val useTime = TimeUtils.getUseTime(formInfoDetail.form.startTime)
                                tvMaintStatusStr.text = getString(R.string.formstatus).plus(getString(R.string.formstatus_ing)).plus(useTime)
                            })

                    btnMaintStatus.text = resources.getString(R.string.spotcheck_finish)
                    val drawable: Drawable = resources.getDrawable(R.mipmap.done)
                    btnMaintStatus.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                }
                Constant.FORM_STATUS_COMPLETED -> {
                    compositeDisposable?.dispose()
                    compositeDisposable?.clear()

//                    "表单状态：已完成"
                    tvMaintStatusStr.text = getString(R.string.formstatus).plus(getString(R.string.formstatus_finish))
                    btnMaintStatus.visibility = View.GONE
                    tvMaintStatus.visibility = View.VISIBLE
                }
            }
            btnMaintStatus.setOnClickListener {
                when (formInfoDetail.form.status) {
                    Constant.FORM_STATUS_PLANNED -> {
                        formInfoDetail.isUpload = FormInfo.WAIT
                        formInfoDetail.form.status = Constant.FORM_STATUS_INPROGRESS
                        formInfoDetail.form.startTime = System.currentTimeMillis() / 1000
                        val user = DataSupport.findLast(User::class.java)
                        if (user != null) formInfoDetail.form.startUser = user.USERNAME
                        formInfoDetail.form.saveOrUpdate("formId='${formInfoDetail.form.formId}'")
                        formInfoDetail.saveOrUpdate("formId='${formInfoDetail.formId}'")
                        initView(formInfoDetail)
                    }
                    Constant.FORM_STATUS_INPROGRESS -> {

                        val emptyIndex = ArrayList<Int>()
                        val checkDataSize = checkData(formInfoDetail, true, emptyIndex)
                        if (checkDataSize == formInfoDetail.formItemList.size) {
                            if (emptyIndex.isNotEmpty()) {
                                val buffer = StringBuffer()
                                buffer.append(getString(R.string.order).plus("："))
                                for (i in emptyIndex.indices) {
                                    if (i == emptyIndex.size - 1) {
                                        buffer.append(emptyIndex[i] + 1)
                                    } else {
                                        buffer.append((emptyIndex[i] +1).toString().plus("、"))
                                    }
                                }
                                buffer.append(getString(R.string.empty_notice))
                                showNotice(buffer.toString())
                            } else {
                                changeStatusAndSave(formInfoDetail)
                            }
                        }
                    }
                }
            }
        }
    }

    private var hashCodeMap:HashMap<String, Int> = HashMap()
    private fun setAdapter() {
        if (formInfoDetail != null) {

            if (spotCheckDetailAdapter == null) {
                hashCodeMap.clear()
                hashCodeMap[formInfoDetail!!.form.formId] = formInfoDetail!!.form.hashCode()
                formInfoDetail!!.formItemList.forEach {
                    hashCodeMap[it.formItemId] = it.hashCode()
                }

                spotCheckDetailAdapter = SpotCheckDetailAdapter(formInfoDetail!!, `this`)
                spotCheckDetailAdapter!!.setOnInnerItemClickListener(object : SpotCheckDetailAdapter.OnInnerItemClickListener {
                    override fun dismissTakePicture(position: Int) {
                        takePicturePopWindow?.dismiss()
                    }

                    override fun onAddClick(position: Int) {
                        showAddPop(position)
                    }

                    override fun takePicture(position: Int) {
                        showTakePictureDialog(position)
                    }
                })
                recyclerView?.adapter = spotCheckDetailAdapter
            } else {
                spotCheckDetailAdapter!!.updateData(formInfoDetail!!.form.status)
            }
        }
    }

    private fun changeStatusAndSave(formInfoDetail: FormInfoDetail) {
        formInfoDetail.form.status = Constant.FORM_STATUS_COMPLETED
        formInfoDetail.form.completeTime = System.currentTimeMillis() / 1000
        val user = DataSupport.findLast(User::class.java)
        if (user != null) formInfoDetail.form.completeUser = user.USERNAME

        if (saveData(formInfoDetail)) {
            ToastUtils.show(R.string.save_success)
        } else {
            ToastUtils.show(R.string.save_fail)
        }
        initView(formInfoDetail)
    }

    private fun showAddPop(position: Int) {
        val popupWindow = PopVideoPicture(`this`)
        popupWindow.setOnCameraShowListener(object : PopVideoPicture.OnCameraShowListener{
            override fun onTakeVideo() {
                popupWindow.dismiss()
//                mVideoUri = IntentUtils.startRecorder(`this`, VIDEO_RESULT)
//                mPosition = position
                IntentUtils.startRecorder(`this`, position, VIDEO_RESULT)
                 // 录制
//                val config = MediaRecorderConfig.Buidler()
////                        .doH264Compress(AutoVBRMode())
////                 .setMediaBitrateConfig(AutoVBRMode())
//                     .smallVideoWidth(480)
//                     .smallVideoHeight(360)
//                     .recordTimeMax(6 * 1000)
//                     .maxFrameRate(20)
//                        .minFrameRate(18)
//                        .captureThumbnailsTime(1)
////                 .recordTimeMin((int) (1.5 * 1000))
//                 .build()
//                MediaRecorderActivity.goSmallVideoRecorder(`this`, SpotCheckDetailActivity::class.java.name, config)
            }

            override fun onTakePicture() {
                popupWindow.dismiss()
                mPhotoPath = IntentUtils.loadImgFromCamera(`this`, position, CAMERA_RESULT)
                mPosition = position
            }
        })
        popupWindow.showAtLocation(recyclerView, Gravity.CENTER, 0, 0)
    }

    private fun showNotice(notice: String) {
        val builder = AlertDialog.Builder(`this`)
        builder.setMessage(notice)
        builder.setNeutralButton(R.string.cancel) { dialog, which ->
            builder.create().dismiss()
        }

        builder.setPositiveButton(R.string.keep_save) { dialog, which ->
            changeStatusAndSave(formInfoDetail!!)
        }
        builder.create().show()
    }

    private val CAMERA_RESULT = 100
    private val VIDEO_RESULT = 101
    private var takePicturePopWindow: PopupWindow? = null
    fun showTakePictureDialog(position: Int) {
        takePicturePopWindow?.dismiss()
        takePicturePopWindow = object: PopTakePicture(`this`) {
            override fun onTakePicture() {
                mPhotoPath = IntentUtils.loadImgFromCamera(`this`, position, CAMERA_RESULT)
                mPosition = position
                dismiss()
            }
        }
        takePicturePopWindow?.showAtLocation(recyclerView, Gravity.CENTER, 0, 0)
    }

    override fun getHasTitle(): Boolean {
        return true
    }

    override fun onLeftClick() {
        if (formInfoDetail?.form?.status == Constant.FORM_STATUS_INPROGRESS) {
            showDialog()
        } else {
            finish()
        }
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(`this`)
        builder.setMessage(R.string.notice_save_data)
        builder.setNeutralButton(R.string.keep_save) { dialog, which ->
            dialog.dismiss()
        }

        builder.setPositiveButton(R.string.sure_save) { dialog, which ->
            for (file in addFiles) file.delete()
            addFiles.clear()
            delFiles.clear()
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
    private fun checkAndSaveData(checkPhoto: Boolean, emptyIndex: ArrayList<Int>? = null):Boolean {
        LogPrinter.i(TAG, "checkData start===" + Calendar.getInstance().time.toString())
        if (checkData(formInfoDetail, checkPhoto, emptyIndex) == formInfoDetail?.formItemList?.size) {
            LogPrinter.i(TAG, "checkData end===" + Calendar.getInstance().time.toString())
            LogPrinter.i(TAG, "saveData start===" + Calendar.getInstance().time.toString())
            return if (saveData(formInfoDetail)) {
                LogPrinter.i(TAG, "saveData end===" + Calendar.getInstance().time.toString())
                ToastUtils.show(R.string.save_success)
                true
            } else {
                LogPrinter.i(TAG, "saveData end===" + Calendar.getInstance().time.toString())
                ToastUtils.show(R.string.save_fail)
                false
            }
        }
        return false
    }

    /**
     * @return 检测的数据量
     */
    private fun checkData(formInfoDetail: FormInfoDetail?, checkDetail: Boolean, emptyIndex: ArrayList<Int>?): Int {
        if (formInfoDetail != null) {
            for (i in formInfoDetail.formItemList.indices) {
                val formItem = formInfoDetail.formItemList[i]
                if (TextUtils.isEmpty(formItem.result)) {
                    if (checkDetail && emptyIndex != null) emptyIndex.add(i)
                } else {
                    if ("N" == formItem.valueType) {
                        try {
                            formItem.result.toFloat()
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                            ToastUtils.show("序号" + (i + 1) + "的取值必须是数字")
                            return i
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                            ToastUtils.show("序号" + (i + 1) + "的取值不能为空")
                            return i
                        }
                    }
                }

                if (!TextUtils.isEmpty(formItem.remarks)) {
                    if (formItem.remarks.length > Constant.MAX_LEN) {
                        ToastUtils.show("序号" + (i + 1) + "的备注超过200的长度限制")
                        return i
                    }
                }

                if (checkDetail) {
                    if ("Y" == formItem.photoMust) {
                        if (formItem.photoPaths == null || formItem.photoPaths.isEmpty()) {
                            ToastUtils.show("序号" + (i + 1) + "的必须进行现场拍照才能保存")
                            return i
                        }
                    }
                }
            }

            if (!TextUtils.isEmpty(formInfoDetail.form.fillinRemarks)) {
                if (formInfoDetail.form.fillinRemarks.length > Constant.MAX_LEN) {
                    ToastUtils.show("备注超过200的长度限制")
                    return -1
                }
            }
            return formInfoDetail.formItemList.size
        }
        return -1
    }

    private fun saveData(formInfoDetail: FormInfoDetail?): Boolean {
        if (formInfoDetail != null) {
            var isSuccessSave: Boolean
            formInfoDetail.formItemList.forEach {
                if (hashCodeMap[it.formItemId] != it.hashCode()) {
                    isSuccessSave = it.saveOrUpdate("(formItemId='${it.formItemId}')")
                    if (!isSuccessSave) return isSuccessSave
                    hashCodeMap[it.formItemId] = it.hashCode()
                }
            }

            if (formInfoDetail.form.hashCode() != hashCodeMap[formInfoDetail.formId]) {
                val formInfo = formInfoDetail.form
                isSuccessSave = formInfo.saveOrUpdate("(formId='${formInfo.formId}')")
                if (!isSuccessSave) return isSuccessSave
                hashCodeMap[formInfoDetail.formId] = formInfo.hashCode()
            }
            formInfoDetail.isUpload = 1

            //删除 本地图片和视频
            for (file in delFiles) file.delete()
            delFiles.clear()
            addFiles.clear()
            return formInfoDetail.saveOrUpdate("(formId='${formInfoDetail.formId}')")
        }
        return false
    }

    override fun onPause() {
        super.onPause()
        compositeDisposable?.dispose()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_RESULT) {
                val formItemList = formInfoDetail?.formItemList
                if (formItemList != null && !TextUtils.isEmpty(mPhotoPath)) {
                    if (mPosition != -1 && mPosition in formInfoDetail?.formItemList?.indices!!) {
                        if (formItemList[mPosition].photoPaths == null)
                            formItemList[mPosition].photoPaths = ArrayList()
                        drawTime(mPhotoPath!!, formItemList[mPosition])

                        mPhotoPath = null
                        mPosition = -1
                    }
                }
            } else if (requestCode == VIDEO_RESULT) {
                if (data != null) {
                    val videoPath = data.getStringExtra("videoPath")
                    val parentPosition = data.getIntExtra("parentPosition", -1)

                    val formItemList = formInfoDetail?.formItemList
                    if (formItemList != null) {
                        if (parentPosition != -1 && parentPosition in formInfoDetail?.formItemList?.indices!!) {
                            if (formItemList[parentPosition].photoPaths == null)
                                formItemList[parentPosition].photoPaths = ArrayList()

                            formItemList[parentPosition].photoPaths.add(Constant.TAKE_VIDEO.plus(";").plus(videoPath))
                            addFiles.add(File(videoPath))
                            setAdapter()

//                            val dialog = LoadingDialog()
//                            val bundle = Bundle()
//                            bundle.putString("content", getString(R.string.saving_video))
//                            dialog.arguments = bundle
//                            dialog.show(fragmentManager, null)
//
//                            Observable.just(videoPath)
//                                    .map {
//                                        val config = LocalMediaConfig.Buidler()
//                                                .setVideoPath(it)
//                                                .captureThumbnailsTime(1)
//                                                .doH264Compress(AutoVBRMode())
//                                                .setFramerate(28)
//                                                .build()
//                                        LocalMediaCompress(config).startCompress()
//                                    }
//                                    .subscribeOn(Schedulers.io())
//                                    .observeOn(AndroidSchedulers.mainThread())
//                                    .subscribe {
//                                        dialog.dismiss()
//                                        if (it != null) {
//                                            FileUtils.deleteAll(videoPath)
//                                            formItemList[parentPosition].photoPaths.add(Constant.TAKE_VIDEO.plus(";").plus(it.videoPath))
//                                            addFiles.add(File(it.videoPath))
//                                            setAdapter()
//                                        } else {
//                                            ToastUtils.show(R.string.save_video_fail)
//                                        }
//                                    }
                        }
                    }
                }
//                if (mVideoUri != null) {
//                    val formItemList = formInfoDetail?.formItemList
//                    if (formItemList != null) {
//                        if (mPosition != -1 && mPosition in formInfoDetail?.formItemList?.indices!!) {
//                            if (formItemList[mPosition].photoPaths == null)
//                                formItemList[mPosition].photoPaths = ArrayList()
//
//                            val dialog = LoadingDialog()
//                            val bundle = Bundle()
//                            bundle.putString("content", getString(R.string.saving_video))
//                            dialog.arguments = bundle
//                            dialog.show(fragmentManager, null)
//
//                            Observable.just(mVideoUri!!.path)
//                                    .map {
//                                        val config = LocalMediaConfig.Buidler()
//                                                .setVideoPath(it)
//                                                .captureThumbnailsTime(1)
//                                                .doH264Compress(AutoVBRMode())
//                                                .setFramerate(28)
//                                                .build()
//                                        LocalMediaCompress(config).startCompress()
//                                    }
//                                    .subscribeOn(Schedulers.io())
//                                    .observeOn(AndroidSchedulers.mainThread())
//                                    .subscribe {
//                                        dialog.dismiss()
//                                        if (it != null) {
//                                            FileUtils.deleteAll(mVideoUri!!.path)
//                                            formItemList[mPosition].photoPaths.add(Constant.TAKE_VIDEO.plus(";").plus(it.videoPath))
//                                            addFiles.add(File(it.videoPath))
//                                            setAdapter()
//
//                                            mVideoUri = null
//                                            mPosition = -1
//                                        } else {
//                                            ToastUtils.show(R.string.save_video_fail)
//                                        }
//                                    }
//                        }
//                    }
//                }
            }
        }
    }

    /**
     * 右下角绘制日期
     */
    private fun drawTime(photoPath: String, formItem: FormItem) {
        val bitmap = BitmapUtils.decodeSampledBitmapFromResource(photoPath, UploadUtils.Companion.reqWidth, UploadUtils.Companion.reqHeight)
                .copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        val currentTime: String = TimeUtils.getCurrentTime()

        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.textSize = UIUtils.dip2px(16).toFloat()
        paint.color = resources.getColor(R.color.red)

        val bounds = Rect()
        paint.getTextBounds(currentTime, 0, currentTime.length, bounds)
        val textWidth = bounds.right - bounds.left

        canvas.drawText(currentTime, (bitmap.width - textWidth - UIUtils.dip2px(15)).toFloat()
                , (bitmap.height - UIUtils.dip2px(15)).toFloat(), paint)
        canvas.save()
        canvas.restore()

        val oldFile = File(photoPath)
        val lastIndexOf = photoPath.lastIndexOf(".")
        val end = photoPath.substring(lastIndexOf)
        val saveFile = File(photoPath.substring(0, lastIndexOf).plus("_2").plus(end))
        if (!saveFile.exists()) {
            saveFile.createNewFile()
        }
        oldFile.delete()
        val fos = FileOutputStream(saveFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)

        addFiles.add(saveFile)
        formItem.photoPaths.add(Constant.TAKE_PHOTO.plus(";").plus(saveFile.absolutePath))
        setAdapter()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeleteFile(localFile: File) {
        for (file in delFiles) {
            if (file.absolutePath == localFile.absolutePath)
                return
        }
        delFiles.add(localFile)
    }
}