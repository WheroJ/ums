package com.zetavision.panda.ums.ui.upkeep

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.adapter.UpKeepAdapter
import com.zetavision.panda.ums.base.BaseActivity
import com.zetavision.panda.ums.model.FormInfo
import com.zetavision.panda.ums.model.FormInfoDetail
import org.litepal.crud.DataSupport

/**
 * Created by wheroj on 2018/1/30.
 * @describeb
 */
class UpKeepActivity: BaseActivity() {

    override fun getContentLayoutId(): Int {
        return R.layout.activity_upkeep
    }

    override fun init() {

        header.setLeftImage(R.mipmap.back)

        val deviceName = intent.extras.getString("deviceName")
        header.setTitle("设备".plus(deviceName).plus("保养表单"))

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val formListDetails = DataSupport.where("(equipmentcode = '$deviceName')").find(FormInfoDetail::class.java, true)
        var formList = ArrayList<FormInfo>()
        formListDetails?.indices?.mapTo(destination = formList) { formListDetails[it].form }

        val upKeepAdapter = UpKeepAdapter(formList)
        recyclerView?.adapter = upKeepAdapter
    }

    override fun onLeftClick() {
        finish()
    }

    override fun getHasTitle(): Boolean {
        return true
    }
}