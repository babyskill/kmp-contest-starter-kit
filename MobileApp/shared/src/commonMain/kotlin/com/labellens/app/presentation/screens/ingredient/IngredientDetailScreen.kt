package com.labellens.app.presentation.screens.ingredient

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.labellens.app.designsystem.theme.AppTheme
import com.labellens.app.domain.analyzer.KnowledgeBase
import com.labellens.app.domain.model.KnowledgeIngredient

@Composable
fun IngredientDetailScreen(
    canonicalName: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ingredient: KnowledgeIngredient? = KnowledgeBase.ingredients.firstOrNull {
        it.canonicalName.equals(canonicalName, ignoreCase = true) ||
            it.aliases.any { alias -> alias.equals(canonicalName, ignoreCase = true) }
    }

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
                        contentDescription = "Back",
                        tint = AppTheme.colors.text.primary,
                    )
                }
                Text(
                    text = "Ingredient Detail",
                    style = AppTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.text.primary,
                    ),
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = ingredient?.canonicalName ?: canonicalName,
                        style = AppTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.text.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = ingredient?.category ?: "Food Ingredient",
                        style = AppTheme.typography.bodyMedium.copy(
                            color = AppTheme.colors.primary,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }
        }

        // Section: What it is
        item {
            InfoSectionCard(
                title = "What it is",
                description = ingredient?.plainExplanation
                    ?: "Standard culinary ingredient commonly present on packaged food and beverage labels.",
            )
        }

        // Section: Why it is used
        item {
            InfoSectionCard(
                title = "Why it is used",
                description = ingredient?.whyUsed
                    ?: "Contributes to structure, shelf freshness, or standard culinary flavoring.",
            )
        }

        // Section: Dose / Context note
        item {
            InfoSectionCard(
                title = "Dose / Context note",
                description = ingredient?.doseContextNote
                    ?: "Standard food-grade usage in accordance with dietary regulatory codex.",
            )
        }

        // Section: Who may want to avoid it
        item {
            val avoidText = if (ingredient?.avoidForTags?.isNotEmpty() == true) {
                "Flagged for: ${ingredient.avoidForTags.joinToString(", ")}. Check your personal allergens and goals."
            } else {
                "Generally recognized as safe with no common restricted dietary flags."
            }
            InfoSectionCard(
                title = "Who may want to avoid it",
                description = avoidText,
            )
        }

        // Section: Aliases & Multilingual Names
        if (ingredient != null && ingredient.aliases.isNotEmpty()) {
            item {
                InfoSectionCard(
                    title = "Label Aliases & Multilingual Names",
                    description = ingredient.aliases.joinToString(" • "),
                )
            }
        }

        // Section: Sources Note
        item {
            InfoSectionCard(
                title = "Sources & Standards",
                description = "Knowledge synthesized from Codex Alimentarius, US FDA GRAS notices, European Food Safety Authority (EFSA), and regional food standards agencies.",
            )
        }
    }
}

@Composable
private fun InfoSectionCard(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = AppTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.text.primary,
                ),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = AppTheme.typography.bodyMedium.copy(
                    color = AppTheme.colors.text.secondary,
                    lineHeight = 22.sp,
                ),
            )
        }
    }
}
