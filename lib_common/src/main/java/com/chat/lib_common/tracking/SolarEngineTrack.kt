@file:JvmName("SolarEngineTrack")

package com.chat.lib_common.tracking

import android.content.Context
import android.util.Log
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.util.toJson
import com.reyun.solar.engine.SolarEngineConfig
import com.reyun.solar.engine.SolarEngineManager
import com.reyun.solar.engine.infos.SECustomEventModel
import com.reyun.solar.engine.infos.SELoginEventModel
import com.reyun.solar.engine.infos.SERegisterEventModel
import org.json.JSONObject
import kotlin.apply
import kotlin.collections.mutableMapOf
import kotlin.collections.set


const val POPUP_IP_RISK_KEY = "popup_ipRisk"

val POPUP_IP_RISK_VALUE = arrayListOf("shuMei","vpn")

const val REG_STEP_KEY = "reg_step"

val REG_STEP_VALUE = arrayListOf("basic info", "dating goal", "photo", "hobby")


const val RECEIVE_MESSAGE = "receive_message"

const val SWIPE_RIGHT = "swipe_right"

const val GOTO_VISITOR = "goto_visitor"




val mPopPopupDialogKey = arrayListOf(
    "popup_location", "click_filter", "swipe_noData", "popup_noTimes", "popup_connect",
    "popup_notification", "click_popupNotification", "click_messageNotification"
)

val mMessageEventKey = arrayListOf(
    "pin",
    "unpin",
    "delete_message",
    "receive_message",
    "click_pp",
    "click_pv",
    "sendMessage_fail"
)

val mMessageEventValue = arrayListOf(
    "text",
    "public photo",
    "private photo-1",
    "private video-1",
)

val mMessageNoticeKey = arrayListOf("inapp_noticeShow", "click_inappNoticeClick","push_click")

val mMessageUserKey = arrayListOf(
    "profile_view",
    "popup_deleteAccount",
    "click_deleteAccount",
    "popup_logOut",
    "popup_update",
    "click_popupUate"
)

val mMessagePayKey = arrayListOf("popup_vip", "buy_vip")

val mRightKey = arrayListOf(
    "popup_pp",
    "buy_pp",
    "popup_pv",
    "buy_pv",
    "popup_flashchat",
    "buy_flashcaht",
    "popup_wlm",
)


//val mVipShowValue = arrayListOf(
//    "chat limit", "swipe limit", "my center", "myCenter_private album",
//    "message_private album", "flashchat limit", "wlm limit", "pp limit",
//    "pv limit", "wlm_vip", "low value", "WLM_message",
//    "my center vip", "profile vip"
//)


val mVipShowValue = arrayListOf(
    "chat limit",          // 0.
    "swipe limit",         // 1.
    "my center",           // 2.
    "wlm limit",           // 3.
    "pp limit",            // 4.
    "pv limit",            // 5.
    "wlm_vip",             // 6.
    "low value",           // 7.
    "WLM_message",         // 8.
    "my center vip",       // 9.
    "profile vip",         // 10.
    "private_album",       // 11.
    "NoData_Banner",       // 12.
    "NoData_Photo",        // 13.
    "myVisitor",           // 14.
    "Visitor_message"            // 15.
)

val mRightShowValue = arrayListOf("my_center", "balance","system_ppv","meessage_privateAlbum")


val mMessageClickValue = arrayListOf("unlocked", "vip", "popup_pp", "popup_pv","popup_privateAlbum")


val MESSAGE_CHAT_KEY = arrayListOf("click_systemPp", "click_systemPv", "popup_privateAlbum", "click_quickPpv","mid_FlashChat", "click_startDatingNow", "popup_guideVip", "click_guideVip","click_popupLimitedTime", "guide_turnOns")


const val appId = "d367bea11523960a"

fun preInitSolarEngine(content: Context) {
    try {
        SolarEngineManager.getInstance().preInit(content, appId)
    } catch (e: Exception) {
        e.printStackTrace()
    }

}

fun initSolarEngine(content: Context, onSuccess: () -> Unit = {}) {

    try {

        val mBuilder = SolarEngineConfig.Builder()
        if (!AppConstant.ClientInfo.OPEN_GOOGLE) {
            mBuilder.logEnabled()
                .isDebugModel(true)
        }
        val config = mBuilder.build()

        SolarEngineManager.getInstance().initialize(content, appId, config) {

            if (it == 0) {

                onSuccess()

                Log.i("TAG", "initSolarEngine: ${SolarEngineManager.getInstance().distinctId}")

            } else {
                //error
                Log.i("TAG", "initSolarEngine: $it")
            }

        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

}


fun loginSolarEngineUser(
    userId: String,
    firstLogin: Boolean,
    type: String,
    success: Boolean,
    userInfo: MutableMap<String, Any?>?,
    reason: String? = ""
) {

    try {

        if (firstLogin) {

            addRegisterEvent(type)

            if (null != userInfo){
                initSolarEngineUser(userInfo)
            }

        }

        addLoginEvent(type, success, reason)

        SolarEngineManager.getInstance().login(userId)


    } catch (e: Exception) {
        e.printStackTrace()
    }

}






fun initSolarEngineUser(userInfo: MutableMap<String, Any?>) {
    try {



        val jsonObject = JSONObject(userInfo)


        SolarEngineManager.getInstance().userInit(jsonObject)
    } catch (e: Exception) {
        e.printStackTrace()
    }


}


fun updateSolarEngineUser(key: String, value: Any?) {

    try {

        val params = mutableMapOf<String, Any?>()

        params[key] = value

        val jsonObject = JSONObject(params)

        SolarEngineManager.getInstance().userUpdate(jsonObject)
    } catch (e: Exception) {
        e.printStackTrace()
    }


}


fun addRegisterEvent(type: String) {

    try {
        val mSERegisterEventModel = SERegisterEventModel(type, "success", null)

        SolarEngineManager.getInstance().trackAppRegister(mSERegisterEventModel)

        reportAdjustRegEvent()

    } catch (e: Exception) {
        e.printStackTrace()
    }

}

fun addLoginEvent(type: String, success: Boolean, reason: String?) {

    try {

        val mSELoginEventModel = if (success) {

            SELoginEventModel(type, "success", null)

        } else {

            val mutableMapOf = mutableMapOf<String, Any?>()

            mutableMapOf["fail_reason"] = reason

            val jsonObject = JSONObject(mutableMapOf)

            SELoginEventModel(type, "fail", jsonObject)
        }

        SolarEngineManager.getInstance().trackAppLogin(mSELoginEventModel)
    } catch (e: Exception) {
        e.printStackTrace()
    }

}

fun addLoginOutEvent() {

    try {

        reportEvent("appLogout", mutableMapOf<String, Any?>().apply {

            put("appLogout", "appLogout")
        })

    } catch (e: Exception) {
        e.printStackTrace()
    }

}


fun reportRegisterStep(key: Int,isSkip : Boolean,isOrganic: Boolean) {

    try {
        val mutableMapOf = mutableMapOf<String, Any?>()

        mutableMapOf["step"] = REG_STEP_VALUE[key]

        mutableMapOf["method"] = if (isSkip) "skip" else "next step"

        mutableMapOf["user_traceability"] = if (isOrganic) "organic" else "ads"

        reportEvent(REG_STEP_KEY, mutableMapOf)

    } catch (e: Exception) {
        e.printStackTrace()
    }

}


fun reportEvent(key: String, value: Any) {

    val params = mutableMapOf<String, Any?>()

    params[key] = value

    addCustomEvent(key, params.toJson())

}

fun reportEvent(key: String, params: MutableMap<String, Any?>) {

    addCustomEvent(key, params.toJson())

}


private fun addCustomEvent(key: String, value: String) {

    try {
        val jsonObject = JSONObject(value)

        val mSECustomEventModel = SECustomEventModel(key, null, jsonObject)

        SolarEngineManager.getInstance().track(mSECustomEventModel)
    } catch (e: Exception) {
        e.printStackTrace()
    }

}


fun loginOutSolarEngineUser() {

    addLoginOutEvent()

    SolarEngineManager.getInstance().logout()

}