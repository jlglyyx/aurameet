package com.chat.jolt.dialog

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.view.Gravity
import com.chat.jolt.databinding.DialogChangeUrlBinding
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.setCache
import java.lang.ref.WeakReference
import kotlin.apply
import kotlin.let
import kotlin.system.exitProcess

class ChangeUrlDialog: BaseDialog<DialogChangeUrlBinding>(DialogChangeUrlBinding::inflate) {


    override fun initView() {

        mDialogBinding.apply {

            root.edgeToEdgeBottom()

            tvTv0.text = "${AppConstant.ClientInfo.BASE_DEV_URL_183}"

            tvTv1.text = "${AppConstant.ClientInfo.BASE_DEV_URL}"

            tvTv2.text = "${AppConstant.ClientInfo.BASE_TEST_URL}"

            tvTv3.text = "${AppConstant.ClientInfo.BASE_REAL_URL}"


            tvTv0.click {

                setCache(AppConstant.Constant.URL,AppConstant.ClientInfo.BASE_DEV_URL_183)

                dismiss()


                restartApp()

            }
            tvTv1.click {

                setCache(AppConstant.Constant.URL,AppConstant.ClientInfo.BASE_DEV_URL)

                dismiss()


                restartApp()

            }
            tvTv2.click {

                setCache(AppConstant.Constant.URL,AppConstant.ClientInfo.BASE_TEST_URL)

                dismiss()


                restartApp()
            }
            tvTv3.click {

                setCache(AppConstant.Constant.URL,AppConstant.ClientInfo.BASE_REAL_URL)

                dismiss()


                restartApp()
            }

        }
    }

    override fun initData() {

    }


    private fun restartApp() {

        val packageManager = requireActivity().packageManager
        val intent = packageManager.getLaunchIntentForPackage(requireActivity().packageName)
        intent?.let {
            it.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NEW_TASK
            )

            val activityRef = WeakReference(activity)

            Handler(Looper.getMainLooper()).postDelayed({
                activityRef.get()?.startActivity(it)
                activityRef.get()?.finish()
                Process.killProcess(Process.myPid())
                exitProcess(0)
            }, 1000)
        }

    }


    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }

}