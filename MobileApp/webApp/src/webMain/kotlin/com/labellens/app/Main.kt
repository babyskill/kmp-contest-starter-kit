package com.labellens.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.labellens.app.root.App
import com.labellens.app.root.AppInitializer

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    AppInitializer.initialize {}
    ComposeViewport {
        App()
    }
}
