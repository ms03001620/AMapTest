package com.polestar.base.player.core

import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView

/**
 * Created by lwh 2021/9/2 10:25
 */
interface IDisplayAdapter {

    /**
     * 以TextureView设置容器
     */
    fun setupTextureViewDisplay(textureView: TextureView)

    /**
     * 以SurfaceView设置容器
     */
    fun setupSurfaceViewDisplay(surfaceView: SurfaceView)

    /**
     * 以Surface设置容器
     */
    fun setupVideoSurface(surface: Surface)
}
