package com.chat.jolt.dialog

import android.view.Gravity
import com.chat.jolt.databinding.DialogAlbumNoticeBinding

import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.tracking.MESSAGE_CHAT_KEY
import com.chat.lib_common.tracking.mRightShowValue
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom

class AlbumNoticeDialog: BaseDialog<DialogAlbumNoticeBinding>(DialogAlbumNoticeBinding::inflate) {

    var onConfirm: () -> Unit = {}

    override fun initView() {

        withViewBinding {

            root.edgeToEdgeBottom()

            stvConfirm.click {

                onConfirm()

                dismissAllowingStateLoss()
            }

            ivClose.click {

                dismissAllowingStateLoss()
            }

        }

    }

    override fun initData() {


    }

    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }


    fun reportEvent(type: Int) {
        val param = mutableMapOf<String, Any?>()
        param["method"] = mRightShowValue[type]
        reportEvent(MESSAGE_CHAT_KEY[2], param)
    }

}