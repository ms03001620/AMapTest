package com.token

import junit.framework.TestCase.assertEquals

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.io.IOException
import java.util.concurrent.TimeUnit


class AuthTokenInterceptorTest {
    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient
    private lateinit var tokenProvider: TokenProvider

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        tokenProvider = mock(TokenProvider::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `不需要token的请求不添加认证头`() {
        // 准备
        `when`(tokenProvider.getAccessToken()).thenReturn("initial_token")
        client = OkHttpClient.Builder()
            .addInterceptor(AuthTokenInterceptor(tokenProvider))
            .build()

        server.enqueue(MockResponse().setResponseCode(200))

        // 执行
        val request = Request.Builder()
            .url(server.url("/public"))
            .get()
            .build()

        client.newCall(request).execute()

        // 验证
        val recordedRequest = server.takeRequest()
        assertTrue(recordedRequest.headers.names().contains("Authorization").not())
    }

    @Test
    fun `需要token的请求添加认证头`() {
        // 准备
        `when`(tokenProvider.getAccessToken()).thenReturn("initial_token")
        client = OkHttpClient.Builder()
            .addInterceptor(AuthTokenInterceptor(tokenProvider))
            .build()

        server.enqueue(MockResponse().setResponseCode(200))

        // 执行
        val request = Request.Builder()
            .url(server.url("/secure"))
            .post("data".toRequestBody("text/plain".toMediaType()))
            .build()

        client.newCall(request).execute()

        // 验证
        val recordedRequest = server.takeRequest()
        assertEquals("Bearer initial_token", recordedRequest.getHeader("Authorization"))
    }

    @Test
    fun `token过期时自动刷新并重试`() {
        // 准备
        `when`(tokenProvider.getAccessToken()).thenReturn("expired_token")
        `when`(tokenProvider.refreshToken()).thenReturn("new_token")

        client = OkHttpClient.Builder()
            .addInterceptor(AuthTokenInterceptor(tokenProvider))
            .build()

        // 第一次响应401
        server.enqueue(MockResponse().setResponseCode(401))

        server.enqueue(MockResponse().setResponseCode(401))
        // 刷新token后响应200
        server.enqueue(MockResponse().setResponseCode(200))

        // 执行
        val request = Request.Builder()
            .url(server.url("/secure"))
            .post("data".toRequestBody("text/plain".toMediaType()))
            .build()

        val response = client.newCall(request).execute()

        // 验证
        assertEquals(200, response.code)

        assertEquals(3, server.requestCount)

        // 验证第一个请求使用了过期token
        val firstRequest = server.takeRequest(1, TimeUnit.SECONDS)
        assertEquals("Bearer expired_token", firstRequest?.getHeader("Authorization"))
        // 验证第二个请求使用了过期token
        val secondRequest = server.takeRequest(1, TimeUnit.SECONDS)
        assertEquals("Bearer expired_token", secondRequest?.getHeader("Authorization"))
        // 验证第三个请求使用了新token
        val thirdRequest = server.takeRequest(1, TimeUnit.SECONDS)
        assertEquals("Bearer new_token", thirdRequest?.getHeader("Authorization"))
        // 验证token刷新只调用了一次
        verify(tokenProvider, times(1)).refreshToken()
    }

    @Test
    fun `多个并发请求时只刷新一次token`() {
        server.dispatcher = object : okhttp3.mockwebserver.Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val token = request.getHeader("Authorization")

                return when(token) {
                    "Bearer expired_token" ->
                        MockResponse().setResponseCode(401)

                    "Bearer new_token" ->
                        MockResponse().setResponseCode(200)

                    else -> MockResponse().setResponseCode(404)
                }
            }
        }

        var token = "expired_token"
        var countRefreshToken = 0

        client = OkHttpClient.Builder()
            .addInterceptor(AuthTokenInterceptor(object: TokenProvider{
                override fun getAccessToken(): String {
                    return token
                }

                override fun refreshToken(): String {
                    Thread.sleep(500)
                    countRefreshToken++
                    token = "new_token"
                    return token
                }
            }))
            .build()


        // 执行并发请求
        val requests = (1..5).map {
            Thread {
                val request = Request.Builder()
                    .url(server.url("/secure"))
                    .post(RequestBody.create("text/plain".toMediaTypeOrNull(), "data$it"))
                    .build()
                client.newCall(request).execute()
            }.apply { start() }
        }

        // 等待所有请求完成
        requests.forEach { it.join() }

        // 验证token刷新只调用了一次
        assertEquals(1, countRefreshToken)
    }

    @Test(expected = IOException::class)
    fun `刷新token失败时抛出异常`() {
        // 准备
        `when`(tokenProvider.getAccessToken()).thenReturn("expired_token")
        `when`(tokenProvider.refreshToken()).thenThrow(IOException("Refresh failed"))

        client = OkHttpClient.Builder()
            .addInterceptor(AuthTokenInterceptor(tokenProvider))
            .build()

        server.enqueue(MockResponse().setResponseCode(401))

        // 执行
        val request = Request.Builder()
            .url(server.url("/secure"))
            .post(RequestBody.create("text/plain".toMediaTypeOrNull(), "data"))
            .build()

        client.newCall(request).execute()
    }
}
