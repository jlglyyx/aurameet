package com.chat.jolt.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.chat.jolt.R
import com.chat.jolt.api.ApiService
import com.chat.jolt.data.CustomMessageExtraData
import com.chat.jolt.data.LikeStatusData
import com.chat.jolt.data.ModelCardData
import com.chat.jolt.data.OrderInfoData
import com.chat.jolt.data.TagData
import com.chat.jolt.data.Tpl
import com.chat.jolt.data.UnlockAlbums
import com.chat.jolt.data.UpdateUserInfoData
import com.chat.jolt.data.UserInfoData
import com.chat.jolt.data.VipData
import com.chat.jolt.data.WlmData
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.helper.UserInfoHold.updateLocalUserInfo
import com.chat.jolt.widget.GridImageView
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.bus.SingleFlow
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.data.OssData
import com.chat.lib_common.http.HttpClient
import com.chat.lib_common.util.GooglePayManager
import com.chat.lib_common.util.fromJson
import com.chat.lib_common.tracking.getAdId
import com.chat.lib_common.util.getCache
import com.chat.lib_common.tracking.getGoogleAdId
import com.chat.lib_common.tracking.mRightShowValue
import com.chat.lib_common.tracking.mVipShowValue
import com.chat.lib_common.tracking.reportAdjustPayEvent
import com.chat.lib_common.tracking.updateSolarEngineUser
import com.chat.lib_common.util.formatListJson
import com.chat.lib_common.util.setCache
import com.chat.lib_common.util.showShort
import com.chat.lib_common.viewmodel.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class PublicViewModel: BaseViewModel() {

    private val mApiService = HttpClient.createApi(ApiService::class.java)

    val mFlashChatStatusData = SingleFlow<LikeStatusData>()

    val mUserInfoData = SingleFlow<UserInfoData>()

    val mTargetUserInfoData = SingleFlow<UserInfoData>()

    val mVipData = SingleFlow<VipData>()

    val mOssData = SingleFlow<OssData>()

    val mUpdateUserInfoStatus = SingleFlow<Boolean>()

    val mUpdateUserInfoError = SingleFlow<UpdateUserInfoData>()

    val mOrderInfoData = SingleFlow<OrderInfoData>()

    val mOrderInfoStatus = SingleFlow<Boolean>()

    val mCreateOrderInfoData = SingleFlow<OrderInfoData>()

    var mTagMapData: MutableMap<String, TagData>? = null

    val mWlmData = SingleFlow<WlmData>()

    val mTpl = SingleFlow<Tpl>()

    val mUnlockCrazyAlbumStatus = SingleFlow<UnlockAlbums>()

    val mVisitorData = SingleFlow<ModelCardData>()

    var mRetryTime = 0

    var mHasLoadUserInfo = false

    var paddleCount: Int = -1

    val mNoCardStatus = SingleFlow<Any>()

    val mLikeStatusData = SingleFlow<LikeStatusData>()

    val mPayErrorStatus = SingleFlow<Boolean>()

    init {

        loginOut = {

            UserInfoHold.loginOut()
        }
    }





    fun updateUserInfo(mUpdateUserInfoData: UpdateUserInfoData) {

        doRequest(onRequest = {
            mApiService.updateUserInfo(mUpdateUserInfoData)
        }, onSuccess = {

            mUpdateUserInfoStatus.postValue(true)
        }, onError = {

            mUpdateUserInfoData.errorCode = it.code

            mUpdateUserInfoError.postValue(mUpdateUserInfoData)


        }, onException = {

            requestFailEvent.postValue(false)
            false
        })


    }




    fun flashChat(friendId: String, busiSource: String,firstMsg: String = "") {

        val params = mutableMapOf<String, Any?>()
        params["friendId"] = friendId
        params["busiSource"] = busiSource
        params["actionType"] = "Click"

        if (firstMsg.isNotEmpty()){
            params["firstMsg"] = firstMsg
        }

        doRequest(onRequest = {
            mApiService.flashChat(params)
        }, onSuccess = {

            it.data.userId = friendId

            mFlashChatStatusData.postValue(it.data)

            getUserInfo()
        }, onError = {

            if (it.code == 1001) {

                getVipInfo(
                    AppConstant.Constant.PAY_FLASH_CHAT,
                    "",
                    "PremiumBadge"
                )

            } else if (it.code == 1003) {

                getVipInfo(AppConstant.Constant.PAY_FLASH_CHAT, mRightShowValue[1], "PremiumBadge")

            }

        })

    }


    fun dislikeModel(friendId: String, busiSource: String = "HomeCard", actionType: String = "Swipe") {


        val params = mutableMapOf<String, Any?>()
        params["friendId"] = friendId
        params["busiSource"] = busiSource
        params["actionType"] = actionType

        doRequest(onRequest = {

            mApiService.dislikeModel(params)
        }, onSuccess = {

            it.data.userId = friendId

            paddleCount = it.data.paddleCount

            mLikeStatusData.postValue(it.data)

        }, onError = {

            when (it.code) {
                1001 -> {
                    getVipInfo(AppConstant.Constant.PAY_VIP, mVipShowValue[1], "MoreSwipe")
                }
                1016 -> {
                    mNoCardStatus.postValue(true)
                }
                else -> {
                    showShort(it.message)
                }
            }

        })

    }

    fun likeModel(friendId: String, busiSource: String = "HomeCard", actionType: String = "Swipe") {


        val params = mutableMapOf<String, Any?>()
        params["friendId"] = friendId
        params["busiSource"] = busiSource
        params["actionType"] = actionType
        doRequest(onRequest = {
            mApiService.likeModel(params)
        }, onSuccess = {

            it.data.userId = friendId

            paddleCount = it.data.paddleCount
            mLikeStatusData.postValue(it.data)

        }, onError = {

            when (it.code) {
                1001 -> {
                    getVipInfo(AppConstant.Constant.PAY_VIP, mVipShowValue[1], "MoreSwipe")
                }
                1016 -> {
                    mNoCardStatus.postValue(true)
                }
                else -> {
                    showShort(it.message)
                }
            }

        })

    }



    fun getVipInfo(type: String, showType: String?, intoType: String?) {

        val vipInfoCache = getVipInfoCache(type, showType, intoType)

        val params = mutableMapOf<String, Any?>()

        params["tplType"] = type

        params["vipPopup"] = "True"

        doRequest(onRequest = {
            mApiService.getVipInfo(params)
        }, onSuccess = {

            val cache = getCache(AppConstant.Constant.VIP_CACHE + type, "")

            var needRefresh = false

            if (cache.isNotEmpty()) {

                val mLocalVipData = cache.fromJson<VipData>()

                needRefresh = mLocalVipData.tplList.size != it.data.tplList.size

            }

            cacheVipInfo(it.data, type)

            setCache(AppConstant.Constant.IS_SUBSCRIPTION, it.data.subscription)


            if (type == AppConstant.Constant.PAY_VIP){

                if (!it.data.tplList.all { all -> all.ttl == 0 }){

                    if (!mHasLoadUserInfo){

                        mHasLoadUserInfo = true

                        getUserInfo()

                        needRefresh = true
                    }
                }

            }

            if (vipInfoCache && !needRefresh) {

                return@doRequest
            }

            it.data.type = type

            it.data.showType = showType

            it.data.intoType = intoType

            mVipData.postValue(it.data)

        })

    }


    private fun getVipInfoCache(type: String, showType: String?, intoType: String?): Boolean {

        val cache = getCache(AppConstant.Constant.VIP_CACHE + type, "")

        if (cache.isNotEmpty()) {

            val mLocalVipData = cache.fromJson<VipData>()

            mLocalVipData.let {

                viewModelScope.launch {

                    it.type = type

                    it.showType = showType

                    it.intoType = intoType

                    delay(10)

                    mVipData.postValue(it)
                }

                return true
            }
        } else {

            return false
        }

    }

    suspend fun cacheVipInfo(mVipData: VipData, type: String) {

        if (!GooglePayManager.isClient()) {

            setCache(AppConstant.Constant.VIP_CACHE + type, mVipData)
            return
        }

        val ids = mVipData.tplList.map { map ->

            map.productId
        }

        val queryProductPrice = GooglePayManager.queryProductPrice(ids, mVipData.subscription)

        queryProductPrice?.productDetailsList?.forEachIndexed { index, productDetails ->

            val item =
                mVipData.tplList.findLast { find -> find.productId == productDetails.productId }

            if (mVipData.subscription == "True") {
                val product = productDetails.subscriptionOfferDetails?.get(0)
                item?.formatMoney =
                    product?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice.toString()
                product?.pricingPhases?.pricingPhaseList?.get(0)?.priceCurrencyCode.toString()
            } else {
                val product = productDetails.oneTimePurchaseOfferDetailsList?.get(0)
                item?.formatMoney = product?.formattedPrice.toString()
            }

        }
        setCache(AppConstant.Constant.VIP_CACHE + type, mVipData)
    }


    fun getUserInfo(isBuy: Boolean = false) {


        val params = mutableMapOf<String, Any?>()
        params["ratePopup"] = "False"

        doRequest(onRequest = {
            mApiService.getUserInfo(params)
        }, onSuccess = {


            updateLocalUserInfo(it.data)

            mUserInfoData.postValue(it.data)

            updateSolarEngineUser("account_type", it.data.userType)

            if (isBuy){
                FlowBus.with(AppConstant.EventConstant.EVENT_IS_BUY_GET_USER_INFO).postValue(it.data)
            }
            FlowBus.with(AppConstant.EventConstant.EVENT_UPDATE_USER_INFO).postValue(it.data)

        })


    }

    fun getTargetUserInfo(userId: String?, mCustomMessageExtraData: CustomMessageExtraData?) {

        if (userId.isNullOrEmpty()) return

        val params = mutableMapOf<String, Any?>()

        params["userId"] = userId

        params["onlineStatus"] = "True"

        params["coverNum"] = 5

        doRequest(onRequest = {
            mApiService.getTargetUserInfo(params)
        }, onSuccess = {

            it.data.mCustomMessageExtraData = mCustomMessageExtraData

            mTargetUserInfoData.postValue(it.data)

        })


    }



    fun ossAuth(uploadId: String, uploadType: String, uploadUri: Uri?,sendMessageType: String? = null) {

        doRequest(onRequest = {
            mApiService.ossAuth()
        }, onSuccess = {

            it.data.apply {

                this.uploadId = uploadId
                this.uploadType = uploadType
                this.uploadUri = uploadUri
                this.sendMessageType = sendMessageType

                if (this.SecurityToken.isNullOrEmpty() || this.AccessKeyId.isNullOrEmpty() || this.AccessKeySecret.isNullOrEmpty()) {

                    showShort("upload error")

                    requestFailEvent.postValue(uploadId)
                } else {

                    mOssData.postValue(it.data)

                }
            }

        }, onException = {

            requestFailEvent.postValue(uploadId)

            false
        }, onError = {

            requestFailEvent.postValue(uploadId)

        })
    }











    fun createOrder(mTpl: Tpl, rechargeEntry: String) {

        val params = mutableMapOf<String, Any?>()

        params["payType"] = if (AppConstant.Constant.threePay) "HuiFuPay" else "GooglePay"
        params["adid"] = ""
        params["productId"] = mTpl.productId
        params["tplId"] = mTpl.tplId
        params["rechargeEntry"] = rechargeEntry

        doRequest(onRequest = {
            mApiService.createOrder(params)
        }, onSuccess = {

            setCache(AppConstant.Constant.BIZ_ID + "-" + mTpl.productId, it.data.bizId)

            it.data.productId = mTpl.productId

            mCreateOrderInfoData.postValue(it.data)


        }, onError = {

            payFail(null, null, "[${it.code}] Create Order error: ${it.message}")

            mPayErrorStatus.postValue(true)

        }, onException = {

            payFail(null, null, "[-8] Create Order error: ${it.message}")

            mPayErrorStatus.postValue(true)
            false
        })

    }




    fun getRechargeStatus(bizId: Int) {

        val params = mutableMapOf<String, Any?>()

        params["bizId"] = bizId

        doRequest(onRequest = {

            mApiService.getRechargeStatus(params)

        }, onSuccess = {

            when(it.data.status){

                "Success" ->{

                    delay(2000)

                    mOrderInfoData.postValue(it.data)

                    getUserInfo(true)
                }

                "Failure" ->{

                    mOrderInfoStatus.postValue(false)

                    showShort("pay error")
                }

                else -> {

                    delay(2000)

                    if (mRetryTime++ <7){
                        getRechargeStatus(bizId)
                    }else{
                        mOrderInfoStatus.postValue(true)

//                        showShort("Hi, tiger! it will take 1-5 minutes for your benefits to becredited, thanks for your patience.")
                    }

                }

            }

        }, onError = {

            payFail(
                bizId,
                "",
                "[${it.code}] ${it.message}"
            )

            mPayErrorStatus.postValue(true)

        }, onException = {

            payFail(bizId, "", "[-7] ${it.message}")

            mPayErrorStatus.postValue(true)
            false
        })

    }


    fun payOrderTest(bizId: Int, price: String) {

        val params = mutableMapOf<String, Any?>()

        params["bizId"] = bizId

        doRequest(onRequest = {

            mApiService.payOrderTest(params)

        }, onSuccess = {

            delay(2000)

            mOrderInfoData.postValue(it.data)

            UserInfoHold.userId?.let { userId ->
                reportAdjustPayEvent(bizId.toString(),price, userId)
            }


            getUserInfo(true)

        }, onError = {

            payFail(
                bizId,
                "Test" + System.currentTimeMillis().toString(),
                "[${it.code}] ${it.message}"
            )

            mPayErrorStatus.postValue(true)

        }, onException = {

            payFail(bizId, "Test" + System.currentTimeMillis().toString(), "[-7] ${it.message}")

            mPayErrorStatus.postValue(true)
            false
        })

    }


    fun payFail(bizId: Int?, orderId: String?, reason: String) {

        val params = mutableMapOf<String, Any?>()

        params["bizId"] = bizId

        params["orderId"] = orderId

        params["reason"] = reason

        doRequest(onRequest = {

            mApiService.payFail(params)

        }, onSuccess = {

        })

    }


    fun payOrderGoogle(bizId: Int, price: String, mPurchase: Purchase, type: String) {

        val params = mutableMapOf<String, Any?>()

        params["orderNo"] = bizId
        params["productId"] = mPurchase.products[0]
        params["orderId"] = mPurchase.orderId
        params["purchaseToken"] = mPurchase.purchaseToken

        doRequest(onRequest = {

            if (type == BillingClient.ProductType.SUBS) {

                delay(2000)

                mApiService.payOrderSub(params)
            } else {
                mApiService.payOrderINAPP(params)
            }

        }, onSuccess = {

            it.data.mPurchase = mPurchase

            it.data.type = type

            mOrderInfoData.postValue(it.data)

            UserInfoHold.userId?.let { userId ->
                reportAdjustPayEvent(bizId.toString(),price, userId)
            }


            delay(2000)

            getUserInfo(true)

        }, onError = {

            payFail(bizId, mPurchase.orderId, "[${it.code}] Pay error: ${it.message}")

            mPayErrorStatus.postValue(true)

        }, onException = {

            payFail(bizId, mPurchase.orderId, "[-7] Pay error: ${it.message}")

            mPayErrorStatus.postValue(true)
            false
        })

    }


    fun getTurnOnsData(): MutableMap<String, TagData> {

        if (null != mTagMapData) return mTagMapData!!

        val turnCache = getCache(AppConstant.Constant.TURN_TAG, "")

        mTagMapData = mutableMapOf<String, TagData>()

        if (turnCache.isNotEmpty()){

            val mTagListData = turnCache.formatListJson<TagData>()

            mTagListData.forEach {

                mTagMapData?.set(it.userTag, it)
            }

        }

        return mTagMapData!!
    }



    fun installInfo(
        network: String?,
    ) {

        doRequest({

            val params = mutableMapOf<String, Any?>()

            if (!network.isNullOrEmpty()) {
                params["network"] = network
            } else {
                val adId = getAdId()
                val getGoogleAdId = getGoogleAdId()
                params["gpsAdid"] = getGoogleAdId
                params["adid"] = adId

                Log.i(TAG, "installInfo: $adId  $getGoogleAdId")
            }
            mApiService.installInfo(params)
        }, {

        }, onError = {

        })

    }


    fun myVisitor() {

        val params = mutableMapOf<String, Any?>()

        doRequest(onRequest = {

            mApiService.myVisitor(params)

        }, onSuccess = {

            mVisitorData.postValue(it.data)

        }, onException = {

            requestFailEvent.postValue(true)
            false
        })


    }


    fun getWlmList(offset: Int) {
        val params = mutableMapOf<String, Any?>()
        params["offset"] = offset
        params["limit"] = AppConstant.Constant.PAGE_SIZE_COUNT

        doRequest(onRequest = {
            mApiService.getWlmList(params)
        }, onSuccess = {
            mWlmData.postValue(it.data)
        }, onException = {

            requestFailEvent.postValue(true)
            false
        })

    }

    fun getTimeLimitInfo() {

        val params = mutableMapOf<String, Any?>()

        params["tplType"] = "Vip"

        doRequest(onRequest = {
            mApiService.getTimeLimitInfo(params)
        }, onSuccess = {
            mTpl.postValue(it.data)
        }, onException = {

            requestFailEvent.postValue(true)
            false
        })

    }


    fun unlockCrazyAlbum(albumId: String,mUnlockAlbums:UnlockAlbums,position: Int) {

        val params = mutableMapOf<String, Any?>()

        params["albumId"] = albumId

        doRequest(onRequest = {
            mApiService.unlockPersonalAlbum(params)
        }, onSuccess = {

            mUnlockAlbums.albumStatus = it.data.albumStatus

            mUnlockAlbums.ttl = it.data.ttl

            mUnlockAlbums.status = GridImageView.NORMAL_STATUS

            mUnlockAlbums.position = position

            mUnlockCrazyAlbumStatus.postValue( mUnlockAlbums)

            getUserInfo()

        },{
            when (it.code.toString()) {
                "1001" -> {


                    getVipInfo(
                        AppConstant.Constant.PAY_VIP,
                        mVipShowValue[5],
                         "SecretVideo"
                    )


                }

                "1004" -> {
                    getVipInfo(AppConstant.Constant.PAY_PRIVATE_PHOTO, mRightShowValue[0], "PremiumBadge")


                }

                "1005" -> {
                    getVipInfo(AppConstant.Constant.PAY_PRIVATE_VIDEO, mRightShowValue[0], "PremiumBadge")


                }

            }
        })

    }



    fun loginOutApp() {

        doRequest({
            mApiService.loginOut()
        }, {
            UserInfoHold.loginOut()
        }, {

        })
    }


}