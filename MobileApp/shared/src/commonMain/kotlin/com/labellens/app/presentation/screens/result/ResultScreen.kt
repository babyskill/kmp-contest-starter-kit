package com.labellens.app.presentation.screens.result

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.labellens.app.domain.model.FlagSeverity
import com.labellens.app.domain.model.FlaggedItem
import com.labellens.app.domain.model.IngredientBreakdown
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.NutritionFact
import com.labellens.app.presentation.navigation.ResultScreenRoute

@Composable
fun ResultScreen(
    route: ResultScreenRoute,
    viewModel: ResultViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToIngredientDetail: (canonicalName: String) -> Unit,
    onNavigateToCompare: (result: LabelAnalysisResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(route) {
        viewModel.loadAnalysis(
            scanId = route.scanId,
            sampleId = route.sampleId,
            rawText = route.rawText,
            productName = route.productName,
            barcode = route.barcode,
            sourceName = route.sourceName,
        )
    }

    val result = state.analysisResult
    if (result == null) {
        Box(
            modifier = modifier.fillMaxSize().background(AppTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            Text("Loading label analysis...", style = AppTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // At-a-glance one-line personal summary banner
        item {
            val personalSummaryText = remember(result.watchOutForYou) {
                if (result.watchOutForYou.isEmpty()) {
                    "For your profile: nothing flagged"
                } else {
                    val flags = result.watchOutForYou.map { item ->
                        when (item.severity) {
                            FlagSeverity.PERSONAL_ALLERGEN -> {
                                val allergenName = item.title.removePrefix("Contains ").lowercase()
                                "contains $allergenName (your allergen)"
                            }

                            FlagSeverity.PERSONAL_AVOID -> {
                                item.title.lowercase()
                            }

                            else -> item.title.lowercase()
                        }
                    }
                    "For your profile: " + flags.joinToString(" · ")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (result.watchOutForYou.isEmpty()) Color(0xFFEDF2ED) else Color(0xFFFDECEA),
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (result.watchOutForYou.isEmpty()) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (result.watchOutForYou.isEmpty()) Color(0xFF16794C) else Color(0xFFB3261E),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = personalSummaryText,
                        style = AppTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (result.watchOutForYou.isEmpty()) Color(0xFF16794C) else Color(0xFFB3261E),
                        ),
                    )
                }
            }
        }

        // Product Title & Verdict Header
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
                            text = result.productName,
                            style = AppTheme.typography.headlineMedium.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.text.primary,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (state.isFavorite) Color(0xFFB3261E) else AppTheme.colors.text.secondary,
                            )
                        }
                    }

                    result.sourceAttribution?.let { source ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = source,
                            style = AppTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.primary,
                            ),
                        )
                    }

                    result.quickGlance?.let { glance ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = glance,
                            style = AppTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = AppTheme.colors.text.secondary,
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = result.verdict,
                        style = AppTheme.typography.bodyLarge.copy(
                            color = AppTheme.colors.text.primary,
                            lineHeight = 22.sp,
                        ),
                    )
                }
            }
        }

        // Section 1: Watch out for you (Personal flags only)
        if (result.watchOutForYou.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Watch out for you",
                    subtitle = "Matches against your personal profile",
                )
            }
            items(result.watchOutForYou) { item ->
                FlagItemCard(item = item)
            }
        }

        // Section 2: Worth noting (General additives, sweeteners, sodium)
        if (result.worthNoting.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Worth noting",
                    subtitle = "Notable additives, flavorings, or preservatives",
                )
            }
            items(result.worthNoting) { item ->
                FlagItemCard(item = item)
            }
        }

        // Section 3: Good to know (Whole grains, wholesome items, botanicals)
        if (result.goodToKnow.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Good to know",
                    subtitle = "Positive attributes and wholesome ingredients",
                )
            }
            items(result.goodToKnow) { item ->
                FlagItemCard(item = item)
            }
        }

        // Section 4: Full breakdown (Expandable rows leading to Ingredient Detail)
        item {
            SectionHeader(
                title = "Full breakdown",
                subtitle = "Tap any ingredient for its culinary purpose & dose context",
            )
        }
        items(result.fullBreakdown) { ingredient ->
            IngredientRow(
                ingredient = ingredient,
                onClick = { onNavigateToIngredientDetail(ingredient.canonicalName) },
            )
        }

        // Section 5: Nutrition highlights (when present)
        if (result.nutritionHighlights.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Nutrition highlights",
                    subtitle = "Key nutritional values parsed from label",
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        result.nutritionHighlights.forEach { fact ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = fact.name,
                                    style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = AppTheme.colors.text.primary,
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = fact.amount,
                                        style = AppTheme.typography.bodyMedium,
                                        color = AppTheme.colors.text.primary,
                                    )
                                    fact.contextIndicator?.let { ctx ->
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(AppTheme.colors.alternative)
                                                .padding(horizontal = 8.dp, vertical = 2.dp),
                                        ) {
                                            Text(
                                                text = ctx,
                                                style = AppTheme.typography.bodyExtraSmall.copy(fontWeight = FontWeight.Medium),
                                                color = AppTheme.colors.primary,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 6: See the label text expandable
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleLabelTextExpansion() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "See the label text",
                            style = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.text.primary,
                        )
                        Text(
                            text = if (state.isLabelTextExpanded) "Collapse" else "Expand",
                            style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.primary, fontWeight = FontWeight.SemiBold),
                        )
                    }
                    if (state.isLabelTextExpanded) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = result.rawInputText,
                            style = AppTheme.typography.bodySmall.copy(
                                color = AppTheme.colors.text.secondary,
                                lineHeight = 18.sp,
                            ),
                        )
                    }
                }
            }
        }

        // 1-Tap Right/Wrong Feedback Control
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8F5)),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = if (state.feedbackSubmitted) "Thank you for your feedback!" else "Was this label analysis helpful?",
                        style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = AppTheme.colors.text.primary,
                    )
                    if (!state.feedbackSubmitted) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            AppButton(
                                text = "Accurate",
                                size = com.labellens.app.designsystem.components.ButtonSize.SMALL,
                                style = ButtonStyle.PRIMARY,
                                onClick = { viewModel.submitFeedback(true) },
                            )
                            AppButton(
                                text = "Needs Fix",
                                size = com.labellens.app.designsystem.components.ButtonSize.SMALL,
                                style = ButtonStyle.ALTERNATIVE,
                                onClick = { viewModel.submitFeedback(false) },
                            )
                        }
                    }
                }
            }
        }

        // Actions: Save to History & Compare
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppButton(
                    text = if (state.isSaved) "Saved to History" else "Save to History",
                    enabled = !state.isSaved,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.saveToHistory() },
                )
                AppButton(
                    text = "Compare",
                    style = ButtonStyle.ALTERNATIVE,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToCompare(result) },
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
    Column {
        Text(
            text = title,
            style = AppTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AppTheme.colors.text.primary,
            ),
        )
        subtitle?.let {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = it,
                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.text.secondary),
            )
        }
    }
}

@Composable
private fun FlagItemCard(item: FlaggedItem) {
    val (containerColor, accentColor) = when (item.severity) {
        FlagSeverity.PERSONAL_ALLERGEN -> Color(0xFFFDECEA) to Color(0xFFB3261E)
        FlagSeverity.PERSONAL_AVOID -> Color(0xFFFBF3DF) to Color(0xFFB7791F)
        FlagSeverity.WORTH_NOTING -> Color(0xFFFBF3DF) to Color(0xFFB7791F)
        FlagSeverity.GOOD_TO_KNOW -> Color(0xFFEDF2ED) to Color(0xFF16794C)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when (item.severity) {
                        FlagSeverity.GOOD_TO_KNOW -> Icons.Default.Check
                        FlagSeverity.PERSONAL_ALLERGEN -> Icons.Default.Close
                        else -> Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    style = AppTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.detail,
                    style = AppTheme.typography.bodySmall.copy(
                        color = AppTheme.colors.text.primary,
                    ),
                )
            }
        }
    }
}

@Composable
private fun IngredientRow(
    ingredient: IngredientBreakdown,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingredient.canonicalName,
                    style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppTheme.colors.text.primary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = ingredient.category,
                    style = AppTheme.typography.bodyExtraSmall,
                    color = AppTheme.colors.text.secondary,
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Details",
                tint = AppTheme.colors.outline,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
