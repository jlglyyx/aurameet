package com.chat.jolt.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import com.chat.jolt.R
import com.chat.jolt.data.Tpl
import com.chat.jolt.databinding.DialogBuyRightSuccessBinding

import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.fromJson
import com.chat.lib_common.util.toJson
import kotlin.apply
import kotlin.let
import kotlin.text.isNullOrEmpty

class BuyRightSuccessDialog: BaseDialog<DialogBuyRightSuccessBinding>(DialogBuyRightSuccessBinding::inflate) {


    private var mTpl: Tpl? = null

    // success but later
    private var isThreePay: Boolean = false

    companion object {
        fun newInstance(data: Tpl,isThreePay: Boolean): BuyRightSuccessDialog {
            return BuyRightSuccessDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, data.toJson())
                    putBoolean(AppConstant.Constant.IS_THREE_PAY, isThreePay)
                }
            }
        }
    }


    override fun initView() {

        withViewBinding {

            root.edgeToEdgeBottom()

            ivClose.click {

                dismissAllowingStateLoss()
            }


            stvConfirm.click {

                dismissAllowingStateLoss()
            }

        }

    }

    override fun initData() {

        AppConstant.Constant.isShowBuy = true

        arguments?.let {

            val data = it.getString(AppConstant.Constant.DATA, "")

            isThreePay = it.getBoolean(AppConstant.Constant.IS_THREE_PAY, false)

            if (!data.isNullOrEmpty()){
                mTpl = data.fromJson<Tpl>()
            }
        }

        withViewBinding {

            mTpl?.let {

                when (it.type) {

                    AppConstant.Constant.PAY_VIP -> {
                        tvContent.text = "Hi, tiger! it will take 1-5 minutes for your benefits to becredited, thanks for your patience."
                    }
                    AppConstant.Constant.PAY_FLASH_CHAT -> {
                        tvContent.text = "You've received ${it.count} FlashChat opportunities."
                    }
                    AppConstant.Constant.PAY_PRIVATE_PHOTO -> {
                        tvContent.text = "You've received ${it.count} PrivatePhoto opportunities."
                    }
                    AppConstant.Constant.PAY_PRIVATE_VIDEO -> {
                        tvContent.text = "You've received ${it.count} PrivateVideo opportunities."
                    }
                }

            }

            if (isThreePay){
                tvContent.text = "Hi, tiger! it will take 1-5 minutes for your benefits to becredited, thanks for your patience."
            }

        }
    }

    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        AppConstant.Constant.isShowBuy = false
    }

}