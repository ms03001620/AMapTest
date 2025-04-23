package com.example.amaptest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem;
import com.example.amaptest.databinding.StoreWidgetProductGalleryVideoBinding


class VideoActivity : AppCompatActivity() {
    private lateinit var binding: StoreWidgetProductGalleryVideoBinding
    //https://gist.github.com/jsturgis/3b19447b304616f18657
    //http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4
    private val VIDEO_URL = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.store_widget_product_gallery_video)
    }

    private fun initializePlayer() {
        // 1. 创建 ExoPlayer 实例
        //    默认情况下，ExoPlayer.Builder 会创建能利用硬件解码的播放器
        player = ExoPlayer.Builder(this).build()

        // 2. 将 PlayerView 绑定到 Player
        binding.playerView.setPlayer(player)

        // 3. 创建要播放的媒体项 (MediaItem)
        val mediaItem: MediaItem = MediaItem.fromUri(VIDEO_URL)

        // 4. 设置媒体项给播放器
        player?.setMediaItem(mediaItem)

        // 5. 准备播放器 (异步操作)
        player?.prepare()

        // 6. 开始播放 (当准备好时自动播放)
        player?.playWhenReady = true

        // 或者 player.play(); // 如果想立即尝试播放
    }

    override fun onStart() {
        super.onStart()
        if (player == null) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (player == null) {
            initializePlayer()
        } else {
            // 如果是从后台返回，可以继续播放
            // player.play();
        }
    }

    override fun onPause() {
        super.onPause()
        if (player != null) {
            // 暂停播放
            player?.pause()
        }
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 确保资源释放
        releasePlayer()
    }


    private fun releasePlayer() {
        if (player != null) {
            player?.release() // 释放播放器资源
            player = null
        }
    }

}