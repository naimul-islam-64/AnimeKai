package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.animekai.ui.AnimeKaiMainScreen
import com.example.ui.theme.AnimeKaiTheme
import com.example.animekai.viewmodel.AnimeBrowserViewModel
import com.example.animekai.viewmodel.AppThemeMode
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: AnimeBrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val systemDark = isSystemInDarkTheme()

            val isDarkTheme = when (themeMode) {
                AppThemeMode.DARK -> true
                AppThemeMode.LIGHT -> false
                AppThemeMode.SYSTEM -> systemDark
            }

            // Observe custom fullscreen video view to trigger orientation change & immersive system bars
            LaunchedEffect(viewModel) {
                viewModel.customView.collectLatest { customView ->
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    if (customView != null) {
                        // Rotate app to landscape when anime video goes fullscreen for optimal viewing
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        insetsController.hide(WindowInsetsCompat.Type.systemBars())
                        insetsController.systemBarsBehavior =
                            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    } else {
                        // Restore standard orientation and system bars on exit
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        insetsController.show(WindowInsetsCompat.Type.systemBars())
                    }
                }
            }

            AnimeKaiTheme(darkTheme = isDarkTheme) {
                AnimeKaiMainScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = {
                        val nextMode = if (isDarkTheme) AppThemeMode.LIGHT else AppThemeMode.DARK
                        viewModel.setThemeMode(nextMode)
                    }
                )
            }
        }
    }
}
