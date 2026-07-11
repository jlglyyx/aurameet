package com.chat.lib_common.widget

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.ranges.downTo


class CardLayoutManager(
    private val maxVisibleCount: Int = 4
) : RecyclerView.LayoutManager() {

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {


        if (itemCount == 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }

        detachAndScrapAttachedViews(recycler)

        val count = kotlin.comparisons.minOf(itemCount, maxVisibleCount)

        for (i in count - 1 downTo 0) {

            val view = recycler.getViewForPosition(i)

            addView(view)

            measureChildWithMargins(view, 0, 0)

            val width = getDecoratedMeasuredWidth(view)

            val height = getDecoratedMeasuredHeight(view)

            val left = (getWidth() - width) / 2

            val top = (getHeight() - height) / 2

            layoutDecoratedWithMargins(view, left, top, left + width, top + height)

        }
    }

}