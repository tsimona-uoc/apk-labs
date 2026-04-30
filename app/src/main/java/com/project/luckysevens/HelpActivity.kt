package com.project.luckysevens

import android.graphics.Color
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class HelpActivity : AppCompatActivity() {

    private lateinit var helpWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        val topBar = findViewById<MaterialToolbar>(R.id.helpTopBar)
        helpWebView = findViewById(R.id.helpWebView)

        topBar.setNavigationOnClickListener {
            finish()
        }

        helpWebView.setBackgroundColor(Color.TRANSPARENT)
        helpWebView.webViewClient = WebViewClient()
        helpWebView.settings.apply {
            javaScriptEnabled = false
            domStorageEnabled = false
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
        }

        // 🔥 Cargar HTML según idioma de la app
        val language = LanguageManager.getLanguage(this)

        val fileName = if (language == "es") {
            "index_es.html"
        } else {
            "index_en.html"
        }

        helpWebView.loadUrl("file:///android_asset/help/$fileName")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (helpWebView.canGoBack()) {
                    helpWebView.goBack()
                } else {
                    finish()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        MusicService.send(this, MusicService.ACTION_RESUME)
    }
}