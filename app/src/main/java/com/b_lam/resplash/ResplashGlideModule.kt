package com.b_lam.resplash

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class ResplashGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val memoryCacheSizeBytes: Long = 1024 * 1024 * 500 // 500 MB
        builder.setMemoryCache(LruResourceCache(memoryCacheSizeBytes))
    }
}
