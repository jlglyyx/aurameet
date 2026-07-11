package com.chat.jolt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.Utils
import com.chat.jolt.R
import com.chat.jolt.databinding.ItemCardImageBinding
import com.chat.lib_common.util.getScreenPx
import com.chat.lib_common.util.loadImage
import com.youth.banner.adapter.BannerAdapter
import kotlin.let


class CardImageAdapter(list: MutableList<String>) :
    BannerAdapter<String, RecyclerView.ViewHolder>(list) {

    val screenPx = getScreenPx(Utils.getApp())

    private val mWidth:Int = screenPx[0] * 4 / 5

    private val mHeight:Int = screenPx[1] * 4 / 5

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {

        val inflate =
            ItemCardImageBinding.inflate(LayoutInflater.from(parent?.context), parent, false)

        return object : RecyclerView.ViewHolder(inflate.root) {

        }

    }

    override fun onBindView(
        holder: RecyclerView.ViewHolder?,
        data: String?,
        position: Int,
        size: Int
    ) {

        holder?.itemView?.let {

            val mItemCardImageBinding = ItemCardImageBinding.bind(it)

            mItemCardImageBinding.ivImage.loadImage(mItemCardImageBinding.ivImage.context,data, R.drawable.iv_model_placeholder,mWidth,mHeight)
        }

    }


}


