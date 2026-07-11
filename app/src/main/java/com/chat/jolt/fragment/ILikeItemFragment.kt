package com.chat.jolt.fragment

import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.chat.jolt.R
import com.chat.jolt.activity.UserInfoActivity
import com.chat.jolt.adapter.ILikeAdapter
import com.chat.jolt.data.ModelUserData
import com.chat.jolt.data.VipData
import com.chat.jolt.databinding.FraIItemLikeBinding
import com.chat.jolt.databinding.ViewILikeEmptyBinding
import com.chat.jolt.databinding.ViewNoNetworkBinding
import com.chat.jolt.dialog.BuyRightDialog
import com.chat.jolt.dialog.BuyVipDialog
import com.chat.jolt.dialog.FlashSuccessDialog
import com.chat.jolt.viewmodel.LikeViewModel
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.observe
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.fragment.BaseFragment
import com.chat.lib_common.util.click
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.refreshLoadDataListener
import com.chat.lib_common.util.refreshLoadListener
import com.chat.lib_common.util.startActivity
import com.chat.lib_common.util.toJson
import com.chat.lib_common.widget.ErrorReLoadView

class ILikeItemFragment :
    BaseFragment<FraIItemLikeBinding, LikeViewModel>(FraIItemLikeBinding::inflate) {

    private var mFlashSuccessDialog: FlashSuccessDialog? = null

    private var mBuyVipDialog: BuyVipDialog? = null

    private var mBuyRightDialog: BuyRightDialog? = null

    private val mILikeAdapter: ILikeAdapter by lazy {
        ILikeAdapter()
    }

    private lateinit var mQuickAdapterHelper: QuickAdapterHelper


    private var currentPosition = -1


    override fun initView() {


        withViewBinding {



            initRecyclerView()
        }


    }

    override fun onResume() {
        super.onResume()

        onRefresh()
    }

    override fun initData() {


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


        mViewModel.mILikeData.observe(this) {

            withViewBinding {

                mILikeAdapter.refreshLoadDataListener(
                    mSwipeRefreshLayout,
                    mQuickAdapterHelper,
                    errorReLoadView,
                    it.userList
                )
            }

        }

        mViewModel.requestFailEvent.observe(this) {

            mViewBinding.mSwipeRefreshLayout.isRefreshing = false

            mViewBinding.errorReLoadView.showStatusView(ErrorReLoadView.Status.NO_NETWORK)

        }

        mViewModel.mFlashChatStatusData.observe(this) {

            try {

                mFlashSuccessDialog?.dismissAllowingStateLoss()

                if (currentPosition == -1) return@observe

                mILikeAdapter.removeAt(currentPosition)

                mViewBinding.errorReLoadView.showSuccessView(mILikeAdapter.items)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }



        FlowBus.with(AppConstant.EventConstant.EVENT_OPERATION_I_LIKE).observe(this) {

            if (it is Int) {

                try {

                    if (currentPosition == -1) return@observe

                    mILikeAdapter.removeAt(currentPosition)

                    mViewBinding.errorReLoadView.showSuccessView(mILikeAdapter.items)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }


    private fun initRecyclerView() {


        withViewBinding {

            mQuickAdapterHelper =
                mILikeAdapter.refreshLoadListener(mSwipeRefreshLayout, onRefresh = {

                    onRefresh()

                }, onLoad = {

                    onLoadMore()
                })

            recyclerView.adapter = mQuickAdapterHelper.adapter


            recyclerView.setRecycledViewPool(RecyclerView.RecycledViewPool())

            recyclerView.itemAnimator = null



            errorReLoadView.addEmptyView { viewGroup ->
                ViewILikeEmptyBinding.inflate(LayoutInflater.from(requireContext()), viewGroup, true)
                    .apply {
                        tvTitle.text = "No Likes From You !"
                        tvDesc.text = "You haven't liked anyone yet. Start swiping to find someone you like!"

                        stv0.click {

                            FlowBus.with(AppConstant.EventConstant.EVENT_SET_PAGE).postValue(0)
                        }

                        stv1.visibility = View.GONE
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

            mViewBinding.errorReLoadView.showSuccessView(mILikeAdapter.items)

            mILikeAdapter.addOnDebouncedChildClick(R.id.scl_flash_chat) { _, _, position ->

                val item = mILikeAdapter.getItem(position) ?: return@addOnDebouncedChildClick

                currentPosition = position

                initFlashDialog(item)




            }
            mILikeAdapter.setOnDebouncedItemClick { adapter, view, position ->

                val item = mILikeAdapter.getItem(position) ?: return@setOnDebouncedItemClick

                currentPosition = position

                createIntent(UserInfoActivity::class.java)
                    .putExtra(AppConstant.Constant.ID, item.userId)
                    .putExtra(
                        AppConstant.Constant.PAGE,
                        "ILike"
                    )
                    .putExtra(AppConstant.Constant.MODEL_DATA, item.toJson())
                    .startActivity(requireActivity())

            }

            mILikeAdapter.onImageClick = { userId,parentPosition ->

                val item = mILikeAdapter.getItem(parentPosition)

                if (null != item){

                    currentPosition = parentPosition

                    createIntent(UserInfoActivity::class.java)
                        .putExtra(AppConstant.Constant.ID, userId)
                        .putExtra(
                            AppConstant.Constant.PAGE,
                            "ILike"
                        )
                        .putExtra(AppConstant.Constant.MODEL_DATA, item.toJson())
                        .startActivity(requireActivity())
                }

            }

        }


    }


    private fun initFlashDialog(
        mModelUserData: ModelUserData,
    ) {

        if (null != mFlashSuccessDialog && mFlashSuccessDialog?.isVisible == true) return

        mFlashSuccessDialog = FlashSuccessDialog.newInstance(mModelUserData).apply {

            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    mFlashSuccessDialog = null
                }
            })
        }


        mFlashSuccessDialog?.onConfirm = {

            mViewModel.flashChat(mModelUserData.userId,"HomeCard",it)

        }

        mFlashSuccessDialog?.show(parentFragmentManager)

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

        mViewModel.queryLiked(0)

        currentPosition = -1
    }

    private fun onLoadMore() {

        mViewModel.queryLiked(mILikeAdapter.itemCount)

    }


}