package com.chat.lib_common.http

import android.os.Build
import android.util.Base64
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.Utils
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.util.getCache
import okio.Buffer
import java.io.EOFException
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.copyOf
import kotlin.collections.set
import kotlin.text.replace
import kotlin.text.substring
import kotlin.text.toByteArray


object HttpSecurityUtils {

    private var headerConfig = ConcurrentHashMap<String, String?>()

    fun createSecretKey(): String {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16)
    }

    fun encryptKey(params: String, publicKey: String): String {
        val data = EncryptUtils.encryptRSA(
            params.toByteArray(Charsets.UTF_8),
            Base64.decode(publicKey.toByteArray(), Base64.NO_WRAP),
            1024,
            "RSA/ECB/PKCS1Padding"
        )
        return String(Base64.encode(data, Base64.NO_WRAP))
    }

    fun encrypt(params: String, secretKey: String): String {
        val keyBytes = secretKey.toByteArray(Charsets.US_ASCII).copyOf(16)
        val data = EncryptUtils.encryptAES2Base64(
            params.toByteArray(Charsets.UTF_8),
            keyBytes,
            "AES/ECB/PKCS5Padding",
            null
        )
        return String(data)
    }

    fun decrypt(params: String, secretKey: String): String {
        val keyBytes = secretKey.toByteArray(Charsets.US_ASCII).copyOf(16)
        val data = EncryptUtils.decryptBase64AES(
            params.toByteArray(),
            keyBytes,
            "AES/ECB/PKCS5Padding",
            null
        )
        return if (null == data) "" else String(data)
    }



    private fun initHeaderService() {
        headerConfig["S"] = getCache(AppConstant.Constant.TOKEN, "")
        headerConfig["K"] = ""
        headerConfig["L"] = Locale.getDefault().language
        headerConfig["T"] = "A"
        headerConfig["V"] = "${AppUtils.getAppVersionName()}.${AppUtils.getAppVersionCode()}"
        headerConfig["D"] = Build.BRAND
        headerConfig["M"] = DeviceUtils.getModel()
        headerConfig["N"] = io.rong.imlib.common.DeviceUtils.getNetworkType(Utils.getApp())
        headerConfig["R"] = NetworkUtils.getNetworkOperatorName()
        headerConfig["O"] = io.rong.imlib.common.DeviceUtils.getDeviceBandModelVersion()
        headerConfig["X"] = ""
        headerConfig["I"] = DeviceUtils.getUniqueDeviceId()
        headerConfig["W"] = ""
        headerConfig["AID"] = DeviceUtils.getAndroidID()
        headerConfig["H"] = System.currentTimeMillis().toString()
        headerConfig["C"] = ""
        headerConfig["E"] = ""
        headerConfig["F"] = ""
        headerConfig["A"] = ""
        headerConfig["U"] = ""
        headerConfig["P"] = "Jolt"
    }

    fun mapToQueryString(): String {

        val query = kotlin.text.StringBuilder()

        try {
            initHeaderService()


            for (key in headerConfig.keys) {
                try {
                    query.append(key).append("=").append(headerConfig[key]).append("&")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (query.isNotEmpty()) {
                query.deleteCharAt(query.length - 1)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return query.toString()
    }

    internal fun Buffer.isProbablyUtf8(): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = this.size.coerceAtMost(64)
            this.copyTo(prefix, 0, byteCount)
            for (i in 0 until 16) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (_: EOFException) {
            return false // Truncated UTF-8 sequence.
        }
    }

}