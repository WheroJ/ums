package com.zetavision.panda.ums.ui.upkeep

import android.annotation.SuppressLint
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.widget.TextView
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.adapter.UpKeepListAdapter
import com.zetavision.panda.ums.fragments.base.BaseFragment
import com.zetavision.panda.ums.model.FormInfo
import com.zetavision.panda.ums.utils.Constant
import com.zetavision.panda.ums.utils.IntentUtils
import com.zetavision.panda.ums.utils.LoadingDialog
import com.zetavision.panda.ums.utils.ToastUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.litepal.crud.DataSupport

/**
 * Created by wheroj on 2018/3/29 17:11.
 * @describe
 */
class UpKeepListFragment: BaseFragment() {

    private var deviceName: String? = null
    private var actionType: String? = null
    private var status: String? = null
    private var recyclerView: RecyclerView? = null

    override fun getContentLayoutId(): Int {
        return R.layout.fragment_upkeeplist
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
            val sql = if (!TextUtils.isEmpty(deviceName)) {
                if (!TextUtils.isEmpty(status)) {
                    "equipmentCode = '$deviceName' and actionType='$actionType' and status = '$status'"
                } else {
                    "equipmentCode = '$deviceName' and actionType='$actionType'"
                }
            } else {
                if (!TextUtils.isEmpty(status)) {
                    "actionType='$actionType' and status = '$status'"
                } else "actionType='$actionType'"
            }
            var formList = ArrayList<FormInfo>()
            val elements = DataSupport.where(sql).find(FormInfo::class.java)
            elements.indices
                    .filter {
                        (Constant.FORM_STATUS_INPROGRESS == elements[it].status
                                || Constant.FORM_STATUS_PLANNED == elements[it].status)
                    }
                    .mapTo(formList) { elements[it] }
            emitter.onNext(formList)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.isEmpty()) {
                        if (!TextUtils.isEmpty(actionType)) {
                            val adapter = UpKeepListAdapter(ArrayList(), actionType!!)
                            recyclerView?.adapter = adapter
                        }
                        ToastUtils.show(R.string.no_data)
                    } else if (it.size == 1 && (arguments != null && firstView)) {
                        IntentUtils.goUpKeepDetail(context, it[0].formId)
                        arguments.remove("firstView")
                    } else {
                        if (!TextUtils.isEmpty(actionType)) {
                            it.sort()
                            val adapter = UpKeepListAdapter(it, actionType!!)
                            recyclerView?.adapter = adapter
                        }
                    }

                    loadingDialog.dismiss()
                }
    }
}