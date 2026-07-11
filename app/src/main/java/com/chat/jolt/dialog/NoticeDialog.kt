package com.chat.jolt.dialog

import android.view.Gravity
import com.chat.jolt.databinding.DialogNoticeBinding
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import com.chat.lib_common.util.getScreenPx

class NoticeDialog: BaseDialog<DialogNoticeBinding>(DialogNoticeBinding::inflate) {

    var onConfirm: () -> Unit = {}

    var onCancel: () -> Unit = {}


    override fun initView() {

        withViewBinding {

            tvCancel.click {

                onCancel()
                dismissAllowingStateLoss()
            }
            tvCommit.click {

                onConfirm()

                dismissAllowingStateLoss()
            }


        }

    }

    override fun initData() {

    }

    override fun setDialogWidth(): Int {
        return getScreenPx(requireContext())[0]*8/10
    }

    override fun setDialogGravity(): Int {
        return Gravity.CENTER
    }


}