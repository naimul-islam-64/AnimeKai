package com.example.animekai.ui

import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.AnimeMagenta
import com.example.animekai.viewmodel.AnimeBrowserViewModel

@Composable
fun FullscreenVideoOverlay(
    customView: View,
    viewModel: AnimeBrowserViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.exitFullscreen()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("fullscreen_video_container")
    ) {
        AndroidView(
            factory = {
                (customView.parent as? ViewGroup)?.removeView(customView)
                customView
            },
            modifier = Modifier.fillMaxSize()
        )

        FloatingActionButton(
            onClick = { viewModel.exitFullscreen() },
            containerColor = AnimeMagenta.copy(alpha = 0.85f),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .size(44.dp)
                .testTag("exit_fullscreen_button")
        ) {
            Icon(
                imageVector = Icons.Default.FullscreenExit,
                contentDescription = "Exit Fullscreen"
            )
        }
    }
}
