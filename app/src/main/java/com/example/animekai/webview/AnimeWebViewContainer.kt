package com.example.animekai.webview

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.animekai.viewmodel.AnimeBrowserViewModel
import com.example.animekai.viewmodel.WebCommand
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AnimeWebViewContainer(
    viewModel: AnimeBrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                setSupportMultipleWindows(false)
                javaScriptCanOpenWindowsAutomatically = false
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
                allowFileAccess = true
                allowContentAccess = true
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = WebSettings.LOAD_DEFAULT
            }

            // Cookie Management
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            // Handle downloads
            setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
                try {
                    val request = DownloadManager.Request(Uri.parse(url)).apply {
                        setMimeType(mimetype)
                        addRequestHeader("User-Agent", userAgent)
                        setDescription("Downloading anime media via AnimeKai...")
                        setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype))
                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            URLUtil.guessFileName(url, contentDisposition, mimetype)
                        )
                    }
                    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                    dm?.enqueue(request)
                    Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Attach Clients
    DisposableEffect(viewModel, isSystemDark) {
        val client = AnimeWebViewClient(viewModel, isSystemDark)
        val chromeClient = AnimeWebChromeClient(viewModel)
        webView.webViewClient = client
        webView.webChromeClient = chromeClient

        onDispose {
            // Keep webview clean
        }
    }

    // Handle desktop vs mobile user-agent toggle
    LaunchedEffect(viewModel.isDesktopMode) {
        viewModel.isDesktopMode.collectLatest { isDesktop ->
            val defaultUa = WebSettings.getDefaultUserAgent(context)
            if (isDesktop) {
                webView.settings.userAgentString =
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                webView.settings.useWideViewPort = true
                webView.settings.loadWithOverviewMode = true
            } else {
                webView.settings.userAgentString = defaultUa
            }
        }
    }

    // Listen to navigation commands
    LaunchedEffect(viewModel) {
        viewModel.webCommands.collectLatest { cmd ->
            when (cmd) {
                is WebCommand.LoadUrl -> webView.loadUrl(cmd.url)
                is WebCommand.GoBack -> if (webView.canGoBack()) webView.goBack()
                is WebCommand.GoForward -> if (webView.canGoForward()) webView.goForward()
                is WebCommand.Reload -> webView.reload()
                is WebCommand.StopLoading -> webView.stopLoading()
                is WebCommand.ClearCache -> {
                    webView.clearCache(true)
                    webView.clearHistory()
                    CookieManager.getInstance().removeAllCookies(null)
                    Toast.makeText(context, "Cache and cookies cleared", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Initial Load
    LaunchedEffect(Unit) {
        if (webView.url == null) {
            webView.loadUrl(AnimeBrowserViewModel.HOME_URL)
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier
    )
}

private object URLUtil {
    fun guessFileName(url: String, contentDisposition: String?, mimeType: String?): String {
        return android.webkit.URLUtil.guessFileName(url, contentDisposition, mimeType)
    }
}
