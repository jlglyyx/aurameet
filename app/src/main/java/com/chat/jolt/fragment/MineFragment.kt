package com.chat.jolt.fragment

import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.chat.jolt.BuildConfig
import com.chat.jolt.R
import com.chat.jolt.activity.DeleteAccountActivity
import com.chat.jolt.activity.EditUserInfoActivity
import com.chat.jolt.activity.FeedBackActivity
import com.chat.jolt.activity.VisitorActivity
import com.chat.jolt.activity.WebActivity
import com.chat.jolt.data.UserInfoData
import com.chat.jolt.data.VipData
import com.chat.jolt.databinding.FraMineBinding
import com.chat.jolt.dialog.AlbumNoticeDialog
import com.chat.jolt.dialog.BuyRightDialog
import com.chat.jolt.dialog.BuyVipDialog
import com.chat.jolt.dialog.LoginOutDialog
import com.chat.jolt.dialog.SendMediaDialog
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.viewmodel.UserViewModel
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.observe
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.fragment.BaseFragment
import com.chat.lib_common.tracking.mMessageUserKey
import com.chat.lib_common.tracking.mRightShowValue
import com.chat.lib_common.tracking.mVipShowValue
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.click
import com.chat.lib_common.util.copyContent
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.dip2px
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.getTimeSecond
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.startActivity
import com.chat.lib_common.util.symbolToList
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.jvm.java

class MineFragment : BaseFragment<FraMineBinding, UserViewModel>(FraMineBinding::inflate) {

    private var mBuyVipDialog: BuyVipDialog? = null

    private var mBuyRightDialog: BuyRightDialog? = null

    private var mSendMediaDialog: SendMediaDialog? = null

    private var timeJob: Job? = null


    override fun initView() {

        withViewBinding {



            llUserInfo.click {

                requireContext().createIntent(EditUserInfoActivity::class.java)
                    .startActivity(requireContext())
            }
            ivEdit.click {

                requireContext().createIntent(EditUserInfoActivity::class.java)
                    .startActivity(requireContext())
            }
            ivAlbum.click {

                initSendMediaDialog()
            }
            ivFeedback.click {

                requireContext().createIntent(FeedBackActivity::class.java)
                    .startActivity(requireContext())
            }




            ivVip.click {

                mViewModel.getVipInfo(AppConstant.Constant.PAY_VIP, mVipShowValue[9],"PremiumBadge")
            }


            sllVip.click {

                mViewModel.getVipInfo(AppConstant.Constant.PAY_VIP,mVipShowValue[2],"PremiumBadge")
            }

            clLimitTime.click {

                mViewModel.getVipInfo(AppConstant.Constant.PAY_VIP,mVipShowValue[2],"PremiumBadge")

            }

            stvSee.click {

                mViewModel.getVipInfo(AppConstant.Constant.PAY_VIP,mVipShowValue[2],"PremiumBadge")

            }

            clPhoto.click {
                if (UserInfoHold.isVip) {
                    mViewModel.getVipInfo(AppConstant.Constant.PAY_PRIVATE_PHOTO, mVipShowValue[0],"PremiumBadge")
                } else {
                    mViewModel.getVipInfo(AppConstant.Constant.PAY_VIP,mVipShowValue[2],"SecretPhoto")
                }


            }
            clVideo.click {
                if (UserInfoHold.isVip) {
                    mViewModel.getVipInfo(AppConstant.Constant.PAY_PRIVATE_VIDEO,mVipShowValue[0],"PremiumBadge")
                } else {
                    mViewModel.getVipInfo(AppConstant.Constant.PAY_VIP,mVipShowValue[2],"SecretVideo")
                }


            }

            clFlashChat.click {

                mViewModel.getVipInfo(AppConstant.Constant.PAY_FLASH_CHAT, mRightShowValue[0],"PremiumBadge")

            }

            sclVisited.click {
                requireContext().createIntent(VisitorActivity::class.java)
                    .startActivity(requireContext())
            }




            tvEmail.text = UserInfoHold.userInfo?.email?:""


            tvVersion.text = "V ${BuildConfig.VERSION_NAME}"

            tvBottomEmail.setOnClickListener {

                requireActivity().copyContent(getString(R.string.app_name),tvBottomEmail.text.toString())
            }

            sllAccount.click {

            }

            sllAgreement.click{

                createIntent(WebActivity::class.java)
                    .putExtra(AppConstant.Constant.URL, AppConstant.ClientInfo.BASE_SERVICE_POLICY_URL)
                    .putExtra(AppConstant.Constant.TITLE, "User Agreement").startActivity(requireActivity())
            }
            sllPrivacy.click{

                createIntent(WebActivity::class.java)
                    .putExtra(AppConstant.Constant.URL, AppConstant.ClientInfo.BASE_PRIVACY_POLICY_URL)
                    .putExtra(AppConstant.Constant.TITLE, "Privacy Policy").startActivity(requireActivity())
            }

            sllDelete.click {
                createIntent(DeleteAccountActivity::class.java).startActivity(requireActivity())
            }
            sllLoginOut.click {

                LoginOutDialog().apply {

                    onConfirm = {

                        mViewModel.loginOutApp()
                    }

                }.show(parentFragmentManager)


            }

        }


    }

    override fun onResume() {
        super.onResume()

        mViewModel.getUserInfo()
        mViewModel.getDataInfo()
    }

    override fun initData() {


    }

    override fun initViewModel() {

        mViewModel.mUserInfoData.observe(this) {


            initUserInfo(it)


        }
        mViewModel.mPreviewUserInfoData.observe(this) {


            initProgress(it)

        }

        FlowBus.with(AppConstant.EventConstant.EVENT_IS_BUY_GET_USER_INFO).observe(this) {

            if (it is UserInfoData){

                initUserInfo(it)

            }



        }


        mViewModel.mVipData.observe(this) {

            when (it.type) {
                AppConstant.Constant.PAY_VIP -> {
                    initVipDialog(it)
                }

                else -> {
                    initBuyRightDialog(it)
                }

            }

        }

    }





    private fun initVipDialog(it: VipData) {

        if (null != mBuyVipDialog && mBuyVipDialog?.isVisible == true){

            mBuyVipDialog?.resetData(it)

            return
        }

        mBuyVipDialog = BuyVipDialog.newInstance(it).apply {

            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    mBuyVipDialog = null
                }
            })
        }

        mBuyVipDialog?.show(parentFragmentManager)


    }

    private fun initBuyRightDialog(it: VipData) {

        if (null != mBuyRightDialog && mBuyRightDialog?.isVisible == true) return

        mBuyRightDialog = BuyRightDialog.newInstance(it).apply {

            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    mBuyRightDialog = null
                }
            })
        }

        mBuyRightDialog?.show(parentFragmentManager)


    }



    private fun initUserInfo(mUserInfoData: UserInfoData) {


        withViewBinding {

            initTimer(mUserInfoData.timeLimitPremiumTtl)

            ivAvatar.loadImage(
                requireContext(),
                mUserInfoData.headPic,
                90f.dip2px(requireContext()),
                90f.dip2px(requireContext())
            )

            tvName.text = mUserInfoData.nickname

            tvAge.text = ","+mUserInfoData.age

            ivVip.setImageResource(if (mUserInfoData.vipType == "None") R.drawable.iv_no_vip else R.drawable.iv_is_vip)

            stvId.text = "ID: ${mUserInfoData.userId}"

            tvFcCount.text = "${mUserInfoData.flashChatCount}"
            tvPhotoCount.text = "${mUserInfoData.privatePhotoCount}"
            tvVideoCount.text = "${mUserInfoData.privateVideoCount}"
            tvVisitorCount.text = "visitor: ${mUserInfoData.visitorCnt?:0}"

            if (mUserInfoData.vipType == "None") {

                stvActivateVip.text = "Upgrade"

                stvActivateVip.visibility = View.VISIBLE

            } else {

                tvExpireTime.text = "Expire on ${mUserInfoData.vipExpireDate}"


                stvActivateVip.visibility = View.GONE
            }



            if (UserInfoHold.isReview){

                clPhoto.visibility = View.GONE
                clVideo.visibility = View.GONE
                ivAlbum.visibility = View.GONE
            }else{
                clPhoto.visibility = View.VISIBLE
                clVideo.visibility = View.VISIBLE
                ivAlbum.visibility = View.VISIBLE
            }

        }

    }

    private fun initProgress(mUserInfoData: UserInfoData){

        var progress = 0

        progress+= if (mUserInfoData.coverPics.isNullOrEmpty()){
            0
        }else{
            mUserInfoData.coverPics!!.size*3
        }

        progress+= if (mUserInfoData.nickname.isNullOrEmpty()) 0 else 10
        progress+= if (mUserInfoData.headPic.isNullOrEmpty()) 0 else 10
        progress+= if (mUserInfoData.height.isNullOrEmpty()) 0 else 10
        progress+= if (mUserInfoData.weight.isNullOrEmpty()) 0 else 10
        progress+= if (mUserInfoData.profession.isNullOrEmpty()) 0 else 10
        progress+= if (mUserInfoData.mySign.isNullOrEmpty()) 0 else 13
        progress+= if (mUserInfoData.hobbyTags.isNullOrEmpty()) 0 else 10



    }



    private fun initSendMediaDialog() {

        if (!UserInfoHold.isVip) {

            AlbumNoticeDialog().apply {

                onConfirm = {

                    mViewModel.getVipInfo(AppConstant.Constant.PAY_VIP, mVipShowValue[11], "PremiumBadge")
                }

                reportEvent(0)

            }.show(parentFragmentManager)

            return
        }


        mSendMediaDialog?.dismissAllowingStateLoss()

        mSendMediaDialog = SendMediaDialog.newInstance(
            "",
            false
        )

        mSendMediaDialog?.show(parentFragmentManager)

    }




    private fun initTimer(time: Int){

        timeJob?.cancel()

        var mTime = time

        if (mTime <= 0) {


            mViewBinding.clLimitTime.visibility = View.GONE

            return
        }

        mViewBinding.clLimitTime.visibility = View.VISIBLE

        timeJob = lifecycleScope.launch {

            while (isActive){

                if (mTime <= 0){

                    mViewBinding.clLimitTime.visibility = View.GONE

                    cancel()
                }else{

                    setTime(mTime)

                    delay(1000)
                }
                mTime --

            }

        }
    }

    private fun setTime(time: Int){

        val timeSecond = getTimeSecond(time,true)

        val symbolToList = timeSecond.symbolToList(":")

        if (symbolToList.size >= 3){

            mViewBinding.tvHour.text = symbolToList[0]
            mViewBinding.tvMinute.text = symbolToList[1]
            mViewBinding.tvSecond.text = symbolToList[2]
        }

//        Log.i(TAG, "initPayInfo: $timeSecond")
    }

    override fun onDestroyView() {
        timeJob?.cancel()
        super.onDestroyView()
    }
}