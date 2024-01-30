package com.example.amaptest

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.example.amaptest.databinding.StoreWidgetProductGalleryVideoBinding
import com.polestar.base.player.view.SupportVideoView

class VideoActivity : AppCompatActivity() {
    private lateinit var binding: StoreWidgetProductGalleryVideoBinding

    //https://gist.github.com/jsturgis/3b19447b304616f18657

    //http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4

    val testUrl1 = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.store_widget_product_gallery_video)
        val t = binding.vsVideoView.viewStub?.inflate()
        val vidoeView = t?.findViewById<SupportVideoView>(R.id.video_view)

        binding.buttonPlay.setOnClickListener {
            vidoeView?.setVideoSource(Uri.parse(testUrl1))
            vidoeView?.startPlayer()
        }



    }

}