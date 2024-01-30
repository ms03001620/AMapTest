package com.polestar.base.player.utils

import android.content.Context
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

/**
 * lwh created in 2023/4/24 15:16
 */
object ExoCacheFactory {
    private const val CONNECT_TIMEOUT_MILLIS = 8000
    private const val READ_TIMEOUT_MILLIS = 8000
    private const val CACHE_MAX_SIZE_BYTES = 1024 * 1024 * 100L
    private var cacheFactory: DataSource.Factory? = null

    @Synchronized
    fun getCacheFactory(ctx: Context): DataSource.Factory {
        if (cacheFactory == null) {
            val downDirectory = File(ctx.cacheDir, "cache_internal_videos")
            val cache = SimpleCache(
                downDirectory,
                LeastRecentlyUsedCacheEvictor(CACHE_MAX_SIZE_BYTES),
                null
            )
            cacheFactory = CacheDataSource.Factory().setCache(cache)
                .setCacheReadDataSourceFactory(
                    DefaultDataSource.Factory(
                        ctx,
                        DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(false)
                            .setConnectTimeoutMs(CONNECT_TIMEOUT_MILLIS)
                            .setReadTimeoutMs(READ_TIMEOUT_MILLIS)
                            .setUserAgent(ctx.packageName)
                    )
                )
                .setUpstreamDataSourceFactory(
                    DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(false)
                        .setConnectTimeoutMs(CONNECT_TIMEOUT_MILLIS)
                        .setReadTimeoutMs(READ_TIMEOUT_MILLIS)
                        .setUserAgent(ctx.packageName)
                )
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        }
        return cacheFactory!!
    }
}
