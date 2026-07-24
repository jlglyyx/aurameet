package com.chat.jolt.adapter


import android.view.View
import com.chat.jolt.R
import com.chat.jolt.data.CustomConversationData
import com.chat.jolt.data.CustomMessageExtraData
import com.chat.jolt.databinding.ItemConversationBinding
import com.chat.jolt.helper.getMessageExtraData
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.im.message.PPVMessage
import com.chat.lib_common.im.message.VideoMessage
import com.chat.lib_common.util.dip2px
import com.chat.lib_common.util.formatListJson
import com.chat.lib_common.util.getLocalFormatTime
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.viewVisibility
import io.rong.imlib.model.Conversation
import io.rong.message.ImageMessage
import io.rong.message.InformationNotificationMessage
import io.rong.message.TextMessage
import java.util.Date
import kotlin.apply
import kotlin.collections.isNotEmpty
import kotlin.text.isNullOrEmpty

class ConversationAdapter :
    BaseRecyclerAdapter<CustomConversationData, ItemConversationBinding>(ItemConversationBinding::inflate) {

    private val avatarWidth = 40f.dip2px(BaseApplication.mApplication)

    private val avatarHeight = avatarWidth

    override fun convert(
        holder: BaseRecyclerViewHolder<ItemConversationBinding>,
        itemView: ItemConversationBinding,
        item: CustomConversationData,
        position: Int
    ) {
        try {

            val mConversationStatusData = item.mConversationStatusData

            item.mConversation.apply {

                itemView.stvMessageCount.text = "$unreadMessageCount"
                itemView.stvMessageCount.visibility =
                    if (unreadMessageCount == 0) View.GONE else View.VISIBLE
                itemView.stvMessageRed.visibility = View.GONE
                itemView.llPin.setBackgroundResource(if (isTop) R.color.color_999999 else R.color.color_1DAA61)
                itemView.ivPin.setImageResource(if (isTop) R.drawable.iv_un_pin else R.drawable.iv_pin)
                itemView.ivPinTag.visibility = if (isTop) View.VISIBLE else View.GONE
                itemView.clContainer.setBackgroundResource(if (isTop) R.color.color_15EAA82B else R.color.color_36343A)


                setLastMessage(itemView, item)


                itemView.swipeMenuLayout.allowRightMenu = true

                val messageExtraData = getMessageExtraData(this)

                if (null == messageExtraData) {
                    itemView.tvName.text = ""
                    itemView.ivAvatar.loadImage(context, "")
                    itemView.ivFlashTag.visibility = View.GONE
                } else {
                    itemView.ivAvatar.loadImage(
                        context,
                        messageExtraData.headPic2 ,avatarWidth,avatarHeight
                    )

                    if (null == mConversationStatusData?.age){

                        itemView.tvName.text = messageExtraData.name2
                    }else{
                        itemView.tvName.text = messageExtraData.name2+",${mConversationStatusData?.age}"
                    }


                    if (messageExtraData.source == AppConstant.RIMConstant.CMD_FLASH_CHAT) {
                        itemView.ivFlashTag.visibility = View.VISIBLE
                        itemView.ivFlashTag.setImageResource(R.drawable.iv_flash_tag)
                    } else {
                        itemView.ivFlashTag.visibility = View.GONE
                    }
                }


                itemView.tvUpdateTime.text = getLocalFormatTime(Date(sentTime))

            }




            if (null == mConversationStatusData) {


                viewVisibility(View.GONE,itemView.stvAline,itemView.ivEm,itemView.ivLove)

            } else {



                itemView.stvAline.visibility =
                    if (mConversationStatusData.onlineStatus == "Online") View.VISIBLE else View.GONE
                itemView.ivEm.visibility =
                    if (mConversationStatusData.receivedPPV == "True") View.VISIBLE else View.GONE
                itemView.ivLove.visibility =
                    if (mConversationStatusData.unlockedPPV == "True") View.VISIBLE else View.GONE

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun setLastMessage(itemView: ItemConversationBinding, item: CustomConversationData) {

        try {

            val mConversation = item.mConversation

            val latestMessage = mConversation.latestMessage


            when (latestMessage) {

                is TextMessage -> {

                    if (mConversation.conversationType == Conversation.ConversationType.PRIVATE) {

                        if (!latestMessage.extra.isNullOrEmpty()) {

                            val mCustomMessageExtraData =
                                latestMessage.extra.formatListJson<CustomMessageExtraData>()

                            if (mCustomMessageExtraData.isNotEmpty()) {

                                itemView.tvMessage.text = mCustomMessageExtraData[0].content
                            }
                        }
                    } else {
                        itemView.tvMessage.text = latestMessage.content
                    }
                }

                is ImageMessage -> {

                    val messageExtraData = getMessageExtraData(mConversation)

                    if (messageExtraData?.isPrivate == "True") {
                        itemView.tvMessage.text = "[Private Photo]"
                    } else {
                        itemView.tvMessage.text = "[Photo]"
                    }

                }

                is VideoMessage -> {

                    val messageExtraData = getMessageExtraData(mConversation)

                    if (messageExtraData?.isPrivate == "True") {

                        itemView.tvMessage.text = "[Private Video]"

                    } else {

                        itemView.tvMessage.text = "[Video]"
                    }

                }

                is PPVMessage -> {

                    if (latestMessage.type == AppConstant.RIMConstant.RC_SEND_PRIVATE_IMAGE_S_MSG){

                        itemView.tvMessage.text = "[Private Photo]"

                    }else{

                        itemView.tvMessage.text = "[Private Video]"
                    }



                }


                is InformationNotificationMessage -> {

                    val messageExtraData = getMessageExtraData(mConversation)

                    if (null == messageExtraData) {

                        itemView.tvMessage.text = latestMessage.message
                    } else {

                        if (messageExtraData.eventCode == AppConstant.RIMConstant.CMD_MATCH_SUCCESS) {

                            itemView.tvMessage.text = "We're matched"
                        } else {
                            itemView.tvMessage.text = latestMessage.message
                        }

                    }


                    if (mConversation.unreadMessageCount != 0) {

                        itemView.stvMessageCount.visibility = View.VISIBLE

                        itemView.stvMessageRed.visibility = View.GONE
                    } else {
                        if (null != mConversation.latestExpansion && mConversation.latestExpansion["red"] == "True") {
                            itemView.stvMessageRed.visibility = View.VISIBLE
                        } else {
                            itemView.stvMessageRed.visibility = View.GONE
                        }
                        itemView.stvMessageCount.visibility = View.GONE
                    }


                }

                else -> {
                    itemView.tvMessage.text = ""
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}