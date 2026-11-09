package com.labellens.app.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.labellens.app.designsystem.theme.AppTheme
import com.labellens.app.domain.model.Allergen
import com.labellens.app.domain.model.AttentionFlag
import com.labellens.app.domain.model.AvoidPreference
import com.labellens.app.domain.model.DietaryGoal
import com.labellens.app.domain.model.Market
import com.labellens.app.domain.model.MarketPacks
import com.labellens.app.generated.resources.Res
import com.labellens.app.generated.resources.profile_ai_assist_subtitle
import com.labellens.app.generated.resources.profile_ai_assist_title
import com.labellens.app.generated.resources.profile_allergies_subtitle
import com.labellens.app.generated.resources.profile_allergies_title
import com.labellens.app.generated.resources.profile_attention_flags_subtitle
import com.labellens.app.generated.resources.profile_attention_flags_title
import com.labellens.app.generated.resources.profile_avoid_subtitle
import com.labellens.app.generated.resources.profile_avoid_title
import com.labellens.app.generated.resources.profile_banner_btn
import com.labellens.app.generated.resources.profile_banner_subtitle
import com.labellens.app.generated.resources.profile_banner_title
import com.labellens.app.generated.resources.profile_btn_settings
import com.labellens.app.generated.resources.profile_custom_allergens_add_btn
import com.labellens.app.generated.resources.profile_custom_allergens_placeholder
import com.labellens.app.generated.resources.profile_custom_allergens_subtitle
import com.labellens.app.generated.resources.profile_custom_allergens_title
import com.labellens.app.generated.resources.profile_exceptions_subtitle
import com.labellens.app.generated.resources.profile_exceptions_title
import com.labellens.app.generated.resources.profile_goals_subtitle
import com.labellens.app.generated.resources.profile_goals_title
import com.labellens.app.generated.resources.profile_imported_mode_subtitle
import com.labellens.app.generated.resources.profile_imported_mode_title
import com.labellens.app.generated.resources.profile_market_pack_subtitle
import com.labellens.app.generated.resources.profile_market_pack_title
import com.labellens.app.generated.resources.profile_subtitle
import com.labellens.app.generated.resources.profile_title
import com.labellens.app.generated.resources.settings_privacy_description
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: LabelLensProfileViewModel,
    onNavigateToPaywall: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val profile = state.userProfile

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
                text = stringResource(Res.string.profile_title),
                style = AppTheme.typography.headlineLarge.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.text.primary,
                ),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.profile_subtitle),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
            )
        }

        // Upgrade Card entry to Paywall
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToPaywall() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF2ED)),
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.profile_banner_title),
                            style = AppTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.primary,
                            ),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(Res.string.profile_banner_subtitle),
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.text.secondary,
                        )
                    }
                    AppButton(
                        text = stringResource(Res.string.profile_banner_btn),
                        size = com.labellens.app.designsystem.components.ButtonSize.EXTRA_SMALL,
                        onClick = onNavigateToPaywall,
                    )
                }
            }
        }

        // Section 1: Market Allergen Standard & Imported Food Mode (Wave 2 - Scope 1)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(Res.string.profile_market_pack_title),
                        style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.colors.text.primary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.profile_market_pack_subtitle),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Market selector chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(Market.US, Market.EU_UK, Market.AU_NZ).forEach { market ->
                            val isSelected = profile.selectedMarket == market
                            val pack = MarketPacks.getPack(market)
                            val label = "${market.localizedName(state.selectedLanguage)} (${pack?.categories?.size ?: 0})"
                            FilterToggleChip(
                                label = label,
                                isSelected = isSelected,
                                activeColor = AppTheme.colors.primary,
                                activeBg = Color(0xFFEDF2ED),
                                onToggle = { viewModel.selectMarket(market) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Imported Food Mode Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF7F8F5))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = stringResource(Res.string.profile_imported_mode_title),
                                style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = AppTheme.colors.text.primary,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(Res.string.profile_imported_mode_subtitle),
                                style = AppTheme.typography.bodyExtraSmall,
                                color = AppTheme.colors.text.secondary,
                            )
                        }
                        Switch(
                            checked = profile.importedFoodMode,
                            onCheckedChange = { viewModel.toggleImportedFoodMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppTheme.colors.primary,
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // AI Assist Mode Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF7F8F5))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = stringResource(Res.string.profile_ai_assist_title),
                                style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = AppTheme.colors.text.primary,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(Res.string.profile_ai_assist_subtitle),
                                style = AppTheme.typography.bodyExtraSmall,
                                color = AppTheme.colors.text.secondary,
                            )
                        }
                        Switch(
                            checked = profile.aiAssistEnabled,
                            onCheckedChange = { viewModel.toggleAiAssist() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppTheme.colors.primary,
                            ),
                        )
                    }
                }
            }
        }

        // Section 2: Allergies (Red flag alert — active categories from current market pack)
        item {
            val activeCategories = MarketPacks.activeCategories(profile.selectedMarket, profile.importedFoodMode)
            val activeAllergens = activeCategories.map { it.toAllergen() }.distinct()

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(Res.string.profile_allergies_title),
                        style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFB3261E),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.profile_allergies_subtitle),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        activeAllergens.forEach { allergen ->
                            val isSelected = profile.allergies.contains(allergen)
                            FilterToggleChip(
                                label = allergen.localizedName(state.selectedLanguage),
                                isSelected = isSelected,
                                activeColor = Color(0xFFB3261E),
                                activeBg = Color(0xFFFDECEA),
                                onToggle = { viewModel.toggleAllergen(allergen) },
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Custom Allergens (Free-text with exact phrase boundary matching)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(Res.string.profile_custom_allergens_title),
                        style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFB3261E),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.profile_custom_allergens_subtitle),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = state.customAllergenInput,
                            onValueChange = { viewModel.updateCustomAllergenInput(it) },
                            placeholder = {
                                Text(
                                    text = stringResource(Res.string.profile_custom_allergens_placeholder),
                                    style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.text.secondary),
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFB3261E),
                                unfocusedBorderColor = Color(0xFFE2E4DE),
                            ),
                        )
                        AppButton(
                            text = stringResource(Res.string.profile_custom_allergens_add_btn),
                            size = com.labellens.app.designsystem.components.ButtonSize.SMALL,
                            style = ButtonStyle.PRIMARY,
                            onClick = { viewModel.addCustomAllergen() },
                        )
                    }

                    if (profile.customAllergens.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            profile.customAllergens.forEach { custom ->
                                RemovableChip(
                                    label = custom,
                                    color = Color(0xFFB3261E),
                                    backgroundColor = Color(0xFFFDECEA),
                                    onRemove = { viewModel.removeCustomAllergen(custom) },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Attention Flags v1 (Wave 2 - Scope 3)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(Res.string.profile_attention_flags_title),
                        style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFB7791F),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.profile_attention_flags_subtitle),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AttentionFlag.entries.forEach { flag ->
                            val isSelected = profile.enabledAttentionFlags.contains(flag)
                            FilterToggleChip(
                                label = flag.localizedName(state.selectedLanguage),
                                isSelected = isSelected,
                                activeColor = Color(0xFFB7791F),
                                activeBg = Color(0xFFFBF3DF),
                                onToggle = { viewModel.toggleAttentionFlag(flag) },
                            )
                        }
                    }
                }
            }
        }

        // Section 5: Personal Exceptions (Wave 2 - Scope 5)
        if (profile.ingredientExceptions.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = stringResource(Res.string.profile_exceptions_title),
                            style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.primary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(Res.string.profile_exceptions_subtitle),
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.text.secondary,
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            profile.ingredientExceptions.forEach { exc ->
                                RemovableChip(
                                    label = exc,
                                    color = AppTheme.colors.primary,
                                    backgroundColor = Color(0xFFEDF2ED),
                                    onRemove = { viewModel.removeException(exc) },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 6: Avoid List (Amber alert)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(Res.string.profile_avoid_title),
                        style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFB7791F),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.profile_avoid_subtitle),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AvoidPreference.entries.forEach { avoid ->
                            val isSelected = profile.avoidList.contains(avoid)
                            FilterToggleChip(
                                label = avoid.localizedName(state.selectedLanguage),
                                isSelected = isSelected,
                                activeColor = Color(0xFFB7791F),
                                activeBg = Color(0xFFFBF3DF),
                                onToggle = { viewModel.toggleAvoid(avoid) },
                            )
                        }
                    }
                }
            }
        }

        // Section 7: Dietary Goals
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(Res.string.profile_goals_title),
                        style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.colors.primary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.profile_goals_subtitle),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DietaryGoal.entries.forEach { goal ->
                            val isSelected = profile.goals.contains(goal)
                            FilterToggleChip(
                                label = goal.localizedName(state.selectedLanguage),
                                isSelected = isSelected,
                                activeColor = AppTheme.colors.primary,
                                activeBg = Color(0xFFEDF2ED),
                                onToggle = { viewModel.toggleGoal(goal) },
                            )
                        }
                    }
                }
            }
        }

        // Section 8: Privacy & Settings Link
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToSettings() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.profile_btn_settings),
                            style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = AppTheme.colors.text.primary,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(Res.string.settings_privacy_description),
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.text.secondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "→",
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.primary,
                        style = AppTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterToggleChip(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    activeBg: Color,
    onToggle: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) activeBg else Color(0xFFF7F8F5))
            .clickable { onToggle() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(activeColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp),
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = AppTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) activeColor else AppTheme.colors.text.primary,
                ),
            )
        }
    }
}

@Composable
private fun RemovableChip(
    label: String,
    color: Color,
    backgroundColor: Color,
    onRemove: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = AppTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                ),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f))
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(10.dp),
                )
            }
        }
    }
}
