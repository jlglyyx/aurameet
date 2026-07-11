package com.chat.jolt.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.media3.exoplayer.ExoPlayer
import com.chat.jolt.R
import com.chat.jolt.databinding.ViewVideoPlayBinding
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.util.getScreenPx
import com.chat.lib_common.util.getTimeSecond
import com.chat.lib_common.util.loadImage
import com.chat.jolt.manager.ScopedVideoManager


class CustomVideoPlay @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val TAG = "CustomVideoPlay"

    val mBinding by lazy {
        ViewVideoPlayBinding.inflate(LayoutInflater.from(context), this, true)
    }

    val mWidth:Int = ((getScreenPx(BaseApplication.Companion.mApplication)[0] )*0.5).toInt()


    var needClickPlay = true

    var onClick:() -> Unit = {

    }

    var onOtherClick:(View, View) -> Unit = {_,_,->

    }


    var doubleClick = {

    }

    var onSeek:(Long) ->Unit = {

    }

    val mOnVideoStatusListener = object : ScopedVideoManager.OnVideoStatusListener {

        override fun onReady(duration: Int) {

            mBinding.mSeekBarProgress.max = duration

            mBinding.tvCountTime.text = "${getTimeSecond(duration)}"

            mBinding.ivImageCover.visibility = GONE
        }


        override fun detachLastPlayer() {

            mBinding.mSeekBarProgress.progress = 0

            mBinding.ivPlay.visibility = GONE

            mBinding.ivProgressPlay.setImageResource(R.drawable.iv_pause)

        }

        override fun onBuffer() {

        }


        override fun onPlayEnd() {

        }

        override fun onVideoPause() {

            mBinding.ivPlay.visibility = VISIBLE
            mBinding.ivProgressPlay.setImageResource(R.drawable.iv_pause)

        }

        override fun onVideoPlay() {

            mBinding.ivPlay.visibility = GONE
            mBinding.ivProgressPlay.setImageResource(R.drawable.iv_resume)

        }

        override fun onProgressUpdate(progress: Int) {

            mBinding.ivPlay.visibility = GONE

            mBinding.ivProgressPlay.setImageResource(R.drawable.iv_resume)

            mBinding.ivImageCover.visibility = GONE

            mBinding.mSeekBarProgress.progress = progress

            mBinding.tvCurrentTime.text = getTimeSecond(progress)

        }

    }

    private var gestureDetector: GestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {


                if (needClickPlay){
                    onClick()
                }else{
                    onOtherClick(mBinding.llMenu,mBinding.ivPlay)
                }


                Log.i(TAG, "onSingleTapConfirmed: ")
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {

                Log.i(TAG, "onDoubleTap: ${e.x} ${e.y}  ${e.rawX}  ${e.rawY}")

                doubleClick()


                return true
            }

            override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                Log.i(TAG, "onDoubleTapEvent: ")
                return super.onDoubleTapEvent(e)
            }
        })


    init {

        mBinding.playView.setOnTouchListener { v, event ->


            gestureDetector.onTouchEvent(event)

            true
        }

        mBinding.ivProgressPlay.setOnClickListener {

            onClick()
        }

        initProgress()
    }


    fun attachPlayer(player: ExoPlayer) {
        mBinding.playView.player = player
    }

    fun detachPlayer() {
        mBinding.playView.player = null
    }


    private fun initProgress() {

        mBinding.mSeekBarProgress.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                Log.i(TAG, "onProgressChanged: $progress  $fromUser")

                if (fromUser) {
                    mBinding.tvCurrentTime.text = getTimeSecond((progress))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {



                Log.i(TAG, "onStartTrackingTouch: ")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {


                seekBar?.let {

                    Log.i(TAG, "onStopTrackingTouch: ${seekBar.progress}")


                    onSeek(seekBar.progress * 1000L)

                }


            }


        })

    }

    private var isHasLoadCover = false

    fun loadCover(url:String?){

        if (isHasLoadCover) return

        if (url.isNullOrEmpty()) return

        mBinding.ivImageCover.loadImage(context,url,mWidth,mWidth)

        mBinding.ivImageCover.visibility = VISIBLE

        isHasLoadCover = true

    }



}