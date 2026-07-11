package com.chat.lib_common.widget

import android.R.attr.orientation
import android.R.attr.radius
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.LinearLayout.VERTICAL
import android.widget.OverScroller
import androidx.core.view.marginRight
import com.chat.lib_common.util.dip2px
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.ranges.until


class SwipeMenuLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    private var leftMenu: View? = null
    private var content: View? = null
    private var rightMenu: View? = null

    private var downX = 0f
    private var lastX = 0f
    private var downY = 0f
    private val scroller = OverScroller(context)

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val minFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    private val maxFlingVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity

    private var isDragging = false
    private var totalHeight = 0
    private var velocityTracker: VelocityTracker? = null

    private var isOpen = -1

    var allowLeftMenu = true

    var allowRightMenu = true


    companion object {
        @SuppressLint("StaticFieldLeak")
        private var mViewCache: SwipeMenuLayout? = null
    }

    private val radius = 10f.dip2px(context).toFloat()


    init {


        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius)
            }
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        require(childCount in 1..3) { "BiDirectionSwipeMenuLayout must have 1-3 children" }

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            totalHeight = max(totalHeight, child.measuredHeight)
        }

        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            totalHeight
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            when (i) {
                0 -> {
                    leftMenu = child
                    val leftWidth = if (allowLeftMenu) child.measuredWidth else 0
                    child.layout(-leftWidth, 0, 0, totalHeight)
                }
                1 -> {
                    content = child
                    child.layout(0, 0, child.measuredWidth, totalHeight)
                }
                2 -> {
                    rightMenu = child
                    val rightWidth = if (allowRightMenu) child.measuredWidth else 0
                    val cl = content?.measuredWidth ?: 0
                    child.layout(cl, 0, cl + rightWidth, totalHeight)
                }
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {


        if (!allowLeftMenu && !allowRightMenu) {
            return false
        }

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                lastX = ev.x
                downY = ev.y
                isDragging = false

                velocityTracker = VelocityTracker.obtain()
                velocityTracker?.addMovement(ev)

                // 如果有菜单正在打开，且不是当前这个
                if (mViewCache != null) {

                    if (mViewCache != this){
                        mViewCache?.closeMenu()
                        mViewCache = null
                    }

                    parent?.requestDisallowInterceptTouchEvent(false)
                }

            }

            MotionEvent.ACTION_MOVE -> {

                val f = ev.y - downY
                if (f > touchSlop){
                    isDragging = false
                    parent?.requestDisallowInterceptTouchEvent(false)
                    return false
                }

                val dx = abs(ev.x - downX)
                if (dx > touchSlop) {
                    isDragging = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                    return true
                }
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (!allowLeftMenu && !allowRightMenu) {
            return false
        }

        velocityTracker?.addMovement(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                lastX = event.x


            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastX
                lastX = event.x
                val targetX = clampScroll(scrollX - dx.toInt())
                scrollTo(targetX, 0)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                velocityTracker?.computeCurrentVelocity(1000, maxFlingVelocity.toFloat())
                val vx = velocityTracker?.xVelocity ?: 0f

                if (abs(vx) > minFlingVelocity) {
                    handleFling(vx)
                } else {
                    settleScroll()
                }

                velocityTracker?.recycle()
                velocityTracker = null
            }
        }
        return true
    }

    private fun handleFling(vx: Float) {
        val leftWidth = leftMenu?.measuredWidth ?: 0
        val rightWidth = rightMenu?.measuredWidth ?: 0

        when {
            vx > 0 -> {
                if (isOpen == -1 &&allowLeftMenu && leftWidth > 0) {
                    openLeftMenu()
                } else {
                    closeMenu()
                }
            }
            vx < 0 -> {
                if (isOpen == -1 && allowRightMenu && rightWidth > 0) {
                    openRightMenu()
                } else {
                    closeMenu()
                }
            }
            else -> {
                settleScroll()
            }
        }
    }

    private fun clampScroll(x: Int): Int {
        val leftLimit = if (allowLeftMenu) -(leftMenu?.measuredWidth ?: 0) else 0
        val rightLimit = if (allowRightMenu) rightMenu?.measuredWidth ?: 0 else 0
        return min(max(x, leftLimit), rightLimit)
    }

    private fun settleScroll() {
        val leftWidth = leftMenu?.measuredWidth ?: 0
        val rightWidth = rightMenu?.measuredWidth ?: 0

        when {
            scrollX > 0 -> {
                if (allowRightMenu && scrollX > rightWidth / 2) {
                    openRightMenu()
                } else {
                    closeMenu()
                }
            }

            scrollX < 0 -> {
                if (allowLeftMenu && abs(scrollX) > leftWidth / 2) {
                    openLeftMenu()
                } else {
                    closeMenu()
                }
            }

            else -> closeMenu()
        }
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, scroller.currY)
            invalidate()
        }
    }

    fun closeMenu() {
        isOpen = -1
        if (this == mViewCache) {
            mViewCache = null
        }
        scroller.startScroll(scrollX, 0, -scrollX, 0, 200)
        invalidate()
    }

    fun openLeftMenu() {

        if (!allowLeftMenu) return

        val leftWidth = leftMenu?.measuredWidth ?: return
        isOpen = 0
        mViewCache = this
        scroller.startScroll(scrollX, 0, -leftWidth - scrollX, 0, 200)
        invalidate()
    }

    fun openRightMenu() {

        if (!allowRightMenu) return

        val rightWidth = rightMenu?.measuredWidth ?: return
        isOpen = 1
        mViewCache = this
        scroller.startScroll(scrollX, 0, rightWidth - scrollX, 0, 200)
        invalidate()
    }


    override fun onDetachedFromWindow() {
        if (this == mViewCache) {
            smoothClose()
            mViewCache = null
        }
        super.onDetachedFromWindow()
    }

    fun smoothClose() {
        if (scrollX != 0) {
            scroller.startScroll(scrollX, 0, -scrollX, 0, 200)
            invalidate()
        }
        isOpen = -1
    }

}
