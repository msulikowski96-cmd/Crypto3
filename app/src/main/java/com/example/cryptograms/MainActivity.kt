package com.example.cryptograms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cryptograms.game.GameViewModel
import com.example.cryptograms.ui.GameScreen
import com.example.cryptograms.ui.HowToPlaySheet
import com.example.cryptograms.ui.SettingsScreen
import com.example.cryptograms.ui.theme.CryptogramsTheme
import com.example.cryptograms.ui.theme.Palette
import com.example.cryptograms.ui.theme.dottedBackground

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bars = SystemBarStyle.dark(Palette.Background.toArgb())
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)

        setContent {
            CryptogramsTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                val settings by viewModel.settings.collectAsStateWithLifecycle()
                val stats by viewModel.stats.collectAsStateWithLifecycle()
                val mistakeEvents by viewModel.mistakeEvents.collectAsStateWithLifecycle()
                var onSettings by rememberSaveable { mutableStateOf(false) }

                // Stop the clock while the app is in the background.
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_START -> viewModel.setScreenVisible(true)
                            Lifecycle.Event.ON_STOP -> viewModel.setScreenVisible(false)
                            else -> Unit
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                Box(
                    Modifier
                        .fillMaxSize()
                        .dottedBackground()
                        .safeDrawingPadding()
                ) {
                    if (onSettings) {
                        BackHandler { onSettings = false }
                        SettingsScreen(
                            settings = settings,
                            stats = stats,
                            onBack = { onSettings = false },
                            onUpdate = viewModel.repository::update,
                            onLanguage = viewModel::setLanguage,
                            onResetStats = viewModel.repository::resetStats,
                        )
                    } else {
                        GameScreen(
                            state = state,
                            settings = settings,
                            stats = stats,
                            mistakeEvents = mistakeEvents,
                            onBack = ::finish,
                            onNewPuzzle = viewModel::newPuzzle,
                            onOpenSettings = { onSettings = true },
                            onSelect = viewModel::select,
                            onType = viewModel::type,
                            onErase = viewModel::erase,
                            onUndo = viewModel::undo,
                            onHint = viewModel::hint,
                            onTogglePause = viewModel::togglePause,
                            onRevealSolution = viewModel::revealSolution,
                        )
                    }
                }

                // Show the tutorial on first launch.
                if (!settings.tutorialSeen) {
                    HowToPlaySheet(onDismiss = {
                        viewModel.repository.update { it.copy(tutorialSeen = true) }
                    })
                }
            }
        }
    }
}
