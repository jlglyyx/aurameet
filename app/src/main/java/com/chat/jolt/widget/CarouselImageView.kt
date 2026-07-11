package com.chat.jolt.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.request.RequestOptions
import com.chat.jolt.R
import com.chat.jolt.helper.UserInfoHold
import com.chat.lib_common.util.avatarWidth
import com.chat.lib_common.util.dip2px
import com.chat.lib_common.util.loadAvatar
import com.chat.lib_common.util.loadOptionImage
import com.chat.lib_common.widget.BlurTransformation
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CarouselImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), DefaultLifecycleObserver {

    private val mScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var carouselJob: Job? = null

    private var currentIndex = 0

    private var imageList = mutableListOf<String>()

    private var isCarouselRunning = false

    private var isVisible = false

    private var mStrokeWidth = 1.5f.dip2px(context)

    private var mDuration = 3000L

    private var mTranDuration = 1000L

    private val requestOptions = RequestOptions.bitmapTransform(BlurTransformation(10,2))

    private val imageView1 = ShapeableImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        strokeWidth = mStrokeWidth.toFloat()
        setPadding(mStrokeWidth)
        strokeColor = ContextCompat.getColorStateList(context, R.color.color_C678FF)
        shapeAppearanceModel = ShapeAppearanceModel.builder(
            context, R.style.circleStyle, R.style.circleStyle
        ).build()
    }

    private val imageView2 = ShapeableImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        strokeWidth = mStrokeWidth.toFloat()
        setPadding(mStrokeWidth)
        strokeColor = ContextCompat.getColorStateList(context, R.color.color_C678FF)
        shapeAppearanceModel = ShapeAppearanceModel.builder(
            context, R.style.circleStyle, R.style.circleStyle
        ).build()
        visibility = INVISIBLE
    }

    init {
        addView(imageView1)
        addView(imageView2)
    }

    fun setStrokeColor(@ColorRes id:Int ){

        imageView1.strokeColor = ContextCompat.getColorStateList(context, id)

        imageView2.strokeColor = ContextCompat.getColorStateList(context, id)
    }


    fun setImageList(list: MutableList<String>) {

        cancelCarouselJob()

        currentIndex = 0
        imageList.clear()
        imageList.addAll(list)
        isCarouselRunning = false

        resetImageViews()

        if (imageList.isNotEmpty()) {

            loadAvatar(imageList[0],imageView1)

            imageView1.visibility = VISIBLE
            if (imageList.size > 1) {
                startCarousel()
            }
        }
    }

    private fun loadAvatar(url: String,imageView: ImageView){

        try {
            if (context is Activity && (context as Activity).isDestroyed){

                release()

                return
            }

            if (UserInfoHold.isVip){

                imageView.loadAvatar(context,url)
            }else{
                imageView.loadOptionImage(context, url,requestOptions,avatarWidth,avatarWidth)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }


    }


    private fun resetImageViews() {
        imageView1.translationY = 0f
        imageView1.visibility = INVISIBLE
        imageView1.setImageDrawable(null)

        imageView2.translationY = 0f
        imageView2.visibility = INVISIBLE
        imageView2.setImageDrawable(null)

        imageView1.animate().cancel()
        imageView2.animate().cancel()
    }

    fun startCarousel() {
        if (imageList.size <= 1 || isCarouselRunning) return

        isCarouselRunning = true
        carouselJob = mScope.launch {
            while (isActive && isCarouselRunning) {

                delay(mDuration)

                if (isVisible){
                    carouselToNext()
                }
            }
        }
    }


    fun stopCarousel() {
        isCarouselRunning = false
        cancelCarouselJob()
    }

    private fun cancelCarouselJob() {
        carouselJob?.apply {
            cancel()
        }
        carouselJob = null
    }


    private fun carouselToNext() {


        val nextIndex = (currentIndex + 1) % imageList.size

        val (currentView, nextView) = if (imageView1.isVisible) {
            imageView1 to imageView2
        } else {
            imageView2 to imageView1
        }


        loadAvatar(imageList[nextIndex],nextView)
        nextView.translationY = height.toFloat()
        nextView.visibility = VISIBLE

        currentView.animate()
            .translationY(-height.toFloat())
            .setDuration(mTranDuration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentView.visibility = INVISIBLE
                    currentView.translationY = 0f
                }
            })
            .start()

        nextView.animate()
            .translationY(0f)
            .setDuration(mTranDuration)
            .setListener(null)
            .start()

        currentIndex = nextIndex
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
//        release()

        Log.i("CarouselImageView", "onDetachedFromWindow: ==============?")
    }

    fun release(){
        stopCarousel()
        mScope.coroutineContext.cancel()

    }


    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        isVisible = true
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        isVisible = false
    }




}