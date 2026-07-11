package com.chat.jolt.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.blankj.utilcode.util.SpanUtils
import com.chat.jolt.R
import com.chat.jolt.activity.PayWebActivity
import com.chat.jolt.data.Tpl
import com.chat.jolt.data.VipData
import com.chat.jolt.databinding.DialogBuyRightBinding
import com.chat.jolt.databinding.ItemRightBinding
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.viewmodel.PublicViewModel
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.tracking.mRightKey
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.GooglePayManager
import com.chat.lib_common.util.click
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.formatProductDayPrice
import com.chat.lib_common.util.fromJson
import com.chat.lib_common.util.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import kotlin.apply
import kotlin.collections.isNullOrEmpty
import kotlin.getValue
import kotlin.let
import kotlin.text.isNotEmpty
import kotlin.text.isNullOrEmpty
import kotlin.to

class BuyRightDialog : BaseDialog<DialogBuyRightBinding>(DialogBuyRightBinding::inflate) {

    private val mViewModel by sharedViewModels<PublicViewModel>()

    private lateinit var mRightAdapter: BaseRecyclerAdapter<Tpl, ItemRightBinding>

    private var mCurrentPosition = 1

    private var mVipData: VipData? = null

    private var currentTpl: Tpl? = null

    private var bizId: Int? = null

    private var mBuyRightSuccessDialog: BuyRightSuccessDialog? = null


    private var imageMap = mutableMapOf(
        AppConstant.Constant.PAY_FLASH_CHAT to R.drawable.iv_buy_flash_chat,
        AppConstant.Constant.PAY_PRIVATE_PHOTO to R.drawable.iv_vip_private_photo_,
        AppConstant.Constant.PAY_PRIVATE_VIDEO to R.drawable.iv_vip_private_video_,
    )

    private var currentImage = R.drawable.iv_buy_flash_chat


    private var mRegisterForActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->

            val data = activityResult.data

            val mBizId = data?.getIntExtra(AppConstant.Constant.BIZ_ID, -1)

            if (null != mBizId && mBizId != -1) {

                mViewModel.mRetryTime = 0

                mViewModel.getRechargeStatus(mBizId)
            } else {
                dismissLoading()
            }


        }


    companion object {
        fun newInstance(data: VipData): BuyRightDialog {
            return BuyRightDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, data.toJson())
                }
            }
        }
    }


    override fun initView() {

        withViewBinding {

            initPayListener()

            root.edgeToEdgeBottom()


            ivCloseRight.click {

                dismissAllowingStateLoss()
            }




            stvNext.click {

                if (mVipData?.tplList.isNullOrEmpty()) return@click

                val item = mRightAdapter.getItem(mCurrentPosition) ?: return@click

                showLoading(dismissOnBackPressed = false)

                item.type = mVipData?.type

                currentTpl = item

                mViewModel.createOrder(item, "MyInfo")

                reportBuyEvent(currentTpl)

            }


        }

    }

    override fun initData() {

        AppConstant.Constant.isShowBuy = true

        arguments?.let {

            val data = it.getString(AppConstant.Constant.DATA, "")


            if (data.isNotEmpty()) {

                mVipData = data.fromJson()

            }

            reportShowEvent()

        }

        currentImage = imageMap[mVipData?.type] ?: currentImage

        initRecyclerView(mVipData?.tplList)



        if (null != mVipData) {

            withViewBinding {
                when (mVipData?.type) {

                    AppConstant.Constant.PAY_FLASH_CHAT -> {

                        tvTitle.text = "Flashchat "

                        val flashChatCount = UserInfoHold.userInfo?.flashChatCount ?: 0

                        tvBalance.text = "Balance:${UserInfoHold.userInfo?.flashChatCount ?: 0}"

                        if (flashChatCount <= 0 && mVipData?.isMeInto != 1) {

                            tvNotice.visibility = View.VISIBLE

                            SpanUtils.with(mDialogBinding.tvNotice).append("Insufficient Flashchat")
                                .append(" , Please recharge to continue.")
                                .setForegroundColor(getColor(R.color.color_C5C5C5)).create()
                        } else {
                            tvNotice.visibility = View.GONE
                        }
                    }

                    AppConstant.Constant.PAY_PRIVATE_PHOTO -> {

                        tvTitle.text = "Private Photo"

                        val privatePhotoCount = UserInfoHold.userInfo?.privatePhotoCount ?: 0

                        tvBalance.text = "Balance:${UserInfoHold.userInfo?.privatePhotoCount ?: 0}"

                        if (privatePhotoCount <= 0 && mVipData?.isMeInto != 1) {

                            tvNotice.visibility = View.VISIBLE

                            SpanUtils.with(mDialogBinding.tvNotice)
                                .append("Insufficient Private Photo")
                                .append(" , Please recharge to continue.")
                                .setForegroundColor(getColor(R.color.color_C5C5C5)).create()
                        } else {
                            tvNotice.visibility = View.GONE
                        }
                    }

                    AppConstant.Constant.PAY_PRIVATE_VIDEO -> {

                        tvTitle.text = "Private Video"

                        val privateVideoCount = UserInfoHold.userInfo?.privateVideoCount ?: 0

                        tvBalance.text = "Balance:${UserInfoHold.userInfo?.privateVideoCount ?: 0}"

                        if (privateVideoCount <= 0 && mVipData?.isMeInto != 1) {

                            tvNotice.visibility = View.VISIBLE

                            SpanUtils.with(mDialogBinding.tvNotice)
                                .append("Insufficient Private Video")
                                .append(" , Please recharge to continue.")
                                .setForegroundColor(getColor(R.color.color_C5C5C5)).create()
                        } else {
                            tvNotice.visibility = View.GONE
                        }
                    }

                }

                currentImage = imageMap[mVipData?.type] ?: currentImage
            }
        }

    }


    private fun initRecyclerView(list: MutableList<Tpl>?) {

        if (list.isNullOrEmpty()) return


        mRightAdapter =
            object : BaseRecyclerAdapter<Tpl, ItemRightBinding>(ItemRightBinding::inflate) {
                override fun convert(
                    holder: BaseRecyclerViewHolder<ItemRightBinding>,
                    itemView: ItemRightBinding,
                    item: Tpl,
                    position: Int
                ) {

                    itemView.apply {

                        tvSave.text =
                            if (item.discountInfo.isNullOrEmpty()) "" else item.discountInfo

                        tvSave.visibility =
                            if (item.discountInfo.isNullOrEmpty()) View.GONE else View.VISIBLE

                        tvName.text = "${item.count} times"

                        val formatProductDayPrice =
                            formatProductDayPrice(item.formatMoney, item.count)

                        if (item.dayMoney.isNullOrEmpty()){

                            tvPrice.visibility = View.GONE
                        }else{
                            tvPrice.visibility = View.VISIBLE
                            tvPrice.text =
                                if (null == item.formatMoney || null == formatProductDayPrice) "$${item.dayMoney}/Time" else "$formatProductDayPrice/Time"
                        }



                        if (item.isSelect) {
                            sllContainer.shapeDrawableBuilder.setSolidColor(
                                requireContext().getColor(
                                    R.color.color_15EAA82B
                                )
                            )
                                .setStrokeColor(requireContext().getColor(R.color.color_button))
                                .intoBackground()

                        } else {
                            sllContainer.shapeDrawableBuilder.setSolidColor(
                                requireContext().getColor(
                                    R.color.color_434343
                                )
                            )
                                .setStrokeColor(requireContext().getColor(R.color.transparent))
                                .intoBackground()
                        }

                    }

                }

            }

        mDialogBinding.recyclerView.adapter = mRightAdapter

        mCurrentPosition = list.indexOfFirst { it.defaultTpl == "True" }.coerceAtLeast(0)


        if (list.size >= mCurrentPosition) {

            list[mCurrentPosition].isSelect = true

            getDesc(list[mCurrentPosition])
        }

        mRightAdapter.submitList(list)


        mRightAdapter.setOnItemClickListener { _, _, position ->

            if (mCurrentPosition == position) {


                mDialogBinding.stvNext.performClick()

                return@setOnItemClickListener
            }

            val item = mRightAdapter.getItem(position) ?: return@setOnItemClickListener

            val lastItem = mRightAdapter.getItem(mCurrentPosition)

            if (null != lastItem) {
                lastItem.isSelect = false

                mRightAdapter.notifyItemChanged(mCurrentPosition, false)
            }


            item.isSelect = true

            mCurrentPosition = position

            getDesc(item)

            mRightAdapter.notifyItemChanged(mCurrentPosition, false)

        }

    }


    private fun getDesc(mTpl: Tpl?) {

        if (null == mTpl) return


        val count = mTpl.count

        val text = if (count > 1) {
            "$count opportunities"
        } else {
            "$count opportunitie"
        }



        when (mVipData?.type) {


            AppConstant.Constant.PAY_FLASH_CHAT -> {
                mDialogBinding.tvDesc.text = "Use Priority to Chat Without Waiting"
                mDialogBinding.ivRight.setImageResource(R.drawable.iv_buy_flash_chat)
            }

            AppConstant.Constant.PAY_PRIVATE_PHOTO -> {

                val type = if (count > 1) {
                    "Photos"
                } else {
                    "Photo"
                }

                mDialogBinding.tvDesc.text = "Receive $text to unlock Private $type"
                mDialogBinding.ivRight.setImageResource(R.drawable.iv_vip_private_photo_)
            }

            AppConstant.Constant.PAY_PRIVATE_VIDEO -> {

                val type = if (count > 1) {
                    "Videos"
                } else {
                    "Video"
                }

                mDialogBinding.tvDesc.text = "Receive $text to unlock Private $type"
                mDialogBinding.ivRight.setImageResource(R.drawable.iv_vip_private_video_)
            }

        }

        mDialogBinding.stvNext.text =
            "Continue (${if (null == mTpl.formatMoney) "$ ${mTpl.money}" else "${mTpl.formatMoney}"})"
    }


    override fun initViewModel() {
        super.initViewModel()

        mViewModel.mCreateOrderInfoData.observe(this) {

            bizId = it.bizId

            if (AppConstant.Constant.threePay) {

                mRegisterForActivityResult.launch(
                    createIntent(PayWebActivity::class.java)
                        .putExtra(AppConstant.Constant.URL, it.content?.payUrl)
                        .putExtra(AppConstant.Constant.TITLE, "Pay")
                        .putExtra(AppConstant.Constant.BIZ_ID, bizId)
                )

                return@observe

            }

            if (AppConstant.ClientInfo.OPEN_GOOGLE) {

                it.productId?.let { productId ->

                    GooglePayManager.queryProduct(
                        requireActivity(),
                        TAG,
                        productId,
                        it.purchaseToken, UserInfoHold.userId
                    )
                }

            } else {

                mViewModel.payOrderTest(it.bizId, currentTpl?.money ?: "0.0")
            }

        }


        mViewModel.mOrderInfoData.observe(this) {

            if (AppConstant.Constant.threePay) {

                dismissLoading()

                initBuySuccessDialog()

            } else {
                if (AppConstant.ClientInfo.OPEN_GOOGLE) {
                    if (null != it.mPurchase) {

                        if (it.type == BillingClient.ProductType.SUBS) {

                            GooglePayManager.handleSunsProduct(it.mPurchase!!)
                        } else {

                            GooglePayManager.handleINAPPProduct(it.mPurchase!!)
                        }

                    }
                } else {

                    dismissLoading()

                    initBuySuccessDialog()
                }
            }

            FlowBus.with(AppConstant.EventConstant.EVENT_REFRESH_CARD_LIST).postValue(true)

        }

        mViewModel.mOrderInfoStatus.observe(this) {

            if (it) {

                initBuySuccessDialog(true)

                dismissAllowingStateLoss()
//                showShort("Hi, tiger! it will take 1-5 minutes for your benefits to becredited, thanks for your patience.")
            } else {
//                showShort("pay error")
            }

            dismissLoading()

        }

        mViewModel.mPayErrorStatus.observe(this) {

            dismissLoading()

        }
    }


    private fun initBuySuccessDialog(isThreeLater: Boolean = false) {

        dismissAllowingStateLoss()

        if (null == currentTpl) return

        if (null != mBuyRightSuccessDialog && mBuyRightSuccessDialog?.isVisible == true) return

        mBuyRightSuccessDialog = BuyRightSuccessDialog.newInstance(currentTpl!!,isThreeLater)

        mBuyRightSuccessDialog?.show(parentFragmentManager)

    }


    private fun initPayListener() {

        GooglePayManager.addListener(TAG, object : GooglePayManager.GooglePayListener {

            override fun onError(code: Int, data: String, orderId: String?) {

                mViewModel.payFail(bizId, orderId, "[${code}] $data")

                dismissLoading()
            }

            override fun onClientSuccess() {

            }


            override fun onPaySuccess(mPurchase: Purchase, type: String, lastOrderId: Int?) {


                if (null == bizId) {

                    if (null != lastOrderId && lastOrderId != -1) {

                        mViewModel.payOrderGoogle(
                            lastOrderId,
                            currentTpl?.money ?: "0.0",
                            mPurchase,
                            type
                        )
                    }

                } else {

                    mViewModel.payOrderGoogle(bizId!!, currentTpl?.money ?: "0.0", mPurchase, type)

                    bizId = null

                }


            }

            override fun onHandlePurchaseSuccess(mPurchase: Purchase, type: String) {

                lifecycleScope.launch(Dispatchers.Main) {

                    dismissLoading()

                    initBuySuccessDialog()
                }

            }


        })


    }


    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }


    private fun reportShowEvent() {

        if (null == mVipData) return

        mVipData?.let {
            val param = mutableMapOf<String, Any?>()
            param["method"] = it.showType
            param["convo_id"] = it.targetId
            param["model_id"] = it.userId2
            param["model_name"] = it.name2
            param["user_type"] = UserInfoHold.isNewUser
            param["m_type"] = it.buyRightCount

            when (mVipData?.type) {
                AppConstant.Constant.PAY_FLASH_CHAT -> {
                    reportEvent(mRightKey[4], param)
                }
                AppConstant.Constant.PAY_PRIVATE_PHOTO -> {
                    reportEvent(mRightKey[0], param)
                }
                AppConstant.Constant.PAY_PRIVATE_VIDEO -> {
                    reportEvent(mRightKey[2], param)
                }
            }



        }
    }
    private fun reportBuyEvent(mTpl:Tpl?) {

        mTpl?.let {
            val param = mutableMapOf<String, Any?>()
            param["product_name"] = mTpl.tplName
            param["user_type"] = UserInfoHold.isNewUser
            when (mVipData?.type) {
                AppConstant.Constant.PAY_FLASH_CHAT -> {
                    reportEvent(mRightKey[5], param)
                }
                AppConstant.Constant.PAY_PRIVATE_PHOTO -> {
                    reportEvent(mRightKey[1], param)
                }
                AppConstant.Constant.PAY_PRIVATE_VIDEO -> {
                    reportEvent(mRightKey[3], param)
                }
            }

        }
    }


    override fun onDismiss(dialog: DialogInterface) {

        GooglePayManager.removeListener(TAG)
        AppConstant.Constant.isShowBuy = false
        super.onDismiss(dialog)



    }

}