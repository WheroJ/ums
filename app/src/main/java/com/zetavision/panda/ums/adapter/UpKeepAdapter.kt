package com.zetavision.panda.ums.adapter

import android.widget.LinearLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.model.FormInfo
import com.zetavision.panda.ums.utils.Constant
import com.zetavision.panda.ums.utils.IntentUtils

/**
 * Created by wheroj on 2018/1/30.
 * @describeb
 */
class UpKeepAdapter(data: List<FormInfo>): BaseQuickAdapter<FormInfo, BaseViewHolder>(R.layout.list_upkeep_item, data) {

    private var statusMap: HashMap<String, String> = HashMap()
    init {
        statusMap[Constant.MAINT_FORM_STATUS_PLANNED] = "已计划"
        statusMap[Constant.MAINT_FORM_STATUS_INPROGRESS] = "进行中"
        statusMap[Constant.MAINT_FORM_STATUS_COMPLETED] = "已完成"
        statusMap[Constant.MAINT_FORM_STATUS_CLOSED] = "已结束"
    }

    override fun convert(helper: BaseViewHolder?, item: FormInfo?) {
        if (helper != null) {
            helper.setText(R.id.itemUpKeep_tvDeviceName, item?.equipmentCode)
                    .setText(R.id.itemUpKeep_tvFormCode, item?.formCode)
                    .setText(R.id.itemUpKeep_tvMaintPeriod, item?.maintPeriodName)
                    .setText(R.id.itemUpKeep_tvMaintPeriodDesc, item?.maintPeriodDescription)
                    .setText(R.id.itemUpKeep_tvPlanDate, item?.planDate)
                    .setText(R.id.itemUpKeep_tvStatus, statusMap[item?.status])

            helper.getView<LinearLayout>(R.id.itemUpKeep_content)
                    .setOnClickListener{
                        IntentUtils.goUpKeepDetail(mContext, item!!.formId)
                    }
        }

    }
}