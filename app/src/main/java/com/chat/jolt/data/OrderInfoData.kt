package com.chat.jolt.data

import com.android.billingclient.api.Purchase


data class OrderInfoData(
    val bizId: Int,
    val purchaseToken: String?,
    val privilegeList: List<Privilege>,
    var type: String?,
    var productId: String?,
    val content: Content?,
    val reason: String?,
    val status: String?, // Wait, Success, Refund, Retry
){

    var mPurchase: Purchase? = null

}


data class Content(
    val payUrl: String
)


