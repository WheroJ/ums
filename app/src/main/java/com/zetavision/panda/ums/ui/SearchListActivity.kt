package com.zetavision.panda.ums.ui

import com.zetavision.panda.ums.R
import com.zetavision.panda.ums.base.BaseActivity
import com.zetavision.panda.ums.model.FormInfo
import com.zetavision.panda.ums.ui.spotcheck.SpotCheckListFragment
import com.zetavision.panda.ums.ui.upkeep.UpKeepListFragment

/**
 * Created by wheroj on 2018/1/30.
 * @describeb
 */
class SearchListActivity : BaseActivity() {

    private var deviceName: String? = null
    private var actionType: String? = null

    override fun getContentLayoutId(): Int {
        return R.layout.framelayout
    }

    override fun init() {
        header.setLeftImage(R.mipmap.back)

        deviceName = intent.extras.getString("deviceName")
        actionType = intent.extras.getString("actionType")
        if (FormInfo.ACTION_TYPE_M == actionType) {
            val fragment = UpKeepListFragment()
            fragment.arguments = intent.extras
            replaceShow(fragment, R.id.frameLayout)
            header.setTitle(getString(R.string.common_device).plus(deviceName).plus(getString(R.string.maint_form)))
        } else {
            val fragment = SpotCheckListFragment()
            fragment.arguments = intent.extras
            replaceShow(fragment, R.id.frameLayout)
            header.setTitle(getString(R.string.common_device).plus(deviceName).plus(getString(R.string.spotcheck_form)))
        }
    }

    override fun onLeftClick() {
        finish()
    }

    override fun getHasTitle(): Boolean {
        return true
    }
}