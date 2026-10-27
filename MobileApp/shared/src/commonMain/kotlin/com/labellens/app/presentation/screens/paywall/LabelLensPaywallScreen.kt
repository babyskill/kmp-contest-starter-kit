package com.labellens.app.presentation.screens.paywall

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.labellens.app.designsystem.components.AppButton
import com.labellens.app.designsystem.components.ButtonStyle
import com.labellens.app.designsystem.theme.AppTheme
import com.labellens.app.presentation.navigation.PaywallScreenRoute

@Composable
fun LabelLensPaywallScreen(
    route: PaywallScreenRoute,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFBF3DF))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "Demo Mode",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB7791F),
                        style = AppTheme.typography.bodySmall,
                    )
                }
            }
        }

        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "LabelLens Plus",
                    style = AppTheme.typography.headlineLarge.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.text.primary,
                    ),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Safety warnings are always 100% free.\nUpgrade for power features.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.text.secondary,
                )
            }
        }

        // Free tier transparency
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Free Tier (Always Free)",
                        style = AppTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PlanFeatureRow("Unlimited barcode scans & lookups")
                    PlanFeatureRow("5 full photo analyses per month")
                    PlanFeatureRow("All allergen & dietary safety warnings always active")
                }
            }
        }

        // Plain Pricing Cards
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF2ED)),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Plus Annual",
                            style = AppTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.text.primary,
                            ),
                        )
                        Text(
                            text = "$24.99 / year",
                            style = AppTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.primary,
                            ),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Unlimited photo OCR analyses, unlimited product comparisons, priority offline knowledge base.",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AppButton(
                        text = "Upgrade to Plus ($24.99/year)",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateBack,
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Family Plan",
                            style = AppTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.text.primary,
                            ),
                        )
                        Text(
                            text = "$39.99 / year",
                            style = AppTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB7791F),
                            ),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Coverage for up to 5 family members with individual allergy and dietary preference profiles.",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AppButton(
                        text = "Select Family ($39.99/year)",
                        style = ButtonStyle.ALTERNATIVE,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateBack,
                    )
                }
            }
        }

        // Restore purchases entry
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Restore Purchases",
                    style = AppTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.primary,
                    ),
                    modifier = Modifier.padding(8.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "This is a demonstration build. No real credit cards or in-app payment transactions are triggered.",
                    style = AppTheme.typography.bodyExtraSmall,
                    color = AppTheme.colors.text.secondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun PlanFeatureRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = AppTheme.colors.primary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, style = AppTheme.typography.bodySmall, color = AppTheme.colors.text.primary)
    }
}
