package com.chat.jolt.activity

import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.blankj.utilcode.util.SpanUtils
import com.chat.jolt.BuildConfig
import com.chat.jolt.R
import com.chat.jolt.databinding.ActLoginBinding
import com.chat.jolt.dialog.ChangeUrlDialog
import com.chat.jolt.dialog.NoticeDialog
import com.chat.jolt.viewmodel.MainViewModel
import com.chat.lib_common.activity.BaseActivity
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.observe
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.tracking.POPUP_IP_RISK_KEY
import com.chat.lib_common.tracking.POPUP_IP_RISK_VALUE
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.GoogleLoginUtil
import com.chat.lib_common.util.click
import com.chat.lib_common.util.copyContent
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.getCache
import com.chat.lib_common.util.isVpnConnected
import com.chat.lib_common.util.setCache
import com.chat.lib_common.util.showShort
import com.chat.lib_common.util.startActivity


class LoginActivity : BaseActivity<ActLoginBinding, MainViewModel>(ActLoginBinding::inflate) {


    private var mGoogleLoginUtil: GoogleLoginUtil? = GoogleLoginUtil()

    private val googleLogin: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data = result.data

            mGoogleLoginUtil?.handleLoginResult(data, onSuccess = {

                mViewModel.login(AppConstant.Constant.GOOGLE, it, "")

            }, onError = {

                showShort(it)
            })
        }




    override fun initView() {

        mViewBinding.apply {

            tvNotice.edgeToEdgeBottom()


            createText(tvNotice)



            sllEmailLogin.click {


                initPrivacyNoticeDialog{
                    createIntent(LoginEmailActivity::class.java).startActivity(this@LoginActivity)
                }


            }
            sllGoogleLogin.click {


                if (getCache(AppConstant.Constant.IS_VPN,"False") == "True" && isVpnConnected(this@LoginActivity)){

                    initNoticeDialog(false)

                    return@click
                }


                initPrivacyNoticeDialog{

                    val startLogin = mGoogleLoginUtil?.startLogin(this@LoginActivity)

                    startLogin?.let { googleLogin.launch(startLogin) }

                    showLoading()
                }

            }





        }


    }

    override fun initData() {


        val isVpn = intent.getStringExtra(AppConstant.Constant.TYPE)


        if (isVpn == "VPN"){

            initNoticeDialog(false)

        }



    }

    private fun createNoticeText(text:TextView) {

       SpanUtils.with(text).append("Your account did not pass our security verification.If you believe this is a mistake, please contact us at:\n")
            .append("service@jolt-chat.com")
            .setClickSpan(getColor(R.color.color_21EACF), true) {

                this.copyContent(getString(R.string.app_name),"service@jolt-chat.com")

            }
            .create()
    }

    private fun createText(text:TextView){
        SpanUtils.with(text).append("By continuing, you agree to ${getString(R.string.app_name)}'s\n")
            .append("Terms of Service")
            .setUnderline()
            .setClickSpan(getColor(R.color.color_FEFDC4), true) {
                createIntent(WebActivity::class.java)
                    .putExtra(AppConstant.Constant.URL, AppConstant.ClientInfo.BASE_SERVICE_POLICY_URL)
                    .putExtra(AppConstant.Constant.TITLE, "User Agreement").startActivity(this@LoginActivity)
            }
            .append(" and ")
            .append("Privacy Policy.")
            .setClickSpan(getColor(R.color.color_FEFDC4), true) {
                createIntent(WebActivity::class.java)
                    .putExtra(AppConstant.Constant.URL, AppConstant.ClientInfo.BASE_PRIVACY_POLICY_URL)
                    .putExtra(AppConstant.Constant.TITLE, "Privacy Policy").startActivity(this@LoginActivity)
            }
            .create()
    }

    override fun initViewModel() {

        mViewModel.mUserInfoData.observe(this) {

            dismissLoading()

            if (it.firstLogin == "True") {

                createIntent(BirthStepActivity::class.java).startActivity(this, true)

            } else {

                createIntent(MainActivity::class.java).startActivity(this, true)
            }

        }

        mViewModel.requestFailEvent.observe(this) {

            dismissLoading()

            if (it is Boolean){
                initNoticeDialog(true)
            }

        }

        FlowBus.with(AppConstant.EventConstant.EVENT_FINISH,0).observe(this){

            finish()
        }
    }

    private fun initNoticeDialog(isShuMei: Boolean) {


        NoticeDialog().apply {

            initView = { dialog, mViewBinding ->

                mViewBinding.tvTitle.visibility = View.GONE
               createNoticeText(mViewBinding.tvContent)
                mViewBinding.tvCancel.visibility = View.GONE
                mViewBinding.tvCommit.text = "OK"

            }

        }.show(supportFragmentManager)

        if (isShuMei){
            reportEvent(POPUP_IP_RISK_KEY, POPUP_IP_RISK_VALUE[0])
        }else{
            reportEvent(POPUP_IP_RISK_KEY, POPUP_IP_RISK_VALUE[1])
        }

    }
    private fun initPrivacyNoticeDialog(onSuccess: () -> Unit) {

        val cache = getCache(AppConstant.Constant.LOGIN_NOTICE, false)

        if (cache) {
            onSuccess()
            return
        }

        setCache(AppConstant.Constant.LOGIN_NOTICE, true)

    }





    override fun onDestroy() {


        mGoogleLoginUtil?.remove()

        mGoogleLoginUtil = null

        super.onDestroy()
    }
}