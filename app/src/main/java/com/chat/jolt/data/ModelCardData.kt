package com.chat.jolt.data

data class ModelCardData(
    val exhaustedBackPics: MutableList<String>,
    val flashChatCount: Int,
    val maxCount: Int,
    val nextFlushCardTime: Int,
    val paddleCount: Int,
    val totalCount: Int,
    val userList: MutableList<ModelUserData>
)

data class ModelUserData(
    val age: Int,
    val aim: String?,
    val coverPic: String,
    var coverPics: MutableList<String>?,
    var headPic: String,
    val hobbyTagContents: MutableList<String>?,
    val hobbyTags: MutableList<String>,
    val commonHobbyTags: MutableList<String>?,
    val turnOnsTags: MutableList<String>?,
    val mySign: String,
    val nearby: String?,
    val nickname: String,
    val onlineStatus: String,
    var publicPic: String,
    val sexType: String,
    val userId: String,
    val vipType: String,
    val distance: String,
    val visitorTimes: String
)



data class LikeStatusData(
    val firstRightPaddle: String,
    val flashChatCount: Int,
    val groupId: String,
    val newFriend: String,
    val turnOnsGuide: String?,
    val paddleCount: Int,
    var userId: String?
)

data class WlmData(
    val userList: MutableList<ModelUserData>,
    val totalCount: Int,
    val wlmCount: Int,
    val groupId: String,
    val newFriend: String,
)

data class ILikeImageData(
    val userId: String,
    val cover: String,
)





