package com.example.amaptest.webview

import android.os.Bundle
import android.webkit.WebChromeClient
import androidx.appcompat.app.AppCompatActivity
import com.example.amaptest.databinding.ActivityWebviewBinding

class WebviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupWebView()
        initView()
        binding.webView.loadUrl("file:///android_asset/index.html")
    }

    private fun initView(){
        binding.btnCall.setOnClickListener {
            // callback js method
            val script = "javascript:setMessageFromNative('这是来自 Native 的消息！')"
            binding.webView.post {
                binding.webView.evaluateJavascript(script, null)
            }
        }
    }

    private fun setupWebView() {
        binding.webView.settings.javaScriptEnabled = true // 必须开启 JS
        binding.webView.webChromeClient = WebChromeClient() // 允许多种 JS 对话框
        binding.webView.addJavascriptInterface(WebAppInterface(binding.webView), "Android")
    }





}