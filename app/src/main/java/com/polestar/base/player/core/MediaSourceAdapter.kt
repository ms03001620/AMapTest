package com.polestar.base.player.core

/**
 * Created by lwh 2021/9/1 11:42
 */
interface MediaSourceAdapter<PlayerInstance> {

    fun setInputMedia(player: PlayerInstance)
}
