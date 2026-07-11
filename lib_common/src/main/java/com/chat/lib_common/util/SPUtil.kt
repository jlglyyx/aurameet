package com.chat.lib_common.util

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit
import com.chat.lib_common.app.BaseApplication

fun getSharedPreferences(): SharedPreferences {

    return BaseApplication.mApplication.getSharedPreferences("app_cache", MODE_PRIVATE)
}

fun <T:Any> getCache(key: String, defaultValue: T): T {

    return when (defaultValue) {
        is Boolean ->
            getSharedPreferences().getBoolean(key, defaultValue) as T

        is String ->
            getSharedPreferences().getString(key, defaultValue) as T

        is Long ->
            getSharedPreferences().getLong(key, defaultValue) as T

        is Float ->
            getSharedPreferences().getFloat(key, defaultValue) as T

        is Int ->
            getSharedPreferences().getInt(key, defaultValue) as T

        else -> {
            getSharedPreferences().getString(key, defaultValue.toJson()) as T
        }
    }
}

fun setCache(key: String, value: Any) {
    when (value) {
        is Boolean ->
            getSharedPreferences().edit { putBoolean(key, value) }

        is String ->
            getSharedPreferences().edit { putString(key, value) }

        is Long ->
            getSharedPreferences().edit { putLong(key, value) }

        is Float ->
            getSharedPreferences().edit { putFloat(key, value) }

        is Int ->
            getSharedPreferences().edit { putInt(key, value) }

        else -> {
            getSharedPreferences().edit { putString(key, value.toJson()) }
        }
    }
}

fun clearCache(key: String) {

    getSharedPreferences().edit { remove(key) }
}

fun clearAllCache() {

    getSharedPreferences().edit { clear() }
}