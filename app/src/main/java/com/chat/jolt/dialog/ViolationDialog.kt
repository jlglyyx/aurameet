package com.chat.jolt.dialog

import android.view.Gravity
import android.view.WindowManager
import com.chat.jolt.databinding.DialogViolationBinding
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom

class ViolationDialog: BaseDialog<DialogViolationBinding>(DialogViolationBinding::inflate) {


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


    override fun setDialogGravity():Int{

        return Gravity.BOTTOM
    }

    override fun setDialogWidth():Int{

        return WindowManager.LayoutParams.MATCH_PARENT
    }


}