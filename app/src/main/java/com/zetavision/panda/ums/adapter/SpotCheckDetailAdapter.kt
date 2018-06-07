package com.zetavision.panda.ums.adapter

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
import com.chad.library.adapter.base.BaseViewHolder
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.base.TextWatcherImpl
import com.zetavision.panda.ums.model.FormInfoDetail
import com.zetavision.panda.ums.model.FormItem
import com.zetavision.panda.ums.utils.Constant
import com.zetavision.panda.ums.utils.IntentUtils
import com.zetavision.panda.ums.utils.UIUtils


/**
 * Created by wheroj on 2018/1/31 17:08.
 * @describe
 */
class SpotCheckDetailAdapter(val formInfoDetail: FormInfoDetail, val context: Activity): BaseQuickAdapter<FormItem, BaseViewHolder>(R.layout.item_spotcheck_param, formInfoDetail.formItemList) {

    private var status: String
    init {
        status = formInfoDetail.form.status
    }

    var watcher: TextWatcherImpl? = null
    private val ITEM: Int = 1
    private val REMARK: Int = 2

    override fun convert(helper: BaseViewHolder?, item: FormItem?) {
        if (item != null) {
            initType1View(helper, item)
        } else {
            initType2View(helper)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder {
        this.mContext = parent?.context
        this.mLayoutInflater = LayoutInflater.from(mContext)
        return when (viewType) {
            ITEM -> createBaseViewHolder(View.inflate(context, R.layout.item_spotcheck_param, null))
            REMARK -> createBaseViewHolder(mLayoutInflater.inflate(R.layout.item_remark, parent, false))
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == mData.size) {
            REMARK
        } else ITEM
    }

    /**
     * 加上最后备注的item
     */
    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    private fun initType2View(helper: BaseViewHolder?) {
        val etRemark = helper?.getView<EditText>(R.id.activitySpotCheckDetail_etRemark)
        val tvRemark = helper?.getView<TextView>(R.id.activitySpotCheckDetail_tvRemark)
        helper?.getView<TextView>(R.id.activityFormDetail_tvRemark)?.text = mContext.getString(R.string.spotcheck_remark)

        if (Constant.FORM_STATUS_CLOSED == status
                || Constant.FORM_STATUS_COMPLETED == status
                || Constant.FORM_STATUS_PLANNED == status) {
            tvRemark?.visibility = View.VISIBLE
            etRemark?.visibility = View.GONE
        } else {
            tvRemark?.visibility = View.GONE
            etRemark?.visibility = View.VISIBLE
            etRemark?.setText(formInfoDetail.form.fillinRemarks)
            etRemark?.addTextChangedListener(object : TextWatcherImpl() {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    formInfoDetail.form.fillinRemarks = s.toString()
                }
            })
        }
    }

    private fun initType1View(helper: BaseViewHolder?, item: FormItem) {
        helper?.setIsRecyclable(false)
        helper?.setText(R.id.itemUpKeepParam_order, (data.indexOf(item) + 1).toString())
                ?.setText(R.id.itemUpKeepParam_code, item.basicActionName)
                ?.setText(R.id.itemUpKeepParam_deviceName, item.equipmentCode)
                ?.setText(R.id.itemUpKeepParam_unit, item.unit)

        val tvValue = helper?.getView<TextView>(R.id.itemUpKeepParam_tvValue)
        val tvRemark = helper?.getView<TextView>(R.id.itemUpKeepParam_tvRemark)
        val rlInput = helper?.getView<RelativeLayout>(R.id.itemUpKeepParam_rlInput)
        val etInput = helper?.getView<EditText>(R.id.itemUpKeepParam_etInput)
        val tvInput = helper?.getView<TextView>(R.id.itemUpKeepParam_tvInput)
        val inputNotice = helper?.getView<TextView>(R.id.itemUpKeepParam_inputNotice)
        val inputNoticeImg = helper?.getView<ImageView>(R.id.itemUpKeepParam_inputNoticeImg)
        val rlChoose = helper?.getView<RelativeLayout>(R.id.itemUpKeepParam_choose)
        val spinner = helper?.getView<Spinner>(R.id.itemUpKeepParam_spinner)
        val etRemark = helper?.getView<EditText>(R.id.itemUpKeepParam_remark)
        val rvPictures = helper?.getView<RecyclerView>(R.id.itemSpotCheckParam_pictures)
//        val addView = helper?.getView<AddView>(R.id.itemUpKeepParam_addView)
        val addView = helper?.getView<ImageView>(R.id.itemUpKeepParam_addView)

        setImageAdapter(item, rvPictures)

        val currentFocus = context.currentFocus
        currentFocus?.clearFocus()

        watcher = object : TextWatcherImpl() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if ((!TextUtils.isEmpty(item.result) && item.result != s.toString())
                        || (TextUtils.isEmpty(item.result) && !TextUtils.isEmpty(s))) {
                    when (status) {
                        Constant.FORM_STATUS_INPROGRESS ->
                            //强制拍照检测
                            if (item.photoMust == FormItem.TYPE_Y) {
                                if (item.photoPaths == null || item.photoPaths.isEmpty()) {
                                    listener?.takePicture(data.indexOf(item))
                                    UIUtils.closeKeyboard(context)
                                    val currentFocus = context.currentFocus
                                    currentFocus?.clearFocus()
                                }
                            }
                    }
                }

                if ("T" == item.valueType) {
                    item.result = s.toString()
                } else {
                    checkFloatValue(s, item, inputNotice, inputNoticeImg, etInput!!, tvInput!!)
                }
            }
        }


        when (status) {
            Constant.FORM_STATUS_COMPLETED,
            Constant.FORM_STATUS_CLOSED,
            Constant.FORM_STATUS_PLANNED -> {
                when (item.valueType) {
                    "N" -> {
                        rlInput?.visibility = View.VISIBLE
                        etInput?.visibility = View.GONE
                        tvInput?.visibility = View.VISIBLE
                        tvValue?.visibility = View.GONE
                    }
                    else -> {
                        rlInput?.visibility = View.GONE
                        tvValue?.visibility = View.VISIBLE
                    }
                }

                etRemark?.visibility = View.GONE
                rlChoose?.visibility = View.GONE
                tvRemark?.visibility = View.VISIBLE

                if ("B" == item.valueType) {
                    val list = item.optionValues.split("|")
                    val valueList = item.optionValuesDescription.split("|")
                    val indexOf = if (TextUtils.isEmpty(item.result)) {
                        list.indexOf(item.presetValue)
                    } else {
//                        list.indexOf(item.result)
                        valueList.indexOf(item.result)
                    }

                    if (indexOf in valueList.indices) {
                        tvValue?.text = valueList[indexOf]
                    } else {
                        tvValue?.text = ""
                    }
                } else if ("N" == item.valueType) {
                    tvInput?.text = item.result ?: ""
                } else {
                    tvValue?.text = if (TextUtils.isEmpty(item.result)) item.presetValue
                            ?: "" else item.result
                }
                tvRemark?.text = item.remarks

                rlChoose?.setOnClickListener(null)
                etRemark?.setOnClickListener(null)
                addView?.setOnClickListener(null)
            }
            Constant.FORM_STATUS_INPROGRESS -> {
                addView?.setOnClickListener {
                    listener?.onAddClick(data.indexOf(item))
                }

                when (item.valueType) {
                    "N", "T" -> {
                        rlInput?.visibility = View.VISIBLE
                        etInput?.visibility = View.VISIBLE
                        tvInput?.visibility = View.GONE
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
        etRemark?.addTextChangedListener(object : TextWatcherImpl() {

            override fun afterTextChanged(s: Editable?) {
                item.remarks = s.toString()
            }
        })

        when (item.valueType) {
            "N", "T" -> {
                helper?.setText(R.id.itemUpKeepParam_lowerLimit, item.lowerLimit)
                        ?.setText(R.id.itemUpKeepParam_upperLimit, item.upperLimit)

                etInput?.removeTextChangedListener(watcher)
                when (item.valueType) {
                    "N" -> {
                        etInput?.setText(item.result ?: "")
                        checkFloatValue(item.result, item, inputNotice, inputNoticeImg, etInput!!, tvInput!!)
                    }
                    "T" -> {
                        inputNotice?.visibility = View.GONE
                        inputNoticeImg?.visibility = View.GONE
                        etInput?.setText(if (TextUtils.isEmpty(item.result)) item.presetValue
                                ?: "" else item.result)
                    }
                }
                etInput?.addTextChangedListener(watcher)
            }
            "O" -> {
                rlChoose?.setBackgroundResource(R.drawable.bg_spinner)

                val list = item.optionValues.split("|")
                val spinnerAdapter = CommonSpinnerAdapter(mContext)
                spinner?.adapter = spinnerAdapter
                spinnerAdapter.notifyDataSetChanged(list)

                val indexOf = if (TextUtils.isEmpty(item.result)) {
                    if (!TextUtils.isEmpty(item.presetValue)) {
                        list.indexOf(item.presetValue)
                    } else -1
                } else list.indexOf(item.result)

                if (indexOf in list.indices)
                    spinner?.setSelection(indexOf)
                else {
                    if (list.isNotEmpty())
                        spinner?.setSelection(0)
                }

                spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        var change = true
                        when (status) {
                            Constant.FORM_STATUS_INPROGRESS ->
                                if (!TextUtils.isEmpty(item.result) && !TextUtils.equals(item.result, list[position])) {
                                    //强制拍照检测
                                    if (item.photoMust == FormItem.TYPE_Y) {
                                        if (item.photoPaths == null || item.photoPaths.isEmpty()) {
                                            listener?.takePicture(data.indexOf(item))
                                            change = false
                                        }
                                    }
                                }
                        }
                        if (change) item.result = list[position]
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

                val indexOf = if (TextUtils.isEmpty(item.result)) {
                    if (!TextUtils.isEmpty(item.presetValue)) {
                        valueList.indexOf(item.presetValue)
                    } else -1
                } else {
//                    valueList.indexOf(item.result)
                    list.indexOf(item.result)
                }

                if (indexOf in list.indices) {
                    spinner?.setSelection(indexOf)
                    checkBoolType(valueList, indexOf, rlChoose)
                } else {
                    if (list.isNotEmpty()) {
                        spinner?.setSelection(0)
                        checkBoolType(valueList, 0, rlChoose)
                    }
                }

                spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        var change = true
                        when (status) {
                            Constant.FORM_STATUS_INPROGRESS ->
                                if (!TextUtils.isEmpty(item.result) && !TextUtils.equals(item.result, valueList[position])) {
                                    //强制拍照检测
                                    if (item.photoMust == FormItem.TYPE_Y) {
                                        if (item.photoPaths == null || item.photoPaths.isEmpty()) {
                                            listener?.takePicture(data.indexOf(item))
                                            change = false
                                        } else {
                                            listener?.dismissTakePicture(data.indexOf(item))
                                        }
                                    }
                                }
                        }
                        if (change) {
//                            item.result = valueList[position]
                            item.result = list[position]
                            val chooseItemIndex = valueList.indexOf(item.result)
                            if (chooseItemIndex != -1) {
                                checkBoolType(valueList, chooseItemIndex, rlChoose)
                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {

                    }
                }
            }
        }
    }

    private fun setImageAdapter(item: FormItem, rvPictures: RecyclerView?) {
        if (item.photoPaths != null && item.photoPaths.isNotEmpty()) {
            rvPictures?.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
            val imageAdapter = ImageAdapter(context, item.photoPaths, status)
            imageAdapter.setItemClickListener(OnItemClickListener { adapter, view, position ->
                val typeAndPath = item.photoPaths[position]
                if (!TextUtils.isEmpty(typeAndPath)) {
                    val splits = typeAndPath.split(";")
                    if (splits.size > 1) {
                        var path = splits[1]
                        val type = splits[0]
                        when (type) {
                            Constant.TAKE_PHOTO -> IntentUtils.goImageViewer(mContext, path)
                            Constant.TAKE_VIDEO -> IntentUtils.startPlay(mContext, path)
                        }
                    }
                }

            })
            rvPictures?.adapter = imageAdapter
            rvPictures?.visibility = View.VISIBLE
        } else {
            rvPictures?.adapter = ImageAdapter(context, ArrayList(), status)
            rvPictures?.visibility = View.GONE
        }
    }

    private fun checkBoolType(list: List<String>, indexOf: Int, rlChoose: RelativeLayout?) {
        if (indexOf != -1) {
            if ("N".equals(list[indexOf], true)) {
                rlChoose?.setBackgroundResource(R.drawable.radius_solid_red_5)
            } else {
                rlChoose?.setBackgroundResource(R.drawable.bg_spinner)
            }
        } else {
            rlChoose?.setBackgroundResource(R.drawable.bg_spinner)
        }
    }

    private fun checkFloatValue(s: CharSequence?, item: FormItem, inputNotice: TextView?, inputNoticeImg: ImageView?, etInput: EditText, tvInput: TextView) {
        try {
            if (!TextUtils.isEmpty(s)) {
                val intValue = s.toString().toFloat()
                var lowerLimit = Float.MIN_VALUE
                var upperLimit = Float.MAX_VALUE
                if (!TextUtils.isEmpty(item.lowerLimit)) {
                    lowerLimit = item.lowerLimit.toFloat()
                }

                if (!TextUtils.isEmpty(item.upperLimit)) {
                    upperLimit = item.upperLimit.toFloat()
                }
                when {
                    intValue < lowerLimit -> {
                        inputNotice?.visibility = View.VISIBLE
                        inputNoticeImg?.visibility = View.VISIBLE
                        inputNotice?.text = mContext.resources.getString(R.string.too_lower)
                        etInput.setBackgroundResource(R.drawable.radius_red_5)
                        tvInput.setBackgroundResource(R.drawable.radius_red_5)
                        item.result = s.toString()
                    }
                    intValue > upperLimit -> {
                        inputNotice?.visibility = View.VISIBLE
                        inputNoticeImg?.visibility = View.VISIBLE
                        inputNotice?.text = mContext.resources.getString(R.string.too_upper)
                        etInput.setBackgroundResource(R.drawable.radius_red_5)
                        tvInput.setBackgroundResource(R.drawable.radius_red_5)
                        item.result = s.toString()
                    }
                    else -> {
                        inputNotice?.visibility = View.GONE
                        inputNoticeImg?.visibility = View.INVISIBLE
                        etInput.setBackgroundResource(R.drawable.radius_blue_5)
                        tvInput.setBackgroundResource(R.drawable.radius_blue_5)
                        item.result = s.toString()
                    }
                }
            } else {
                inputNotice?.visibility = View.GONE
                inputNoticeImg?.visibility = View.INVISIBLE
                etInput.setBackgroundResource(R.drawable.radius_blue_5)
                tvInput.setBackgroundResource(R.drawable.radius_blue_5)
                item.result = s.toString()
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            item.result = s.toString()
        } catch (e: NullPointerException) {
            item.result = ""
            e.printStackTrace()
        }
    }

    fun updateData(status: String) {
        this.status = status
        notifyDataSetChanged()
    }

    interface OnInnerItemClickListener {
        fun takePicture(position: Int)

        fun dismissTakePicture(position: Int)

        fun onAddClick(position: Int)
    }

    private var listener: OnInnerItemClickListener? = null
    fun setOnInnerItemClickListener(listener: OnInnerItemClickListener) {
        this.listener = listener
    }
}
