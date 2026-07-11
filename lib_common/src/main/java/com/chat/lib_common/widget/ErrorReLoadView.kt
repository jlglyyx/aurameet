package com.chat.lib_common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.viewbinding.ViewBinding
import com.chat.lib_common.databinding.ViewErrorReLoadBinding
import kotlin.collections.isNullOrEmpty
import kotlin.sequences.findLast
import kotlin.sequences.forEachIndexed

class ErrorReLoadView : ConstraintLayout {


    enum class Status {

        LOADING,

        ERROR,

        NORMAL,

        EMPTY,

        NO_NETWORK
    }


    var onClick = {}

    val mBinding by lazy {
        ViewErrorReLoadBinding.inflate(LayoutInflater.from(context), this, false)
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mBinding.clContainer.setOnClickListener {

            onClick()
        }
    }


    override fun onFinishInflate() {
        super.onFinishInflate()
        addView(mBinding.root)
        showStatusView(Status.LOADING)
    }


    fun showStatusView(show: Status) {

        showView(show)

    }

    fun <T> showSuccessView(data: List<T>?) {

        if (data.isNullOrEmpty()) {
            showStatusView(Status.EMPTY)
        } else {
            showStatusView(Status.NORMAL)
        }

    }





    private fun showView(status:Status) {


        if (status == Status.NORMAL){

            showNormalView()

            return
        }


        val mLoadView = mBinding.root.children.findLast { it.tag == status }

        if (null == mLoadView){

            showNormalView()

            return
        }

        this.children.forEachIndexed { index, view ->

            view.visibility = GONE

        }

        mBinding.root.visibility = VISIBLE

        mBinding.root.children.forEachIndexed { index, view ->

            if (view.tag == status) {
                view.visibility = VISIBLE
            } else {
                view.visibility = GONE
            }

        }

    }


    fun showNormalView() {

        this.children.forEachIndexed { index, view ->

            view.visibility = VISIBLE
        }

        mBinding.root.visibility = GONE

    }





    inline fun <reified T : ViewBinding> addEmptyView(crossinline bind: (ViewGroup) -> T) =
        bind(mBinding.root).apply {

            root.tag = Status.EMPTY
        }

    inline fun <reified T : ViewBinding> addErrorView(crossinline bind: (ViewGroup) -> T) =
        bind(mBinding.root).apply {

            root.tag = Status.ERROR
        }

    inline fun <reified T : ViewBinding> addLoadView(crossinline bind: (ViewGroup) -> T) =
        bind(mBinding.root).apply {

            root.tag = Status.LOADING
        }
    inline fun <reified T : ViewBinding> addNoNetView(crossinline bind: (ViewGroup) -> T) =
        bind(mBinding.root).apply {

            root.tag = Status.NO_NETWORK
        }
}