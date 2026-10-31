@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.labellens.app.root

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.labellens.app.generated.resources.Res
import com.labellens.app.generated.resources.bottom_nav_label_home
import org.jetbrains.compose.resources.ComposeEnvironment
import org.jetbrains.compose.resources.DensityQualifier
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.LanguageQualifier
import org.jetbrains.compose.resources.LocalComposeEnvironment
import org.jetbrains.compose.resources.RegionQualifier
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.ThemeQualifier
import org.jetbrains.compose.resources.stringResource
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(InternalResourceApi::class, ExperimentalTestApi::class)
class LocaleEnvironmentTest {
    @Test
    fun testVietnameseLocaleResolution() = runComposeUiTest {
        var resolvedText = ""
        setContent {
            val density = LocalDensity.current
            val isDark = isSystemInDarkTheme()
            val customEnv = remember(density, isDark) {
                object : ComposeEnvironment {
                    @Composable
                    override fun rememberEnvironment(): ResourceEnvironment = ResourceEnvironment(
                        language = LanguageQualifier("vi"),
                        region = RegionQualifier(""),
                        theme = ThemeQualifier.selectByValue(isDark),
                        density = DensityQualifier.selectByDensity(density.density),
                    )
                }
            }

            CompositionLocalProvider(LocalComposeEnvironment provides customEnv) {
                resolvedText = stringResource(Res.string.bottom_nav_label_home)
                Text(resolvedText)
            }
        }
        assertEquals("Trang chủ", resolvedText)
    }

    @Test
    fun testEnglishLocaleResolution() = runComposeUiTest {
        var resolvedText = ""
        setContent {
            val density = LocalDensity.current
            val isDark = isSystemInDarkTheme()
            val customEnv = remember(density, isDark) {
                object : ComposeEnvironment {
                    @Composable
                    override fun rememberEnvironment(): ResourceEnvironment = ResourceEnvironment(
                        language = LanguageQualifier("en"),
                        region = RegionQualifier(""),
                        theme = ThemeQualifier.selectByValue(isDark),
                        density = DensityQualifier.selectByDensity(density.density),
                    )
                }
            }

            CompositionLocalProvider(LocalComposeEnvironment provides customEnv) {
                resolvedText = stringResource(Res.string.bottom_nav_label_home)
                Text(resolvedText)
            }
        }
        assertEquals("Home", resolvedText)
    }
}
