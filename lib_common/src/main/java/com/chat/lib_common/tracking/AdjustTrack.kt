@file:JvmName("AdjustTrack")

package com.chat.lib_common.tracking

import android.content.Context
import com.adjust.sdk.Adjust
import com.adjust.sdk.Adjust.trackEvent
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustEvent
import com.adjust.sdk.LogLevel
import com.blankj.utilcode.util.Utils
import com.chat.lib_common.constant.AppConstant
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.text.toDouble


var payToAd = true


const val adjustId = "snutbdld7l6o"




fun initAdjust(content: Context, onSuccess: () -> Unit = {}) {
    try {
        val appToken = adjustId
        val environment = if(AppConstant.ClientInfo.OPEN_GOOGLE) AdjustConfig.ENVIRONMENT_PRODUCTION else AdjustConfig.ENVIRONMENT_SANDBOX
        val config = AdjustConfig(content, appToken, environment)
        config.setLogLevel(if(AppConstant.ClientInfo.OPEN_GOOGLE) LogLevel.WARN else LogLevel.VERBOSE)
        Adjust.initSdk(config)
        onSuccess()
    } catch (e: Exception) {
        e.printStackTrace()
    }

}


fun reportAdjustRegEvent() {
    trackEvent(AdjustEvent("u2gw2n"))
}
fun reportAdjustPayEvent(bizId: String?, payAmount: String?,userId: String) {
    if (!payToAd) return
    val event = AdjustEvent("sfvr44")
    event.setRevenue(payAmount?.toDouble()?:0.0, "USD")
    event.addCallbackParameter("bizId", bizId)
    reportEvent(event,userId)
}

private fun reportEvent(event: AdjustEvent,userId: String) {

    event.addCallbackParameter("userId", userId)

    val env = when(AppConstant.ClientInfo.BASE_URL){

        AppConstant.ClientInfo.BASE_REAL_URL ->{
            "prod"
        }
        AppConstant.ClientInfo.BASE_TEST_URL ->{
            "test"
        }
        else ->{
            "dev"
        }
    }
    event.addCallbackParameter("env", env)
    trackEvent(event)
}


suspend fun getAdId(): String {
    return suspendCancellableCoroutine { coroutine ->
        Adjust.getAdid {
            if (coroutine.isActive) {
                coroutine.resume(it)
            }
        }
    }
}
suspend fun getGoogleAdId(): String {
    return suspendCancellableCoroutine { coroutine ->
        Adjust.getGoogleAdId(Utils.getApp()) {
            if (coroutine.isActive) {
                coroutine.resume(it)
            }
        }
    }
}
