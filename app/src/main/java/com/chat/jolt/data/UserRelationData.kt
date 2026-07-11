package com.chat.jolt.data


data class UserRelationData(
    val blacklist: String,
    val beBlack: String?,
    val friendUser: RelationData,
    val user: RelationData,
    val matchSource: String?,
    val turnOnsTags: MutableList<String>?,
    val unlockAlbums: MutableList<UnlockAlbums>?,
    val unlockAlbums2: MutableList<UnlockAlbums>?,
    val userMsgCnt: Int
)

data class RelationData(
    val age: Int,
    val headPic: String?,
    val nickname: String?,
    val sex: String,
    val location: String?,
    val userId: String,
    val vipType: String,
    val mySign: String?,
    val packageType: String?,
    val vipStatus : Int?,
    val onlineStatus: String,
    val onlineStatusText: String,
    val coverPic: String,
)



data class UnlockAlbums(
    val albumId: String,
    var albumStatus: String,
    val albumType: String,
    val albumUrl: String,
    val id: String,
    var ttl: Int
){
    var status = -1
    var position = -1
}







