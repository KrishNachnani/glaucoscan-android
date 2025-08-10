package com.glaucoma.ai.ui

import android.content.Intent
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.glaucoma.ai.R
import com.glaucoma.ai.base.BaseActivity
import com.glaucoma.ai.base.BaseViewModel
import com.glaucoma.ai.base.utils.BindingUtils
import com.glaucoma.ai.databinding.ActivityDisclaimerBinding
import com.glaucoma.ai.ui.splash.AuthCommonVM
import com.glaucoma.ai.ui.splash.MySplashActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DisclaimerActivity : BaseActivity<ActivityDisclaimerBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    override fun getLayoutResource(): Int {
        return R.layout.activity_disclaimer
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        initView()
        initOnClick()
    }

    private fun initOnClick() {
        viewModel.onClick.observe(this, Observer {
            when (it?.id) {
                R.id.btnAgree -> {
                    val intent = Intent(this, MySplashActivity::class.java)
                    startActivity(intent)
                }
            }
        })

    }

    private fun initView() {
        BindingUtils.statusBarStyleBlack(this)
        BindingUtils.styleSystemBars(this, getColor(R.color.black))
    }

}