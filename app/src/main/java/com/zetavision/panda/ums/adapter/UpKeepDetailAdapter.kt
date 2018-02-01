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

/**
 * Created by wheroj on 2018/1/31 17:08.
 * @describe
 */
class UpKeepDetailAdapter(data: List<FormItem>): BaseQuickAdapter<FormItem, BaseViewHolder>(R.layout.item_upkeep_param, data) {
    override fun convert(helper: BaseViewHolder?, item: FormItem?) {
        if (item != null) {
            helper?.setText(R.id.itemUpKeepParam_order, (data.indexOf(item) + 1).toString())
                    ?.setText(R.id.itemUpKeepParam_code, item.basicActionName)
                    ?.setText(R.id.itemUpKeepParam_unit, item.unit)

            var rlInput = helper?.getView<RelativeLayout>(R.id.itemUpKeepParam_rlInput)
            var etInput = helper?.getView<EditText>(R.id.itemUpKeepParam_etInput)
            var inputNotice = helper?.getView<TextView>(R.id.itemUpKeepParam_inputNotice)
            var inputNoticeImg = helper?.getView<ImageView>(R.id.itemUpKeepParam_inputNoticeImg)
            var etRemark = helper?.getView<EditText>(R.id.itemUpKeepParam_remark)
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

            var rlChoose = helper?.getView<RelativeLayout>(R.id.itemUpKeepParam_choose)
            var spinner = helper?.getView<Spinner>(R.id.itemUpKeepParam_spinner)

            when (item.valueType) {
                "N", "T" -> {
                    rlInput?.visibility = View.VISIBLE
                    rlChoose?.visibility = View.GONE
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
                                    val intValue = s.toString().toInt()
                                    val lowerLimit = item.lowerLimit.toInt()
                                    val upperLimit = item.upperLimit.toInt()
                                    if (intValue < lowerLimit || intValue > upperLimit) {
                                        inputNotice?.visibility = View.VISIBLE
                                        inputNoticeImg?.visibility = View.VISIBLE
                                        etInput.setBackgroundResource(R.drawable.radius_red_5)
                                    } else {
                                        inputNotice?.visibility = View.GONE
                                        inputNoticeImg?.visibility = View.INVISIBLE
                                        etInput.setBackgroundResource(R.drawable.radius_blue_5)
                                        item.presetValue = s.toString()
                                    }
                                } catch (e: NumberFormatException) {
                                    e.printStackTrace()
                                } catch (e: NullPointerException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    })
                }
                "O", "B" -> {
                    rlInput?.visibility = View.GONE
                    rlChoose?.visibility = View.VISIBLE

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
            }
        }
    }
}