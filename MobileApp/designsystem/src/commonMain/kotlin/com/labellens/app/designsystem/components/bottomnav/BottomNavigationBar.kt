package com.labellens.app.designsystem.components.bottomnav

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.labellens.app.designsystem.generated.resources.UiRes
import com.labellens.app.designsystem.generated.resources.bottom_nav_label_favorites
import com.labellens.app.designsystem.generated.resources.bottom_nav_label_home
import com.labellens.app.designsystem.generated.resources.bottom_nav_label_profile
import com.labellens.app.designsystem.generated.resources.ic_favorite
import com.labellens.app.designsystem.generated.resources.ic_home
import com.labellens.app.designsystem.generated.resources.ic_profile
import com.labellens.app.designsystem.theme.AppTheme
import com.labellens.app.designsystem.util.PreviewHelper
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    items: List<BottomNavItem>,
    selectedIndex: Int = 0,
    onClickItem: (BottomNavItem) -> Unit,
) {
    val isFiveItems = items.size == 5
    val centerIndex = items.size / 2

    if (!isFiveItems) {
        // Fallback standard navigation bar for non-5 item layouts
        NavigationBar(
            modifier = modifier,
            containerColor = AppTheme.colors.bottomNav.background,
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index
                NavigationBarItem(
                    icon = {
                        item.icon?.let {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(it),
                                contentDescription = stringResource(item.label),
                            )
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(item.label),
                            style = AppTheme.typography.bodyExtraSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppTheme.colors.bottomNav.selectedTextIcon,
                        selectedTextColor = AppTheme.colors.bottomNav.selectedTextIcon,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = AppTheme.colors.bottomNav.unselectedTextIcon,
                        unselectedTextColor = AppTheme.colors.bottomNav.unselectedTextIcon,
                    ),
                    selected = isSelected,
                    enabled = item.icon != null,
                    onClick = { onClickItem(item) },
                )
            }
        }
        return
    }

    // Tokens v2: white surface with center Scan action raised button overlapping the bar
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(68.dp),
            color = AppTheme.colors.bottomNav.background,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, Color(0xFFEBE6DC)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { index, item ->
                    val isCenter = index == centerIndex
                    val isSelected = selectedIndex == index

                    if (isCenter) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onClickItem(item) }
                                .padding(bottom = 6.dp),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            Text(
                                text = stringResource(item.label),
                                style = AppTheme.typography.bodyExtraSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AppTheme.colors.primary else AppTheme.colors.bottomNav.unselectedTextIcon,
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onClickItem(item) }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            item.icon?.let { iconRes ->
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(iconRes),
                                    contentDescription = stringResource(item.label),
                                    tint = if (isSelected) AppTheme.colors.primary else AppTheme.colors.bottomNav.unselectedTextIcon,
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(item.label),
                                style = AppTheme.typography.bodyExtraSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AppTheme.colors.primary else AppTheme.colors.bottomNav.unselectedTextIcon,
                            )
                        }
                    }
                }
            }
        }

        // Center raised circular green button
        val centerItem = items[centerIndex]
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(54.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.scanCard)
                .clickable { onClickItem(centerItem) },
            contentAlignment = Alignment.Center,
        ) {
            centerItem.icon?.let { iconRes ->
                Icon(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(iconRes),
                    contentDescription = stringResource(centerItem.label),
                    tint = Color.White,
                )
            }
        }
    }
}

@Preview
@Composable
internal fun BottomNavigationBarPreview() {
    PreviewHelper {
        val bottomNavItems =
            listOf(
                BottomNavItem(
                    label = UiRes.string.bottom_nav_label_home,
                    icon = UiRes.drawable.ic_home,
                ),
                BottomNavItem(
                    label = UiRes.string.bottom_nav_label_favorites,
                    icon = UiRes.drawable.ic_favorite,
                ),
                BottomNavItem(
                    label = UiRes.string.bottom_nav_label_profile,
                    icon = UiRes.drawable.ic_profile,
                ),
            )
        var currentSelectedIndex by remember { mutableStateOf(0) }
        BottomNavigationBar(
            selectedIndex = currentSelectedIndex,
            items = bottomNavItems,
            onClickItem = { clickedItem ->
                currentSelectedIndex =
                    bottomNavItems.indexOfFirst {
                        it.label == clickedItem.label
                    }
            },
        )
    }
}
