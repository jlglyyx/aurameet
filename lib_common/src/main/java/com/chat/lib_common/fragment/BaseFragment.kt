package com.chat.lib_common.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.chat.lib_common.viewmodel.BaseViewModel
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.LoadingPopupView
import java.lang.reflect.ParameterizedType
import kotlin.jvm.javaClass
import kotlin.run

abstract class BaseFragment<VB : ViewBinding, VM : BaseViewModel>(
    private val bind: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment() {

    val TAG = this.javaClass.simpleName

    private var _mViewBinding: VB? = null

    val mViewBinding: VB get() = _mViewBinding!!

    lateinit var mViewModel: VM

    private var isFirstLoad = true

    private var loadingPopupView: LoadingPopupView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        _mViewBinding = bind(layoutInflater,container,false)

        val type = javaClass.genericSuperclass as ParameterizedType
        val modelClass = type.actualTypeArguments[1] as Class<VM>
        mViewModel = getViewModel(modelClass)

        viewLifecycleOwnerLiveData.observe(this.viewLifecycleOwner) { owner ->
            owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    _mViewBinding = null
                }
            })
        }

        return mViewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
    }


    override fun onResume() {
        super.onResume()
        if (isFirstLoad && lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            isFirstLoad = false
            initData()
        }
    }




    abstract fun initView()

    abstract fun initData()

    abstract fun initViewModel()



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

    inline fun <T> withViewBinding(action: VB.() -> T): T {
        return mViewBinding.run(action)
    }

    fun showLoading(title: String = "loading...", dismissOnTouchOutside: Boolean = false) {
        if (loadingPopupView == null) {
            loadingPopupView = XPopup.Builder(requireContext())
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

    override fun onDestroyView() {
        super.onDestroyView()
        loadingPopupView?.dismiss()
        loadingPopupView = null
        _mViewBinding = null
    }

}