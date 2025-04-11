package com.token

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException

// 实现TokenProvider
class MyTokenProvider : TokenProvider {
    private var accessToken: String = ""
    private var refreshToken: String = "your_refresh_token"

    override fun getAccessToken(): String = accessToken

    override fun refreshToken(): String {
        // 实现刷新token的逻辑
        val response = OkHttpClient().newCall(
            Request.Builder()
                .url("https://api.example.com/refresh-token")
                .post(
                    RequestBody.create(
                        "application/json".toMediaTypeOrNull(),
                    "{\"refresh_token\":\"$refreshToken\"}"))
                .build()
        ).execute()

        if (!response.isSuccessful) {
            throw IOException("Failed to refresh token")
        }

        val json = response.body?.string() ?: throw IOException("Empty response")
        val newTokens = JSONObject(json)
        accessToken = newTokens.getString("access_token")
        refreshToken = newTokens.getString("refresh_token")

        return accessToken
    }
}

// 创建OkHttpClient
val client = OkHttpClient.Builder()
    .addInterceptor(AuthTokenInterceptor(MyTokenProvider()))
    .build()
