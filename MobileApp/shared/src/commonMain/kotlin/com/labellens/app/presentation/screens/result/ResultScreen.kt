package com.labellens.app.presentation.screens.result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.labellens.app.designsystem.components.AppButton
import com.labellens.app.designsystem.components.ButtonStyle
import com.labellens.app.designsystem.generated.resources.UiRes
import com.labellens.app.designsystem.generated.resources.ic_beaker_amber
import com.labellens.app.designsystem.generated.resources.ic_leaf_amber
import com.labellens.app.designsystem.theme.AppTheme
import com.labellens.app.domain.analyzer.AnalysisStrings
import com.labellens.app.domain.analyzer.PortionMath
import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.ClusterRollUp
import com.labellens.app.domain.model.FitHeaderInfo
import com.labellens.app.domain.model.FlagSeverity
import com.labellens.app.domain.model.FlaggedItem
import com.labellens.app.domain.model.IngredientBreakdown
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.PortionMode
import com.labellens.app.domain.model.ProfileFitState
import com.labellens.app.generated.resources.Res
import com.labellens.app.generated.resources.action_details
import com.labellens.app.generated.resources.action_favorite
import com.labellens.app.generated.resources.ai_tag_chip
import com.labellens.app.generated.resources.btn_back
import com.labellens.app.generated.resources.content_desc_delete
import com.labellens.app.generated.resources.result_action_collapse
import com.labellens.app.generated.resources.result_action_expand
import com.labellens.app.generated.resources.result_btn_accurate
import com.labellens.app.generated.resources.result_btn_compare
import com.labellens.app.generated.resources.result_btn_needs_fix
import com.labellens.app.generated.resources.result_btn_save_history
import com.labellens.app.generated.resources.result_btn_saved_history
import com.labellens.app.generated.resources.result_catch_all_badge
import com.labellens.app.generated.resources.result_catch_all_note
import com.labellens.app.generated.resources.result_cut_line_divider
import com.labellens.app.generated.resources.result_delete_dialog_cancel
import com.labellens.app.generated.resources.result_delete_dialog_confirm
import com.labellens.app.generated.resources.result_delete_dialog_message
import com.labellens.app.generated.resources.result_delete_dialog_title
import com.labellens.app.generated.resources.result_exception_btn
import com.labellens.app.generated.resources.result_feedback_prompt
import com.labellens.app.generated.resources.result_feedback_thanks
import com.labellens.app.generated.resources.result_fix_dialog_cancel
import com.labellens.app.generated.resources.result_fix_dialog_confirm
import com.labellens.app.generated.resources.result_fix_dialog_title
import com.labellens.app.generated.resources.result_fix_misread_btn
import com.labellens.app.generated.resources.result_header_title
import com.labellens.app.generated.resources.result_loading
import com.labellens.app.generated.resources.result_personal_summary_contains
import com.labellens.app.generated.resources.result_personal_summary_nothing
import com.labellens.app.generated.resources.result_personal_summary_prefix
import com.labellens.app.generated.resources.result_portion_servings_count
import com.labellens.app.generated.resources.result_portion_single_serving
import com.labellens.app.generated.resources.result_portion_whole_pack
import com.labellens.app.generated.resources.result_rollup_sodium_subtitle
import com.labellens.app.generated.resources.result_rollup_sweeteners_subtitle
import com.labellens.app.generated.resources.result_section_breakdown_subtitle
import com.labellens.app.generated.resources.result_section_breakdown_title
import com.labellens.app.generated.resources.result_section_good_to_know_subtitle
import com.labellens.app.generated.resources.result_section_good_to_know_title
import com.labellens.app.generated.resources.result_section_nutrition_subtitle
import com.labellens.app.generated.resources.result_section_nutrition_title
import com.labellens.app.generated.resources.result_section_watch_out_subtitle
import com.labellens.app.generated.resources.result_section_watch_out_title
import com.labellens.app.generated.resources.result_section_worth_noting_subtitle
import com.labellens.app.generated.resources.result_section_worth_noting_title
import com.labellens.app.generated.resources.result_see_label_text
import com.labellens.app.generated.resources.result_source_verbatim_prefix
import com.labellens.app.generated.resources.result_watch_out_clean_subtitle
import com.labellens.app.generated.resources.result_watch_out_clean_title
import com.labellens.app.presentation.navigation.ResultScreenRoute
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

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
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(Res.string.btn_back),
                    tint = AppTheme.colors.text.primary,
                )
            }
            Text(
                text = stringResource(Res.string.result_loading),
                style = AppTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        return
    }

    val groupedIngredients = remember(result) { groupAndOrderIngredients(result) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Top Header Bar with Back Button & Favorite Toggle
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
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(Res.string.result_header_title),
                    style = AppTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.text.primary,
                    ),
                    modifier = Modifier.weight(1f),
                )
                if (state.isDeletable) {
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.content_desc_delete),
                            tint = AppTheme.colors.text.secondary,
                        )
                    }
                }
                IconButton(onClick = { viewModel.toggleFavorite() }) {
                    Icon(
                        imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(Res.string.action_favorite),
                        tint = if (state.isFavorite) Color(0xFFB3261E) else AppTheme.colors.text.secondary,
                    )
                }
            }
        }

        // Wave 2: Six-State Deterministic Profile-Fit Header Card
        result.fitHeader?.let { fitHeader ->
            item {
                ProfileFitHeaderCard(
                    fitHeader = fitHeader,
                    language = state.language,
                    onFixMisreadClick = { viewModel.openFixDialog() },
                )
            }
        }

        // Product Title & Verdict Header
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFEBE6DC)),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = result.productName,
                        style = AppTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.text.primary,
                        ),
                    )

                    result.productType?.let { type ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = type,
                            style = AppTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.primary,
                            ),
                        )
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

        // Alias Roll-Up Card (Sweeteners & Sodium Sources)
        if (result.sweetenerRollUp != null || result.sodiumRollUp != null) {
            item {
                AliasRollUpCard(
                    sweetenerRollUp = result.sweetenerRollUp,
                    sodiumRollUp = result.sodiumRollUp,
                    onNavigateToIngredientDetail = onNavigateToIngredientDetail,
                )
            }
        }

        // Portion Reality & Nutrition Highlights (when present)
        if (result.nutritionHighlights.isNotEmpty()) {
            item {
                SectionHeader(
                    title = stringResource(Res.string.result_section_nutrition_title),
                    subtitle = stringResource(Res.string.result_section_nutrition_subtitle),
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        // Serving size & Servings per container info
                        result.portionInfo.servingSize?.let { size ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = size,
                                    style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = AppTheme.colors.text.secondary,
                                )
                                result.portionInfo.servingsPerContainer?.let { s ->
                                    val countStr = if (s % 1.0 == 0.0) s.toInt().toString() else s.toString()
                                    Text(
                                        text = stringResource(Res.string.result_portion_servings_count, countStr),
                                        style = AppTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = AppTheme.colors.primary,
                                    )
                                }
                            }
                        }

                        // [1 Serving] / [Whole Pack] Segmented Toggle (Shown only when servingsPerContainer > 1)
                        if (PortionMath.shouldShowToggle(result.portionInfo.servingsPerContainer)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF2F4EF))
                                    .padding(4.dp),
                            ) {
                                val isSingle = state.portionMode == PortionMode.SINGLE_SERVING
                                val isWhole = state.portionMode == PortionMode.WHOLE_PACK

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSingle) Color.White else Color.Transparent)
                                        .clickable { viewModel.setPortionMode(PortionMode.SINGLE_SERVING) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(Res.string.result_portion_single_serving),
                                        style = AppTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSingle) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSingle) AppTheme.colors.primary else AppTheme.colors.text.secondary,
                                        ),
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isWhole) Color.White else Color.Transparent)
                                        .clickable { viewModel.setPortionMode(PortionMode.WHOLE_PACK) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(Res.string.result_portion_whole_pack),
                                        style = AppTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isWhole) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isWhole) AppTheme.colors.primary else AppTheme.colors.text.secondary,
                                        ),
                                    )
                                }
                            }
                        }

                        val currentFacts = remember(result.nutritionHighlights, state.portionMode, result.portionInfo.servingsPerContainer, state.language) {
                            PortionMath.scaleNutritionFacts(
                                facts = result.nutritionHighlights,
                                mode = state.portionMode,
                                servingsPerContainer = result.portionInfo.servingsPerContainer,
                                language = state.language,
                            )
                        }

                        currentFacts.forEach { fact ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fact.name,
                                        style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = AppTheme.colors.text.primary,
                                    )
                                    fact.secondaryDetail?.let { sec ->
                                        Text(
                                            text = sec,
                                            style = AppTheme.typography.bodyExtraSmall.copy(fontWeight = FontWeight.Medium),
                                            color = AppTheme.colors.text.secondary,
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = fact.amount,
                                        style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
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
                                                maxLines = 1,
                                                softWrap = false,
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

        // Unified Ingredient Detail list
        if (groupedIngredients.isNotEmpty()) {
            item {
                SectionHeader(
                    title = stringResource(Res.string.result_section_breakdown_title),
                    subtitle = stringResource(Res.string.result_section_breakdown_subtitle),
                )
            }
            items(groupedIngredients) { grouped ->
                IngredientRow(
                    ingredient = grouped.ingredient,
                    group = grouped.group,
                    onClick = { onNavigateToIngredientDetail(grouped.ingredient.canonicalName) },
                )
            }
        }

        // Section 5: See the label text expandable
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
                            text = stringResource(Res.string.result_see_label_text),
                            style = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.text.primary,
                        )
                        Text(
                            text = stringResource(if (state.isLabelTextExpanded) Res.string.result_action_collapse else Res.string.result_action_expand),
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
                        text = stringResource(if (state.feedbackSubmitted) Res.string.result_feedback_thanks else Res.string.result_feedback_prompt),
                        style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = AppTheme.colors.text.primary,
                    )
                    if (!state.feedbackSubmitted) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            AppButton(
                                text = stringResource(Res.string.result_btn_accurate),
                                size = com.labellens.app.designsystem.components.ButtonSize.SMALL,
                                style = ButtonStyle.PRIMARY,
                                onClick = { viewModel.submitFeedback(true) },
                            )
                            AppButton(
                                text = stringResource(Res.string.result_btn_needs_fix),
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
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.isSaved) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = AppTheme.colors.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(Res.string.result_btn_saved_history),
                            style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = AppTheme.colors.primary,
                        )
                    }
                } else {
                    AppButton(
                        text = stringResource(Res.string.result_btn_save_history),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.saveToHistory() },
                    )
                }
                AppButton(
                    text = stringResource(Res.string.result_btn_compare),
                    style = ButtonStyle.ALTERNATIVE,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToCompare(result) },
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // Wave 2 - Scope 5: "Misread label? Fix" dialog
    if (state.isFixDialogOpen) {
        var editedText by remember(result.rawInputText) { mutableStateOf(result.rawInputText) }
        AlertDialog(
            onDismissRequest = { viewModel.dismissFixDialog() },
            title = {
                Text(
                    text = stringResource(Res.string.result_fix_dialog_title),
                    style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.colors.text.primary,
                )
            },
            text = {
                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = AppTheme.typography.bodySmall,
                )
            },
            confirmButton = {
                AppButton(
                    text = stringResource(Res.string.result_fix_dialog_confirm),
                    size = com.labellens.app.designsystem.components.ButtonSize.SMALL,
                    style = ButtonStyle.PRIMARY,
                    onClick = { viewModel.fixMisreadLabel(editedText) },
                )
            },
            dismissButton = {
                AppButton(
                    text = stringResource(Res.string.result_fix_dialog_cancel),
                    size = com.labellens.app.designsystem.components.ButtonSize.SMALL,
                    style = ButtonStyle.ALTERNATIVE,
                    onClick = { viewModel.dismissFixDialog() },
                )
            },
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = stringResource(Res.string.result_delete_dialog_title),
                    style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.colors.text.primary,
                )
            },
            text = {
                Text(
                    text = stringResource(Res.string.result_delete_dialog_message),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.text.secondary,
                )
            },
            confirmButton = {
                AppButton(
                    text = stringResource(Res.string.result_delete_dialog_confirm),
                    size = com.labellens.app.designsystem.components.ButtonSize.SMALL,
                    style = ButtonStyle.PRIMARY,
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteResult(onDeleted = onNavigateBack)
                    },
                )
            },
            dismissButton = {
                AppButton(
                    text = stringResource(Res.string.result_delete_dialog_cancel),
                    size = com.labellens.app.designsystem.components.ButtonSize.SMALL,
                    style = ButtonStyle.ALTERNATIVE,
                    onClick = { showDeleteConfirmDialog = false },
                )
            },
        )
    }
}

@Composable
private fun ProfileFitHeaderCard(
    fitHeader: FitHeaderInfo,
    language: AppLanguage = AppLanguage.ENGLISH,
    onFixMisreadClick: () -> Unit,
) {
    val state = fitHeader.state
    val (containerColor, borderColor, accentColor) = when (state) {
        ProfileFitState.CONTAINS_YOURS -> Triple(AppTheme.colors.status.allergenContainer, Color(0xFFF5C2C7), AppTheme.colors.status.allergenRed)
        ProfileFitState.CANNOT_RULE_OUT -> Triple(AppTheme.colors.status.amberContainer, Color(0xFFF1D38A), AppTheme.colors.status.amber)
        ProfileFitState.NOT_ENOUGH_INFO -> Triple(Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFF546E7A))
        ProfileFitState.DEPENDS_ON_PORTION -> Triple(Color(0xFFFFF3E0), Color(0xFFFFCC80), Color(0xFFE65100))
        ProfileFitState.WORTH_NOTING -> Triple(AppTheme.colors.status.amberContainer, Color(0xFFF1D38A), AppTheme.colors.status.amber)
        ProfileFitState.NO_MATCHES -> Triple(AppTheme.colors.bannerTint, Color(0xFFC3DBC8), AppTheme.colors.primary)
    }

    val iconVector = when (state) {
        ProfileFitState.CONTAINS_YOURS -> Icons.Default.Close
        ProfileFitState.CANNOT_RULE_OUT -> Icons.Default.Warning
        ProfileFitState.NOT_ENOUGH_INFO -> Icons.Default.Info
        ProfileFitState.DEPENDS_ON_PORTION -> Icons.Default.Warning
        ProfileFitState.WORTH_NOTING -> Icons.Default.Info
        ProfileFitState.NO_MATCHES -> Icons.Default.Check
    }

    val shapeModifier = when (state) {
        ProfileFitState.CONTAINS_YOURS -> RoundedCornerShape(6.dp)
        ProfileFitState.CANNOT_RULE_OUT -> RoundedCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
        ProfileFitState.NOT_ENOUGH_INFO -> CircleShape
        ProfileFitState.DEPENDS_ON_PORTION -> RoundedCornerShape(8.dp)
        ProfileFitState.WORTH_NOTING -> RoundedCornerShape(4.dp)
        ProfileFitState.NO_MATCHES -> RoundedCornerShape(8.dp)
    }

    val badgeLabelText = if (fitHeader.badgeLabel.isNotBlank()) {
        fitHeader.badgeLabel
    } else {
        AnalysisStrings.fitStateBadge(language, state)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Colour-blind safe badge: distinct shape + icon + label text
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(shapeModifier)
                        .background(accentColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = badgeLabelText,
                    style = AppTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp,
                        color = if (state == ProfileFitState.NO_MATCHES) AppTheme.colors.bannerHeadline else accentColor,
                    ),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = fitHeader.title,
                style = AppTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (state == ProfileFitState.NO_MATCHES) AppTheme.colors.bannerHeadline else accentColor,
                ),
            )

            val showSubtitle = fitHeader.subtitle.isNotBlank() &&
                !fitHeader.subtitle.trim().equals(fitHeader.title.trim(), ignoreCase = true)
            if (showSubtitle) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = fitHeader.subtitle,
                    style = AppTheme.typography.bodyMedium.copy(
                        color = AppTheme.colors.text.primary,
                        lineHeight = 21.sp,
                    ),
                )
            }

            fitHeader.reasonNote?.let { reason ->
                if (!reason.trim().equals(fitHeader.title.trim(), ignoreCase = true) &&
                    (!showSubtitle || !reason.trim().equals(fitHeader.subtitle.trim(), ignoreCase = true))
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.85f))
                            .padding(12.dp),
                    ) {
                        Text(
                            text = reason,
                            style = AppTheme.typography.bodySmall.copy(
                                color = AppTheme.colors.text.secondary,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 18.sp,
                            ),
                        )
                    }
                }
            }

            fitHeader.verbatimSourceLine?.let { line ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(Res.string.result_source_verbatim_prefix) + line,
                    style = AppTheme.typography.bodyExtraSmall.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = AppTheme.colors.text.secondary,
                    ),
                )
            }

            // Correction loop trigger: Misread label? Fix (styled as compact text button at card bottom)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onFixMisreadClick() },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp,
            ) {
                Text(
                    text = stringResource(Res.string.result_fix_misread_btn),
                    style = AppTheme.typography.bodyExtraSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.primary,
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
private fun TwoUpAmberCard(
    item: FlaggedItem,
    modifier: Modifier = Modifier,
    onExceptionClick: ((String) -> Unit)? = null,
) {
    val iconRes = if (item.title.contains("hương", ignoreCase = true) ||
        item.title.contains("flavor", ignoreCase = true) ||
        item.title.contains("fragrance", ignoreCase = true) ||
        item.title.contains("oil", ignoreCase = true) ||
        item.title.contains("dầu", ignoreCase = true)
    ) {
        UiRes.drawable.ic_leaf_amber
    } else {
        UiRes.drawable.ic_beaker_amber
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFFBF3DF),
        border = BorderStroke(1.dp, Color(0xFFF5E3C0)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Color(0xFFB7791F),
                modifier = Modifier.size(24.dp).padding(top = 1.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9E6515),
                    style = AppTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.detail,
                    style = AppTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = AppTheme.colors.text.secondary,
                        lineHeight = 16.sp,
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun FlagItemCard(
    item: FlaggedItem,
    onExceptionClick: ((String) -> Unit)? = null,
) {
    val (containerColor, accentColor, borderColor) = when (item.severity) {
        FlagSeverity.PERSONAL_ALLERGEN -> Triple(Color(0xFFFDECEA), Color(0xFFB3261E), Color(0xFFF5C2C7))
        FlagSeverity.PERSONAL_AVOID -> Triple(Color(0xFFFBF3DF), Color(0xFFB7791F), Color(0xFFF5E3C0))
        FlagSeverity.WORTH_NOTING -> Triple(Color(0xFFFBF3DF), Color(0xFFB7791F), Color(0xFFF5E3C0))
        FlagSeverity.GOOD_TO_KNOW -> Triple(AppTheme.colors.bannerTint, AppTheme.colors.primary, Color(0xFFD6E5D7))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
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
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        style = AppTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (item.isAiGenerated) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEDE7F6))
                                .border(1.dp, Color(0xFFB39DDB), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.ai_tag_chip),
                                style = AppTheme.typography.bodyExtraSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5E35B1),
                                ),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.detail,
                    style = AppTheme.typography.bodySmall.copy(
                        color = AppTheme.colors.text.primary,
                        lineHeight = 18.sp,
                    ),
                )

                // Scope 5: Verbatim source line stays visible
                item.verbatimSourceLine?.let { line ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.result_source_verbatim_prefix) + line,
                        style = AppTheme.typography.bodyExtraSmall.copy(
                            color = AppTheme.colors.text.secondary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        ),
                    )
                }

                // Scope 5: "I'm fine with this ingredient" action for non-allergen items
                if (item.isExceptionEligible && item.severity != FlagSeverity.PERSONAL_ALLERGEN && onExceptionClick != null) {
                    val exemptTarget = item.relatedIngredient ?: item.title
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White)
                            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .clickable { onExceptionClick(exemptTarget) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.result_exception_btn),
                            style = AppTheme.typography.bodyExtraSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IngredientRow(
    ingredient: IngredientBreakdown,
    group: IngredientDetailGroup = IngredientDetailGroup.REMAINING,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Color(0xFFEBE6DC)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = ingredient.canonicalName,
                    style = AppTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = AppTheme.colors.text.primary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = stringResource(Res.string.action_details),
                    tint = AppTheme.colors.text.secondary,
                    modifier = Modifier.size(16.dp).padding(top = 2.dp),
                )
            }

            val hasGroupChip = group != IngredientDetailGroup.REMAINING
            val hasAiChip = ingredient.isAiGenerated
            val showCategoryChip = !hasAiChip && ingredient.category.isNotBlank()

            if (hasGroupChip || hasAiChip || showCategoryChip) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    when (group) {
                        IngredientDetailGroup.WATCH_OUT -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppTheme.colors.status.allergenContainer)
                                    .border(1.dp, Color(0xFFF5C2C7), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = stringResource(Res.string.result_section_watch_out_title),
                                    style = AppTheme.typography.bodyExtraSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AppTheme.colors.status.allergenRed,
                                    ),
                                )
                            }
                        }

                        IngredientDetailGroup.WORTH_NOTING -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppTheme.colors.status.amberContainer)
                                    .border(1.dp, Color(0xFFF5E3C0), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = stringResource(Res.string.result_section_worth_noting_title),
                                    style = AppTheme.typography.bodyExtraSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AppTheme.colors.status.amber,
                                    ),
                                )
                            }
                        }

                        IngredientDetailGroup.GOOD_TO_KNOW -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppTheme.colors.bannerTint)
                                    .border(1.dp, Color(0xFFD6E5D7), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = stringResource(Res.string.result_section_good_to_know_title),
                                    style = AppTheme.typography.bodyExtraSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AppTheme.colors.primary,
                                    ),
                                )
                            }
                        }

                        IngredientDetailGroup.REMAINING -> { /* No tag chip */ }
                    }

                    if (hasAiChip) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEDE7F6))
                                .border(1.dp, Color(0xFFB39DDB), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.ai_tag_chip),
                                style = AppTheme.typography.bodyExtraSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5E35B1),
                                ),
                            )
                        }
                    } else if (showCategoryChip) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppTheme.colors.bannerTint)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = ingredient.category,
                                style = AppTheme.typography.bodyExtraSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.colors.primary,
                                ),
                            )
                        }
                    }
                }
            }

            if (ingredient.isCatchAll && ingredient.catchAllNote != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ingredient.catchAllNote,
                    style = AppTheme.typography.bodyExtraSmall.copy(
                        color = Color(0xFF8A6D3B),
                        lineHeight = 15.sp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun CutLineDivider(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color(0xFFE2E4DE),
            thickness = 1.dp,
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF0F2ED))
                .padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            Text(
                text = label,
                style = AppTheme.typography.bodyExtraSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.text.secondary,
                ),
            )
        }
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color(0xFFE2E4DE),
            thickness = 1.dp,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AliasRollUpCard(
    sweetenerRollUp: ClusterRollUp?,
    sodiumRollUp: ClusterRollUp?,
    onNavigateToIngredientDetail: (String) -> Unit,
) {
    if (sweetenerRollUp == null && sodiumRollUp == null) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Sweeteners roll-up
            sweetenerRollUp?.let { rollUp ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = rollUp.title,
                            style = AppTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB7791F),
                            ),
                        )
                        Text(
                            text = stringResource(Res.string.result_rollup_sweeteners_subtitle),
                            style = AppTheme.typography.bodyExtraSmall.copy(
                                fontWeight = FontWeight.Normal,
                                color = AppTheme.colors.text.secondary,
                            ),
                        )
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rollUp.items.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFDF7E7))
                                    .border(1.dp, Color(0xFFEEDBB2), RoundedCornerShape(10.dp))
                                    .clickable { onNavigateToIngredientDetail(item.canonicalName) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = item.canonicalName,
                                    style = AppTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF7A4D05),
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            if (sweetenerRollUp != null && sodiumRollUp != null) {
                HorizontalDivider(color = Color(0xFFF0F0EE), thickness = 1.dp)
            }

            // Sodium roll-up
            sodiumRollUp?.let { rollUp ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = rollUp.title,
                            style = AppTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.primary,
                            ),
                        )
                        Text(
                            text = stringResource(Res.string.result_rollup_sodium_subtitle),
                            style = AppTheme.typography.bodyExtraSmall.copy(
                                fontWeight = FontWeight.Normal,
                                color = AppTheme.colors.text.secondary,
                            ),
                        )
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rollUp.items.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEDF2ED))
                                    .border(1.dp, Color(0xFFCCE0D3), RoundedCornerShape(10.dp))
                                    .clickable { onNavigateToIngredientDetail(item.canonicalName) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = item.canonicalName,
                                    style = AppTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F5A37),
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
