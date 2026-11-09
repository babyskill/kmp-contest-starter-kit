package com.labellens.app.presentation.screens.result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.labellens.app.designsystem.components.AppButton
import com.labellens.app.designsystem.components.ButtonStyle
import com.labellens.app.designsystem.generated.resources.UiRes
import com.labellens.app.designsystem.generated.resources.ic_beaker_amber
import com.labellens.app.designsystem.generated.resources.ic_leaf_amber
import com.labellens.app.designsystem.generated.resources.ic_leaf_logo
import com.labellens.app.designsystem.theme.AppTheme
import com.labellens.app.domain.analyzer.AnalysisStrings
import com.labellens.app.domain.analyzer.LocalComparisonHelper
import com.labellens.app.domain.analyzer.ProductNameSanitizer
import com.labellens.app.domain.analyzer.ScanSummaryLocalizer
import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.FlagSeverity
import com.labellens.app.domain.model.FlaggedItem
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.ProductCategory
import com.labellens.app.domain.model.ProfileFitState
import com.labellens.app.generated.resources.Res
import com.labellens.app.generated.resources.action_favorite
import com.labellens.app.generated.resources.btn_back
import com.labellens.app.generated.resources.content_desc_delete
import com.labellens.app.generated.resources.result_action_collapse
import com.labellens.app.generated.resources.result_action_expand
import com.labellens.app.generated.resources.result_btn_accurate
import com.labellens.app.generated.resources.result_btn_compare
import com.labellens.app.generated.resources.result_btn_needs_fix
import com.labellens.app.generated.resources.result_btn_save_history
import com.labellens.app.generated.resources.result_btn_saved_history
import com.labellens.app.generated.resources.result_btn_saved_history_desc
import com.labellens.app.generated.resources.result_delete_dialog_cancel
import com.labellens.app.generated.resources.result_delete_dialog_confirm
import com.labellens.app.generated.resources.result_delete_dialog_message
import com.labellens.app.generated.resources.result_delete_dialog_title
import com.labellens.app.generated.resources.result_feedback_prompt
import com.labellens.app.generated.resources.result_feedback_thanks
import com.labellens.app.generated.resources.result_fix_dialog_cancel
import com.labellens.app.generated.resources.result_fix_dialog_confirm
import com.labellens.app.generated.resources.result_fix_dialog_title
import com.labellens.app.generated.resources.result_fix_misread_btn
import com.labellens.app.generated.resources.result_header_title
import com.labellens.app.generated.resources.result_loading
import com.labellens.app.generated.resources.result_section_good_to_know_title
import com.labellens.app.generated.resources.result_section_main_ingredients_title
import com.labellens.app.generated.resources.result_section_worth_noting_title
import com.labellens.app.generated.resources.result_see_label_text
import com.labellens.app.generated.resources.result_warning_cannot_rule_out
import com.labellens.app.generated.resources.result_warning_cannot_rule_out_generic
import com.labellens.app.generated.resources.result_warning_contains
import com.labellens.app.generated.resources.result_warning_contains_generic
import com.labellens.app.generated.resources.result_warning_for_you_label
import com.labellens.app.presentation.navigation.ResultScreenRoute
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

data class PlainResultRowModel(
    val title: String,
    val value: String? = null,
    val compare: String? = null,
    val percentValue: Int? = null,
    val detail: String = "",
    val ingredientTarget: String = "",
    val severity: FlagSeverity = FlagSeverity.GOOD_TO_KNOW,
    val isPersonalWarning: Boolean = false,
)

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

    // Build row models for Section 5 (Good to know) and Section 6 (Worth noting)
    val goodToKnowRows = remember(result, state.language) {
        buildGoodToKnowRows(result, state.language)
    }

    val worthNotingRows = remember(result, state.language) {
        buildWorthNotingRows(result, state.language)
    }

    val mainIngredients = remember(result.fullBreakdown) {
        result.fullBreakdown.filter { it.canonicalName.isNotBlank() }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Top Header Bar: Back arrow + "Phân tích nhãn" / "Label analysis"
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

        // 2. Product Card: photo thumbnail (or category icon), product name, short subtitle
        item {
            ProductCard(
                result = result,
                language = state.language,
            )
        }

        // 3. Verdict Card (light green, leaf icon): ONE short plain sentence
        item {
            VerdictCard(
                result = result,
                language = state.language,
            )
        }

        // 4. Personal Warning Banner (light red, warning icon): shown ONLY for CONTAINS_YOURS and CANNOT_RULE_OUT
        val fitState = result.fitHeader?.state
        if (fitState == ProfileFitState.CONTAINS_YOURS || fitState == ProfileFitState.CANNOT_RULE_OUT) {
            item {
                PersonalWarningBanner(
                    fitHeader = result.fitHeader,
                    fitState = fitState,
                    onFixClick = { viewModel.openFixDialog() },
                )
            }
        }

        // Supplement Education Disclaimer (stays exactly as-is for supplement category)
        if (result.category == ProductCategory.SUPPLEMENT) {
            item {
                SupplementDisclaimerCard(language = state.language)
            }
        }

        // 5. Section "Điểm tốt" / "Good to know"
        if (goodToKnowRows.isNotEmpty()) {
            item {
                SectionTitle(text = stringResource(Res.string.result_section_good_to_know_title))
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEBE6DC)),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        goodToKnowRows.forEachIndexed { index, row ->
                            PlainResultRow(
                                item = row,
                                onClick = { onNavigateToIngredientDetail(row.ingredientTarget) },
                            )
                            if (index < goodToKnowRows.size - 1) {
                                HorizontalDivider(color = Color(0xFFF2F4EF), thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }

        // 6. Section "Cần để ý" / "Worth noting"
        if (worthNotingRows.isNotEmpty()) {
            item {
                SectionTitle(text = stringResource(Res.string.result_section_worth_noting_title))
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEBE6DC)),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        worthNotingRows.forEachIndexed { index, row ->
                            PlainResultRow(
                                item = row,
                                onClick = { onNavigateToIngredientDetail(row.ingredientTarget) },
                            )
                            if (index < worthNotingRows.size - 1) {
                                HorizontalDivider(color = Color(0xFFF2F4EF), thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }

        // 7. Section "Thành phần chính" / "Main ingredients"
        if (mainIngredients.isNotEmpty()) {
            item {
                SectionTitle(text = stringResource(Res.string.result_section_main_ingredients_title))
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEBE6DC)),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        mainIngredients.forEachIndexed { index, ing ->
                            val gloss = ing.plainExplanation.trim()
                            val isDefaultGloss = gloss.isBlank() ||
                                gloss.contains("standard knowledge base", ignoreCase = true) ||
                                gloss.contains("cơ sở dữ liệu", ignoreCase = true)
                            val displayText = if (!isDefaultGloss) {
                                "${ing.canonicalName} — $gloss"
                            } else {
                                ing.canonicalName
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onNavigateToIngredientDetail(ing.canonicalName) }
                                    .padding(vertical = 4.dp, horizontal = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = displayText,
                                    style = AppTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        color = AppTheme.colors.text.primary,
                                    ),
                                    modifier = Modifier.weight(1f),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = AppTheme.colors.text.secondary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp),
                                )
                            }

                            if (index < mainIngredients.size - 1) {
                                HorizontalDivider(color = Color(0xFFF4F5F1), thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }

        // Expandable Raw Label Text
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleLabelTextExpansion() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFEBE6DC)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.result_see_label_text),
                            style = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.text.primary,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(if (state.isLabelTextExpanded) Res.string.result_action_collapse else Res.string.result_action_expand),
                            style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.primary, fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            softWrap = false,
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

        // 1-Tap Feedback Control
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8F5)),
                border = BorderStroke(1.dp, Color(0xFFEBE6DC)),
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

        // 8. Bottom actions: Saved to History & Compare
        item {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.isSaved) {
                    val savedDesc = stringResource(Res.string.result_btn_saved_history_desc)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .defaultMinSize(minHeight = 48.dp)
                            .semantics(mergeDescendants = true) {
                                contentDescription = savedDesc
                            }
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
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
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

    // Misread Label Fix Dialog
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
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = AppTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = AppTheme.colors.text.primary,
        ),
        modifier = Modifier.padding(top = 4.dp),
    )
}

/**
 * 2. Product Card: photo thumbnail or category icon, product name, short subtitle
 */
@Composable
private fun ProductCard(
    result: LabelAnalysisResult,
    language: AppLanguage,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Color(0xFFEBE6DC)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Category Icon Thumbnail in tinted rounded box
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEDF2ED)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(
                        when (result.category) {
                            ProductCategory.SUPPLEMENT -> UiRes.drawable.ic_beaker_amber
                            ProductCategory.COSMETICS -> UiRes.drawable.ic_leaf_amber
                            else -> UiRes.drawable.ic_leaf_logo
                        },
                    ),
                    contentDescription = null,
                    tint = when (result.category) {
                        ProductCategory.SUPPLEMENT, ProductCategory.COSMETICS -> Color(0xFFB7791F)
                        else -> AppTheme.colors.primary
                    },
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ProductNameSanitizer.displayProductName(result.productName, language),
                    style = AppTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.text.primary,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                // Subtitle: honest short subtitle (e.g. "Không đường" / "No added sugar", productType, or calm descriptor)
                val subtitle = determineProductSubtitle(result, language)
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = AppTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = AppTheme.colors.text.secondary,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private fun determineProductSubtitle(result: LabelAnalysisResult, language: AppLanguage): String {
    // Check if zero/no added sugar is notable
    val hasNoSugar = result.goodToKnow.any {
        it.title.contains("không đường", ignoreCase = true) ||
            it.title.contains("no added sugar", ignoreCase = true) ||
            it.title.contains("0g sugar", ignoreCase = true)
    }
    if (hasNoSugar) {
        return if (language == AppLanguage.VIETNAMESE) "Không thêm đường" else "No added sugar"
    }

    // Check quickGlance if it's not the old ingredient count line
    val qg = result.quickGlance.orEmpty().trim()
    if (qg.isNotBlank() &&
        !qg.contains("analysed", ignoreCase = true) &&
        !qg.contains("phân tích", ignoreCase = true) &&
        !qg.contains("ingredients", ignoreCase = true)
    ) {
        return qg
    }

    if (!result.productType.isNullOrBlank()) {
        return result.productType
    }

    return when (result.category) {
        ProductCategory.FOOD -> if (language == AppLanguage.VIETNAMESE) "Thực phẩm đóng gói" else "Packaged food"
        ProductCategory.BEVERAGE -> if (language == AppLanguage.VIETNAMESE) "Đồ uống" else "Beverage"
        ProductCategory.SUPPLEMENT -> if (language == AppLanguage.VIETNAMESE) "Thực phẩm bổ sung" else "Dietary supplement"
        ProductCategory.COSMETICS -> if (language == AppLanguage.VIETNAMESE) "Mỹ phẩm / Chăm sóc cá nhân" else "Cosmetics / Personal care"
    }
}

/**
 * 3. Verdict Card (light green, leaf icon): ONE short plain sentence. No model ids, no tier text, no scores.
 */
@Composable
private fun VerdictCard(
    result: LabelAnalysisResult,
    language: AppLanguage,
) {
    val verdictSentence = remember(result.verdict, language) {
        val v = result.verdict.trim()
        if (v.isNotBlank() && !v.equals("Verdict", ignoreCase = true)) {
            v
        } else {
            ScanSummaryLocalizer.localizedSummary(result, language)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFEDF2ED),
        border = BorderStroke(1.dp, Color(0xFFC3DBC8)),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(UiRes.drawable.ic_leaf_logo),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = verdictSentence,
                style = AppTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F5A37),
                    lineHeight = 21.sp,
                ),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * 4. Personal Warning Banner (light red, warning icon, small label "Cảnh báo cho bạn" / "Warning for you"):
 * ONE short line derived from the fit state. Shown ONLY for CONTAINS_YOURS and CANNOT_RULE_OUT.
 */
@Composable
private fun PersonalWarningBanner(
    fitHeader: com.labellens.app.domain.model.FitHeaderInfo?,
    fitState: ProfileFitState,
    onFixClick: () -> Unit,
) {
    val matchedTerm = fitHeader?.matchedTerms?.firstOrNull()?.trim().orEmpty()

    val warningLine = when (fitState) {
        ProfileFitState.CONTAINS_YOURS -> {
            if (matchedTerm.isNotBlank()) {
                stringResource(Res.string.result_warning_contains, matchedTerm)
            } else {
                stringResource(Res.string.result_warning_contains_generic)
            }
        }

        ProfileFitState.CANNOT_RULE_OUT -> {
            if (matchedTerm.isNotBlank()) {
                stringResource(Res.string.result_warning_cannot_rule_out, matchedTerm)
            } else {
                stringResource(Res.string.result_warning_cannot_rule_out_generic)
            }
        }

        else -> ""
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFFDECEA),
        border = BorderStroke(1.dp, Color(0xFFF5C2C7)),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB3261E)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.result_warning_for_you_label),
                    style = AppTheme.typography.bodyExtraSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB3261E),
                        letterSpacing = 0.5.sp,
                    ),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = warningLine,
                    style = AppTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7A130D),
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                    ),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onFixClick() },
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = stringResource(Res.string.result_fix_misread_btn),
                    style = AppTheme.typography.bodyExtraSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB3261E),
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun SupplementDisclaimerCard(language: AppLanguage) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFBF3DF),
        border = BorderStroke(1.dp, Color(0xFFF5E3C0)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(UiRes.drawable.ic_beaker_amber),
                contentDescription = null,
                tint = Color(0xFFB7791F),
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = AnalysisStrings.verdictDisclaimer(language),
                style = AppTheme.typography.bodyExtraSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF7A4D05),
                    lineHeight = 16.sp,
                ),
            )
        }
    }
}

/**
 * 5 & 6. Row item pattern:
 * icon in tinted circle + short title (label) + value chip + everyday comparison line (with progress bar for %) + chevron
 */
@Composable
private fun PlainResultRow(
    item: PlainResultRowModel,
    onClick: () -> Unit,
) {
    val (circleBg, iconColor, iconVector) = when {
        item.isPersonalWarning -> Triple(Color(0xFFFDECEA), Color(0xFFB3261E), Icons.Default.Close)

        item.severity == FlagSeverity.WORTH_NOTING || item.severity == FlagSeverity.PERSONAL_AVOID ->
            Triple(Color(0xFFFBF3DF), Color(0xFFB7791F), Icons.Default.Warning)

        else -> Triple(Color(0xFFEDF2ED), AppTheme.colors.primary, Icons.Default.Check)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon in tinted circle
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(circleBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title + comparison line (+ progress bar)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = AppTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = AppTheme.colors.text.primary,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (!item.compare.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.compare,
                    style = AppTheme.typography.bodyExtraSmall.copy(
                        fontWeight = FontWeight.Normal,
                        color = AppTheme.colors.text.secondary,
                        fontSize = 12.sp,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Slim progress bar for percentage comparisons (cap 0..100)
                if (item.percentValue != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val fraction = (item.percentValue / 100f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFE2E4DE)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction)
                                .clip(RoundedCornerShape(2.dp))
                                .background(iconColor),
                        )
                    }
                }
            }
        }

        // Value chip (e.g. "0 g", "36 mg", "3,1 g")
        if (!item.value.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF2F4EF))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = item.value,
                    style = AppTheme.typography.bodyExtraSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.text.primary,
                        fontSize = 12.sp,
                    ),
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Chevron
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = AppTheme.colors.text.secondary.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp),
        )
    }
}

/**
 * Builds rows for Section 5: "Điểm tốt" / "Good to know"
 */
private fun buildGoodToKnowRows(result: LabelAnalysisResult, language: AppLanguage): List<PlainResultRowModel> {
    val rows = mutableListOf<PlainResultRowModel>()
    val seenKeys = mutableSetOf<String>()

    // 1. From goodToKnow
    for (item in result.goodToKnow) {
        val key = item.title.lowercase().trim()
        if (key.isNotBlank() && seenKeys.add(key)) {
            val compareVal = item.compare?.ifBlank { null }
                ?: LocalComparisonHelper.computeComparison(item.title, item.value, language).first
            val pct = LocalComparisonHelper.parsePercentage(compareVal)

            rows.add(
                PlainResultRowModel(
                    title = item.title,
                    value = item.value?.ifBlank { null },
                    compare = compareVal,
                    percentValue = pct,
                    detail = item.detail,
                    ingredientTarget = item.relatedIngredient?.ifBlank { item.title } ?: item.title,
                    severity = FlagSeverity.GOOD_TO_KNOW,
                    isPersonalWarning = false,
                ),
            )
        }
    }

    // 2. From nutritionHighlights when local analysis didn't provide AI goodToKnow
    if (result.goodToKnow.isEmpty()) {
        for (fact in result.nutritionHighlights) {
            val norm = fact.name.lowercase().trim()
            val isPositive = norm.contains("dam") || norm.contains("protein") ||
                norm.contains("canxi") || norm.contains("calcium") ||
                norm.contains("fiber") || norm.contains("xo") ||
                ((norm.contains("sugar") || norm.contains("duong")) && fact.amount.startsWith("0"))
            if (isPositive) {
                val displayName = AnalysisStrings.nutritionName(language, fact.name)
                val key = displayName.lowercase().trim()
                if (seenKeys.add(key)) {
                    val (comp, pct) = LocalComparisonHelper.computeComparison(displayName, fact.amount, language)
                    rows.add(
                        PlainResultRowModel(
                            title = displayName,
                            value = fact.amount,
                            compare = comp ?: fact.secondaryDetail,
                            percentValue = pct ?: LocalComparisonHelper.parsePercentage(fact.secondaryDetail),
                            detail = fact.name,
                            ingredientTarget = fact.name,
                            severity = FlagSeverity.GOOD_TO_KNOW,
                        ),
                    )
                }
            }
        }
    }

    return rows
}

/**
 * Builds rows for Section 6: "Cần để ý" / "Worth noting"
 */
private fun buildWorthNotingRows(result: LabelAnalysisResult, language: AppLanguage): List<PlainResultRowModel> {
    val rows = mutableListOf<PlainResultRowModel>()
    val seenKeys = mutableSetOf<String>()

    // 1. Personal warnings from watchOutForYou (styled RED)
    for (item in result.watchOutForYou) {
        val key = item.title.lowercase().trim()
        if (key.isNotBlank() && seenKeys.add(key)) {
            val compareVal = item.compare?.ifBlank { null }
                ?: LocalComparisonHelper.computeComparison(item.title, item.value, language).first
            val pct = LocalComparisonHelper.parsePercentage(compareVal)

            rows.add(
                PlainResultRowModel(
                    title = item.title,
                    value = item.value?.ifBlank { null },
                    compare = compareVal,
                    percentValue = pct,
                    detail = item.detail,
                    ingredientTarget = item.relatedIngredient?.ifBlank { item.title } ?: item.title,
                    severity = item.severity,
                    isPersonalWarning = true,
                ),
            )
        }
    }

    // 2. Notable items from worthNoting (styled AMBER)
    for (item in result.worthNoting) {
        val key = item.title.lowercase().trim()
        if (key.isNotBlank() && seenKeys.add(key)) {
            val compareVal = item.compare?.ifBlank { null }
                ?: LocalComparisonHelper.computeComparison(item.title, item.value, language).first
            val pct = LocalComparisonHelper.parsePercentage(compareVal)

            rows.add(
                PlainResultRowModel(
                    title = item.title,
                    value = item.value?.ifBlank { null },
                    compare = compareVal,
                    percentValue = pct,
                    detail = item.detail,
                    ingredientTarget = item.relatedIngredient?.ifBlank { item.title } ?: item.title,
                    severity = FlagSeverity.WORTH_NOTING,
                    isPersonalWarning = false,
                ),
            )
        }
    }

    // 3. From nutritionHighlights when local analysis didn't provide AI worthNoting
    if (result.worthNoting.isEmpty()) {
        for (fact in result.nutritionHighlights) {
            val norm = fact.name.lowercase().trim()
            val isNotable = ((norm.contains("sugar") || norm.contains("duong")) && !fact.amount.startsWith("0")) ||
                norm.contains("sodium") || norm.contains("natri") ||
                norm.contains("fat") || norm.contains("chat beo")
            if (isNotable) {
                val displayName = AnalysisStrings.nutritionName(language, fact.name)
                val key = displayName.lowercase().trim()
                if (seenKeys.add(key)) {
                    val (comp, pct) = LocalComparisonHelper.computeComparison(displayName, fact.amount, language)
                    rows.add(
                        PlainResultRowModel(
                            title = displayName,
                            value = fact.amount,
                            compare = comp ?: fact.secondaryDetail,
                            percentValue = pct ?: LocalComparisonHelper.parsePercentage(fact.secondaryDetail),
                            detail = fact.name,
                            ingredientTarget = fact.name,
                            severity = FlagSeverity.WORTH_NOTING,
                        ),
                    )
                }
            }
        }
    }

    return rows
}
