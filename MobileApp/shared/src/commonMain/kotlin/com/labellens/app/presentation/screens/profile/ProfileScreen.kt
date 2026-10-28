package com.labellens.app.presentation.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
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
import com.labellens.app.designsystem.components.AppButton
import com.labellens.app.designsystem.components.ButtonStyle
import com.labellens.app.designsystem.theme.AppTheme
import com.labellens.app.domain.model.Allergen
import com.labellens.app.domain.model.AvoidPreference
import com.labellens.app.domain.model.DietaryGoal

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
                text = "My Profile & Context",
                style = AppTheme.typography.headlineLarge.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.text.primary,
                ),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Personalize your safety alerts. All saved scans immediately re-flag when you update your settings.",
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
                            text = "LabelLens Plus (Demo)",
                            style = AppTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.primary,
                            ),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Safety warnings are always free. Upgrade for unlimited photo OCR analysis.",
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.text.secondary,
                        )
                    }
                    AppButton(
                        text = "View",
                        size = com.labellens.app.designsystem.components.ButtonSize.EXTRA_SMALL,
                        onClick = onNavigateToPaywall,
                    )
                }
            }
        }

        // Section 1: Allergies (Red flag alert)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "My Allergies",
                        style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFB3261E),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Strict personal alerts. Highlights in red whenever detected on a label.",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Allergen.entries.forEach { allergen ->
                            val isSelected = profile.allergies.contains(allergen)
                            FilterToggleChip(
                                label = allergen.displayName,
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

        // Section 2: Avoid List (Amber alert)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Ingredients to Avoid",
                        style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFB7791F),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Flagged under 'Watch out for you' with clear context.",
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
                                label = avoid.displayName,
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

        // Section 3: Dietary Goals
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Dietary Goals & Cautions",
                        style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.colors.primary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tailors the verdict and nutrition breakdown to your health focus.",
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
                                label = goal.displayName,
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

        // Section 4: Privacy & Settings Link
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
                            text = "Settings, Privacy & Disclaimers",
                            style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = AppTheme.colors.text.primary,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "On-device processing details, telemetry toggle, and medical disclaimer.",
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.text.secondary,
                        )
                    }
                    Text(
                        text = "Open ->",
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.primary,
                        style = AppTheme.typography.bodySmall,
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
                        modifier = Modifier.size(12.dp),
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = AppTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) activeColor else AppTheme.colors.text.primary,
                ),
            )
        }
    }
}
