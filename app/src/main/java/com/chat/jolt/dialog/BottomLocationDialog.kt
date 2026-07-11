package com.chat.jolt.dialog

import com.chat.jolt.databinding.DialogViolationBinding
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.getScreenPx

class BottomLocationDialog: BaseDialog<DialogViolationBinding>(DialogViolationBinding::inflate) {


    var onConfirm: () -> Unit = {}

    var onCancel: () -> Unit = {}


    override fun initView() {

        withViewBinding {

            root.edgeToEdgeBottom()

            stvConfirm.click {

                onConfirm()

                dismissAllowingStateLoss()
            }

            ivClose.click {

                onCancel()

                dismissAllowingStateLoss()
            }


        }

    }

    override fun initData() {

    }

    override fun setDialogWidth(): Int {
        return getScreenPx(requireContext())[0]*8/10
    }



}