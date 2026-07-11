package com.chat.jolt.widget

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.util.getScreenPx
import java.lang.ref.WeakReference

class FloatingView(context: Context) {

    private var params: WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
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


    private var downRawX = 0f

    private var downRawY = 0f

    private var downX = 0f

    private var downY = 0f

    private var mView: View? = null

    private val screenPx = getScreenPx(BaseApplication.mApplication)

    private var isAdded = false

    private val contextRef: WeakReference<Context> = WeakReference(context)

    private var windowManager: WindowManager? =
        context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager

    init {

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 0


        (context as? LifecycleOwner)?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                removeView()
                clearReferences()
            }
        })

    }


    @SuppressLint("ClickableViewAccessibility")
    fun addView(view: View) {

        try {

            val context = contextRef.get()

            if (context == null || (context is Activity && context.isDestroyed)) {
                return
            }


            mView = view

            mView?.setOnTouchListener { v, event ->

                when (event.action) {

                    MotionEvent.ACTION_DOWN -> {

                        downRawX = event.rawX
                        downRawY = event.rawY

                        downX = params.x.toFloat()

                        downY = params.y.toFloat()

                    }

                    MotionEvent.ACTION_MOVE -> {

                        val moveRawX = event.rawX
                        val moveRawY = event.rawY

                        val offsetX = moveRawX - downRawX
                        val offsetY = moveRawY - downRawY

                        params.x = (downX + offsetX).toInt()
                        params.y = (downY + offsetY).toInt()

                        try {
                            windowManager?.updateViewLayout(mView, params)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        autoStickEdge()
                    }

                }
                return@setOnTouchListener false
            }


            mView?.let {
                windowManager?.addView(it, params)

                isAdded = true

                it.post {
                    try {
                        params.x = screenPx[0] - it.measuredWidth
                        params.y = it.measuredHeight+100
                        windowManager?.updateViewLayout(it, params)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {

            e.printStackTrace()
        }
    }


    private fun autoStickEdge() {

        mView?.let {

            try {

                val context = contextRef.get()

                if (context == null || (context is Activity && context.isDestroyed)) {
                    return
                }

                val screenWidth = screenPx[0]

                val viewWidth = it.measuredWidth

                val midX = params.x + viewWidth / 2

                params.x = if (midX < screenWidth / 2) {
                    0
                } else {
                    screenWidth - viewWidth
                }

                windowManager?.updateViewLayout(it, params)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    fun showOrHide(isShow: Boolean) {

        try {

            val context = contextRef.get()

            if (context == null || (context is Activity && context.isDestroyed)) {
                return
            }

            mView?.let {

                if (isShow) {
                    it.visibility = View.VISIBLE
                } else {
                    it.visibility = View.GONE
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeView() {

        try {

            val context = contextRef.get()

            if (context == null || (context is Activity && context.isDestroyed)) {
                return
            }

            mView?.let {

                if (isAdded){
                    windowManager?.removeView(it)

                    isAdded = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }



    private fun clearReferences() {
        mView = null
        windowManager = null
        contextRef.clear()
    }
}