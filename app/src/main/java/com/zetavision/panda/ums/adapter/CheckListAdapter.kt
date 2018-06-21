package com.zetavision.panda.ums.adapter

import android.widget.LinearLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.model.FormInfo
import com.zetavision.panda.ums.utils.Constant
import com.zetavision.panda.ums.utils.IntentUtils
import com.zetavision.panda.ums.utils.UIUtils

/**
 * Created by wheroj on 2018/1/30.
 * @describeb
 */
class CheckListAdapter(data: List<FormInfo>, val actionType: String): BaseQuickAdapter<FormInfo, BaseViewHolder>(R.layout.list_upkeep_item, data) {

    private var statusMap: HashMap<String, String> = HashMap()
    init {
        var context = UIUtils.getContext()
        statusMap[Constant.FORM_STATUS_PLANNED] = context.getString(R.string.status_planed)
        statusMap[Constant.FORM_STATUS_INPROGRESS] = context.getString(R.string.status_inprogress)
        statusMap[Constant.FORM_STATUS_COMPLETED] = context.getString(R.string.status_complete)
        statusMap[Constant.FORM_STATUS_CLOSED] = context.getString(R.string.status_closed)
    }

    override fun convert(helper: BaseViewHolder?, item: FormInfo?) {
        if (helper != null) {
            helper.setText(R.id.itemUpKeep_tvDeviceName
                    , if (FormInfo.ACTION_TYPE_M == actionType) item?.equipmentCode else item?.inspectRouteCode)
                    .setText(R.id.itemUpKeep_tvFormCode
                            , if (FormInfo.ACTION_TYPE_M == actionType) item?.formCode else item?.inspectRouteDescription)
                    .setText(R.id.itemUpKeep_tvMaintPeriod
                            , if (FormInfo.ACTION_TYPE_M == actionType) item?.maintPeriodName else item?.inspectPeriodDescription)
                    .setText(R.id.itemUpKeep_tvMaintPeriodDesc
                            , if (FormInfo.ACTION_TYPE_M == actionType) item?.maintPeriodDescription else item?.formCode )
                    .setText(R.id.itemUpKeep_tvPlanDate, item?.planDate)
                    .setText(R.id.itemUpKeep_tvStatus, statusMap[item?.status])

            helper.getView<LinearLayout>(R.id.itemUpKeep_content)
                    .setOnClickListener{
                        if (FormInfo.ACTION_TYPE_M == actionType) {
                            IntentUtils.goUpKeepDetail(mContext, item!!.formId)
                        } else {
                            IntentUtils.goSpotCheckDetail(mContext, item!!.formId)
                        }
                    }
        }

    }
}