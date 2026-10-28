package com.labellens.app.presentation.screens.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.labellens.app.designsystem.components.AppCardContainer
import com.labellens.app.designsystem.components.ButtonStyle
import com.labellens.app.designsystem.theme.AppTheme
import com.labellens.app.domain.model.BuiltInSamples
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.SampleProduct
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToResult: (sampleId: String?, rawText: String?, name: String?) -> Unit,
    onNavigateToHistoryItem: (result: LabelAnalysisResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state: LabelLensHomeUiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // App Header
        item {
            Column {
                Text(
                    text = "LabelLens",
                    style = AppTheme.typography.headlineLarge.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.text.primary,
                    ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Understand the label. In seconds.",
                    style = AppTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        color = AppTheme.colors.text.secondary,
                    ),
                )
            }
        }

        // Oversized Hero Scan Card
        item {
            AppCardContainer(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = AppTheme.colors.primary,
                shape = RoundedCornerShape(20.dp),
                onClick = onNavigateToScan,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                ) {
                    Text(
                        text = "Scan Any Label",
                        style = AppTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        ),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Point camera at food or supplement packaging. Plain-language insights, zero fear.",
                        style = AppTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f),
                        ),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Start Scan",
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.primary,
                            style = AppTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Scan",
                            tint = AppTheme.colors.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Text(
                text = "Quick Actions",
                style = AppTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.text.primary,
                ),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                QuickActionChip(
                    label = "Barcode",
                    iconName = "Barcode",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToScan,
                )
                QuickActionChip(
                    label = "Photo",
                    iconName = "Photo",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToScan,
                )
                QuickActionChip(
                    label = "Paste Text",
                    iconName = "Text",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToScan,
                )
            }
        }

        // Built-in Samples Carousel
        item {
            Text(
                text = "Try Built-in Samples",
                style = AppTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.text.primary,
                ),
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.sampleProducts) { sample ->
                    SampleProductCard(
                        sample = sample,
                        onClick = {
                            onNavigateToResult(sample.id, sample.fullLabelText, sample.name)
                        },
                    )
                }
            }
        }

        // Calm Tip Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFBF3DF), // Amber container
                ),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Tip",
                        tint = Color(0xFFB7791F),
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Calm Tip",
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB7791F),
                            style = AppTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = state.calmTip,
                            style = AppTheme.typography.bodySmall.copy(
                                color = AppTheme.colors.text.secondary,
                            ),
                        )
                    }
                }
            }
        }

        // Recent Scans
        item {
            Text(
                text = "Recent Scans",
                style = AppTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.text.primary,
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (state.recentScans.isEmpty()) {
                Text(
                    text = "No scans yet. Try a sample or scan a label above!",
                    style = AppTheme.typography.bodyMedium.copy(
                        color = AppTheme.colors.text.secondary,
                    ),
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.recentScans.forEach { scan ->
                        RecentScanRow(
                            scan = scan,
                            onClick = { onNavigateToHistoryItem(scan) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    iconName: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = AppTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.colors.text.primary,
                ),
            )
        }
    }
}

@Composable
private fun SampleProductCard(
    sample: SampleProduct,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF),
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = sample.name,
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = sample.frontDescription,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Tap to test ->",
                style = AppTheme.typography.bodyExtraSmall.copy(
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}

@Composable
private fun RecentScanRow(
    scan: LabelAnalysisResult,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scan.productName,
                    fontWeight = FontWeight.SemiBold,
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.text.primary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = scan.verdict,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = AppTheme.colors.outline,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
