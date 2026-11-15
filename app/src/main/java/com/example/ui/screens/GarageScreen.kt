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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerSaveData
import com.example.model.ALL_CARS
import com.example.model.CarPaintColor
import com.example.model.GarageTab
import com.example.model.UpgradeType
import com.example.model.WheelStyle
import com.example.ui.components.CarPreview
import com.example.ui.components.GameHeader
import com.example.ui.components.NeonPanel
import com.example.ui.components.RacingBackdropCanvas
import com.example.ui.components.RacingButton
import com.example.ui.components.RacingButtonStyle
import com.example.ui.components.RacingIconButton
import com.example.ui.components.UpgradeBar
import com.example.ui.theme.BorderBlueGlow
import com.example.ui.theme.GlossyBlueBottom
import com.example.ui.theme.GlossyBlueTop
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.RacingYellow
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextWhite

@Composable
fun GarageScreen(
    saveData: PlayerSaveData,
    currentTab: GarageTab,
    previewCarIndex: Int,
    onSelectTab: (GarageTab) -> Unit,
    onPrevCar: () -> Unit,
    onNextCar: () -> Unit,
    onBuyOrSelectCar: () -> Unit,
    onUpgradeStat: (UpgradeType) -> Unit,
    onSelectOrBuyPaint: (CarPaintColor) -> Unit,
    onSelectOrBuyWheel: (WheelStyle) -> Unit,
    onOpenShop: () -> Unit,
    onHome: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val carSpec = ALL_CARS.getOrElse(previewCarIndex) { ALL_CARS.first() }
    val isUnlocked = carSpec.id in saveData.unlockedCarIds
    val isSelected = carSpec.id == saveData.selectedCarId
    val upgrades = saveData.getCarUpgrades(carSpec.id)
    val paint = saveData.getCarPaint(carSpec.id)
    val wheel = saveData.getCarWheel(carSpec.id)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("garage_screen"),
        contentAlignment = Alignment.Center
    ) {
        RacingBackdropCanvas(showCheckeredFlags = false)

        NeonPanel(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.95f),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
            // Top Header: Coin Counter on left, Car Title in center, Home icon on right
            GameHeader(
                coins = saveData.coins,
                onCoinAddClick = onOpenShop,
                title = "${carSpec.name} (${carSpec.maxKmh + (upgrades.speed - 1) * 16} KM/H)",
                rightContent = {
                    RacingIconButton(
                        icon = Icons.Filled.Home,
                        contentDescription = "Home",
                        onClick = onHome,
                        testTagName = "garage_home_button"
                    )
                }
            )

            // 3 Garage Tabs matching Screen 8 reference image: Cars | Upgrade | Customize
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GarageTabButton(
                    text = "Cars",
                    icon = Icons.Filled.DirectionsCar,
                    selected = currentTab == GarageTab.CARS,
                    onClick = { onSelectTab(GarageTab.CARS) },
                    testTagName = "garage_tab_cars"
                )
                GarageTabButton(
                    text = "Upgrade",
                    icon = Icons.Filled.Build,
                    selected = currentTab == GarageTab.UPGRADE,
                    onClick = { onSelectTab(GarageTab.UPGRADE) },
                    testTagName = "garage_tab_upgrade"
                )
                GarageTabButton(
                    text = "Customize",
                    icon = Icons.Filled.Palette,
                    selected = currentTab == GarageTab.CUSTOMIZE,
                    onClick = { onSelectTab(GarageTab.CUSTOMIZE) },
                    testTagName = "garage_tab_customize"
                )
            }

            // Tab Content
            when (currentTab) {
                GarageTab.CARS -> {
                    // Center Car Showcase with < and > buttons matching Screen 8 reference image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CarPreview(
                            carSpec = carSpec,
                            paint = paint,
                            wheel = wheel,
                            onPrevious = onPrevCar,
                            onNext = onNextCar,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (!isUnlocked) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NavyDark.copy(alpha = 0.85f))
                                    .border(1.dp, RacingYellow, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Locked",
                                    tint = RacingYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Unlock for ${carSpec.price} Coins",
                                    color = RacingYellow,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Bottom Bar matching Screen 8 reference image:
                    // Left: Speed, Handling, Acceleration stat bars
                    // Right: Bright Yellow "Buy" / "Select" / "Selected" button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        NeonPanel(
                            modifier = Modifier.weight(0.68f),
                            cornerRadius = 14.dp,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                UpgradeBar(
                                    label = "Speed",
                                    progressRatio = carSpec.baseSpeed,
                                    bonusRatio = (upgrades.speed - 1) * 0.05f
                                )
                                UpgradeBar(
                                    label = "Handling",
                                    progressRatio = carSpec.baseHandling,
                                    bonusRatio = (upgrades.handling - 1) * 0.05f
                                )
                                UpgradeBar(
                                    label = "Acceleration",
                                    progressRatio = carSpec.baseAcceleration,
                                    bonusRatio = (upgrades.acceleration - 1) * 0.05f
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        val buttonText = when {
                            !isUnlocked -> "Buy (${carSpec.price})"
                            isSelected -> "Selected"
                            else -> "Select"
                        }
                        val buttonStyle = when {
                            !isUnlocked -> RacingButtonStyle.YELLOW
                            isSelected -> RacingButtonStyle.GREEN
                            else -> RacingButtonStyle.YELLOW
                        }
                        val buttonIcon = when {
                            !isUnlocked -> Icons.Filled.ShoppingCart
                            isSelected -> Icons.Filled.Check
                            else -> Icons.Filled.DirectionsCar
                        }

                        RacingButton(
                            text = buttonText,
                            style = buttonStyle,
                            icon = buttonIcon,
                            height = 50.dp,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(0.32f),
                            onClick = onBuyOrSelectCar,
                            testTagName = "garage_buy_select_button"
                        )
                    }
                }

                GarageTab.UPGRADE -> {
                    // Split view: Left Car Preview + Right 5 Interactive Upgrade Bars (Engine, Speed, Handling, Acceleration, Braking)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(0.42f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            CarPreview(
                                carSpec = carSpec,
                                paint = paint,
                                wheel = wheel,
                                onPrevious = onPrevCar,
                                onNext = onNextCar,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        NeonPanel(
                            modifier = Modifier
                                .weight(0.58f)
                                .fillMaxHeight(),
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (type in UpgradeType.entries) {
                                    val lvl = upgrades.getLevel(type)
                                    val cost = type.baseCost * lvl
                                    val isMax = lvl >= 5
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        UpgradeBar(
                                            label = "${type.label} Lv.$lvl",
                                            progressRatio = lvl / 5f,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        RacingButton(
                                            text = if (isMax) "MAX" else "$cost",
                                            style = if (isMax) RacingButtonStyle.GREEN else RacingButtonStyle.YELLOW,
                                            enabled = !isMax && isUnlocked,
                                            height = 32.dp,
                                            fontSize = 12.sp,
                                            modifier = Modifier.width(88.dp),
                                            onClick = { onUpgradeStat(type) },
                                            testTagName = "upgrade_${type.key}_button"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                GarageTab.CUSTOMIZE -> {
                    // Split view: Left Car Preview + Right Paint Colors & Wheel Styles
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(0.44f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            CarPreview(
                                carSpec = carSpec,
                                paint = paint,
                                wheel = wheel,
                                onPrevious = onPrevCar,
                                onNext = onNextCar,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        NeonPanel(
                            modifier = Modifier
                                .weight(0.56f)
                                .fillMaxHeight(),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Body Paint Finish",
                                    style = MaterialTheme.typography.titleMedium.copy(color = TextWhite)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    for (paintOption in CarPaintColor.entries) {
                                        val owned = paintOption.id in saveData.unlockedPaintIds || paintOption.price == 0
                                        val active = paint == paintOption
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .clickable { onSelectOrBuyPaint(paintOption) }
                                                .testTag("paint_${paintOption.id}")
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(paintOption.highlightHex, paintOption.primaryHex)
                                                        )
                                                    )
                                                    .border(
                                                        width = if (active) 3.dp else 1.5.dp,
                                                        color = if (active) RacingYellow else TextWhite,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (active) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Check,
                                                        contentDescription = null,
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                } else if (!owned) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Lock,
                                                        contentDescription = null,
                                                        tint = TextWhite,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = if (owned) "Owned" else "${paintOption.price}",
                                                color = if (active) RacingYellow else TextSilver,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "Forged Wheel Style",
                                    style = MaterialTheme.typography.titleMedium.copy(color = TextWhite)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (wheelOption in WheelStyle.entries) {
                                        val owned = wheelOption.id in saveData.unlockedWheelIds || wheelOption.price == 0
                                        val active = wheel == wheelOption
                                        RacingButton(
                                            text = if (owned) wheelOption.label else "${wheelOption.label} (${wheelOption.price})",
                                            style = if (active) RacingButtonStyle.YELLOW else RacingButtonStyle.DARK_SLATE,
                                            height = 38.dp,
                                            fontSize = 11.sp,
                                            modifier = Modifier.weight(1f),
                                            onClick = { onSelectOrBuyWheel(wheelOption) },
                                            testTagName = "wheel_${wheelOption.id}"
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
}
}

@Composable
private fun GarageTabButton(
    text: String,
    icon: ImageVector,
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
            .padding(horizontal = 16.dp)
            .testTag(testTagName),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = if (selected) TextWhite else TextSilver,
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
