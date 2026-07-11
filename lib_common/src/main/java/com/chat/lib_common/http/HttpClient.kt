package com.chat.lib_common.http

import com.google.gson.GsonBuilder
import com.chat.lib_common.constant.AppConstant
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object HttpClient {

    private val mHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpInterceptor.HeadInterceptor())
        .addInterceptor(HttpInterceptor.LogInterceptor())
        .connectTimeout(AppConstant.ClientInfo.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
        .readTimeout(AppConstant.ClientInfo.READ_TIMEOUT, TimeUnit.MILLISECONDS)
        .writeTimeout(AppConstant.ClientInfo.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
        .build()



   private val mRetrofit = Retrofit.Builder().baseUrl(AppConstant.ClientInfo.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().disableHtmlEscaping().setDateFormat("yyyy-MM-dd HH:mm:ss").create()))
        .client(mHttpClient)
        .build()


    fun <T> createApi(clazz: Class<T>):T{


       return mRetrofit.create(clazz)
    }

}