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
import com.labellens.app.generated.resources.Res
import com.labellens.app.generated.resources.content_desc_close
import com.labellens.app.generated.resources.paywall_demo_disclaimer
import com.labellens.app.generated.resources.paywall_demo_mode
import com.labellens.app.generated.resources.paywall_family_plan
import com.labellens.app.generated.resources.paywall_family_plan_btn
import com.labellens.app.generated.resources.paywall_family_plan_desc
import com.labellens.app.generated.resources.paywall_family_plan_price
import com.labellens.app.generated.resources.paywall_feature_barcode
import com.labellens.app.generated.resources.paywall_feature_photo
import com.labellens.app.generated.resources.paywall_feature_warnings
import com.labellens.app.generated.resources.paywall_free_tier
import com.labellens.app.generated.resources.paywall_plus_annual
import com.labellens.app.generated.resources.paywall_plus_annual_btn
import com.labellens.app.generated.resources.paywall_plus_annual_desc
import com.labellens.app.generated.resources.paywall_plus_annual_price
import com.labellens.app.generated.resources.paywall_restore_purchases_btn
import com.labellens.app.generated.resources.paywall_safety_free
import com.labellens.app.presentation.navigation.PaywallScreenRoute
import org.jetbrains.compose.resources.stringResource

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
                    Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(Res.string.content_desc_close))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFBF3DF))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.paywall_demo_mode),
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
                    text = stringResource(Res.string.paywall_safety_free),
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
                        text = stringResource(Res.string.paywall_free_tier),
                        style = AppTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PlanFeatureRow(stringResource(Res.string.paywall_feature_barcode))
                    PlanFeatureRow(stringResource(Res.string.paywall_feature_photo))
                    PlanFeatureRow(stringResource(Res.string.paywall_feature_warnings))
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
                            text = stringResource(Res.string.paywall_plus_annual),
                            style = AppTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.text.primary,
                            ),
                        )
                        Text(
                            text = stringResource(Res.string.paywall_plus_annual_price),
                            style = AppTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.primary,
                            ),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.paywall_plus_annual_desc),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AppButton(
                        text = stringResource(Res.string.paywall_plus_annual_btn),
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
                            text = stringResource(Res.string.paywall_family_plan),
                            style = AppTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.text.primary,
                            ),
                        )
                        Text(
                            text = stringResource(Res.string.paywall_family_plan_price),
                            style = AppTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB7791F),
                            ),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.paywall_family_plan_desc),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AppButton(
                        text = stringResource(Res.string.paywall_family_plan_btn),
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
                    text = stringResource(Res.string.paywall_restore_purchases_btn),
                    style = AppTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.primary,
                    ),
                    modifier = Modifier.padding(8.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.paywall_demo_disclaimer),
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
