package com.labellens.app.designsystem.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * In case you don't know what colors to choose, choose any color from Material color palette,
 * then use material color shade generator, set 900 as primary, and 50 as alternative.
 *
 * For supporting colors, based on primary color HSB values, keep  S, and B values the same or
 * 5-10 difference, and for H value based on status set like green, yellow, red, blue
 *
 * @param primary: Primary color for the application
 * @param onPrimary: Text color on the primary color
 * @param alternative: Subtle color elements that is less important than the primary colored action
 * @param onAlternative: Text color on the subtle color
 * @param background: Background color for the application
 * @param surfaceContainer: Used for background color of dialog, bottomsheet
 * @param text: General text colors used in the application
 * @param textInput: Text input colors used in the application
 * @param bottomNav: Bottom navigation bar colors used in the application
 * @param outline: Outline colors used in the application. Ex: border colors, divider colors, inactive indicator colors, etc.
 * @param status: Status colors used in the application. Default values are usually okay for most cases.
 */
@Immutable
data class AppColors(
    val primary: Color = Color.Unspecified,
    val onPrimary: Color = Color.Unspecified,
    val alternative: Color = Color.Unspecified,
    val onAlternative: Color = Color.Unspecified,
    val background: Color = Color.Unspecified,
    val surfaceContainer: Color = Color.Unspecified,
    val text: Text = Text(),
    val status: Status = Status(),
    val textInput: TextInput = TextInput(),
    val bottomNav: BottomNav = BottomNav(),
    val outline: Color = Color.Unspecified,
) {
    // App is light-only; always returns light Material ColorScheme
    fun asMaterialColorScheme(@Suppress("UNUSED_PARAMETER") isDark: Boolean = false): androidx.compose.material3.ColorScheme = lightColorScheme(
        primary = primary,
        surfaceContainer = surfaceContainer,
        surfaceContainerHighest = surfaceContainer,
        surfaceContainerLow = surfaceContainer,
        onPrimary = onPrimary,
        error = status.error,
        errorContainer = status.errorContainer,
        background = background,
        onBackground = text.primary,
        outline = outline,
    )

    @Immutable
    data class Status(
        val error: Color = Color(0xFFF75555),
        val errorContainer: Color = Color(0xFFFFEFED),
        val warning: Color = Color(0xFFFACC15),
        val warningContainer: Color = Color(0xFFFFFBEB),
        val info: Color = Color(0xFF235DFF),
        val infoContainer: Color = Color(0xFF235DFF).copy(alpha = 0.08f),
        val success: Color = Color(0xFF16794C),
        val successContainer: Color = Color(0xFFEDF2ED),
        val amber: Color = Color(0xFFB7791F),
        val amberContainer: Color = Color(0xFFFBF3DF),
        val allergenRed: Color = Color(0xFFB3261E),
        val allergenContainer: Color = Color(0xFFFDECEA),
    )

    @Immutable
    data class TextInput(
        val textIcon: Color = Color.Unspecified,
        val background: Color = Color.Unspecified,
        val placeholder: Color = Color.Unspecified,
        val readOnlyBackground: Color = Color.Unspecified,
        val disabledTextIcon: Color = Color.Unspecified,
        val disabledBackground: Color = Color.Unspecified,
    ) {
        companion object {
            val DefaultLight =
                TextInput(
                    textIcon = Color.GRAY_900,
                    background = Color.GRAY_50,
                    placeholder = Color.GRAY_500,
                    readOnlyBackground = Color.GRAY_100,
                    disabledTextIcon = Color.GRAY_600,
                    disabledBackground = Color(0xFFD8D8D8),
                )
            val DefaultDark =
                TextInput(
                    textIcon = Color.White,
                    background = Color.BLACK_3_HEX_1F,
                    placeholder = Color.GRAY_500,
                    readOnlyBackground = Color.BLACK_2_HEX_1E,
                    disabledBackground = Color(0xFF23252B),
                    disabledTextIcon = Color.GRAY_600,
                )
        }
    }

    /**
     * @param primary: Primary text color, used for important text, such as title
     * @param secondary: Secondary text color, used for less important text, such as description
     */
    @Immutable
    data class Text(
        val primary: Color = Color.Unspecified,
        val secondary: Color = Color.Unspecified,
    ) {
        companion object {
            val DefaultLight =
                Text(
                    primary = Color(0xFF14201A),
                    secondary = Color(0xFF3D4A42),
                )
            val DefaultDark =
                Text(
                    primary = Color.White,
                    secondary = Color.GRAY_200,
                )
        }
    }

    /**
     * @param background: Background color for the bottom navigation bar
     * @param selectedTextIcon: Text color for the selected item
     * @param unselectedTextIcon: Text color for the unselected item
     */
    @Immutable
    data class BottomNav(
        val background: Color = Color.Unspecified,
        val selectedTextIcon: Color = Color.Unspecified,
        val unselectedTextIcon: Color = Color.Unspecified,
    ) {
        companion object {
            val DefaultLight =
                BottomNav(
                    background = Color.White,
                    selectedTextIcon = PrimaryColor,
                    unselectedTextIcon = Color.GRAY_500,
                )
            val DefaultDark =
                BottomNav(
                    background = Color.BLACK_1_HEX_18,
                    selectedTextIcon = Color.White,
                    unselectedTextIcon = Color.GRAY_500,
                )
        }
    }
}

val PrimaryColor = Color(0xFF16794C)
val PrimaryAlpha8Color = Color(0xFFEDF2ED)

val lightModeAppColors =
    AppColors(
        primary = PrimaryColor,
        onPrimary = Color.White,
        alternative = PrimaryAlpha8Color,
        onAlternative = PrimaryColor,
        background = Color(0xFFF7F8F5),
        surfaceContainer = Color(0xFFFFFFFF),
        text = AppColors.Text.DefaultLight,
        textInput =
        AppColors.TextInput.DefaultLight.copy(
            background = Color(0xFFEDF2ED),
            readOnlyBackground = Color(0xFFE2E8E2),
            disabledBackground = Color(0xFFE2E8E2),
        ),
        bottomNav =
        AppColors.BottomNav.DefaultLight.copy(
            background = Color(0xFFFFFFFF),
            selectedTextIcon = PrimaryColor,
            unselectedTextIcon = Color(0xFF3D4A42).copy(alpha = 0.6f),
        ),
        outline = Color(0xFFE2E8E2),
    )

val darkModeAppColors =
    AppColors(
        primary = Color(0xFF2E9A68),
        onPrimary = Color.White,
        alternative = Color(0xFF1B3D2B),
        onAlternative = Color(0xFFEDF2ED),
        background = Color(0xFF111714),
        surfaceContainer = Color(0xFF1A241F),
        text = AppColors.Text.DefaultDark,
        textInput =
        AppColors.TextInput.DefaultDark.copy(
            background = Color(0xFF1E2A24),
            readOnlyBackground = Color(0xFF26352E),
            disabledBackground = Color(0xFF2F4239),
        ),
        bottomNav =
        AppColors.BottomNav.DefaultDark.copy(
            background = Color(0xFF111714),
            selectedTextIcon = Color(0xFF2E9A68),
        ),
        outline = Color(0xFF2F4239),
    )
