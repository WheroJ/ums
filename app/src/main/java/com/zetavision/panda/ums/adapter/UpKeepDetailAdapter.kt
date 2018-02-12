package com.zetavision.panda.ums.adapter

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.model.FormItem
import com.zetavision.panda.ums.utils.Constant

/**
 * Created by wheroj on 2018/1/31 17:08.
 * @describe
 */
class UpKeepDetailAdapter(data: List<FormItem>, var status: String): BaseQuickAdapter<FormItem, BaseViewHolder>(R.layout.item_upkeep_param, data) {
    override fun convert(helper: BaseViewHolder?, item: FormItem?) {
        if (item != null) {
            helper?.setText(R.id.itemUpKeepParam_order, (data.indexOf(item) + 1).toString())
                    ?.setText(R.id.itemUpKeepParam_code, item.basicActionName)
                    ?.setText(R.id.itemUpKeepParam_unit, item.unit)

            val tvValue = helper?.getView<TextView>(R.id.itemUpKeepParam_tvValue)
            val tvRemark = helper?.getView<TextView>(R.id.itemUpKeepParam_tvRemark)
            val rlInput = helper?.getView<RelativeLayout>(R.id.itemUpKeepParam_rlInput)
            val etInput = helper?.getView<EditText>(R.id.itemUpKeepParam_etInput)
            val inputNotice = helper?.getView<TextView>(R.id.itemUpKeepParam_inputNotice)
            val inputNoticeImg = helper?.getView<ImageView>(R.id.itemUpKeepParam_inputNoticeImg)
            val rlChoose = helper?.getView<RelativeLayout>(R.id.itemUpKeepParam_choose)
            val spinner = helper?.getView<Spinner>(R.id.itemUpKeepParam_spinner)
            val etRemark = helper?.getView<EditText>(R.id.itemUpKeepParam_remark)
            when(status) {
                Constant.FORM_STATUS_COMPLETED,
                    Constant.FORM_STATUS_CLOSED,
                    Constant.FORM_STATUS_PLANNED -> {
                    if("B" == item.valueType) {
                        val list = item.optionValues.split("|")
                        val valueList = item.optionValuesDescription.split("|")
                        val indexOf = list.indexOf(item.presetValue)
                        if(indexOf in valueList.indices) {
                            tvValue?.text = valueList[indexOf]
                        }
                    } else {
                        tvValue?.text = item.presetValue.toString()
                    }
                    tvRemark?.text = item.remarks
                    tvValue?.visibility = View.VISIBLE
                    tvRemark?.visibility = View.VISIBLE
                    rlInput?.visibility = View.GONE
                    rlChoose?.visibility = View.GONE
                    etRemark?.visibility = View.GONE
                }
                Constant.FORM_STATUS_INPROGRESS -> {
                    when(item.valueType) {
                        "N", "T" -> {
                            rlInput?.visibility = View.VISIBLE
                            rlChoose?.visibility = View.GONE
                        }
                        "O", "B" -> {
                            rlInput?.visibility = View.GONE
                            rlChoose?.visibility = View.VISIBLE
                        }
                    }
                    tvValue?.visibility = View.GONE
                    tvRemark?.visibility = View.GONE
                    etRemark?.visibility = View.VISIBLE
                }
            }

            etRemark?.setText(item.remarks)
            etRemark?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    item.remarks = s.toString()
                }
            })

            when (item.valueType) {
                "N", "T" -> {
                    helper?.setText(R.id.itemUpKeepParam_lowerLimit, item.lowerLimit)
                            ?.setText(R.id.itemUpKeepParam_upperLimit, item.upperLimit)
                    if (!TextUtils.isEmpty(item.presetValue)) {
                        etInput?.setText(item.presetValue)
                    }
                    etInput?.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            if ("T" == item.valueType) {
                                item.presetValue = s.toString()
                            } else {
                                try {
                                    if (!TextUtils.isEmpty(s)) {
                                        val intValue = s.toString().toFloat()
                                        val lowerLimit = item.lowerLimit.toFloat()
                                        val upperLimit = item.upperLimit.toFloat()
                                        when {
                                            intValue < lowerLimit -> {
                                                inputNotice?.visibility = View.VISIBLE
                                                inputNoticeImg?.visibility = View.VISIBLE
                                                inputNotice?.text = mContext.resources.getString(R.string.too_lower)
                                                etInput.setBackgroundResource(R.drawable.radius_red_5)
                                                item.presetValue = s.toString()
                                            }
                                            intValue > upperLimit -> {
                                                inputNotice?.visibility = View.VISIBLE
                                                inputNoticeImg?.visibility = View.VISIBLE
                                                inputNotice?.text = mContext.resources.getString(R.string.too_upper)
                                                etInput.setBackgroundResource(R.drawable.radius_red_5)
                                                item.presetValue = s.toString()
                                            }
                                            else -> {
                                                inputNotice?.visibility = View.GONE
                                                inputNoticeImg?.visibility = View.INVISIBLE
                                                etInput.setBackgroundResource(R.drawable.radius_blue_5)
                                                item.presetValue = s.toString()
                                            }
                                        }
                                    } else {
                                        inputNotice?.visibility = View.GONE
                                        inputNoticeImg?.visibility = View.INVISIBLE
                                        etInput.setBackgroundResource(R.drawable.radius_blue_5)
                                        item.presetValue = s.toString()
                                    }
                                } catch (e: NumberFormatException) {
                                    e.printStackTrace()
                                    item.presetValue = s.toString()
                                } catch (e: NullPointerException) {
                                    item.presetValue = ""
                                    e.printStackTrace()
                                }
                            }
                        }
                    })
                }
                "O" -> {
                    val list = item.optionValues.split("|")
                    val spinnerAdapter = CommonSpinnerAdapter(mContext)
                    spinner?.adapter = spinnerAdapter
                    spinnerAdapter.notifyDataSetChanged(list)
                    if (!TextUtils.isEmpty(item.presetValue)) {
                        val indexOf = list.indexOf(item.presetValue)
                        if (indexOf in list.indices)
                            spinner?.setSelection(indexOf)
                        else {
                            if (list.isNotEmpty())
                                spinner?.setSelection(0)
                        }
                    } else {
                        if (list.isNotEmpty())
                            spinner?.setSelection(0)
                    }

                    spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                            item.presetValue = list[position]
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {

                        }
                    }
                }
                "B" -> {
                    val valueList = item.optionValues.split("|")
                    val list = item.optionValuesDescription.split("|")
                    val spinnerAdapter = CommonSpinnerAdapter(mContext)
                    spinner?.adapter = spinnerAdapter
                    spinnerAdapter.notifyDataSetChanged(list)
                    if (!TextUtils.isEmpty(item.presetValue)) {
                        val indexOf = valueList.indexOf(item.presetValue)
                        if (indexOf in list.indices)
                            spinner?.setSelection(indexOf)
                        else {
                            if (list.isNotEmpty())
                                spinner?.setSelection(0)
                        }
                    } else {
                        if (list.isNotEmpty())
                            spinner?.setSelection(0)
                    }

                    spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                            item.presetValue = valueList[position]
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {

                        }
                    }
                }
            }
        }
    }

    fun updateData(status: String) {
        this.status = status
        notifyDataSetChanged()
    }
}