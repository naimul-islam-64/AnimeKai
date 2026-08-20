package com.example.animekai.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AnimeCyan
import com.example.ui.theme.AnimeMagenta
import com.example.ui.theme.AnimeViolet
import com.example.animekai.viewmodel.AnimeBrowserViewModel
import com.example.animekai.viewmodel.AppThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBrowserBar(
    viewModel: AnimeBrowserViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUrl by viewModel.currentUrl.collectAsState()
    val pageTitle by viewModel.pageTitle.collectAsState()
    val loadingProgress by viewModel.loadingProgress.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isAdBlockerEnabled by viewModel.isAdBlockerEnabled.collectAsState()
    val blockedCount by viewModel.blockedAdCount.collectAsState()
    val bookmarks by viewModel.bookmarkManager.bookmarks.collectAsState()
    val isOnline by viewModel.networkMonitor.isOnline.collectAsState()
    val isBookmarked = bookmarks.any { it.url == currentUrl }
    val currentOfflinePage by viewModel.currentOfflinePage.collectAsState()

    Column(modifier = modifier.fillMaxWidth()) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            ),
            title = {
                // Address & Status Pill
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { viewModel.setShowExploreSheet(true) }
                        .testTag("address_bar_pill"),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isOnline) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "Offline",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        } else if (currentOfflinePage != null) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(AnimeCyan)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Secure SSL",
                                tint = AnimeCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (currentOfflinePage != null) "[Offline] ${currentOfflinePage?.title}" else pageTitle,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (currentOfflinePage != null) "Cached Archive" else getDisplayHost(currentUrl),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            navigationIcon = {
                // Brand Badge
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 4.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(AnimeMagenta, AnimeViolet))
                        )
                        .clickable { viewModel.goHome() }
                        .testTag("brand_logo_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AK",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            },
            actions = {
                // Ad Blocker Shield Action
                IconButton(
                    onClick = { viewModel.setShowAdBlockSheet(true) },
                    modifier = Modifier.testTag("ad_blocker_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (isAdBlockerEnabled && blockedCount > 0) {
                                Badge(
                                    containerColor = AnimeMagenta,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = if (blockedCount > 99) "99+" else "$blockedCount",
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Ad Blocker Shield",
                            tint = if (isAdBlockerEnabled) AnimeCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Bookmark Toggle Action
                IconButton(
                    onClick = { viewModel.toggleBookmarkCurrentPage() },
                    modifier = Modifier.testTag("bookmark_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) AnimeMagenta else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Theme Mode Switch Action
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Dark/Light Theme",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // More Settings Action
                IconButton(
                    onClick = { viewModel.setShowSettingsSheet(true) },
                    modifier = Modifier.testTag("settings_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )

        // Smooth Neon Loading Progress Bar
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LinearProgressIndicator(
                progress = { loadingProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = AnimeCyan,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

private fun getDisplayHost(url: String): String {
    return try {
        val uri = android.net.Uri.parse(url)
        val host = uri.host
        if (!host.isNullOrBlank()) host else url
    } catch (e: Exception) {
        url
    }
}
