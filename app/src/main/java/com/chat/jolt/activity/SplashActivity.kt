package com.chat.jolt.activity

import android.view.LayoutInflater
import com.blankj.utilcode.util.AppUtils
import com.chat.jolt.BuildConfig
import com.chat.jolt.R
import com.chat.jolt.databinding.ActSplashBinding
import com.chat.jolt.databinding.ViewSplashNoNetBinding
import com.chat.jolt.dialog.ChangeUrlDialog
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.viewmodel.MainViewModel
import com.chat.lib_common.activity.BaseActivity
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.observe
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.tracking.updateSolarEngineUser
import com.chat.lib_common.util.click
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.isVpnConnected
import com.chat.lib_common.util.startActivity
import com.chat.lib_common.widget.ErrorReLoadView
import java.net.SocketException
import java.net.UnknownHostException
import kotlin.jvm.java


class SplashActivity : BaseActivity<ActSplashBinding, MainViewModel>(ActSplashBinding::inflate) {

    override fun initView() {


        mViewBinding.apply {

            errorReLoadView.addNoNetView { viewGroup ->
                ViewSplashNoNetBinding.inflate(LayoutInflater.from(this@SplashActivity), viewGroup, true)
                    .apply {

                        stvNext.click {

                            initConfig()
                        }

                        ivImage.setOnLongClickListener {

                            if (BuildConfig.DEBUG) {
                                ChangeUrlDialog().show(supportFragmentManager)
                            }

                            return@setOnLongClickListener true
                        }
                    }
            }

        }


    }

    override fun initViewModel() {

        mViewModel.mHobbyTagData.observe(this){


        }
        mViewModel.mConfigData.observe(this){


            if (null == UserInfoHold.userInfo) {

                createIntent(LoginActivity::class.java)
                    .startActivity(this, true)


                return@observe

            }

            if (it.vpnSwitch == "True" && isVpnConnected(this)) {

                createIntent(LoginActivity::class.java).putExtra(AppConstant.Constant.TYPE, "VPN")
                    .startActivity(this, true)

                return@observe

            }


            createIntent(MainActivity::class.java).startActivity(this, true)

            overridePendingTransition(0, R.anim.fade_out)

        }

        mViewModel.mSplashErrorStatus.observe(this) {

            dismissLoading()

            if (it is UnknownHostException || it is SocketException){

                mViewBinding.errorReLoadView.showStatusView(ErrorReLoadView.Status.NO_NETWORK)

            }


        }

        mViewModel.requestFailEvent.observe(this) {

            dismissLoading()

            if (it is Boolean){
                initNoticeDialog()
            }


        }


        FlowBus.with(AppConstant.EventConstant.APP_INIT_ADJUST).observe(this){

            if (it is String){
                mViewModel.installInfo(it)
            }

        }

    }



    override fun initData() {

        initConfig()

        mViewModel.installInfo(null)

        updateSolarEngineUser("useapp_version","V${AppUtils.getAppVersionName()}")
    }


    private fun initConfig() {

        mViewModel.findNews()

        mViewModel.configInfo()

        mViewModel.initTag()

        mViewModel.initTag2()

        mViewModel.initProfession()

        mViewModel.initSocialAim()

    }

    private fun initNoticeDialog() {


        createIntent(LoginActivity::class.java).putExtra(AppConstant.Constant.TYPE, "VPN")
            .startActivity(this, true)


    }
}