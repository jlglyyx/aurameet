package com.chat.jolt.helper
import android.util.Log
import com.chat.jolt.data.CustomMessageExtraData
import com.chat.jolt.data.ModelMediaData
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.im.message.PPVMessage
import com.chat.lib_common.im.message.VideoMessage
import com.chat.lib_common.tracking.RECEIVE_MESSAGE
import com.chat.lib_common.tracking.mMessageEventValue
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.formatListJson
import com.chat.lib_common.util.fromJson
import com.chat.lib_common.util.toJson
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import io.rong.message.CommandMessage


fun getMessageExtraData(mConversation: Conversation?): CustomMessageExtraData? {

    try {
        if (null == mConversation) return null

        if (null == mConversation.latestMessage) return null

        val extra = mConversation.latestMessage.extra

        if (extra.isNullOrEmpty()) return null

        if (mConversation.conversationType == Conversation.ConversationType.PRIVATE || extra.startsWith("[{")) {

            val formatListJson = extra.formatListJson<CustomMessageExtraData>()

            if (formatListJson.isNullOrEmpty()) return null

            return formatListJson[0]

        } else {

            return extra.fromJson<CustomMessageExtraData>()

        }

    } catch (e: Exception) {
        e.printStackTrace()

        Log.i("TAG", "getMessageExtraData: ${mConversation?.toJson()}")
    }
    return null
}

fun getMessageExtraData(mMessage: Message?): CustomMessageExtraData? {

    try {
        if (null == mMessage) return null

        val extra = mMessage.content.extra

        if (extra.isNullOrEmpty()) return null


        if (mMessage.conversationType == Conversation.ConversationType.PRIVATE || extra.startsWith("[{")) {

            val formatListJson = extra.formatListJson<CustomMessageExtraData>()

            if (formatListJson.isNullOrEmpty()) return null

            return formatListJson[0]

        } else {
            return extra.fromJson<CustomMessageExtraData>()
        }


    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}


fun getCmdMessageExtraData(mMessage: Message?): CustomMessageExtraData? {

    try {
        if (null == mMessage) return null

        if (mMessage.content !is CommandMessage) return null

        val commandMessage = mMessage.content as CommandMessage

        val extra = commandMessage.data

        if (extra.isNullOrEmpty()) return null

        val formatListJson = extra.formatListJson<CustomMessageExtraData>()

        if (formatListJson.isNullOrEmpty()) return null

        return formatListJson[0]


    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}


fun getMessageMediaData(mMessage: VideoMessage?): ModelMediaData? {

    try {
        if (null == mMessage) return null

        val content = mMessage.content ?: return null

        return content.fromJson<ModelMediaData>()

    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun getMessagePPVData(mMessage: PPVMessage?): MutableList<ModelMediaData>? {

    try {
        if (null == mMessage) return null

        val content = mMessage.content ?: return null

        return content.formatListJson<ModelMediaData>()

    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}






fun handleMessageReportEvent(message: Message) {

    try {


        if (message.isOffline) return

        val extra = message.content.extra

        if (extra.isNullOrEmpty()) return

        if (message.objectName == AppConstant.RIMConstant.RC_TXT_MSG && message.senderUserId == AppConstant.RIMConstant.SYSTEM_NOTICE) return

        val mCustomMessageExtraData = extra.fromJson<CustomMessageExtraData>()

        val param = mutableMapOf<String, Any?>()

        param["convo_id"] = message.targetId
        param["model_id"] = mCustomMessageExtraData.userId2
        param["model_name"] = mCustomMessageExtraData.name2


        when (message.objectName) {

            AppConstant.RIMConstant.RC_TXT_MSG -> {


                param["message_type"] = mMessageEventValue[0]

                reportEvent(RECEIVE_MESSAGE, param)

            }

            AppConstant.RIMConstant.RC_IMG_MSG -> {


                param["message_type"] =
                    if (mCustomMessageExtraData.isPrivate == "True") mMessageEventValue[2] else mMessageEventValue[1]

                reportEvent(RECEIVE_MESSAGE, param)
            }

            AppConstant.RIMConstant.RC_IMG_VIDEO -> {


                if (mCustomMessageExtraData.isPrivate == "True"){

                    param["message_type"] = mMessageEventValue[3]

                    reportEvent(RECEIVE_MESSAGE, param)
                }



            }

            AppConstant.RIMConstant.RC_PP_VM_MSG -> {

                val mPPVMessage = message.content as PPVMessage

                val mPPVMessageListData =
                    mPPVMessage.content.formatListJson<ModelMediaData>()

                if (mPPVMessage.type == AppConstant.RIMConstant.RC_SEND_PRIVATE_IMAGE_S_MSG) {


                    param["message_type"] = "private photo-${mPPVMessageListData.size}"
                } else {
                    param["message_type"] =  "private video-${mPPVMessageListData.size}"
                }

                reportEvent(RECEIVE_MESSAGE, param)
            }

            AppConstant.RIMConstant.RC_NTF_MSG -> {


            }

            AppConstant.RIMConstant.RC_CMD_MSG -> {


            }

            else -> {

            }
        }

    } catch (e: Exception) {

        e.printStackTrace()
    }
}
