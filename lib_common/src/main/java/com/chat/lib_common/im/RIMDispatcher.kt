package com.chat.lib_common.im

import android.util.Log
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import io.rong.imlib.model.ReceivedProfile

object RIMDispatcher  {

    interface MessageListener {
        fun onMessageReceiptResponse(message: Message,type: Conversation.ConversationType,
                                      targetId: String,
                                      mReceivedProfile:ReceivedProfile)
    }

    private val listeners = mutableSetOf<MessageListener>()

    fun addListener(listener: MessageListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: MessageListener) {
        listeners.remove(listener)
    }
    fun removeAllListener() {
        listeners.clear()
    }



    fun onDispatch(
        message: Message?,
        mReceivedProfile:ReceivedProfile
    ) {

        if (null == message) return

        if (listeners.isEmpty()){

            return
        }

        Log.i("TAG", "onDispatch: = $listeners")
        for (listener in listeners) {
            listener.onMessageReceiptResponse(message,message.conversationType,message.targetId,mReceivedProfile)
        }
    }


}
