package com.chat.jolt.dialog

import android.view.Gravity
import com.chat.jolt.databinding.DialogExposureBinding
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom

class ExposureDialog: BaseDialog<DialogExposureBinding>(DialogExposureBinding::inflate) {


    override fun initView() {

        withViewBinding {

            root.edgeToEdgeBottom()

            ivClose.click {

                dismissAllowingStateLoss()
            }


            tvCommit.click {


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