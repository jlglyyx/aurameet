package com.chat.jolt.adapter

import com.chat.jolt.R
import com.chat.jolt.databinding.ItemNoticeMessageBinding
import com.chat.jolt.helper.getMessageExtraData
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.util.highlightContacts
import io.rong.imlib.model.Message

class NoticeMessageAdapter :
    BaseRecyclerAdapter<Message, ItemNoticeMessageBinding>(ItemNoticeMessageBinding::inflate) {

    override fun convert(
        holder: BaseRecyclerViewHolder<ItemNoticeMessageBinding>,
        itemView: ItemNoticeMessageBinding,
        item: Message,
        position: Int
    ) {

        try {
            val messageExtraData = getMessageExtraData(item)


            highlightContacts(itemView.tvMessage,messageExtraData?.content.toString(), R.color.color_button)


        }catch (e: Exception){
            e.printStackTrace()
        }


    }
}




