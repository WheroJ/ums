package com.zetavision.panda.ums.ui.spotcheck

import android.annotation.SuppressLint
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.widget.TextView
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.adapter.SpotCheckListAdapter
import com.zetavision.panda.ums.fragments.base.BaseFragment
import com.zetavision.panda.ums.model.FormInfo
import com.zetavision.panda.ums.model.FormItem
import com.zetavision.panda.ums.utils.Constant
import com.zetavision.panda.ums.utils.IntentUtils
import com.zetavision.panda.ums.utils.LoadingDialog
import com.zetavision.panda.ums.utils.ToastUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.litepal.crud.DataSupport

/**
 * Created by wheroj on 2018/3/29 16:56.
 * @describe
 */
class SpotCheckListFragment: BaseFragment() {

    private var deviceName: String? = null
    private var actionType: String? = null
    private var status: String? = null
    private var recyclerView: RecyclerView? = null

    override fun getContentLayoutId(): Int {
        return R.layout.fragment_spotchecklist
    }

    override fun init() {

        if (arguments != null) {
            deviceName = arguments.getString("deviceName")
            actionType = arguments.getString("actionType")
            status = arguments.getString("status")

            recyclerView = contentView.findViewById(R.id.recyclerView)
            recyclerView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            changeLabelByActionType()
        }
    }

    @SuppressLint("WrongViewCast")
    private fun changeLabelByActionType() {
        if (FormInfo.ACTION_TYPE_M == actionType) {
            contentView.findViewById<TextView>(R.id.label1).text = getString(R.string.device_code)
            contentView.findViewById<TextView>(R.id.label2).text = getString(R.string.form_number)
            contentView.findViewById<TextView>(R.id.label3).text = getString(R.string.maintain_cycle)
            contentView.findViewById<TextView>(R.id.label4).text = getString(R.string.maintain_cycle_desc)
            contentView.findViewById<TextView>(R.id.label5).text = getString(R.string.maintain_plan_date)
        } else {
            contentView.findViewById<TextView>(R.id.label1).text = getString(R.string.spotcheck_route)
            contentView.findViewById<TextView>(R.id.label2).text = getString(R.string.spotcheck_route_desc)
            contentView.findViewById<TextView>(R.id.label3).text = getString(R.string.spotcheck_period_desc)
            contentView.findViewById<TextView>(R.id.label4).text = getString(R.string.spotcheck_formcode)
            contentView.findViewById<TextView>(R.id.label5).text = getString(R.string.spotcheck_date)
        }
    }

    override fun getHasTitle(): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()

        val loadingDialog = LoadingDialog()

        val firstView = arguments.getBoolean("firstView", false)
        loadingDialog.show(fragmentManager, null)

        Observable.create<ArrayList<FormInfo>> {
            emitter ->
            val where = if (!TextUtils.isEmpty(status)) {
                "actionType='$actionType' and status = '$status'"
            } else {
                "actionType='$actionType'"
            }
            val formInfos = DataSupport.where(where).find(FormInfo::class.java)

            val formIds = ArrayList<String>()
            val formList = ArrayList<FormInfo>()

            for (i in formInfos.indices) {
                val formInfo = formInfos[i]
                if (Constant.FORM_STATUS_INPROGRESS == formInfo.status
                        || Constant.FORM_STATUS_PLANNED == formInfo.status) {
                    if (!formIds.contains(formInfo.formId)) {
                        var sql = if (!TextUtils.isEmpty(deviceName)) "equipmentCode = '$deviceName'" else ""
                        if (!TextUtils.isEmpty(sql)) {
                            sql += " and formId='${formInfo.formId}'"
                        } else {
                            sql = "formId='${formInfo.formId}'"
                        }
                        val list = DataSupport.where(sql).find(FormItem::class.java)
                        if (!list.isEmpty()) {
                            formList.add(formInfo)
                        }
                        formIds.add(formInfo.formId)
                    }
                }
            }

            emitter.onNext(formList)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    it ->
                    if (it.isEmpty()) {
                        if (!TextUtils.isEmpty(actionType)) {
                            val adapter = SpotCheckListAdapter(ArrayList(), actionType!!)
                            recyclerView?.adapter = adapter
                        }
                        ToastUtils.show(R.string.no_data)
                    } else if (it.size == 1 && arguments != null && firstView) {
                        IntentUtils.goSpotCheckDetail(context, it[0].formId)
                        arguments.remove("firstView")
                    } else {
                        if (!TextUtils.isEmpty(actionType)) {
                            it.sort()
                            val adapter = SpotCheckListAdapter(it, actionType!!)
                            recyclerView?.adapter = adapter
                        }
                    }
                    loadingDialog.dismiss()

        }
    }
}