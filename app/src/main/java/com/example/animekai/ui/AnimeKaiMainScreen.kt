package com.example.animekai.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.animekai.ui.sheets.BookmarksSheet
import com.example.animekai.ui.sheets.ExploreSearchSheet
import com.example.animekai.ui.sheets.OfflinePagesSheet
import com.example.animekai.ui.sheets.SettingsSheet
import com.example.animekai.ui.sheets.WhitelistSheet
import com.example.animekai.viewmodel.AnimeBrowserViewModel
import com.example.animekai.webview.AnimeWebViewContainer
import com.example.ui.theme.AnimeMagenta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeKaiMainScreen(
    viewModel: AnimeBrowserViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canGoBack by viewModel.canGoBack.collectAsState()
    val customView by viewModel.customView.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingProgress by viewModel.loadingProgress.collectAsState()

    // Sheet states
    val showOfflineSheet by viewModel.showOfflineSheet.collectAsState()
    val showAdBlockSheet by viewModel.showAdBlockSheet.collectAsState()
    val showExploreSheet by viewModel.showExploreSheet.collectAsState()
    val showBookmarksSheet by viewModel.showBookmarksSheet.collectAsState()
    val showSettingsSheet by viewModel.showSettingsSheet.collectAsState()

    val offlineSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val whitelistSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val exploreSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val bookmarksSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle System Back Button
    BackHandler(enabled = canGoBack || customView != null) {
        if (customView != null) {
            viewModel.exitFullscreen()
        } else if (canGoBack) {
            viewModel.goBack()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main Screen Structure with single bottom bar (hidden in video fullscreen mode)
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                AnimatedVisibility(
                    visible = customView == null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    BottomBrowserBar(viewModel = viewModel)
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Permanent, uninterrupted WebView Container
                AnimeWebViewContainer(
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("anime_webview_container")
                )

                // Slim, non-intrusive loading bar at top edge
                if (isLoading && customView == null) {
                    LinearProgressIndicator(
                        progress = { loadingProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.TopCenter)
                            .testTag("loading_progress_bar"),
                        color = AnimeMagenta,
                        trackColor = Color.Transparent
                    )
                }
            }
        }

        // Fullscreen Video Player Overlay (mounted on top of WebView without reloading it)
        if (customView != null) {
            FullscreenVideoOverlay(
                customView = customView!!,
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(100f)
            )
        }
    }

    // Modal Bottom Sheets
    if (showOfflineSheet) {
        OfflinePagesSheet(
            viewModel = viewModel,
            sheetState = offlineSheetState,
            onDismiss = { viewModel.setShowOfflineSheet(false) }
        )
    }

    if (showAdBlockSheet) {
        WhitelistSheet(
            viewModel = viewModel,
            sheetState = whitelistSheetState,
            onDismiss = { viewModel.setShowAdBlockSheet(false) }
        )
    }

    if (showExploreSheet) {
        ExploreSearchSheet(
            viewModel = viewModel,
            sheetState = exploreSheetState,
            onDismiss = { viewModel.setShowExploreSheet(false) }
        )
    }

    if (showBookmarksSheet) {
        BookmarksSheet(
            viewModel = viewModel,
            sheetState = bookmarksSheetState,
            onDismiss = { viewModel.setShowBookmarksSheet(false) }
        )
    }

    if (showSettingsSheet) {
        SettingsSheet(
            viewModel = viewModel,
            sheetState = settingsSheetState,
            onDismiss = { viewModel.setShowSettingsSheet(false) }
        )
    }
}
