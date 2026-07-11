package com.chat.jolt.viewmodel

import android.location.Address
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.AppUtils
import com.chat.jolt.api.ApiService
import com.chat.jolt.data.ConfigData
import com.chat.jolt.data.HobbyTagData
import com.chat.jolt.data.ModelCardData
import com.chat.jolt.data.SocialAimData
import com.chat.jolt.data.TagData
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.helper.UserInfoHold.updateLocalUserInfo
import com.chat.lib_common.bus.SingleFlow
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.http.HttpClient
import com.chat.lib_common.tracking.loginSolarEngineUser
import com.chat.lib_common.tracking.payToAd
import com.chat.lib_common.util.dateFormat
import com.chat.lib_common.util.setCache
import com.chat.lib_common.util.showShort
import com.chat.lib_common.util.toJson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.sql.Date
import java.util.Locale
import java.util.TimeZone

class MainViewModel : PublicViewModel() {

    private val mApiService = HttpClient.createApi(ApiService::class.java)



    val mConfigData = SingleFlow<ConfigData>()

    val mSplashErrorStatus = SingleFlow<Throwable>()

    val mHobbyTagData = SingleFlow<MutableList<HobbyTagData>>()

    val mTurnTagData = SingleFlow<MutableList<TagData>>()

    val mSocialAimData = SingleFlow<MutableList<SocialAimData>>()

    val mModelCardData = SingleFlow<ModelCardData>()


    var mOnlineJob: Job? = null



    fun login(loginType: String, email: String, password: String) {

        val params = mutableMapOf<String, Any?>()
        params[AppConstant.Constant.LOGIN_TYPE] = loginType

        if (loginType == AppConstant.Constant.EMAIL) {
            params["email"] = email
            params["password"] = password
        } else {
            params["googleCode"] = email
        }


        doRequest(onRequest = {

            mApiService.login(params)
        }, onSuccess = {

            mUserInfoData.postValue(it.data)

            setCache(AppConstant.Constant.TOKEN, it.data.sessionId)

            setCache(AppConstant.Constant.IS_LOGIN, true)

            setCache(AppConstant.RIMConstant.RIM_TOKEN, it.data.imToken)

            updateLocalUserInfo(it.data)


            val mutableMapOf = mutableMapOf<String, Any?>()
            mutableMapOf["nickname"] = UserInfoHold.userInfo?.nickname
            mutableMapOf["age"] = UserInfoHold.userInfo?.age
            mutableMapOf["gender"] = UserInfoHold.userInfo?.sex
            mutableMapOf["register_time"] =
                Date(System.currentTimeMillis()).dateFormat(timeZone = TimeZone.getTimeZone("GMT-5"))
            mutableMapOf["country"] = Locale.getDefault()
            mutableMapOf["register_type"] = loginType
            mutableMapOf["app_name"] = "Jolt"
            mutableMapOf["registerapp_version"] = "V${AppUtils.getAppVersionName()}"
            mutableMapOf["useapp_version"] = "V${AppUtils.getAppVersionName()}"
            mutableMapOf["account_type"] = UserInfoHold.userInfo?.userType

            loginSolarEngineUser(it.data.userId,it.data.firstLogin == "True",loginType, true,mutableMapOf)

        }, onError = {

            when (it.code) {

                1101 -> {

                    showShort("Login failed. Incorrect account information.")
                }

                1103 -> {
                    showShort("Login failed. Incorrect account information.")
                }

                1110 -> {
                    showShort("Login failed. This account is no longer in use.")
                }

                1104 -> {

                    requestFailEvent.postValue(false)

                }

                in 1120..1149 -> {

                    requestFailEvent.postValue(false)

                }


                else -> {
                    showShort(it.message)
                }
            }


            requestFailEvent.postValue(it.message)


        }, onException = {


            requestFailEvent.postValue(it.message.toString())
            false
        })


    }

    fun findNews() {

        val params = mutableMapOf<String, Any?>()

        doRequest(
            onRequest = {
                mApiService.findNews(params)
            },
            onSuccess = {

                setCache(AppConstant.Constant.IS_VPN, it.data.vpnSwitch)

                setCache(AppConstant.Constant.IS_REVIEW_VERSION, it.data.reviewVersion)

                AppConstant.Constant.ppvsEnable = it.data.ppvsEnable == "True"

                AppConstant.Constant.threePay = it.data.threePay == "True"

                payToAd = it.data.payToAd != "False"

                mConfigData.postValue(it.data)
            }, onError = {


                val text = when (it.code) {


                    1101 -> {

                        "Login failed. Incorrect account information."
                    }

                    1103 -> {
                        "Login failed. Incorrect account information."
                    }

                    1110 -> {
                        "Login failed. This account is no longer in use."
                    }

                    1104 -> {

                        requestFailEvent.postValue(false)

                        "Login failed. Account restricted due to network risk."

                    }

                    in 1120..1149 -> {

                        requestFailEvent.postValue(false)

                        "Login failed. Account restricted due to network risk."
                    }


                    else -> {
                        it.message
                    }
                }
                requestFailEvent.postValue(text)


            }, onException = {

                mSplashErrorStatus.postValue(it)
                false
            })

    }

    fun configInfo() {

        val params = mutableMapOf<String, Any?>()

        doRequest(
            onRequest = {
                mApiService.configInfo(params)
            },
            onSuccess = {


                AppConstant.Constant.MEDIA_ENABLE_TIME = it.data.destroyPpvTime * 1000 + 1000


            }, onError = {


            })

    }

    fun initTag() {

        val params = mutableMapOf<String, Any?>()

        doRequest(onRequest = {
            mApiService.initTag(params)
        }, onSuccess = {

            mHobbyTagData.postValue(it.data)

            setCache(AppConstant.Constant.HOBBY_TAG, it.data.toJson())
        })


    }
    fun initTag2() {

        val params = mutableMapOf<String, Any?>()

        params["tagType"] = "TurnOns"

        doRequest(onRequest = {
            mApiService.initTag2(params)
        }, onSuccess = {

            mTurnTagData.postValue(it.data)

            setCache(AppConstant.Constant.TURN_TAG, it.data.toJson())
        })


    }

    fun initProfession() {

        val params = mutableMapOf<String, Any?>()

        doRequest(onRequest = {
            mApiService.initProfession(params)
        }, onSuccess = {

            setCache(AppConstant.Constant.PROFESSION, it.data.toJson())
        })


    }

    fun initSocialAim() {

        val params = mutableMapOf<String, Any?>()

        doRequest(onRequest = {
            mApiService.initSocialAim(params)
        }, onSuccess = {

            mSocialAimData.postValue(it.data)

            setCache(AppConstant.Constant.SOCIAL_AIM, it.data.toJson())
        })


    }



    fun saveLocation(
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        mAddress: Address?,
        granted: Int = 1,
    ) {
        val params = mutableMapOf<String, Any?>()

        params["lat"] = latitude

        params["lng"] = longitude

        if (null != mAddress) {
            params["country"] = mAddress.countryName
            params["city"] = mAddress.locality
            params["state"] = mAddress.adminArea
        }

        params["granted"] = granted

        doRequest(onRequest = {
            mApiService.saveLocation(params)
        }, onSuccess = {

        })


    }







    fun online() {

        doRequest(onRequest = {
            mApiService.online()
        }, onSuccess = {

            if (it.data.onlineId.isNullOrEmpty()) {

                return@doRequest
            }

            mOnlineJob?.cancel()


            mOnlineJob = viewModelScope.launch {

                while (isActive) {

                    delay(1000 * 30)

                    alive(it.data.onlineId!!)
                }

            }

        })

    }

    private fun alive(onlineId: String) {

        val params = mutableMapOf<String, Any?>()

        params["onlineId"] = onlineId


        doRequest(onRequest = {
            mApiService.alive(params)
        }, onSuccess = {

        })

    }




    fun getModelCard(
        distance: Int = 100,
        minAge: Int = 18,
        maxAge: Int = 36,
        sexType: String = "",
        hobbyTags: MutableList<String> = mutableListOf(),
    ) {

        val params = mutableMapOf<String, Any?>()
        params["distance"] = distance
        params["maxAge"] = maxAge
        params["minAge"] = minAge
        params["sexType"] = sexType
        params["hobbyTags"] = hobbyTags

        doRequest(onRequest = {
            mApiService.getModelCard(params)
        }, onSuccess = {


            mModelCardData.postValue(it.data)

        }, onException = {

            requestFailEvent.postValue(true)

            false
        })

    }





    fun cacheAllVipInfo() {

        requestCacheVipInfo(AppConstant.Constant.PAY_FLASH_CHAT)
        requestCacheVipInfo(AppConstant.Constant.PAY_VIP)

    }

    private fun requestCacheVipInfo(type: String) {

        val params = mutableMapOf<String, Any?>()

        params["tplType"] = type

        params["vipPopup"] = "False"

        doRequest(onRequest = {
            mApiService.getVipInfo(params)
        }, onSuccess = {

            cacheVipInfo(it.data, type)

        })
    }



}