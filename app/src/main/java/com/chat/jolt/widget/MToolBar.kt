package com.chat.jolt.widget

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.blankj.utilcode.util.ColorUtils
import com.chat.jolt.R
import com.chat.jolt.databinding.ViewToolbarBinding
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeTop

class MToolBar : LinearLayout {

    var mToolbarBinding: ViewToolbarBinding

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

        mToolbarBinding = ViewToolbarBinding.inflate(LayoutInflater.from(context), this, true)

        mToolbarBinding.root.edgeToEdgeTop()

        val obtainStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.AppToolBar)
        val mAppToolBarTitle = obtainStyledAttributes.getString(R.styleable.AppToolBar_title)

        val mAppToolBarTitleColor = obtainStyledAttributes.getResourceId(R.styleable.AppToolBar_titleColor,0)

        val mAppToolBarBackImgVisible =
            obtainStyledAttributes.getBoolean(R.styleable.AppToolBar_backImgVisible, true)

        val mAppToolBarEndImgVisible =
            obtainStyledAttributes.getBoolean(R.styleable.AppToolBar_endImgVisible, false)

        val mAppToolBarBackImgSrc =
            obtainStyledAttributes.getResourceId(R.styleable.AppToolBar_backImgSrc, 0)

        val mAppToolBarEndImgSrc =
            obtainStyledAttributes.getResourceId(R.styleable.AppToolBar_endImgSrc, 0)

        val toolbarColor =
            obtainStyledAttributes.getResourceId(R.styleable.AppToolBar_toolbarColor, 0)


        obtainStyledAttributes.recycle()



        mToolbarBinding.tvTitle.text = mAppToolBarTitle

        mToolbarBinding.ivBack.visibility = if (mAppToolBarBackImgVisible) VISIBLE else GONE

        mToolbarBinding.ivRight.visibility = if (mAppToolBarEndImgVisible) VISIBLE else GONE

        if (mAppToolBarBackImgSrc != 0) {
            mToolbarBinding.ivBack.setImageResource(mAppToolBarBackImgSrc)
        }
        if (mAppToolBarEndImgSrc != 0) {
            mToolbarBinding.ivRight.setImageResource(mAppToolBarEndImgSrc)
        }

        if (toolbarColor != 0) {
            mToolbarBinding.llToolbar.setBackgroundResource(toolbarColor)
        }
        if (mAppToolBarTitleColor != 0) {
            mToolbarBinding.tvTitle.setTextColor(ColorUtils.getColor(mAppToolBarTitleColor))
        }

        mToolbarBinding.ivBack.click {
            if (context is Activity) {
                context.finish()
            }
        }

    }


}