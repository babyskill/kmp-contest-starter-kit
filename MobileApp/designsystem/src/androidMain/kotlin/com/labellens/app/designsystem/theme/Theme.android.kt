package com.labellens.app.designsystem.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

@Composable
internal actual fun SystemAppearance(@Suppress("UNUSED_PARAMETER") isDark: Boolean) {
    val view = LocalView.current
    val backgroundColor = AppTheme.colors.background
    LaunchedEffect(Unit) {
        val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
        WindowInsetsControllerCompat(window, window.decorView).apply {
            // Light appearance for system bars -> light status/navigation bars with dark icons
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        // Explicitly set navigation bar and status bar color to match the light theme background
        window.navigationBarColor = backgroundColor.toArgb()
        window.statusBarColor = backgroundColor.toArgb()
    }
}
