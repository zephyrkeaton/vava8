package com.vava8.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.vava8.app.navigation.AppNavHost
import com.vava8.app.ui.theme.ReaderPrefs
import com.vava8.app.ui.theme.ReadingFontSize
import com.vava8.app.ui.theme.ThemeMode
import com.vava8.app.ui.theme.Vava8Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val prefs = Vava8App.instance.preferences
            val scope = rememberCoroutineScope()
            val themeMode by prefs.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.System)
            val noImageMode by prefs.noImageMode.collectAsStateWithLifecycle(initialValue = false)
            val fontSize by prefs.fontSize.collectAsStateWithLifecycle(initialValue = ReadingFontSize.Medium)
            val readerPrefs = ReaderPrefs(
                fontSize = fontSize,
                themeMode = themeMode,
                noImageMode = noImageMode
            )

            Vava8Theme(
                themeMode = themeMode,
                readerPrefs = readerPrefs
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavHost(
                        navController = navController,
                        onFontSizeChange = { size ->
                            scope.launch { prefs.setFontSize(size) }
                        },
                        onThemeModeChange = { mode ->
                            scope.launch { prefs.setThemeMode(mode) }
                        },
                        onNoImageModeChange = { enabled ->
                            scope.launch { prefs.setNoImageMode(enabled) }
                        }
                    )
                }
            }
        }
    }
}
