package com.robolectric

import android.os.Bundle
import android.util.Base64
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.example.amaptest.AssetsReadUtils
import com.example.amaptest.R

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val t = AssetsReadUtils.mockPolicy(this, null)
        println(t)

        val web = findViewById<WebView>(R.id.webivew)


        loadHtml1(web, t?.data?.appChinaPrivacyPolicy?.content!!)
    }

    private fun loadHtml1(webview: WebView, data: String){
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadDataWithBaseURL(null,data,"text/html", "UTF-8", null)
    }

    private fun loadHtml(webview: WebView, data: String) {
        val encodedHtml = Base64.encodeToString(data.toByteArray(), Base64.NO_PADDING);
        // Base64 HTML String
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadData(encodedHtml, "text/html", "base64");

    }
}