package com.chat.jolt.helper


import android.content.Context
import android.content.Intent
import com.chat.jolt.activity.LoginActivity
import com.chat.jolt.data.UserInfoData
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.constant.AppConstant.Constant
import com.chat.lib_common.im.RIMClient
import com.chat.lib_common.im.RIMDispatcher
import com.chat.lib_common.tracking.loginOutSolarEngineUser
import com.chat.lib_common.util.GoogleLoginUtil
import com.chat.lib_common.util.GooglePayManager
import com.chat.lib_common.util.clearAllCache
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.fromJson
import com.chat.lib_common.util.getCache
import com.chat.lib_common.util.setCache
import com.chat.lib_common.util.toJson

object UserInfoHold {

    val userInfo: UserInfoData?
        get() = getUserInfoData()


    val userId: String?
        get() =userInfo?.userId

    val isVip: Boolean
        get() = getIsVip()

    val isNewUser: String
        get() = if (userInfo?.regFirstDay == "True") "new" else "old"

    val mRIMToken: String
        get() = getCache(AppConstant.RIMConstant.RIM_TOKEN, "")


    val isReview :Boolean
        get() = getCache(AppConstant.Constant.IS_REVIEW_VERSION,"False") == "True"

    val isLowUse :Boolean
        get() = userInfo?.lowUser == "True"

    val isOrganic :Boolean
        get() = userInfo?.organic == "True"





    private fun getIsVip():Boolean{

        if (null == userInfo) return false

        if (null == userInfo!!.vipType) return false

        if (userInfo!!.vipType.isNullOrEmpty() || userInfo!!.vipType == "null") return false

        if ("None" != userInfo!!.vipType) return true

        return false
    }

    private fun getUserInfoData(): UserInfoData?{

        val cache = getCache(AppConstant.Constant.USER_INFO, "")

        if (cache == ""){

            return null
        }
        return cache.fromJson<UserInfoData>()

    }


    fun updateLocalUserInfo(userInfoData: UserInfoData) {
        setCache(AppConstant.Constant.USER_INFO, userInfoData.toJson())
    }




    fun loginOut(context: Context = BaseApplication.mApplication) {


        RIMClient.logout()

        GooglePayManager.disClient()

        GoogleLoginUtil().googleLogOut(context)

        RIMDispatcher.removeAllListener()

        val mSocialAim = getCache(AppConstant.Constant.SOCIAL_AIM, "")
        val mProfession = getCache(AppConstant.Constant.PROFESSION, "")
        val mHobbyTag = getCache(AppConstant.Constant.HOBBY_TAG, "")
        val mLoginNotice = getCache(AppConstant.Constant.LOGIN_NOTICE, false)
        val mStartNotice = getCache(AppConstant.Constant.START_NOTICE, false)
        val hasSwip = getCache(AppConstant.Constant.HAS_SWIP, false)
        val turnTag = getCache(AppConstant.Constant.TURN_TAG, "")

        AppConstant.Constant.LAST_OPEN_VIP_COUNT = 0

        AppConstant.Constant.LAST_OPEN_VIP_TIME = System.currentTimeMillis()

        clearAllCache()

        setCache(AppConstant.Constant.SOCIAL_AIM, mSocialAim)
        setCache(AppConstant.Constant.PROFESSION, mProfession)
        setCache(AppConstant.Constant.HOBBY_TAG, mHobbyTag)
        setCache(AppConstant.Constant.LOGIN_NOTICE, mLoginNotice)
        setCache(AppConstant.Constant.START_NOTICE, mStartNotice)
        setCache(AppConstant.Constant.HAS_SWIP, hasSwip)
        setCache(AppConstant.Constant.TURN_TAG, turnTag)

        loginOutSolarEngineUser()

        val mIntent = context.createIntent(LoginActivity::class.java)

        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        context.startActivity(mIntent)


    }
}