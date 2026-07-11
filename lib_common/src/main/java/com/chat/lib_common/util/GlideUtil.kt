package com.chat.lib_common.util

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.chat.lib_common.R
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.constant.AppConstant
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlin.collections.any
import kotlin.text.contains
import kotlin.text.endsWith
import kotlin.text.isNullOrEmpty


val avatarWidth = 40f.dip2px(BaseApplication.mApplication)

val videoExtensions = listOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "mpeg", "3gp")

fun String?.isVideo(): Boolean {

    if (this.isNullOrEmpty()){
        return false
    }

    return videoExtensions.any { this.endsWith(".$it", ignoreCase = true) }

}



fun String.isImage(): Boolean {

    return this.endsWith(".jpg", true) || this.endsWith(".png", true)
            || this.endsWith(".gif", true)
            || this.endsWith(".tiff", true)
            || this.endsWith(".bmp", true)
            || this.endsWith(".webp", true)
            || this.endsWith(".ico", true)

}


fun preload(context: Context,url: Any?){

    Glide.with(context).load(getRealUrl(url)).preload()
}





fun ImageView.loadImage(context: Context, url: Any?) {

    Glide.with(context)
        .load(getRealUrl(url))
        .format(DecodeFormat.PREFER_RGB_565)
        .placeholder(R.drawable.iv_placeholder)
        .error(R.drawable.iv_placeholder)
        .into(this)
}
fun ImageView.loadImageNoPlaceholder(context: Context, url: Any?) {

    Glide.with(context)
        .load(getRealUrl(url))
        .format(DecodeFormat.PREFER_RGB_565)
        .into(this)
}

fun ImageView.loadAvatar(context: Context, url: Any?) {

    Glide.with(context)
        .load(getRealUrl(url))
        .override(avatarWidth, avatarWidth)
        .format(DecodeFormat.PREFER_RGB_565)
        .placeholder(R.drawable.iv_placeholder)
        .error(R.drawable.iv_placeholder)
        .into(this)
}


fun ImageView.loadImage(context: Context, url: Any?, width: Int, height: Int) {

    val realUrl = getRealUrl(url)
    Glide.with(context)
        .load(realUrl)
        .format(DecodeFormat.PREFER_RGB_565)
        .override(width, height)
        .placeholder(R.drawable.iv_placeholder)
        .error(R.drawable.iv_placeholder)
        .into(this)
}


fun ImageView.loadImage(context: Context, url: Any?,placeholder:Int = R.drawable.iv_placeholder, width: Int, height: Int) {

    Glide.with(context)
        .load(getRealUrl(url))
        .format(DecodeFormat.PREFER_RGB_565)
        .override(width, height)
        .placeholder(placeholder)
        .error(placeholder)
        .into(this)
}



fun ImageView.loadOptionImage(context: Context, url: Any?, requestOptions:RequestOptions,width: Int, height: Int) {

    try {


        Glide.with(context)
            .load(getRealUrl(url))
            .format(DecodeFormat.PREFER_RGB_565)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .skipMemoryCache(false)
            .override(width, height)
            .apply(requestOptions)
            .placeholder(R.drawable.iv_placeholder)
            .error(R.drawable.iv_placeholder)
            .into(this)
    }catch (e: Exception){

        e.printStackTrace()
    }

}

fun ImageView.loadOptionVideo(context: Context, url: Any?, requestOptions:RequestOptions,width: Int, height: Int) {

    Glide.with(context)
        .asBitmap()
        .load(getRealUrl(url))
        .format(DecodeFormat.PREFER_RGB_565)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .skipMemoryCache(false)
        .override(width, height)
        .apply(requestOptions)
        .placeholder(R.drawable.iv_placeholder)
        .error(R.drawable.iv_placeholder)
        .into(this)
}


fun getRealUrl(url: Any?): Any {

    if(null == url) return R.drawable.iv_placeholder

    return when (url) {
        is Uri -> url
        is String -> {
            if (url.toString().contains("http")) {
                url
            } else {
                AppConstant.ClientInfo.BASE_IMAGE_URL + "$url"
            }
        }

        else -> url
    }


}
