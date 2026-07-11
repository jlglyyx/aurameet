package com.chat.lib_common.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColorInt

class RingConstraintLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private var mWidth = 0

    private var mHeight = 0

    private val mPaint = Paint()

    private val strokeWidth = 10f

    private var normalColor = "#564F4D".toColorInt()

    private var progressColor = "#EAA82B".toColorInt()

    var currentProgress = 0f
        set(value) {
            field = value
            invalidate()
        }

    init {


        mPaint.color = normalColor

        mPaint.strokeWidth = strokeWidth

        mPaint.isAntiAlias = true

        mPaint.style = Paint.Style.STROKE
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mWidth = w

        mHeight = h
    }


    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        canvas.drawCircle(mWidth/2f,mHeight/2f,mWidth/2f-strokeWidth,mPaint)

        val saveLayer = canvas.saveLayer(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), mPaint)

        mPaint.color = progressColor

        canvas.drawArc(0f+strokeWidth, 0f+strokeWidth, mWidth.toFloat()-strokeWidth, mHeight.toFloat()-strokeWidth,90f,currentProgress,false,mPaint)

        canvas.restoreToCount(saveLayer)

        mPaint.color = normalColor

    }



}