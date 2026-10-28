package com.labellens.app.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.UIKit.UIApplication
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.setStatusBarStyle

@Composable
internal actual fun SystemAppearance(@Suppress("UNUSED_PARAMETER") isDark: Boolean) {
    LaunchedEffect(Unit) {
        // App is light-only: status bar must use dark icons/content
        UIApplication.sharedApplication.setStatusBarStyle(
            UIStatusBarStyleDarkContent,
        )
    }
}
