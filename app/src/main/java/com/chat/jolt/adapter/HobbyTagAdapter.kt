package com.chat.jolt.adapter

import com.chat.jolt.R
import com.chat.jolt.databinding.ItemHobbyStepBinding
import  com.chat.jolt.data.HobbyTagData
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.util.formatListJson
import com.chat.lib_common.util.getCache
import kotlin.text.isEmpty

class HobbyTagAdapter :
    BaseRecyclerAdapter<HobbyTagData, ItemHobbyStepBinding>(ItemHobbyStepBinding::inflate) {


    override fun convert(
        holder: BaseRecyclerViewHolder<ItemHobbyStepBinding>,
        itemView: ItemHobbyStepBinding,
        item: HobbyTagData,
        position: Int
    ) {
        itemView.tvTitle.text = item.hobbyTagName

        if (item.isCheck) {
            itemView.tvTitle.setTextColor(context.getColor(R.color.firstTextColor))
            itemView.sllContainer.shapeDrawableBuilder.setSolidColor(context.getColor(R.color.color_FDDBFF))
                .intoBackground()
        } else {
            itemView.tvTitle.setTextColor(context.getColor(R.color.white))
            itemView.sllContainer.shapeDrawableBuilder.setSolidColor(context.getColor(R.color.inputColor))
                .intoBackground()
        }
    }


    fun getCacheHobbyTag(): MutableList<HobbyTagData>? {

        val mHobbyTag = getCache(AppConstant.Constant.HOBBY_TAG, "")

        return if (mHobbyTag.isEmpty()) {

            mutableListOf()
        } else {

            mHobbyTag.formatListJson()
        }
    }
}