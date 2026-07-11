package com.chat.lib_common.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.observe
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.viewmodel.BaseViewModel
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.LoadingPopupView
import java.lang.reflect.ParameterizedType
import kotlin.jvm.javaClass
import kotlin.run

abstract class BaseActivity<VB: ViewBinding,VM: BaseViewModel>(
    private val bind: (LayoutInflater) -> VB
) : AppCompatActivity() {

    private var _mViewBinding: VB? = null

    val mViewBinding :VB get() = _mViewBinding!!

    lateinit var mViewModel:VM

    val TAG = this.javaClass.simpleName

    private var loadingPopupView: LoadingPopupView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }

        _mViewBinding = bind(layoutInflater)
        val type = javaClass.genericSuperclass as ParameterizedType
        val modelClass = type.actualTypeArguments[1] as Class<VM>
        mViewModel = getViewModel(modelClass)
        setContentView(mViewBinding.root)
        initView()
        initData()
        initViewModel()

        FlowBus.with(AppConstant.EventConstant.EVENT_LOGIN_OUT).observe(this){

        }
    }



    abstract fun initView()

    abstract fun initData()

    abstract fun initViewModel()



    inline fun <T> withViewBinding(action: VB.() -> T): T {
        return mViewBinding.run(action)
    }


    private fun <T : BaseViewModel> getViewModel(
        factory: ViewModelProvider.Factory,
        clazz: Class<T>
    ): T {

        return ViewModelProvider(this, factory)[clazz]
    }

    private fun <T : BaseViewModel> getViewModel(
        clazz: Class<T>
    ): T {

        return ViewModelProvider(this)[clazz]
    }


    fun showLoading(title: String = "loading...", dismissOnTouchOutside: Boolean = false) {
        if (loadingPopupView == null) {
            loadingPopupView = XPopup.Builder(this)
                .dismissOnTouchOutside(dismissOnTouchOutside)
                .isViewMode(true)
                .hasStatusBarShadow(false)
                .hasShadowBg(false)
                .asLoading(title)
        } else {
            loadingPopupView?.setTitle(title)
        }
        if (!loadingPopupView?.isShow!!) {
            loadingPopupView?.show()
        }
    }

    fun dismissLoading() {
        loadingPopupView?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingPopupView?.dismiss()
        loadingPopupView = null
        _mViewBinding = null
    }

}