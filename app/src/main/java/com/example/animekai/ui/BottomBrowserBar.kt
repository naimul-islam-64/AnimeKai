package com.example.animekai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AnimeCyan
import com.example.ui.theme.AnimeMagenta
import com.example.ui.theme.AnimeViolet
import com.example.animekai.viewmodel.AnimeBrowserViewModel

@Composable
fun BottomBrowserBar(
    viewModel: AnimeBrowserViewModel,
    modifier: Modifier = Modifier
) {
    val offlinePages by viewModel.offlinePageManager.offlinePages.collectAsState()
    val isSiteWhitelisted by viewModel.isCurrentSiteWhitelisted.collectAsState()
    val isShieldEnabled by viewModel.isAdBlockerEnabled.collectAsState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 10.dp
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .height(64.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home
                BottomBarItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    tint = AnimeViolet,
                    testTag = "nav_home_button",
                    onClick = { viewModel.goHome() }
                )

                // 2. Explore
                BottomBarItem(
                    icon = Icons.Default.Explore,
                    label = "Explore",
                    tint = AnimeCyan,
                    testTag = "nav_explore_button",
                    onClick = { viewModel.setShowExploreSheet(true) }
                )

                // 3. Whitelist
                BottomBarItem(
                    icon = Icons.Default.Security,
                    label = "Whitelist",
                    tint = if (isSiteWhitelisted) AccentGreen else if (isShieldEnabled) AnimeCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    badgeContent = if (isSiteWhitelisted) {
                        {
                            Badge(
                                containerColor = AccentGreen,
                                modifier = Modifier.size(7.dp)
                            )
                        }
                    } else null,
                    testTag = "nav_whitelist_button",
                    onClick = { viewModel.setShowAdBlockSheet(true) }
                )

                // 4. Offline
                BottomBarItem(
                    icon = Icons.Default.CloudDownload,
                    label = "Offline",
                    tint = if (offlinePages.isNotEmpty()) AnimeMagenta else MaterialTheme.colorScheme.onSurfaceVariant,
                    badgeContent = if (offlinePages.isNotEmpty()) {
                        {
                            Badge(
                                containerColor = AnimeMagenta,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = "${offlinePages.size}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else null,
                    testTag = "nav_offline_button",
                    onClick = { viewModel.setShowOfflineSheet(true) }
                )

                // 5. Settings
                BottomBarItem(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    testTag = "nav_settings_button",
                    onClick = { viewModel.setShowSettingsSheet(true) }
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    testTag: String,
    onClick: () -> Unit,
    badgeContent: (@Composable androidx.compose.foundation.layout.BoxScope.() -> Unit)? = null
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(54.dp)
            .testTag(testTag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (badgeContent != null) {
                BadgedBox(badge = { badgeContent() }) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = tint
            )
        }
    }
}
