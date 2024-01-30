package com.polestar.base.player.core

/**
 * Created by lwh 2021/9/2 10:59
 */
interface IPlayerState {

    fun isPlaying(): Boolean

    fun getVolume(): Float

    fun getPlaybackSpeed(): Float

    fun isCallPrepare(): Boolean

    fun isRelease(): Boolean

    fun getDuration(): Long

    fun getPlayPosition(): Long
}
