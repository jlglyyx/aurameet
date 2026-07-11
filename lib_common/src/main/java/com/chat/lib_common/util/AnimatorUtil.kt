package com.chat.lib_common.util

import android.animation.ObjectAnimator
import android.util.Property
import android.view.View

fun View.mRotation(vararg array: Float): ObjectAnimator {
    return ObjectAnimator.ofFloat(this, "rotation", *array)
}

fun View.mTranslation(x: String = "X", vararg array: Float): ObjectAnimator {
    return ObjectAnimator.ofFloat(this, "translation$x", *array)
}

fun View.mAlpha(vararg array: Float): ObjectAnimator {
    return ObjectAnimator.ofFloat(this, "alpha", *array)
}

fun View.mScale(x: String = "X", vararg array: Float): ObjectAnimator {
    return ObjectAnimator.ofFloat(this, "scale$x", *array)
}


//fun View.mRotation(type:Property<View, Float> = View.ROTATION,vararg array: Float): ObjectAnimator {
//    return ObjectAnimator.ofFloat(this, type, *array)
//}
//
//fun View.mTranslation(type:Property<View, Float> = View.TRANSLATION_X, vararg array: Float): ObjectAnimator {
//    return ObjectAnimator.ofFloat(this, type, *array)
//}
//
//fun View.mAlpha(vararg array: Float): ObjectAnimator {
//    return ObjectAnimator.ofFloat(this, View.ALPHA, *array)
//}
//
//fun View.mScale(type:Property<View, Float> = View.SCALE_X, vararg array: Float): ObjectAnimator {
//    return ObjectAnimator.ofFloat(this, type, *array)
//}