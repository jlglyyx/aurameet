package com.chat.lib_common.http

class HttpException(override var message: String, var code: Int) : Exception()