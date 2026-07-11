package com.chat.jolt.data


data class VipData(
    val payTypes: String,
    val privilegeList: MutableList<Privilege>,
    val privilegeList2: List<Privilege>,
    val subscribeNums: Int,
    val subscription: String,
    val tplList: MutableList<Tpl>,
    val vipType: String
){
    var type:String? = null
    var showType:String? = null
    var targetId:String? = null
    var userId2:String? = null
    var name2:String? = null
    var buyRightCount:Int? = null
    var isMeInto:Int = 0
    var intoType:String? = null
}

data class Privilege(
    val remark: String,
    val remarkList: List<String>?,
    val title: String,
    val type: String
)


data class Tpl(
    val count: Int,
    val dayMoney: String,
    val discountInfo: String?,
    var money: String,
    var formatMoney: String?,
    val productId: String,
    val tplId: Int,
    val tplName: String,
    val defaultTpl: String? = "False"
){
    var isSelect = false
    var type:String? = null
    var prePrice:String? = null
    var oriPrice:String? = null
    val dayUnit: String? = "0.0"
    val ttl: Int = 0
}