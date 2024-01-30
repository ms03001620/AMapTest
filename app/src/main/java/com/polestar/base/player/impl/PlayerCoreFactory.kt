package com.polestar.base.player.impl

import android.content.Context
import android.media.MediaPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.polestar.base.player.core.IPlayerCore
import com.polestar.base.player.core.MainThreadCoreWrapper

/**
 * Created by lwh 2021/9/1 15:30
 */
object PlayerCoreFactory {

    fun createExoPlayerCore(context: Context): IPlayerCore<SimpleExoPlayer> {
        return MainThreadCoreWrapper(ExoPlayerCore(context))
    }

    fun createMediaPlayerCore(context: Context): IPlayerCore<MediaPlayer> {
        return MainThreadCoreWrapper(MediaPlayerCore(context))
    }
}
