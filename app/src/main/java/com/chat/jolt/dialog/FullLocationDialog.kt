package com.chat.jolt.dialog

import android.view.WindowManager
import com.chat.jolt.databinding.DialogFullLocationBinding
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeAll

class FullLocationDialog: BaseDialog<DialogFullLocationBinding>(DialogFullLocationBinding::inflate) {

    var onConfirm: () -> Unit = {}


    override fun initView() {

        withViewBinding {


            root.edgeToEdgeAll()


            tvCancel.click {

                dismissAllowingStateLoss()
            }

            stvConfirm.click {


                onConfirm()


                dismissAllowingStateLoss()

            }

        }

    }

    override fun initData() {
    }


    override fun setDialogHeight(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }


}