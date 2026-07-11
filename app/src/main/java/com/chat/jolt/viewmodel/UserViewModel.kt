package com.chat.jolt.viewmodel

import com.chat.jolt.api.ApiService
import com.chat.jolt.data.NoticeData
import com.chat.jolt.data.UserInfoData
import com.chat.lib_common.bus.SingleFlow
import com.chat.lib_common.http.HttpClient

class UserViewModel: PublicViewModel() {

    private val mApiService = HttpClient.createApi(ApiService::class.java)


    val mDeleteAccountNoticeStatus = SingleFlow<NoticeData>()

    val mDeleteAccountStatus = SingleFlow<Boolean>()

    val mFeedBackStatus = SingleFlow<Boolean>()



    val mVisitorStatusData = SingleFlow<Boolean>()

    val mPreviewUserInfoData = SingleFlow<UserInfoData>()

    val mHasChangeInfo = SingleFlow<Boolean>()

    val mUserInfoDataStatus = SingleFlow<Boolean>()

//
    fun getDataInfo() {

        doRequest(onRequest = {
            mApiService.getDataInfo()
        }, onSuccess = {

            mPreviewUserInfoData.postValue(it.data)
        })


    }


    fun getHomePage(userId: String) {

        val params = mutableMapOf<String, Any?>()

        params["userId"] = userId

        doRequest(onRequest = {
            mApiService.getHomePage(params)
        }, onSuccess = {

            mUserInfoData.postValue(it.data)
        }, onError = {
            mUserInfoDataStatus.postValue(false)
        }, onException = {
            mUserInfoDataStatus.postValue(false)
            false
        })


    }



    fun destroyCheck() {


        doRequest(onRequest = {
            mApiService.destroyCheck()
        }, onSuccess = {

            mDeleteAccountNoticeStatus.postValue(it.data)
        })


    }

    fun destroyAcct() {


        val params = mutableMapOf<String, Any?>()

        params["otherReason"] = "reason"

        doRequest(onRequest = {
            mApiService.destroyAcct(params)
        }, onSuccess = {
            mDeleteAccountStatus.postValue(true)
        })


    }

    fun feedback(details: String, email: String = "") {

        val params = mutableMapOf<String, Any?>()

        params["details"] = details
        params["userEmail"] = email

        doRequest(onRequest = {
            mApiService.feedback(params)
        }, onSuccess = {
            mFeedBackStatus.postValue(true)
        })


    }




    fun visitorChat(friendId: String) {

        val params = mutableMapOf<String, Any?>()

        params["friendId"] = friendId
        params["busiSource"] = "Visitor"
        params["actionType"] = "Click"

        doRequest(onRequest = {

            mApiService.visitorChat(params)

        }, onSuccess = {

            mVisitorStatusData.postValue(true)

        })


    }


}