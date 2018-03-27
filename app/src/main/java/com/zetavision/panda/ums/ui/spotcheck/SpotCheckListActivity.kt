package com.zetavision.panda.ums.ui.spotcheck

import android.annotation.SuppressLint
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.widget.TextView
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.adapter.SpotCheckListAdapter
import com.zetavision.panda.ums.base.BaseActivity
import com.zetavision.panda.ums.model.FormInfo
import com.zetavision.panda.ums.model.FormItem
import com.zetavision.panda.ums.utils.Constant
import com.zetavision.panda.ums.utils.IntentUtils
import com.zetavision.panda.ums.utils.ToastUtils
import org.litepal.crud.DataSupport

/**
 * Created by wheroj on 2018/1/30.
 * @describeb
 */
class SpotCheckListActivity : BaseActivity() {

    private var deviceName: String? = null
    private var actionType: String? = null
    private var recyclerView:RecyclerView? = null

    override fun getContentLayoutId(): Int {
        return R.layout.activity_spotchecklist
    }

    override fun init() {

        header.setLeftImage(R.mipmap.back)

        deviceName = intent.extras.getString("deviceName")
        actionType = intent.extras.getString("actionType")
        if (FormInfo.ACTION_TYPE_M == actionType) {
            header.setTitle(getString(R.string.common_device).plus(deviceName).plus(getString(R.string.maint_form)))
        } else {
            header.setTitle(getString(R.string.common_device).plus(deviceName).plus(getString(R.string.spotcheck_form)))
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        changeLabelByActionType()
    }

    @SuppressLint("WrongViewCast")
    private fun changeLabelByActionType() {
        if (FormInfo.ACTION_TYPE_M == actionType) {
            findViewById<TextView>(R.id.label1).text = getString(R.string.device_code)
            findViewById<TextView>(R.id.label2).text = getString(R.string.form_number)
            findViewById<TextView>(R.id.label3).text = getString(R.string.maintain_cycle)
            findViewById<TextView>(R.id.label4).text = getString(R.string.maintain_cycle_desc)
            findViewById<TextView>(R.id.label5).text = getString(R.string.maintain_plan_date)
        } else {
            findViewById<TextView>(R.id.label1).text = getString(R.string.spotcheck_route)
            findViewById<TextView>(R.id.label2).text = getString(R.string.spotcheck_route_desc)
            findViewById<TextView>(R.id.label3).text = getString(R.string.spotcheck_period_desc)
            findViewById<TextView>(R.id.label4).text = getString(R.string.spotcheck_formcode)
            findViewById<TextView>(R.id.label5).text = getString(R.string.spotcheck_date)
        }
    }

    override fun onLeftClick() {
        finish()
    }

    override fun getHasTitle(): Boolean {
        return true
    }

    override fun onResume() {
        super.onResume()

        var formList = ArrayList<FormInfo>()
        var where =  "(equipmentCode = '$deviceName')"
        val itemList = DataSupport.where(where).find(FormItem::class.java)

        var formIds = ArrayList<String>()
        for (i in itemList.indices) {
            val formId = itemList[i].formId
            if (!formIds.contains(formId)) {
                formIds.add(formId)
                val sql = "formId='$formId' and actionType='$actionType'"
                val formInfo = DataSupport.where(sql).findFirst(FormInfo::class.java)
                if (formInfo != null) {
                    if (Constant.FORM_STATUS_INPROGRESS == formInfo.status
                            || Constant.FORM_STATUS_PLANNED == formInfo.status) {
                        formList.add(formInfo)
                    }
                }
            }
        }

        if (formList.isEmpty()) {
            if (!TextUtils.isEmpty(actionType)) {
                val adapter = SpotCheckListAdapter(ArrayList(), actionType!!)
                recyclerView?.adapter = adapter
            }
            ToastUtils.show(R.string.no_data)
        } else if (formList.size == 1 && intent.getBooleanExtra("firstView", false)) {
            IntentUtils.goSpotCheckDetail(`this`, formList[0].formId)
            intent.removeExtra("firstView")
        } else {
            if (!TextUtils.isEmpty(actionType)) {
                formList.sort()
                val adapter = SpotCheckListAdapter(formList, actionType!!)
                recyclerView?.adapter = adapter
            }
        }
    }
}