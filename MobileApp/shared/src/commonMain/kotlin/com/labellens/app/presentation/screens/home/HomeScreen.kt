package com.labellens.app.presentation.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.labellens.app.data.repository.UserProfileRepository
import com.labellens.app.designsystem.generated.resources.UiRes
import com.labellens.app.designsystem.generated.resources.ic_barcode_card
import com.labellens.app.designsystem.generated.resources.ic_bell
import com.labellens.app.designsystem.generated.resources.ic_camera
import com.labellens.app.designsystem.generated.resources.ic_document_card
import com.labellens.app.designsystem.generated.resources.ic_leaf_logo
import com.labellens.app.designsystem.theme.AppTheme
import com.labellens.app.designsystem.util.PreviewHelper
import com.labellens.app.domain.analyzer.ProductNameSanitizer
import com.labellens.app.domain.analyzer.ScanSummaryLocalizer
import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.BuiltInSamples
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.SampleProduct
import com.labellens.app.generated.resources.Res
import com.labellens.app.generated.resources.hero_grocery_bag
import com.labellens.app.generated.resources.home_greeting_afternoon
import com.labellens.app.generated.resources.home_greeting_evening
import com.labellens.app.generated.resources.home_greeting_morning
import com.labellens.app.generated.resources.home_greeting_subtitle
import com.labellens.app.generated.resources.home_quick_action_barcode
import com.labellens.app.generated.resources.home_quick_action_paste
import com.labellens.app.generated.resources.home_quick_action_photo
import com.labellens.app.generated.resources.home_recent_scans_empty
import com.labellens.app.generated.resources.home_recent_scans_title
import com.labellens.app.generated.resources.home_samples_title
import com.labellens.app.generated.resources.home_scan_card_subtitle
import com.labellens.app.generated.resources.home_scan_card_title
import com.labellens.app.generated.resources.home_see_all
import com.labellens.app.generated.resources.sample_cereal
import com.labellens.app.generated.resources.sample_shampoo
import com.labellens.app.generated.resources.sample_yogurt
import com.labellens.app.generated.resources.scan_card_packet
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.time.Clock

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToResult: (sampleId: String?, rawText: String?, name: String?) -> Unit,
    onNavigateToHistoryItem: (result: LabelAnalysisResult) -> Unit,
    onNavigateToHistory: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val userProfileRepository = koinInject<UserProfileRepository>()
    val language by userProfileRepository.languageFlow.collectAsState()
    val state: LabelLensHomeUiState by viewModel.uiState.collectAsState()
    HomeScreen(
        state = state,
        language = language,
        onNavigateToScan = onNavigateToScan,
        onNavigateToResult = onNavigateToResult,
        onNavigateToHistoryItem = onNavigateToHistoryItem,
        onNavigateToHistory = onNavigateToHistory,
        modifier = modifier,
    )
}

@Composable
fun HomeScreen(
    state: LabelLensHomeUiState,
    language: AppLanguage = AppLanguage.DEFAULT,
    onNavigateToScan: () -> Unit,
    onNavigateToResult: (sampleId: String?, rawText: String?, name: String?) -> Unit,
    onNavigateToHistoryItem: (result: LabelAnalysisResult) -> Unit,
    onNavigateToHistory: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val currentHour = remember {
        runCatching {
            val epochSec = Clock.System.now().toEpochMilliseconds() / 1000L
            val instant = Instant.fromEpochSeconds(epochSec)
            instant.toLocalDateTime(TimeZone.currentSystemDefault()).hour
        }.getOrNull() ?: 14
    }

    val greetingRes = when (currentHour) {
        in 5..11 -> Res.string.home_greeting_morning
        in 12..17 -> Res.string.home_greeting_afternoon
        else -> Res.string.home_greeting_evening
    }

    // Prioritize sample products with dedicated preview assets
    val orderedSamples = remember(state.sampleProducts) {
        val sample1 = state.sampleProducts.firstOrNull { it.id == "sample_honey_oats" } ?: BuiltInSamples.sample1
        val sample3 = state.sampleProducts.firstOrNull { it.id == "sample_matcha_biscuits" } ?: BuiltInSamples.sample3
        val sample8 = state.sampleProducts.firstOrNull { it.id == "sample_clony_shampoo" } ?: BuiltInSamples.sample8
        val featured = listOf(sample1, sample3, sample8)
        val others = state.sampleProducts.filterNot { it.id in listOf(sample1.id, sample3.id, sample8.id) }
        featured + others
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // 1. App Header: Logo Mark + LabelLens + Notification Bell
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppTheme.colors.scanCard),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(UiRes.drawable.ic_leaf_logo),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "LabelLens",
                        style = AppTheme.typography.titleLarge.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.text.primary,
                        ),
                    )
                }

                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { /* Visual only */ },
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, Color(0xFFEBE6DC)),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(UiRes.drawable.ic_bell),
                            contentDescription = null,
                            tint = AppTheme.colors.text.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        // 2. Greeting Block with Hero Grocery Bag illustration on the right
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val rawGreeting = stringResource(greetingRes)
                    val greetingText = rawGreeting
                        .replace(" ☀️", "\u00A0☀️")
                        .replace(" ✌", "\u00A0✌")
                        .replace(" 🌙", "\u00A0🌙")
                    Text(
                        text = greetingText,
                        style = AppTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.text.primary,
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(Res.string.home_greeting_subtitle),
                        style = AppTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            color = AppTheme.colors.text.secondary,
                            lineHeight = 20.sp,
                        ),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = painterResource(Res.drawable.hero_grocery_bag),
                    contentDescription = null,
                    modifier = Modifier.width(124.dp).height(100.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }

        // 3. Oversized deep-green Scan Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToScan() },
                shape = RoundedCornerShape(24.dp),
                color = AppTheme.colors.scanCard,
                shadowElevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.scan_card_packet),
                        contentDescription = null,
                        modifier = Modifier.size(92.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.home_scan_card_title),
                            style = AppTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 19.sp,
                            ),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(Res.string.home_scan_card_subtitle),
                            style = AppTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.92f),
                                lineHeight = 17.sp,
                            ),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = Color.White,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = AppTheme.colors.scanCard,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }

        // 4. Quick Actions Row (3 White Cards)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                QuickActionCard(
                    label = stringResource(Res.string.home_quick_action_photo),
                    iconRes = UiRes.drawable.ic_camera,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToScan,
                )
                QuickActionCard(
                    label = stringResource(Res.string.home_quick_action_barcode),
                    iconRes = UiRes.drawable.ic_barcode_card,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToScan,
                )
                QuickActionCard(
                    label = stringResource(Res.string.home_quick_action_paste),
                    iconRes = UiRes.drawable.ic_document_card,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToScan,
                )
            }
        }

        // 5. "Thử mẫu có sẵn" Horizontal Cards with Photo Tops
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.home_samples_title),
                    style = AppTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AppTheme.colors.text.primary,
                    ),
                )
                Row(
                    modifier = Modifier.clickable { onNavigateToScan() },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.home_see_all),
                        style = AppTheme.typography.bodySmall.copy(
                            color = AppTheme.colors.text.secondary,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = AppTheme.colors.text.secondary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(orderedSamples) { sample ->
                    SampleProductCard(
                        sample = sample,
                        onClick = {
                            onNavigateToResult(sample.id, sample.fullLabelText, sample.name)
                        },
                    )
                }
            }
        }

        // 6. "Gần đây" List Rows
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.home_recent_scans_title),
                    style = AppTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AppTheme.colors.text.primary,
                    ),
                )
                Row(
                    modifier = Modifier.clickable { onNavigateToHistory() },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.home_see_all),
                        style = AppTheme.typography.bodySmall.copy(
                            color = AppTheme.colors.text.secondary,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = AppTheme.colors.text.secondary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (state.recentScans.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFEBE6DC)),
                ) {
                    Text(
                        text = stringResource(Res.string.home_recent_scans_empty),
                        style = AppTheme.typography.bodyMedium.copy(
                            color = AppTheme.colors.text.secondary,
                        ),
                        modifier = Modifier.padding(16.dp),
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.recentScans.forEach { scan ->
                        RecentScanRow(
                            scan = scan,
                            language = language,
                            onClick = { onNavigateToHistoryItem(scan) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    label: String,
    iconRes: DrawableResource,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFEBE6DC)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = AppTheme.colors.primary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = AppTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = AppTheme.colors.text.primary,
                ),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SampleProductCard(
    sample: SampleProduct,
    onClick: () -> Unit,
) {
    val photoRes = when (sample.id) {
        "sample_honey_oats" -> Res.drawable.sample_cereal
        "sample_matcha_biscuits" -> Res.drawable.sample_yogurt
        "sample_clony_shampoo" -> Res.drawable.sample_shampoo
        else -> null
    }

    Surface(
        modifier = Modifier
            .width(148.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFEBE6DC)),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (photoRes != null) {
                Image(
                    painter = painterResource(photoRes),
                    contentDescription = sample.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(105.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(105.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(AppTheme.colors.alternative),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(UiRes.drawable.ic_leaf_logo),
                        contentDescription = null,
                        tint = AppTheme.colors.primary,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            ) {
                Text(
                    text = sample.name,
                    fontWeight = FontWeight.Bold,
                    style = AppTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = AppTheme.colors.text.primary,
                        lineHeight = 18.sp,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RecentScanRow(
    scan: LabelAnalysisResult,
    language: AppLanguage,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFEBE6DC)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppTheme.colors.alternative),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.scan_card_packet),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Fit,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ProductNameSanitizer.displayProductName(scan.productName, language),
                    fontWeight = FontWeight.Bold,
                    style = AppTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                    color = AppTheme.colors.text.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = ScanSummaryLocalizer.localizedSummary(scan, language),
                    style = AppTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = AppTheme.colors.text.secondary,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = AppTheme.colors.text.secondary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Preview
@Composable
internal fun HomeScreenPreview() {
    PreviewHelper {
        HomeScreen(
            state = LabelLensHomeUiState(),
            onNavigateToScan = {},
            onNavigateToResult = { _, _, _ -> },
            onNavigateToHistoryItem = {},
            onNavigateToHistory = {},
        )
    }
}
