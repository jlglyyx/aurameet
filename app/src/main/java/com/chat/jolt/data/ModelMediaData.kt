package com.chat.jolt.data

import android.net.Uri


data class ModelMediaData(
    var albumId: String?,
    var id: String,
    var albumStatus: String?,
    var albumType: String,
    var albumUrl: String?,
    var videoSeconds: Int,
    var videoCover: String?,

    var url: String?,
    var cover: String?,
    var duration: Int?,
    var tag: Int?,
    var ttl: Int = 0,
    var turnOnsGuide: String = "False",

){
    var content: String? = null
    var msgId: String? = null
    var sendType: String? = null
    var isCheck: Boolean? = null
    var isSelect: Boolean? = null
    var itemTitle :String? = null
    var itemType :Int = 0
    var uploadId :String? = null

    var localMessageId:String = ""

    var sendMessageStatus:Int = 0

    //0 load 1 error 2 success
    var uploadStatus = 0

    var uri : Uri? = null

    var isLocal = false

    var errorCode: Int? = null
    var destroyTime: String? = null
}


data class CurrentModelMediaData(val index:Int,var data:MutableList<ModelMediaData>?,val mSendModelMediaData:SendModelMediaData?)




data class SendModelMediaData(
    val albumVOs: MutableList<ModelMediaData>,
    val send1: String,
    var send4: String,
    val send9: String
)


