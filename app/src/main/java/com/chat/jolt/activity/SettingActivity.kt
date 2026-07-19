package com.chat.jolt.activity

import android.view.View
import com.chat.jolt.BuildConfig
import com.chat.jolt.R
import com.chat.jolt.databinding.ActSettingBinding
import com.chat.jolt.dialog.LoginOutDialog
import com.chat.jolt.dialog.NoticeDialog
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.helper.UserInfoHold.loginOut
import com.chat.jolt.viewmodel.MainViewModel
import com.chat.lib_common.activity.BaseActivity
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.tracking.mMessageUserKey
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.click
import com.chat.lib_common.util.copyContent
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.startActivity
import kotlin.jvm.java


class SettingActivity : BaseActivity<ActSettingBinding, MainViewModel>(ActSettingBinding::inflate) {

    override fun initView() {


        withViewBinding {

            root.edgeToEdgeBottom()

            tvEmail.text = UserInfoHold.userInfo?.email?:""


            tvVersion1.text = "V ${BuildConfig.VERSION_NAME}"

            tvBottomEmail.setOnClickListener {


                this@SettingActivity.copyContent(getString(R.string.app_name),tvBottomEmail.text.toString())
            }

            sllAccount.click {

            }

            sllAgreement.click{

                createIntent(WebActivity::class.java)
                    .putExtra(AppConstant.Constant.URL, AppConstant.ClientInfo.BASE_SERVICE_POLICY_URL)
                    .putExtra(AppConstant.Constant.TITLE, "User Agreement").startActivity(this@SettingActivity)
            }
            sllPrivacy.click{

                createIntent(WebActivity::class.java)
                    .putExtra(AppConstant.Constant.URL, AppConstant.ClientInfo.BASE_PRIVACY_POLICY_URL)
                    .putExtra(AppConstant.Constant.TITLE, "Privacy Policy").startActivity(this@SettingActivity)
            }

            sllDelete.click {
                createIntent(DeleteAccountActivity::class.java).startActivity(this@SettingActivity)
            }
            sllLoginOut.click {

                LoginOutDialog().apply {

                    onConfirm = {

                        mViewModel.loginOutApp()
                    }

                }.show(supportFragmentManager)


                reportEvent(mMessageUserKey[3],true)
            }

        }


    }

    override fun initViewModel() {




    }



    override fun initData() {



    }


}