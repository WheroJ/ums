package com.zetavision.panda.ums.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.fragments.base.BaseFragment
import com.zetavision.panda.ums.model.FormInfo
import com.zetavision.panda.ums.ui.spotcheck.SpotCheckListFragment
import com.zetavision.panda.ums.ui.upkeep.UpKeepListFragment
import com.zetavision.panda.ums.utils.Constant

/**
 * Created by wheroj on 2018/3/29 18:00.
 * @describe
 */
class FormListFragment : BaseFragment() {

    private var actionType: String? = null
    private var status: String? = Constant.FORM_STATUS_ALL

    override fun getContentLayoutId(): Int {
        return R.layout.framelayout
    }

    override fun init() {
        if (arguments != null) {
            actionType = arguments.getString("actionType")
            status = if (arguments.getString("status") == null) status
            else arguments.getString("status")
        }

        val data = ArrayList<String>()
        data.add(getString(R.string.maint_form))
        data.add(getString(R.string.spotcheck_form))
        header.setSpinner(data, object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position) {
                    0 -> changeFragment(FormInfo.ACTION_TYPE_M)
                    1 -> changeFragment(FormInfo.ACTION_TYPE_P)
                }
            }
        })
        if (FormInfo.ACTION_TYPE_M == actionType) {
            header.getSpinner().setSelection(0)
        } else {
            header.getSpinner().setSelection(1)
        }
//        changeFragment(actionType)
    }

    private fun changeFragment(actionType: String?) {
        var fragment: BaseFragment? = null
        when (actionType) {
            FormInfo.ACTION_TYPE_M -> {
                fragment = UpKeepListFragment()
            }
            FormInfo.ACTION_TYPE_P -> {
                fragment = SpotCheckListFragment()
            }
        }
        val bundle = Bundle()
        bundle.putString("actionType", actionType)
        bundle.putString("status", status)
        fragment!!.arguments = bundle
        replaceShow(fragment)
    }

    private fun replaceShow(fragment: BaseFragment) {
        childFragmentManager.beginTransaction().replace(R.id.frameLayout, fragment)
                .commitAllowingStateLoss()
    }
}