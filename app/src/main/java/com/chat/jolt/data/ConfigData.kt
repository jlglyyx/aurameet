package com.chat.jolt.data

data class ConfigData(
    val backImage: String,
    val bindModel: String,
    val chatterAccount: String,
    val payToAd: String,
    val reviewVersion: String,
    val showNewTaskTab: String,
    val threePay: String? = "False",
    val version: Version?,
    val vpnSwitch: String,
    var onlineId:String? = "",
    var ppvsEnable:String? = "False",
    var popupMsg: String? = null
){
    var destroyPpvTime = 0
}


data class Version(
    val installUrl: String?,
    val newVersion: String?,
    val updateType: String?,
    val updateContent: String?
)






