package com.glaucoma.ai.ui.splash

import android.content.Intent
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.glaucoma.ai.R
import com.glaucoma.ai.base.BaseActivity
import com.glaucoma.ai.base.BaseViewModel
import com.glaucoma.ai.base.utils.BindingUtils
import com.glaucoma.ai.data.api.Constants
import com.glaucoma.ai.data.api.Constants.finalBitmap
import com.glaucoma.ai.data.api.Constants.isClicked

import com.glaucoma.ai.data.api.Constants.selectedImageShow
import com.glaucoma.ai.databinding.ActivityMySplashBinding
import com.glaucoma.ai.ui.information.InformationActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MySplashActivity : BaseActivity<ActivityMySplashBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    override fun getLayoutResource(): Int {
        return R.layout.activity_my_splash
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
                    val intent = Intent(this, InformationActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        })


    }

    private fun initView() {
        BindingUtils.statusBarStyleBlack(this)
        BindingUtils.styleSystemBars(this, getColor(R.color.black))
    }

    override fun onResume() {
        super.onResume()
        selectedImageShow = ""
        isClicked = ""
        finalBitmap = null

        InformationActivity.rightEyePosition = null//0 camera 1 for gallery
        InformationActivity.leftEyePosition = null //0 camera 1 for gallery
        InformationActivity.resultScanDataRight = null
        InformationActivity.resultScanDataLeft = null
        Constants.age = ""
        Constants.gender = ""
        Constants.ethnicity = ""
        Constants.eysPos = "l"
        Constants.selectedImageShow = ""

        Constants.isClicked = ""
        Constants.finalBitmap = null

        Constants.leftEyeCam = null
        Constants.rightEyeCam = null

//
//        leftEyeCamBitmap = null
//        rightEyeCamBitmap = null
//
//        leftEyeGalleyBitmap = null
//        rightEyeGalleryBitmap = null
    }

}