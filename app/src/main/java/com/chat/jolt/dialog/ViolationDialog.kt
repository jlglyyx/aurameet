package com.chat.jolt.dialog

import android.view.Gravity
import android.view.WindowManager
import com.chat.jolt.databinding.DialogViolationBinding
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom

class ViolationDialog: BaseDialog<DialogViolationBinding>(DialogViolationBinding::inflate) {


    var onConfirm: (() -> Unit)? = null

    var onCancel: (() -> Unit)? = null


    override fun initView() {

        withViewBinding {

            root.edgeToEdgeBottom()

            stvConfirm.click {

                onConfirm?.invoke()

                dismissAllowingStateLoss()
            }

            ivClose.click {

                onCancel?.invoke()

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