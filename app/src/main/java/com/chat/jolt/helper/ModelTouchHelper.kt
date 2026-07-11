package com.chat.jolt.helper

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class ModelTouchHelper : ItemTouchHelper.SimpleCallback(
    0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            or ItemTouchHelper.UP or ItemTouchHelper.DOWN
) {

    var isSwipeEnabled: Boolean = true

    var isNeedRemoveCard: Boolean = true

    var onReset: () -> Unit = { ->

    }
    var onSwiped: (Int) -> Unit = { position ->

    }
    var onLike: (Int) -> Unit = { position ->

    }
    var onDisLike: (Int) -> Unit = { position ->

    }
    var onMove: (Boolean) -> Unit = { state ->

    }


    var onShowImage: (Int, Float) -> Unit = { type, dX ->

    }


    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {

        if (!isSwipeEnabled || viewHolder.adapterPosition != 0) {
            return makeMovementFlags(0, 0)
        }

        return super.getMovementFlags(recyclerView, viewHolder)

    }


    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {

        return 0.4f


    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        onShowImage(0, 0f)


        val position = viewHolder.adapterPosition

        if (!isNeedRemoveCard) {

            resetSwipedView(viewHolder)

            (viewHolder.itemView.parent as? RecyclerView)?.adapter?.notifyItemChanged(position)

            onReset()

            return
        }




        onSwiped(position)

        when (direction) {
            ItemTouchHelper.LEFT, ItemTouchHelper.UP -> {
                onDisLike(position)
            }

            ItemTouchHelper.RIGHT, ItemTouchHelper.DOWN -> {
                onLike(position)
            }

        }

    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

            val ratio = dX / recyclerView.width

            viewHolder.itemView.rotation = ratio * 15

            val int = if (abs(ratio) == 1.0f) {
                0
            } else {
                if (dX < 0) {

                    -1

                } else if (dX == 0f) 0 else 1
            }


            onShowImage(int,ratio)
        }


        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)


    }


    private fun resetSwipedView(viewHolder: RecyclerView.ViewHolder) {

        viewHolder.itemView.animate()
            .translationX(0f)
            .rotation(0f)
            .start()

    }


    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)


        onMove(actionState == 1)
    }


}