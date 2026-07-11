package com.chat.jolt.dialog

import android.view.Gravity
import com.chat.jolt.databinding.DialogQuickFlashBinding
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import kotlin.text.isBlank
import kotlin.text.trim
import kotlin.toString

class QuickFlashDialog : BaseDialog<DialogQuickFlashBinding>(DialogQuickFlashBinding::inflate) {

    var onConfirm: (String) -> Unit = {}

    companion object {
        fun newInstance(): QuickFlashDialog {
            return QuickFlashDialog()
        }
    }


    override fun initView() {


        withViewBinding {


            ivClose.click {

                dismissAllowingStateLoss()

            }

            tvSave.click {

                val toString = setText.text.toString()

                if (toString.isBlank()) return@click

                onConfirm(toString.trim())

                dismissAllowingStateLoss()
            }

        }

    }

    override fun initData() {
    }

    override fun setDialogGravity(): Int {
        return Gravity.CENTER
    }


}