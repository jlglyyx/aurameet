package com.chat.jolt.viewmodel

import com.chat.jolt.api.ApiService
import com.chat.jolt.data.WlmData
import com.chat.lib_common.bus.SingleFlow
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.http.HttpClient

class LikeViewModel : PublicViewModel() {

    private val mApiService = HttpClient.createApi(ApiService::class.java)


    val mILikeData = SingleFlow<WlmData>()



    val mUseWlmStatus = SingleFlow<WlmData>()







    fun useWlm(friendId: String) {
        val params = mutableMapOf<String, Any?>()

        params["friendId"] = friendId

        doRequest(onRequest = {
            mApiService.useWlm(params)
        }, onSuccess = {
            mUseWlmStatus.postValue(it.data)

//            getUserInfo()
        }, onError = {

            if (it.code == 1001) {


            } else if (it.code == 1002) {

            }

        })

    }

    fun queryLiked(offset: Int) {

        val params = mutableMapOf<String, Any?>()

        params["offset"] = offset

        params["limit"] = AppConstant.Constant.PAGE_SIZE_COUNT

        doRequest(onRequest = {
            mApiService.queryLiked(params)
        }, onSuccess = {

            mILikeData.postValue(it.data)

        }, onException = {

            requestFailEvent.postValue(true)
            false
        })

    }




}