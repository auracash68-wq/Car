package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerSaveData
import com.example.game.LevelManager
import com.example.model.ShopIconType
import com.example.model.ShopProduct
import com.example.model.ShopTab
import com.example.ui.components.GameHeader
import com.example.ui.components.NeonPanel
import com.example.ui.components.RacingBackdropCanvas
import com.example.ui.components.RacingButton
import com.example.ui.components.RacingButtonStyle
import com.example.ui.components.RacingCard
import com.example.ui.components.RacingIconButton
import com.example.ui.components.ShopItemIllustrationCanvas
import com.example.ui.theme.BorderBlueGlow
import com.example.ui.theme.GlossyBlueBottom
import com.example.ui.theme.GlossyBlueTop
import com.example.ui.theme.GoldStar
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.RacingYellow
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextWhite

@Composable
fun ShopScreen(
    saveData: PlayerSaveData,
    currentTab: ShopTab,
    onSelectTab: (ShopTab) -> Unit,
    onProductClick: (ShopProduct) -> Unit,
    onHome: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val products = remember(currentTab) {
        LevelManager.shopProducts.filter { it.tab == currentTab }
    }

    val freeRewardProduct = remember {
        ShopProduct(
            id = "daily_free_coins",
            tab = ShopTab.COINS,
            title = "Free Crate",
            subtitle = "+250 Coins",
            iconType = ShopIconType.COIN_CHEST,
            coinCost = 0,
            realMoneyLabel = null,
            rewardCoins = 250,
            isFreeDailyReward = true
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("shop_screen"),
        contentAlignment = Alignment.Center
    ) {
        RacingBackdropCanvas(showCheckeredFlags = false)

        NeonPanel(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.95f),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                GameHeader(
                    coins = saveData.coins,
                    onCoinAddClick = { onSelectTab(ShopTab.COINS) },
                    title = "Shop",
                    rightContent = {
                        RacingButton(
                            text = "Free +250",
                            style = RacingButtonStyle.GREEN,
                            icon = Icons.Filled.CardGiftcard,
                            height = 38.dp,
                            fontSize = 12.sp,
                            onClick = { onProductClick(freeRewardProduct) },
                            testTagName = "shop_free_crate_button"
                        )
                        RacingIconButton(
                            icon = Icons.Filled.Home,
                            contentDescription = "Home",
                            onClick = onHome,
                            testTagName = "shop_home_button"
                        )
                    }
                )

                // 3 Shop Tabs matching Screen 9 reference image: Coins | Boosters | Special
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ShopTabPill(
                            text = "Coins",
                            icon = Icons.Filled.MonetizationOn,
                            iconTint = GoldStar,
                            selected = currentTab == ShopTab.COINS,
                            onClick = { onSelectTab(ShopTab.COINS) },
                            testTagName = "shop_tab_coins"
                        )
                        ShopTabPill(
                            text = "Boosters",
                            icon = Icons.Filled.Bolt,
                            iconTint = NeonBlue,
                            selected = currentTab == ShopTab.BOOSTERS,
                            onClick = { onSelectTab(ShopTab.BOOSTERS) },
                            testTagName = "shop_tab_boosters"
                        )
                        ShopTabPill(
                            text = "Special",
                            icon = Icons.Filled.Star,
                            iconTint = GoldStar,
                            selected = currentTab == ShopTab.SPECIAL,
                            onClick = { onSelectTab(ShopTab.SPECIAL) },
                            testTagName = "shop_tab_special"
                        )
                    }

                    Text(
                        text = "Nitro: ${saveData.nitroBoosters}  •  Shields: ${saveData.shieldBoosters}  •  Fuel: ${saveData.fuelPercent}%",
                        color = TextSilver,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 3 Product Cards Row matching Screen 9 reference image
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    for (product in products) {
                        val priceText = when {
                            product.isFreeDailyReward -> "FREE"
                            product.coinCost != null -> "${product.coinCost} Coins"
                            else -> product.realMoneyLabel ?: "৳ 0.99"
                        }
                        RacingCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .testTag("shop_card_${product.id}"),
                            isHighlighted = product.coinCost != null,
                            onClick = { onProductClick(product) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = product.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = TextWhite,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = product.subtitle,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = RacingYellow,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }

                                ShopItemIllustrationCanvas(
                                    iconType = product.iconType,
                                    modifier = Modifier.size(76.dp)
                                )

                                RacingButton(
                                    text = priceText,
                                    style = RacingButtonStyle.YELLOW,
                                    height = 38.dp,
                                    fontSize = 13.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { onProductClick(product) },
                                    testTagName = "shop_buy_${product.id}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopTabPill(
    text: String,
    icon: ImageVector,
    iconTint: Color,
    selected: Boolean,
    onClick: () -> Unit,
    testTagName: String
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .height(38.dp)
            .clip(shape)
            .background(
                if (selected) {
                    Brush.verticalGradient(listOf(GlossyBlueTop, GlossyBlueBottom))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF142454), Color(0xFF0A1432)))
                }
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) NeonBlue else BorderBlueGlow.copy(alpha = 0.45f),
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp)
            .testTag(testTagName),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                color = if (selected) TextWhite else TextSilver,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        )
    }
}
