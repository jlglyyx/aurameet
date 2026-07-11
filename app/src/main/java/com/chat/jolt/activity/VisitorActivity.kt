package com.chat.jolt.activity

import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.chat.jolt.R
import com.chat.jolt.adapter.VisitorAdapter
import com.chat.jolt.data.CustomMessageExtraData
import com.chat.jolt.data.UserInfoData
import com.chat.jolt.data.VipData
import com.chat.jolt.databinding.ActVisitorBinding
import com.chat.jolt.databinding.ViewILikeEmptyBinding
import com.chat.jolt.databinding.ViewNoNetworkBinding
import com.chat.jolt.dialog.BuyVipDialog
import com.chat.jolt.dialog.MatchSuccessDialog
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.helper.getCmdMessageExtraData
import com.chat.jolt.viewmodel.UserViewModel
import com.chat.lib_common.activity.BaseActivity
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.observe
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.im.RIMDispatcher
import com.chat.lib_common.tracking.GOTO_VISITOR
import com.chat.lib_common.tracking.mVipShowValue
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.click
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.refreshLoadDataListener
import com.chat.lib_common.util.refreshLoadListener
import com.chat.lib_common.util.startActivity
import com.chat.lib_common.util.toJson
import com.chat.lib_common.util.viewVisibility
import com.chat.lib_common.widget.ErrorReLoadView
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import io.rong.imlib.model.ReceivedProfile
import kotlinx.coroutines.launch
import kotlin.jvm.java


class VisitorActivity : BaseActivity<ActVisitorBinding, UserViewModel>(ActVisitorBinding::inflate) {


    private val mVisitorAdapter: VisitorAdapter by lazy {
        VisitorAdapter()
    }

    private lateinit var mQuickAdapterHelper: QuickAdapterHelper

    private var mBuyVipDialog: BuyVipDialog? = null

    private var mMatchSuccessDialog: MatchSuccessDialog? = null

    private var currentPosition = -1


    private val mMessageListener = object : RIMDispatcher.MessageListener {
        override fun onMessageReceiptResponse(
            message: Message,
            type: Conversation.ConversationType,
            targetId: String,
            mReceivedProfile: ReceivedProfile
        ) {

            val isOffline = mReceivedProfile.isOffline

            if (!isOffline) {
                showMatch(message)
            }


        }


    }


    override fun initView() {

        withViewBinding {

            initRecyclerView()

            root.edgeToEdgeBottom()

            sclOpenVip.click {

                mViewModel.getVipInfo(AppConstant.Constant.PAY_VIP, mVipShowValue[14], "PremiumBadge")
            }
        }

    }

    override fun initViewModel() {

        mViewModel.mVisitorData.observe(this){

            withViewBinding {

                mVisitorAdapter.refreshLoadDataListener(
                    mSwipeRefreshLayout,
                    mQuickAdapterHelper,
                    errorReLoadView,
                    it.userList
                )
            }
        }

        mViewModel.mVipData.observe(this) {

            when (it.type) {
                AppConstant.Constant.PAY_VIP -> {
                    initVipDialog(it)
                }
            }

        }


        mViewModel.requestFailEvent.observe(this) {

            mViewBinding.mSwipeRefreshLayout.isRefreshing = false

            mViewBinding.errorReLoadView.showStatusView(ErrorReLoadView.Status.NO_NETWORK)

        }


        mViewModel.mVisitorStatusData.observe(this) {

            try {

                if (currentPosition == -1) return@observe

                mVisitorAdapter.removeAt(currentPosition)

                mViewBinding.errorReLoadView.showSuccessView(mVisitorAdapter.items)


                FlowBus.with(AppConstant.EventConstant.EVENT_REFRESH_LIKE_AND_VISITOR).postValue(true)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        mViewModel.mTargetUserInfoData.observe(this) {

            initMatchDialog(it, it.mCustomMessageExtraData)

        }


        FlowBus.with(AppConstant.EventConstant.EVENT_IS_BUY_GET_USER_INFO).observe(this) {

            mVisitorAdapter.openAllImage()

            if (UserInfoHold.isVip) {
                viewVisibility( View.GONE,mViewBinding.sclOpenVip,mViewBinding.svCover)
            } else {
                viewVisibility( View.VISIBLE,mViewBinding.sclOpenVip,mViewBinding.svCover)
            }

        }

        FlowBus.with(AppConstant.EventConstant.EVENT_OPERATION_VISITOR).observe(this) {

            if (it is Int) {

                try {

                    if (currentPosition == -1) return@observe

                    mVisitorAdapter.removeAt(currentPosition)

                    mViewBinding.errorReLoadView.showSuccessView(mVisitorAdapter.items)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }



    override fun initData() {

        RIMDispatcher.addListener(mMessageListener)

        onRefresh()


        if (UserInfoHold.isVip) {
            viewVisibility( View.GONE,mViewBinding.sclOpenVip,mViewBinding.svCover)
        } else {
            viewVisibility( View.VISIBLE,mViewBinding.sclOpenVip,mViewBinding.svCover)
        }

        reportEvent(GOTO_VISITOR,true)
    }

    private fun initRecyclerView() {


        withViewBinding {

            mQuickAdapterHelper =
                mVisitorAdapter.refreshLoadListener(mSwipeRefreshLayout, onRefresh = {

                    onRefresh()

                }, onLoad = {

                })

            recyclerView.adapter = mQuickAdapterHelper.adapter


            recyclerView.setRecycledViewPool(RecyclerView.RecycledViewPool())

            recyclerView.itemAnimator = null



            errorReLoadView.addEmptyView { viewGroup ->
                ViewILikeEmptyBinding.inflate(
                    LayoutInflater.from(this@VisitorActivity),
                    viewGroup,
                    true
                )
                    .apply {

                        tvTitle.text = "No Visitors Yet"
                        tvDesc.text = "Explore new matches or polish your profile to stand out."

                        stv0.click {

                            finish()

                            FlowBus.with(AppConstant.EventConstant.EVENT_SET_PAGE).postValue(0)
                        }

                        stv1.click {

                            createIntent(EditUserInfoActivity::class.java)
                                .startActivity(this@VisitorActivity)

                        }


                    }
            }

            errorReLoadView.addNoNetView { viewGroup ->
                ViewNoNetworkBinding.inflate(LayoutInflater.from(this@VisitorActivity), viewGroup, true)
                    .apply {

                        stvConfirm.click {

                            onRefresh()
                        }
                    }
            }




            mVisitorAdapter.addOnDebouncedChildClick(R.id.tv_chat){_, _, position ->
                val item = mVisitorAdapter.getItem(position) ?: return@addOnDebouncedChildClick

                currentPosition = position

                if (UserInfoHold.isVip){

                    mViewModel.visitorChat(item.userId)

                }else{

                    mViewModel.getVipInfo(AppConstant.Constant.PAY_VIP, mVipShowValue[14], "PremiumBadge")
                }

            }


            mVisitorAdapter.setOnDebouncedItemClick { _, _, position ->



                val item = mVisitorAdapter.getItem(position) ?: return@setOnDebouncedItemClick

                currentPosition = position

                if (UserInfoHold.isVip) {

                    createIntent(UserInfoActivity::class.java)
                        .putExtra(AppConstant.Constant.ID, item.userId)
                        .putExtra(AppConstant.Constant.SHOW_FLASH, true)
                        .putExtra(
                            AppConstant.Constant.PAGE,
                            "Visitor"
                        )
                        .putExtra(AppConstant.Constant.MODEL_DATA, item.toJson())
                        .startActivity(this@VisitorActivity)

                }else{

                    mViewModel.getVipInfo(AppConstant.Constant.PAY_VIP, mVipShowValue[14], "PremiumBadge")
                }


            }



        }


    }

    private fun onRefresh() {

        mViewModel.myVisitor()

        currentPosition = -1
    }


    private fun initVipDialog(it: VipData) {

        if (null != mBuyVipDialog && mBuyVipDialog?.isVisible == true){

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


    private fun showMatch(message: Message?) {

        if (null == message) return

        lifecycleScope.launch {

            try {

                if (message.objectName == AppConstant.RIMConstant.RC_CMD_MSG) {

                    val messageExtraData = getCmdMessageExtraData(message) ?: return@launch

                    if (messageExtraData.eventCode == AppConstant.RIMConstant.CMD_MATCH_SUCCESS) {

                        if (messageExtraData.data?.oriSource == AppConstant.RIMConstant.CMD_FLASH_CHAT) return@launch

                        mViewModel.getTargetUserInfo(
                            messageExtraData.data?.userId2,
                            messageExtraData.data
                        )

                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }



    private fun initMatchDialog(
        userInfoData: UserInfoData,
        mCustomMessageExtraData: CustomMessageExtraData?
    ) {

        if (AppConstant.Constant.isShowBuy) return

        if (mCustomMessageExtraData == null) return

        if (null != mMatchSuccessDialog && mMatchSuccessDialog?.isVisible == true) return

        mMatchSuccessDialog = MatchSuccessDialog.newInstance(userInfoData, mCustomMessageExtraData)

        mMatchSuccessDialog?.show(supportFragmentManager)

    }

    override fun onDestroy() {
        super.onDestroy()
        RIMDispatcher.removeListener(mMessageListener)
    }
}