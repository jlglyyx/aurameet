package com.chat.jolt.dialog

import android.view.Gravity
import com.chat.jolt.databinding.DialogLoginOutBinding
import com.chat.jolt.databinding.DialogNoticeBinding
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.getScreenPx

class LoginOutDialog: BaseDialog<DialogLoginOutBinding>(DialogLoginOutBinding::inflate) {

    var onConfirm: (() -> Unit)? = null

    var onCancel: (() -> Unit)? = null


    override fun initView() {

        withViewBinding {

            root.edgeToEdgeBottom()

            tvCancel.click {

                onCancel?.invoke()
                dismissAllowingStateLoss()
            }
            tvCommit.click {

                onConfirm?.invoke()

                dismissAllowingStateLoss()
            }


        }

    }

    override fun initData() {

    }



    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }


}