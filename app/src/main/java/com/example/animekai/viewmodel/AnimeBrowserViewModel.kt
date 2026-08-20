package com.example.animekai.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.webkit.WebChromeClient
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.animekai.bookmarks.BookmarkManager
import com.example.animekai.network.NetworkMonitor
import com.example.animekai.offline.OfflinePage
import com.example.animekai.offline.OfflinePageManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppThemeMode {
    DARK, LIGHT, SYSTEM
}

sealed class WebCommand {
    data class LoadUrl(val url: String) : WebCommand()
    object GoBack : WebCommand()
    object GoForward : WebCommand()
    object Reload : WebCommand()
    object StopLoading : WebCommand()
    object ClearCache : WebCommand()
}

class AnimeBrowserViewModel(application: Application) : AndroidViewModel(application) {

    val offlinePageManager = OfflinePageManager(application)
    val bookmarkManager = BookmarkManager(application)
    val networkMonitor = NetworkMonitor(application)

    private val prefs: SharedPreferences =
        application.getSharedPreferences("animekai_settings", Context.MODE_PRIVATE)

    // Web Page State
    private val _currentUrl = MutableStateFlow(HOME_URL)
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _pageTitle = MutableStateFlow("AnimeKai - Anime Portal")
    val pageTitle: StateFlow<String> = _pageTitle.asStateFlow()

    private val _loadingProgress = MutableStateFlow(0)
    val loadingProgress: StateFlow<Int> = _loadingProgress.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private val _canGoForward = MutableStateFlow(false)
    val canGoForward: StateFlow<Boolean> = _canGoForward.asStateFlow()

    // Features State
    private val _isAdBlockerEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_AD_BLOCK_ENABLED, true)
    )
    val isAdBlockerEnabled: StateFlow<Boolean> = _isAdBlockerEnabled.asStateFlow()

    private val _blockedAdCount = MutableStateFlow(0)
    val blockedAdCount: StateFlow<Int> = _blockedAdCount.asStateFlow()

    private val _whitelistedDomains = MutableStateFlow<Set<String>>(emptySet())
    val whitelistedDomains: StateFlow<Set<String>> = _whitelistedDomains.asStateFlow()

    private val _isCurrentSiteWhitelisted = MutableStateFlow(false)
    val isCurrentSiteWhitelisted: StateFlow<Boolean> = _isCurrentSiteWhitelisted.asStateFlow()

    private val _themeMode = MutableStateFlow(
        AppThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name)
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _isDesktopMode = MutableStateFlow(false)
    val isDesktopMode: StateFlow<Boolean> = _isDesktopMode.asStateFlow()

    // Offline view state
    private val _currentOfflinePage = MutableStateFlow<OfflinePage?>(null)
    val currentOfflinePage: StateFlow<OfflinePage?> = _currentOfflinePage.asStateFlow()

    // Video Fullscreen State
    private val _customView = MutableStateFlow<View?>(null)
    val customView: StateFlow<View?> = _customView.asStateFlow()
    var customViewCallback: WebChromeClient.CustomViewCallback? = null

    // UI Dialogs & BottomSheets
    private val _showOfflineSheet = MutableStateFlow(false)
    val showOfflineSheet: StateFlow<Boolean> = _showOfflineSheet.asStateFlow()

    private val _showAdBlockSheet = MutableStateFlow(false)
    val showAdBlockSheet: StateFlow<Boolean> = _showAdBlockSheet.asStateFlow()

    private val _showExploreSheet = MutableStateFlow(false)
    val showExploreSheet: StateFlow<Boolean> = _showExploreSheet.asStateFlow()

    private val _showBookmarksSheet = MutableStateFlow(false)
    val showBookmarksSheet: StateFlow<Boolean> = _showBookmarksSheet.asStateFlow()

    private val _showSettingsSheet = MutableStateFlow(false)
    val showSettingsSheet: StateFlow<Boolean> = _showSettingsSheet.asStateFlow()

    // Web Action Commands
    private val _webCommands = MutableSharedFlow<WebCommand>()
    val webCommands: SharedFlow<WebCommand> = _webCommands.asSharedFlow()

    fun navigateTo(url: String) {
        val target = if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://")) {
            url
        } else {
            "https://$url"
        }
        _currentOfflinePage.value = null
        viewModelScope.launch {
            _webCommands.emit(WebCommand.LoadUrl(target))
        }
    }

    fun searchAnime(query: String) {
        if (query.isNotBlank()) {
            val encoded = java.net.URLEncoder.encode(query.trim(), "UTF-8")
            navigateTo("https://animekai.be/search?keyword=$encoded")
        }
    }

    fun goHome() {
        navigateTo(HOME_URL)
    }

    fun goBack() {
        viewModelScope.launch {
            _webCommands.emit(WebCommand.GoBack)
        }
    }

    fun goForward() {
        viewModelScope.launch {
            _webCommands.emit(WebCommand.GoForward)
        }
    }

    fun reload() {
        viewModelScope.launch {
            _webCommands.emit(WebCommand.Reload)
        }
    }

    fun stopLoading() {
        viewModelScope.launch {
            _webCommands.emit(WebCommand.StopLoading)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _webCommands.emit(WebCommand.ClearCache)
        }
    }

    fun loadOfflinePage(page: OfflinePage) {
        _currentOfflinePage.value = page
        viewModelScope.launch {
            _webCommands.emit(WebCommand.LoadUrl("file://${page.filePath}"))
        }
    }

    fun toggleCurrentSiteWhitelist() {
        val current = _currentUrl.value
        val host = try {
            android.net.Uri.parse(current).host ?: "animekai.be"
        } catch (e: Exception) {
            "animekai.be"
        }
        com.example.animekai.adblock.AdBlocker.toggleWhitelist(host)
        _whitelistedDomains.value = com.example.animekai.adblock.AdBlocker.getWhitelistedDomains()
        _isCurrentSiteWhitelisted.value = com.example.animekai.adblock.AdBlocker.isWhitelisted(current)
        reload()
    }

    fun addWhitelistedDomain(domain: String) {
        com.example.animekai.adblock.AdBlocker.addWhitelist(domain)
        _whitelistedDomains.value = com.example.animekai.adblock.AdBlocker.getWhitelistedDomains()
        _isCurrentSiteWhitelisted.value = com.example.animekai.adblock.AdBlocker.isWhitelisted(_currentUrl.value)
    }

    fun removeWhitelistedDomain(domain: String) {
        com.example.animekai.adblock.AdBlocker.removeWhitelist(domain)
        _whitelistedDomains.value = com.example.animekai.adblock.AdBlocker.getWhitelistedDomains()
        _isCurrentSiteWhitelisted.value = com.example.animekai.adblock.AdBlocker.isWhitelisted(_currentUrl.value)
    }

    fun toggleAdBlocker() {
        val newState = !_isAdBlockerEnabled.value
        _isAdBlockerEnabled.value = newState
        prefs.edit().putBoolean(KEY_AD_BLOCK_ENABLED, newState).apply()
        reload()
    }

    fun incrementBlockedCount() {
        _blockedAdCount.value += 1
    }

    fun resetBlockedCountForPage() {
        // Keeps cumulative count or page level count
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun toggleDesktopMode() {
        _isDesktopMode.value = !_isDesktopMode.value
        reload()
    }

    fun toggleBookmarkCurrentPage() {
        val url = _currentUrl.value
        val title = _pageTitle.value
        if (bookmarkManager.isBookmarked(url)) {
            val existing = bookmarkManager.bookmarks.value.firstOrNull { it.url == url }
            if (existing != null) {
                bookmarkManager.removeBookmark(existing.id)
            }
        } else {
            bookmarkManager.addBookmark(title, url)
        }
    }

    // Setters for WebView callbacks
    fun updateUrl(url: String) {
        _currentUrl.value = url
        _isCurrentSiteWhitelisted.value = com.example.animekai.adblock.AdBlocker.isWhitelisted(url)
    }

    fun updateTitle(title: String) {
        if (title.isNotBlank() && !title.startsWith("http")) {
            _pageTitle.value = title
        }
    }

    fun updateProgress(progress: Int) {
        _loadingProgress.value = progress
        _isLoading.value = progress in 1..99
    }

    fun updateNavigationState(canBack: Boolean, canForward: Boolean) {
        _canGoBack.value = canBack
        _canGoForward.value = canForward
    }

    fun setFullscreenView(view: View?, callback: WebChromeClient.CustomViewCallback?) {
        _customView.value = view
        this.customViewCallback = callback
    }

    fun exitFullscreen() {
        customViewCallback?.onCustomViewHidden()
        _customView.value = null
        customViewCallback = null
    }

    // Sheet visibility toggles
    fun setShowOfflineSheet(show: Boolean) { _showOfflineSheet.value = show }
    fun setShowAdBlockSheet(show: Boolean) { _showAdBlockSheet.value = show }
    fun setShowExploreSheet(show: Boolean) { _showExploreSheet.value = show }
    fun setShowBookmarksSheet(show: Boolean) { _showBookmarksSheet.value = show }
    fun setShowSettingsSheet(show: Boolean) { _showSettingsSheet.value = show }

    companion object {
        const val HOME_URL = "https://animekai.be/home"
        private const val KEY_AD_BLOCK_ENABLED = "key_ad_block_enabled"
        private const val KEY_THEME_MODE = "key_theme_mode"
    }
}
