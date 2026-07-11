package com.chat.jolt.data

data class PreViewMediaData(val id: String, val url: String, val cover: String?, var time: Int, var status: Int){

    var uri: String? = null

    var isCheck = false

}

