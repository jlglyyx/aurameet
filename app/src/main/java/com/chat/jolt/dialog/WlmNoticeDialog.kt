package com.chat.jolt.dialog

import android.os.Bundle
import android.view.Gravity
import com.bumptech.glide.request.RequestOptions
import com.chat.jolt.data.CustomMessageExtraData
import com.chat.jolt.databinding.DialogWlmNoticeBinding
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import com.chat.lib_common.util.dip2px
import com.chat.lib_common.util.fromJson
import com.chat.lib_common.util.getScreenPx
import com.chat.lib_common.util.loadOptionImage
import com.chat.lib_common.util.toJson
import com.chat.lib_common.widget.BlurTransformation

class WlmNoticeDialog: BaseDialog<DialogWlmNoticeBinding>(DialogWlmNoticeBinding::inflate) {

    var onConfirm: () -> Unit = {}

    private val avatarWidth = 40f.dip2px(BaseApplication.mApplication)

    private val avatarHeight = avatarWidth

    private val requestOptions =
        RequestOptions.bitmapTransform(BlurTransformation(10, 2))


    companion object {
        fun newInstance(data: CustomMessageExtraData): WlmNoticeDialog {
            return WlmNoticeDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, data.toJson())
                }
            }
        }
    }

    override fun initView() {

        withViewBinding {


            ivClose.click {

                dismissAllowingStateLoss()
            }

            stvConfirm.click {

                onConfirm()

                dismissAllowingStateLoss()
            }
        }


    }

    override fun initData() {

        arguments?.let {

            val data = it.getString(AppConstant.Constant.DATA)

            if (!data.isNullOrEmpty()){

                val mCustomMessageExtraData = data.fromJson<CustomMessageExtraData>()

                val messageExtraData = mCustomMessageExtraData.data

                withViewBinding {

                    messageExtraData?.let {
                        tvName.text =  messageExtraData.name2 + ",${messageExtraData.age2}"

                        ivAvatar.loadOptionImage(ivAvatar.context,messageExtraData.headPic2,requestOptions,avatarWidth,avatarHeight)
                    }


                }
            }
        }

    }

    override fun setDialogWidth(): Int {
        return getScreenPx(requireContext())[0]*8/10
    }

    override fun setDialogGravity(): Int {
        return Gravity.CENTER
    }
}