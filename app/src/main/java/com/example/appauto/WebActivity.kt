package com.example.appauto

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat

class WebActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var toolbar: Toolbar

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.webactivity)

        window.statusBarColor = Color.parseColor("#111827")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        toolbar = findViewById(R.id.toolbarWeb)
        progressBar = findViewById(R.id.progressWeb)
        errorText = findViewById(R.id.textWebError)
        webView = findViewById(R.id.webViewOfficial)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.setNavigationOnClickListener { finish() }

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView.settings.loadsImagesAutomatically = true
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress >= 100) View.GONE else View.VISIBLE
                if (newProgress > 15) {
                    errorText.visibility = View.GONE
                }
            }
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageCommitVisible(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
                toolbar.title = view?.title?.takeIf { it.isNotBlank() } ?: "司南官网"
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    progressBar.visibility = View.GONE
                    errorText.visibility = View.VISIBLE
                    errorText.text = "页面加载失败：${error?.description ?: "未知错误"}"
                }
            }
        }

        webView.loadUrl("https://www.sinognss.com")
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()
        webView.destroy()
        super.onDestroy()
    }
}
