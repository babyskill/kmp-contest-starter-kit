package com.labellens.app.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.labellens.app.designsystem.theme.AppTheme
import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.generated.resources.Res
import com.labellens.app.generated.resources.btn_back
import com.labellens.app.generated.resources.settings_asian_support_description
import com.labellens.app.generated.resources.settings_asian_support_title
import com.labellens.app.generated.resources.settings_disclaimer_description
import com.labellens.app.generated.resources.settings_disclaimer_title
import com.labellens.app.generated.resources.settings_language_en
import com.labellens.app.generated.resources.settings_language_subtitle
import com.labellens.app.generated.resources.settings_language_title
import com.labellens.app.generated.resources.settings_language_vi
import com.labellens.app.generated.resources.settings_privacy_description
import com.labellens.app.generated.resources.settings_privacy_title
import com.labellens.app.generated.resources.settings_screen_title
import com.labellens.app.generated.resources.settings_telemetry_description
import com.labellens.app.generated.resources.settings_telemetry_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    viewModel: LabelLensProfileViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 60.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(Res.string.btn_back),
                        tint = AppTheme.colors.text.primary,
                    )
                }
                Text(
                    text = stringResource(Res.string.settings_screen_title),
                    style = AppTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.text.primary,
                    ),
                )
            }
        }

        // Privacy note (100% on-device)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_privacy_title),
                        style = AppTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(Res.string.settings_privacy_description),
                        style = AppTheme.typography.bodySmall.copy(
                            color = AppTheme.colors.text.secondary,
                            lineHeight = 20.sp,
                        ),
                    )
                }
            }
        }

        // App Language Selection & Localization Note
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_language_title),
                        style = AppTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.text.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.settings_language_subtitle),
                        style = AppTheme.typography.bodySmall.copy(
                            color = AppTheme.colors.text.secondary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        LanguageOptionPill(
                            label = stringResource(Res.string.settings_language_vi),
                            isSelected = state.selectedLanguage == AppLanguage.VIETNAMESE,
                            onClick = { viewModel.selectLanguage(AppLanguage.VIETNAMESE) },
                            modifier = Modifier.weight(1f),
                        )
                        LanguageOptionPill(
                            label = stringResource(Res.string.settings_language_en),
                            isSelected = state.selectedLanguage == AppLanguage.ENGLISH,
                            onClick = { viewModel.selectLanguage(AppLanguage.ENGLISH) },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(Res.string.settings_asian_support_title),
                        style = AppTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.text.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.settings_asian_support_description),
                        style = AppTheme.typography.bodySmall.copy(
                            color = AppTheme.colors.text.secondary,
                            lineHeight = 20.sp,
                        ),
                    )
                }
            }
        }

        // Telemetry Consent Toggle (default off)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.settings_telemetry_title),
                            style = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.text.primary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(Res.string.settings_telemetry_description),
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.text.secondary,
                        )
                    }
                    Switch(
                        checked = state.anonymousConsent,
                        onCheckedChange = { viewModel.toggleConsent(it) },
                    )
                }
            }
        }

        // Hard Guardrail Disclaimer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF3DF)),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_disclaimer_title),
                        style = AppTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB7791F),
                        ),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(Res.string.settings_disclaimer_description),
                        style = AppTheme.typography.bodySmall.copy(
                            color = AppTheme.colors.text.primary,
                            lineHeight = 20.sp,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageOptionPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeBorderColor = AppTheme.colors.primary
    val activeBgColor = Color(0xFFEDF2ED)
    val inactiveBgColor = Color(0xFFF7F8F5)
    val inactiveBorderColor = Color(0xFFE2E4E0)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) activeBgColor else inactiveBgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) activeBorderColor else inactiveBorderColor,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = activeBorderColor,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = AppTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) activeBorderColor else AppTheme.colors.text.primary,
                ),
            )
        }
    }
}
