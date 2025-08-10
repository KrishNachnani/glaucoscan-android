package com.glaucoma.ai.ui.splash

import com.glaucoma.ai.base.BaseViewModel
import com.glaucoma.ai.data.api.ApiHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthCommonVM @Inject constructor(
    private val apiHelper: ApiHelper,
) : BaseViewModel() {

}

