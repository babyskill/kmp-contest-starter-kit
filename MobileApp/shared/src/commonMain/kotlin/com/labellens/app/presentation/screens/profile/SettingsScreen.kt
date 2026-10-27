package com.labellens.app.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.labellens.app.designsystem.theme.AppTheme

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
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Settings & Disclaimers",
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
                        text = "100% On-Device Privacy",
                        style = AppTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "All label analysis, OCR text recognition, and personal health profiles are computed entirely on your device. Your label photos and dietary settings never leave your phone.",
                        style = AppTheme.typography.bodySmall.copy(
                            color = AppTheme.colors.text.secondary,
                            lineHeight = 20.sp,
                        ),
                    )
                }
            }
        }

        // Language & Localization Note
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Multilingual Asian Label Support",
                        style = AppTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.text.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Our seed knowledge base incorporates Korean, Japanese, and Chinese ingredient names alongside English codex standards to seamlessly decode imported snacks, noodles, and teas.",
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
                            text = "Anonymous Diagnostic Telemetry",
                            style = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.text.primary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Record local performance metrics (OCR processing duration, match success rate). Stored on device only — no network telemetry.",
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
                        text = "Educational Notice & Health Guardrail",
                        style = AppTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB7791F),
                        ),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "LabelLens provides educational translation and structured breakdown of printed packaging text. It does not provide medical diagnoses, treatment advice, or drug-interaction verdicts.\n\nEducation only - this is not medical advice.",
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
