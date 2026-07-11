package com.chat.jolt.activity

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ResourceUtils
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.loadState.leading.LeadingLoadStateAdapter.OnLeadingListener
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.chat.jolt.R
import com.chat.jolt.adapter.ChatAdapter
import com.chat.jolt.data.CustomLockMessageData
import com.chat.jolt.data.CustomMessage
import com.chat.jolt.data.CustomMessageExtraData
import com.chat.jolt.data.ModelMediaData
import com.chat.jolt.data.PreViewMediaData
import com.chat.jolt.data.UserRelationData
import com.chat.jolt.data.VipData
import com.chat.jolt.databinding.ActChatBinding
import com.chat.jolt.databinding.ItemEmojiBinding
import com.chat.jolt.dialog.AlbumNoticeDialog
import com.chat.jolt.dialog.BuyRightDialog
import com.chat.jolt.dialog.BuyVipDialog
import com.chat.jolt.dialog.ChatMoreDialog
import com.chat.jolt.dialog.PreviewMediaDialog
import com.chat.jolt.dialog.SendMediaDialog
import com.chat.jolt.dialog.TextOperationDialog
import com.chat.jolt.helper.ChatHelper
import com.chat.jolt.helper.FloatingWindowUtil
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.helper.getCmdMessageExtraData
import com.chat.jolt.helper.getMessageExtraData
import com.chat.jolt.helper.getMessageMediaData
import com.chat.jolt.helper.getMessagePPVData
import com.chat.jolt.viewmodel.ChatViewModel
import com.chat.jolt.widget.GridImageView
import com.chat.lib_common.activity.BaseActivity
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.adapter.TopLoadAdapter
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.observe
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.http.HttpException
import com.chat.lib_common.im.RIMClient
import com.chat.lib_common.im.RIMClient.PUSH_CONTENT
import com.chat.lib_common.im.RIMClient.PUSH_TITLE
import com.chat.lib_common.im.RIMDispatcher
import com.chat.lib_common.im.message.PPVMessage
import com.chat.lib_common.im.message.VideoMessage
import com.chat.lib_common.tracking.MESSAGE_CHAT_KEY
import com.chat.lib_common.tracking.mMessageClickValue
import com.chat.lib_common.tracking.mMessageNoticeKey
import com.chat.lib_common.tracking.mVipShowValue
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.CompressUtil.isValidMedia
import com.chat.lib_common.util.OSSUtil
import com.chat.lib_common.util.ScreenChangeUtil
import com.chat.lib_common.util.VideoPlayerUtil
import com.chat.lib_common.util.cancelNotification
import com.chat.lib_common.util.click
import com.chat.lib_common.util.copyContent
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.edgeToEdgeTop
import com.chat.lib_common.util.findNextIndexLoop
import com.chat.lib_common.util.formatListJson
import com.chat.lib_common.util.formatWithSymbol
import com.chat.lib_common.util.fromJson
import com.chat.lib_common.util.getRealUrl
import com.chat.lib_common.util.hideSoftInput
import com.chat.lib_common.util.isAlive
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.loadOptionImage
import com.chat.lib_common.util.showShort
import com.chat.lib_common.util.startActivity
import com.chat.lib_common.util.symbolToList
import com.chat.lib_common.util.toJson
import com.chat.lib_common.util.viewVisibility
import com.chat.lib_common.widget.BlurTransformation
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.lxj.xpopup.enums.PopupPosition
import io.rong.imlib.RongCoreClient
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import io.rong.imlib.model.MessageContent
import io.rong.imlib.model.MessagePushConfig
import io.rong.imlib.model.ReceivedProfile
import io.rong.message.ImageMessage
import io.rong.message.TextMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.TimeZone


class ChatActivity : BaseActivity<ActChatBinding, ChatViewModel>(ActChatBinding::inflate) {

    private lateinit var mChatAdapter: ChatAdapter

    private lateinit var mQuickAdapterHelper: QuickAdapterHelper

    private lateinit var mEmojiAdapter: BaseRecyclerAdapter<String, ItemEmojiBinding>

    private var targetId: String = ""

    private var text: String = ""

    private val conversationType = Conversation.ConversationType.GROUP

    private var mSendTextMessageList = mutableListOf<ModelMediaData>()

    private lateinit var mCustomMessage: CustomMessage

    private var pushData = ""

    private var messageExpansionListener: RongIMClient.MessageExpansionListener? = null


    private var isFirstSend = true

    private var friendId: String? = ""

    private var mUserRelationData: UserRelationData? = null

    private var mBuyVipDialog: BuyVipDialog? = null

    private var mBuyRightDialog: BuyRightDialog? = null

    private var lastPPvIndex = 0

    private var countPPv = 0

    private var currentCountPPv = 0

    private var isCheckScroll = false

    private var isNoticeInto: Boolean = false

    private var pushMark: String = ""

    private var isOffline: Boolean = false

    private var buyRightCount = 1

    private var mCoverImageView: ImageView? = null

    private val requestOptions =
        RequestOptions.bitmapTransform(BlurTransformation(AppConstant.Constant.PPV_BLUR_RADIUS, 2))


    private var mLinearLayoutManager: LinearLayoutManager? = null

    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        onPhotoPicked(uri)
    }

    private val pickLegacy = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            onPhotoPicked(uri)
        }
    }


    private var mMessageListener = object : RIMDispatcher.MessageListener {
        override fun onMessageReceiptResponse(
            message: Message,
            type: Conversation.ConversationType,
            targetId: String,
            mReceivedProfile: ReceivedProfile
        ) {
            if (message.objectName != AppConstant.RIMConstant.RC_CMD_MSG && targetId == this@ChatActivity.targetId) {

                lifecycleScope.launch(Dispatchers.Main) {
                    try {
                        mCustomMessage = CustomMessage(message)

                        mChatAdapter.add(mCustomMessage)

                        addOrReduceCheckPpvImage(true, message)

                        toLastMessage()

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            } else {

                showMatch(message)
            }
        }


    }

    override fun initData() {

        targetId = intent.getStringExtra(AppConstant.Constant.TARGET_ID) ?: targetId

        text = intent.getStringExtra(AppConstant.Constant.DATA) ?: ""

        isNoticeInto = intent.getBooleanExtra(AppConstant.Constant.IS_NOTICE_INTO, false)

        pushMark = intent.getStringExtra(AppConstant.Constant.PUSH_MARK) ?: pushMark

        isOffline = intent.getBooleanExtra(AppConstant.Constant.IS_OFFLINE, isOffline)

        val pushMapData = mutableMapOf<String, String>()

        pushMapData["touchType"] = "Chat"

        pushMapData["touchValue"] = targetId

        pushData = pushMapData.toJson()

        RIMDispatcher.addListener(mMessageListener)

        mViewModel.getChatBasic(targetId)


        RIMClient.clearMessagesUnread(targetId, onSuccess = {


        })

        setMessageExpansionListener()


    }

    override fun onResume() {
        super.onResume()
        cancelNotification(this)
    }


    override fun initView() {

        withViewBinding {

            ChatHelper.add(this@ChatActivity)

            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

            window.setBackgroundDrawableResource(R.color.appColor)

            root.edgeToEdgeTop()

            ScreenChangeUtil().onScreenChange(root, true) { screenOpen, screenHeight ->

                if (screenOpen) {
                    ivEmoji.visibility = View.VISIBLE
                    viewVisibility(View.GONE, ivInput, llAlbum, emojiRecyclerView)
                    ivEmoji.isSelected = false
                    toLastMessage()
                }

            }

            ivIsVip.visibility = if (UserInfoHold.isVip) View.INVISIBLE else View.VISIBLE

            initRecyclerView()

            initEmojiRecyclerView()

            ivMore.setOnClickListener {

                initReportDialog()
            }

            ivBack.setOnClickListener {

                finish()
            }

            setSendMessage.doAfterTextChanged {

                if (it.toString().isBlank()) {
                    ivSendMessage.setImageResource(R.drawable.iv_un_send_message)
                } else {
                    ivSendMessage.setImageResource(R.drawable.iv_send_message)
                }

            }



            ivInput.setOnClickListener {

                viewVisibility(View.GONE, ivInput, llAlbum)

                ivEmoji.visibility = View.VISIBLE

                setSendMessage.hideSoftInput(this@ChatActivity, true)

            }

            ivEmoji.setOnClickListener {

                ivInput.visibility = View.VISIBLE

                viewVisibility(View.GONE, ivEmoji, llAlbum)

                setSendMessage.hideSoftInput(this@ChatActivity)

                if (ivEmoji.isSelected) {
                    emojiRecyclerView.visibility = View.GONE
                    ivEmoji.isSelected = false
                } else {
                    lifecycleScope.launch {
                        delay(100)
                        emojiRecyclerView.visibility = View.VISIBLE
                        ivEmoji.isSelected = true
                        toLastMessage()
                    }

                }


            }
            ivSendMedia.setOnClickListener {

                viewVisibility(View.GONE, ivInput, emojiRecyclerView)

                llAlbum.visibility = View.VISIBLE

                ivEmoji.visibility = View.VISIBLE

                setSendMessage.hideSoftInput(this@ChatActivity, false)

                toLastMessage()
            }

            clLocalAlbum.click {

                pickImage()
            }
            clPrivateAlbum.click {

                initSendMediaDialog()

            }


            ivSendMessage.click {

                if (UserInfoHold.isLowUse) {

                    mViewModel.getVipInfo(
                        AppConstant.Constant.PAY_VIP,
                        mVipShowValue[0],
                        "PremiumBadge"
                    )

                    return@click
                }

                val message = setSendMessage.text.toString()

                sendLocalTextMessage(message, targetId)

            }

            stv1.click {
                val message = stv1.text.toString()
                sendLocalTextMessage(message, targetId)

            }
            stv2.click {
                val message = stv2.text.toString()
                sendLocalTextMessage(message, targetId)

            }
            stv3.click {

                val message = stv3.text.toString()
                sendLocalTextMessage(message, targetId)
            }

            stvCheck.setOnClickListener {

                checkPpvImage()
            }


            if (UserInfoHold.isReview) {
                clPrivateAlbum.visibility = View.GONE
            } else {
                clPrivateAlbum.visibility = View.VISIBLE
            }
        }


    }

    private fun sendLocalTextMessage(message: String, targetId: String) {

        if (message.isBlank()) {
            return
        }

        mViewBinding.setSendMessage.setText("")

        mSendTextMessageList.clear()

        val mLocalMessageId = "ID${System.currentTimeMillis()}"

        mSendTextMessageList.add("{}".fromJson<ModelMediaData>().apply {
            content = message
            localMessageId = mLocalMessageId
        })

        mCustomMessage = CustomMessage("{}".fromJson<Message>().apply {
            objectName = AppConstant.RIMConstant.RC_TXT_MSG
            uId = mLocalMessageId
        }).apply {

            text = message

            isLocal = true

            sendMessageType = AppConstant.RIMConstant.RC_SEND_TEXT_MSG

            id = mLocalMessageId
        }

        mChatAdapter.add(mCustomMessage)


        toLastMessage()

        mViewModel.sendMsg(
            targetId,
            mSendTextMessageList,
            AppConstant.RIMConstant.RC_SEND_TEXT_MSG
        )
    }


    private fun sendLocalMediaMessage(
        path: Uri?,
    ): CustomMessage? {

        if (null == path) {

            return null
        }

        val mLocalMessageId = "ID${System.currentTimeMillis()}"

        mCustomMessage = CustomMessage("{}".fromJson<Message>().apply {
            objectName = AppConstant.RIMConstant.RC_IMG_MSG
            uId = mLocalMessageId
            senderUserId = UserInfoHold.userId
        }).apply {

            uri = path

            isLocal = true

            sendMessageType = AppConstant.RIMConstant.RC_SEND_PUBLIC_IMAGE_MSG

            id = mLocalMessageId
        }

        mChatAdapter.add(mCustomMessage)

        toLastMessage()

        return mCustomMessage

    }


    override fun initViewModel() {


        mViewModel.mUserRelationData.observe(this) {


            mUserRelationData = it

            mViewModel.getHistoryMessage(targetId)


            friendId = it.friendUser.userId

            withViewBinding {


                tvName.text = it.friendUser.nickname

                sivUser.loadImage(this@ChatActivity, it.friendUser.headPic)

                when (it.friendUser.vipStatus) {

                    -1 -> {
                        ivVip.visibility = View.GONE
                    }

                    0 -> {
                        ivVip.setImageResource(R.drawable.iv_vip)
                        ivVip.visibility = View.VISIBLE
                    }

                    1 -> {
                        ivVip.setImageResource(R.drawable.iv_vip)
                        ivVip.visibility = View.VISIBLE
                    }

                    else -> {
                        ivVip.visibility = View.GONE
                    }

                }


                appToolBar.click { view ->

                    createIntent(UserInfoActivity::class.java)
                        .putExtra(AppConstant.Constant.ID, it.friendUser.userId)
                        .putExtra(
                            AppConstant.Constant.PAGE,
                            "chat"
                        )
                        .startActivity(this@ChatActivity)

                }

                if (isNoticeInto) {
                    val param = mutableMapOf<String, Any?>()
                    param["convo_id"] = targetId
                    param["model_id"] = it.friendUser.userId
                    param["model_name"] = it.friendUser.nickname
                    reportEvent(mMessageNoticeKey[1], param)
                }

                if (isOffline) {
                    val param = mutableMapOf<String, Any?>()
                    param["m_type"] = pushMark
                    param["model_id"] = it.friendUser.userId
                    param["model_name"] = it.friendUser.nickname
                    reportEvent(mMessageNoticeKey[2], param)
                }

            }

        }



        mViewModel.mConversationStatusData.observe(this) {

            if (null == mUserRelationData) return@observe

            withViewBinding {

                val symbolToList = targetId.symbolToList("_")

                val conversationStatusData = it[symbolToList[0]]

                if (null != conversationStatusData) {

                    tvName.append(",${conversationStatusData.age}")

                    if (conversationStatusData.onlineStatus == "Online") {

                        stvAline.visibility = View.VISIBLE
                    } else {
                        stvAline.visibility = View.GONE
                    }
                } else {

                    stvAline.visibility = View.GONE
                }

            }
        }


        mViewModel.mMessageListData.observe(this) {

            val list = it.map { map -> CustomMessage(map) }.toMutableList()

            list.reverse()


            val item = list.findLast { find ->

                val messageExtraData = getMessageExtraData(find.message)

                messageExtraData?.eventCode == AppConstant.RIMConstant.CMD_MATCH_SUCCESS
            }

            item?.let { item ->

                item.mUserRelationData = mUserRelationData

            }

            if (mQuickAdapterHelper.leadingLoadState == LoadState.Loading) {
                if (it.size < AppConstant.Constant.PAGE_SIZE_COUNT) {
                    mQuickAdapterHelper.leadingLoadState = LoadState.NotLoading(true)
                } else {
                    mQuickAdapterHelper.leadingLoadState = LoadState.NotLoading(false)
                }
                mChatAdapter.addAll(0, list)
                mChatAdapter.notifyItemRangeChanged(0, mChatAdapter.itemCount, false)
            } else {
                if (it.size < AppConstant.Constant.PAGE_SIZE_COUNT) {
                    mQuickAdapterHelper.leadingLoadState = LoadState.NotLoading(true)
                } else {
                    mQuickAdapterHelper.leadingLoadState = LoadState.NotLoading(false)
                }

                if (list.size >= 10) {

                    mLinearLayoutManager?.stackFromEnd = true
                } else {

                    mLinearLayoutManager?.stackFromEnd = false
                }

                mChatAdapter.submitList(list)
//                toLastMessage(0)
            }



            if (isFirstSend) {

                mUserRelationData

                if (mChatAdapter.itemCount > 0) {
                    val message = mChatAdapter.items.last().message
                    if (message.isCanIncludeExpansion) {

                        val param = if (null == message.expansion) {
                            HashMap<String, String>().apply {
                                this["red"] = "False"
                                this["isNewConversation"] = "False"
                            }
                        } else {
                            message.expansion.apply {
                                this["red"] = "False"
                                this["isNewConversation"] = "False"
                            } as HashMap<String, String>
                        }


                        RIMClient.updateMessageExpansion(param, message.uId, onSuccess = {
                            message.setExpansion(param)
                        }, onError = {
                        })
                    }
                }

                hasMeSendMessage()
                sendLocalTextMessage(text, targetId)

                getCheckPpvImageCount()

                isFirstSend = false
            }


        }


        lifecycleScope.launch {

            try {

                mViewModel.mSendSingleMessageFlow.collect {


                    when (it[0].sendType) {

                        AppConstant.RIMConstant.RC_SEND_TEXT_MSG -> {
                            sendTextMessage(it[0])
                        }

                        AppConstant.RIMConstant.RC_SEND_PUBLIC_IMAGE_MSG -> {

                            sendPublicPictureMessage(it)
                        }

                        AppConstant.RIMConstant.RC_SEND_PRIVATE_IMAGE_MSG -> {


                            sendPrivateVideoOrImagesMessage(
                                AppConstant.RIMConstant.RC_SEND_PRIVATE_IMAGE_MSG,
                                it[0]
                            )
                        }

                        AppConstant.RIMConstant.RC_SEND_PRIVATE_VIDEO_MSG -> {


                            sendPrivateVideoOrImagesMessage(
                                AppConstant.RIMConstant.RC_SEND_PRIVATE_VIDEO_MSG,
                                it[0]
                            )
                        }

                        AppConstant.RIMConstant.RC_SEND_PRIVATE_IMAGE_S_MSG -> {

                            sendPrivateVideoOrImagesMessages(
                                AppConstant.RIMConstant.RC_SEND_PRIVATE_IMAGE_S_MSG,
                                it
                            )
                        }

                        AppConstant.RIMConstant.RC_SEND_PRIVATE_VIDEO_S_MSG -> {

                            sendPrivateVideoOrImagesMessages(
                                AppConstant.RIMConstant.RC_SEND_PRIVATE_VIDEO_S_MSG,
                                it
                            )
                        }

                    }

                    hasMeSendMessage(true)

                    handTurnMessage(true, it[0].turnOnsGuide)

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }

        lifecycleScope.launch {

            try {
                mViewModel.messageSendStatusFlow.collect {

                    try {

                        val index =
                            mChatAdapter.items.indexOfFirst { index -> index.id == it.localMessageId }

                        if (index == -1) {

                            return@collect
                        }


                        if (it.sendType != AppConstant.RIMConstant.RC_SEND_TEXT_MSG) {

                            mChatAdapter.removeAt(index)

                        } else {

                            val item = mChatAdapter.items[index]

                            item.status = 1

                            when (it.errorCode) {
                                1001 -> {
                                    item.errorReason = "Reached the limit"
                                }

                                1107 -> {
                                    item.errorReason = "Content violation"
                                }
                            }

                            mChatAdapter.notifyItemChanged(index, false)

                        }

                        if (it.errorCode == 1001) {

                            handLimitMessage(true)
                        }


                    } catch (e: Exception) {

                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        mViewModel.requestFailEvent.observe(this) {

            if (it is HttpException && it.code == 1001) {

                KeyboardUtils.hideSoftInput(this)

            }

        }


        mViewModel.mOssData.observe(this) {

            OSSUtil.uploadPicture(it, it.uploadType, it.uploadUri, onSuccess = { path, uploadId ->

                if (!this@ChatActivity.lifecycle.isAlive()) return@uploadPicture

                mSendTextMessageList.clear()

                mSendTextMessageList.add("{}".fromJson<ModelMediaData>().apply {
                    content = path
                    albumUrl = path
                    localMessageId = uploadId.toString()
                })

                it.sendMessageType?.let { sendMessageType ->
                    mViewModel.sendMsg(
                        targetId,
                        mSendTextMessageList,
                        sendMessageType
                    )
                }


            }, onError = {

                if (!this@ChatActivity.lifecycle.isAlive()) return@uploadPicture

                lifecycleScope.launch {
                    showShort("upload error")
                }

            })


        }



        mViewModel.mBlockStatus.observe(this) {

            FlowBus.with(AppConstant.EventConstant.EVENT_BLOCK_USER).tryEmit(targetId)

            finish()
        }




        mViewModel.mVipData.observe(this) {

            mUserRelationData?.let { mUserRelationData ->

                it.targetId = targetId
                it.userId2 = mUserRelationData.friendUser.userId
                it.name2 = mUserRelationData.friendUser.nickname
                it.buyRightCount = buyRightCount
            }

            when (it.type) {
                AppConstant.Constant.PAY_VIP -> {

                    initVipDialog(it)
                }

                else -> {
                    initBuyRightDialog(it)
                }

            }

        }


        mViewModel.mUnlockMediaData.observe(this) { data ->


            val mMessage = data.mMessage

            val timestamps = data.timestamps

            if (null == mMessage || timestamps.isNullOrEmpty()) return@observe

            val ppvUnlockStatus = mMessage.expansion["ppvUnlockStatus"]

            val param = HashMap<String, String>().apply {
                this["isLocked"] = "False"
                this["unlockTimestamp"] = "${System.currentTimeMillis()}"
                this["isDestroy"] = "False"
                this["ppPrice"] = data.ppPrice ?: "0.0"
                this["pvPrice"] = data.pvPrice ?: "0.0"
            }

            if (null != ppvUnlockStatus) {

                val mCustomLockMessageData = ppvUnlockStatus.formatListJson<CustomLockMessageData>()

                timestamps.forEachIndexed { index, lng ->

                    val lockMessageData = mCustomLockMessageData[index]

                    if (lockMessageData.unlockTimestamp == 0L) {

                        lockMessageData.isLocked = "False"

                        lockMessageData.isDestroy = "False"

                        lockMessageData.unlockTimestamp = System.currentTimeMillis()
                    }
                }

                param["ppvUnlockStatus"] = mCustomLockMessageData.toJson()

                if (mCustomLockMessageData.all { it.isLocked == "False" }) {
                    addOrReduceCheckPpvImage(false)
                }

            } else {

                addOrReduceCheckPpvImage(false)
            }



            mMessage.let { message ->

                RIMClient.updateMessageExpansion(param, message.uId, onSuccess = {
                    lifecycleScope.launch(Dispatchers.Main) {
                        message.setExpansion(param)

                        data.position?.let {

                            mChatAdapter.notifyItemChanged(it, false)

                        }

                    }
                })
            }

        }


        mViewModel.mUnlockAlbumStatus.observe(this) { data ->

            if (data.position != -1) {
                mChatAdapter.notifyItemChanged(data.position, false)
            }
        }


        FlowBus.with(AppConstant.EventConstant.EVENT_REFRESH_CARD_LIST).observe(this) {

            handLimitMessage(false)

            mViewBinding.ivIsVip.visibility = View.INVISIBLE
        }

    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initRecyclerView() {

        mChatAdapter = ChatAdapter(lifecycleScope)

        mQuickAdapterHelper = QuickAdapterHelper.Builder(mChatAdapter)
            .setLeadingLoadStateAdapter(TopLoadAdapter().setOnLeadingListener(object :
                OnLeadingListener {
                override fun onLoad() {

                    mViewModel.getHistoryMessage(targetId)

                }

                override fun isAllowLoading(): Boolean {
                    return true
                }


            })).build()

        mQuickAdapterHelper.leadingLoadStateAdapter?.preloadSize = -1

        withViewBinding {

            mLinearLayoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
                reverseLayout = false
            }
            chatRecyclerView.layoutManager = mLinearLayoutManager

            chatRecyclerView.adapter = mQuickAdapterHelper.adapter

            VideoPlayerUtil.init(this@ChatActivity)

            chatRecyclerView.addOnScrollListener(object :
                RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    mQuickAdapterHelper.leadingLoadStateAdapter?.preloadSize = 0

                    scrollToPositionNotice()

                    autoPlayVideo(recyclerView, newState)


                }

            })


            chatRecyclerView.setOnTouchListener { v, event ->
                try {
                    setSendMessage.hideSoftInput(this@ChatActivity)
                    ivEmoji.visibility = View.VISIBLE
                    viewVisibility(View.GONE, ivInput, llAlbum, emojiRecyclerView)
                    ivEmoji.isSelected = false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return@setOnTouchListener false
            }
        }

        mChatAdapter.addOnItemChildLongClickListener(R.id.tv_message) { adapter, view, pisition ->

            try {


                val item =
                    mChatAdapter.getItem(pisition) ?: return@addOnItemChildLongClickListener false


                val messageExtraData =
                    getMessageExtraData(item.message)
                        ?: return@addOnItemChildLongClickListener false


                XPopup.Builder(this)
                    .atView(view)
                    .offsetY(10)
                    .popupPosition(PopupPosition.Top)
                    .isViewMode(true)
                    .hasStatusBarShadow(false)
                    .hasShadowBg(false)
                    .popupAnimation(PopupAnimation.NoAnimation)
                    .asCustom(
                        TextOperationDialog(
                            this,
                            item.message.senderUserId == UserInfoHold.userId
                        ).apply {

                            onCopy = {

                                try {

                                    if (item.message.content is TextMessage){

                                        val textMessage = item.message.content as TextMessage

                                        copyContent(getString(R.string.app_name), textMessage.content)
                                    }

                                }catch (e: Exception){
                                    e.printStackTrace()
                                }

                            }

                            onDelete = {

                                RIMClient.deleteMessage(arrayOf(item.message), onSuccess = {

                                    mChatAdapter.remove(item)
                                })

                            }

                            onReport = {

                                createIntent(ReportActivity::class.java)
                                    .putExtra(AppConstant.Constant.ID, messageExtraData.userId2)
                                    .startActivity(this@ChatActivity)

                            }
                        })
                    .show()

            } catch (e: Exception) {

                e.printStackTrace()
            }

            return@addOnItemChildLongClickListener false
        }


        mChatAdapter.addOnDebouncedChildClick(
            R.id.iv_send_text_message_error,
            1000
        ) { adapter, view, pisition ->

            try {


                val item = mChatAdapter.getItem(pisition) ?: return@addOnDebouncedChildClick

                item.status = 0

                mChatAdapter.notifyItemChanged(pisition, false)

                mSendTextMessageList.clear()

                mSendTextMessageList.add("{}".fromJson<ModelMediaData>().apply {
                    content = item.text
                    localMessageId = item.id
                })


                toLastMessage()

                item.sendMessageType?.let { sendMessageType ->

                    mViewModel.sendMsg(
                        targetId,
                        mSendTextMessageList,
                        sendMessageType
                    )

                }

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }

//
//
        mChatAdapter.addOnDebouncedChildClick(
            R.id.iv_avatar,
            1000
        ) { adapter, view, pisition ->

            try {


                mChatAdapter.getItem(pisition) ?: return@addOnDebouncedChildClick

                if (null == mUserRelationData) return@addOnDebouncedChildClick

                createIntent(UserInfoActivity::class.java)
                    .putExtra(AppConstant.Constant.ID, friendId)
                    .putExtra(
                        AppConstant.Constant.PAGE,
                        "chat"
                    )
                    .startActivity(this)


            } catch (e: Exception) {

                e.printStackTrace()
            }
        }


        mChatAdapter.addOnDebouncedChildClick(
            R.id.stv_image,
            1000
        ) { adapter, view, position ->

            try {

                val mMessage = mChatAdapter.getItem(position) ?: return@addOnDebouncedChildClick

                val item = mMessage.message

                if (null != item.expansion && item.expansion["isDestroy"] == "True") {

                    return@addOnDebouncedChildClick
                }

                val messageExtraData = getMessageExtraData(item) ?: return@addOnDebouncedChildClick

                if (null != item.expansion && item.expansion["isLocked"] != "False" && item.senderUserId != UserInfoHold.userId) {

                    if (messageExtraData.msgId.isNullOrEmpty() || messageExtraData.albumId.isNullOrEmpty()) return@addOnDebouncedChildClick

                    buyRightCount = 1

                    mViewModel.unlockV2(
                        item,
                        item.objectName == AppConstant.RIMConstant.RC_IMG_MSG,
                        position,
                        item.targetId,
                        messageExtraData.albumId!!,
                        messageExtraData.msgId!!,
                    )

                } else {

                    val data = mutableListOf<PreViewMediaData>()

                    var url = ""

                    var cover = ""

                    var time = 0L

                    var status = GridImageView.LOCK_STATUS

                    if (messageExtraData.isPrivate == "True") {

                        val unlockTimestamp: Long =
                            item.expansion["unlockTimestamp"]?.toLong() ?: 0L

                        time =
                            (unlockTimestamp + AppConstant.Constant.MEDIA_ENABLE_TIME - System.currentTimeMillis()) / 1000


                        status =
                            if (item.senderUserId == UserInfoHold.userId && time <= 0) GridImageView.LOCK_STATUS else GridImageView.NORMAL_STATUS


                    }

                    if (item.objectName == AppConstant.RIMConstant.RC_IMG_MSG) {

                        val imageMessage = item.content as ImageMessage

                        if (null != imageMessage.remoteUri) {

                            url = imageMessage.remoteUri.toString()

                        }

                    } else if (item.objectName == AppConstant.RIMConstant.RC_IMG_VIDEO) {

                        val mVideoMessage = item.content as VideoMessage

                        if (!mVideoMessage.content.isNullOrEmpty()) {
                            val mModelMediaData = mVideoMessage.content.fromJson<ModelMediaData>()

                            if (null != mModelMediaData.url) {

                                url = mModelMediaData.url.toString()

                                cover = mModelMediaData.cover.toString()

                            }
                        }
                    }

                    data.add(
                        PreViewMediaData(
                            "ID_${System.currentTimeMillis()}",
                            url,
                            cover,
                            time.toInt(),
                            status
                        )
                    )
                    if (data.isNotEmpty()) {

                        showPreviewMediaDialog(
                            data,
                            0,
                            item.senderUserId == UserInfoHold.userId,
                            messageExtraData.isPrivate == "True"
                        )

                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        mChatAdapter.addOnDebouncedChildClick(
            R.id.left_play_view,
            1000
        ) { adapter, view, position ->

            try {

                val mMessage = mChatAdapter.getItem(position) ?: return@addOnDebouncedChildClick

                val item = mMessage.message

                if (null != item.expansion && item.expansion["isDestroy"] == "True") {

                    return@addOnDebouncedChildClick
                }

                val messageExtraData = getMessageExtraData(item) ?: return@addOnDebouncedChildClick

                if (null != item.expansion && item.expansion["isLocked"] != "False" && item.senderUserId != UserInfoHold.userId) {

                    if (messageExtraData.msgId.isNullOrEmpty() || messageExtraData.albumId.isNullOrEmpty()) return@addOnDebouncedChildClick

                    buyRightCount = 1

                    mViewModel.unlockV2(
                        item,
                        item.objectName == AppConstant.RIMConstant.RC_IMG_MSG,
                        position,
                        item.targetId,
                        messageExtraData.albumId!!,
                        messageExtraData.msgId!!,
                    )

                } else {

                    val data = mutableListOf<PreViewMediaData>()

                    var url = ""

                    var cover = ""

                    var time = 0L

                    var status = GridImageView.LOCK_STATUS

                    if (messageExtraData.isPrivate == "True") {

                        val unlockTimestamp: Long =
                            item.expansion["unlockTimestamp"]?.toLong() ?: 0L

                        time =
                            (unlockTimestamp + AppConstant.Constant.MEDIA_ENABLE_TIME - System.currentTimeMillis()) / 1000


                        status =
                            if (item.senderUserId == UserInfoHold.userId && time <= 0) GridImageView.LOCK_STATUS else GridImageView.NORMAL_STATUS


                    }

                    if (item.objectName == AppConstant.RIMConstant.RC_IMG_MSG) {

                        val imageMessage = item.content as ImageMessage

                        if (null != imageMessage.remoteUri) {

                            url = imageMessage.remoteUri.toString()

                        }

                    } else if (item.objectName == AppConstant.RIMConstant.RC_IMG_VIDEO) {

                        val mVideoMessage = item.content as VideoMessage

                        if (!mVideoMessage.content.isNullOrEmpty()) {
                            val mModelMediaData = mVideoMessage.content.fromJson<ModelMediaData>()

                            if (null != mModelMediaData.url) {

                                url = mModelMediaData.url.toString()

                                cover = mModelMediaData.cover.toString()

                            }
                        }
                    }

                    data.add(
                        PreViewMediaData(
                            "ID_${System.currentTimeMillis()}",
                            url,
                            cover,
                            time.toInt(),
                            status
                        )
                    )
                    if (data.isNotEmpty()) {

                        showPreviewMediaDialog(
                            data,
                            0,
                            item.senderUserId == UserInfoHold.userId,
                            messageExtraData.isPrivate == "True"
                        )

                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }







        mChatAdapter.onGridItemClick = { list, position, parentPosition ->


            try {

                val mMessage = mChatAdapter.getItem(parentPosition)

                mMessage?.let {

                    val item = it.message

                    val mPPVMessage = item.content as PPVMessage

                    val messageExtraData = getMessageExtraData(item)

                    when (list[position].status) {

                        GridImageView.LOCK_STATUS -> {

                            if (item.senderUserId == UserInfoHold.userId) {

                                val data =
                                    list.filter { filter -> it.status != GridImageView.DESTROY_STATUS }
                                        .toMutableList()

                                val realPosition =
                                    data.indexOfLast { index -> index.id == list[position].id }
                                        .coerceAtLeast(0)

                                showPreviewMediaDialog(data, realPosition, true)

                            } else {
                                if (null == messageExtraData || messageExtraData.msgId.isNullOrEmpty() || messageExtraData.albumIds.isNullOrEmpty()) return@let

                                val data =
                                    list.filter { filter -> it.status != GridImageView.LOCK_STATUS }
                                        .toMutableList()

                                buyRightCount = data.size

                                mViewModel.unlockV2(
                                    item,
                                    mPPVMessage.type == AppConstant.RIMConstant.RC_SEND_PRIVATE_IMAGE_S_MSG,
                                    parentPosition,
                                    item.targetId,
                                    "",
                                    messageExtraData.msgId!!,
                                    messageExtraData.albumIds!!
                                )
                            }

                        }

                        GridImageView.NORMAL_STATUS -> {

                            if (item.senderUserId == UserInfoHold.userId) {

                                val data =
                                    list.filter { filter -> filter.status != GridImageView.DESTROY_STATUS }
                                        .toMutableList()

                                val realPosition =
                                    data.indexOfLast { index -> index.id == list[position].id }
                                        .coerceAtLeast(0)

                                showPreviewMediaDialog(data, realPosition, true)
                            } else {
                                val data =
                                    list.filter { filter -> filter.status == GridImageView.NORMAL_STATUS }
                                        .toMutableList()

                                val realPosition =
                                    data.indexOfLast { index -> index.id == list[position].id }
                                        .coerceAtLeast(0)

                                showPreviewMediaDialog(data, realPosition, false)
                            }


                        }

                    }


                }

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }


        mChatAdapter.addOnDebouncedChildClick(
            R.id.siv_photo,
            1000
        ) { adapter, view, position ->

            try {


                if (!UserInfoHold.isVip) {

                    initAlbumNoticeDialog(1)

                    val reportParams = mutableMapOf<String, Any?>()

                    reportParams["user_type"] = UserInfoHold.isNewUser

                    reportParams["method"] = mMessageClickValue[4]

                    reportEvent(MESSAGE_CHAT_KEY[0], reportParams)

                    return@addOnDebouncedChildClick
                }


                val item = mChatAdapter.getItem(position) ?: return@addOnDebouncedChildClick

                val mUserRelationData = item.mUserRelationData

                if (null == mUserRelationData || mUserRelationData.unlockAlbums2.isNullOrEmpty()) return@addOnDebouncedChildClick

                val findItem =
                    mUserRelationData.unlockAlbums2.findLast { it.albumType == "SYS_PRIVATE_PIC" }
                        ?: return@addOnDebouncedChildClick

                if (findItem.albumStatus == "Sent") {

                    mViewModel.unlockAlbum(findItem.id, findItem, item, position, true)
                } else {

                    if (findItem.ttl > 0) {
                        showPreviewMediaDialog(
                            mutableListOf(
                                PreViewMediaData(
                                    findItem.albumId,
                                    findItem.albumUrl,
                                    findItem.albumUrl,
                                    findItem.ttl,
                                    GridImageView.NORMAL_STATUS
                                )
                            ), 0, false
                        )
                    }

                }


            } catch (e: Exception) {

                e.printStackTrace()
            }
        }


        mChatAdapter.addOnDebouncedChildClick(
            R.id.system_play_view,
            1000
        ) { adapter, view, position ->

            try {


                if (!UserInfoHold.isVip) {

                    initAlbumNoticeDialog(2)

                    val reportParams = mutableMapOf<String, Any?>()

                    reportParams["user_type"] = UserInfoHold.isNewUser

                    reportParams["method"] = mMessageClickValue[4]

                    reportEvent(MESSAGE_CHAT_KEY[1], reportParams)

                    return@addOnDebouncedChildClick
                }

                val item = mChatAdapter.getItem(position) ?: return@addOnDebouncedChildClick

                val mUserRelationData = item.mUserRelationData

                if (null == mUserRelationData || mUserRelationData.unlockAlbums2.isNullOrEmpty()) return@addOnDebouncedChildClick

                val findItem =
                    mUserRelationData.unlockAlbums2.findLast { it.albumType == "SYS_PRIVATE_VIDEO" }
                        ?: return@addOnDebouncedChildClick


                if (findItem.albumStatus == "Sent") {

                    mViewModel.unlockAlbum(findItem.id, findItem, item, position, false)


                } else {

                    if (findItem.ttl > 0) {
                        showPreviewMediaDialog(
                            mutableListOf(
                                PreViewMediaData(
                                    findItem.albumId,
                                    findItem.albumUrl,
                                    findItem.albumUrl,
                                    findItem.ttl,
                                    GridImageView.NORMAL_STATUS
                                )
                            ), 0, false
                        )
                    }

                }

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }


    }

    private fun initEmojiRecyclerView() {

        withViewBinding {

            mEmojiAdapter =
                object : BaseRecyclerAdapter<String, ItemEmojiBinding>(ItemEmojiBinding::inflate) {
                    override fun convert(
                        holder: BaseRecyclerViewHolder<ItemEmojiBinding>,
                        itemView: ItemEmojiBinding,
                        item: String,
                        position: Int
                    ) {
                        itemView.tvEmoji.text = Html.fromHtml(item, Html.FROM_HTML_MODE_LEGACY)
                    }

                }

            emojiRecyclerView.adapter = mEmojiAdapter

            emojiRecyclerView.layoutManager = GridLayoutManager(this@ChatActivity, 8)


            ResourceUtils.readAssets2String("face_emoji.json").formatListJson<String>().apply {

                mEmojiAdapter.submitList(this)
            }


            mEmojiAdapter.setOnItemClickListener { _, _, position ->

                try {
                    val emojiItem = mEmojiAdapter.getItem(position) ?: return@setOnItemClickListener
                    val editText = setSendMessage
                    val currentText = editText.text ?: return@setOnItemClickListener
                    val selectionStart = editText.selectionStart.coerceAtLeast(0)
                    val spannedInserted = Html.fromHtml(emojiItem, Html.FROM_HTML_MODE_LEGACY)
                    val newText = SpannableStringBuilder(currentText).apply {
                        insert(selectionStart, spannedInserted)
                    }
                    editText.setText(newText)
                    editText.setSelection(newText.length)

                } catch (e: Exception) {
                    e.printStackTrace()
                }


            }

        }

    }

    private fun buildMessage(
        messageContent: MessageContent,
        mPushTitle: String = PUSH_TITLE
    ): Message? {

        if (null == mUserRelationData) return null

        val message = Message.obtain(targetId, conversationType, messageContent)
        message.messagePushConfig = MessagePushConfig.Builder()
            .setPushTitle(mPushTitle)
            .setPushContent(PUSH_CONTENT)
            .setPushData(pushData)
            .build()

        mUserRelationData?.let {
            messageContent.extra = CustomMessageExtraData().apply {

                name1 = it.user.nickname ?: ""
                headPic1 = it.user.headPic ?: ""
                userId1 = it.user.userId ?: ""
                name2 = it.friendUser.nickname ?: ""
                headPic2 = it.friendUser.headPic ?: ""
                userId2 = it.friendUser.userId ?: ""
                source = it.matchSource ?: ""
                tzId1 = TimeZone.getDefault().id

            }.toJson()
        }

        message.isCanIncludeExpansion = true

        message.setExpansion(HashMap<String, String>().apply {
            this["isLocked"] = "True"
            this["unlockTimestamp"] = "0"
            this["isDestroy"] = "False"
            this["isNewConversation"] = "False"
        })

        return message
    }


    private fun sendTextMessage(mModelMediaData: ModelMediaData) {

        val messageContent = TextMessage.obtain(mModelMediaData.content)

        val message = buildMessage(messageContent) ?: return


        RIMClient.sendMessage(message, pushData, onSuccess = {

            try {


                RongCoreClient.getInstance().sendReadReceiptRequest(message, null)

                val index =
                    mChatAdapter.items.indexOfFirst { it.id == mModelMediaData.localMessageId }

                if (index != -1) {

                    mChatAdapter[index] = CustomMessage(message)
                }

                toLastMessage()

            } catch (e: Exception) {

                e.printStackTrace()
            }
        })

    }


    private fun sendPublicPictureMessage(data: MutableList<ModelMediaData>) {


        val item = data[0]

        if (null == item.albumUrl) return

        val parse = item.albumUrl?.toUri()

        val messageContent = ImageMessage.obtain(parse).apply {

            remoteUri = parse
        }

        val message = buildMessage(messageContent) ?: return


        RIMClient.sendMessage(message, pushData, onSuccess = {

            try {


                RongCoreClient.getInstance().sendReadReceiptRequest(message, null)

                val index =
                    mChatAdapter.items.indexOfFirst { it.id == item.localMessageId }

                if (index != -1) {

                    val currentItem = mChatAdapter.getItem(index) ?: return@sendMessage

                    mChatAdapter[index] = CustomMessage(message).apply {

                        uri = currentItem.uri
                    }
                }

                toLastMessage()

            } catch (e: Exception) {

                e.printStackTrace()
            }
        })

    }


    private fun sendPrivateVideoOrImagesMessage(type: String, item: ModelMediaData) {

        if (null == item.albumUrl) return

        val messageContent = if (type == AppConstant.RIMConstant.RC_SEND_PRIVATE_VIDEO_MSG) {


            val modelMediaData = "{}".fromJson<ModelMediaData>().apply {

                url = item.albumUrl
                cover = item.videoCover
                duration = item.videoSeconds
            }

            val messageContent = VideoMessage.obtain(modelMediaData.toJson())

            messageContent

        } else {


            val parse = item.albumUrl?.toUri()

            val messageContent = ImageMessage.obtain(parse).apply {

                remoteUri = parse
            }

            messageContent
        }


        val message = buildMessage(
            messageContent,
            if (type == AppConstant.RIMConstant.RC_SEND_PRIVATE_VIDEO_MSG) "[Video]" else "[Photo]"
        ) ?: return


        messageContent.extra = messageContent.extra.fromJson<CustomMessageExtraData>().apply {
            isPrivate = "True"
            albumId = item.albumId
            msgId = item.msgId
        }.toJson()




        RIMClient.sendMessage(message, pushData, onSuccess = {

            lifecycleScope.launch(Dispatchers.Main) {

                try {

                    RongCoreClient.getInstance().sendReadReceiptRequest(message, null)

                    mCustomMessage = CustomMessage(message)

                    mChatAdapter.add(mCustomMessage)


                    toLastMessage()

                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }
        })

    }


    private fun sendPrivateVideoOrImagesMessages(type: String, item: MutableList<ModelMediaData>) {

        if (item.isEmpty()) {

            return
        }

        val resultList = mutableListOf<ModelMediaData>()

        val mCustomLockMessageData = mutableListOf<CustomLockMessageData>()

        item.forEach {

            val modelMediaData = "{}".fromJson<ModelMediaData>().apply {

                albumUrl = it.albumUrl
                albumId = it.albumId
                cover = it.videoCover
                duration = it.videoSeconds
            }

            mCustomLockMessageData.add(CustomLockMessageData())

            resultList.add(modelMediaData)
        }

        val mAlbumIds = item.map { map ->
            map.albumId!!
        }.toMutableList().formatWithSymbol(",")


        val messageContent = PPVMessage.obtain(type, resultList.toJson())

        val message = buildMessage(
            messageContent,
            if (type == AppConstant.RIMConstant.RC_SEND_PRIVATE_IMAGE_S_MSG) "[Photo]" else "[Video]"
        ) ?: return


        messageContent.extra = messageContent.extra.fromJson<CustomMessageExtraData>().apply {
            isPrivate = "True"
            albumIds = mAlbumIds
            msgId = item[0].msgId
        }.toJson()


        message.expansion["ppvUnlockStatus"] = mCustomLockMessageData.toJson()

        RIMClient.sendMessage(message, pushData, onSuccess = {

            lifecycleScope.launch(Dispatchers.Main) {

                try {

                    RongCoreClient.getInstance().sendReadReceiptRequest(message, null)

                    mCustomMessage = CustomMessage(message)

                    mChatAdapter.add(mCustomMessage)

                    toLastMessage()

                    Log.i(TAG, "sendPrivateVideoOrImagesMessages: ${message.toJson()}")

                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }
        })

    }


    private fun setMessageExpansionListener() {

        messageExpansionListener =
            RIMClient.setMessageExpansionListener(onMessageExpansionUpdate = {

                lifecycleScope.launch(Dispatchers.Main) {
                    try {
                        val index = mChatAdapter.items.indexOfFirst { item ->
                            item.message.uId == it.uId
                        }

                        if (index != -1) {

                            mChatAdapter.getItem(index)
                                ?.message?.setExpansion(it.expansion as HashMap<String, String>?)

                            mChatAdapter.notifyItemChanged(index, false)


                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            })

    }


    private fun initReportDialog() {

        ChatMoreDialog().apply {

            initView = { dialog, mViewBinding ->

                onConfirm = {

                    friendId?.let {
                        mViewModel.blockUser(it)
                    }

                }

                mViewBinding.tvRetort.click {

                    if (friendId.isNullOrEmpty()) {

                        return@click
                    }

                    createIntent(ReportActivity::class.java)
                        .putExtra(AppConstant.Constant.ID, friendId)
                        .startActivity(this@ChatActivity)

                    dialog.dismissAllowingStateLoss()
                }

            }

        }.show(supportFragmentManager)
    }


    private fun onPhotoPicked(uri: Uri?): Uri? {

        if (null == uri) return null

        if (!isValidMedia(this, uri, true)) {

            showShort("too large, cannot send")

            return null
        }

        val sendMediaMessage = sendLocalMediaMessage(uri) ?: return null

        mViewModel.ossAuth(
            sendMediaMessage.id,
            OSSUtil.PICTURE,
            sendMediaMessage.uri,
            sendMediaMessage.sendMessageType
        )
        if (this@ChatActivity.lifecycle.isAlive()) {
            mViewBinding.llAlbum.visibility = View.GONE
        }

        return uri
    }


    private fun pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            pickLegacy.launch(Intent.createChooser(intent, "Select Picture"))
        }
    }


    private fun initVipDialog(it: VipData) {

        if (null != mBuyVipDialog && mBuyVipDialog?.isVisible == true) {

            mBuyVipDialog?.resetData(it)

            return
        }

        mBuyVipDialog = BuyVipDialog.newInstance(it).apply {

            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    mBuyVipDialog = null
                }
            })
        }

        mBuyVipDialog?.show(supportFragmentManager)

    }

    private fun initBuyRightDialog(it: VipData) {

        if (null != mBuyRightDialog && mBuyRightDialog?.isVisible == true) return

        mBuyRightDialog = BuyRightDialog.newInstance(it).apply {

            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    mBuyRightDialog = null
                }
            })
        }

        mBuyRightDialog?.show(supportFragmentManager)


    }


    private fun showMatch(message: Message?) {


        lifecycleScope.launch {

            try {

                if (null == message || message.isOffline) return@launch

                if (message.objectName == AppConstant.RIMConstant.RC_CMD_MSG) {

                    val messageExtraData = getCmdMessageExtraData(message) ?: return@launch

                    if (messageExtraData.eventCode != AppConstant.RIMConstant.CMD_NEW_VISITOR) {
                        FloatingWindowUtil.showFloatMessage(this@ChatActivity, message)

                    }

                } else {

                    FloatingWindowUtil.showFloatMessage(this@ChatActivity, message)

                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun initAlbumNoticeDialog(reportType: Int) {

        AlbumNoticeDialog().apply {

            onConfirm = {

                mViewModel.getVipInfo(
                    AppConstant.Constant.PAY_VIP,
                    mVipShowValue[11],
                    "PremiumBadge"
                )
            }

            reportEvent(reportType)

        }.show(supportFragmentManager)
    }


    private fun initSendMediaDialog() {

        if (!UserInfoHold.isVip) {

            initAlbumNoticeDialog(3)

            return
        }



        val mSendMediaDialog = SendMediaDialog.newInstance(
            targetId,
            true
        )

        mSendMediaDialog.onSendMessage = { item, sendType ->

            mViewModel.sendMsg(targetId, item, sendType)

            if (this@ChatActivity.lifecycle.isAlive()) {
                mViewBinding.llAlbum.visibility = View.GONE
            }
        }

        mSendMediaDialog.show(supportFragmentManager)

    }


    private fun hasMeSendMessage(isSend: Boolean = false) {

        try {


            if (null == UserInfoHold.userInfo?.organic || UserInfoHold.userInfo?.organic == "True") {

                mViewBinding.scrollView.visibility = View.GONE

                return
            }

            if (isSend) {

                if (mViewBinding.scrollView.isGone) return

                mViewBinding.scrollView.visibility = View.GONE

                return
            }

            val message = mChatAdapter.items.find { it.message.senderUserId == UserInfoHold.userId }

            if (null == message) {
                mViewBinding.scrollView.visibility = View.VISIBLE
            } else {
                mViewBinding.scrollView.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mViewBinding.scrollView.visibility = View.GONE
        }

    }

    private fun handTurnMessage(isAdd: Boolean, turnOnsGuide: String) {

        val item =
            mChatAdapter.items.findLast { it.message.objectName == AppConstant.RIMConstant.RC_TURN_ONS_MSG }

        if (!isAdd) {

            item?.let {
                mChatAdapter.remove(item)
            }

            return
        }

        if (turnOnsGuide == "True") {
            val mCustomMessage = CustomMessage("{}".fromJson<Message>().apply {
                objectName = AppConstant.RIMConstant.RC_TURN_ONS_MSG
            })
            mChatAdapter.add(mCustomMessage)

            mChatAdapter.addOnDebouncedChildClick(
                R.id.cl_turn_ons,
                1000
            ) { adapter, view, position ->

                createIntent(EditUserInfoActivity::class.java)
                    .putExtra(
                        AppConstant.Constant.PAGE,
                        "chat"
                    )
                    .putExtra(AppConstant.Constant.IS_TURN_ONS, true)
                    .startActivity(this@ChatActivity)
            }
            val param = mutableMapOf<String, Any?>()
            param["method"] = "Message"
            reportEvent(MESSAGE_CHAT_KEY[9], param)
        }
    }

    private fun handLimitMessage(isAdd: Boolean) {

        val item =
            mChatAdapter.items.findLast { it.message.objectName == AppConstant.RIMConstant.RC_LIMIT_MESSAGE_MSG }

        if (!isAdd) {

            item?.let {
                mChatAdapter.remove(item)
            }

            return
        }

        if (null != item) {

            return
        }

        val mCustomMessage = CustomMessage("{}".fromJson<Message>().apply {
            objectName = AppConstant.RIMConstant.RC_LIMIT_MESSAGE_MSG
        })
        mChatAdapter.add(mCustomMessage)

        mChatAdapter.addOnDebouncedChildClick(
            R.id.ll_limit_message,
            1000
        ) { adapter, view, position ->

            mViewModel.getVipInfo(AppConstant.Constant.PAY_VIP, mVipShowValue[0], "UnlimitedChat")
        }

    }


    private fun showPreviewMediaDialog(
        data: MutableList<PreViewMediaData>,
        position: Int,
        isMe: Boolean,
        isPrivate: Boolean = true
    ) {
        if (data.isEmpty()) return

        PreviewMediaDialog.newInstance(data, position, isMe, isPrivate)
            .show(supportFragmentManager)
    }


    private fun toLastMessage(
        scrollType: Int = 1,
        delayDuration: Long = 0L,
        isFirst: Boolean = false
    ) {

        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return

        lifecycleScope.launch(Dispatchers.Main) {

            if (mChatAdapter.items.isEmpty()) {
                return@launch
            }

            if (isFirst) {
                delay(500)
            }

            delay(delayDuration)

            val recyclerView = mViewBinding.chatRecyclerView

            recyclerView.post {
                try {
                    val position = mChatAdapter.items.lastIndex
                    if (scrollType == 0) {
                        if (position >= 0) {
                            recyclerView.scrollToPosition(position)
                        }

                    } else {
                        if (position >= 0) {
                            recyclerView.smoothScrollToPosition(position)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }

    }


    private fun addOrReduceCheckPpvImage(isAdd: Boolean, message: Message? = null) {

        if (isAdd) {

            if (null == message) return

            when (message.objectName) {
                AppConstant.RIMConstant.RC_IMG_MSG,
                AppConstant.RIMConstant.RC_IMG_VIDEO -> {

                    val messageExtraData = getMessageExtraData(message)

                    if (messageExtraData?.isPrivate == "True") {
                        countPPv++
                    }
                }

                AppConstant.RIMConstant.RC_PP_VM_MSG -> {
                    countPPv++
                }
            }

        } else {

            countPPv = (countPPv - 1).coerceAtLeast(0)

            currentCountPPv = currentCountPPv.coerceAtMost(countPPv)
        }

        mViewBinding.stvCheck.text = "Check($currentCountPPv/${countPPv})"


        if (countPPv < 3) {

            mViewBinding.clImageCheck.visibility = View.GONE
        } else {
            mViewBinding.clImageCheck.visibility = View.VISIBLE
        }
    }


    private fun getCheckPpvImageCount() {


        val count = mChatAdapter.items.count {

            val message = it.message

            var isResult = false

            if (message.senderUserId != UserInfoHold.userId && (message.objectName == AppConstant.RIMConstant.RC_PP_VM_MSG || message.objectName == AppConstant.RIMConstant.RC_IMG_VIDEO || message.objectName == AppConstant.RIMConstant.RC_IMG_MSG)) {

                if (null != message.expansion) {

                    val ppvUnlockStatus = message.expansion["ppvUnlockStatus"]

                    if (null != ppvUnlockStatus) {

                        val mCustomLockMessageData =
                            ppvUnlockStatus.formatListJson<CustomLockMessageData>()

                        isResult =
                            mCustomLockMessageData.any { it.isLocked == "True" && it.unlockTimestamp == 0L }


                    } else {

                        isResult =
                            message.expansion?.get("isLocked") == "True" && message.expansion?.get("unlockTimestamp") == "0"

                    }
                }


            }

            isResult
//            it.message.senderUserId != UserInfoHold.userId && it.message.expansion?.get("isLocked") == "True"


        }

        Log.i(TAG, "handleCheckPpvImage: $count")

        countPPv = count

        mViewBinding.stvCheck.text = "Check($currentCountPPv/${countPPv})"

        if (countPPv < 3) {

            mViewBinding.clImageCheck.visibility = View.GONE
        } else {
            mViewBinding.clImageCheck.visibility = View.VISIBLE
        }
    }

    private fun checkPpvImage() {

        val index = mChatAdapter.items.findNextIndexLoop(lastPPvIndex) {

            val message = it.message

            var isResult = false

            if (message.senderUserId != UserInfoHold.userId && (message.objectName == AppConstant.RIMConstant.RC_PP_VM_MSG || message.objectName == AppConstant.RIMConstant.RC_IMG_VIDEO || message.objectName == AppConstant.RIMConstant.RC_IMG_MSG)) {

                if (null != message.expansion) {

                    val ppvUnlockStatus = message.expansion["ppvUnlockStatus"]

                    if (null != ppvUnlockStatus) {

                        val mCustomLockMessageData =
                            ppvUnlockStatus.formatListJson<CustomLockMessageData>()

                        isResult =
                            mCustomLockMessageData.any { it.isLocked == "True" && it.unlockTimestamp == 0L }

                    } else {

                        isResult =
                            message.expansion?.get("isLocked") == "True" && message.expansion?.get("unlockTimestamp") == "0"
                    }
                }


            }

            isResult
        }
//        val index = mChatAdapter.items.findNextIndexLoop(lastPPvIndex) {
//
//            it.message.senderUserId != UserInfoHold.userId && it.message.expansion?.get("isLocked") == "True"
//        }

        if (index == -1) return

        if (lastPPvIndex > index) {

            currentCountPPv++
        } else {
            currentCountPPv = 1
        }

        lastPPvIndex = index

        val item = mChatAdapter.getItem(index) ?: return

        val message = item.message

        var url = ""

        when (message.objectName) {

            AppConstant.RIMConstant.RC_IMG_MSG -> {

                val imageMessage = message.content as ImageMessage

                url = imageMessage.remoteUri.toString()

            }

            AppConstant.RIMConstant.RC_IMG_VIDEO -> {

                val mVideoMessage = message.content as VideoMessage

                val messageMediaData = getMessageMediaData(mVideoMessage)

                url = messageMediaData?.cover.toString()

            }

            AppConstant.RIMConstant.RC_PP_VM_MSG -> {

                val mPPVMessage = item.message.content as PPVMessage


                val messagePPVData = getMessagePPVData(mPPVMessage)

                val ppvUnlockStatus = message.expansion["ppvUnlockStatus"]

                if (null != ppvUnlockStatus) {

                    val mCustomLockMessageData =
                        ppvUnlockStatus.formatListJson<CustomLockMessageData>()

                    val index = mCustomLockMessageData.indexOfFirst { it.isLocked == "True" }

                    if (index != -1) {

                        if (!messagePPVData.isNullOrEmpty()) {

                            url = messagePPVData[index].albumUrl.toString()

                        }
                    }

                } else {

                    if (!messagePPVData.isNullOrEmpty()) {

                        url = messagePPVData[0].albumUrl.toString()

                    }

                }
            }
        }

        mViewBinding.sivImage.loadOptionImage(
            mViewBinding.sivImage.context,
            url,
            requestOptions,
            100,
            100
        )

        mViewBinding.stvCheck.text = "Check($currentCountPPv/${countPPv})"

        isCheckScroll = true

        mViewBinding.chatRecyclerView.smoothScrollToPosition(lastPPvIndex)

        scrollToPositionNotice()


        reportEvent(MESSAGE_CHAT_KEY[3], true)

    }

    private fun scrollToPositionNotice() {


        lifecycleScope.launch {

            try {

                delay(150)

                if (!isCheckScroll) {

                    return@launch
                }

                val layoutManager = mViewBinding.chatRecyclerView.layoutManager ?: return@launch

                val itemView = layoutManager.findViewByPosition(lastPPvIndex) ?: return@launch

                if (isCheckScroll && lastPPvIndex <= mChatAdapter.items.lastIndex - 2){
                    val rvCenter = mViewBinding.chatRecyclerView.height / 2
                    val itemCenter = (itemView.top + itemView.bottom) / 2
                    val dy = itemCenter - rvCenter
                    mViewBinding.chatRecyclerView.smoothScrollBy(0, dy)
                }

                itemView.let { itemView ->

                    itemView.animate().cancel()

                    val startColor = "#33ffffff".toColorInt()

                    val endColor = Color.TRANSPARENT

                    ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor).apply {
                        duration = 2000L
                        addUpdateListener { anim ->
                            val newColor = anim.animatedValue as Int
                            itemView.setBackgroundColor(newColor)
                        }
//                    startDelay = 500

                        start()
                    }

                }
                isCheckScroll = false

                this.cancel()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }


    private fun autoPlayVideo(recyclerView: RecyclerView, newState: Int) {

        if (newState == RecyclerView.SCROLL_STATE_IDLE) {


            mCoverImageView?.visibility = View.VISIBLE

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager

            val firstVisiblePos = layoutManager.findFirstCompletelyVisibleItemPosition()

            val lastVisiblePos = layoutManager.findLastCompletelyVisibleItemPosition()

//            if (firstVisiblePos == RecyclerView.NO_POSITION) {
//                VideoPlayerUtil.releaseCurrent()
//                return
//            }


            var mCustomMessage: CustomMessage? = null

            var index = -1



            for (i in firstVisiblePos..lastVisiblePos) {

                Log.i(TAG, "autoPlayVideo: $i")

                mCustomMessage = mChatAdapter.getItem(i) ?: return

                if (mCustomMessage.message.objectName == AppConstant.RIMConstant.RC_NTF_MSG) {

                    index = i

                    break
                } else if (mCustomMessage.message.objectName == AppConstant.RIMConstant.RC_IMG_VIDEO) {

                    if (mCustomMessage.message.expansion["isDestroy"] != "True") {

                        index = i

                        break
                    }
                }


            }

            if (null == mCustomMessage) return

            val firstVisibleView = layoutManager.findViewByPosition(index) ?: return

            val mPlayerView = firstVisibleView.findViewById<PlayerView>(R.id.play_view) ?: return

            if (mCustomMessage.message.objectName == AppConstant.RIMConstant.RC_NTF_MSG) {

                val messageExtraData = getMessageExtraData(mCustomMessage.message) ?: return

                if (messageExtraData.eventCode == AppConstant.RIMConstant.CMD_MATCH_SUCCESS && !UserInfoHold.isOrganic) {

                    val mUserRelationData = mCustomMessage.mUserRelationData

                    if (null == mUserRelationData || mUserRelationData.unlockAlbums2.isNullOrEmpty()) return

                    val findItem =
                        mUserRelationData.unlockAlbums2.findLast { it.albumType == "SYS_PRIVATE_VIDEO" }
                            ?: return

                    if (findItem.ttl <= 0) return

                    mCoverImageView?.visibility = View.VISIBLE

                    mCoverImageView = firstVisibleView.findViewById<ImageView>(R.id.siv_video_cover)

                    val proxy = BaseApplication.mApplication.getProxy()

                    val proxyUrl = proxy.getProxyUrl(getRealUrl(findItem.albumUrl).toString())

                    VideoPlayerUtil.play(proxyUrl, mPlayerView)

                    VideoPlayerUtil.onReady = {

                        mCoverImageView?.visibility = View.GONE
                    }

                    VideoPlayerUtil.setVolume(0f)
                }


            } else if (mCustomMessage.message.objectName == AppConstant.RIMConstant.RC_IMG_VIDEO) {


                val messageExtraData = getMessageExtraData(mCustomMessage.message) ?: return

                if (messageExtraData.isPrivate == "True" && mCustomMessage.message.expansion["isDestroy"] == "True") {

                    return

                }


                val mVideoMessage = mCustomMessage.message.content as VideoMessage

                val messageMediaData = getMessageMediaData(mVideoMessage)


                if (null != messageMediaData) {

                    mCoverImageView?.visibility = View.VISIBLE

                    mCoverImageView = firstVisibleView.findViewById<ImageView>(R.id.stv_image_cover)

                    val url = messageMediaData.url

                    val proxy = BaseApplication.mApplication.getProxy()

                    val proxyUrl = proxy.getProxyUrl(getRealUrl(url).toString())

                    VideoPlayerUtil.play(proxyUrl, mPlayerView)

                    VideoPlayerUtil.setVolume(0f)

                    VideoPlayerUtil.onReady = {

                        mCoverImageView?.visibility = View.GONE
                    }

                }

            } else {

                mCoverImageView?.visibility = View.VISIBLE

                VideoPlayerUtil.releaseCurrent()
            }


        }
    }

    override fun finish() {

        try {

            if (mChatAdapter.items.isNotEmpty()) {

                RIMClient.clearMessagesUnread(targetId, onSuccess = {

                    FlowBus.with(AppConstant.EventConstant.CLEAR_READ_MESSAGE).postValue(targetId)

                })
            } else {

                FlowBus.with(AppConstant.EventConstant.CLEAR_BLACK_READ_MESSAGE)
                    .postValue(targetId)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }


        super.finish()

    }

    override fun onDestroy() {
        try {
            messageExpansionListener = null
            RongIMClient.getInstance().setMessageExpansionListener(null)
            RIMDispatcher.removeListener(mMessageListener)
            VideoPlayerUtil.releaseAll()
//            ChatHelper.remove(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

}