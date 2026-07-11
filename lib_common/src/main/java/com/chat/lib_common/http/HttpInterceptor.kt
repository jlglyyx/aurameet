package com.chat.lib_common.http

import android.util.Log
import com.chat.lib_common.BuildConfig
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.http.HttpSecurityUtils.isProbablyUtf8
import com.chat.lib_common.http.HttpSecurityUtils.mapToQueryString
import com.chat.lib_common.util.fromJson
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.net.URLDecoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8

object HttpInterceptor {



    class HeadInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {

            return try {

                createParam(chain)

            } catch (e: Exception) {

                Log.i("TAG", "error -> intercept:${chain.request().url} -> ${e.message.toString()}")

                chain.proceed(chain.request())
            }
        }


        private fun createParam(chain: Interceptor.Chain): Response {

            var request = chain.request()

            val header = request.headers.newBuilder()


            val key = HttpSecurityUtils.createSecretKey()


            header["x-tid"] = "A-${key}"
            header["x-arg"] = HttpSecurityUtils.encryptKey(key, AppConstant.Constant.PUBLIC_KEY)
            val params1 = mapToQueryString()
            Log.i("TAG", "intercept: $params1")
            header["x-inf"] = HttpSecurityUtils.encrypt(params1, key)


            val requestBody = request.body
            val buffer = Buffer()
            requestBody?.writeTo(buffer)

            val contentType = requestBody?.contentType()

            val charset: Charset = contentType?.charset(UTF_8) ?: UTF_8

            var encryptData = ""
            if (buffer.isProbablyUtf8()) {
                val body = buffer.readString(charset)
                encryptData = HttpSecurityUtils.encrypt(body, key)
            }


            val param = "{\"data\":\"$encryptData\"}"


            val newBody = param.toRequestBody(contentType)

            request = request.newBuilder().headers(header.build()).apply {

                if (encryptData.isNotEmpty()) {
                    post(newBody)
                }

            }.build()


            var response = chain.proceed(request)
            if (response.isSuccessful && response.body != null) {
                val responseBody = response.body

                val content = HttpSecurityUtils.decrypt(responseBody!!.string(), key)

                val newResponseBody = content.toResponseBody(contentType)
                response = response.newBuilder().body(newResponseBody).build()

            }

            return response

        }

    }

    class LogInterceptor : Interceptor {

        private val TAG = "LogInterceptor"

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            var response = chain.proceed(request)

            if (!BuildConfig.DEBUG){

                return response
            }

            val key = request.header("x-tid")?.substring(2)
            Log.d(
                AppConstant.ClientInfo.TAG_LOG,
                "intercept: ===============request==============="
            )
            Log.d(
                AppConstant.ClientInfo.TAG_LOG,
                "request.url: ${URLDecoder.decode(request.url.toString(), "UTF-8")}\n"
            )
            Log.d(AppConstant.ClientInfo.TAG_LOG, "request.method: ${request.method}\n")
            Log.d(AppConstant.ClientInfo.TAG_LOG, "request.headers: ${request.headers}\n")
            if (BuildConfig.DEBUG) {
                val body = getBody(request)
                Log.d(AppConstant.ClientInfo.TAG_LOG, "request.body: $body\n")
                try {

                    if (body.isNotEmpty() && body != "zzzzzz") {

                        val fromJson = body.fromJson<HttpResult<String>>()

                        Log.d(
                            AppConstant.ClientInfo.TAG_LOG,
                            "request.body: ${HttpSecurityUtils.decrypt(fromJson.data, key ?: "")}\n"
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }


            }
            Log.d(
                AppConstant.ClientInfo.TAG_LOG,
                "intercept: ===============request==============="
            )
            Log.d(
                AppConstant.ClientInfo.TAG_LOG,
                "intercept: ===============Start --- Response==============="
            )
            Log.d(
                AppConstant.ClientInfo.TAG_LOG,
                "response.isSuccessful: ${response.isSuccessful}\n"
            )
            Log.d(AppConstant.ClientInfo.TAG_LOG, "response.message: ${response.message}\n")
            Log.d(AppConstant.ClientInfo.TAG_LOG, "response.headers: ${response.headers}\n")
            Log.d(AppConstant.ClientInfo.TAG_LOG, "response.code: ${response.code}\n")
            val content = response.body?.string()
            val contentType = response.body?.contentType()
            //Log.d(TAG_LOG, "response.body: ${content?.length} ${content}\n")


            if (BuildConfig.DEBUG) {
                decryptResponse(content.toString(), 5000, key ?: "")
            }


            Log.d(AppConstant.ClientInfo.TAG_LOG, "response.request.url: ${response.request.url}\n")
            Log.d(
                AppConstant.ClientInfo.TAG_LOG,
                "intercept: ===============End --- Response==============="
            )
            response = response.newBuilder().body(content?.toResponseBody(contentType)).build()
            return response
        }


        private fun getBody(request: Request): String {
            try {

                val body = request.body ?: return "zzzzzz"

                if (request.body!!.contentType().toString().contains("multipart")) {
                    return "[]"
                }

                if (body.contentLength() > 1024 * 10) {
                    return "[: ${body.contentLength()} bytes]"
                }

                val buffer = Buffer()

                body.writeTo(buffer)

                val contentType = body.contentType()
                val charset = contentType?.charset(Charsets.UTF_8)
                return if (charset != null) {
                    buffer.readString(charset)
                } else {
                    "zzzzzz"
                }
            } catch (e: Exception) {
                Log.i(TAG, "getBody: ${e.message}")
            }
            return ""
        }


        private fun decryptResponse(content: String, size: Int, key: String) {


            try {
                val decryptedLog = HttpSecurityUtils.decrypt(content, key) //

                showLogCompletion(decryptedLog, size)


            } catch (e: Exception) {
                e.printStackTrace()
            }

        }


        private fun showLogCompletion(log: String, size: Int) {

            try {


//                val log = RSAUtils.decrypt(content, key)


                if (log.length > size) {
                    val substring = log.substring(0, size)
                    Log.d(AppConstant.ClientInfo.TAG_LOG, "response.body: ${substring}")
                    if (log.length - substring.length > size) {
                        val substring1 = log.substring(substring.length, log.length)
                        showLogCompletion(substring1, size)
                    } else {
                        val substring1 = log.substring(substring.length, log.length)
                        Log.d(AppConstant.ClientInfo.TAG_LOG, "${substring1}")
                    }

                } else {
                    Log.d(AppConstant.ClientInfo.TAG_LOG, "response.body: ${log}\n")
                }

            } catch (e: Exception) {

                e.printStackTrace()
            }


        }
    }






}