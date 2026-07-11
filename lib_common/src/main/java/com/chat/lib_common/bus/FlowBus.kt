package com.chat.lib_common.bus

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

object FlowBus {

    private val bus = ConcurrentHashMap<String, MutableSharedFlow<Any>>()


    @Suppress("UNCHECKED_CAST")
    private fun <T> withFlow(key: String,replay:Int): MutableSharedFlow<T> {
        return bus.getOrPut(key) {
            MutableSharedFlow(
                replay = replay,
                extraBufferCapacity = 1,
                onBufferOverflow = BufferOverflow.DROP_OLDEST
            )
        } as MutableSharedFlow<T>
    }

    fun with(key: String,replay:Int = 0):MutableSharedFlow<Any>{

        return withFlow(key,replay)
    }


    fun  <T> MutableSharedFlow<T>.postValue(value: T){

        this.tryEmit(value)
    }

    suspend fun <T> MutableSharedFlow<T>.emitValue(value: T) {
        emit(value)
    }


   inline fun MutableSharedFlow<Any>.observe(owner: LifecycleOwner, crossinline onObserve:(Any) ->Unit){

       owner.lifecycleScope.launch {

            this@observe.collect{

                onObserve(it)
            }

        }
    }



}