package com.chat.lib_common.data

import android.net.Uri

data class OssData(
    val AccessKeyId: String?,
    val AccessKeySecret: String?,
    val SecurityToken: String?,
    val StatusCode: String?,
    var uploadId: String?
){

    var sendMessageType: String? = ""
    var uploadType: String? = ""
    var uploadUri: Uri? = null
}
