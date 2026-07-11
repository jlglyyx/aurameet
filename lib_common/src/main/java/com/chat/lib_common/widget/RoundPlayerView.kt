package com.chat.lib_common.widget

import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import androidx.media3.ui.PlayerView
import com.chat.lib_common.util.dip2px

class RoundPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : PlayerView(context, attrs) {

    private val radius = 10f.dip2px(context).toFloat()

    init {

        this.clipToOutline = true
        this.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius)
            }
        }

    }
}