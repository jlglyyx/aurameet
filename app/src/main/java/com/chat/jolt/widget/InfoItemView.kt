package com.chat.jolt.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.chat.jolt.R
import com.chat.jolt.databinding.ViewInfoItemBinding
import com.chat.lib_common.util.loadImage


class InfoItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {


    private val mViewInfoItemBinding by lazy {

        ViewInfoItemBinding.inflate(LayoutInflater.from(context), this, true)
    }


    init {

        context.withStyledAttributes(attrs, R.styleable.InfoItemView) {

            val title = getString(R.styleable.InfoItemView_itvTitle)
            val content = getString(R.styleable.InfoItemView_itvContent)

            val mItvImgVisible = getBoolean(R.styleable.InfoItemView_itvImgVisible, false)

            val mItvIntoImgVisible = getBoolean(R.styleable.InfoItemView_itvIntoImgVisible, true)

            mViewInfoItemBinding.apply {

                tvTitle.text = title
                tvContent.text = content

                ivImage.visibility = if (mItvImgVisible) VISIBLE else GONE
                ivInto.visibility = if (mItvIntoImgVisible) VISIBLE else INVISIBLE

            }
        }





    }


    fun setTitle(content:String?){

        mViewInfoItemBinding.tvTitle.text = content
    }
    fun setContent(content:String?){

        mViewInfoItemBinding.tvContent.text = content
    }

    fun setImage(content:Any?){

        mViewInfoItemBinding.ivImage.loadImage(context,content,40,40)
    }


    fun getTitle():String{

       return mViewInfoItemBinding.tvTitle.text.toString()
    }
    fun getContent():String{

        return mViewInfoItemBinding.tvContent.text.toString()
    }


    fun setRadius(
        topLeftRadius: Float,
        topRightRadius: Float,
        bottomLeftRadius: Float,
        bottomRightRadius: Float
    ) {

        mViewInfoItemBinding.sllContainer.shapeDrawableBuilder.setRadius(
            topLeftRadius,
            topRightRadius,
            bottomLeftRadius,
            bottomRightRadius
        ).intoBackground()

    }




}