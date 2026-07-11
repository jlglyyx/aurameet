package com.chat.lib_common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.bus.SingleFlow
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.http.HttpException
import com.chat.lib_common.http.HttpResult
import com.chat.lib_common.util.showShort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

open class BaseViewModel : ViewModel() {

    val TAG = this.javaClass.simpleName

    val requestFailEvent:SingleFlow<Any> = SingleFlow()


    var loginOut:() -> Unit = {}




    suspend fun <T : Any> withContextIO(mResult: suspend () -> HttpResult<T>): HttpResult<T> {
        return withContext(Dispatchers.IO) {

            val httpResult = mResult()

            if (httpResult.code == 200) httpResult else throw HttpException(httpResult.message,httpResult.code)

        }
    }


     fun <T : Any> doRequest(
        onRequest:suspend () -> HttpResult<T>,
        onSuccess:suspend (HttpResult<T>) -> Unit = {},
        onError:suspend (HttpException) -> Unit = {},
        onException:suspend (Throwable) -> Boolean = { false },
        showLoading: Boolean = false
    ) {
        viewModelScope.launch {
            try {

                if (showLoading){

                    delay(1000)
                }

                onSuccess(withContextIO {
                    onRequest()
                })
            } catch (t: Throwable) {
                t.printStackTrace()

                when (t) {
                    is HttpException -> {

                        when (t.code){

                            500 ->{

                                loginOut()

                            }
                            501,502 ->{

                                showShort(t.message)

                                onError(t)
                            }
                            else ->{
                                onError(t)
                            }
                        }
                    }

                    is CancellationException -> {}

                    else -> {

                        val needShowToast = onException(t)

                        if (needShowToast){
                            ToastUtils.showShort(t.message)
                        }
                    }
                }

            }finally {

                if (showLoading){

                    delay(1000)

                }

            }
        }
    }


}