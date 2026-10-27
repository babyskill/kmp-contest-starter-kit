package com.labellens.app.presentation.screens.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.labellens.app.designsystem.components.AppButton
import com.labellens.app.designsystem.components.ButtonStyle
import com.labellens.app.designsystem.theme.AppTheme
import com.labellens.app.domain.model.BuiltInSamples
import com.labellens.app.domain.model.SampleProduct

@Composable
fun LabelLensOnBoardingScreen(
    onFinished: () -> Unit,
    onTryDemoScan: (sample: SampleProduct) -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentStep by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top bar with skip button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onFinished) {
                Text(
                    text = "Skip",
                    style = AppTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.text.secondary,
                    ),
                )
            }
        }

        // Center Content Card
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (currentStep) {
                0 -> {
                    OnboardingCard(
                        title = "Understand the label.\nIn seconds.",
                        description = "Photograph any food or supplement label, including imported Asian packaging, and get an honest, plain-language explanation.",
                        badgeText = "Honest Breakdown",
                        showDemoButton = true,
                        onDemoClick = {
                            onTryDemoScan(BuiltInSamples.sample1)
                        },
                    )
                }

                1 -> {
                    OnboardingCard(
                        title = "Personal context,\nnot a scary single-score.",
                        description = "We never label foods 'toxic' or 'dirty'. We highlight wholesome ingredients, note additives plainly, and alert you to your allergens.",
                        badgeText = "Calm & Science-Backed",
                        showDemoButton = false,
                    )
                }

                2 -> {
                    OnboardingCard(
                        title = "100% on-device.\nYour health stays private.",
                        description = "Everything runs locally on your phone. No logins required, no tracking profiles sold, and all warnings remain completely free.",
                        badgeText = "Private & Free Safety",
                        showDemoButton = false,
                    )
                }
            }
        }

        // Bottom progress & navigation
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Dots indicator
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (currentStep == index) 24.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentStep == index) AppTheme.colors.primary else AppTheme.colors.outline,
                            ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (currentStep < 2) {
                AppButton(
                    text = "Next",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { currentStep++ },
                )
            } else {
                AppButton(
                    text = "Get Started",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onFinished,
                )
            }
        }
    }
}

@Composable
private fun OnboardingCard(
    title: String,
    description: String,
    badgeText: String,
    showDemoButton: Boolean,
    onDemoClick: () -> Unit = {},
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEDF2ED))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = badgeText,
                    style = AppTheme.typography.bodyExtraSmall.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.colors.primary,
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = title,
                textAlign = TextAlign.Center,
                style = AppTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.text.primary,
                    lineHeight = 32.sp,
                ),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = description,
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyMedium.copy(
                    color = AppTheme.colors.text.secondary,
                    lineHeight = 22.sp,
                ),
            )

            if (showDemoButton) {
                Spacer(modifier = Modifier.height(24.dp))
                AppButton(
                    text = "Try a demo scan",
                    style = ButtonStyle.ALTERNATIVE,
                    onClick = onDemoClick,
                )
            }
        }
    }
}
