package com.example.animekai.webview

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.animekai.adblock.AdBlocker
import com.example.animekai.offline.OfflineFallbackPage
import com.example.animekai.viewmodel.AnimeBrowserViewModel
import com.example.animekai.viewmodel.AppThemeMode
import java.io.ByteArrayInputStream

class AnimeWebViewClient(
    private val viewModel: AnimeBrowserViewModel,
    private val isSystemDark: Boolean
) : WebViewClient() {

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString() ?: return null

        if (viewModel.isAdBlockerEnabled.value && AdBlocker.isAd(url)) {
            Log.d(TAG, "AdBlocker blocked network resource: $url")
            viewModel.incrementBlockedCount()
            // Return empty 200 response to cleanly drop the ad request without page crash
            return WebResourceResponse(
                "text/plain",
                "UTF-8",
                200,
                "OK",
                mapOf("Access-Control-Allow-Origin" to "*"),
                ByteArrayInputStream(ByteArray(0))
            )
        }

        return super.shouldInterceptRequest(view, request)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false
        val currentUrl = view?.url ?: viewModel.currentUrl.value

        // Handle internal app action schemes from offline page or navigation buttons
        if (url.startsWith("animekai://action/offline_sheet")) {
            viewModel.setShowOfflineSheet(true)
            return true
        }
        if (url.startsWith("animekai://action/explore_sheet")) {
            viewModel.setShowExploreSheet(true)
            return true
        }
        if (url.startsWith("animekai://action/retry")) {
            val targetUrl = Uri.parse(url).getQueryParameter("url") ?: AnimeBrowserViewModel.HOME_URL
            viewModel.navigateTo(targetUrl)
            return true
        }

        // Block rogue ad redirects, popups, and click-anywhere navigation traps
        if (viewModel.isAdBlockerEnabled.value) {
            if (AdBlocker.shouldBlockNavigation(url, currentUrl)) {
                Log.d(TAG, "AdBlocker blocked rogue redirect navigation: $url (from $currentUrl)")
                viewModel.incrementBlockedCount()
                return true
            }
        }

        // Allow internal site navigation and legitimate http/https/file
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://")) {
            return false
        }

        // Catch rogue external app redirects (market://, intent://) triggered by ad networks
        if (viewModel.isAdBlockerEnabled.value && AdBlocker.isSuspiciousScheme(url)) {
            Log.d(TAG, "Blocked suspicious external scheme launch: $url")
            viewModel.incrementBlockedCount()
            return true
        }

        // Handle legitimate external intents if user explicitly intended
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
            view?.context?.startActivity(intent)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Cannot handle custom scheme: $url", e)
            return true
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            val failingUrl = request.url.toString()
            Log.w(TAG, "Main frame error on $failingUrl: ${error?.description}")

            // 1. If page is already saved in 10-page offline library, load the offline file version seamlessly
            val cachedPage = viewModel.offlinePageManager.getOfflineVersionForUrl(failingUrl)
            if (cachedPage != null) {
                Log.d(TAG, "Found offline cached page for $failingUrl, serving file: ${cachedPage.filePath}")
                view?.loadUrl("file://${cachedPage.filePath}")
                return
            }

            // 2. If page is NOT loaded/cached, render the custom AnimeKai offline page
            val offlineHtml = OfflineFallbackPage.generateHtml(
                attemptedUrl = failingUrl,
                offlinePages = viewModel.offlinePageManager.offlinePages.value
            )
            view?.loadDataWithBaseURL("about:blank", offlineHtml, "text/html", "UTF-8", null)
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        url?.let {
            viewModel.updateUrl(it)
            viewModel.updateNavigationState(view?.canGoBack() == true, view?.canGoForward() == true)
        }

        // Early injection of AdShield to prevent early scripts from escaping window.open interception
        if (viewModel.isAdBlockerEnabled.value) {
            view?.evaluateJavascript(AdBlocker.AD_SHIELD_JS, null)
            view?.evaluateJavascript(AdBlocker.AD_HIDING_CSS, null)
        }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        view ?: return
        val currentUrl = url ?: ""
        viewModel.updateUrl(currentUrl)
        viewModel.updateNavigationState(view.canGoBack(), view.canGoForward())

        val title = view.title ?: ""
        viewModel.updateTitle(title)

        // Inject AdBlocker CSS and Anti-Popup / Anti-Redirect JS
        if (viewModel.isAdBlockerEnabled.value) {
            view.evaluateJavascript(AdBlocker.AD_SHIELD_JS, null)
            view.evaluateJavascript(AdBlocker.AD_HIDING_CSS, null)
        }

        // Inject Dark Mode styling if user requested Dark theme
        val isDark = when (viewModel.themeMode.value) {
            AppThemeMode.DARK -> true
            AppThemeMode.LIGHT -> false
            AppThemeMode.SYSTEM -> isSystemDark
        }

        if (isDark) {
            val darkModeCss = """
                (function() {
                    var bg = window.getComputedStyle(document.body).backgroundColor;
                    if (bg === 'rgb(255, 255, 255)' || bg === 'rgba(0, 0, 0, 0)') {
                        document.documentElement.style.backgroundColor = '#0E0C18';
                        document.body.style.backgroundColor = '#0E0C18';
                    }
                })();
            """
            view.evaluateJavascript(darkModeCss, null)
        }

        // Auto-save last 10 visited pages for offline view (only for genuine online web pages)
        if (currentUrl.startsWith("http") && !currentUrl.contains("error") && !currentUrl.contains("about:blank")) {
            viewModel.offlinePageManager.savePage(view, currentUrl, title)
        }
    }

    companion object {
        private const val TAG = "AnimeWebViewClient"
    }
}
