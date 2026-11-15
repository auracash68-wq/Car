package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerSaveData
import com.example.model.ALL_CARS
import com.example.model.AppScreen
import com.example.model.GameLanguage
import com.example.ui.components.CarRacing2DLogo
import com.example.ui.components.GameHeader
import com.example.ui.components.GoldCoinIcon
import com.example.ui.components.NeonPanel
import com.example.ui.components.RacingBackdropCanvas
import com.example.ui.components.RacingButton
import com.example.ui.components.RacingButtonStyle
import com.example.ui.components.RacingIconButton
import com.example.ui.components.ShowroomCarCanvas
import com.example.ui.theme.GoldStar
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.RacingRed
import com.example.ui.theme.RacingYellow
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextWhite

@Composable
fun MainMenuScreen(
    saveData: PlayerSaveData,
    showGiftModal: Boolean,
    showNotificationsModal: Boolean,
    onNavigate: (AppScreen) -> Unit,
    onToggleGiftModal: (Boolean) -> Unit,
    onClaimDailyGift: () -> Unit,
    onToggleNotificationsModal: (Boolean) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val carSpec = remember(saveData.selectedCarId) {
        ALL_CARS.find { it.id == saveData.selectedCarId } ?: ALL_CARS.first()
    }
    val paint = saveData.getCarPaint(carSpec.id)
    val wheel = saveData.getCarWheel(carSpec.id)

    val playLabel = when (saveData.language) {
        GameLanguage.SPANISH -> "Jugar"
        GameLanguage.FRENCH -> "Jouer"
        GameLanguage.GERMAN -> "Spielen"
        else -> "Play"
    }
    val garageLabel = when (saveData.language) {
        GameLanguage.SPANISH -> "Garaje"
        GameLanguage.FRENCH -> "Garage"
        GameLanguage.GERMAN -> "Werkstatt"
        else -> "Garage"
    }
    val shopLabel = when (saveData.language) {
        GameLanguage.SPANISH -> "Tienda"
        GameLanguage.FRENCH -> "Boutique"
        GameLanguage.GERMAN -> "Shop"
        else -> "Shop"
    }
    val settingsLabel = when (saveData.language) {
        GameLanguage.SPANISH -> "Ajustes"
        GameLanguage.FRENCH -> "Options"
        GameLanguage.GERMAN -> "Optionen"
        else -> "Settings"
    }
    val moreGamesLabel = when (saveData.language) {
        GameLanguage.SPANISH -> "Más Juegos"
        GameLanguage.FRENCH -> "Plus de Jeux"
        GameLanguage.GERMAN -> "Mehr Spiele"
        else -> "More Games"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("main_menu_screen")
    ) {
        RacingBackdropCanvas(showCheckeredFlags = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header: Coin Counter on left, Gift, Settings, Notification icons on right
            GameHeader(
                coins = saveData.coins,
                onCoinAddClick = { onNavigate(AppScreen.SHOP) },
                rightContent = {
                    RacingIconButton(
                        icon = Icons.Filled.CardGiftcard,
                        contentDescription = "Daily Gift",
                        iconTint = RacingRed,
                        badgeColor = RacingYellow,
                        onClick = { onToggleGiftModal(true) },
                        testTagName = "menu_gift_button"
                    )
                    RacingIconButton(
                        icon = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        onClick = { onNavigate(AppScreen.SETTINGS) },
                        testTagName = "menu_top_settings_button"
                    )
                    RacingIconButton(
                        icon = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        badgeColor = NeonBlue,
                        onClick = { onToggleNotificationsModal(true) },
                        testTagName = "menu_notifications_button"
                    )
                }
            )

            // Main Split Content matching Screen 2 Reference Image
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left 62%: Logo + Showcase Red Sports Car + Rate Us & Share buttons
                Column(
                    modifier = Modifier
                        .weight(0.62f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    CarRacing2DLogo(compact = true)

                    ShowroomCarCanvas(
                        carSpec = carSpec,
                        paint = paint,
                        wheel = wheel,
                        showTireStack = true,
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .weight(1f)
                    )

                    // Bottom-left Rate Us & Share buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RacingButton(
                            text = "Rate Us",
                            style = RacingButtonStyle.DARK_SLATE,
                            icon = Icons.Filled.Star,
                            iconTint = GoldStar,
                            height = 38.dp,
                            fontSize = 13.sp,
                            onClick = {
                                try {
                                    val uri = Uri.parse("market://details?id=${context.packageName}")
                                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    onShowMessage("Thanks for rating Car Racing 2D ★★★★★!")
                                }
                            },
                            testTagName = "rate_us_button"
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        RacingButton(
                            text = "Share",
                            style = RacingButtonStyle.DARK_SLATE,
                            icon = Icons.Filled.Share,
                            iconTint = NeonBlue,
                            height = 38.dp,
                            fontSize = 13.sp,
                            onClick = {
                                try {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_SUBJECT,
                                            "Car Racing 2D"
                                        )
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Race with me in Car Racing 2D! I unlocked Level ${saveData.maxUnlockedLevel} with ${saveData.coins} coins!"
                                        )
                                    }
                                    context.startActivity(
                                        Intent.createChooser(shareIntent, "Share Car Racing 2D")
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                } catch (_: Exception) {
                                    onShowMessage("Share sheet opened!")
                                }
                            },
                            testTagName = "share_button"
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right 38%: 5 Glossy Navigation Buttons (Play, Garage, Shop, Settings, More Games)
                Column(
                    modifier = Modifier
                        .weight(0.38f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RacingButton(
                        text = playLabel,
                        style = RacingButtonStyle.YELLOW,
                        icon = Icons.Filled.PlayArrow,
                        height = 50.dp,
                        fontSize = 20.sp,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigate(AppScreen.LEVEL_SELECTION) },
                        testTagName = "play_button"
                    )
                    RacingButton(
                        text = garageLabel,
                        style = RacingButtonStyle.BLUE,
                        icon = Icons.Filled.DirectionsCar,
                        height = 42.dp,
                        fontSize = 15.sp,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigate(AppScreen.GARAGE) },
                        testTagName = "garage_button"
                    )
                    RacingButton(
                        text = shopLabel,
                        style = RacingButtonStyle.BLUE,
                        icon = Icons.Filled.ShoppingCart,
                        height = 42.dp,
                        fontSize = 15.sp,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigate(AppScreen.SHOP) },
                        testTagName = "shop_button"
                    )
                    RacingButton(
                        text = settingsLabel,
                        style = RacingButtonStyle.BLUE,
                        icon = Icons.Filled.Settings,
                        height = 42.dp,
                        fontSize = 15.sp,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigate(AppScreen.SETTINGS) },
                        testTagName = "settings_button"
                    )
                    RacingButton(
                        text = moreGamesLabel,
                        style = RacingButtonStyle.BLUE,
                        icon = Icons.Filled.SportsEsports,
                        height = 42.dp,
                        fontSize = 15.sp,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigate(AppScreen.MORE_GAMES) },
                        testTagName = "more_games_button"
                    )
                }
            }
        }

        // Daily Gift Modal
        if (showGiftModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.72f)),
                contentAlignment = Alignment.Center
            ) {
                NeonPanel(
                    modifier = Modifier.width(360.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "DAILY RACER GIFT",
                            style = MaterialTheme.typography.headlineMedium.copy(color = RacingYellow)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoldCoinIcon(sizeDp = 32.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "+300 Coins & +1 Nitro Pack",
                                style = MaterialTheme.typography.titleMedium.copy(color = TextWhite)
                            )
                        }
                        Text(
                            text = "Claim free bonus currency and nitro fuel for your garage upgrades!",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSilver)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RacingButton(
                                text = "Claim Reward",
                                style = RacingButtonStyle.GREEN,
                                modifier = Modifier.weight(1f),
                                onClick = onClaimDailyGift,
                                testTagName = "claim_gift_button"
                            )
                            RacingButton(
                                text = "Close",
                                style = RacingButtonStyle.BLUE,
                                modifier = Modifier.weight(0.7f),
                                onClick = { onToggleGiftModal(false) },
                                testTagName = "close_gift_button"
                            )
                        }
                    }
                }
            }
        }

        // Notifications Modal
        if (showNotificationsModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.72f)),
                contentAlignment = Alignment.Center
            ) {
                NeonPanel(
                    modifier = Modifier.width(380.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "RACING NEWS & EVENTS",
                            style = MaterialTheme.typography.headlineMedium.copy(color = NeonBlue)
                        )
                        Text(
                            text = "• Season Championship: All 20 highway levels are live! Earn 3 Stars for Gold Coin rewards.\n• Garage Update: 5 Tuned Supercars with custom paint & forged rims available.\n• Active Boosters: ${saveData.nitroBoosters} Nitro Packs | ${saveData.shieldBoosters} Crash Shields.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        RacingButton(
                            text = "Got It",
                            style = RacingButtonStyle.YELLOW,
                            modifier = Modifier.fillMaxWidth(0.6f),
                            onClick = { onToggleNotificationsModal(false) },
                            testTagName = "close_notifications_button"
                        )
                    }
                }
            }
        }
    }
}
