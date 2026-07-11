package com.chat.jolt.activity

import com.chat.jolt.databinding.ActDeleteAccountBinding
import com.chat.jolt.dialog.NoticeDialog
import com.chat.jolt.helper.UserInfoHold.loginOut
import com.chat.jolt.viewmodel.UserViewModel
import com.chat.lib_common.activity.BaseActivity
import com.chat.lib_common.tracking.mMessageUserKey
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom


class DeleteAccountActivity : BaseActivity<ActDeleteAccountBinding, UserViewModel>(ActDeleteAccountBinding::inflate) {

    private var mNoticeDialog: NoticeDialog? = null




    override fun initView() {

        mViewBinding.root.edgeToEdgeBottom()

        mViewBinding.llCheck.setOnClickListener {

            mViewBinding.cbCheck.isChecked = !mViewBinding.cbCheck.isChecked

            mViewBinding.stvDelete.isEnabled = mViewBinding.cbCheck.isChecked
        }


        mViewBinding.stvDelete.click {

            if (mViewBinding.cbCheck.isChecked) {

                mViewModel.destroyCheck()

            }
        }
    }

    override fun initData() {


    }

    override fun initViewModel() {

        mViewModel.mDeleteAccountNoticeStatus.observe(this) {

            initNoticeDialog(it.msg)
        }
        mViewModel.mDeleteAccountStatus.observe(this) {

            loginOut(this)
        }

    }


    private fun initNoticeDialog(content: String) {

        try {
            mNoticeDialog?.dismissAllowingStateLoss()

            mNoticeDialog = NoticeDialog().apply {

                initView = { dialog, mViewBinding ->
                    mViewBinding.tvTitle.text = "Delete account"
                    mViewBinding.tvContent.text = content
                }

                onConfirm = {
                    mViewModel.destroyAcct()

                    val params = mutableMapOf<String, Any?>()
                    params["button_name"] = "click_delete"
                    reportEvent(mMessageUserKey[2],params)

                }

                onCancel ={
                    val params = mutableMapOf<String, Any?>()
                    params["button_name"] = "click_cancel"
                    reportEvent(mMessageUserKey[2],params)
                }
            }

            mNoticeDialog?.show(supportFragmentManager)

            reportEvent(mMessageUserKey[1],true)

        } catch (e: Exception) {

            e.printStackTrace()
        }

    }


}