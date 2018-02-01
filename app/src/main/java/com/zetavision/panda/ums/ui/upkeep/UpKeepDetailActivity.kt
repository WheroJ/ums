package com.zetavision.panda.ums.ui.upkeep

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.adapter.CommonSpinnerAdapter
import com.zetavision.panda.ums.adapter.UpKeepDetailAdapter
import com.zetavision.panda.ums.base.BaseActivity
import com.zetavision.panda.ums.model.FormInfoDetail
import com.zetavision.panda.ums.model.Result
import com.zetavision.panda.ums.model.User
import com.zetavision.panda.ums.utils.Constant
import com.zetavision.panda.ums.utils.TimeUtils
import com.zetavision.panda.ums.utils.ToastUtils
import com.zetavision.panda.ums.utils.network.Client
import com.zetavision.panda.ums.utils.network.RxUtils
import com.zetavision.panda.ums.utils.network.UmsApi
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.litepal.crud.DataSupport
import java.util.concurrent.TimeUnit

/**
 * Created by wheroj on 2018/1/31.
 * @describe
 */
class UpKeepDetailActivity: BaseActivity() {

    var recyclerView: RecyclerView? = null
    private var formInfoDetail = FormInfoDetail()
    private lateinit var maintFormId: String

    private val tempers = arrayListOf("默认不填写", "晴", "多云", "多云转小雨", "小雨", "大雨", "暴雨", "小雪", "大雪", "暴雪")
    private val classes = arrayListOf("白班", "夜班")

    private var temperSpinnerAdapter: CommonSpinnerAdapter? = null
    private var classesSpinnerAdapter: CommonSpinnerAdapter? = null

    private var compositeDisposable = CompositeDisposable()
    override fun getContentLayoutId(): Int {
        return R.layout.activity_upkeep_detail
    }

    override fun init() {
        header.setLeftImage(R.mipmap.back)
        header.setRightText("保存数据", R.color.main_color)
        recyclerView = findViewById(R.id.activityUpKeepDetail_recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        maintFormId = intent.getStringExtra("maintFormId")



        loadData()
    }

    private fun loadData() {

        formInfoDetail = DataSupport.where("(formId = '$maintFormId')").findFirst(FormInfoDetail::class.java, true)
        if(formInfoDetail == null) {
            RxUtils.acquireString(Client.getApi(UmsApi::class.java).downloadMaintForm(maintFormId)
                    , object : RxUtils.DialogListener() {
                override fun onResult(result: Result) {
                    val formInfoDetails = result.getList(FormInfoDetail::class.java)
                    if (formInfoDetails != null && formInfoDetails.size > 0) {
                        recyclerView?.adapter = UpKeepDetailAdapter(formInfoDetails[0].formItemList)
                    }
                }
            })
        } else {
            recyclerView?.adapter = UpKeepDetailAdapter(formInfoDetail!!.formItemList)
        }

        initView()
    }

    @SuppressLint("WrongViewCast")
    private fun initView() {
        val temperSpinner = findViewById<Spinner>(R.id.activityUpKeepDetail_temperSpinner)
        val classesSpinner = findViewById<Spinner>(R.id.activityUpKeepDetail_temperSpinner)

        if (temperSpinnerAdapter == null) {
            temperSpinnerAdapter = CommonSpinnerAdapter(this)
            temperSpinner.adapter = temperSpinnerAdapter
            temperSpinnerAdapter!!.notifyDataSetChanged(tempers)
            temperSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    formInfoDetail.form.weather = tempers[position]
                }
            }
        }

        if (TextUtils.isEmpty(formInfoDetail.form.weather)) {
            temperSpinner.setSelection(0)
        } else {
            val indexOf = tempers.indexOf(formInfoDetail.form.weather)
            if (indexOf in tempers.indices) temperSpinner.setSelection(indexOf)
            else {
                formInfoDetail.form.weather = tempers[0]
                temperSpinner.setSelection(0)
            }
        }

        if (classesSpinnerAdapter == null) {
            classesSpinnerAdapter = CommonSpinnerAdapter(this)
            classesSpinner.adapter = classesSpinnerAdapter
            classesSpinnerAdapter!!.notifyDataSetChanged(classes)
            classesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    formInfoDetail.form.shift = classes[position]
                }
            }
        }
        if (TextUtils.isEmpty(formInfoDetail.form.shift)) {
            classesSpinner.setSelection(0)
        } else {
            val indexOf = classes.indexOf(formInfoDetail.form.shift)
            if (indexOf in classes.indices) classesSpinner.setSelection(indexOf)
            else {
                formInfoDetail.form.shift = classes[0]
                classesSpinner.setSelection(0)
            }
        }


        findViewById<TextView>(R.id.activityUpKeepDetail_deviceName).text = formInfoDetail.form.formCode
        findViewById<TextView>(R.id.activityUpKeepDetail_maintPeroid).text = formInfoDetail.form.maintPeriodName
        val etRemark = findViewById<EditText>(R.id.activityUpKeepDetail_etRemark)
        etRemark.setText(formInfoDetail.form.desc)
        etRemark.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                formInfoDetail.form.fillinRemarks = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })


        val btnMaintStatus = findViewById<Button>(R.id.activityUpKeepDetail_btnMaintStatus)
        val tvMaintStatus = findViewById<TextView>(R.id.activityUpKeepDetail_tvMaintStatus)
        val tvMaintStatusStr = findViewById<TextView>(R.id.activityUpKeepDetail_tvMaintStatusStr)
        when (formInfoDetail.form.status) {
            Constant.MAINT_FORM_STATUS_CLOSED -> tvMaintStatusStr.text = "表单状态：已结束"
            Constant.MAINT_FORM_STATUS_PLANNED -> {
                tvMaintStatusStr.text = "表单状态：已计划"
                btnMaintStatus.text = "保养开始"
                var drawable: Drawable = resources.getDrawable(R.mipmap.start)
//                drawable.bounds = Rect(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                btnMaintStatus.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
            }
            Constant.MAINT_FORM_STATUS_INPROGRESS -> {
                compositeDisposable.add(Observable.interval(0, 1, TimeUnit.SECONDS)
                        .subscribe {
                            val useTime = TimeUtils.getUseTime(System.currentTimeMillis() / 1000 - formInfoDetail.form.startTime)
                            tvMaintStatusStr.text = "表单状态：已进行".plus(useTime)
                        })

                btnMaintStatus.text = "保养完成"
                var drawable: Drawable = resources.getDrawable(R.mipmap.done)
//                drawable.bounds = Rect(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                btnMaintStatus.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
            }
            Constant.MAINT_FORM_STATUS_COMPLETED -> {
                compositeDisposable.dispose()
                compositeDisposable.clear()

                tvMaintStatusStr.text = "表单状态：已完成"
                btnMaintStatus.visibility = View.GONE
                tvMaintStatus.visibility = View.VISIBLE
            }
        }
        btnMaintStatus.setOnClickListener {
            when (formInfoDetail.form.status) {
                Constant.MAINT_FORM_STATUS_PLANNED -> {
                    formInfoDetail.form.status = Constant.MAINT_FORM_STATUS_INPROGRESS
                    formInfoDetail.form.startTime = System.currentTimeMillis() / 1000
                    val user = DataSupport.findLast(User::class.java)
                    if (user != null) formInfoDetail.form.startUser = user.username
                    initView()
                }
                Constant.MAINT_FORM_STATUS_INPROGRESS -> {
                    formInfoDetail.form.status = Constant.MAINT_FORM_STATUS_COMPLETED
                    formInfoDetail.form.completeTime = System.currentTimeMillis() / 1000
                    val user = DataSupport.findLast(User::class.java)
                    if (user != null) formInfoDetail.form.completeUser = user.username
                    initView()
                }
            }
        }
        findViewById<EditText>(R.id.activityUpKeepDetail_etRemark).setText(formInfoDetail.form.desc)
        findViewById<EditText>(R.id.activityUpKeepDetail_etRemark).setText(formInfoDetail.form.desc)
    }

    override fun getHasTitle(): Boolean {
        return true
    }

    override fun onLeftClick() {
        finish()
    }

    override fun onRightTextClick() {
        if (checkData()) {
            if (saveData()) {
                ToastUtils.show(R.string.save_success)
                finish()
            } else {
                ToastUtils.show(R.string.save_fail)
            }
        }
    }

    private fun checkData(): Boolean {
        for (i in formInfoDetail.formItemList.indices) {
            val formItem = formInfoDetail.formItemList[i]
            if ("N" == formItem.valueType) {
                try {
                    val intValue = formItem.presetValue.toInt()
                    val lowerLimit = formItem.lowerLimit.toInt()
                    val upperLimit = formItem.upperLimit.toInt()
                    if (intValue < lowerLimit || intValue > upperLimit) {
                        ToastUtils.show("序号" + (i+1) + "设定的值超出了范围")
                        return false
                    }
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    return false
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                    return false
                }
            }
        }
        return true
    }

    private fun saveData(): Boolean {
        var isSuccessSave: Boolean
        formInfoDetail.formItemList.indices
                .map { formInfoDetail.formItemList[it] }
                .forEach {
                    isSuccessSave = it.saveOrUpdate("(formId='${it.formId}')")
                    if (!isSuccessSave) return isSuccessSave
                }

        val formInfo = formInfoDetail.form
        isSuccessSave = formInfo.saveOrUpdate("(formId='${formInfo.formId}')")
        if (!isSuccessSave) return isSuccessSave
        formInfoDetail.isUpload = 1
        return formInfoDetail.saveOrUpdate("(formId='${formInfoDetail.formId}')")
    }
}