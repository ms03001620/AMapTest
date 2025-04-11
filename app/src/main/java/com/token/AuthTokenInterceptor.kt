package com.token

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock

class AuthTokenInterceptor(
    private val tokenProvider: TokenProvider
) : Interceptor {

    private val lock = ReentrantLock()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        // 非认证请求直接放行
        if (!requiresToken(chain.request())) {
            return chain.proceed(chain.request())
        }

        // 第一次尝试使用当前token
        val firstAttemptRequest = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${tokenProvider.getAccessToken()}")
            .build()

        val firstResponse = chain.proceed(firstAttemptRequest)

        // 如果响应成功，直接返回
        if (firstResponse.isSuccessful) {
            return firstResponse
        }

        // 如果响应是401未授权，尝试刷新token
        if (firstResponse.code == 401) {
            firstResponse.close()
             return synchronizedRefreshToken(chain)
        }

        // 其他错误直接返回
        return firstResponse
    }

    @Throws(IOException::class)
    private fun synchronizedRefreshToken(chain: Interceptor.Chain): Response {
        lock.lock()
        try {
            // 再次检查token是否已被其他线程刷新
            val secondAttemptRequest = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${tokenProvider.getAccessToken()}")
                .build()

            val secondResponse = chain.proceed(secondAttemptRequest)
            if (secondResponse.isSuccessful) {
                return secondResponse
            }

            // 如果仍然401，执行token刷新
            if (secondResponse.code == 401) {
                secondResponse.close()
                val newToken = tokenProvider.refreshToken()

                // 使用新token重试请求
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $newToken")
                    .build()

                return chain.proceed(newRequest)
            }

            return secondResponse
        } finally {
            lock.unlock()
        }
    }

    private fun requiresToken(request: Request): Boolean {
        // 根据业务需求判断哪些请求需要token
        // 这里简单示例：所有非GET请求都需要token
        return request.method != "GET"

        // 实际项目中可能需要更复杂的逻辑，例如：
        // return !request.url.pathSegments.contains("login")
        // && !request.url.pathSegments.contains("refresh-token")
    }
}

// TokenProvider接口，由调用方实现
interface TokenProvider {
    fun getAccessToken(): String
    @Throws(IOException::class)
    fun refreshToken(): String
}
