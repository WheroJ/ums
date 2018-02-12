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
class UpKeepListAdapter(data: List<FormInfo>, val actionType: String): BaseQuickAdapter<FormInfo, BaseViewHolder>(R.layout.list_upkeep_item, data) {

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
            helper.setText(R.id.itemUpKeep_tvDeviceName, item?.equipmentCode )
                    .setText(R.id.itemUpKeep_tvFormCode, item?.formCode)
                    .setText(R.id.itemUpKeep_tvMaintPeriod, item?.maintPeriodName)
                    .setText(R.id.itemUpKeep_tvMaintPeriodDesc, item?.maintPeriodDescription)
                    .setText(R.id.itemUpKeep_tvPlanDate, item?.planDate)
                    .setText(R.id.itemUpKeep_tvStatus, statusMap[item?.status])
                    .setText(R.id.itemUpKeep_tvDeviceDescription, item?.equipmentName)

            helper.getView<LinearLayout>(R.id.itemUpKeep_content)
                    .setOnClickListener{
                        IntentUtils.goUpKeepDetail(mContext, item!!.formId)
                    }
        }

    }
}