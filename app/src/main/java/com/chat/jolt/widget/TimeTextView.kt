package com.chat.jolt.widget

import android.content.Context
import android.util.AttributeSet
import com.chat.lib_common.util.getTimeSecond
import com.hjq.shape.view.ShapeTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimeTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ShapeTextView(context, attrs) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var job: Job? = null

    var onEndTime = {

    }

    fun startTimer(time: Int) {

        job?.cancel()

        var mTime = time

        if (mTime <= 0) return

        job = scope.launch {
            try {


                while (isActive) {


                    if (mTime <= 0) {

                        onEndTime()

                        cancel()
                    } else {

                        val time = getTimeSecond(mTime, true)

                        text = time

                        delay(1000)
                    }
                    mTime--

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

    fun stopTimer() {

        job?.cancel()
    }

    override fun onDetachedFromWindow() {
        stopTimer()
        super.onDetachedFromWindow()
    }

}