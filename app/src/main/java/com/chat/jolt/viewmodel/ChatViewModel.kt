package com.chat.jolt.viewmodel

import android.util.Log
import com.chat.jolt.api.ApiService
import com.chat.jolt.data.ConversationStatusData
import com.chat.jolt.data.CurrentModelMediaData
import com.chat.jolt.data.CustomMessage
import com.chat.jolt.data.CustomMessageExtraData
import com.chat.jolt.data.ModelMediaData
import com.chat.jolt.data.ReportData
import com.chat.jolt.data.UnlockAlbums
import com.chat.jolt.data.UserRelationData
import com.chat.jolt.helper.UserInfoHold
import com.chat.lib_common.bus.SingleFlow
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.http.HttpClient
import com.chat.lib_common.im.RIMClient
import com.chat.lib_common.tracking.MESSAGE_CHAT_KEY
import com.chat.lib_common.tracking.mMessageClickValue
import com.chat.lib_common.tracking.mMessageEventKey
import com.chat.lib_common.tracking.mRightShowValue
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.showShort
import com.chat.lib_common.util.toJson
import io.rong.imlib.IRongCoreCallback
import io.rong.imlib.IRongCoreEnum.CoreErrorCode
import io.rong.imlib.RongCoreClient
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.ConversationIdentifier
import io.rong.imlib.model.HistoryMessageOption
import io.rong.imlib.model.Message
import kotlinx.coroutines.flow.MutableSharedFlow
import java.net.SocketTimeoutException

class ChatViewModel : PublicViewModel() {

    private val mApiService = HttpClient.createApi(ApiService::class.java)

    val mConversationListData = SingleFlow<MutableList<Conversation>>()

    val mNewConversationListData = SingleFlow<MutableList<Conversation>>()

    val mConversationListStatus = SingleFlow<Boolean>()

    val mConversationStatusData = SingleFlow<MutableMap<String, ConversationStatusData>>()

    val mLoveConversationStatusData = SingleFlow<MutableMap<String, ConversationStatusData>>()

    val mLoveConversationListData = SingleFlow<MutableList<Conversation>>()

    private var chatStartHistoryTime = 0L

    private var lastSendTime = 0L

    private val historyMessageOption = HistoryMessageOption().apply {

        setOrder(HistoryMessageOption.PullOrder.DESCEND)

        count = 100
//        count = AppConstant.Constant.PAGE_SIZE_COUNT
    }

    val mUserRelationData = SingleFlow<UserRelationData>()

    val mSendSingleMessageFlow = MutableSharedFlow<MutableList<ModelMediaData>>()

    val messageSendStatusFlow = MutableSharedFlow<ModelMediaData>()

    val mMessageListData = SingleFlow<MutableList<Message>>()

    val mBlockStatus = SingleFlow<Boolean>()

    val mReportData = SingleFlow<MutableList<ReportData>>()

    val mReportStatus = SingleFlow<Boolean>()

    val mModelMediaPhotoData = SingleFlow<CurrentModelMediaData>()

    val mModelMediaVideoData = SingleFlow<CurrentModelMediaData>()

    val mAddModelMediaData = SingleFlow<ModelMediaData>()

    val mDeleteModelMediaStatus = SingleFlow<MutableList<String>>()

    val mUnlockMediaData = SingleFlow<CustomMessageExtraData>()

    val mUnlockAlbumStatus = SingleFlow<CustomMessage>()

    fun getConversationList(isRefresh: Boolean = false) {

        if (isRefresh) {
            chatStartHistoryTime = 0L
        }

        RongCoreClient.getInstance().getConversationListByPage(
            object : IRongCoreCallback.ResultCallback<MutableList<Conversation>>() {
                override fun onSuccess(list: MutableList<Conversation>) {


                    if (list.isNotEmpty()) {


                        chatStartHistoryTime = list.last().sentTime

                        getOnlineStatus(relationIds = list.map {

                            it.targetId

                        }.filter { it.isNotEmpty() }.toMutableList())

                    }
                    mConversationListData.postValue(list)

                    mConversationListStatus.postValue(true)

                }

                override fun onError(e: CoreErrorCode?) {

                    mConversationListStatus.postValue(false)

                }

            },
            chatStartHistoryTime,
            AppConstant.Constant.PAGE_SIZE_COUNT,
            false,
            Conversation.ConversationType.GROUP
        )


    }

    fun getNewConversationList() {

        RongCoreClient.getInstance().getConversationListByPage(
            object : IRongCoreCallback.ResultCallback<MutableList<Conversation>>() {
                override fun onSuccess(list: MutableList<Conversation>) {

                    if (list.isNotEmpty()) {

                        val data = list.filter {
                            null != it.latestMessage && (null != it.latestExpansion && it.latestExpansion["isNewConversation"] == "True")
                        }.toMutableList()


                        mNewConversationListData.postValue(data)

                    } else {
                        mNewConversationListData.postValue(list)
                    }

                }

                override fun onError(e: CoreErrorCode?) {


                }

            },
            0L,
            50,
            false,
            Conversation.ConversationType.GROUP
        )


    }


    fun getOnlineStatus(relationIds: MutableList<String>) {

        if (relationIds.isEmpty()) return

        val params = mutableMapOf<String, Any?>()

        params["relationIds"] = relationIds

        doRequest(onRequest = {
            mApiService.getOnlineStatus(params)
        }, onSuccess = {

            mConversationStatusData.postValue(it.data)
        })

    }

    fun getLoveConversation() {

        val params = mutableMapOf<String, Any?>()

        params["cRelationStatus"] = "Love"

        doRequest(onRequest = {
            mApiService.getLoveConversation(params)
        }, onSuccess = {


            val list = it.data.map { map ->

                ConversationIdentifier(Conversation.ConversationType.GROUP, map.value.relationId)
            }.toMutableList()

            if (list.isNullOrEmpty()){

                mLoveConversationListData.postValue(mutableListOf())

                mLoveConversationStatusData.postValue(it.data)
            }

            RIMClient.getHistoryConversations(list, onMSuccess = { mLoveConversation ->

                mLoveConversationListData.postValue(mLoveConversation)

                mLoveConversationStatusData.postValue(it.data)
            })


        })

    }

    fun updateSetting(status: String) {

        val params = mutableMapOf<String, Any?>()

        params["setValue"] = status

        params["settingType"] = "APP_NOTIFY_GRANT"

        doRequest(onRequest = {
            mApiService.updateSetting(params)
        }, onSuccess = {

        })

    }


    fun getChatBasic(groupId: String) {

        val params = mutableMapOf<String, Any?>()

        params["groupId"] = groupId

        doRequest({

            mApiService.getChatBasic(params)
        }, {

            getOnlineStatus(mutableListOf(groupId))

            mUserRelationData.postValue(it.data)
        }, {

            showShort(it.message)
        })

    }


    fun getHistoryMessage(targetId: String) {

        historyMessageOption.dataTime = lastSendTime

        RIMClient.getHistoryMessages(
            targetId,
            Conversation.ConversationType.GROUP,
            historyMessageOption,
            onSuccess = { messageList ->

                val list = messageList?.toMutableList() ?: mutableListOf()

                mMessageListData.postValue(list)

                if (list.isNotEmpty()) {
                    lastSendTime = list.last().sentTime
                }

//                list.forEach { ii ->
//
//                    Log.i(TAG, "getHistoryMessage: ${ii.toJson()}")
//                }

                Log.i(TAG, "getMessage: ${list.toJson()}")
                Log.e("tag", "list---" + list.size)
            })


    }


    fun sendMsg(
        groupId: String,
        mModelMediaData: MutableList<ModelMediaData>,
        contentType: String,
        onSuccess: () -> Unit = {}
    ) {

        if (null == mUserRelationData.currentValue()) {

            showShort("Get userInfo error")

            return
        }

        val params = mutableMapOf<String, Any?>()
        params["contentType"] = contentType
        params["relationId"] = groupId
        params["content"] = mModelMediaData[0].content

        doRequest({

            mApiService.sendMsg(params)
        }, {

            mModelMediaData[0].msgId = it.data.msgId
            mModelMediaData[0].turnOnsGuide = it.data.turnOnsGuide
            mModelMediaData[0].sendType = contentType
            mSendSingleMessageFlow.emit(mModelMediaData)

            onSuccess()
        }, onError = {

            mModelMediaData[0].sendType = contentType
            mModelMediaData[0].errorCode = it.code

            when (it.code.toString()) {
                "1001" -> {

                    mModelMediaData[0].sendMessageStatus = 0

                    messageSendStatusFlow.emit(mModelMediaData[0])


                    requestFailEvent.postValue(it)
                }

                "1006", "1007" -> {

                    messageSendStatusFlow.emit(mModelMediaData[0])

                    requestFailEvent.postValue(it)
                }

                "1107" -> {

                    showShort("Content violation. Not sent")

                    messageSendStatusFlow.emit(mModelMediaData[0])
                }

                "1008" -> {

                    showShort("You have been blacklisted by the other party")

                    messageSendStatusFlow.emit(mModelMediaData[0])
                }

                "1108" -> {

                    showShort("Content violation. Not sent")

                    messageSendStatusFlow.emit(mModelMediaData[0])
                }


                "501" -> {

                    mModelMediaData[0].sendMessageStatus = 0

                    messageSendStatusFlow.emit(mModelMediaData[0])

                    requestFailEvent.postValue(it)

                }

                else -> {

                }
            }

            reportEvent(mMessageEventKey[6], it.message)

        }, onException = {

            mModelMediaData[0].sendType = contentType

            when (it) {

                is SocketTimeoutException -> {
                    showShort("Please check your network connection")
                }

                else -> {

                    showShort(it.message.toString())
                }
            }

            mModelMediaData[0].sendMessageStatus = 0

            messageSendStatusFlow.emit(mModelMediaData[0])

            reportEvent(mMessageEventKey[6],it.message.toString())
            false
        })


    }


    fun sendMatchMsg(
        groupId: String,
        content: String,
        contentType: String,
        onSuccess: () -> Unit = {}
    ) {

        doRequest({
            val params = mutableMapOf<String, Any?>()
            params["contentType"] = contentType
            params["relationId"] = groupId
            params["content"] = content
            mApiService.sendMsg(params)
        }, {

            onSuccess()
        }, onError = {

        })

    }


    fun blockUser(friendId: String) {

        val params = mutableMapOf<String, Any?>()

        params["friendId"] = friendId

        doRequest(onRequest = {
            mApiService.blockUser(params)
        }, onSuccess = {

            mBlockStatus.postValue(true)

        })

    }


    fun getReportType() {


        doRequest(onRequest = {
            mApiService.getReportType()
        }, onSuccess = {

            mReportData.postValue(it.data)

        })

    }

    fun doReport(list: MutableList<String>, friendId: String, detail: String) {

        val params = mutableMapOf<String, Any?>()

        params["reportType"] = list
        params["friendId"] = friendId
        params["detail"] = detail
        params["pics"] = mutableListOf<String>()

        doRequest(onRequest = {
            mApiService.doReport(params)
        }, onSuccess = {

            mReportStatus.postValue(true)

        })

    }


    fun queryMediaPhoto(
        relationId: String,
    ) {
        val params = mutableMapOf<String, Any?>()
        params["albumType"] = "Photo"
        params["relationId"] = relationId

        doRequest({

            mApiService.queryMediaV2(params)
        }, {
            mModelMediaPhotoData.postValue(CurrentModelMediaData(0, it.data.albumVOs, it.data))

        }, {
        })
    }

    fun queryMediaVideo(
        relationId: String,
    ) {

        val params = mutableMapOf<String, Any?>()
        params["albumType"] = "Video"
        params["relationId"] = relationId

        doRequest({

            mApiService.queryMediaV2(params)
        }, {
            mModelMediaVideoData.postValue(CurrentModelMediaData(0, it.data.albumVOs, it.data))

        }, {

        })
    }


    fun addMedia(
        uploadId: String?,
        albumType: String,
        albumUrl: String,
        videoSeconds: Int = 0,
        videoCover: String? = "",
    ) {

        val params = mutableMapOf<String, Any?>()

        params["albumType"] = albumType

        params["albumUrl"] = albumUrl

        params["videoSeconds"] = videoSeconds

        params["videoCover"] = videoCover

        doRequest({

            mApiService.addMedia(params)
        }, {

            it.data.albumType = albumType
            it.data.albumUrl = albumUrl
            it.data.videoSeconds = videoSeconds
            it.data.videoCover = videoCover
            it.data.uploadId = uploadId
            it.data.duration = videoSeconds

            mAddModelMediaData.postValue(it.data)

        }, {
        })
    }

    fun deleteMedia(
        albumIds: MutableList<String>,
    ) {

        val params = mutableMapOf<String, Any?>()

        params["albumIds"] = albumIds


        doRequest({

            mApiService.deleteMedia(params)
        }, {

            mDeleteModelMediaStatus.postValue(albumIds)

        }, {
        })
    }


    fun unlockV2(
        message: Message,
        isImage: Boolean,
        position: Int,
        groupId: String,
        albumId: String,
        msgId: String,
        albumIds: String = "",
    ) {
        val params = mutableMapOf<String, Any?>()
        params["groupId"] = groupId
        params["albumId"] = albumId
        params["albumIds"] = albumIds
        params["msgId"] = msgId


        val reportParams = mutableMapOf<String, Any?>()

        reportParams["user_type"] = UserInfoHold.isNewUser


        doRequest({

            mApiService.unlockV2(params)
        }, {

            it.data.position = position

            it.data.mMessage = message

            mUnlockMediaData.postValue(it.data)

            getUserInfo()

            reportParams["method"] = mMessageClickValue[0]

            if (isImage){

                reportEvent(mMessageEventKey[4],reportParams)
            }else{

                reportEvent(mMessageEventKey[5],reportParams)
            }



        }, {

            when (it.code.toString()) {
                "1001" -> {


                    getVipInfo(
                        AppConstant.Constant.PAY_VIP,
                        "",
                        if (isImage) "SecretPhoto" else "SecretVideo"
                    )
                    reportParams["method"] = mMessageClickValue[1]

                    if (isImage){

                        reportEvent(mMessageEventKey[4],reportParams)
                    }else{

                        reportEvent(mMessageEventKey[5],reportParams)
                    }

                }

                "1004" -> {
                    getVipInfo(AppConstant.Constant.PAY_PRIVATE_PHOTO, mRightShowValue[1], "PremiumBadge")
                    reportParams["method"] = mMessageClickValue[2]

                    if (isImage){

                        reportEvent(mMessageEventKey[4],reportParams)
                    }else{

                        reportEvent(mMessageEventKey[5],reportParams)
                    }

                }

                "1005" -> {
                    getVipInfo(AppConstant.Constant.PAY_PRIVATE_VIDEO, mRightShowValue[1], "PremiumBadge")

                    reportParams["method"] = mMessageClickValue[3]

                    if (isImage){

                        reportEvent(mMessageEventKey[4],reportParams)
                    }else{

                        reportEvent(mMessageEventKey[5],reportParams)
                    }
                }

            }
        })
    }


    fun unlockAlbum(unlockId: String,mUnlockAlbums:UnlockAlbums,mCustomMessage:CustomMessage,position: Int,isPhoto: Boolean) {

        val params = mutableMapOf<String, Any?>()

        params["unlockId"] = unlockId

        val reportParams = mutableMapOf<String, Any?>()

        reportParams["user_type"] = UserInfoHold.isNewUser

        doRequest(onRequest = {
            mApiService.unlockAlbum(params)
        }, onSuccess = {

            mUnlockAlbums.albumStatus = "Unlock"

            mCustomMessage.position = position

            mUnlockAlbumStatus.postValue(mCustomMessage)


            getUserInfo()

            reportParams["method"] = mMessageClickValue[0]

            if (isPhoto){
                reportEvent(MESSAGE_CHAT_KEY[0],reportParams)
            }else{
                reportEvent(MESSAGE_CHAT_KEY[1],reportParams)
            }




        },{
            when (it.code.toString()) {
                "1001" -> {


                    getVipInfo(
                        AppConstant.Constant.PAY_VIP,
                        "",
                        "SecretVideo"
                    )


                }

                "1004" -> {
                    getVipInfo(AppConstant.Constant.PAY_PRIVATE_PHOTO, mRightShowValue[1], "PremiumBadge")

                    reportParams["method"] = mMessageClickValue[2]

                    reportEvent(MESSAGE_CHAT_KEY[0],reportParams)
                }

                "1005" -> {
                    getVipInfo(AppConstant.Constant.PAY_PRIVATE_VIDEO, mRightShowValue[1], "PremiumBadge")

                    reportParams["method"] = mMessageClickValue[3]

                    reportEvent(MESSAGE_CHAT_KEY[1],reportParams)
                }

            }
        })

    }

}