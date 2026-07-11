package com.chat.lib_common.bus

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class SingleFlow<T> {

    private val _events = MutableSharedFlow<T>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    @Volatile
    private var currentValue: T? = null

    fun currentValue(): T? = currentValue

    private val events: SharedFlow<T> = _events

    suspend fun setValue(value: T) {
        try {
            _events.emit(value)
        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    fun postValue(value: T) {
        try {
            _events.tryEmit(value)
        } catch (e: Exception) {

            e.printStackTrace()
        }

    }

    fun observe(owner: LifecycleOwner, block: (T) -> Unit) {
        owner.lifecycleScope.launch {
                try {
                    events.collect {
                        currentValue = it
                        block(it)
                    }
                } catch (e: Exception) {

                    e.printStackTrace()
                }
        }
    }
}