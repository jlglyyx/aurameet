package com.chat.lib_common.util

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup

class ScreenChangeUtil {

    private var isFirst = true

    private var currentHeight = 0

    private var layoutParams: ViewGroup.LayoutParams? = null

    fun onScreenChange(
        rootView: View,
        requestLayout: Boolean,
        onChange: (screenOpen: Boolean, screenHeight: Int) -> Unit
    ) {

        layoutParams = rootView.layoutParams
        isFirst = false

        val rect = Rect()
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            if (!isFirst) {
                rootView.getWindowVisibleDisplayFrame(rect)
                if (currentHeight != rect.bottom) {
                    if (requestLayout && null != layoutParams) {
                        layoutParams!!.height = rect.bottom
                        rootView.requestLayout()
                    }
                    val height = currentHeight - rect.bottom
                    if (height > 0) {

                    }
                    onChange(currentHeight > rect.bottom, height)
                    currentHeight = rect.bottom
                }
            }
        }

    }
}