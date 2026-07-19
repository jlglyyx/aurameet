package com.chat.jolt.dialog

import com.chat.jolt.databinding.DialogOpenNoticeBinding
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.tracking.mPopPopupDialogKey
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.click
import com.chat.lib_common.util.getScreenPx

class OpenNoticeDialog: BaseDialog<DialogOpenNoticeBinding>(DialogOpenNoticeBinding::inflate) {


    var onConfirm: () -> Unit = {}

    var onCancel: () -> Unit = {}


    override fun initView() {

        withViewBinding {

            stvConfirm.click {

                reportEvent(false)

                onConfirm()

                dismissAllowingStateLoss()
            }

            stvCancel.click {

                reportEvent(true)

                onCancel()

                dismissAllowingStateLoss()
            }


        }

    }

    override fun initData() {

        reportEvent(mPopPopupDialogKey[5], true)
    }

    override fun setDialogWidth(): Int {
        return getScreenPx(requireContext())[0]*8/10
    }

    private fun reportEvent(isCancel: Boolean){

        val params = mutableMapOf<String, Any?>()

        params["button_name"] = if (isCancel) "click_maybeLater" else "click_enable"

        reportEvent(mPopPopupDialogKey[6],params)
    }

}