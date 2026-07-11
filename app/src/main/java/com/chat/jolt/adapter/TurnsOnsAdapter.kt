package com.chat.jolt.adapter

import android.view.View
import com.chat.jolt.R
import com.chat.jolt.data.TagData
import com.chat.jolt.databinding.ItemTurnsOnsBinding
import com.chat.jolt.databinding.ItemTurnsOnsBinding.inflate
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.loadImageNoPlaceholder

class TurnsOnsAdapter(val isShowCheck: Boolean = true):BaseRecyclerAdapter<TagData, ItemTurnsOnsBinding>(ItemTurnsOnsBinding::inflate) {
    override fun convert(
        holder: BaseRecyclerViewHolder<ItemTurnsOnsBinding>,
        itemView: ItemTurnsOnsBinding,
        item: TagData,
        position: Int
    ) {

        itemView.ivImage.loadImageNoPlaceholder(itemView.ivImage.context,item.tagUrl)

        itemView.tvTitle.text = item.tagName
        itemView.tvContent.text = item.tagDesc

        if (isShowCheck){

            if (item.isCheck){

                itemView.ivImageCheck.visibility = View.VISIBLE

                itemView.sllContainer.shapeDrawableBuilder.setSolidColor(context.getColor(R.color.color_FDDBFF)).intoBackground()
            }else{
                itemView.ivImageCheck.visibility = View.GONE
                itemView.sllContainer.shapeDrawableBuilder.setSolidColor(context.getColor(R.color.white)).intoBackground()
            }

        }else{
            itemView.ivImageCheck.visibility = View.GONE
            itemView.sllContainer.shapeDrawableBuilder.setSolidColor(context.getColor(R.color.white)).intoBackground()
        }

    }
}