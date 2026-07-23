package com.chat.jolt.helper

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.ColorUtils
import com.chat.jolt.R
import com.chat.jolt.activity.ChatActivity
import com.chat.jolt.activity.VisitorActivity
import com.chat.jolt.databinding.FloatMessageBinding
import com.chat.jolt.databinding.FloatSwipGuideBinding
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.im.message.PPVMessage
import com.chat.lib_common.tracking.mMessageNoticeKey
import com.chat.lib_common.tracking.mPopPopupDialogKey
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.click
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.edgeToEdgeTop
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.mTranslation
import com.chat.lib_common.util.startActivity
import io.rong.imlib.model.Message
import io.rong.message.TextMessage
import java.lang.ref.WeakReference
import kotlin.apply
import kotlin.jvm.java
import kotlin.let

object FloatingWindowUtil {


    private var floatingView: WeakReference<View>? = null

    private var addAnimator: ObjectAnimator? = null

    private var closeAnimator: ObjectAnimator? = null

    private var startY = 0f

    private var hasSlid = false

    private var isClosing = false

    private val handler = Handler(Looper.getMainLooper())

    private val autoCloseRunnable = Runnable {

        if (null != floatingView){
            closeWithAnimation()
        }else{
            removeCallbacks()
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    fun showFloatMessage(context: Context, message: Message) {


        if (context is LifecycleOwner) {
            context.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    remove()
                }
            })
        }

        if (message.messageDirection == Message.MessageDirection.SEND) return

        if (AppConstant.Constant.isShowBuy) return

        val  windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val resultView: View? = when (message.objectName) {

            AppConstant.RIMConstant.RC_TXT_MSG,
            AppConstant.RIMConstant.RC_IMG_MSG,
            AppConstant.RIMConstant.RC_IMG_VIDEO,
            AppConstant.RIMConstant.RC_PP_VM_MSG
            -> {

                if (message.senderUserId == AppConstant.RIMConstant.SYSTEM_NOTICE) return

                val mFloatMessageBinding =
                    FloatMessageBinding.inflate(LayoutInflater.from(context))

                val messageContent = message.content

                val messageExtraData = getMessageExtraData(message)

                if (null == messageExtraData){

                    return
                }

                mFloatMessageBinding.ivAvatar.loadImage(
                    BaseApplication.mApplication,
                    messageExtraData.headPic2
                )

                mFloatMessageBinding.tvName.text = messageExtraData.name2


                if (message.objectName == AppConstant.RIMConstant.RC_TXT_MSG) {

                    mFloatMessageBinding.tvMessage.text = (messageContent as TextMessage).content

                } else if (message.objectName == AppConstant.RIMConstant.RC_IMG_MSG) {

                    mFloatMessageBinding.tvMessage.text = if (messageExtraData.isPrivate == "True") "[Private Photo]" else "[Photo]"

                } else if (message.objectName == AppConstant.RIMConstant.RC_IMG_VIDEO){

                    mFloatMessageBinding.tvMessage.text = if (messageExtraData.isPrivate == "True") "[Private Video]" else "[Video]"
                }else{

                    if (messageContent is PPVMessage){

                        mFloatMessageBinding.tvMessage.text =  if (messageContent.type == AppConstant.RIMConstant.RC_SEND_PRIVATE_IMAGE_S_MSG) "[Private Photo]" else "[Private Video]"
                    }


                }

                mFloatMessageBinding.root.click {

                    ChatHelper.removeLast()

                    context.createIntent(ChatActivity::class.java)
                        .putExtra(
                            AppConstant.Constant.TARGET_ID,
                            message.targetId
                        )
                        .putExtra(
                            AppConstant.Constant.IS_NOTICE_INTO,
                            true
                        )

                        .startActivity(context)
                    remove()
                }

                mFloatMessageBinding.root

            }


            AppConstant.RIMConstant.RC_CMD_MSG -> {

                val cmdMessageExtraData = getCmdMessageExtraData(message)

                if (null == cmdMessageExtraData) return

                val messageExtraData = cmdMessageExtraData.data?:return

                val mFloatMessageBinding =
                    FloatMessageBinding.inflate(LayoutInflater.from(context))


                mFloatMessageBinding.ivAvatar.loadImage(
                    BaseApplication.mApplication,
                    messageExtraData.headPic2
                )

                mFloatMessageBinding.tvName.text = messageExtraData.name2

                if (cmdMessageExtraData.eventCode == AppConstant.RIMConstant.CMD_MATCH_SUCCESS){

                    mFloatMessageBinding.clContainer.setBackgroundResource(R.drawable.iv_new_match_bg)

                    mFloatMessageBinding.tvTitle.text = "New Match"

                    mFloatMessageBinding.tvTitle.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.iv_new_match_tag,
                        0,
                        0,
                        0
                    )

                    mFloatMessageBinding.tvMessage.text = "Chat her right here right now"

                    reportEvent()


                    mFloatMessageBinding.root.click {

                        ChatHelper.removeLast()

                        context.createIntent(ChatActivity::class.java)
                            .putExtra(
                                AppConstant.Constant.TARGET_ID,
                                messageExtraData.groupId
                            )
                            .putExtra(
                                AppConstant.Constant.IS_NOTICE_INTO,
                                true
                            )

                            .startActivity(context)
                        remove()
                    }
                    mFloatMessageBinding.root
                }else if (cmdMessageExtraData.eventCode == AppConstant.RIMConstant.CMD_NEW_VISITOR){


                    mFloatMessageBinding.clContainer.setBackgroundResource(R.drawable.iv_new_visitor_bg)

                    mFloatMessageBinding.tvTitle.text = "New Visitor"

                    mFloatMessageBinding.tvTitle.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.iv_new_visitor_tag,
                        0,
                        0,
                        0
                    )
                    mFloatMessageBinding.tvMessage.text = "She viewed your profile!"


                    mFloatMessageBinding.root.click {

                        context.createIntent(VisitorActivity::class.java)
                            .startActivity(context)
                        remove()
                    }
                    mFloatMessageBinding.root
                }

                else {
                    null
                }
            }

            else -> {

                null

            }
        }

        if (null == resultView) {

            return
        }


        remove()

        floatingView = WeakReference(resultView)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
            } else {
                WindowManager.LayoutParams.TYPE_APPLICATION
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.CENTER

        floatingView?.get()?.let { v ->

            v.edgeToEdgeTop()

            windowManager?.addView(v, params)


            addAnimator = v.mTranslation("Y", -100f, 0f)
            addAnimator?.let {
                it.duration = 300
                it.interpolator = LinearInterpolator()
                it.start()
            }

            v.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startY = event.rawY
                        hasSlid = false
                        false
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaY = startY - event.rawY
                        if (deltaY > 20) {
                            if (!hasSlid) {
                                hasSlid = true
                                closeWithAnimation()
                            }
                            return@setOnTouchListener true
                        }
                        false
                    }

                    MotionEvent.ACTION_UP -> {
                        return@setOnTouchListener hasSlid
                    }

                    else -> false
                }
            }

        }

        removeCallbacks()

        reportEvent(mMessageNoticeKey[0],true)

        handler.postDelayed(autoCloseRunnable, 4000)

    }



    @SuppressLint("ClickableViewAccessibility")
    fun showFloatView(context: Context) {

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val mFloatSwipGuideBinding =
            FloatSwipGuideBinding.inflate(LayoutInflater.from(context))

        mFloatSwipGuideBinding.root.click {

            remove()
        }

        mFloatSwipGuideBinding.ivClose.click {

            remove()
        }

        val resultView: View? = mFloatSwipGuideBinding.root

        if (null == resultView) {

            return
        }




        remove()

        floatingView = WeakReference(resultView)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
            } else {
                WindowManager.LayoutParams.TYPE_APPLICATION
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.CENTER

        floatingView?.get()?.let { v ->

            v.edgeToEdgeTop()

            windowManager?.addView(v, params)


            addAnimator = v.mTranslation("Y", -100f, 0f)
            addAnimator?.let {
                it.duration = 300
                it.interpolator = LinearInterpolator()
                it.start()
            }

            v.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startY = event.rawY
                        hasSlid = false
                        false
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaY = startY - event.rawY
                        if (deltaY > 20) {
                            if (!hasSlid) {
                                hasSlid = true
                                closeWithAnimation()
                            }
                            return@setOnTouchListener true
                        }
                        false
                    }

                    MotionEvent.ACTION_UP -> {
                        return@setOnTouchListener hasSlid
                    }

                    else -> false
                }
            }

        }

        removeCallbacks()

        reportEvent(mMessageNoticeKey[0],true)

        handler.postDelayed(autoCloseRunnable, 4000)

    }

    private fun removeCallbacks(){
        handler.removeCallbacks(autoCloseRunnable)
    }


    private fun closeWithAnimation() {

        if (isClosing) return
        isClosing = true

        floatingView?.get()?.let { view ->

            val currentY = view.y
            closeAnimator =
                view.mTranslation("Y", currentY, -view.height.toFloat() - 10f).apply {
                    duration = 300
                    interpolator = LinearInterpolator()

                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            remove()
                        }
                    })
                    start()
                }

            isClosing = false
        }

    }


    fun remove() {

        floatingView?.get()?.let { view ->

            view.visibility = View.GONE

            if (view.parent != null) {
                val wm = view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                try {
                    wm.removeViewImmediate(view)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        floatingView?.clear()
        floatingView = null
        addAnimator = null
        closeAnimator = null
    }


    private fun reportEvent(){

        val params = mutableMapOf<String, Any?>()

        params["m_type"] = "bubble"

        com.chat.lib_common.tracking.reportEvent(mPopPopupDialogKey[4], params)
    }

}
