package com.chat.jolt.data




data class SocialAimData(

    val socialAim: String,

    val socialAimName: String
){
    var isCheck = false
}



data class HobbyTagData(
    val hobbyTag: String,
    val hobbyTagName: String
){
    var isCheck = false
}


data class ProfessionData(
    val profession: String,
    val professionName: String
)



data class TagData(
    val tagDesc: String,
    val tagName: String,
    val tagType: String,
    val tagUrl: String,
    val userTag: String
){

    var isCheck = false

}