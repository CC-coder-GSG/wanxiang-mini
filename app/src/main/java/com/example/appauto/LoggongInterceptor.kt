package com.example.appauto

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException

class LoggingInterceptor: Interceptor {
    companion object {
        fun RequestBody.asString(): String? {
            return try {
                val buffer = okio.Buffer()
                this.writeTo(buffer)
                buffer.readUtf8()
            } catch (e: IOException) {
                "Unable to convert request body to string: ${e.message}"
            }
        }
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val t1 = System.nanoTime()
        println("发送请求: ${request.url}")
        println("请求方法: ${request.method}")
        println("请求头: ${request.headers}")
        println("请求体: ${request.body?.asString()}")

        val response = chain.proceed(request)
        val t2 = System.nanoTime()

        println("接收响应: ${response.code} ${response.message}")
        println("响应头: ${response.headers}")
        println("响应体: ${response.body?.asString()}")
        println("请求耗时: ${(t2 - t1) / 1e6} ms")

        return response
    }

    // 扩展函数，将ResponseBody转换为字符串
    fun ResponseBody.asString(): String {
        return this.source().let { source ->
            source.request(Long.MAX_VALUE) // 读取整个响应体
            val buffer = source.buffer()
            buffer.clone().readUtf8()
        }
    }
}
