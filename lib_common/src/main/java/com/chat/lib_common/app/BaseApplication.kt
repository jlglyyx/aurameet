package com.chat.lib_common.app

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.blankj.utilcode.util.ProcessUtils
import com.bumptech.glide.Glide
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.im.RIMClient
import com.chat.lib_common.tracking.initAdjust
import com.chat.lib_common.tracking.initSolarEngine
import com.chat.lib_common.tracking.preInitSolarEngine
import com.danikula.videocache.HttpProxyCacheServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BaseApplication : Application() {


    private var proxy: HttpProxyCacheServer? = null

    companion object {

        private const val TAG = "BaseApplication"

        lateinit var mApplication: BaseApplication


        var isAppForeground = true

    }

    fun getProxy(): HttpProxyCacheServer {

        return proxy ?: newProxy().also { proxy = it }
    }

    private fun newProxy(): HttpProxyCacheServer {
        return HttpProxyCacheServer.Builder(this)
            .maxCacheSize((1024 * 1024 * 1024).toLong()) // 1 Gb for cache
            .build()
    }


    override fun onCreate() {

        mApplication = this
        super.onCreate()

        initGlide(mApplication)


        if (ProcessUtils.isMainProcess()) {

            try {
                preInitSolarEngine(mApplication)
                initSolarEngine(mApplication)
                initRIM(mApplication)
                initAdjust(mApplication)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }



        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                isAppForeground = true

                FlowBus.with(AppConstant.EventConstant.APP_ENTERED_FOREGROUND).postValue(true)

            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                isAppForeground = false

            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        mApplication = this
    }


    private fun initGlide(application: BaseApplication) {
        Glide.get(application)
    }

    private fun initRIM(application: BaseApplication) {
        RIMClient.initRIMCoreClient(application)
        RIMClient.initRIMPush(application)
        RIMClient.logout()
//        RIMClient.onClientRIM(onSuccess = {
//
//        })
    }


    private fun initAdjust(application: BaseApplication) {

        try {

           initAdjust(application){


           }

            val build = InstallReferrerClient.newBuilder(application).build()


            build.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {

                    try {

                        if (!build.isReady) return

                        when (responseCode) {
                            InstallReferrerClient.InstallReferrerResponse.OK -> {
                                // Connection established.

                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val response: ReferrerDetails? = build?.installReferrer

                                        val referrerUrl = response?.installReferrer

                                        if (referrerUrl?.isNotEmpty() == true) {
                                            FlowBus.with(AppConstant.EventConstant.APP_INIT_ADJUST)
                                                .postValue(referrerUrl)
                                        }
                                    } finally {
                                        if (build.isReady) build.endConnection()
                                    }
                                }
                            }

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onInstallReferrerServiceDisconnected() {

                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }




    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        try {
            if (level >= TRIM_MEMORY_UI_HIDDEN) {
                Glide.get(this).clearMemory()
            } else {
                Glide.get(this).onTrimMemory(level)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        try {
            Glide.get(this).onLowMemory()
            Glide.get(this).clearMemory()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}