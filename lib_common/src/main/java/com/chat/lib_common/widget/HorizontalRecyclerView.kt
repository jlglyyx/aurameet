package com.chat.lib_common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class HorizontalRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    private var startX = 0f
    private var startY = 0f
    private var isHorizontalDrag = false

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                startX = e.x
                startY = e.y
                isHorizontalDrag = false
                parent.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = e.x - startX
                val dy = e.y - startY

                val absDx = abs(dx)
                val absDy = abs(dy)

                if (absDy > absDx && absDy > touchSlop) {
                    parent.requestDisallowInterceptTouchEvent(false)
                    return false
                }

                if (absDx > absDy && absDx > touchSlop) {
                    isHorizontalDrag = true

                    val canScroll = canScrollHorizontally((-dx).toInt())
                    parent.requestDisallowInterceptTouchEvent(canScroll)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                isHorizontalDrag = false
            }
        }

        return super.onInterceptTouchEvent(e)
    }
}
