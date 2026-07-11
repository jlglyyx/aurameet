package com.chat.jolt.dialog

import android.view.Gravity
import com.chat.jolt.databinding.DialogChatMoreBinding
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom
import kotlin.apply

class ChatMoreDialog: BaseDialog<DialogChatMoreBinding>(DialogChatMoreBinding::inflate) {



    var onConfirm: () -> Unit = {}


    override fun initView() {

        withViewBinding {

            root.edgeToEdgeBottom()

            tvCancel.click {

                dismissAllowingStateLoss()
            }

            tvBlock.click {

                initReportDialog()
            }


        }

    }

    override fun initData() {
    }


    private fun initReportDialog(){


       val mNoticeDialog = NoticeDialog().apply {

            initView = { dialog, mViewBinding ->
                mViewBinding.tvTitle.text = "Block"
                mViewBinding.tvContent.text = "Confirm to blacklist this user"
            }

            onConfirm = {
                this@ChatMoreDialog.onConfirm()
                this@ChatMoreDialog.dismissAllowingStateLoss()
            }
        }

        mNoticeDialog.show(parentFragmentManager)

    }

    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }


}