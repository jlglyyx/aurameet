package com.chat.jolt.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.blankj.utilcode.util.SpanUtils
import com.chat.jolt.R
import com.chat.jolt.activity.PayWebActivity
import com.chat.jolt.activity.WebActivity
import com.chat.jolt.data.Privilege
import com.chat.jolt.data.Tpl
import com.chat.jolt.data.VipData
import com.chat.jolt.databinding.DialogBuyVipBinding
import com.chat.jolt.databinding.ItemVipBinding
import com.chat.jolt.databinding.ItemVipPrivilegeBinding
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.viewmodel.PublicViewModel
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.tracking.MESSAGE_CHAT_KEY
import com.chat.lib_common.tracking.mMessagePayKey
import com.chat.lib_common.tracking.mVipShowValue
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.GooglePayManager
import com.chat.lib_common.util.click
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.dip2px
import com.chat.lib_common.util.edgeToEdgeAll
import com.chat.lib_common.util.formatProductDayPrice
import com.chat.lib_common.util.fromJson
import com.chat.lib_common.util.getScreenPx
import com.chat.lib_common.util.startActivity
import com.chat.lib_common.util.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BuyVipDialog : BaseDialog<DialogBuyVipBinding>(DialogBuyVipBinding::inflate) {


    private val mViewModel by sharedViewModels<PublicViewModel>()


    private lateinit var mVipAdapter: BaseRecyclerAdapter<Tpl, ItemVipBinding>

    private lateinit var mPrivilegeAdapter: BaseRecyclerAdapter<Privilege, ItemVipPrivilegeBinding>

    private var currentTpl: Tpl? = null

    private var mPrivilegeList: List<Privilege>? = null

    private var bizId: Int? = null

    var onConfirm: (Tpl) -> Unit = {}

    private var mBuyVipSuccessDialog: BuyVipSuccessDialog? = null

    private var mBuyRightSuccessDialog: BuyRightSuccessDialog? = null

    private var hasBuy = false

    private val recyclerviewWidth = getScreenPx(BaseApplication.mApplication)[0]-54f.dip2px(BaseApplication.mApplication)

    private var imageMap = mutableMapOf(
        "MoreSwipe" to R.drawable.iv_more_swipe,
        "PremiumBadge" to R.drawable.iv_light_premium,
        "UnlimitedChat" to R.drawable.iv_unlimited_chat,
        "WhoLikesMe" to R.drawable.iv_who_likes,
        "FlashChat" to R.drawable.iv_vip_flash_chat,
        "SecretAlbum" to R.drawable.iv_vip_private_album_,
        "SecretPhoto" to R.drawable.iv_vip_private_photo_,
        "SecretVideo" to R.drawable.iv_vip_private_video_,
        "MyVisitor" to R.drawable.iv_vip_visitor
    )

    private var mCurrentPosition = 1


    private var mVipData: VipData? = null

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
        fun newInstance(data: VipData): BuyVipDialog {
            return BuyVipDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, data.toJson())
                }
            }
        }
    }


    override fun initView() {

        withViewBinding {

            initPayListener()

            llContainer.edgeToEdgeAll()


            if (UserInfoHold.isVip) {

                tvTime.visibility = View.VISIBLE

                tvTime.text =
                    "All privileges acquired\nExpiration date: ${UserInfoHold.userInfo?.vipExpireDate}"

                stvNext.text = "Renewal"

            } else {
                tvTime.visibility = View.GONE

                stvNext.text = "Get Premium"
            }

            if (UserInfoHold.isVip){

                tvCancel.visibility = View.GONE
            }else{
                tvCancel.visibility = View.VISIBLE
            }


            SpanUtils.with(tvPrivacy)
                .append("Terms of Service")
                .setClickSpan(requireContext().getColor(R.color.color_999999), true) {
                    requireContext().createIntent(WebActivity::class.java)
                        .putExtra(
                            AppConstant.Constant.URL,
                            AppConstant.ClientInfo.BASE_SERVICE_POLICY_URL
                        )
                        .putExtra(AppConstant.Constant.TITLE, "User Agreement")
                        .startActivity(requireContext())
                }
                .append("  &  ")
                .append("Privacy Policy.")
                .setClickSpan(requireContext().getColor(R.color.color_999999), true) {
                    requireContext().createIntent(WebActivity::class.java)
                        .putExtra(
                            AppConstant.Constant.URL,
                            AppConstant.ClientInfo.BASE_PRIVACY_POLICY_URL
                        )
                        .putExtra(AppConstant.Constant.TITLE, "Privacy Policy")
                        .startActivity(requireContext())
                }
                .create()



            ivClose.click {

                reportBuyEvent(false)

                dismissAllowingStateLoss()
            }

            stvNext.click {

                val item = mVipAdapter.getItem(mCurrentPosition) ?: return@click

                showLoading(dismissOnBackPressed = false)

                currentTpl = item


                mViewModel.createOrder(item, "Home")

                reportBuyEvent(true)

                reportBuyEvent(item)

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

        }

        if (null != mVipData) {

            val list = mVipData?.privilegeList ?: mutableListOf()

            if (!mVipData?.privilegeList2.isNullOrEmpty()) {

                mVipData?.privilegeList2?.let { list.addAll(it) }
            }


            initRecyclerView(mVipData?.tplList)

            initPrivilegeRecyclerView(list)

            reportShowEvent()
        }



        GooglePayManager.queryINAPPPPurchasesAsync()

        GooglePayManager.querySubsPurchasesAsync()

    }

    fun resetData(mVipData: VipData?) {

        if (null != mVipData) {

            val list = mVipData.privilegeList

            if (!mVipData.privilegeList2.isNullOrEmpty()) {

                mVipData.privilegeList2.let { list.addAll(it) }
            }

            mVipAdapter.submitList(mVipData.tplList)

            initRecyclerView(mVipData.tplList)

            mPrivilegeAdapter.submitList(list)

        }
    }




    private fun initRecyclerView(list: MutableList<Tpl>?) {

        if (list.isNullOrEmpty()) return


        val mItemWidth = recyclerviewWidth/3


        mVipAdapter = object : BaseRecyclerAdapter<Tpl, ItemVipBinding>(ItemVipBinding::inflate) {

            override fun onInitViewHolder(holder: BaseRecyclerViewHolder<ItemVipBinding>) {
                super.onInitViewHolder(holder)


                holder.itemView.updateLayoutParams {

                    width = mItemWidth
                }

            }

            override fun convert(
                holder: BaseRecyclerViewHolder<ItemVipBinding>,
                itemView: ItemVipBinding,
                item: Tpl,
                position: Int
            ) {

                itemView.apply {


                    if (item.ttl != 0) {
                        tvSave.startTimer(item.ttl)
                    } else {
                        tvSave.text =
                            if (item.discountInfo.isNullOrEmpty()) "Original" else item.discountInfo
                    }


                    tvName.text = item.tplName
                    tvPrice.text =
                        if (null == item.formatMoney) "$${item.money}" else "${item.formatMoney}"

                    val formatProductDayPrice = formatProductDayPrice(item.formatMoney, item.count)

                    tvContent.text =
                        if (null == item.formatMoney || null == formatProductDayPrice) "$${item.dayMoney}/day" else "$formatProductDayPrice/day"

                    if (item.isSelect) {
                        sllContainer.shapeDrawableBuilder.setSolidColor(requireContext().getColor(R.color.color_EAA82B))
                            .intoBackground()
                        tvSave.shapeDrawableBuilder.setSolidColor(requireContext().getColor(R.color.color_EAA82B))
                            .intoBackground()
                    } else {
                        sllContainer.shapeDrawableBuilder.setSolidColor(requireContext().getColor(R.color.color_36343A))
                            .intoBackground()
                        tvSave.shapeDrawableBuilder.setSolidColor(requireContext().getColor(R.color.color_666666))
                            .intoBackground()
                    }

                }

            }

        }

        mDialogBinding.recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.HORIZONTAL, false
        )

        mDialogBinding.recyclerView.adapter = mVipAdapter


        mCurrentPosition = list.indexOfFirst { it.defaultTpl == "True" }

        if (mCurrentPosition == -1){

            mCurrentPosition = 1
        }

        if (list.size >= mCurrentPosition) {

            list[mCurrentPosition].isSelect = true

            getDesc(list[mCurrentPosition])
        }

        mVipAdapter.submitList(list)

        currentTpl = mVipAdapter.getItem(mCurrentPosition)

        mVipAdapter.setOnItemClickListener { _, _, position ->

            if (mCurrentPosition == position) {


                mDialogBinding.stvNext.performClick()

                return@setOnItemClickListener
            }

            val item = mVipAdapter.getItem(position) ?: return@setOnItemClickListener

            val lastItem = mVipAdapter.getItem(mCurrentPosition)

            if (null != lastItem) {
                lastItem.isSelect = false

                mVipAdapter.notifyItemChanged(mCurrentPosition, false)
            }


            item.isSelect = true

            mCurrentPosition = position

            getDesc(item)

            mVipAdapter.notifyItemChanged(mCurrentPosition, false)

            mPrivilegeAdapter.notifyItemRangeChanged(0, mPrivilegeAdapter.itemCount, false)


        }

    }


    private fun initPrivilegeRecyclerView(list: MutableList<Privilege>? = mutableListOf()) {

        if (list.isNullOrEmpty()) return


        mPrivilegeAdapter = object :
            BaseRecyclerAdapter<Privilege, ItemVipPrivilegeBinding>(ItemVipPrivilegeBinding::inflate) {
            override fun convert(
                holder: BaseRecyclerViewHolder<ItemVipPrivilegeBinding>,
                itemView: ItemVipPrivilegeBinding,
                item: Privilege,
                position: Int
            ) {

                itemView.apply {

                    tvTitle.text = item.title

                    if (item.remarkList.isNullOrEmpty()) {

                        tvContent.text = item.remark
                    } else {

                        if (mCurrentPosition <= item.remarkList.lastIndex) {
                            tvContent.text = item.remarkList[mCurrentPosition]
                        }
                    }
                    ivImage.setImageResource(imageMap[item.type] ?: R.drawable.iv_special_badge_vip_success)

                }
            }
        }

        mDialogBinding.privilegeRecyclerView.adapter = mPrivilegeAdapter

        mDialogBinding.privilegeRecyclerView.layoutManager = LinearLayoutManager(context)

        mPrivilegeAdapter.submitList(list)


    }


    private fun getDesc(mTpl: Tpl?) {

        if (null == mTpl) return

        SpanUtils.with(mDialogBinding.tvDesc)
            .append("Your subscription will automatically renew every ${mTpl.tplName} for ${if (null == mTpl.formatMoney) "$${mTpl.money}" else "${mTpl.formatMoney}"}.")
            .append(" Cancel  anytime")
            .setForegroundColor(requireContext().getColor(R.color.color_EAA82B))
            .append(" on your Google Play. For more information, visit our:").create()

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

            mPrivilegeList = it.privilegeList

            hasBuy = true

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


    private fun initBuySuccessDialog() {

        dismissAllowingStateLoss()


        if (mPrivilegeList.isNullOrEmpty()) return

        if (null != mBuyVipSuccessDialog && mBuyVipSuccessDialog?.isVisible == true) return

        mBuyVipSuccessDialog = BuyVipSuccessDialog.newInstance(mPrivilegeList!!)

        mBuyVipSuccessDialog?.show(parentFragmentManager)


    }


    private fun initBuySuccessDialog(isThreeLater: Boolean = false) {

        dismissAllowingStateLoss()

        val mTpl = currentTpl

        if (null == mTpl) return


        mTpl.type = AppConstant.Constant.PAY_VIP

        if (null != mBuyRightSuccessDialog && mBuyRightSuccessDialog?.isVisible == true) return

        mBuyRightSuccessDialog = BuyRightSuccessDialog.newInstance(mTpl, isThreeLater)

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


    override fun setDialogHeight(): Int {

        return WindowManager.LayoutParams.MATCH_PARENT
    }


    override fun onDismiss(dialog: DialogInterface) {


        AppConstant.Constant.isShowBuy = false

        if (UserInfoHold.isVip || UserInfoHold.isOrganic || hasBuy) {


        } else {
            val currentTimeMillis = System.currentTimeMillis()


            if ((AppConstant.Constant.LAST_OPEN_VIP_TIME + 1000 * 60 * 5) <= currentTimeMillis) {

                val item = mVipAdapter.getItem(mCurrentPosition)

                item?.let {

                    RetainDialog.newInstance(it, mPrivilegeAdapter.items)
                        .show(parentFragmentManager)

                    AppConstant.Constant.LAST_OPEN_VIP_COUNT = 0
                }

            } else {

                if (AppConstant.Constant.LAST_OPEN_VIP_COUNT > 3) {

                    val item = mVipAdapter.getItem(mCurrentPosition)

                    item?.let {

                        RetainDialog.newInstance(it, mPrivilegeAdapter.items)
                            .show(parentFragmentManager)

                        AppConstant.Constant.LAST_OPEN_VIP_COUNT = 0
                    }
                }

            }

            AppConstant.Constant.LAST_OPEN_VIP_TIME = currentTimeMillis

            AppConstant.Constant.LAST_OPEN_VIP_COUNT++

        }

        GooglePayManager.removeListener(TAG)

        super.onDismiss(dialog)
    }


    private fun reportShowEvent() {

        if (null == mVipData) return

        mVipData?.let {
            val param = mutableMapOf<String, Any?>()
            param["method"] = if (UserInfoHold.isLowUse) mVipShowValue[7] else it.showType
            param["convo_id"] = it.targetId
            param["model_id"] = it.userId2
            param["model_name"] = it.name2
            param["user_type"] = UserInfoHold.isNewUser
            reportEvent(mMessagePayKey[0], param)

        }
    }

    private fun reportBuyEvent(mTpl: Tpl?) {

        mTpl?.let {
            val param = mutableMapOf<String, Any?>()
            param["product_name"] = mTpl.tplName
            param["user_type"] = UserInfoHold.isNewUser
            reportEvent(mMessagePayKey[1], param)

        }
    }

    private fun reportBuyEvent(isBuy: Boolean) {

        currentTpl?.let {
            if (it.discountInfo == "Best Offer") {
                val param = mutableMapOf<String, Any?>()
                param["Button_name"] = if (isBuy) "click_purchaseNow" else "click_close"
                param["user_type"] = UserInfoHold.isNewUser
                reportEvent(MESSAGE_CHAT_KEY[8], param)
            }

        }
    }
}