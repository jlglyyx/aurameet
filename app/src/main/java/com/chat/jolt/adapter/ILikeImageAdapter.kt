package com.chat.jolt.adapter

import com.chat.jolt.R
import com.chat.jolt.data.ILikeImageData
import com.chat.jolt.databinding.ItemILikeImageBinding
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.util.getScreenPx
import com.chat.lib_common.util.loadImage


class ILikeImageAdapter() :
    BaseRecyclerAdapter<ILikeImageData, ItemILikeImageBinding>(ItemILikeImageBinding::inflate) {

    private val pictureWidth = getScreenPx(BaseApplication.mApplication)[0]/3

    private val pictureHeight = pictureWidth
    override fun convert(
        holder: BaseRecyclerViewHolder<ItemILikeImageBinding>,
        itemView: ItemILikeImageBinding,
        item: ILikeImageData,
        position: Int
    ) {
        itemView.ivImage.loadImage(itemView.ivImage.context,item.cover, R.drawable.iv_placeholder,pictureWidth,pictureHeight)
    }




}


