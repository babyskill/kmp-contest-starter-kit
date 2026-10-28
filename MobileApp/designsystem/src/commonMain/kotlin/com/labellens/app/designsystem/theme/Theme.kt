package com.labellens.app.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

val LocalThemeIsDark = compositionLocalOf { false }
val LocalAppColors = staticCompositionLocalOf { lightModeAppColors }
val LocalAppTypography =
    staticCompositionLocalOf<AppTypography> { error("Typography not provided") }
val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }

@Composable
fun AppTheme(
    @Suppress("UNUSED_PARAMETER") isDarkMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    // App design is light-only; force light theme app-wide regardless of system setting
    CompositionLocalProvider(
        LocalThemeIsDark provides false,
        LocalAppColors provides lightModeAppColors,
        LocalAppTypography provides appTypography,
        LocalAppSpacing provides appSpacing,
    ) {
        SystemAppearance(isDark = false)
        MaterialTheme(
            colorScheme = LocalAppColors.current.asMaterialColorScheme(isDark = false),
            typography = MaterialThemAppTypography,
            content = {
                Surface(
                    content = content,
                    color = AppTheme.colors.background,
                )
            },
        )
    }
}

object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current

    val typography: AppTypography
        @Composable @ReadOnlyComposable
        get() = LocalAppTypography.current

    val spacing: AppSpacing
        @Composable @ReadOnlyComposable
        get() = LocalAppSpacing.current
}

@Composable
internal expect fun SystemAppearance(isDark: Boolean)
