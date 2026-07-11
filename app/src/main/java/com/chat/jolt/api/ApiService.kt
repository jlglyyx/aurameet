package com.chat.jolt.api

import  com.chat.jolt.data.HobbyTagData
import com.chat.jolt.data.ConfigData
import com.chat.jolt.data.ConversationStatusData
import  com.chat.jolt.data.CustomMessageExtraData
import  com.chat.jolt.data.LikeStatusData
import  com.chat.jolt.data.ModelCardData
import  com.chat.jolt.data.ModelMediaData
import  com.chat.jolt.data.NoticeData
import  com.chat.jolt.data.OrderInfoData
import  com.chat.jolt.data.ProfessionData
import  com.chat.jolt.data.ReportData
import  com.chat.jolt.data.SendModelMediaData
import  com.chat.jolt.data.SocialAimData
import com.chat.jolt.data.TagData
import com.chat.jolt.data.Tpl
import com.chat.jolt.data.UnlockAlbums
import  com.chat.jolt.data.UpdateUserInfoData
import  com.chat.jolt.data.UserInfoData
import  com.chat.jolt.data.UserRelationData
import  com.chat.jolt.data.VipData
import  com.chat.jolt.data.WlmData
import com.chat.lib_common.data.OssData
import com.chat.lib_common.http.HttpResult
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("usr/acct/login")
    suspend fun login(@Body param:MutableMap<String,Any?>): HttpResult<UserInfoData>

    @POST("usr/app/findNews")
    suspend fun findNews(@Body param:MutableMap<String,Any?>):HttpResult<ConfigData>

    @POST("usr/app/configInfo")
    suspend fun configInfo(@Body param:MutableMap<String,Any?>):HttpResult<ConfigData>

    @POST("usr/update/initTag")
    suspend fun initTag(@Body param:MutableMap<String,Any?>):HttpResult<MutableList<HobbyTagData>>

    @POST("usr/update/initTag2")
    suspend fun initTag2(@Body param:MutableMap<String,Any?>):HttpResult<MutableList<TagData>>

    @POST("usr/update/initProfession")
    suspend fun initProfession(@Body param:MutableMap<String,Any?>):HttpResult<MutableList<ProfessionData>>

    @POST("usr/update/initSocialAim")
    suspend fun initSocialAim(@Body param:MutableMap<String,Any?>):HttpResult<MutableList<SocialAimData>>

    @POST("usr/update/userInfo")
    suspend fun updateUserInfo(@Body param: UpdateUserInfoData):HttpResult<Boolean>


    @POST("usr/update/saveLocation")
    suspend fun saveLocation(@Body param:MutableMap<String,Any?>):HttpResult<Boolean>

    @POST("usr/update/updateSetting")
    suspend fun updateSetting(@Body param:MutableMap<String,Any?>):HttpResult<Any>

    @POST("usr/app/ossAuth")
    suspend fun ossAuth(): HttpResult<OssData>

    @POST("usr/info/myInfo2")
    suspend fun getUserInfo(@Body param:MutableMap<String,Any?>): HttpResult<UserInfoData>

    @POST("usr/info/userInfo")
    suspend fun getTargetUserInfo(@Body param:MutableMap<String,Any?>): HttpResult<UserInfoData>

    @POST("soc/personal/home")
    suspend fun getHomePage(@Body param:MutableMap<String,Any?>):HttpResult<UserInfoData>

    @POST("usr/info/dataInfo")
    suspend fun getDataInfo():HttpResult<UserInfoData>

    @POST("usr/acct/destroyCheck")
    suspend fun destroyCheck():HttpResult<NoticeData>


    @POST("usr/acct/destroyAcct")
    suspend fun destroyAcct(@Body param:MutableMap<String,Any?>):HttpResult<Boolean>

    @POST("usr/feedback/add")
    suspend fun feedback(@Body param:MutableMap<String,Any?>):HttpResult<Boolean>


    @POST("acc/wallet/recharge/info")
    suspend fun getVipInfo(@Body param:MutableMap<String,Any?>):HttpResult<VipData>

    @POST("usr/opt/reportType")
    suspend fun getReportType():HttpResult<MutableList<ReportData>>

    @POST("usr/opt/report")
    suspend fun doReport(@Body param:MutableMap<String,Any?>):HttpResult<Boolean>

    @POST("soc/friend/relation/black")
    suspend fun blockUser(@Body param:MutableMap<String,Any?>):HttpResult<Boolean>

    @POST("usr/app/online")
    suspend fun online():HttpResult<ConfigData>

    @POST("usr/app/alive")
    suspend fun alive(@Body param:MutableMap<String,Any?>):HttpResult<ConfigData>


    @POST("soc/home/card")
    suspend fun getModelCard(@Body param:MutableMap<String,Any?>): HttpResult<ModelCardData>

    @POST("soc/home/left")
    suspend fun dislikeModel(@Body param:MutableMap<String,Any?>): HttpResult<LikeStatusData>

    @POST("soc/home/right")
    suspend fun likeModel(@Body param:MutableMap<String,Any?>): HttpResult<LikeStatusData>

    @POST("soc/home/flashChat")
    suspend fun flashChat(@Body param:MutableMap<String,Any?>): HttpResult<LikeStatusData>

    @POST("soc/home/wlmPage")
    suspend fun getWlmList(@Body param:MutableMap<String,Any?>): HttpResult<WlmData>

    @POST("soc/home/unlockWlm")
    suspend fun useWlm(@Body param:MutableMap<String,Any?>): HttpResult<WlmData>

    @POST("soc/friend/relation/relationListInfo")
    suspend fun getOnlineStatus(@Body param:MutableMap<String,Any?>): HttpResult<MutableMap<String, ConversationStatusData>>

    @POST("soc/friend/relation/cRelationStatusInfo")
    suspend fun getLoveConversation(@Body param:MutableMap<String,Any?>): HttpResult<MutableMap<String, ConversationStatusData>>

    @POST("soc/friend/chat/basic")
    suspend fun getChatBasic(@Body param:MutableMap<String,Any?>): HttpResult<UserRelationData>

    @POST("soc/friend/chat/sendMsg")
    suspend fun sendMsg(@Body param:MutableMap<String,Any?>):HttpResult<ModelMediaData>


    @POST("acc/wallet/recharge/order")
    suspend fun createOrder(@Body param:MutableMap<String,Any?>):HttpResult<OrderInfoData>

    @POST("acc/wallet/recharge/test")
    suspend fun payOrderTest(@Body param:MutableMap<String,Any?>):HttpResult<OrderInfoData>


    @POST("acc/wallet/recharge/getRechargeStatus")
    suspend fun getRechargeStatus(@Body param:MutableMap<String,Any?>):HttpResult<OrderInfoData>

    @POST("acc/wallet/recharge/google")
    suspend fun payOrderINAPP(@Body param:MutableMap<String,Any?>):HttpResult<OrderInfoData>

    @POST("acc/wallet/recharge/googleConfirm")
    suspend fun payOrderSub(@Body param:MutableMap<String,Any?>):HttpResult<OrderInfoData>


    @POST("acc/wallet/recharge/payFail")
    suspend fun payFail(@Body param:MutableMap<String,Any?>):HttpResult<OrderInfoData>


    @POST("soc/home/liked")
    suspend fun queryLiked(@Body param:MutableMap<String,Any?>):HttpResult<WlmData>

    @POST("soc/album/queryV2")
    suspend fun queryMediaV2(@Body param:MutableMap<String,Any?>):HttpResult<SendModelMediaData>


    @POST("soc/album/add")
    suspend fun addMedia(@Body param:MutableMap<String,Any?>):HttpResult<ModelMediaData>

    @POST("soc/album/delete")
    suspend fun deleteMedia(@Body param:MutableMap<String,Any?>):HttpResult<Boolean>

    @POST("soc/album/unlockV2")
    suspend fun unlockV2(@Body param:MutableMap<String,Any?>):HttpResult<CustomMessageExtraData>

    @POST("soc/home/myVisitor")
    suspend fun myVisitor(@Body param:MutableMap<String,Any?>):HttpResult<ModelCardData>

    @POST("soc/home/visitorChat")
    suspend fun visitorChat(@Body param:MutableMap<String,Any?>):HttpResult<ModelCardData>

    @POST("usr/app/installInfo")
    suspend fun installInfo(@Body param:MutableMap<String,Any?>):HttpResult<Any>

    @POST("acc/wallet/recharge/timeLimitInfo")
    suspend fun getTimeLimitInfo(@Body param:MutableMap<String,Any?>):HttpResult<Tpl>

    @POST("soc/friend/chat/unlockAlbum")
    suspend fun unlockAlbum(@Body param:MutableMap<String,Any?>):HttpResult<Any>

    @POST("soc/personal/unlock")
    suspend fun unlockPersonalAlbum(@Body param:MutableMap<String,Any?>):HttpResult<UnlockAlbums>

    @POST("usr/acct/logout")
    suspend fun loginOut():HttpResult<Boolean>

}