package com.zetavision.panda.ums.adapter

import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.utils.Constant
import com.zetavision.panda.ums.widget.GlideRoundTransform
import java.io.File

/**
 * Created by wheroj on 2018/2/6 12:01.
 * @describe
 */
class ImageAdapter(data: List<String>, val status: String): BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_img, data) {

    private var listener: OnItemClickListener? = null

    override fun convert(helper: BaseViewHolder?, item: String?) {
        if (helper != null && item != null) {
            val ivImg = helper.getView<ImageView>(R.id.spotcheck_ivImg)
            val ivImgDel = helper.getView<ImageView>(R.id.spotcheck_ivImgDel)

            val splits = item.split(";")
            if (splits.size > 1) {
                var type = splits[0]
                var path = splits[1]
                val manager = Glide.with(mContext)
                if (!TextUtils.isEmpty(path)) {
                    var file = File(path)
                    if (Constant.TAKE_PHOTO == type) {
                        manager.load(Uri.fromFile( file)).transform(GlideRoundTransform(mContext, Constant.REC_RADIUS_5))
                                .placeholder(R.mipmap.icon_default_order_big).error(R.mipmap.icon_default_order_big).into(ivImg)
                    } else {
                        manager.load(file).transform(GlideRoundTransform(mContext, Constant.REC_RADIUS_5))
                                .placeholder(R.mipmap.icon_default_order_big).error(R.mipmap.icon_default_order_big).into(ivImg)
                    }
                } else {
                    manager.load(R.mipmap.icon_default_order_big).transform(GlideRoundTransform(mContext, Constant.REC_RADIUS_5)).into(ivImg)
                }
            }

            when(status) {
                Constant.FORM_STATUS_PLANNED,
                        Constant.FORM_STATUS_INPROGRESS -> {
                    ivImgDel.visibility = View.VISIBLE
                    ivImgDel.setOnClickListener {
                        data.remove(item)
                        notifyDataSetChanged()
                    }
                }
            }

            helper.itemView.setOnClickListener {
                listener?.onItemClick(this, helper.itemView, data.indexOf(item))
            }
        }
    }

    fun setItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
}