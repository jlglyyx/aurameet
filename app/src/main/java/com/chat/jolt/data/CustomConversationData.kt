package com.chat.jolt.data

import io.rong.imlib.model.Conversation

data class CustomConversationData(var mConversation: Conversation) {

    var mConversationStatusData:ConversationStatusData? = null
}

data class ConversationStatusData(
    val onlineStatus: String,
    val age: Int,
    val relationId: String,
    val userId: Int,
    var receivedPPV: String?,
    val unlockedPPV: String?
)


class NewConversationData(var mConversation: Conversation?){

    var mConversationStatusData:ConversationStatusData? = null

    var count = 0

    var avatar: String? = ""

    var name = ""

    var addCount = 0

    var list = mutableListOf<ModelUserData>()
}