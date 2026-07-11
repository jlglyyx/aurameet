package com.chat.jolt.dialog

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.core.net.toUri
import com.chat.jolt.BuildConfig
import com.chat.jolt.data.ConfigData
import com.chat.jolt.databinding.DialogUpdateVersionBinding
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.tracking.mMessageUserKey
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.click
import com.chat.lib_common.util.fromJson
import com.chat.lib_common.util.getScreenPx
import com.chat.lib_common.util.toGoogleStore
import com.chat.lib_common.util.toJson
import kotlin.apply
import kotlin.let
import kotlin.text.isNullOrEmpty

class UpdateVersionDialog: BaseDialog<DialogUpdateVersionBinding>(DialogUpdateVersionBinding::inflate) {

    var onConfirm: () -> Unit = {}



    private var mConfigData: ConfigData? = null

    companion object {

        fun newInstance(
            mConfigData: ConfigData
        ): UpdateVersionDialog {

            return UpdateVersionDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, mConfigData.toJson())
                }
            }
        }

    }


    override fun initView() {

        withViewBinding {





            ivClose.click {
                reportUpdateEvent(false)
                dismissAllowingStateLoss()
            }
            tvCancel.click {
                reportUpdateEvent(false)
                dismissAllowingStateLoss()
            }
            tvCommit.click {

                val version = mConfigData?.version ?: return@click

                onConfirm()

                if (version.installUrl.isNullOrEmpty() || version.installUrl == "*"){

                    requireContext().toGoogleStore(BuildConfig.APPLICATION_ID)
                }else{
                    try {
                        val url = version.installUrl
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                            addCategory(Intent.CATEGORY_BROWSABLE)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }

                reportUpdateEvent(true)
            }

        }

    }

    override fun initData() {

        mConfigData = arguments?.getString(AppConstant.Constant.DATA)?.fromJson()



        withViewBinding {

            val version = mConfigData?.version ?: return

            tvContent.text = version.updateContent ?: ""

            ivClose.visibility = if (version.updateType == "Force") View.GONE else View.VISIBLE
            tvCancel.visibility = if (version.updateType == "Force") View.GONE else View.VISIBLE

        }
        reportShowEvent()

    }

    override fun onStart() {
        super.onStart()

        val version = mConfigData?.version
        version?.let {
            val cancel = version.updateType != "Force"

            setCanceledOnTouchOutside(cancel)

            isCancelable = cancel
        }

    }

    override fun setDialogWidth(): Int {
        return getScreenPx(requireContext())[0] * 31 / 40
    }

    override fun setDialogGravity(): Int {
        return Gravity.CENTER
    }


    private fun reportShowEvent() {

        val version = mConfigData?.version ?: return

        val param = mutableMapOf<String, Any?>()

        val type = if (version.updateType == "Force") "low version" else "new version"

        param["m_type"] = type

        reportEvent(mMessageUserKey[4], param)

    }
    private fun reportUpdateEvent(isUpdate:Boolean) {
        val param = mutableMapOf<String, Any?>()
        param["button_name"] = if (isUpdate) "click_update" else "click_cancel"
        reportEvent(mMessageUserKey[5], param)
    }

}