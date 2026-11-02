package com.labellens.app.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.labellens.app.core.navigation.DeepLinkManager
import com.labellens.app.data.repository.UserRepository
import com.labellens.app.designsystem.components.bottomnav.BottomNavItem
import com.labellens.app.designsystem.components.bottomnav.BottomNavigationBar
import com.labellens.app.designsystem.generated.resources.UiRes
import com.labellens.app.designsystem.generated.resources.ic_compare_scale
import com.labellens.app.designsystem.generated.resources.ic_history
import com.labellens.app.designsystem.generated.resources.ic_home
import com.labellens.app.designsystem.generated.resources.ic_profile
import com.labellens.app.designsystem.generated.resources.ic_scan_viewfinder
import com.labellens.app.generated.resources.Res
import com.labellens.app.generated.resources.bottom_nav_label_compare
import com.labellens.app.generated.resources.bottom_nav_label_history
import com.labellens.app.generated.resources.bottom_nav_label_home
import com.labellens.app.generated.resources.bottom_nav_label_profile
import com.labellens.app.generated.resources.bottom_nav_label_scan
import com.labellens.app.presentation.screens.compare.CompareScreen
import com.labellens.app.presentation.screens.compare.CompareViewModel
import com.labellens.app.presentation.screens.debug.DebugMenuScreen
import com.labellens.app.presentation.screens.debug.DebugMenuViewModel
import com.labellens.app.presentation.screens.history.HistoryScreen
import com.labellens.app.presentation.screens.history.HistoryViewModel
import com.labellens.app.presentation.screens.home.HomeScreen
import com.labellens.app.presentation.screens.home.HomeViewModel
import com.labellens.app.presentation.screens.ingredient.IngredientDetailScreen
import com.labellens.app.presentation.screens.onboarding.LabelLensOnBoardingScreen
import com.labellens.app.presentation.screens.paywall.LabelLensPaywallScreen
import com.labellens.app.presentation.screens.profile.LabelLensProfileViewModel
import com.labellens.app.presentation.screens.profile.ProfileScreen
import com.labellens.app.presentation.screens.profile.SettingsScreen
import com.labellens.app.presentation.screens.result.ResultScreen
import com.labellens.app.presentation.screens.result.ResultViewModel
import com.labellens.app.presentation.screens.scan.ScanProcessingCard
import com.labellens.app.presentation.screens.scan.ScanScreen
import com.labellens.app.presentation.screens.scan.ScanViewModel
import com.labellens.app.util.analytics.Analytics
import com.labellens.app.util.analytics.logScreenView
import com.labellens.app.util.extensions.isKeyboardOpen
import com.labellens.app.util.file.isCameraCaptureSupported
import com.labellens.app.util.file.openCameraPicker
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

val LocalNavigator = compositionLocalOf<Navigator> { error("No Navigator provided") }

private val noAnimationTransition = EnterTransition.None togetherWith ExitTransition.None
private val noAnimationMetadata =
    NavDisplay.transitionSpec { noAnimationTransition } +
        NavDisplay.popTransitionSpec { noAnimationTransition } +
        NavDisplay.predictivePopTransitionSpec { noAnimationTransition }

private val TOP_LEVEL_ROUTES: Map<TopLevelScreenRoute, BottomNavItem> = linkedMapOf(
    HomeScreenRoute to BottomNavItem(
        label = Res.string.bottom_nav_label_home,
        icon = UiRes.drawable.ic_home,
    ),
    HistoryScreenRoute to BottomNavItem(
        label = Res.string.bottom_nav_label_history,
        icon = UiRes.drawable.ic_history,
    ),
    ScanTabScreenRoute to BottomNavItem(
        label = Res.string.bottom_nav_label_scan,
        icon = UiRes.drawable.ic_scan_viewfinder,
    ),
    CompareScreenRoute to BottomNavItem(
        label = Res.string.bottom_nav_label_compare,
        icon = UiRes.drawable.ic_compare_scale,
    ),
    ProfileScreenRoute to BottomNavItem(
        label = Res.string.bottom_nav_label_profile,
        icon = UiRes.drawable.ic_profile,
    ),
)

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
) {
    val navigationState = rememberNavigationState(
        startRoute = OnBoardingScreenRoute,
        topLevelRoutes = TOP_LEVEL_ROUTES.keys,
        primaryTopLevelRoute = HomeScreenRoute,
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val scanViewModel = koinViewModel<ScanViewModel>()
    val scanUiState by scanViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val entryProvider = remember(navigator, scanViewModel) { entryProvider { screens(navigator, scanViewModel) } }

    val isAtRoot = navigationState.currentBackstack.size <= 1
    val activeTopLevel = navigationState.topLevelRoute
    val selectedTopLevelIndex = TOP_LEVEL_ROUTES.keys.indexOf(activeTopLevel).coerceAtLeast(0)
    val bottomNavItems = remember { TOP_LEVEL_ROUTES.values.toList() }
    val isBottomNavVisible = isAtRoot && activeTopLevel != null && !isKeyboardOpen()

    val analytics = koinInject<Analytics>()
    val deepLinkManager = koinInject<DeepLinkManager>()
    val currentRoute = navigationState.currentBackstack.lastOrNull()
    LaunchedEffect(currentRoute) {
        if (currentRoute != null) {
            val screenName = currentRoute::class.simpleName ?: currentRoute.toString()
            analytics.logScreenView(screenName)
        }
    }

    LaunchedEffect(navigator) {
        deepLinkManager.consumePendingEvent()?.let { event ->
            navigator.navigate(event.route)
        }
        deepLinkManager.events.collect { event ->
            navigator.navigate(event.route)
        }
    }

    CompositionLocalProvider(LocalNavigator provides navigator) {
        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    NavDisplay(
                        entries = navigationState.toDecoratedEntries(entryProvider),
                        onBack = { navigator.goBack() },
                    )
                }

                AnimatedVisibility(
                    visible = isBottomNavVisible,
                    enter = fadeIn(animationSpec = snap()),
                    exit = fadeOut(animationSpec = snap()),
                ) {
                    BottomNavigationBar(
                        modifier = Modifier.fillMaxWidth(),
                        items = bottomNavItems,
                        selectedIndex = selectedTopLevelIndex,
                        onClickItem = { item ->
                            val targetRoute = TOP_LEVEL_ROUTES.entries
                                .firstOrNull { it.value === item }?.key ?: return@BottomNavigationBar
                            if (targetRoute == ScanTabScreenRoute && isCameraCaptureSupported()) {
                                coroutineScope.launch {
                                    val captured = FileKit.openCameraPicker()
                                    if (captured != null) {
                                        scanViewModel.processImageSource(captured) { raw, name, barcode, sourceName ->
                                            navigator.navigate(
                                                ResultScreenRoute(
                                                    rawText = raw,
                                                    productName = name,
                                                    barcode = barcode,
                                                    sampleId = null,
                                                    sourceName = sourceName,
                                                ),
                                            )
                                        }
                                    }
                                }
                            } else {
                                navigator.navigate(targetRoute)
                            }
                        },
                    )
                }
            }

            if (scanUiState.isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center,
                ) {
                    ScanProcessingCard(
                        state = scanUiState,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }
        }
    }
}

private fun EntryProviderScope<ScreenRoute>.screens(
    navigator: Navigator,
    scanViewModel: ScanViewModel,
) {
    entry<HomeScreenRoute>(metadata = noAnimationMetadata) {
        val viewModel = koinViewModel<HomeViewModel>()
        HomeScreen(
            viewModel = viewModel,
            onNavigateToScan = { navigator.navigate(ScanScreenRoute) },
            onNavigateToResult = { sampleId, rawText, name ->
                navigator.navigate(
                    ResultScreenRoute(sampleId = sampleId, rawText = rawText, productName = name),
                )
            },
            onNavigateToHistoryItem = { result ->
                navigator.navigate(
                    ResultScreenRoute(
                        scanId = result.id ?: result.barcode ?: result.productName,
                        productName = result.productName,
                        rawText = result.rawInputText,
                    ),
                )
            },
        )
    }

    entry<HistoryScreenRoute>(metadata = noAnimationMetadata) {
        val viewModel = koinViewModel<HistoryViewModel>()
        HistoryScreen(
            viewModel = viewModel,
            onNavigateToResult = { scan ->
                navigator.navigate(
                    ResultScreenRoute(
                        scanId = scan.id ?: scan.barcode ?: scan.productName,
                        productName = scan.productName,
                        rawText = scan.rawInputText,
                    ),
                )
            },
        )
    }

    entry<ScanTabScreenRoute>(metadata = noAnimationMetadata) {
        ScanScreen(
            viewModel = scanViewModel,
            onNavigateBack = { navigator.switchToTab(HomeScreenRoute) },
            onNavigateToResult = { rawText, name, barcode, sampleId, sourceName ->
                navigator.navigate(
                    ResultScreenRoute(
                        rawText = rawText,
                        productName = name,
                        barcode = barcode,
                        sampleId = sampleId,
                        sourceName = sourceName,
                    ),
                )
            },
        )
    }

    entry<CompareScreenRoute>(metadata = noAnimationMetadata) {
        val viewModel = koinViewModel<CompareViewModel>()
        CompareScreen(viewModel = viewModel)
    }

    entry<ProfileScreenRoute>(metadata = noAnimationMetadata) {
        val viewModel = koinViewModel<LabelLensProfileViewModel>()
        ProfileScreen(
            viewModel = viewModel,
            onNavigateToPaywall = { navigator.navigate(PaywallScreenRoute()) },
            onNavigateToSettings = { navigator.navigate(SettingsScreenRoute) },
        )
    }

    entry<OnBoardingScreenRoute> {
        LabelLensOnBoardingScreen(
            onFinished = {
                navigator.set(HomeScreenRoute)
            },
            onTryDemoScan = { sample ->
                navigator.set(HomeScreenRoute)
                navigator.navigate(
                    ResultScreenRoute(
                        sampleId = sample.id,
                        productName = sample.name,
                        rawText = sample.fullLabelText,
                    ),
                )
            },
        )
    }

    entry<ScanScreenRoute> {
        ScanScreen(
            viewModel = scanViewModel,
            onNavigateBack = { navigator.goBack() },
            onNavigateToResult = { rawText, name, barcode, sampleId, sourceName ->
                navigator.navigate(
                    ResultScreenRoute(
                        rawText = rawText,
                        productName = name,
                        barcode = barcode,
                        sampleId = sampleId,
                        sourceName = sourceName,
                    ),
                )
            },
        )
    }

    entry<ResultScreenRoute> { key ->
        val viewModel = koinViewModel<ResultViewModel>()
        ResultScreen(
            route = key,
            viewModel = viewModel,
            onNavigateBack = { navigator.goBack() },
            onNavigateToIngredientDetail = { name ->
                navigator.navigate(IngredientDetailScreenRoute(canonicalName = name))
            },
            onNavigateToCompare = {
                navigator.switchToTab(CompareScreenRoute)
            },
        )
    }

    entry<IngredientDetailScreenRoute> { key ->
        IngredientDetailScreen(
            canonicalName = key.canonicalName,
            onNavigateBack = { navigator.goBack() },
        )
    }

    entry<PaywallScreenRoute> { key ->
        LabelLensPaywallScreen(
            route = key,
            onNavigateBack = { navigator.goBack() },
        )
    }

    entry<SettingsScreenRoute> {
        val viewModel = koinViewModel<LabelLensProfileViewModel>()
        SettingsScreen(
            viewModel = viewModel,
            onNavigateBack = { navigator.goBack() },
        )
    }

    entry<DebugMenuScreenRoute> {
        val viewModel = koinViewModel<DebugMenuViewModel>()
        DebugMenuScreen(
            viewModel = viewModel,
            onNavigateBack = { navigator.goBack() },
        )
    }
}
