package com.chat.jolt.helper

import com.chat.jolt.activity.ChatActivity
import java.lang.ref.WeakReference

object ChatHelper {

    val mChatList = mutableListOf<WeakReference<ChatActivity>>()

    fun add(mChatActivity:ChatActivity){


        try {

        }catch (e: Exception){
            e.printStackTrace()
        }

        val weakReference = WeakReference(mChatActivity)

        mChatList.add(weakReference)
    }

    fun remove(mChatActivity:ChatActivity){

        try {
            val weakReference = WeakReference(mChatActivity)

            if (mChatList.isNotEmpty()){

                mChatList.remove(weakReference)

            }
        }catch (e: Exception){
            e.printStackTrace()
        }



    }

    fun removeLast(){

        try {
            if (mChatList.isNotEmpty()){

                mChatList.forEach {

                    it.get()?.finish()
                }

                mChatList.clear()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }


    }
}