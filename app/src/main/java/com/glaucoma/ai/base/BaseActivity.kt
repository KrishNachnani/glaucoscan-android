package com.glaucoma.ai.base

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.glaucoma.ai.App
import com.glaucoma.ai.BR
import com.glaucoma.ai.R
import com.glaucoma.ai.base.local.SharedPrefManager
import com.glaucoma.ai.base.connectivity.ConnectivityProvider
import com.glaucoma.ai.base.network.ErrorCodes
import com.glaucoma.ai.base.network.NetworkError
import com.glaucoma.ai.base.utils.AlertManager
import com.glaucoma.ai.base.utils.event.NoInternetSheet
import com.glaucoma.ai.base.utils.hideKeyboard
import javax.inject.Inject

abstract class BaseActivity<Binding : ViewDataBinding> : AppCompatActivity(){
    lateinit var progressDialogAvl: ProgressDialogAvl
    open val onRetry: (() -> Unit)? = null
    lateinit var binding: Binding
    val app: App
        get() = application as App

  //  private lateinit var connectivityProvider: ConnectivityProvider
   // private var noInternetSheet: NoInternetSheet? = null

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout: Int = getLayoutResource()
        binding = DataBindingUtil.setContentView(this, layout)
        binding.setVariable(BR.vm, getViewModel())
      //  connectivityProvider = ConnectivityProvider.createProvider(this)
      //  connectivityProvider.addListener(this)
        progressDialogAvl = ProgressDialogAvl(this)
        setStatusBarColor(R.color.white)
        setStatusBarDarkText()
        onCreateView()

        val vm = getViewModel()
        binding.setVariable(BR.vm, vm)
        vm.onUnAuth.observe(this) {
            showUnauthorised()
        }
    }

    fun showUnauthorised() {
        sharedPrefManager.clear()
       // startActivity(LoginActivity.newIntent(this))
       // finishAffinity()
    }

    private fun setStatusBarColor(colorResId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, colorResId)
        }
    }

    private fun setStatusBarDarkText() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    protected abstract fun getLayoutResource(): Int
    protected abstract fun getViewModel(): BaseViewModel
    protected abstract fun onCreateView()

    fun showToast(msg: String? = "Something went wrong !!") {
        Toast.makeText(this, msg ?: "Showed null value !!", Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }

    fun showLoading(){
        progressDialogAvl.isLoading(true)

    }

    fun hideLoading() {
        progressDialogAvl.isLoading(false)
    }
    fun onError(error: Throwable, showErrorView: Boolean) {
        if (error is NetworkError) {

            when (error.errorCode) {
                ErrorCodes.SESSION_EXPIRED -> {
                    showToast(getString(R.string.session_expired))
                    app.onLogout()
                }

                else -> AlertManager.showNegativeAlert(
                    this,
                    error.message,
                    getString(R.string.alert)
                )
            }
        } else {
            AlertManager.showNegativeAlert(
                this,
                getString(R.string.please_try_again),
                getString(R.string.alert)
            )
        }
    }


}