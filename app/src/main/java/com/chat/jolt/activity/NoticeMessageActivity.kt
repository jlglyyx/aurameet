package com.chat.jolt.activity


import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter.OnTrailingListener
import com.chat.jolt.adapter.NoticeMessageAdapter
import com.chat.jolt.databinding.ActNoticeMessageBinding
import com.chat.jolt.viewmodel.MainViewModel
import com.chat.lib_common.activity.BaseActivity
import com.chat.lib_common.adapter.BottomLoadAdapter
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.im.RIMClient
import com.chat.lib_common.util.edgeToEdgeTop
import io.rong.imlib.RongCoreClient
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.HistoryMessageOption
import kotlin.apply
import kotlin.collections.isNullOrEmpty
import kotlin.collections.last
import kotlin.collections.toMutableList
import kotlin.let


class NoticeMessageActivity : BaseActivity<ActNoticeMessageBinding, MainViewModel>(ActNoticeMessageBinding::inflate) {

    private val mNoticeMessageAdapter: NoticeMessageAdapter by lazy { NoticeMessageAdapter() }

    private lateinit var mQuickAdapterHelper: QuickAdapterHelper



    private var lastSendTime = 0L

    private var historyMessageOption = HistoryMessageOption(0L,
        AppConstant.Constant.PAGE_SIZE_COUNT,HistoryMessageOption.PullOrder.DESCEND)


    override fun initView() {


        mViewBinding.apply {

            root.edgeToEdgeTop()

            ivBack.setOnClickListener {
                finish()
            }
        }


    }

    override fun initViewModel() {



    }



    override fun initData() {


        initRecyclerView()

        getHistoryMessage()


        RIMClient.clearMessagesUnread(AppConstant.RIMConstant.SYSTEM_NOTICE, conversationType =  Conversation.ConversationType.PRIVATE, onSuccess = {

            FlowBus.with(AppConstant.EventConstant.EVENT_GET_UNREAD_NOTICE_COUNT).tryEmit(AppConstant.RIMConstant.SYSTEM_NOTICE)
        })
    }


    private fun getHistoryMessage() {


        historyMessageOption = HistoryMessageOption(lastSendTime,
            AppConstant.Constant.PAGE_SIZE_COUNT,HistoryMessageOption.PullOrder.DESCEND)

        RongCoreClient.getInstance().getMessages(
            Conversation.ConversationType.PRIVATE, AppConstant.RIMConstant.SYSTEM_NOTICE, historyMessageOption
        ) { messageList, coreErrorCode ->

            val list = messageList?.toMutableList() ?: mutableListOf()

            if (!list.isNullOrEmpty()) {
                lastSendTime = list.last().sentTime
            }

            list.let {
                if (mQuickAdapterHelper.trailingLoadState == LoadState.Loading) {
                    if (it.size < AppConstant.Constant.PAGE_SIZE_COUNT) {
                        mQuickAdapterHelper.trailingLoadState = LoadState.NotLoading(true)
                    } else {
                        mQuickAdapterHelper.trailingLoadState = LoadState.NotLoading(false)
                    }

                    mNoticeMessageAdapter.addAll(it)
                    mNoticeMessageAdapter.notifyItemRangeChanged(0, mNoticeMessageAdapter.itemCount, false)
                } else {
                    if (it.size < AppConstant.Constant.PAGE_SIZE_COUNT) {
                        mQuickAdapterHelper.trailingLoadState = LoadState.NotLoading(true)
                    } else {
                        mQuickAdapterHelper.trailingLoadState = LoadState.NotLoading(false)
                    }

                    mNoticeMessageAdapter.submitList(list)
                }

            }

        }

    }

    private fun initRecyclerView() {



        mQuickAdapterHelper = QuickAdapterHelper.Builder(mNoticeMessageAdapter)
            .setTrailingLoadStateAdapter(BottomLoadAdapter().setOnLoadMoreListener(object :
                OnTrailingListener {
                override fun onFailRetry() {

                    getHistoryMessage()

                }

                override fun onLoad() {

                    getHistoryMessage()
                }


            })).build()


        mQuickAdapterHelper.trailingLoadStateAdapter?.preloadSize = 3


        mViewBinding.recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = false
        }


        mViewBinding.recyclerView.adapter = mQuickAdapterHelper.adapter


    }



}