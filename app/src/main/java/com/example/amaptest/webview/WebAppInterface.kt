package com.example.amaptest.webview

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast

class WebAppInterface(private val webView: WebView) {

    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(webView.context, message, Toast.LENGTH_SHORT).show()
    }
}