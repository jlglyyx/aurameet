package com.chat.lib_common.http

data class HttpResult<T : Any>(
    val data: T, val code: Int = -1, val message: String = "",val success : Boolean = false,val total:Int? = null,val count:Int? = null
)

