package com.example.appauto

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ScrollingView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import kotlin.concurrent.thread


class WebActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.webactivity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.responseText)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        thread{
            try {
                //创建okhttp实例
                val client = OkHttpClient()
                //发起请求
                val request = Request.Builder().url("https://cloud.sinognss.com/cm/#/deviceManage").build()
                //请求返回数据
                val response = client.newCall(request).execute()
                val responseData = response.body?.string()
                if (responseData != null) {
                    showResponse(responseData)
                }
            }
            catch (e:Exception){
                e.printStackTrace()
            }
        }
        }
    private fun showResponse(response: String){
        val resTe = findViewById<TextView>(R.id.responseText)
        runOnUiThread{
            resTe.text = response
        }
    }
    }
