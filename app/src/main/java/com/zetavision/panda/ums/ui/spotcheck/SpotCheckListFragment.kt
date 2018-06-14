package com.zetavision.panda.ums.ui.spotcheck

import android.annotation.SuppressLint
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ListView
import android.widget.TextView
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.adapter.SpotCheckListAdapter
import com.zetavision.panda.ums.adapter.SpotCheckSwipeListAdapter
import com.zetavision.panda.ums.fragments.base.BaseFragment
import com.zetavision.panda.ums.model.FormInfo
import com.zetavision.panda.ums.model.FormInfoDetail
import com.zetavision.panda.ums.model.FormItem
import com.zetavision.panda.ums.utils.Constant
import com.zetavision.panda.ums.utils.IntentUtils
import com.zetavision.panda.ums.utils.ToastUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
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
    private var listView: ListView? = null
    private var downloadedAdapter: SpotCheckSwipeListAdapter? = null

    override fun getContentLayoutId(): Int {
        return R.layout.fragment_spotchecklist
    }

    override fun init() {

        if (arguments != null) {
            deviceName = arguments.getString("deviceName")
            actionType = arguments.getString("actionType")
            status = arguments.getString("status")

            recyclerView = contentView.findViewById(R.id.recyclerView)
            listView = contentView.findViewById(R.id.listView)
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

        val firstView = arguments.getBoolean("firstView", false)
//        val loadingDialog = LoadingDialog()
//        if (isAdded) {
//            loadingDialog.show(fragmentManager, null)
//        }

        ToastUtils.showLong("Loading")
        Observable.create<ArrayList<FormInfo>> {
            emitter ->
            val where = if (!TextUtils.isEmpty(status) && !TextUtils.equals(status, Constant.FORM_STATUS_ALL)) {
                "actionType='$actionType' and status = '$status'"
            } else {
                "actionType='$actionType'"
            }
            val formInfos = DataSupport.where(where).find(FormInfo::class.java)

            val formIds = ArrayList<String>()
            val formList = ArrayList<FormInfo>()

            if (!TextUtils.equals(status, Constant.FORM_STATUS_ALL)) {
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
            } else emitter.onNext(formInfos as ArrayList<FormInfo>)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    it ->
                    if (it.isEmpty()) {
                        if (!TextUtils.isEmpty(actionType)) {
                            val adapter = SpotCheckListAdapter(ArrayList(), !TextUtils.equals(status, Constant.FORM_STATUS_ALL))
                            recyclerView?.adapter = adapter
                        }
                        ToastUtils.show(R.string.no_data)
                    } else if (it.size == 1 && arguments != null && firstView) {
                        IntentUtils.goSpotCheckDetail(context, it[0].formId)
                        arguments.remove("firstView")
                    } else {
                        if (!TextUtils.isEmpty(actionType)) {
                            it.sort()
                            initList(it)
                        }
                    }
//                    if (isAdded) {
//                        loadingDialog.dismiss()
//                    }
        }
    }

    private fun initList(data: ArrayList<FormInfo>) {
        if (TextUtils.equals(status, Constant.FORM_STATUS_ALL)) {
            if (downloadedAdapter == null) {
                downloadedAdapter = SpotCheckSwipeListAdapter(data, activity)
                downloadedAdapter!!.setOnDeleteClickListener(object : SpotCheckSwipeListAdapter.OnDeleteClickListener {
                    override fun deleteItem(position: Int) {
                        showConfirm(data, position)
                    }
                })
                listView?.adapter = downloadedAdapter
            } else {
                downloadedAdapter!!.updateDatas(data)
            }
            recyclerView?.visibility = View.GONE
            listView?.visibility = View.VISIBLE
        } else {
            recyclerView?.visibility = View.VISIBLE
            listView?.visibility = View.GONE
            val adapter = SpotCheckListAdapter(data, !TextUtils.equals(status, Constant.FORM_STATUS_ALL))
            recyclerView?.adapter = adapter
        }
    }

    private fun showConfirm(data: ArrayList<FormInfo>, position: Int) {
        val builder = AlertDialog.Builder(activity)

        builder.setMessage(R.string.sure_del)
        builder.setPositiveButton(R.string.sure) { dialog, which ->
            val formInfo = data[position]
            DataSupport.deleteAll(FormInfoDetail::class.java, "formId = ${formInfo.formId}")
            DataSupport.deleteAll(FormInfo::class.java, "formId = ${formInfo.formId}")
            DataSupport.deleteAll(FormItem::class.java, "formId = ${formInfo.formId}")

            data.removeAt(position)
            initList(data)
            EventBus.getDefault().post(Constant.UPDATE_DOWN_COUNT)
            dialog.dismiss()
        }

        builder.setNegativeButton(R.string.cancel) {
            dialog, which -> dialog.dismiss()
        }

        if (isAdded) builder.create().show()
    }
}