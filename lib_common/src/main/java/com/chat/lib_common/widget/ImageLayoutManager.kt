package com.chat.lib_common.widget

import android.view.View.MeasureSpec
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ImageLayoutManager : RecyclerView.LayoutManager() {

    private val maxVisibleCount: Int = 9

    private val aspectRatio : Float = 1.3f

    private val space = 1

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {

        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }

    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int
    ) {
        val parentWidth = MeasureSpec.getSize(widthSpec)
        val count = minOf(itemCount, maxVisibleCount)

        if (count == 0) {
            setMeasuredDimension(parentWidth, 0)
            return
        }

        var totalHeight = 0

        when (count) {
            1 -> {
                val itemWidth = parentWidth
                val itemHeight = (itemWidth * aspectRatio).toInt()
                totalHeight = itemHeight
            }

            2, 4 -> {
                val itemWidth = parentWidth / 2
                val itemHeight = (itemWidth * aspectRatio).toInt()
                val rowCount = (count + 1) / 2
                totalHeight = rowCount * itemHeight
            }

            3 -> {
                // 第一个占 2/3 宽，后两个各 1/3 宽
                val largeItemWidth = parentWidth * 2 / 3
                val largeItemHeight = (largeItemWidth * aspectRatio).toInt()
                val smallItemWidth = parentWidth - largeItemWidth
                val smallItemHeight = (smallItemWidth * aspectRatio).toInt()

                // 右边两张竖排
                val rightHeight = smallItemHeight * 2
                totalHeight = maxOf(largeItemHeight, rightHeight)
            }

            5 -> {
                // 前两张一行两列，后三张一行三列
                val itemWidth2 = parentWidth / 2
                val itemHeight2 = (itemWidth2 * aspectRatio).toInt()
                val itemWidth3 = parentWidth / 3
                val itemHeight3 = (itemWidth3 * aspectRatio).toInt()
                totalHeight = itemHeight2 + itemHeight3
            }

            6 -> {
                val itemWidth = parentWidth / 3
                val itemHeight = (itemWidth * aspectRatio).toInt()
                totalHeight = 2 * itemHeight
            }

            7 -> {
                // 前四张两列，后三张三列
                val itemWidth2 = parentWidth / 2
                val itemHeight2 = (itemWidth2 * aspectRatio).toInt()
                val itemWidth3 = parentWidth / 3
                val itemHeight3 = (itemWidth3 * aspectRatio).toInt()
                totalHeight = 2 * itemHeight2 + itemHeight3
            }

            8 -> {
                // 前三张三列，中间两张两列，最后三张三列
                val itemWidth3 = parentWidth / 3
                val itemHeight3 = (itemWidth3 * aspectRatio).toInt()
                val itemWidth2 = parentWidth / 2
                val itemHeight2 = (itemWidth2 * aspectRatio).toInt()
                totalHeight = itemHeight3 + itemHeight2 + itemHeight3
            }

            9 -> {
                val itemWidth = parentWidth / 3
                val itemHeight = (itemWidth * aspectRatio).toInt()
                totalHeight = 3 * itemHeight
            }
        }

        totalHeight += paddingTop + paddingBottom
        setMeasuredDimension(parentWidth, totalHeight)
    }


    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State?) {

        if (itemCount == 0 || state == null || state.isPreLayout) {
            detachAndScrapAttachedViews(recycler)
            return
        }


        detachAndScrapAttachedViews(recycler)

        val count = minOf(itemCount, maxVisibleCount)

        val parentWidth = width - paddingLeft - paddingRight

        when (count) {

            1, 2, 4, 6, 9 -> {

                layoutGrid(parentWidth,count, recycler)
            }

            3 -> {

                layoutGrid3(parentWidth,count, recycler)

            }

            5 -> {

                layoutGrid578(parentWidth,count, recycler)
            }

            7 -> {

                layoutGrid578(parentWidth,count, recycler)
            }

            8 -> {

                layoutGrid578(parentWidth,count, recycler)
            }
        }


    }


    private fun layoutGrid578(parentWidth:Int,count: Int, recycler: RecyclerView.Recycler) {


        val space = 1
        var top = paddingTop

        var rowCount = 0
        var colCount = 0

        fun getSpanCountForRow(row: Int): Int {
            return when (count) {
                5 -> if (row == 0) 2 else 3
                7 -> if (row < 2) 2 else 3
                8 -> if (row == 1) 2 else 3
                else -> 2
            }
        }

        var i = 0
        while (i < count) {
            val spanCount = getSpanCountForRow(rowCount)
            val itemWidth = parentWidth / spanCount
            val itemHeight = (itemWidth * aspectRatio).toInt()

            for (col in 0 until spanCount) {
                if (i >= count) break
                val view = recycler.getViewForPosition(i)
                addView(view)

                val lp = view.layoutParams as RecyclerView.LayoutParams
                val widthSpec = MeasureSpec.makeMeasureSpec(
                    itemWidth - lp.leftMargin - lp.rightMargin,
                    MeasureSpec.EXACTLY
                )
                val heightSpec = MeasureSpec.makeMeasureSpec(
                    itemHeight - lp.topMargin - lp.bottomMargin,
                    MeasureSpec.EXACTLY
                )
                view.measure(widthSpec, heightSpec)

                val width = getDecoratedMeasuredWidth(view)
                val height = getDecoratedMeasuredHeight(view)

                val left = paddingLeft + col * itemWidth + lp.leftMargin
                val topPos = top + lp.topMargin
                val right = left + width
                val bottom = topPos + height

                layoutDecoratedWithMargins(view, left, topPos, right - space, bottom - space)

                i++
            }

            // 换行
            top += (parentWidth / spanCount * aspectRatio).toInt()
            rowCount++
        }
    }



    private fun layoutGrid3(parentWidth:Int,count: Int, recycler: RecyclerView.Recycler) {



        val largeItemWidth = parentWidth * 2 / 3

        val largeItemHeight = (largeItemWidth * aspectRatio).toInt()

        val smartItemWidth = parentWidth - largeItemWidth

        val smartItemHeight = (smartItemWidth * aspectRatio).toInt()


        var left = 0

        var top = 0

        for (i in 0 until count) {

            val itemWidth = if (i == 0) largeItemWidth else smartItemWidth

            val itemHeight = if (i == 0) largeItemHeight else smartItemHeight

            val view = recycler.getViewForPosition(i)

            addView(view)

            val lp = view.layoutParams as RecyclerView.LayoutParams

            val widthSpec = MeasureSpec.makeMeasureSpec(
                itemWidth - lp.leftMargin - lp.rightMargin,
                MeasureSpec.EXACTLY
            )
            val heightSpec = MeasureSpec.makeMeasureSpec(
                itemHeight - lp.topMargin - lp.bottomMargin,
                MeasureSpec.EXACTLY
            )
            view.measure(widthSpec, heightSpec)

            val width = getDecoratedMeasuredWidth(view)

            val height = getDecoratedMeasuredHeight(view)



            when (i) {
                0 -> {

                    layoutDecoratedWithMargins(view, 0, 0, width-space, height)

                    left = width + paddingLeft + lp.leftMargin

                }

                1 -> {

                    layoutDecoratedWithMargins(view, left, 0, left + width, height-space)

                    top = height + paddingTop + lp.topMargin

                }

                else -> {

                    layoutDecoratedWithMargins(view, left, top, left + width, top + height)
                }
            }

        }

    }


    private fun layoutGrid(parentWidth:Int,count: Int, recycler: RecyclerView.Recycler) {


        val spaceCount = when (count) {

            2, 4 -> {
                2
            }

            6, 9 -> {

                3
            }

            else -> 1

        }


        var left: Int

        var top: Int

        for (i in 0 until count) {

            val itemWidth = parentWidth / spaceCount

            val itemHeight = (itemWidth * aspectRatio).toInt()

            val view = recycler.getViewForPosition(i)

            addView(view)

            val lp = view.layoutParams as RecyclerView.LayoutParams

            val widthSpec = MeasureSpec.makeMeasureSpec(
                itemWidth - lp.leftMargin - lp.rightMargin,
                MeasureSpec.EXACTLY
            )
            val heightSpec = MeasureSpec.makeMeasureSpec(
                itemHeight - lp.topMargin - lp.bottomMargin,
                MeasureSpec.EXACTLY
            )
            view.measure(widthSpec, heightSpec)

            val width = getDecoratedMeasuredWidth(view)

            val height = getDecoratedMeasuredHeight(view)


            val col = i % spaceCount

            val row = i / spaceCount

            left = paddingLeft + col * itemWidth + lp.leftMargin
            top = paddingTop + row * itemHeight + lp.topMargin
            val right = left + width
            val bottom = top + height

            if ( (i+1) % spaceCount == 0){
                layoutDecoratedWithMargins(view, left, top, right, bottom-space)
            }else{
                layoutDecoratedWithMargins(view, left, top, right- space, bottom-space)
            }

        }


    }


}

