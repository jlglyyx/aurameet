package com.chat.jolt.fragment

import android.Manifest
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.setPadding
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.chat.jolt.R
import com.chat.jolt.activity.ChatActivity
import com.chat.jolt.activity.NoticeMessageActivity
import com.chat.jolt.activity.VisitorActivity
import com.chat.jolt.adapter.ConversationAdapter
import com.chat.jolt.adapter.NewConversationAdapter
import com.chat.jolt.data.CustomConversationData
import com.chat.jolt.data.ModelCardData
import com.chat.jolt.data.ModelUserData
import com.chat.jolt.data.NewConversationData
import com.chat.jolt.data.VipData
import com.chat.jolt.data.WlmData
import com.chat.jolt.databinding.FraConversationBinding
import com.chat.jolt.databinding.ViewLikeTabBinding
import com.chat.jolt.databinding.ViewMessageEmptyBinding
import com.chat.jolt.databinding.ViewNoNetworkBinding
import com.chat.jolt.dialog.BuyVipDialog
import com.chat.jolt.dialog.OpenNoticeDialog
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.helper.getCmdMessageExtraData
import com.chat.jolt.helper.getMessageExtraData
import com.chat.jolt.helper.handleMessageReportEvent
import com.chat.jolt.viewmodel.ChatViewModel
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.observe
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.fragment.BaseFragment
import com.chat.lib_common.im.RIMClient
import com.chat.lib_common.im.RIMDispatcher
import com.chat.lib_common.im.message.PPVMessage
import com.chat.lib_common.im.message.VideoMessage
import com.chat.lib_common.tracking.mMessageEventKey
import com.chat.lib_common.tracking.mPopPopupDialogKey
import com.chat.lib_common.tracking.mVipShowValue
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.tracking.updateSolarEngineUser
import com.chat.lib_common.util.click
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.fromJson
import com.chat.lib_common.util.getCache
import com.chat.lib_common.util.getColor
import com.chat.lib_common.util.hasNotificationPermission
import com.chat.lib_common.util.openNoticePermissionDetail
import com.chat.lib_common.util.refreshLoadListener
import com.chat.lib_common.util.setCache
import com.chat.lib_common.util.showShort
import com.chat.lib_common.util.startActivity
import com.chat.lib_common.util.symbolToList
import com.chat.lib_common.util.toJson
import com.chat.lib_common.widget.ErrorReLoadView
import com.chat.lib_common.widget.SwipeMenuLayout
import com.google.android.material.tabs.TabLayout
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import io.rong.imlib.model.ReceivedProfile
import io.rong.message.ImageMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConversationFragment :
    BaseFragment<FraConversationBinding, ChatViewModel>(FraConversationBinding::inflate) {

    private lateinit var mConversationAdapter: ConversationAdapter

    private val mNewConversationAdapter by lazy { NewConversationAdapter(lifecycle) }

    private val mNewConversationDataList = mutableListOf<NewConversationData>()

    private lateinit var mQuickAdapterHelper: QuickAdapterHelper

    private var maxTop = 4

    private var isCloseNotice = false

    private var titles = arrayOf("ALL", "Love")


    private var isGetNewOnline = false

    private var mBuyVipDialog: BuyVipDialog? = null

    private var emptyImage: ImageView? = null

    private val registerForActivityResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            if (permissions.all { it.value }) {

                withViewBinding {

                    sclNotice.visibility = View.GONE
                }

            } else {
                withViewBinding {

                    sclNotice.visibility = View.VISIBLE
                }

            }

        }


    private var mMessageListener = object : RIMDispatcher.MessageListener {


        override fun onMessageReceiptResponse(
            message: Message,
            type: Conversation.ConversationType,
            targetId: String,
            mReceivedProfile: ReceivedProfile
        ) {

            lifecycleScope.launch(Dispatchers.Main) {

                try {

                    when (message.objectName) {

                        AppConstant.RIMConstant.RC_TXT_MSG -> {

                            if (message.conversationType == Conversation.ConversationType.GROUP) {

                                handleConversationMessage(message)
                            } else {

                                getUnreadCount()
                            }

                        }

                        AppConstant.RIMConstant.RC_IMG_MSG,
                        AppConstant.RIMConstant.RC_IMG_VIDEO,
                        AppConstant.RIMConstant.RC_PP_VM_MSG -> {

                            handleConversationMessage(message)

                        }

                        AppConstant.RIMConstant.RC_NTF_MSG -> {


                            val isOffline = mReceivedProfile.isOffline

                            if (!isOffline) {
                                handleNewConversationMessage(message)
                            }

                        }

                        AppConstant.RIMConstant.RC_CMD_MSG -> {


                            val isOffline = mReceivedProfile.isOffline

                            if (!isOffline) {
                                handleCmdMessage(message)
                            }

                        }

                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            handleMessageReportEvent(message)

            Log.i(TAG, "onMessageReceiptResponse: ----> ${message.toJson()}")
        }

    }


    override fun onResume() {
        super.onResume()

        if (!isCloseNotice) {

            val isHasNotice = getCache(AppConstant.Constant.IS_HAS_NOTICE, false)

            if (requireContext().hasNotificationPermission()) {

                withViewBinding {

                    sclNotice.visibility = View.GONE

                    if (!isHasNotice) {
                        updateSolarEngineUser("notification_permissions", true)
                        mViewModel.updateSetting("0")

                        setCache(AppConstant.Constant.IS_HAS_NOTICE, true)
                    }

                }
            } else {
                withViewBinding {

                    sclNotice.visibility = View.VISIBLE

                    if (isHasNotice) {
                        updateSolarEngineUser("notification_permissions", false)
                        mViewModel.updateSetting("1")

                        setCache(AppConstant.Constant.IS_HAS_NOTICE, false)
                    }

                }
            }
        }

        initNoticeDialog()
    }


    override fun initView() {


        withViewBinding {

            mSwipeRefreshLayout.setOnRefreshListener {

                onRefresh()
            }

            stvEnable.click {

                val params = mutableMapOf<String, Any?>()
                params["button_name"] = "click_enable"
                reportEvent(mPopPopupDialogKey[7], params)

                requestNoticePermission()

            }

            ivX.click {

                val params = mutableMapOf<String, Any?>()
                params["button_name"] = "click_close"
                reportEvent(mPopPopupDialogKey[7], params)

                isCloseNotice = true

                sclNotice.visibility = View.GONE

            }

            ivSystemNotice.click {

                createIntent(NoticeMessageActivity::class.java).startActivity(requireActivity())
            }


        }


        initTabLayout()


        initRecyclerView()
        initNewConversationRecyclerView()
        RIMDispatcher.addListener(mMessageListener)
    }

    override fun initData() {


        if (requireContext().hasNotificationPermission()) {

            mViewModel.updateSetting("0")

        } else {
            mViewModel.updateSetting("1")
        }



        onRefresh()
    }


    override fun initViewModel() {

        mViewModel.mConversationListData.observe(this) {

            val list = it.map { map -> CustomConversationData(map) }.toMutableList()

            withViewBinding {

                refreshLoadDataListener(
                    mSwipeRefreshLayout,
                    mQuickAdapterHelper,
                    errorReLoadView,
                    list
                )
            }

            sortByDescending()

        }

        mViewModel.mLoveConversationListData.observe(this) {

            mConversationAdapter.submitList(null)

            val list = it.map { map -> CustomConversationData(map) }.toMutableList()

            withViewBinding {

                refreshLoadDataListener(
                    mSwipeRefreshLayout,
                    mQuickAdapterHelper,
                    errorReLoadView,
                    list
                )
            }

            sortByDescending()

        }

        mViewModel.mNewConversationListData.observe(this) {

            val list = it.map { map ->

                NewConversationData(map)

            }.toMutableList()


            mNewConversationDataList.addAll(list)

            mViewModel.getWlmList(0)

        }

        mViewModel.mConversationListStatus.observe(this) {

            withViewBinding {

                mSwipeRefreshLayout.isRefreshing = false
            }

        }
        mViewModel.mConversationStatusData.observe(this) {

            withViewBinding {

                mConversationAdapter.items.forEachIndexed { index, item ->

                    val symbolToList = item.mConversation.targetId.symbolToList("_")

                    if (symbolToList.isNotEmpty() && symbolToList.size >= 2) {

                        val data = it[symbolToList[0]]
                        if (null != data) {

                            item.mConversationStatusData = data

                            mConversationAdapter.notifyItemChanged(index, false)

                        }

                    }

                }


                if (isGetNewOnline) {

                    mNewConversationAdapter.items.forEachIndexed { index, item ->

                        val mConversation = item.mConversation

                        if (null != mConversation) {
                            val symbolToList = mConversation.targetId.symbolToList("_")

                            if (symbolToList.isNotEmpty() && symbolToList.size >= 2) {

                                val data = it[symbolToList[0]]
                                if (null != data) {

                                    item.mConversationStatusData = data

                                    mNewConversationAdapter.notifyItemChanged(index, false)

                                }

                            }
                        }


                    }

                    isGetNewOnline = false
                }

            }

        }


        mViewModel.mLoveConversationStatusData.observe(this) {

            mConversationAdapter.items.forEachIndexed { index, item ->

                val symbolToList = item.mConversation.targetId.symbolToList("_")

                if (symbolToList.isNotEmpty() && symbolToList.size >= 2) {

                    val data = it[symbolToList[0]]
                    if (null != data) {

                        item.mConversationStatusData = data

                        mConversationAdapter.notifyItemChanged(index, false)

                    }

                }

            }


        }






        mViewModel.mVisitorData.observe(this) {

            addVisitorConversation(it)


            mViewBinding.newConversationRecyclerView.scrollToPosition(0)

        }

        mViewModel.mWlmData.observe(this) {

            addLikeConversation(it)


            mViewModel.myVisitor()

        }


        mViewModel.mVipData.observe(this) {

            when (it.type) {
                AppConstant.Constant.PAY_VIP -> {
                    initVipDialog(it)
                }

            }

        }



        FlowBus.with(AppConstant.EventConstant.EVENT_BLOCK_USER).observe(this) {

            removeConversation(it.toString())
        }

        FlowBus.with(AppConstant.EventConstant.CLEAR_READ_MESSAGE).observe(this) {

            clearRedNewMessage(it.toString())

            clearRedMessage(it.toString())

            mViewModel.getOnlineStatus(mutableListOf(it.toString()))


        }

        FlowBus.with(AppConstant.EventConstant.EVENT_TO_TOP_MESSAGE).observe(this) {


            withViewBinding {

                recyclerView.smoothScrollToPosition(0)
            }

        }

        FlowBus.with(AppConstant.EventConstant.EVENT_REFRESH_MATCH_MESSAGE_ITEM).observe(this) {

            try {

                if (it !is Message) return@observe

                if (it.targetId.isEmpty()) {

                    return@observe
                }

                val index = mConversationAdapter.items.indexOfFirst { item ->
                    item.mConversation.targetId == it.targetId
                }

                if (index == -1) return@observe

                val item = mConversationAdapter.getItem(index) ?: return@observe

                if (null == item.mConversation.conversationType) return@observe


                RIMClient.getConversationDetail(
                    it.targetId,
                    item.mConversation.conversationType,
                    onSuccess = { mCurrentConversation ->


                        if (null == mCurrentConversation) {
                            return@getConversationDetail
                        }

                        item.mConversation = mCurrentConversation

                        mConversationAdapter.notifyItemChanged(index, false)

                    })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        FlowBus.with(AppConstant.EventConstant.CLEAR_BLACK_READ_MESSAGE)
            .observe(this) {

                try {

                    val targetId = it.toString()

                    if (targetId.isNotEmpty()) {

                        val index = mConversationAdapter.items.indexOfFirst { item ->
                            item.mConversation.targetId == targetId
                        }

                        getCurrentConversation(targetId)?.let { item ->

                            val param = HashMap<String, String>().apply {
                                this["red"] = "False"
                            }

                            item.mConversation.latestExpansion = param

                            item.mConversation.unreadMessageCount = 0

                            mConversationAdapter.notifyItemChanged(index, false)

                            sortByDescending()

                        }

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }


        FlowBus.with(AppConstant.EventConstant.EVENT_GET_UNREAD_NOTICE_COUNT).observe(this) {

            getUnreadCount()
        }

        FlowBus.with(AppConstant.EventConstant.EVENT_REFRESH_CARD_LIST).observe(this) {

            if (mConversationAdapter.itemCount >= 2) {
                mConversationAdapter.notifyItemChanged(0, false)
                mConversationAdapter.notifyItemChanged(1, false)
            }
        }

        FlowBus.with(AppConstant.EventConstant.EVENT_REFRESH_LIKE_AND_VISITOR).observe(this) {

            mNewConversationDataList.clear()

            mViewModel.getNewConversationList()

        }

    }

    private fun initRecyclerView() {


        mConversationAdapter = ConversationAdapter()

        withViewBinding {

            mQuickAdapterHelper =
                mConversationAdapter.refreshLoadListener(mSwipeRefreshLayout, onRefresh = {

                    onRefresh()

                }, onLoad = {

                    onLoadMore()
                })

            recyclerView.adapter = mQuickAdapterHelper.adapter



            errorReLoadView.addEmptyView { viewGroup ->
                ViewMessageEmptyBinding.inflate(
                    LayoutInflater.from(requireContext()),
                    viewGroup,
                    true
                ).apply {


                    emptyImage = ivImage

                }
            }

            errorReLoadView.addNoNetView { viewGroup ->
                ViewNoNetworkBinding.inflate(LayoutInflater.from(requireContext()), viewGroup, true)
                    .apply {

                        stvConfirm.click {

                            onRefresh()
                        }
                    }
            }
        }


        mConversationAdapter.addOnDebouncedChildClick(R.id.cl_container) { adapter, view, position ->

            val item = mConversationAdapter.getItem(position)
            item?.let {

                requireContext().createIntent(ChatActivity::class.java)
                    .putExtra(AppConstant.Constant.TARGET_ID, it.mConversation.targetId)
                    .startActivity(requireActivity())

            }

        }

        mConversationAdapter.addOnDebouncedChildClick(R.id.ll_pin) { adapter, view, position ->

            val item = mConversationAdapter.getItem(position)
            item?.let {

                val isTop = it.mConversation.isTop

                if (!isTop && getCurrentTopCount() >= maxTop) {

                    showShort("Up to $maxTop items can be placed at the top")

                    return@addOnDebouncedChildClick
                }

                RIMClient.setConversationToTop(
                    it.mConversation.targetId,
                    !isTop,
                    it.mConversation.conversationType,
                    onSuccess = {

                        item.mConversation.isTop = !isTop

                        if (isTop) {
                            reportEvent(1, item)

                        } else {
                            reportEvent(0, item)
                        }


                        (view.parent as SwipeMenuLayout).closeMenu()

                        mConversationAdapter.notifyItemChanged(position, false)

                        sortByDescending()
                    })


            }

        }

        mConversationAdapter.addOnDebouncedChildClick(R.id.ll_delete) { adapter, view, position ->

            val item = mConversationAdapter.getItem(position)
            item?.let {


                RIMClient.setConversationToTop(
                    it.mConversation.targetId,
                    false,
                    it.mConversation.conversationType,
                    onSuccess = {
                        RIMClient.removeConversation(item.mConversation.targetId, onSuccess = {

                            reportEvent(2, item)

                            mConversationAdapter.removeAt(position)

                            FlowBus.with(AppConstant.EventConstant.EVENT_GET_UNREAD_COUNT)
                                .postValue(true)
                        })
                    })

            }

        }

    }


    private fun initNewConversationRecyclerView() {

        withViewBinding {

            newConversationRecyclerView.adapter = mNewConversationAdapter
            newConversationRecyclerView.layoutManager = LinearLayoutManager(
                requireContext(),
                RecyclerView.HORIZONTAL, false
            )

            newConversationRecyclerView.itemAnimator = null

            mNewConversationAdapter.setOnDebouncedItemClick { _, _, position ->

                val item =
                    mNewConversationAdapter.getItem(position) ?: return@setOnDebouncedItemClick

                if (null == item.mConversation) {

                    if (!UserInfoHold.isVip) {

                        if (item.name == "Likes") {
                            mViewModel.getVipInfo(
                                AppConstant.Constant.PAY_VIP,
                                mVipShowValue[8],
                                "PremiumBadge"
                            )
                        } else {
                            mViewModel.getVipInfo(
                                AppConstant.Constant.PAY_VIP,
                                mVipShowValue[15],
                                "PremiumBadge"
                            )
                        }

                        return@setOnDebouncedItemClick
                    }

                    if (item.name == "Likes") {

                        FlowBus.with(AppConstant.EventConstant.EVENT_SET_PAGE).postValue(1)
                        FlowBus.with(AppConstant.EventConstant.EVENT_SET_LIKE_PAGE).postValue(0)
                    } else {
                        requireContext().createIntent(VisitorActivity::class.java)
                            .startActivity(requireActivity())
                    }

                } else {

                    requireContext().createIntent(ChatActivity::class.java)
                        .putExtra(AppConstant.Constant.TARGET_ID, item.mConversation!!.targetId)
                        .startActivity(requireActivity())
                }


            }

        }
    }


    fun handleConversationMessage(
        message: Message,
        type: Int = 0
    ) {
        try {

            if (null != message.expansion && message.expansion["isNewConversation"] == "True") {

                return
            }

            val index = mConversationAdapter.items.indexOfFirst { item ->
                item.mConversation.targetId == message.targetId
            }

            if (index != -1) {
                val item = mConversationAdapter.getItem(index)
                item?.let {

                    if (type == 0) {

                        if (message.messageDirection != Message.MessageDirection.SEND) {
                            item.mConversation.unreadMessageCount += 1
                        }
                        item.mConversation.latestMessage = message.content
                        item.mConversation.receivedTime = message.receivedTime
                        item.mConversation.sentTime = message.sentTime
                        item.mConversation.conversationType = message.conversationType

                        if (message.content is ImageMessage || message.content is VideoMessage) {

                            val messageExtraData = getMessageExtraData(message)

                            if (messageExtraData?.isPrivate == "True") {
                                item.mConversationStatusData?.receivedPPV = "True"
                            }
                        } else if (message.content is PPVMessage) {
                            item.mConversationStatusData?.receivedPPV = "True"
                        } else {

                        }

                        mConversationAdapter.notifyItemChanged(index, false)
                    } else {
                        RIMClient.getConversationDetail(
                            message.targetId,
                            message.conversationType,
                            onSuccess = {
                                if (it != null) {
                                    item.mConversation = it
                                    mConversationAdapter.notifyItemChanged(index, false)
                                }
                            })
                    }
                }
            } else {

                if (mViewBinding.tabLayout.selectedTabPosition == 1) {

                    return
                }

                val item =
                    mNewConversationAdapter.items.findLast { find -> find.mConversation?.targetId == message.targetId }

                if (null != item) {
                    mNewConversationAdapter.remove(item)
                }

                val conversation = Conversation()
                conversation.targetId = message.targetId
                conversation.message = message
                conversation.latestMessage = message.content
                conversation.receivedTime = message.receivedTime
                conversation.sentTime = message.sentTime
                conversation.conversationType = message.conversationType
                val customConversationData = CustomConversationData(conversation)
                if (type == 0) {
                    if (message.messageDirection != Message.MessageDirection.SEND) {
                        conversation.unreadMessageCount = 1
                    } else {
                        conversation.unreadMessageCount = 0
                    }
                } else {
                    conversation.unreadMessageCount = 0
                }

                mConversationAdapter.add(0, customConversationData)

                mViewModel.getOnlineStatus(mutableListOf(message.targetId))

                mConversationAdapter.notifyItemRangeChanged(
                    0,
                    mConversationAdapter.itemCount,
                    false
                )
            }
            sortByDescending()
        } catch (e: Exception) {
            Log.i(TAG, "handleModelListMessage: ${e.message}")
            e.printStackTrace()
        }

    }

    fun handleNewConversationMessage(
        message: Message,
    ) {


        RIMClient.updateMessageExpansion(
            message.expansion.apply {
                this["isNewConversation"] = "True"
            } as HashMap<String, String>, message.uId,
            onSuccess = {
                val conversation = Conversation()
                conversation.targetId = message.targetId
                conversation.message = message
                conversation.latestMessage = message.content
                conversation.receivedTime = message.receivedTime
                conversation.sentTime = message.sentTime
                conversation.conversationType = message.conversationType

                if (mNewConversationAdapter.itemCount < 2) {

                    mNewConversationAdapter.add(0, NewConversationData(conversation))
                } else {

                    mNewConversationAdapter.add(2, NewConversationData(conversation))
                }


            }
        )
    }

    fun handleCmdMessage(
        message: Message,
    ) {


        val messageExtraData = getCmdMessageExtraData(message) ?: return

        val data = messageExtraData.data?:return

        val mModelUserData = "{}".fromJson<ModelUserData>()

        mModelUserData.headPic = data.headPic2?:""

        when (messageExtraData.eventCode) {

            AppConstant.RIMConstant.CMD_NEW_WHO_LIKE_ME ->{

                val item = mNewConversationDataList.findLast { it.name == "Likes" }

                if (null == item){

                    mNewConversationDataList.add(0, NewConversationData(null).apply {

                        name = "Likes"

                        count = 1

                        list = mutableListOf(mModelUserData)

                    })

                }else{

                    item.count = item.count+1

                    item.list.add(mModelUserData)

                }

                mNewConversationAdapter.notifyItemRangeChanged(0,mNewConversationAdapter.itemCount,false)
            }

            AppConstant.RIMConstant.CMD_NEW_VISITOR ->{


                val item = mNewConversationDataList.findLast { it.name == "Visitor" }

                if (null == item){

                    mNewConversationDataList.add(
                        if (null == mNewConversationDataList.findLast { it.name == "Likes" }) 0 else 1,
                        NewConversationData(null).apply {

                            name = "Visitor"

                            count = 1

                            list = mutableListOf(mModelUserData)
                        })

                }else{

                    item.count = item.count+1

                    item.list.add(mModelUserData)

                }

                mNewConversationAdapter.notifyItemRangeChanged(0,mNewConversationAdapter.itemCount,false)
            }


        }



    }


    private fun getCurrentTopCount(): Int {

        val count = mConversationAdapter.items.count { it.mConversation.isTop }

        return count

    }

    private fun sortByDescending(): MutableList<CustomConversationData> {

        try {
            val mSortedByDescendingData = mConversationAdapter.items
                .sortedWith(
                    compareByDescending<CustomConversationData> { it.mConversation.isTop }
                        .thenByDescending { it.mConversation.sentTime }
                )
                .toMutableList()

            mConversationAdapter.submitList(mSortedByDescendingData)

            mViewBinding.errorReLoadView.showSuccessView(mConversationAdapter.items)

            return mSortedByDescendingData
        }catch (e: Exception){

            return mutableListOf()
        }

    }


    private fun clearRedNewMessage(targetId: String) {

        try {


            if (targetId.isEmpty()) {

                return
            }

            val index = mNewConversationAdapter.items.indexOfFirst { item ->
                item.mConversation?.targetId == targetId
            }

            if (index == -1) return

            val item = mNewConversationAdapter.getItem(index) ?: return

            val mConversation = item.mConversation ?: return

            if (null == mConversation.conversationType) return

            RIMClient.getConversationDetail(
                targetId,
                mConversation.conversationType,
                onSuccess = { mCurrentConversation ->

                    if (null == mCurrentConversation) {
                        return@getConversationDetail
                    }

                    mNewConversationAdapter.remove(item)

                    if (mViewBinding.tabLayout.selectedTabPosition == 0) {
                        mConversationAdapter.add(CustomConversationData(mCurrentConversation))
                        sortByDescending()
                    }

                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clearRedMessage(targetId: String) {

        try {


            if (targetId.isEmpty()) {

                return
            }

            val index = mConversationAdapter.items.indexOfFirst { item ->
                item.mConversation.targetId == targetId
            }

            if (index == -1) return

            val item = mConversationAdapter.getItem(index) ?: return

            if (null == item.mConversation.conversationType) return

            RIMClient.getConversationDetail(
                targetId,
                item.mConversation.conversationType,
                onSuccess = { mCurrentConversation ->

                    if (null == mCurrentConversation) {
                        return@getConversationDetail
                    }


                    getCurrentConversation(targetId)?.let { item ->

                        val param = HashMap<String, String>()
                            .apply { this["red"] = "False" }

                        item.mConversation = mCurrentConversation

                        item.mConversation.latestExpansion = param

                        mConversationAdapter.notifyItemChanged(index, false)


                        sortByDescending()


                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private fun getCurrentConversation(targetId: String): CustomConversationData? {

        if (targetId.isNotEmpty()) {

            val index = mConversationAdapter.items.indexOfFirst { item ->
                item.mConversation.targetId == targetId
            }

            if (index != -1) {

                return mConversationAdapter.getItem(index)

            }

        }

        return null
    }


    private fun removeConversation(targetId: String?) {

        try {
            if (targetId.isNullOrEmpty()) {

                return
            }
            val index = mConversationAdapter.items.indexOfFirst { item ->
                item.mConversation.targetId == targetId
            }
            if (index != -1 && index < mConversationAdapter.itemCount) {
                mConversationAdapter.removeAt(index)
            }
            RIMClient.removeConversation(
                targetId,
                onSuccess = {

                    FlowBus.with(AppConstant.EventConstant.EVENT_GET_UNREAD_COUNT).postValue(true)

                })

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private fun requestNoticePermission() {

        if (requireContext().hasNotificationPermission()) {
            withViewBinding {
                sclNotice.visibility = View.GONE
            }

        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                ) {

                    requireContext().openNoticePermissionDetail()
                } else {

                    registerForActivityResult.launch(
                        arrayOf(
                            Manifest.permission.POST_NOTIFICATIONS,
                        )
                    )
                }

            } else {
                requireContext().openNoticePermissionDetail()
            }
        }
    }

    private fun initNoticeDialog() {

        if (requireContext().hasNotificationPermission()) return

        val cache = getCache(AppConstant.Constant.HAS_MESSAGE_NOTICE, false)

        if (cache) return

        setCache(AppConstant.Constant.HAS_MESSAGE_NOTICE, true)

        val mNoticeDialog = OpenNoticeDialog().apply {

            onConfirm = {

                requestNoticePermission()

            }

        }

        lifecycleScope.launch {

            delay(1000)

            mNoticeDialog.show(parentFragmentManager)
        }


    }


    private fun onRefresh() {

        mNewConversationDataList.clear()

        mConversationAdapter.submitList(null)

        val mLikes = mNewConversationDataList.findLast { it.name == "Likes" }

        if (null != mLikes){
            mNewConversationDataList.remove(mLikes)
        }

        val mVisitor = mNewConversationDataList.findLast { it.name == "Visitor" }

        if (null != mVisitor){
            mNewConversationDataList.remove(mVisitor)
        }


//        mNewConversationAdapter.submitList(null)

        if (mViewBinding.tabLayout.selectedTabPosition == 0) {
            mViewModel.getConversationList(true)
        } else {
            mViewModel.getLoveConversation()
        }



        mViewModel.getNewConversationList()

        getUnreadCount()

        FlowBus.with(AppConstant.EventConstant.EVENT_GET_UNREAD_COUNT).postValue(true)
    }

    private fun onLoadMore() {

        mViewModel.getConversationList(false)

    }


    private fun refreshLoadDataListener(
        refresh: SwipeRefreshLayout,
        loading: QuickAdapterHelper,
        errorReLoadView: ErrorReLoadView?,
        data: MutableList<CustomConversationData>
    ) {


        if (refresh.isRefreshing) {

            refresh.isRefreshing = false

            if (data.isNotEmpty()) {

                if (data.size < AppConstant.Constant.PAGE_SIZE_COUNT) {
                    loading.trailingLoadState = LoadState.NotLoading(true)
                } else {
                    loading.trailingLoadState = LoadState.NotLoading(false)
                }

                mConversationAdapter.submitList(data.filter {
                    null != it.mConversation.latestMessage

                            && (null == it.mConversation.latestExpansion || it.mConversation.latestExpansion["isNewConversation"] != "True")
                })

            }

        } else if (loading.trailingLoadState == LoadState.Loading) {

            if (data.size < AppConstant.Constant.PAGE_SIZE_COUNT) {

                loading.trailingLoadState = LoadState.NotLoading(true)
            } else {
                loading.trailingLoadState = LoadState.NotLoading(false)
            }


            mConversationAdapter.addAll(data.filter {
                null != it.mConversation.latestMessage

                        && (null == it.mConversation.latestExpansion || it.mConversation.latestExpansion["isNewConversation"] != "True")
            })
        } else {
            if (data.isNotEmpty()) {

                if (data.size < AppConstant.Constant.PAGE_SIZE_COUNT) {
                    loading.trailingLoadState = LoadState.NotLoading(true)
                } else {
                    loading.trailingLoadState = LoadState.NotLoading(false)
                }



                mConversationAdapter.submitList(data.filter {
                    null != it.mConversation.latestMessage

                            && (null == it.mConversation.latestExpansion || it.mConversation.latestExpansion["isNewConversation"] != "True")
                })
            }
        }

        errorReLoadView?.showSuccessView(mConversationAdapter.items)

        emptyImage?.let {

            if (mConversationAdapter.itemCount == 0){
                if (mViewBinding.tabLayout.selectedTabPosition == 0){
                    it.setImageResource(R.drawable.iv_empty_message)
                }else{
                    it.setImageResource(R.drawable.iv_empty_like_message)
                }
            }
        }

    }


    private fun initTabLayout() {

        titles.forEachIndexed { index, s ->

            mViewBinding.tabLayout.addTab(mViewBinding.tabLayout.newTab().apply {

                val mViewLikeTabBinding =
                    ViewLikeTabBinding.inflate(LayoutInflater.from(context))

                mViewLikeTabBinding.tvTitle.text = s

                if (index == 0) {

                    mViewLikeTabBinding.tvTitle.setTextColor(requireContext().getColor(R.color.white))
                } else {
                    mViewLikeTabBinding.tvTitle.setTextColor(requireContext().getColor(R.color.color_999999))

                }
                customView = mViewLikeTabBinding.root
            })

        }

        val tabStrip = mViewBinding.tabLayout.getChildAt(0) as ViewGroup
        for (i in 0 until tabStrip.childCount) {
            val tabView = tabStrip.getChildAt(i)
            tabView.setPadding(0)
        }


        mViewBinding.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                if (null == tab) {

                    return
                }
                tab.customView?.let {

                    val mViewLikeTabBinding = ViewLikeTabBinding.bind(it)

                    mViewLikeTabBinding.tvTitle.setTextColor(getColor(R.color.white))

                }

                if (tab.position == 0) {

                    mViewModel.getConversationList(true)

                } else {
                    mViewModel.getLoveConversation()
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

                if (null == tab) {

                    return
                }
                tab.customView?.let {

                    val mViewLikeTabBinding = ViewLikeTabBinding.bind(it)

                    mViewLikeTabBinding.tvTitle.setTextColor(getColor(R.color.color_999999))

                }

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })


        if (UserInfoHold.isReview) {

            mViewBinding.tabLayout.visibility = View.GONE

        } else {

            mViewBinding.tabLayout.visibility = View.VISIBLE
        }
    }


    fun getUnreadCount() {
        try {

            RIMClient.getUnreadCount(onSuccess = {

                if (it > 0) {
                    mViewBinding.stvMessageCount.visibility = View.VISIBLE
                    if (it > 999) {
                        mViewBinding.stvMessageCount.text = "${999}+"
                    } else {
                        mViewBinding.stvMessageCount.text = "$it"
                    }

                } else {

                    mViewBinding.stvMessageCount.visibility = View.GONE

                }

            }, conversationType = Conversation.ConversationType.PRIVATE)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addLikeConversation(mWlmData: WlmData) {


        if (!mWlmData.userList.isNullOrEmpty()) {

            val item = mNewConversationDataList.findLast { it.name == "Likes" }

            if (null != item){
                mNewConversationDataList.remove(item)
            }

            mNewConversationDataList.add(0, NewConversationData(null).apply {

                name = "Likes"

                count = mWlmData.totalCount

                list = mWlmData.userList

            })
        }


    }


    private fun addVisitorConversation(mModelCardData: ModelCardData) {


        if (!mModelCardData.userList.isNullOrEmpty()) {

            val item = mNewConversationDataList.findLast { it.name == "Visitor" }

            if (null != item){
                mNewConversationDataList.remove(item)
            }

            mNewConversationDataList.add(
                if (null == mNewConversationDataList.findLast { it.name == "Likes" }) 0 else 1,
                NewConversationData(null).apply {

                    name = "Visitor"

                    count = mModelCardData.totalCount

                    list = mModelCardData.userList
                })
        }

        mNewConversationAdapter.submitList(mNewConversationDataList)

        val relationIds = mNewConversationAdapter.items.filter { null != it.mConversation }.map {

            it.mConversation?.targetId ?: ""

        }.filter { it.isNotEmpty() }.toMutableList()

        isGetNewOnline = true

        mViewModel.getOnlineStatus(relationIds = relationIds)

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

        mBuyVipDialog?.show(parentFragmentManager)


    }


    private fun reportEvent(nameIndex: Int, mCustomConversationData: CustomConversationData) {

        val params = mutableMapOf<String, Any?>()

        val messageExtraData = getMessageExtraData(mCustomConversationData.mConversation)

        params["model_id"] = messageExtraData?.userId2
        params["model_name"] = messageExtraData?.name2
        params["convo_id"] = mCustomConversationData.mConversation.targetId

        if (nameIndex == 0) {
            params["pin_amount"] = getCurrentTopCount()
        }

        reportEvent(mMessageEventKey[nameIndex], params)
    }

    override fun onDestroy() {

        RIMDispatcher.removeListener(mMessageListener)
        mNewConversationAdapter.stopCarousel()
        super.onDestroy()
    }
}