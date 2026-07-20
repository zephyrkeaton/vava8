package com.vava8.app.ui.profile

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.vava8.app.Vava8App
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebInfoScreen(
    title: String,
    url: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    syncAppCookiesToWebView(url)
                    loadUrl(url)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}

/** 把 OkHttp 登录 Cookie 同步到 WebView，个人中心等需登录页面才能正常打开。 */
private fun syncAppCookiesToWebView(pageUrl: String) {
    val jar = Vava8App.instance.cookieJar
    val manager = CookieManager.getInstance()
    manager.setAcceptCookie(true)
    val target = pageUrl.toHttpUrlOrNull()
    jar.snapshot().forEach { cookie ->
        if (target != null && !cookie.matches(target)) return@forEach
        val host = cookie.domain.trimStart('.')
        val cookieUrl = "https://$host${cookie.path}"
        val parts = buildList {
            add("${cookie.name}=${cookie.value}")
            add("Path=${cookie.path}")
            if (!cookie.hostOnly) add("Domain=${cookie.domain}")
            if (cookie.secure) add("Secure")
            if (cookie.httpOnly) add("HttpOnly")
        }
        manager.setCookie(cookieUrl, parts.joinToString("; "))
    }
    manager.flush()
}
