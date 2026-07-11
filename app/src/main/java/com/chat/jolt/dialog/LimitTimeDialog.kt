package com.chat.jolt.dialog

import android.content.DialogInterface
import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.chat.jolt.R
import com.chat.jolt.activity.PayWebActivity
import com.chat.jolt.data.Privilege
import com.chat.jolt.data.Tpl
import com.chat.jolt.databinding.DialogLimitTimeBinding
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.viewmodel.PublicViewModel
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.GooglePayManager
import com.chat.lib_common.util.click
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.edgeToEdgeTop
import com.chat.lib_common.util.getColor
import com.chat.lib_common.util.getTimeSecond
import com.chat.lib_common.util.symbolToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LimitTimeDialog : BaseDialog<DialogLimitTimeBinding>(DialogLimitTimeBinding::inflate) {

    private val mViewModel by sharedViewModels<PublicViewModel>()

    private var mTpl: Tpl? = null

    private var mPrivilegeList: List<Privilege>? = null

    private var bizId: Int? = null

    private var mBuyVipSuccessDialog: BuyVipSuccessDialog? = null

    private var mBuyRightSuccessDialog: BuyRightSuccessDialog? = null

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
        fun newInstance(): LimitTimeDialog {
            return LimitTimeDialog().apply {
                arguments = Bundle().apply {
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            it.navigationBarColor = getColor(R.color.appColor)

        }
    }

    override fun initView() {

        initPayListener()

        withViewBinding {

            llClose.edgeToEdgeTop()

            root.edgeToEdgeBottom()

            llClose.click {

                dismissAllowingStateLoss()
            }
            stvConfirm.click {

                mTpl?.let { mTpl ->

                    if (mTpl.productId.isNullOrEmpty()) return@click

                    showLoading(dismissOnBackPressed = false)

                    mViewModel.createOrder(mTpl, "Home")

                }


            }
        }

    }

    override fun initData() {


        mViewModel.getTimeLimitInfo()

    }


    override fun initViewModel() {
        super.initViewModel()


        mViewModel.mTpl.observe(this) {

            mTpl = it

            initPayInfo(it)
        }

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

    private fun initPayInfo(mTpl: Tpl) {


        withViewBinding {

            tvPrice.text = "$${mTpl.prePrice}"

//            tvName.text = "$${mTpl.tplName}"


            tvDesc.text = "instead of\n$${mTpl.oriPrice} per week"

            tvDesc.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG

            stvConfirm.text = "Purchase Now (${mTpl.dayUnit ?: "0.0"}/Day)"

            initTimer(mTpl.ttl)

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

        mBuyRightSuccessDialog = BuyRightSuccessDialog.newInstance(mTpl!!, isThreeLater)

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

    private fun initTimer(time: Int) {

        var mTime = time

        if (mTime <= 0) return

        lifecycleScope.launch {

            while (isActive) {

                if (mTime <= 0) {

                    cancel()
                } else {

                    if (isVisible) {
                        setTime(mTime)
                    }

                    delay(1000)
                }
                mTime--

            }

        }
    }

    private fun setTime(time: Int) {
        try {
            val timeSecond = getTimeSecond(time, true)

            val symbolToList = timeSecond.symbolToList(":")

            if (symbolToList.size >= 3) {

                mDialogBinding.tvHour.text = symbolToList[0]
                mDialogBinding.tvMinute.text = symbolToList[1]
                mDialogBinding.tvSecond.text = symbolToList[2]
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        Log.i(TAG, "initPayInfo: $timeSecond")
    }

    override fun setDialogHeight(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }

    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }

    override fun onDismiss(dialog: DialogInterface) {
        GooglePayManager.removeListener(TAG)
        super.onDismiss(dialog)
    }
}