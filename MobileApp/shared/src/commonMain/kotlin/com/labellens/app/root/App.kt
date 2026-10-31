@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.labellens.app.root

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.labellens.app.data.repository.UserProfileRepository
import com.labellens.app.designsystem.AllComponentsGallery
import com.labellens.app.designsystem.theme.AppTheme
import com.labellens.app.presentation.navigation.AppNavigation
import com.labellens.app.util.UiMessage
import com.labellens.app.util.extensions.ObserveFlowAsEvent
import com.labellens.app.util.logging.AppLogger
import com.labellens.app.util.logging.logAppOpened
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.jetbrains.compose.resources.ComposeEnvironment
import org.jetbrains.compose.resources.DensityQualifier
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.LanguageQualifier
import org.jetbrains.compose.resources.LocalComposeEnvironment
import org.jetbrains.compose.resources.RegionQualifier
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.ThemeQualifier
import org.koin.compose.koinInject

/** Root composable — the single entry point every platform (Android/iOS/Desktop/Web) renders. */
@OptIn(InternalResourceApi::class)
@Composable
fun App() {
    val userProfileRepository = koinInject<UserProfileRepository>()
    val currentLanguage by userProfileRepository.languageFlow.collectAsState()

    val composeEnvironment = remember(currentLanguage) {
        object : ComposeEnvironment {
            @Composable
            override fun rememberEnvironment(): ResourceEnvironment {
                val density = LocalDensity.current
                val isDark = isSystemInDarkTheme()
                return remember(density, isDark) {
                    ResourceEnvironment(
                        language = LanguageQualifier(currentLanguage.code),
                        region = RegionQualifier(""),
                        theme = ThemeQualifier.selectByValue(isDark),
                        density = DensityQualifier.selectByDensity(density.density),
                    )
                }
            }
        }
    }

    CompositionLocalProvider(LocalComposeEnvironment provides composeEnvironment) {
        key(currentLanguage) {
            AppTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Set this to true for showing app, or false for ui components gallery
                    val isPreviewComponentsMode = false
                    if (isPreviewComponentsMode) {
                        AllComponentsGallery()
                    } else {
                        AppScaffold()
                    }
                }
            }
        }
    }
}

@Composable
private fun AppScaffold() {
    val snackbarHostState = remember { SnackbarHostState() }
    var uiMessage by remember { mutableStateOf<UiMessage?>(null) }

    ObserveFlowAsEvent(AppGlobalUiState.uiMessageFlow) { uiMessage = it }
    LaunchedEffect(Unit) {
        AppLogger.logAppOpened()
    }

    uiMessage?.value?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message = message)
            uiMessage = null
        }
    }
    Scaffold(
        containerColor = AppTheme.colors.background,
        contentColor = AppTheme.colors.text.primary,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) {
        AppNavigation()
    }
}

/**
 * App-wide bus for one-off UI messages (snackbars). Lets any layer surface a transient message
 * without holding a screen reference — [AppScaffold] collects [uiMessageFlow] and shows it.
 */
object AppGlobalUiState {
    private val uiMessageChannel = Channel<UiMessage>(Channel.BUFFERED)
    val uiMessageFlow = uiMessageChannel.receiveAsFlow()

    fun showUiMessage(uiMessage: UiMessage) {
        uiMessageChannel.trySend(uiMessage)
    }
}
