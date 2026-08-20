package com.example.animekai.webview

import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.example.animekai.viewmodel.AnimeBrowserViewModel

class AnimeWebChromeClient(
    private val viewModel: AnimeBrowserViewModel
) : WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        viewModel.updateProgress(newProgress)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        title?.let { viewModel.updateTitle(it) }
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        super.onShowCustomView(view, callback)
        // Enables full screen video player for anime episodes
        viewModel.setFullscreenView(view, callback)
    }

    override fun onHideCustomView() {
        super.onHideCustomView()
        viewModel.exitFullscreen()
    }

    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        // Block all popup windows initiated by ad networks or video player click traps
        if (viewModel.isAdBlockerEnabled.value) {
            Log.d(TAG, "Blocked popup/new-window creation attempt (isUserGesture=$isUserGesture)")
            viewModel.incrementBlockedCount()
            return false
        }
        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
    }

    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        // Prevent intrusive spam alerts (e.g. fake virus notices, lottery traps)
        if (viewModel.isAdBlockerEnabled.value) {
            result?.confirm()
            return true
        }
        return super.onJsAlert(view, url, message, result)
    }

    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        if (viewModel.isAdBlockerEnabled.value) {
            result?.cancel()
            return true
        }
        return super.onJsConfirm(view, url, message, result)
    }

    override fun onJsPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?
    ): Boolean {
        if (viewModel.isAdBlockerEnabled.value) {
            result?.cancel()
            return true
        }
        return super.onJsPrompt(view, url, message, defaultValue, result)
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        return true
    }

    companion object {
        private const val TAG = "AnimeWebChromeClient"
    }
}
