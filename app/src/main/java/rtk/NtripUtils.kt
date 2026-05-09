package com.example.appauto

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket

/**
 * 挂载点数据实体
 */
data class MountPointBean(
    val name: String,      // 名称 (STR;后面的第1项)
    val identifier: String,// 标识符
    val format: String,    // 格式 (RTCM 3.2 等)
    val navSystem: String  // 卫星系统 (GPS+BDS 等)
)

object NtripUtils {

    /**
     * 获取 Ntrip 源列表 (改为 Socket 实现，兼容 SOURCETABLE 响应头)
     */
    suspend fun getNtripSourceTable(host: String, port: Int): List<MountPointBean> {
        return withContext(Dispatchers.IO) {
            val list = mutableListOf<MountPointBean>()
            var socket: Socket? = null

            try {
                // 1. 建立原始 TCP 连接
                socket = Socket()
                // 设置连接超时 5秒
                socket.connect(InetSocketAddress(host, port), 5000)
                // 设置读取超时 5秒
                socket.soTimeout = 5000

                // 2. 手动发送 HTTP GET 请求报文
                val request = "GET / HTTP/1.0\r\n" +
                        "User-Agent: NTRIP GNSSInternetRadio/1.4.10\r\n" +
                        "Accept: */*\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" // 空行表示头结束

                val out = socket.getOutputStream()
                out.write(request.toByteArray())
                out.flush()

                // 3. 读取响应流
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                var line: String?

                // 4. 逐行解析
                while (reader.readLine().also { line = it } != null) {
                    // 无论服务器返回 "SOURCETABLE 200 OK" 还是 "HTTP/1.1 200 OK"
                    // 我们只关心以 "STR;" 开头的行
                    if (line?.startsWith("STR;") == true) {
                        val bean = parseStrLine(line!!)
                        if (bean != null) list.add(bean)
                    }
                }

                reader.close()
            } catch (e: Exception) {
                Log.e("NtripUtils", "获取源列表失败: ${e.message}")
                throw e
            } finally {
                try { socket?.close() } catch (_: Exception) {}
            }

            list
        }
    }

    // 解析一行 STR 数据
    // 格式: STR;MountName;Identifier;Format;...;NavSystem;...
    private fun parseStrLine(line: String): MountPointBean? {
        val parts = line.split(";")
        // Ntrip 协议中 STR 行至少要有 12 个字段
        if (parts.size < 5) return null

        // 容错处理：有些服务器字段不够全，用 getOrNull 防止越界
        return MountPointBean(
            name = parts.getOrNull(1) ?: "",
            identifier = parts.getOrNull(2) ?: "",
            format = parts.getOrNull(3) ?: "",
            navSystem = parts.getOrNull(6) ?: "" // 第7项通常是卫星系统
        )
    }
}