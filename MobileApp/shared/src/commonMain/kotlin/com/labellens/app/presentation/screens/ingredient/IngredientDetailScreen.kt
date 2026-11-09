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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.labellens.app.data.repository.UserProfileRepository
import com.labellens.app.designsystem.theme.AppTheme
import com.labellens.app.domain.analyzer.KnowledgeBase
import com.labellens.app.domain.analyzer.category
import com.labellens.app.domain.analyzer.displayName
import com.labellens.app.domain.analyzer.doseContextNote
import com.labellens.app.domain.analyzer.plainExplanation
import com.labellens.app.domain.analyzer.whyUsed
import com.labellens.app.domain.model.KnowledgeIngredient
import com.labellens.app.generated.resources.Res
import com.labellens.app.generated.resources.btn_back
import com.labellens.app.generated.resources.ingredient_avoid_flagged
import com.labellens.app.generated.resources.ingredient_avoid_safe
import com.labellens.app.generated.resources.ingredient_detail_title
import com.labellens.app.generated.resources.ingredient_fallback_standard
import com.labellens.app.generated.resources.ingredient_section_aliases
import com.labellens.app.generated.resources.ingredient_section_dose_context
import com.labellens.app.generated.resources.ingredient_section_sources
import com.labellens.app.generated.resources.ingredient_section_what_is_it
import com.labellens.app.generated.resources.ingredient_section_who_avoid
import com.labellens.app.generated.resources.ingredient_section_why_used
import com.labellens.app.generated.resources.ingredient_sources_description
import com.labellens.app.generated.resources.ingredient_unknown_definition
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun IngredientDetailScreen(
    canonicalName: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val userProfileRepository = koinInject<UserProfileRepository>()
    val language by userProfileRepository.languageFlow.collectAsStateWithLifecycle()

    val ingredient: KnowledgeIngredient? = KnowledgeBase.findIngredientByAnyName(canonicalName)

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
                    text = stringResource(Res.string.ingredient_detail_title),
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
                        text = ingredient?.displayName(language) ?: canonicalName,
                        style = AppTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.text.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = ingredient?.category(language) ?: stringResource(Res.string.ingredient_fallback_standard),
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
                title = stringResource(Res.string.ingredient_section_what_is_it),
                description = ingredient?.plainExplanation(language)
                    ?: stringResource(Res.string.ingredient_unknown_definition),
            )
        }

        // Section: Why it is used
        item {
            InfoSectionCard(
                title = stringResource(Res.string.ingredient_section_why_used),
                description = ingredient?.whyUsed(language) ?: "",
            )
        }

        // Section: Dose / Context note
        item {
            InfoSectionCard(
                title = stringResource(Res.string.ingredient_section_dose_context),
                description = ingredient?.doseContextNote(language) ?: "",
            )
        }

        // Section: Who may want to avoid it (shown only when ingredient is known)
        if (ingredient != null) {
            item {
                val avoidText = if (ingredient.avoidForTags.isNotEmpty()) {
                    stringResource(Res.string.ingredient_avoid_flagged, ingredient.avoidForTags.joinToString(", "))
                } else {
                    stringResource(Res.string.ingredient_avoid_safe)
                }
                InfoSectionCard(
                    title = stringResource(Res.string.ingredient_section_who_avoid),
                    description = avoidText,
                )
            }
        }

        // Section: Aliases & Multilingual Names
        if (ingredient != null && ingredient.aliases.isNotEmpty()) {
            item {
                InfoSectionCard(
                    title = stringResource(Res.string.ingredient_section_aliases),
                    description = ingredient.aliases.joinToString(" • "),
                )
            }
        }

        // Section: Sources Note
        item {
            InfoSectionCard(
                title = stringResource(Res.string.ingredient_section_sources),
                description = stringResource(Res.string.ingredient_sources_description),
            )
        }
    }
}

@Composable
private fun InfoSectionCard(title: String, description: String) {
    if (description.isBlank()) return
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
