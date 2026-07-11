package com.chat.lib_common.manager

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File


@UnstableApi
object CacheManager {

    private var simpleCache: SimpleCache? = null

    fun buildCacheDataSourceFactory(context: Context): DataSource.Factory {
        val appContext = context.applicationContext
        if (simpleCache == null) {
            val cacheDir = File(appContext.cacheDir, "exo_media_cache")
            val evictor = LeastRecentlyUsedCacheEvictor(1024 * 1024 * 1024)
            simpleCache = SimpleCache(cacheDir, evictor)
        }

        val upstreamFactory = DefaultHttpDataSource.Factory()

        return CacheDataSource.Factory()
            .setCache(simpleCache!!)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }


    fun getCache(context: Context): SimpleCache {
        if (simpleCache == null) {
            val cacheDir = File(context.cacheDir, "exo_media_cache")
            val evictor = LeastRecentlyUsedCacheEvictor(512 * 1024 * 1024)
            simpleCache = SimpleCache(cacheDir, evictor)
        }
        return simpleCache!!
    }


    fun clearCache() {
        simpleCache?.release()
        simpleCache = null
    }
}
