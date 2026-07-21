package com.chat.jolt.dialog

import android.view.Gravity
import com.chat.jolt.databinding.DialogSwipGuideBinding
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.tracking.SWIPE_RIGHT
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.click
import com.chat.lib_common.util.getScreenPx

class SwipGuideDialog: BaseDialog<DialogSwipGuideBinding>(DialogSwipGuideBinding::inflate) {

    var onConfirm: () -> Unit = {}


    override fun initView() {

        withViewBinding {

            ivClose.setOnClickListener {

                dismissAllowingStateLoss()
            }

            tvCommit.click {

                onConfirm()

                dismissAllowingStateLoss()
            }

        }

    }

    override fun initData() {


        reportEvent()

    }


    override fun onStart() {
        super.onStart()
        setCanceledOnTouchOutside(false)
    }

    override fun setDialogWidth(): Int {
        return getScreenPx(requireContext())[0]*8/10
    }

    override fun setDialogGravity(): Int {
        return Gravity.CENTER
    }

    private fun reportEvent() {
        val param = mutableMapOf<String, Any?>()
        param["method"] = "click_letsgo"
        reportEvent(SWIPE_RIGHT, param)
    }
}