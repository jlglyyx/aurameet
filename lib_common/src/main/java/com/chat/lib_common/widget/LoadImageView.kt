package com.chat.lib_common.widget
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.chat.lib_common.util.mRotation


class LoadImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs), DefaultLifecycleObserver {


    private var mRotation: ObjectAnimator? = null

    init {
        this.post {

            createAnimate()

        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }


    private fun createAnimate() {

        mRotation = mRotation(0f, 360f)

        mRotation?.repeatCount = -1

        mRotation?.setDuration(1000)

        start()
    }


    fun start() {

        this.post {

            mRotation?.start()
        }

    }

    fun stop() {

        mRotation?.cancel()
        mRotation = null
    }

    fun pause() {

        mRotation?.pause()
    }

    fun resume() {

        mRotation?.resume()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        start()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        pause()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        resume()
    }


    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        stop()
    }

}