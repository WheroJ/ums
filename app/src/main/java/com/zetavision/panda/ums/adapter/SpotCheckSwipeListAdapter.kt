package com.zetavision.panda.ums.adapter

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.adapters.BaseSwipeAdapter
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.model.FormInfo
import com.zetavision.panda.ums.utils.Constant
import com.zetavision.panda.ums.utils.UIUtils

/**
 * Created by wheroj on 2018/6/13 15:03.
 * @describe
 */
class SpotCheckSwipeListAdapter(private var mData: ArrayList<FormInfo>?, val mContext: Context): BaseSwipeAdapter() {

    private var statusMap: HashMap<String, String> = HashMap()
    init {
        var context = UIUtils.getContext()
        statusMap[Constant.FORM_STATUS_PLANNED] = context.getString(R.string.status_planed)
        statusMap[Constant.FORM_STATUS_INPROGRESS] = context.getString(R.string.status_inprogress)
        statusMap[Constant.FORM_STATUS_COMPLETED] = context.getString(R.string.status_complete)
        statusMap[Constant.FORM_STATUS_CLOSED] = context.getString(R.string.status_closed)
    }

    fun updateDatas(datas: ArrayList<FormInfo>) {
        mData = datas
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): FormInfo? {
        return mData?.get(position)
    }

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.spotcheck_swipe
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        if (mData == null || mData!!.isEmpty()) {
            return 0
        }
        return mData!!.size
    }

    override fun generateView(position: Int, parent: ViewGroup?): View {
        return View.inflate(mContext, R.layout.list_spotcheck_swipeitem, null)
    }

    override fun fillValues(position: Int, convertView: View?) {
        val swipeLayout = convertView?.findViewById<SwipeLayout>(R.id.spotcheck_swipe)
        val llDel = convertView?.findViewById<LinearLayout>(R.id.spotcheck_llDel)
        val item = getItem(position)
        if (swipeLayout != null) {
            swipeLayout.close()
            if (TextUtils.equals(item?.status, Constant.FORM_STATUS_PLANNED)) {
                swipeLayout.isSwipeEnabled = true
                swipeLayout.addDrag(SwipeLayout.DragEdge.Right, llDel)
                llDel?.setOnClickListener {
                    if (mListener != null)
                        mListener!!.deleteItem(position)
                }
            } else {
                swipeLayout.isSwipeEnabled = false
            }
        }


        val tvInspectRoute = convertView?.findViewById<TextView>(R.id.itemSpotCheckParam_tvInspectRoute)
        val tvInspectRouteDesc = convertView?.findViewById<TextView>(R.id.itemSpotCheckParam_tvInspectRouteDesc)
        val tvInspectPeriodCode = convertView?.findViewById<TextView>(R.id.itemSpotCheckParam_tvInspectPeroidCode)
        val tvInspectPeriodCodeDesc = convertView?.findViewById<TextView>(R.id.itemSpotCheckParam_tvInspectPeroidCodeDesc)
        val tvFormCode = convertView?.findViewById<TextView>(R.id.itemSpotCheckParam_tvFormCode)
        val tvPlanDate = convertView?.findViewById<TextView>(R.id.itemSpotCheckParam_tvPlanDate)
        val tvStatus = convertView?.findViewById<TextView>(R.id.itemSpotCheckParam_tvStatus)

        tvInspectRoute?.text = item?.inspectRouteCode
        tvInspectRouteDesc?.text = item?.inspectRouteName
        tvInspectPeriodCode?.text = item?.inspectPeriodCode
        tvInspectPeriodCodeDesc?.text = item?.inspectPeriodName
        tvFormCode?.text = item?.formCode
        tvPlanDate?.text = item?.planDate
        tvStatus?.text = statusMap[item?.status]
    }

    interface OnDeleteClickListener {
        fun deleteItem(position: Int)
    }

    private var mListener: OnDeleteClickListener? = null
    fun setOnDeleteClickListener(listener: OnDeleteClickListener) {
        mListener = listener
    }
}