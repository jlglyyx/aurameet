package com.chat.jolt.data

import android.net.Uri
import io.rong.imlib.model.Message

data class CustomMessage(val message:Message){

    var id : String = ""

    var text : String = ""

    var uri : Uri? = null

    var sendMessageType : String? = null

    val time:Long = System.currentTimeMillis()

    //0 load 1 error 2 success
    var status = 0

    var errorReason = ""

    var isLocal = false

    var mUserRelationData:UserRelationData? = null

    var position = -1

}