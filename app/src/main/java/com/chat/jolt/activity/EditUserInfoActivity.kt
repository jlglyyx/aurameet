package com.chat.jolt.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chat.jolt.R
import com.chat.jolt.data.UpdateUserInfoData
import com.chat.jolt.databinding.ActEditUserInfoBinding
import com.chat.jolt.databinding.ViewLikeTabBinding
import com.chat.jolt.dialog.NoticeDialog
import com.chat.jolt.dialog.ViolationDialog
import com.chat.jolt.fragment.EditUserInfoFragment
import com.chat.jolt.fragment.PreviewUserInfoFragment
import com.chat.jolt.viewmodel.UserViewModel
import com.chat.lib_common.activity.BaseActivity
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.tracking.updateSolarEngineUser
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.edgeToEdgeTop
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class EditUserInfoActivity :
    BaseActivity<ActEditUserInfoBinding, UserViewModel>(ActEditUserInfoBinding::inflate) {


    private val mFragments = mutableListOf<Fragment>()

    private val mTitles = mutableListOf("Edit", "Preview")


    private var isTurnOns = false


    private var mIsSave = false

    override fun initView() {


        withViewBinding {

            root.edgeToEdgeBottom()

            llSave.edgeToEdgeTop()




            tvSave.click {

                val currentValue = mViewModel.mPreviewUserInfoData.currentValue()

                showLoading()

                currentValue?.let { info ->

                    val mUpdateUserInfoData = UpdateUserInfoData()

                    mUpdateUserInfoData.nickname = info.nickname
                    mUpdateUserInfoData.headPic = info.headPic
                    mUpdateUserInfoData.birthday = info.birthDay
                    mUpdateUserInfoData.weight = info.weight
                    mUpdateUserInfoData.height = info.height
                    mUpdateUserInfoData.profession = info.profession
                    mUpdateUserInfoData.mySign = info.mySign
                    mUpdateUserInfoData.socialAim = info.socialAim
                    mUpdateUserInfoData.hobbyTags = info.hobbyTags
                    mUpdateUserInfoData.coverPics = info.coverPics
                    mUpdateUserInfoData.turnOnsTags = info.turnOnsTags

                    mViewModel.updateUserInfo(mUpdateUserInfoData)


                    updateSolarEngineUser("nickname", info.nickname)
                    updateSolarEngineUser("age", info.age)


                }

            }

        }

    }

    override fun initData() {

        isTurnOns = intent.getBooleanExtra(AppConstant.Constant.IS_TURN_ONS, isTurnOns)

        initViewPager()

        initTabLayout()

        mViewModel.getDataInfo()
    }

    override fun initViewModel() {

        mViewModel.mPreviewUserInfoData.observe(this) {

        }

        mViewModel.mHasChangeInfo.observe(this) {

        }
        mViewModel.mUpdateUserInfoStatus.observe(this) {

            mIsSave = true

            dismissLoading()

            finish()


        }

        mViewModel.mUpdateUserInfoError.observe(this) {

            dismissLoading()


            if (it.errorCode == 1105) {

//                mViewBinding.ivtName.setContent(UserInfoHold.userInfo?.nickname)

                initViolationDialog(0)

            } else if (it.errorCode == 1106) {

//                mViewBinding.ivtIntroduction.setTitle(
//                    UserInfoHold.userInfo?.mySign ?: "Self Introduction"
//                )

                initViolationDialog(1)


            } else if (it.errorCode == 1013) {

//                mViewBinding.ivtAvatar.setImage(UserInfoHold.userInfo?.headPic)

                initViolationDialog(2)
            }


        }
    }


    private fun initTabLayout() {

        mViewBinding.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                if (null == tab) {

                    return
                }
                tab.customView?.let {

                    val mViewLikeTabBinding = ViewLikeTabBinding.bind(it)

                    mViewLikeTabBinding.tvTitle.setTextColor(getColor(R.color.white))

                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

                if (null == tab) {

                    return
                }
                tab.customView?.let {

                    val mViewLikeTabBinding = ViewLikeTabBinding.bind(it)

                    mViewLikeTabBinding.tvTitle.setTextColor(getColor(R.color.color_999999))

                }

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

                if (tab?.position == 2) {
                    FlowBus.with(AppConstant.EventConstant.EVENT_TO_TOP_MESSAGE).postValue(0)

                }

            }

        })

    }


    private fun initViewPager() {

        mFragments.add(EditUserInfoFragment.newInstance(isTurnOns))
        mFragments.add(PreviewUserInfoFragment())

        mViewBinding.apply {


            viewPager.adapter = object : FragmentStateAdapter(this@EditUserInfoActivity) {
                override fun getItemCount(): Int {

                    return mFragments.size

                }

                override fun createFragment(position: Int): Fragment {

                    return mFragments[position]

                }

            }

            TabLayoutMediator(
                tabLayout,
                viewPager,
                false,
                true
            ) { tab, position ->

                val mViewLikeTabBinding =
                    ViewLikeTabBinding.inflate(LayoutInflater.from(this@EditUserInfoActivity))

                tab.customView = mViewLikeTabBinding.root

                mViewLikeTabBinding.tvTitle.text = mTitles[position]

                if (position == 0) {
                    mViewLikeTabBinding.tvTitle.setTextColor(getColor(R.color.white))
                } else {
                    mViewLikeTabBinding.tvTitle.setTextColor(getColor(R.color.color_999999))
                }
                mViewLikeTabBinding.stvMessageCount.visibility = View.GONE

                tab.view.setOnLongClickListener {

                    return@setOnLongClickListener true
                }


            }.attach()

            viewPager.isUserInputEnabled = false

            viewPager.offscreenPageLimit = mFragments.size

            val tabStrip = tabLayout.getChildAt(0) as ViewGroup
            for (i in 0 until tabStrip.childCount) {
                val tabView = tabStrip.getChildAt(i)
                tabView.setPadding(0)
            }


        }
    }


    private fun initNoticeDialog() {


        val mNoticeDialog = NoticeDialog().apply {

            initView = { dialog, mViewBinding ->
                mViewBinding.tvTitle.text = "Edit Profile"
                mViewBinding.tvContent.text = "Do you want to save your changes ?"
            }

            onConfirm = {

                mViewBinding.tvSave.performClick()
            }

            onCancel = {

                mViewModel.mHasChangeInfo.postValue(false)

                finish()
            }
        }

        mNoticeDialog?.show(supportFragmentManager)


    }


    /**
     * 0 name 1 sign 2 picture
     */
    private fun initViolationDialog(type: Int) {


        val mViolationDialog = ViolationDialog().apply {

            initView = { dialog, mViewBinding ->


                when (type) {
                    0 -> {
                        mViewBinding.ivImage.setImageResource(R.drawable.iv_text_violation)
                        mViewBinding.tvTitle.text = "NickName Violation"
                        mViewBinding.tvContent.text =
                            "Oops! Your NickName didn't pass our review. Please update it and try again"
                    }

                    1 -> {
                        mViewBinding.ivImage.setImageResource(R.drawable.iv_text_violation)
                        mViewBinding.tvTitle.text = "Self Introduction Violation"
                        mViewBinding.tvContent.text =
                            "Oops! Your Self Introduction didn't pass our review. Please update it and try again"
                    }

                    2 -> {
                        mViewBinding.tvTitle.text = "Photo Violation"
                        mViewBinding.tvContent.text =
                            "Oops! Your Photo didn't pass our review. Please update it and try again"
                        mViewBinding.ivImage.setImageResource(R.drawable.iv_image_violation)
                    }
                }


                mViewBinding.stvConfirm.text = "I Know"

            }
        }

        mViolationDialog?.show(supportFragmentManager)

    }


    override fun finish() {

        if (mIsSave) {
            super.finish()
        } else {

            if (mViewModel.mHasChangeInfo.currentValue() == true) {

                initNoticeDialog()
            } else {

                super.finish()
            }
        }

    }


}