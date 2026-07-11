package com.chat.jolt.activity

import android.animation.ValueAnimator
import com.chat.jolt.R
import com.chat.jolt.data.UpdateUserInfoData
import com.chat.jolt.databinding.ActBirthStepBinding
import com.chat.jolt.dialog.EditBirthDialog
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.viewmodel.MainViewModel
import com.chat.lib_common.activity.BaseActivity
import com.chat.lib_common.tracking.reportRegisterStep
import com.chat.lib_common.tracking.updateSolarEngineUser
import com.chat.lib_common.util.click
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.dateFormat
import com.chat.lib_common.util.edgeToEdgeAll
import com.chat.lib_common.util.showShort
import com.chat.lib_common.util.startActivity
import com.chat.lib_common.util.startLoadingAnimation
import java.util.Calendar
import java.util.Locale


class BirthStepActivity : BaseActivity<ActBirthStepBinding, MainViewModel>(ActBirthStepBinding::inflate) {

    private var loadingAnimator: ValueAnimator? = null

    private val mCalendar = Calendar.getInstance().apply {

        add(Calendar.YEAR, -26)
        add(Calendar.DAY_OF_WEEK, -1)
    }

    private var currentAge = 26

    override fun initView() {

        mViewBinding.root.edgeToEdgeAll()

        mViewBinding.apply {

            if (null != UserInfoHold.userInfo){

                setName.setText(UserInfoHold.userInfo?.nickname)

            }

            setBirth.text = mCalendar.time.dateFormat("MM/dd/yyyy", Locale.US)


            setBirth.click {

                EditBirthDialog.newInstance(setBirth.text.toString()).apply {

                    onConfirm = { it, age ->

                        if (null != it) {
                            setBirth.text = it.dateFormat("MM/dd/yyyy", Locale.US)
                            currentAge = age
                        }
                    }

                }.show(supportFragmentManager)
            }


            stvNext.click {

                if (loadingAnimator?.isRunning == true) return@click

                val name = setName.text.toString()

                val birth = setBirth.text.toString()

                if (name.isBlank()){

                    showShort("input your name")

                    return@click
                }
                if (birth.isBlank()){

                    showShort("input your birth")

                    return@click
                }

                if (currentAge < 18){

                    showShort("Not allowed for use under 18 years old")

                    return@click
                }


                val updateUserInfoData = UpdateUserInfoData()

                updateUserInfoData.nickname = name

                updateUserInfoData.birthday = birth

                loadingAnimator = stvNext.startLoadingAnimation("Next")

                mViewModel.updateUserInfo(updateUserInfoData)


                updateSolarEngineUser("age",currentAge)

                updateSolarEngineUser("nickname",name)


            }
        }




    }

    override fun initViewModel() {

        mViewModel.mUpdateUserInfoStatus.observe(this) {

            loadingAnimator?.cancel()

            reportRegisterStep(0,false, UserInfoHold.isOrganic)

            createIntent(SexStepActivity::class.java).startActivity(this@BirthStepActivity, true)
        }


        mViewModel.mUpdateUserInfoError.observe(this) {

            loadingAnimator?.cancel()

        }


        mViewModel.requestFailEvent.observe(this) {

            loadingAnimator?.cancel()


        }
    }



    override fun initData() {


    }





    override fun onDestroy() {
        loadingAnimator?.cancel()
        loadingAnimator = null
        super.onDestroy()
    }

    override fun onBackPressed() {

        if (false){
            super.onBackPressed()
        }

    }
}