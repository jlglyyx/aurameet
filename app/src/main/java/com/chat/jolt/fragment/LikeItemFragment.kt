package com.chat.jolt.fragment

import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.chat.jolt.activity.EditUserInfoActivity
import com.chat.jolt.adapter.LikeItemAdapter
import com.chat.jolt.data.VipData
import com.chat.jolt.databinding.FraItemLikeBinding
import com.chat.jolt.databinding.ViewILikeEmptyBinding
import com.chat.jolt.databinding.ViewNoNetworkBinding
import com.chat.jolt.dialog.BuyRightDialog
import com.chat.jolt.dialog.BuyVipDialog
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.viewmodel.LikeViewModel
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.observe
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.fragment.BaseFragment
import com.chat.lib_common.tracking.mVipShowValue
import com.chat.lib_common.util.click
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.refreshLoadDataListener
import com.chat.lib_common.util.refreshLoadListener
import com.chat.lib_common.util.startActivity
import com.chat.lib_common.util.viewVisibility
import com.chat.lib_common.widget.ErrorReLoadView

class LikeItemFragment :
    BaseFragment<FraItemLikeBinding, LikeViewModel>(FraItemLikeBinding::inflate) {

    private var mBuyVipDialog: BuyVipDialog? = null

    private var mBuyRightDialog: BuyRightDialog? = null


    private val mLikeItemAdapter: LikeItemAdapter by lazy {
        LikeItemAdapter()
    }

    private lateinit var mQuickAdapterHelper: QuickAdapterHelper


    private var currentPosition = -1


    override fun initView() {


        withViewBinding {

            initRecyclerView()


            sclOpenVip.click {

                mViewModel.getVipInfo(AppConstant.Constant.PAY_VIP, mVipShowValue[6], "PremiumBadge")
            }


        }


    }

    override fun onResume() {
        super.onResume()

        onRefresh()
    }

    override fun initData() {


        if (UserInfoHold.isVip) {
            viewVisibility( View.GONE,mViewBinding.sclOpenVip,mViewBinding.svCover)
        } else {
            viewVisibility( View.VISIBLE,mViewBinding.sclOpenVip,mViewBinding.svCover)
        }
    }

    override fun initViewModel() {


        mViewModel.mVipData.observe(this) {

            when (it.type) {
                AppConstant.Constant.PAY_VIP -> {
                    initVipDialog(it)
                }

                else -> {
                    initBuyRightDialog(it)
                }

            }

        }


        mViewModel.mWlmData.observe(this) {


            withViewBinding {

                mLikeItemAdapter.refreshLoadDataListener(
                    mSwipeRefreshLayout,
                    mQuickAdapterHelper,
                    errorReLoadView,
                    it.userList
                )
            }

        }
        mViewModel.mUseWlmStatus.observe(this) {

            try {


                if (currentPosition == -1) return@observe

                mLikeItemAdapter.removeAt(currentPosition)


                mViewBinding.errorReLoadView.showSuccessView(mLikeItemAdapter.items)

                FlowBus.with(AppConstant.EventConstant.EVENT_REFRESH_LIKE_AND_VISITOR).postValue(true)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        mViewModel.requestFailEvent.observe(this) {

            mViewBinding.mSwipeRefreshLayout.isRefreshing = false

            mViewBinding.errorReLoadView.showStatusView(ErrorReLoadView.Status.NO_NETWORK)

        }

        FlowBus.with(AppConstant.EventConstant.EVENT_IS_BUY_GET_USER_INFO).observe(this) {

            mLikeItemAdapter.openAllImage()

            if (UserInfoHold.isVip) {
                viewVisibility( View.GONE,mViewBinding.sclOpenVip,mViewBinding.svCover)
            } else {
                viewVisibility( View.VISIBLE,mViewBinding.sclOpenVip,mViewBinding.svCover)
            }

        }
        FlowBus.with(AppConstant.EventConstant.EVENT_REFRESH_WLM_LIST).observe(this) {

          onRefresh()
        }

    }


    private fun initRecyclerView() {


        withViewBinding {

            mQuickAdapterHelper = mLikeItemAdapter.refreshLoadListener(mSwipeRefreshLayout, onRefresh = {

                onRefresh()

            }, onLoad = {

                onLoadMore()
            })

            recyclerView.adapter = mQuickAdapterHelper.adapter


            recyclerView.setRecycledViewPool(RecyclerView.RecycledViewPool())

            recyclerView.itemAnimator = null

            recyclerView.layoutManager = GridLayoutManager(context, 2)


            errorReLoadView.addEmptyView { viewGroup ->
                ViewILikeEmptyBinding.inflate(
                    LayoutInflater.from(requireContext()),
                    viewGroup,
                    true
                )
                    .apply {
                        tvTitle.text = "No Likes Yet !"
                        tvDesc.text = "Explore new matches or polish your profile to stand out."

                        stv0.click {

                            FlowBus.with(AppConstant.EventConstant.EVENT_SET_PAGE).postValue(0)
                        }

                        stv1.click {

                            requireContext().createIntent(EditUserInfoActivity::class.java)
                                .startActivity(requireContext())

                        }


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

            mViewBinding.errorReLoadView.showSuccessView(mLikeItemAdapter.items)


            mLikeItemAdapter.setOnDebouncedItemClick(500) { _, _, position ->

                val item = mLikeItemAdapter.getItem(position) ?: return@setOnDebouncedItemClick

                currentPosition = position

                if (UserInfoHold.isVip) {

                    mViewModel.useWlm(item.userId)

                } else {
                    mViewModel.getVipInfo(AppConstant.Constant.PAY_VIP, mVipShowValue[6], "PremiumBadge")
                }


            }

        }


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

        mBuyVipDialog?.show(parentFragmentManager)


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

        mBuyRightDialog?.show(parentFragmentManager)


    }

    private fun onRefresh() {

        mViewModel.getWlmList(0)

        currentPosition = -1
    }

    private fun onLoadMore() {

        mViewModel.getWlmList(mLikeItemAdapter.itemCount)

    }


}