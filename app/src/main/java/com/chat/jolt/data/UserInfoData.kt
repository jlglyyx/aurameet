package com.chat.jolt.data


data class UserInfoData(
    var userId: String,
    var userType: String?,
    var firstLogin: String = "",
    var imToken: String = "",
    var sessionId: String = "",
    var nickname: String? = "",
    var headPic: String? = "",
    var headStatus: String? = "",
    var email: String? = "",
    var sex: String? = "",
    var age: String? = "",
    var birthDay: String? = "",
    var constellation: String? = "",
    var height: String? = "",
    var weight: String? = "",
    var hometown: String? = "",
    var profession: String? = "",
    var mySign: String? = "",
    var coverPic: String? = "",
    var coverStatus: MutableList<String> = mutableListOf(),
    var coverPics: MutableList<String>? = mutableListOf(),
    var socialAim: String? = "",
    var city: String? = "",
    var ipCity: String? = "",
    var distance: String? = "",
    var stationCityAddr: String? = "",
    var black: String?,
    var targetBlack: String?,
    var vipType: String?,
    var vipExpireDate: String? = "",
    var flashChatCount: Int = 0,
    var wlmCount: Int? = 0,
    var privatePhotoCount: Int? = 0,
    var privateVideoCount: Int? = 0,
    var visitorCnt: Int? = 0,
    var hobbyTags: MutableList<String>? = mutableListOf(),
    var hobbyTagContents: MutableList<String>? = mutableListOf(),
    var commonHobbyTags: MutableList<String>? = mutableListOf(),
    var turnOnsTags: MutableList<String>? = mutableListOf(),
    var freeFlashChatCount: Int? = 0,
    val ratePopup: String?,
    val onlineStatus: String?,
    val popupConditions: List<String>?,
    val timeLimitPremiumTtl: Int = 0,
    val privateAlbums: MutableList<ModelMediaData>? = mutableListOf(),
    val publicAlbums: MutableList<ModelMediaData>? = mutableListOf(),

    val follow: String,
    val friend: String,
    val scene: String,
    val lowUser: String,
    val newUser: String,
    val organic: String? = "True",
    val regFirstDay: String,
    val replyCnt: Int,
    val usedFlashChat: String,
    val reviewVersion: String?,
){

   var  mCustomMessageExtraData:CustomMessageExtraData? = null
}


class UploadPictureData{

    var url:Any? = null

    var httpUrl:String? = null

    var status:Int = 0

    var albumStatus: String = "Pass"

    var id:String = "ID${System.currentTimeMillis()}${(Math.random() * 1000).toInt()}"
}

class UpdateUserInfoData{
    var birthday: String? = null
    var coverPics: MutableList<String>? = null
    var headPic: String? = null
    var height: String? = null
    var hobbyTags: MutableList<String>? = null
    var hometown: String? = null
    var mySign: String? = null
    var nickname: String? = null
    var profession: String? = null
    var sex: String? = null
    var socialAim: String? = null
    var turnOnsTags: MutableList<String>? = null
    var weight: String? = null
    var errorCode: Int? = null

}










