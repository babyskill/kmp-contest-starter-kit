package com.labellens.app.presentation.screens.compare

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.labellens.app.data.repository.UserProfileRepository
import com.labellens.app.designsystem.theme.AppTheme
import com.labellens.app.domain.analyzer.AnalysisStrings
import com.labellens.app.domain.analyzer.ProductNameSanitizer
import com.labellens.app.domain.analyzer.ScanSummaryLocalizer
import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.generated.resources.Res
import com.labellens.app.generated.resources.compare_available_products
import com.labellens.app.generated.resources.compare_badge_product_a
import com.labellens.app.generated.resources.compare_badge_product_b
import com.labellens.app.generated.resources.compare_flagged_items_title
import com.labellens.app.generated.resources.compare_no_flags
import com.labellens.app.generated.resources.compare_nutrition_metrics_title
import com.labellens.app.generated.resources.compare_row_calories
import com.labellens.app.generated.resources.compare_row_dietary_fiber
import com.labellens.app.generated.resources.compare_row_protein
import com.labellens.app.generated.resources.compare_row_saturated_fat
import com.labellens.app.generated.resources.compare_row_sodium
import com.labellens.app.generated.resources.compare_row_sugars
import com.labellens.app.generated.resources.compare_subtitle
import com.labellens.app.generated.resources.compare_title
import com.labellens.app.generated.resources.compare_verdicts_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun CompareScreen(
    viewModel: CompareViewModel,
    modifier: Modifier = Modifier,
) {
    val userProfileRepository = koinInject<UserProfileRepository>()
    val language by userProfileRepository.languageFlow.collectAsState()
    val state by viewModel.uiState.collectAsState()
    val itemA = state.firstItem
    val itemB = state.secondItem

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Text(
                text = stringResource(Res.string.compare_title),
                style = AppTheme.typography.headlineLarge.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.text.primary,
                ),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.compare_subtitle),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
            )
        }

        // Product Selector Carousel
        item {
            Text(
                text = stringResource(Res.string.compare_available_products),
                style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AppTheme.colors.text.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.availableItems) { item ->
                    val isA = item.productName == itemA?.productName
                    val isB = item.productName == itemB?.productName
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isA -> AppTheme.colors.primary
                                    isB -> Color(0xFFB7791F)
                                    else -> Color.White
                                },
                            )
                            .clickable {
                                if (!isA && !isB) {
                                    viewModel.selectSecond(item)
                                } else if (isA) {
                                    // already A
                                } else {
                                    viewModel.selectFirst(item)
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Text(
                            text = ProductNameSanitizer.displayProductName(item.productName, language),
                            style = AppTheme.typography.bodySmall.copy(
                                fontWeight = if (isA || isB) FontWeight.Bold else FontWeight.Medium,
                                color = if (isA || isB) Color.White else AppTheme.colors.text.primary,
                            ),
                        )
                    }
                }
            }
        }

        if (itemA != null && itemB != null) {
            // Side-by-side header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ProductHeaderCard(item = itemA, language = language, badge = stringResource(Res.string.compare_badge_product_a), badgeColor = AppTheme.colors.primary, modifier = Modifier.weight(1f))
                    ProductHeaderCard(item = itemB, language = language, badge = stringResource(Res.string.compare_badge_product_b), badgeColor = Color(0xFFB7791F), modifier = Modifier.weight(1f))
                }
            }

            // Verdict Comparison
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(Res.string.compare_verdicts_title), style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(ScanSummaryLocalizer.localizedSummary(itemA, language), style = AppTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Text(ScanSummaryLocalizer.localizedSummary(itemB, language), style = AppTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Nutrition Highlights Side-by-Side
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(Res.string.compare_nutrition_metrics_title), style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(12.dp))
                        val metrics = listOf("Calories", "Sugars", "Sodium", "Saturated Fat", "Protein", "Dietary Fiber")
                        metrics.forEach { metric ->
                            val metricVi = AnalysisStrings.nutritionName(AppLanguage.VIETNAMESE, metric)
                            val localizedMetricName = when (metric) {
                                "Calories" -> stringResource(Res.string.compare_row_calories)
                                "Sugars" -> stringResource(Res.string.compare_row_sugars)
                                "Sodium" -> stringResource(Res.string.compare_row_sodium)
                                "Saturated Fat" -> stringResource(Res.string.compare_row_saturated_fat)
                                "Protein" -> stringResource(Res.string.compare_row_protein)
                                "Dietary Fiber" -> stringResource(Res.string.compare_row_dietary_fiber)
                                else -> metric
                            }
                            val valA = itemA.nutritionHighlights.firstOrNull {
                                it.name.equals(metric, ignoreCase = true) ||
                                    it.name.equals(metricVi, ignoreCase = true) ||
                                    it.name.contains(metric, ignoreCase = true) ||
                                    it.name.contains(metricVi, ignoreCase = true)
                            }?.amount ?: "-"
                            val valB = itemB.nutritionHighlights.firstOrNull {
                                it.name.equals(metric, ignoreCase = true) ||
                                    it.name.equals(metricVi, ignoreCase = true) ||
                                    it.name.contains(metric, ignoreCase = true) ||
                                    it.name.contains(metricVi, ignoreCase = true)
                            }?.amount ?: "-"
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(valA, style = AppTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.weight(1f))
                                Text(localizedMetricName, style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.text.secondary), modifier = Modifier.weight(1f))
                                Text(valB, style = AppTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Flagged Ingredients Comparison
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(Res.string.compare_flagged_items_title), style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (itemA.watchOutForYou.isEmpty() && itemA.worthNoting.isEmpty()) {
                                    Text(stringResource(Res.string.compare_no_flags), style = AppTheme.typography.bodySmall, color = AppTheme.colors.text.secondary)
                                } else {
                                    (itemA.watchOutForYou + itemA.worthNoting).take(4).forEach {
                                        Text("• ${it.title}", style = AppTheme.typography.bodySmall)
                                    }
                                }
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (itemB.watchOutForYou.isEmpty() && itemB.worthNoting.isEmpty()) {
                                    Text(stringResource(Res.string.compare_no_flags), style = AppTheme.typography.bodySmall, color = AppTheme.colors.text.secondary)
                                } else {
                                    (itemB.watchOutForYou + itemB.worthNoting).take(4).forEach {
                                        Text("• ${it.title}", style = AppTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductHeaderCard(
    item: LabelAnalysisResult,
    language: AppLanguage,
    badge: String,
    badgeColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(badge, color = Color.White, style = AppTheme.typography.bodyExtraSmall.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = ProductNameSanitizer.displayProductName(item.productName, language),
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.primary,
            )
        }
    }
}
