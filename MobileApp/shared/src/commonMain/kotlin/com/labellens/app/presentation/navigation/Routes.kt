package com.labellens.app.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Every navigation destination in the app.
 *
 * `@SerialName` values are the wire format on disk — keep them stable when
 * renaming routes.
 */
@Serializable
sealed interface ScreenRoute : NavKey

/** Marker for routes that root a top-level back stack (a tab in the bottom nav). */
@Serializable
sealed interface TopLevelScreenRoute : ScreenRoute

@Serializable
@SerialName("Home")
data object HomeScreenRoute : TopLevelScreenRoute

@Serializable
@SerialName("History")
data object HistoryScreenRoute : TopLevelScreenRoute

@Serializable
@SerialName("ScanTab")
data object ScanTabScreenRoute : TopLevelScreenRoute

@Serializable
@SerialName("Compare")
data object CompareScreenRoute : TopLevelScreenRoute

@Serializable
@SerialName("Profile")
data object ProfileScreenRoute : TopLevelScreenRoute

// Legacy top-level aliases retained for deep-link compatibility
@Serializable
@SerialName("Gallery")
data object GalleryScreenRoute : TopLevelScreenRoute

@Serializable
@SerialName("Account")
data object AccountScreenRoute : TopLevelScreenRoute

// Push screens
@Serializable
@SerialName("OnBoarding")
data object OnBoardingScreenRoute : ScreenRoute

@Serializable
@SerialName("Scan")
data object ScanScreenRoute : ScreenRoute

@Serializable
@SerialName("Camera")
data object CameraScreenRoute : ScreenRoute

@Serializable
@SerialName("Result")
data class ResultScreenRoute(
    val scanId: String? = null,
    val sampleId: String? = null,
    val rawText: String? = null,
    val productName: String? = null,
    val barcode: String? = null,
    val sourceName: String? = null,
) : ScreenRoute

@Serializable
@SerialName("IngredientDetail")
data class IngredientDetailScreenRoute(
    val canonicalName: String,
) : ScreenRoute

@Serializable
@SerialName("Paywall")
data class PaywallScreenRoute(
    val placementId: String? = null,
    val userGoal: String? = null,
    val userBarrier: String? = null,
    val userDailyMinutes: Int? = null,
) : ScreenRoute

@Serializable
@SerialName("Settings")
data object SettingsScreenRoute : ScreenRoute

@Serializable
@SerialName("SignIn")
data class SignInScreenRoute(val isSignIn: Boolean = false) : ScreenRoute

@Serializable
@SerialName("Subscriptions")
data object SubscriptionsScreenRoute : ScreenRoute

@Serializable
@SerialName("HelpAndSupport")
data object HelpAndSupportScreenRoute : ScreenRoute

@Serializable
@SerialName("CreditBalance")
data object CreditBalanceScreenRoute : ScreenRoute

@Serializable
@SerialName("GenerationResult")
data class GenerationResultScreenRoute(val id: String) : ScreenRoute

@Serializable
@SerialName("DebugMenu")
data class DebugMenuScreenRoute(val from: String = "settings") : ScreenRoute
