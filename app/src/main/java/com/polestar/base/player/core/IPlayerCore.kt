package com.polestar.base.player.core

import android.content.Context
import android.net.Uri

/**
 * Created by lwh 2021/9/1 10:48
 * 抽象播放器相关行为接口, 半屏蔽播放器实现
 * 其他自定义行为需要通过[getPlayer]自行设置
 */
interface IPlayerCore<PlayerInstance> : IDisplayAdapter, IPlayerState {

    /**
     * 输入播放源, 并进入prepare状态
     */
    fun prepare(context: Context, uri: Uri)

    /**
     * 考虑到每个播放器支持的播放列表逻辑都不一样
     * 这里直接提供接口给外部设置待播放媒体列表
     */
    fun prepare(adapter: MediaSourceAdapter<PlayerInstance>)

    /**
     * 开始/继续播放
     * @param reset 是否重新播放
     */
    fun startPlayer(reset: Boolean)

    /**
     * 停止播放
     */
    fun stopPlayer()

    /**
     * 暂停播放
     */
    fun pausePlayer()

    /**
     * 释放播放器资源
     */
    fun release()

    /**
     * 设置播放进度(毫秒)
     */
    fun seekTo(millis: Long)

    /**
     * 设置播放速度
     */
    fun setPlaybackSpeed(speed: Float)

    /**
     * 设置播放音量
     */
    fun setVolume(volume: Float)

    /**
     * 设置循环播放
     */
    fun setLooping(loop: Boolean)

    /**
     * 获取播放器实例
     */
    fun getPlayer(): PlayerInstance

    /**
     * 设置播放事件监听
     */
    fun addPlayerListener(listener: PlayerListener)

    /**
     * 移除播放监听
     */
    fun removePlayerListener(listener: PlayerListener)

    /**
     * 获取当前监听者的副本
     */
    fun getPlayerListeners(): List<PlayerListener>

    /**
     * 创建一个新的播放器内核实例
     */
    fun createNewPlayerCore(): IPlayerCore<PlayerInstance>
}
