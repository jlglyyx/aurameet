package com.chat.lib_common.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.chat.lib_common.R
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.LoadingPopupView
import kotlin.apply
import kotlin.jvm.javaClass
import kotlin.run


abstract class BaseDialog<VB : ViewBinding>(
    private val bind: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : DialogFragment() {

    val TAG = this.javaClass.simpleName

    private var _mViewBinding: VB? = null

    val mDialogBinding get() = _mViewBinding!!

    private var loadingPopupView: LoadingPopupView? = null

    var initView :(dialog: BaseDialog<VB>, mViewBinding:VB) ->Unit = { d, v ->

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        setStyle(STYLE_NORMAL,R.style.Theme_Dialog)

        return super.onCreateDialog(savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initViewModel()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        _mViewBinding = bind(layoutInflater,container,false)

        initView()

        initView(this,mDialogBinding)

        viewLifecycleOwnerLiveData.observe(this.viewLifecycleOwner) { owner ->
            owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    _mViewBinding = null
                }
            })
        }

        return mDialogBinding.root
    }

    abstract fun initView()

    abstract fun initData()

    open fun initViewModel(){}



    override fun onStart() {
        super.onStart()
        val window = dialog!!.window
        window?.apply {

            WindowCompat.setDecorFitsSystemWindows(window, false)

            WindowInsetsControllerCompat(window, window.decorView).apply {
                isAppearanceLightStatusBars = false
            }
            val windowParams: WindowManager.LayoutParams = window.attributes
            setCanceledOnTouchOutside(true)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(setDialogWidth(), setDialogHeight())
            windowParams.gravity = setDialogGravity()
            window.attributes = windowParams
        }
    }

    inline fun <T> withViewBinding(action: VB.() -> T): T {
        return mDialogBinding.run(action)
    }

    inline fun <reified T : ViewModel> DialogFragment.sharedViewModels() =
        lazy {
            when {
                parentFragment != null -> ViewModelProvider(requireParentFragment())[T::class.java]
                else -> ViewModelProvider(requireActivity())[T::class.java]
            }
        }

    fun show(fragmentManager: FragmentManager){

        try {
            if (!isAdded && !fragmentManager.isStateSaved) {
                showNow(fragmentManager, TAG)
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            Log.w(TAG, "Fragment transaction error: ${e.message}")
        }
    }

    fun setCanceledOnTouchOutside(cancel:Boolean = true){
        dialog?.setCanceledOnTouchOutside(cancel)
    }

    fun getColor(color:Int):Int{

        return requireContext().getColor(color)
    }



    open fun setDialogGravity():Int{

        return Gravity.CENTER
    }

    open fun setDialogWidth():Int{

        return WindowManager.LayoutParams.MATCH_PARENT
    }

    open fun setDialogHeight():Int{

        return WindowManager.LayoutParams.WRAP_CONTENT
    }


    fun showLoading(title: String = "loading...", dismissOnTouchOutside: Boolean = false,dismissOnBackPressed: Boolean = true) {
        if (loadingPopupView == null) {
            loadingPopupView = XPopup.Builder(requireContext())
                .dismissOnTouchOutside(dismissOnTouchOutside)
                .dismissOnBackPressed(dismissOnBackPressed)
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


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        loadingPopupView?.dismiss()
        loadingPopupView = null
        _mViewBinding = null
    }
}