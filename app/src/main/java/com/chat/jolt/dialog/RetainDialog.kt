package com.chat.jolt.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.chat.jolt.activity.PayWebActivity
import com.chat.jolt.data.Privilege
import com.chat.jolt.data.Tpl
import com.chat.jolt.databinding.DialogRetainBinding
import com.chat.jolt.databinding.ItemRetainNoticeBinding
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.viewmodel.PublicViewModel
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.tracking.MESSAGE_CHAT_KEY
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.GooglePayManager
import com.chat.lib_common.util.click
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.formatListJson
import com.chat.lib_common.util.fromJson
import com.chat.lib_common.util.getCache
import com.chat.lib_common.util.setCache
import com.chat.lib_common.util.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RetainDialog : BaseDialog<DialogRetainBinding>(DialogRetainBinding::inflate) {

    private val mViewModel by sharedViewModels<PublicViewModel>()

    private var mTpl: Tpl? = null

    private var mShowPrivilegeList: MutableList<Privilege>? = null

    private var mPrivilegeList: List<Privilege>? = null

    private var bizId: Int? = null

    private var mBuyVipSuccessDialog: BuyVipSuccessDialog? = null

    private var mBuyRightSuccessDialog: BuyRightSuccessDialog? = null

    private lateinit var mPrivilegeAdapter: BaseRecyclerAdapter<Privilege, ItemRetainNoticeBinding>

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
        fun newInstance(data: Tpl, privilegeList: List<Privilege>): RetainDialog {
            return RetainDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, data.toJson())
                    putString(AppConstant.Constant.PRIVILEGE_DATA, privilegeList.toJson())
                }
            }
        }
    }

    override fun initView() {

        initPayListener()

        withViewBinding {

            root.edgeToEdgeBottom()

            ivClose.click {

                reportEvent(true)

                dismissAllowingStateLoss()
            }
            stvConfirm.click {

                reportEvent(false)

                mTpl?.let { mTpl ->

                    showLoading(dismissOnBackPressed = false)

                    mViewModel.createOrder(mTpl, "Home")

                }


            }
        }

    }

    override fun initData() {

        arguments?.let {

            val data = it.getString(AppConstant.Constant.DATA, "")

            val privilegeData = it.getString(AppConstant.Constant.PRIVILEGE_DATA, "")

            if (data.isNotEmpty()) {

                mTpl = data.fromJson()

            }
            if (privilegeData.isNotEmpty()) {

                mShowPrivilegeList = privilegeData.formatListJson()

                initPrivilegeRecyclerView(mShowPrivilegeList)
            }

            mTpl?.let { mTpl ->

                mDialogBinding.stvConfirm.text =
                    if (null == mTpl.formatMoney) "Continue ($${mTpl.money})" else "Continue (${mTpl.formatMoney})"
            }

        }
        reportEvent(MESSAGE_CHAT_KEY[6], true)
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

                mViewModel.payOrderTest(it.bizId, mTpl?.money ?: "0.0")
            }

        }


        mViewModel.mOrderInfoData.observe(this) {

            mPrivilegeList = it.privilegeList

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

        if (null == mTpl) return

        if (null != mBuyRightSuccessDialog && mBuyRightSuccessDialog?.isVisible == true) return

        mBuyRightSuccessDialog = BuyRightSuccessDialog.newInstance(mTpl!!,isThreeLater)

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
                            mTpl?.money ?: "0.0",
                            mPurchase,
                            type
                        )
                    }

                } else {

                    mViewModel.payOrderGoogle(bizId!!, mTpl?.money ?: "0.0", mPurchase, type)

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



    private fun initPrivilegeRecyclerView(list: MutableList<Privilege>? = mutableListOf()) {

        if (list.isNullOrEmpty()) return

        mPrivilegeAdapter = object : BaseRecyclerAdapter<Privilege, ItemRetainNoticeBinding>(ItemRetainNoticeBinding::inflate) {
            override fun convert(
                holder: BaseRecyclerViewHolder<ItemRetainNoticeBinding>,
                itemView: ItemRetainNoticeBinding,
                item: Privilege,
                position: Int
            ) {

                itemView.apply {

                    tvText.text = item.title

                }
            }
        }

        mDialogBinding.recyclerView.adapter = mPrivilegeAdapter

        mDialogBinding.recyclerView.layoutManager = GridLayoutManager(context,2)

        mPrivilegeAdapter.submitList(list)


    }


    private fun reportEvent(isCancel: Boolean){

        val params = mutableMapOf<String, Any?>()

        params["Button_name"] = if (isCancel) "click_close" else "click_getPremium"

        params["product_name"] = mTpl?.tplName

        params["user_type"] = UserInfoHold.isNewUser

        reportEvent(MESSAGE_CHAT_KEY[7], params)
    }

    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }

    override fun onDismiss(dialog: DialogInterface) {

        val hasShowLimit = getCache(AppConstant.Constant.HAS_SHOW_LIMIT, false)

        if (!hasShowLimit) {

            val timeLimitPremiumTtl = UserInfoHold.userInfo?.timeLimitPremiumTtl ?: 0

            if (timeLimitPremiumTtl > 0) {

                setCache(AppConstant.Constant.HAS_SHOW_LIMIT, true)

                LimitTimeDialog.newInstance().show(parentFragmentManager)
            }
        }
        GooglePayManager.removeListener(TAG)
        super.onDismiss(dialog)
    }
}