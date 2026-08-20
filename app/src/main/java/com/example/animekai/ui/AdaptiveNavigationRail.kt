package com.example.animekai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AnimeCyan
import com.example.ui.theme.AnimeMagenta
import com.example.ui.theme.AnimeViolet
import com.example.animekai.viewmodel.AnimeBrowserViewModel

@Composable
fun AdaptiveNavigationRail(
    viewModel: AnimeBrowserViewModel,
    modifier: Modifier = Modifier
) {
    val canGoBack by viewModel.canGoBack.collectAsState()
    val canGoForward by viewModel.canGoForward.collectAsState()
    val offlinePages by viewModel.offlinePageManager.offlinePages.collectAsState()
    val isAdBlockerEnabled by viewModel.isAdBlockerEnabled.collectAsState()
    val blockedCount by viewModel.blockedAdCount.collectAsState()

    NavigationRail(
        modifier = modifier.testTag("tablet_nav_rail"),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        header = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(AnimeMagenta, AnimeViolet))
                    )
                    .clickable { viewModel.goHome() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AK",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }
    ) {
        NavigationRailItem(
            selected = false,
            onClick = { viewModel.goHome() },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = AnimeViolet) },
            label = { Text("Home", fontSize = 11.sp) },
            modifier = Modifier.testTag("rail_home_button")
        )

        NavigationRailItem(
            selected = false,
            onClick = { viewModel.setShowExploreSheet(true) },
            icon = { Icon(Icons.Default.Search, contentDescription = "Search Anime", tint = AnimeCyan) },
            label = { Text("Explore", fontSize = 11.sp) },
            modifier = Modifier.testTag("rail_explore_button")
        )

        NavigationRailItem(
            selected = false,
            onClick = { viewModel.setShowOfflineSheet(true) },
            icon = {
                BadgedBox(
                    badge = {
                        if (offlinePages.isNotEmpty()) {
                            Badge(
                                containerColor = AnimeMagenta,
                                contentColor = Color.White
                            ) {
                                Text("${offlinePages.size}", fontSize = 9.sp)
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.DownloadDone,
                        contentDescription = "Offline Cache",
                        tint = if (offlinePages.isNotEmpty()) AnimeMagenta else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            label = { Text("Offline", fontSize = 11.sp) },
            modifier = Modifier.testTag("rail_offline_button")
        )

        NavigationRailItem(
            selected = false,
            onClick = { viewModel.setShowBookmarksSheet(true) },
            icon = { Icon(Icons.Default.Bookmarks, contentDescription = "Bookmarks", tint = AnimeViolet) },
            label = { Text("Saved", fontSize = 11.sp) },
            modifier = Modifier.testTag("rail_bookmarks_button")
        )

        NavigationRailItem(
            selected = false,
            onClick = { viewModel.setShowAdBlockSheet(true) },
            icon = {
                BadgedBox(
                    badge = {
                        if (isAdBlockerEnabled && blockedCount > 0) {
                            Badge(
                                containerColor = AnimeMagenta,
                                contentColor = Color.White
                            ) {
                                Text(if (blockedCount > 99) "99+" else "$blockedCount", fontSize = 9.sp)
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = "Ad Shield",
                        tint = if (isAdBlockerEnabled) AnimeCyan else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            label = { Text("Shield", fontSize = 11.sp) },
            modifier = Modifier.testTag("rail_ad_shield_button")
        )

        Spacer(modifier = Modifier.weight(1f))

        NavigationRailItem(
            selected = false,
            onClick = { if (canGoBack) viewModel.goBack() },
            enabled = canGoBack,
            icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") },
            label = { Text("Back", fontSize = 10.sp) }
        )

        NavigationRailItem(
            selected = false,
            onClick = { if (canGoForward) viewModel.goForward() },
            enabled = canGoForward,
            icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward") },
            label = { Text("Next", fontSize = 10.sp) }
        )

        NavigationRailItem(
            selected = false,
            onClick = { viewModel.reload() },
            icon = { Icon(Icons.Default.Refresh, contentDescription = "Reload") },
            label = { Text("Reload", fontSize = 10.sp) }
        )

        NavigationRailItem(
            selected = false,
            onClick = { viewModel.setShowSettingsSheet(true) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings", fontSize = 10.sp) },
            modifier = Modifier.testTag("rail_settings_button")
        )
        
        Spacer(modifier = Modifier.height(12.dp))
    }
}
