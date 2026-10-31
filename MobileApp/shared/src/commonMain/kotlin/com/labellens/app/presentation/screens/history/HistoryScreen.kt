package com.labellens.app.presentation.screens.history

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.labellens.app.designsystem.components.AppButton
import com.labellens.app.designsystem.components.ButtonStyle
import com.labellens.app.designsystem.theme.AppTheme
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.generated.resources.Res
import com.labellens.app.generated.resources.content_desc_delete
import com.labellens.app.generated.resources.history_alert_pattern
import com.labellens.app.generated.resources.history_alert_plural
import com.labellens.app.generated.resources.history_alert_single
import com.labellens.app.generated.resources.history_empty_description
import com.labellens.app.generated.resources.history_empty_favorites
import com.labellens.app.generated.resources.history_empty_scans
import com.labellens.app.generated.resources.history_filter_all
import com.labellens.app.generated.resources.history_filter_favorites
import com.labellens.app.generated.resources.history_product_plural
import com.labellens.app.generated.resources.history_product_single
import com.labellens.app.generated.resources.history_subtitle_pattern
import com.labellens.app.generated.resources.history_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateToResult: (result: LabelAnalysisResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val scans = if (state.filterFavoritesOnly) state.favoriteScans else state.allScans

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.history_title),
                        style = AppTheme.typography.headlineLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.text.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val productWord = if (scans.size != 1) stringResource(Res.string.history_product_plural) else stringResource(Res.string.history_product_single)
                    Text(
                        text = stringResource(Res.string.history_subtitle_pattern, scans.size, productWord),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                }

                AppButton(
                    text = if (state.filterFavoritesOnly) stringResource(Res.string.history_filter_all) else stringResource(Res.string.history_filter_favorites),
                    size = com.labellens.app.designsystem.components.ButtonSize.EXTRA_SMALL,
                    style = ButtonStyle.ALTERNATIVE,
                    onClick = { viewModel.toggleFavoritesFilter() },
                )
            }
        }

        if (scans.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = if (state.filterFavoritesOnly) stringResource(Res.string.history_empty_favorites) else stringResource(Res.string.history_empty_scans),
                            style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = AppTheme.colors.text.primary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(Res.string.history_empty_description),
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.text.secondary,
                        )
                    }
                }
            }
        } else {
            items(scans) { scan ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToResult(scan) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = scan.productName,
                                fontWeight = FontWeight.Bold,
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
                            if (scan.watchOutForYou.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                val alertWord = if (scan.watchOutForYou.size > 1) stringResource(Res.string.history_alert_plural) else stringResource(Res.string.history_alert_single)
                                Text(
                                    text = stringResource(Res.string.history_alert_pattern, scan.watchOutForYou.size, alertWord),
                                    style = AppTheme.typography.bodyExtraSmall.copy(
                                        color = Color(0xFFB3261E),
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.deleteItem(scan.barcode ?: scan.productName) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.content_desc_delete),
                                tint = AppTheme.colors.outline,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
