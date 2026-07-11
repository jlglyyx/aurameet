package com.chat.jolt.adapter

import android.view.View
import androidx.lifecycle.Lifecycle
import com.chat.jolt.R
import com.chat.jolt.data.NewConversationData
import com.chat.jolt.databinding.ItemNewConversationBinding
import com.chat.jolt.helper.getMessageExtraData
import com.chat.jolt.widget.CarouselImageView
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.util.dip2px
import com.chat.lib_common.util.loadImage

class NewConversationAdapter(val lifecycle: Lifecycle) :
    BaseRecyclerAdapter<NewConversationData, ItemNewConversationBinding>(ItemNewConversationBinding::inflate) {

    private val avatarWidth = 40f.dip2px(BaseApplication.mApplication)


    override fun convert(
        holder: BaseRecyclerViewHolder<ItemNewConversationBinding>,
        itemView: ItemNewConversationBinding,
        item: NewConversationData,
        position: Int
    ) {
        try {

            lifecycle.removeObserver(itemView.mCarouselImageView)

            lifecycle.addObserver( itemView.mCarouselImageView)

            if (null == item.mConversation) {

                itemView.mCarouselImageView.visibility = View.VISIBLE
                itemView.ivAvatar.visibility = View.INVISIBLE

                itemView.sclContainer.shapeDrawableBuilder.setSolidColor(context.getColor(R.color.color_393539)).intoBackground()

                itemView.mCarouselImageView.setImageList(item.list.map { it.headPic }
                    .toMutableList())

                if (item.name == "Likes") {
                    itemView.mCarouselImageView.setStrokeColor(R.color.color_C678FF)
                } else {
                    itemView.mCarouselImageView.setStrokeColor(R.color.color_FF78C1)
                }

                itemView.tvName.text = item.name

                if (item.count == 0) {
                    itemView.stvMessageCount.visibility = View.GONE
                } else {
                    itemView.stvMessageCount.text = "${item.count}"
                    itemView.stvMessageCount.visibility = View.VISIBLE
                }
                if (item.addCount == 0) {
                    itemView.tvAddCount.visibility = View.GONE
                } else {
                    itemView.tvAddCount.text = "${item.addCount}"
                    itemView.tvAddCount.visibility = View.VISIBLE
                }
                itemView.stvNew.visibility = View.GONE
            } else {
                itemView.mCarouselImageView.visibility = View.GONE
                itemView.ivAvatar.visibility = View.VISIBLE

                setLastMessage(itemView, item)
                itemView.sclContainer.shapeDrawableBuilder.setSolidColor(context.getColor(R.color.color_4FFF3170)).intoBackground()
                itemView.stvNew.visibility = View.VISIBLE
                itemView.tvAddCount.visibility = View.GONE

            }


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private fun setLastMessage(itemView: ItemNewConversationBinding, item: NewConversationData) {


        val mConversation = item.mConversation ?: return

        val messageExtraData = getMessageExtraData(mConversation)

        if (null == messageExtraData) {
            itemView.tvName.text = ""
            itemView.ivAvatar.loadImage(context, "")
        } else {
            itemView.ivAvatar.loadImage(
                context,
                messageExtraData.headPic2, avatarWidth, avatarWidth
            )
            if (null == item.mConversationStatusData?.age) {

                itemView.tvName.text = messageExtraData.name2
            } else {
                itemView.tvName.text =
                    messageExtraData.name2 + ",${item.mConversationStatusData?.age}"
            }

        }
        if (mConversation.unreadMessageCount == 0) {
            itemView.stvMessageCount.visibility = View.GONE
        } else {
            itemView.stvMessageCount.text = "${mConversation.unreadMessageCount}"
            itemView.stvMessageCount.visibility = View.VISIBLE
        }


    }

    fun stopCarousel(){
        try {
            items.forEachIndexed { index, data ->
                val layoutManager = recyclerView.layoutManager?:return
                val findViewByPosition = layoutManager.findViewByPosition(index)?:return
                val mCarouselImageView = findViewByPosition.findViewById<CarouselImageView>(R.id.mCarouselImageView)?:return
                mCarouselImageView.release()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

    }
}