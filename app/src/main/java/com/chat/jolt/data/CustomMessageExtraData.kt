package com.chat.jolt.data

import io.rong.imlib.model.Message


class CustomMessageExtraData {

    var content: String? = null
    var title: String? = null
    var headPic1: String? = null
    var headPic2: String? = null
    var name1: String? = null
    var name2: String? = null
    var userId1: String? = null
    var userId2: String? = null
    var age1: Int? = null
    var age2: Int? = null
    var source: String? = null
    var groupId: String? = null
    var firstChat: String? = null
    var tzId1: String? = null
    var eventCode: String? = null
    var oriSource: String? = null
    var isPrivate: String? = null
    var data: CustomMessageExtraData? = null
    var position: Int? = null
    var albumIds: String? = null
    var albumId: String? = null
    var msgId: String? = null
    var mMessage: Message? = null
    var ppPrice: String? = null
    var pvPrice: String? = null
    var timestamps: MutableList<Long>? = null
}



class CustomLockMessageData {

    var isLocked: String? = "True"

    var unlockTimestamp: Long? = 0

    var isDestroy: String? = "False"

}

