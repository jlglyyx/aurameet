package com.chat.lib_common.im

import android.R.attr.targetId
import android.content.Context
import android.content.Intent
import android.util.Log
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.data.CustomPushMessageData
import com.chat.lib_common.im.message.PPVMessage
import com.chat.lib_common.im.message.VideoMessage
import com.chat.lib_common.tracking.mMessageNoticeKey
import com.chat.lib_common.tracking.mPopPopupDialogKey
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.cancelNotification
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.fromJson
import com.chat.lib_common.util.getCache
import com.chat.lib_common.util.startActivity
import com.chat.lib_common.util.toJson
import io.rong.common.fwlog.FwLog.param
import io.rong.imlib.IRongCoreCallback
import io.rong.imlib.IRongCoreEnum
import io.rong.imlib.IRongCoreEnum.CoreErrorCode
import io.rong.imlib.IRongCoreListener
import io.rong.imlib.RongCoreClient
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.ConversationIdentifier
import io.rong.imlib.model.HistoryMessageOption
import io.rong.imlib.model.InitOption
import io.rong.imlib.model.Message
import io.rong.imlib.model.MessageContent
import io.rong.push.PushEventListener
import io.rong.push.PushType
import io.rong.push.RongPushClient
import io.rong.push.RongPushPlugin
import io.rong.push.notification.PushNotificationMessage
import kotlin.collections.set
import kotlin.jvm.java

object RIMClient {


    private const val TAG = "RIMClient"

    const val PUSH_CONTENT = "You got a new message."

    const val PUSH_TITLE = "ChatBuddy"


    fun initRIMCoreClient(application: BaseApplication) {
//            CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {

        if (true) {
//                    delay(500)

            val appKey = when (AppConstant.ClientInfo.BASE_URL) {

                AppConstant.ClientInfo.BASE_DEV_URL -> AppConstant.RIMConstant.APP_DEV_KEY
                AppConstant.ClientInfo.BASE_DEV_URL_183 -> AppConstant.RIMConstant.APP_DEV_KEY
                AppConstant.ClientInfo.BASE_TEST_URL -> AppConstant.RIMConstant.APP_TEST_KEY
                AppConstant.ClientInfo.BASE_REAL_URL -> AppConstant.RIMConstant.APP_REAL_KEY
                else -> {
                    AppConstant.RIMConstant.APP_REAL_KEY
                }
            }

            val builder = InitOption.Builder()

            if (AppConstant.ClientInfo.BASE_URL == AppConstant.ClientInfo.BASE_REAL_URL || AppConstant.ClientInfo.BASE_URL == AppConstant.ClientInfo.BASE_TEST_URL) {

                builder.setAreaCode(InitOption.AreaCode.NA)
            }
            builder.enablePush(true)
            RongPushPlugin.init(application)
            RongCoreClient.getInstance().enableSingleProcess(true)
            RongCoreClient.init(application, appKey, builder.build())
            registerMessage()
        }
//            }
    }

    fun initRIMPush(application: BaseApplication) {
        RongPushClient.setPushEventListener(
            object : PushEventListener {
                override fun preNotificationMessageArrived(
                    context: Context,
                    pushType: PushType,
                    notificationMessage: PushNotificationMessage
                ): Boolean {
                    Log.i(TAG, "preNotificationMessageArrived: $notificationMessage")
                    // 该回调仅在通知类型为透传消息时生效。返回 true 表示拦截，false 为不拦截
                    return true
                }

                override fun afterNotificationMessageArrived(
                    context: Context,
                    pushType: PushType,
                    notificationMessage: PushNotificationMessage
                ) {

//                    try {
//                        if (!BaseApplication.isAppForeground) {
//                            playSound(this@BaseApplication)
//                            vibrate(this@BaseApplication)
//                        }
//                    } catch (e: Exception) {
//
//                    }


                    Log.i(TAG, "afterNotificationMessageArrived: ${notificationMessage.toJson()}")
                }

                override fun onNotificationMessageClicked(
                    context: Context?,
                    pushType: PushType,
                    notificationMessage: PushNotificationMessage
                ): Boolean {

                    try {


                        val mCustomPushMessageData = notificationMessage.pushData.toString()
                            .fromJson<CustomPushMessageData>()
                        Log.i(
                            TAG,
                            "afterNotificationMessageArrived: ${notificationMessage.toJson()}"
                        )
                        context?.let {


                            val createIntent = Intent(
                                context,
                                Class.forName("com.chat.jolt.activity.MainActivity")
                            )
                                .putExtra(
                                    AppConstant.Constant.PUSH_MARK,
                                    mCustomPushMessageData.pushMark
                                )
                                .putExtra(
                                    AppConstant.Constant.TARGET_ID,
                                    mCustomPushMessageData.touchValue
                                )
                                .putExtra(
                                    AppConstant.Constant.TOUCH_TYPE,
                                    mCustomPushMessageData.touchType
                                )
                            createIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                            createIntent.startActivity(context)




                            cancelNotification(application)
                        }

                        return true

                    } catch (e: Exception) {
                        e.printStackTrace()

                    }

                    return false
                }

                override fun onThirdPartyPushState(
                    pushType: PushType, action: String, resultCode: Long
                ) {
                }

                override fun onTokenReceived(pushType: PushType?, token: String?) {
                }

                override fun onTokenReportResult(
                    reportType: PushType?,
                    code: Int,
                    finalType: PushType?,
                    finalToken: String?
                ) {
                }
            })
    }


    fun onClientRIM(
        onSuccess: (Boolean) -> Unit = {},
        onError: (IRongCoreEnum.ConnectionErrorCode) -> Unit = {},
    ) {

        val mRIMToken = getCache(AppConstant.RIMConstant.RIM_TOKEN, "")

        if (mRIMToken.isEmpty()) {

            return
        }

        if (RongCoreClient.getInstance().currentConnectionStatus == IRongCoreListener.ConnectionStatusListener.ConnectionStatus.CONNECTED) {

            onSuccess(true)

            return
        }


        RongCoreClient.connect(
            mRIMToken,
            object : IRongCoreCallback.ConnectCallback() {

                override fun onDatabaseOpened(code: IRongCoreEnum.DatabaseOpenStatus) {

                    Log.i(TAG, "onDatabaseOpened")
                }

                override fun onSuccess(userId: String) {
                    Log.i(TAG, "onSuccess")
                    try {
                        onSuccess(false)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onError(errorCode: IRongCoreEnum.ConnectionErrorCode) {
                    if (errorCode.equals(RongIMClient.ConnectionErrorCode.RC_CONN_TOKEN_EXPIRE)) {
                        //从 APP 服务请求新 token，获取到新 token 后重新 connect()
                    } else if (errorCode.equals(RongIMClient.ConnectionErrorCode.RC_CONNECT_TIMEOUT)) {
                        //连接超时，弹出提示，可以引导用户等待网络正常的时候再次点击进行连接
                    } else {
                        //其它业务错误码，请根据相应的错误码作出对应处理。
                    }

                    if (errorCode.equals(RongIMClient.ConnectionErrorCode.RC_CONNECTION_EXIST)) {

                        onSuccess(true)
                    } else {
                        onError(errorCode)
                    }


                    Log.i(TAG, "onError: $errorCode")

                }


            })


    }


    fun getHistoryConversations(
        conversationIdentifiers: MutableList<ConversationIdentifier>,
        onMSuccess: (MutableList<Conversation>) -> Unit = {},
        onMError: (CoreErrorCode) -> Unit = {}
    ) {

        RongCoreClient.getInstance().getConversations(
            conversationIdentifiers,
            object : IRongCoreCallback.ResultCallback<MutableList<Conversation>>() {
                override fun onSuccess(conversations: MutableList<Conversation>) {

                    onMSuccess(conversations)

                }

                override fun onError(e: CoreErrorCode?) {

                    if (null != e) {
                        onMError(e)
                    }

                }
            })
    }

    /**
     * 获取历史消息
     */
    fun getHistoryMessages(
        targetId: String,
        conversationType: Conversation.ConversationType = Conversation.ConversationType.GROUP,
        historyMessageOption: HistoryMessageOption,
        onSuccess: (List<Message>?) -> Unit = {},
        onError: (IRongCoreEnum.CoreErrorCode) -> Unit = {},
    ) {
        RongCoreClient.getInstance().getMessages(
            conversationType,
            targetId,
            historyMessageOption
        ) { list, coreErrorCode ->

            onSuccess(list)

        }

    }


    /**
     * 会话详情
     */
    fun getConversationDetail(
        targetId: String,
        conversationType: Conversation.ConversationType = Conversation.ConversationType.GROUP,
        onSuccess: (Conversation?) -> Unit = {},
        onError: (IRongCoreEnum.CoreErrorCode) -> Unit = {},
    ) {

        RongCoreClient.getInstance().getConversation(
            conversationType,
            targetId,
            object : IRongCoreCallback.ResultCallback<Conversation?>() {
                override fun onSuccess(conversation: Conversation?) {
                    // 成功并返回会话信息
                    onSuccess(conversation)
                }

                override fun onError(errorCode: IRongCoreEnum.CoreErrorCode?) {

                    if (null != errorCode) {
                        onError(errorCode)
                    }
                }
            })
    }

    /**
     * 查询会话置顶状态
     */
    fun getConversationTopStatus(
        targetId: String,
        onSuccess: (Boolean?) -> Unit = {},
        onError: (IRongCoreEnum.CoreErrorCode) -> Unit = {},
        conversationType: Conversation.ConversationType = Conversation.ConversationType.GROUP
    ) {

        RongCoreClient.getInstance().getConversationTopStatus(
            targetId,
            conversationType,

            object : IRongCoreCallback.ResultCallback<Boolean>() {
                override fun onSuccess(t: Boolean?) {
                    onSuccess(t)
                }

                override fun onError(errorCode: IRongCoreEnum.CoreErrorCode?) {
                    if (null != errorCode) {
                        onError(errorCode)
                    }
                }

            }

        )

    }


    /**
     * 设置会话置顶状态
     */
    fun setConversationToTop(
        targetId: String,
        isTop: Boolean,
        conversationType: Conversation.ConversationType = Conversation.ConversationType.GROUP,
        onSuccess: (Boolean?) -> Unit = {},
        onError: (RongIMClient.ErrorCode) -> Unit = {}
    ) {

        RongIMClient.getInstance().setConversationToTop(
            conversationType,
            targetId,
            isTop,
            object : RongIMClient.ResultCallback<Boolean?>() {
                override fun onSuccess(success: Boolean?) {

                    onSuccess(success)
                }

                override fun onError(errorCode: RongIMClient.ErrorCode?) {

                    if (null != errorCode) {
                        onError(errorCode)
                    }
                }
            })

    }


    /**
     * 清除消息未读数
     */
    fun clearMessagesUnread(
        targetId: String,
        onSuccess: () -> Unit = {},
        onError: (RongIMClient.ErrorCode) -> Unit = {},
        conversationType: Conversation.ConversationType = Conversation.ConversationType.GROUP
    ) {

        RongIMClient.getInstance().clearMessagesUnreadStatus(
            conversationType,
            targetId,
            System.currentTimeMillis(),
            object : RongIMClient.OperationCallback() {
                override fun onSuccess() {

                    onSuccess()
                }

                override fun onError(errorCode: RongIMClient.ErrorCode?) {

                    if (null != errorCode) {
                        onError(errorCode)
                    }

                }

            })
    }


    /**
     *  getTotalUnreadCount
     */
    fun getUnreadCount(
        onSuccess: (Int) -> Unit = {},
        onError: (RongIMClient.ErrorCode) -> Unit = {},
        conversationType: Conversation.ConversationType = Conversation.ConversationType.GROUP
    ) {

        RongIMClient.getInstance().getUnreadCount(object : RongIMClient.ResultCallback<Int>() {

            override fun onSuccess(t: Int) {

                try {
                    onSuccess(t)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            override fun onError(errorCode: RongIMClient.ErrorCode?) {

                if (null != errorCode) {
                    onError(errorCode)
                }
            }

        }, conversationType)

    }

    /**
     *  getTotalUnreadCount
     */
    fun getTotalUnreadCount(
        onSuccess: (Int) -> Unit = {},
        onError: (RongIMClient.ErrorCode) -> Unit = {},
    ) {
        RongIMClient.getInstance().getTotalUnreadCount(object : RongIMClient.ResultCallback<Int>() {

            override fun onSuccess(t: Int) {

                try {
                    onSuccess(t)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            override fun onError(errorCode: RongIMClient.ErrorCode?) {

                if (null != errorCode) {
                    onError(errorCode)
                }
            }

        })


    }

    /**
     * 删除指定会话
     */
    fun removeConversation(
        targetId: String,
        onSuccess: (Boolean) -> Unit = {},
        onError: (RongIMClient.ErrorCode) -> Unit = {},
        conversationType: Conversation.ConversationType = Conversation.ConversationType.GROUP
    ) {

        RongIMClient.getInstance().removeConversation(
            conversationType,
            targetId,
            object : RongIMClient.ResultCallback<Boolean>() {

                override fun onSuccess(t: Boolean) {

                    onSuccess(t)
                }

                override fun onError(errorCode: RongIMClient.ErrorCode?) {

                    if (null != errorCode) {
                        onError(errorCode)
                    }
                }

            })


    }


    /**
     * 设置扩展字段改变监听
     */
    fun setMessageExpansionListener(
        onMessageExpansionUpdate: (Message) -> Unit = {},
        onMessageExpansionRemove: (Message?) -> Unit = {}
    ): RongIMClient.MessageExpansionListener {

        val mMessageExpansionListener = object :
            RongIMClient.MessageExpansionListener {
            override fun onMessageExpansionUpdate(
                expansion: MutableMap<String, String>?,
                message: Message
            ) {

                onMessageExpansionUpdate(message)
            }

            override fun onMessageExpansionRemove(
                keyArray: MutableList<String>?,
                message: Message?
            ) {
                onMessageExpansionRemove(message)
            }

        }
        RongIMClient.getInstance().setMessageExpansionListener(mMessageExpansionListener)

        return mMessageExpansionListener
    }


    /**
     * 更新扩展字段
     */
    fun updateMessageExpansion(
        param: HashMap<String, String>,
        uId: String,
        onSuccess: (String) -> Unit = {},
        onError: (RongIMClient.ErrorCode) -> Unit = {}
    ) {
        RongIMClient.getInstance()
            .updateMessageExpansion(
                param,
                uId,
                object : RongIMClient.OperationCallback() {
                    override fun onSuccess() {
                        onSuccess(uId)

                    }

                    override fun onError(errorCode: RongIMClient.ErrorCode) {

                        onError(errorCode)
                    }
                })

    }


    /**
     * 保存草稿
     */
    fun saveTextMessageDraft(
        targetId: String,
        content: String,
        onSuccess: (Boolean) -> Unit = {},
        onError: (RongIMClient.ErrorCode) -> Unit = {},
        conversationType: Conversation.ConversationType = Conversation.ConversationType.GROUP
    ) {
        RongIMClient.getInstance()
            .saveTextMessageDraft(conversationType, targetId, content, object :
                RongIMClient.ResultCallback<Boolean>() {
                override fun onSuccess(t: Boolean) {
                    onSuccess(t)
                }

                override fun onError(e: RongIMClient.ErrorCode) {
                    onError(e)
                }

            })
    }

    /**
     * 获取草稿
     */
    fun getTextMessageDraft(
        targetId: String,
        onSuccess: (String) -> Unit = {},
        onError: (RongIMClient.ErrorCode) -> Unit = {},
        conversationType: Conversation.ConversationType = Conversation.ConversationType.GROUP
    ) {
        RongIMClient.getInstance()
            .getTextMessageDraft(conversationType, targetId, object :
                RongIMClient.ResultCallback<String>() {
                override fun onSuccess(t: String?) {

                    var result = ""

                    if (null != t) {
                        result = t
                    }

                    onSuccess(result)
                }

                override fun onError(e: RongIMClient.ErrorCode) {
                    onError(e)
                }

            })
    }

    /**
     * 删除草稿
     */
    fun clearTextMessageDraft(

        targetId: String,
        onSuccess: (Boolean) -> Unit = {},
        onError: (RongIMClient.ErrorCode) -> Unit = {},
        conversationType: Conversation.ConversationType = Conversation.ConversationType.GROUP
    ) {
        RongIMClient.getInstance()
            .clearTextMessageDraft(conversationType, targetId, object :
                RongIMClient.ResultCallback<Boolean>() {
                override fun onSuccess(t: Boolean) {
                    onSuccess(t)
                }

                override fun onError(e: RongIMClient.ErrorCode) {
                    onError(e)
                }

            })
    }


    /**
     * 发送消息
     */
    fun sendMessage(
        message: Message,
        pushData: String,
        onSuccess: (Message) -> Unit = {},
        onError: (IRongCoreEnum.CoreErrorCode?) -> Unit = { _ -> },
    ) {
        RongCoreClient.getInstance()
            .sendMessage(
                message,
                PUSH_CONTENT,
                pushData,
                object : IRongCoreCallback.ISendMessageCallback {

                    override fun onAttached(message: Message?) {
                    }

                    override fun onSuccess(message: Message) {

                        onSuccess(message)

                    }

                    override fun onError(
                        message: Message?,
                        coreErrorCode: IRongCoreEnum.CoreErrorCode?
                    ) {
                        onError(coreErrorCode)
                    }

                })
    }


    fun deleteMessage(
        message: Array<Message>,
        conversationType: Conversation.ConversationType = Conversation.ConversationType.GROUP,
        onSuccess: () -> Unit = {},
        onError: (IRongCoreEnum.CoreErrorCode?) -> Unit = { _ -> },
    ) {
        RongCoreClient.getInstance()
            .deleteRemoteMessages(
                conversationType,
                message.first().targetId,
                message,
                object : IRongCoreCallback.OperationCallback() {


                    override fun onSuccess() {
                        onSuccess()
                    }

                    override fun onError(coreErrorCode: IRongCoreEnum.CoreErrorCode?) {
                        onError(coreErrorCode)
                    }

                })
    }


    /**
     * 注册自定义字段
     */
    fun registerMessage() {
        val myMessages = ArrayList<Class<out MessageContent?>>()
        myMessages.add(VideoMessage::class.java)
        myMessages.add(PPVMessage::class.java)
        RongIMClient.registerMessageType(myMessages)
    }


    /**
     * 退出登录
     */
    fun logout() {
        RongIMClient.getInstance().logout()
    }

}