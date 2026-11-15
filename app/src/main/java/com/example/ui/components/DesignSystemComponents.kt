package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarPaintColor
import com.example.model.CarSpec
import com.example.model.LevelProgress
import com.example.model.WheelStyle
import com.example.ui.theme.BorderBlueGlow
import com.example.ui.theme.DarkText
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlossyBlueBottom
import com.example.ui.theme.GlossyBlueTop
import com.example.ui.theme.GlossyGreenBottom
import com.example.ui.theme.GlossyGreenTop
import com.example.ui.theme.GlossyPurpleBottom
import com.example.ui.theme.GlossyPurpleTop
import com.example.ui.theme.GlossyRedBottom
import com.example.ui.theme.GlossyRedTop
import com.example.ui.theme.GlossyYellowBottom
import com.example.ui.theme.GlossyYellowTop
import com.example.ui.theme.GoldStar
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPanel
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.RacingRed
import com.example.ui.theme.RacingYellow
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextWhite
import java.text.NumberFormat
import java.util.Locale

enum class RacingButtonStyle {
    YELLOW,
    BLUE,
    GREEN,
    PURPLE,
    RED,
    DARK_SLATE
}

@Composable
fun RacingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: RacingButtonStyle = RacingButtonStyle.BLUE,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    enabled: Boolean = true,
    height: Dp = 48.dp,
    fontSize: TextUnit = 16.sp,
    cornerRadius: Dp = 14.dp,
    testTagName: String = "racing_btn_$text"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        label = "btn_scale"
    )

    val (topColor, bottomColor, borderColor, contentColor) = when (style) {
        RacingButtonStyle.YELLOW -> Quad(
            GlossyYellowTop,
            GlossyYellowBottom,
            Color(0xFFFFF59D),
            DarkText
        )
        RacingButtonStyle.BLUE -> Quad(
            GlossyBlueTop,
            GlossyBlueBottom,
            BorderBlueGlow,
            TextWhite
        )
        RacingButtonStyle.GREEN -> Quad(
            GlossyGreenTop,
            GlossyGreenBottom,
            Color(0xFF69F0AE),
            TextWhite
        )
        RacingButtonStyle.PURPLE -> Quad(
            GlossyPurpleTop,
            GlossyPurpleBottom,
            NeonPurple,
            TextWhite
        )
        RacingButtonStyle.RED -> Quad(
            GlossyRedTop,
            GlossyRedBottom,
            Color(0xFFFF8A80),
            TextWhite
        )
        RacingButtonStyle.DARK_SLATE -> Quad(
            Color(0xFF1E3264),
            Color(0xFF0D1B3E),
            BorderBlueGlow.copy(alpha = 0.65f),
            TextWhite
        )
    }

    val shape = RoundedCornerShape(cornerRadius)
    val alpha = if (enabled) 1f else 0.5f

    Box(
        modifier = modifier
            .scale(scale)
            .height(height)
            .shadow(elevation = 6.dp, shape = shape, ambientColor = borderColor, spotColor = borderColor)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor.copy(alpha = alpha), bottomColor.copy(alpha = alpha))
                )
            )
            .border(width = 1.5.dp, color = borderColor.copy(alpha = 0.85f * alpha), shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .testTag(testTagName),
        contentAlignment = Alignment.Center
    ) {
        // Top glossy specular highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.30f * alpha), Color.Transparent)
                    )
                )
        )

        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = iconTint ?: contentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    shadow = if (contentColor == TextWhite) {
                        Shadow(color = Color.Black.copy(alpha = 0.7f), offset = Offset(1f, 2f), blurRadius = 3f)
                    } else null
                ),
                maxLines = 1
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun NeonPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderColor: Color = BorderBlueGlow,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .shadow(elevation = 10.dp, shape = shape, spotColor = NeonBlue.copy(alpha = 0.45f))
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NavyPanel.copy(alpha = 0.94f),
                        NavyDark.copy(alpha = 0.96f),
                        NavyDeep.copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.8.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor.copy(alpha = 0.9f),
                        ElectricBlue.copy(alpha = 0.6f),
                        NeonPurple.copy(alpha = 0.75f),
                        borderColor.copy(alpha = 0.9f)
                    )
                ),
                shape = shape
            )
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun CoinCounter(
    coins: Int,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedCoins = remember(coins) {
        NumberFormat.getNumberInstance(Locale.US).format(coins)
    }
    val shape = RoundedCornerShape(24.dp)

    Row(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF0A1638), Color(0xFF142A66))
                )
            )
            .border(1.5.dp, BorderBlueGlow.copy(alpha = 0.75f), shape)
            .clickable { onAddClick() }
            .padding(horizontal = 8.dp)
            .testTag("coin_counter"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GoldCoinIcon(sizeDp = 24.dp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formattedCoins,
            style = MaterialTheme.typography.titleMedium.copy(
                color = TextWhite,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GlossyBlueTop, GlossyBlueBottom)
                    )
                )
                .border(1.dp, NeonBlue, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Coins",
                tint = TextWhite,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun RacingIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = TextWhite,
    badgeColor: Color? = null,
    testTagName: String = "icon_btn_$contentDescription"
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E3C8C), Color(0xFF0E1E4D))
                )
            )
            .border(1.5.dp, BorderBlueGlow.copy(alpha = 0.75f), shape)
            .clickable(onClick = onClick)
            .testTag(testTagName),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        if (badgeColor != null) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 2.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            )
        }
    }
}

@Composable
fun GameHeader(
    coins: Int,
    onCoinAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    leftContent: (@Composable RowScope.() -> Unit)? = null,
    rightContent: (@Composable RowScope.() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leftContent != null) {
                leftContent()
            } else {
                CoinCounter(coins = coins, onAddClick = onCoinAddClick)
            }
        }

        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = TextWhite,
                    shadow = Shadow(color = NeonBlue, offset = Offset(0f, 0f), blurRadius = 10f)
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (rightContent != null) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rightContent()
            }
        }
    }
}

@Composable
fun RacingCard(
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val borderBrush = if (isHighlighted) {
        Brush.verticalGradient(listOf(RacingYellow, Color(0xFFFF8F00)))
    } else {
        Brush.verticalGradient(listOf(BorderBlueGlow.copy(alpha = 0.85f), ElectricBlue.copy(alpha = 0.45f)))
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(NavyCard, NavyPanel, NavyDark)
                )
            )
            .border(width = if (isHighlighted) 2.dp else 1.5.dp, brush = borderBrush, shape = shape)
            .let {
                if (onClick != null) it.clickable(onClick = onClick) else it
            },
        content = content
    )
}

@Composable
fun CarPreview(
    carSpec: CarSpec,
    paint: CarPaintColor,
    wheel: WheelStyle,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        ShowroomCarCanvas(
            carSpec = carSpec,
            paint = paint,
            wheel = wheel,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .fillMaxHeight()
        )

        // Left Arrow
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(48.dp)
                .clip(CircleShape)
                .background(NavyDark.copy(alpha = 0.78f))
                .border(1.5.dp, BorderBlueGlow, CircleShape)
                .clickable(onClick = onPrevious)
                .testTag("car_prev_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "❮",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Right Arrow
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(48.dp)
                .clip(CircleShape)
                .background(NavyDark.copy(alpha = 0.78f))
                .border(1.5.dp, BorderBlueGlow, CircleShape)
                .clickable(onClick = onNext)
                .testTag("car_next_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "❯",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LevelCard(
    progress: LevelProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    val unlocked = progress.isUnlocked
    val borderColor = when {
        unlocked && progress.stars == 3 -> RacingYellow
        unlocked -> BorderBlueGlow
        else -> Color(0xFF3949AB).copy(alpha = 0.55f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (unlocked) {
                    Brush.verticalGradient(listOf(Color(0xFF1E88E5), Color(0xFF1565C0), Color(0xFF0D47A1)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF182447), Color(0xFF0E1730)))
                }
            )
            .border(width = if (unlocked) 2.dp else 1.5.dp, color = borderColor, shape = shape)
            .clickable(enabled = unlocked, onClick = onClick)
            .testTag("level_card_${progress.levelNumber}")
    ) {
        if (unlocked) {
            // Miniature racing road & city skyline thumbnail matching Screen 3 reference image
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                // City skyline silhouette
                drawRect(
                    color = Color(0xFF0D47A1).copy(alpha = 0.65f),
                    topLeft = Offset(w * 0.08f, h * 0.25f),
                    size = Size(w * 0.18f, h * 0.35f)
                )
                drawRect(
                    color = Color(0xFF0D47A1).copy(alpha = 0.65f),
                    topLeft = Offset(w * 0.72f, h * 0.20f),
                    size = Size(w * 0.18f, h * 0.40f)
                )
                // Perspective highway trapezoid
                val road = Path().apply {
                    moveTo(w * 0.35f, h * 0.52f)
                    lineTo(w * 0.65f, h * 0.52f)
                    lineTo(w * 0.95f, h)
                    lineTo(w * 0.05f, h)
                    close()
                }
                drawPath(road, color = Color(0xFF263238))
                // Center dashed road stripe
                drawLine(
                    color = RacingYellow,
                    start = Offset(w * 0.5f, h * 0.56f),
                    end = Offset(w * 0.5f, h * 0.95f),
                    strokeWidth = 3f
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${progress.levelNumber}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 26.sp,
                        color = TextWhite,
                        shadow = Shadow(color = Color.Black, offset = Offset(2f, 3f), blurRadius = 5f)
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (s in 1..3) {
                        val earned = s <= progress.stars
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (earned) GoldStar else Color(0xFF0A1435).copy(alpha = 0.65f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        } else {
            // Locked level card matching Screen 3 reference image
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "${progress.levelNumber}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextSilver.copy(alpha = 0.65f),
                        fontSize = 18.sp
                    )
                )
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Locked Level ${progress.levelNumber}",
                    tint = TextWhite.copy(alpha = 0.85f),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
fun UpgradeBar(
    label: String,
    progressRatio: Float,
    modifier: Modifier = Modifier,
    bonusRatio: Float = 0f
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            modifier = Modifier.width(96.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF08112B))
                .border(1.dp, BorderBlueGlow.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
        ) {
            val totalRatio = (progressRatio + bonusRatio).coerceIn(0.08f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth(totalRatio)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFF8F00), RacingYellow, Color(0xFFFFEA00))
                        )
                    )
            )
        }
    }
}

@Composable
fun BottomActionBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        content = content
    )
}

@Composable
fun RacingBackdropCanvas(
    modifier: Modifier = Modifier,
    showCheckeredFlags: Boolean = true
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Deep navy to neon blue-purple atmosphere
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF04091A),
                    Color(0xFF0B1942),
                    Color(0xFF15296B),
                    Color(0xFF070F2B)
                )
            )
        )

        // Subtle radial neon spotlights
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonBlue.copy(alpha = 0.20f), Color.Transparent),
                center = Offset(w * 0.35f, h * 0.45f),
                radius = w * 0.45f
            ),
            center = Offset(w * 0.35f, h * 0.45f),
            radius = w * 0.45f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonPurple.copy(alpha = 0.16f), Color.Transparent),
                center = Offset(w * 0.78f, h * 0.35f),
                radius = w * 0.40f
            ),
            center = Offset(w * 0.78f, h * 0.35f),
            radius = w * 0.40f
        )

        // Distant City Skyline Silhouette
        val buildingWidths = floatArrayOf(0.06f, 0.05f, 0.08f, 0.06f, 0.07f, 0.05f, 0.09f, 0.06f, 0.07f, 0.08f, 0.06f, 0.07f)
        val buildingHeights = floatArrayOf(0.28f, 0.36f, 0.44f, 0.30f, 0.48f, 0.34f, 0.42f, 0.26f, 0.39f, 0.45f, 0.32f, 0.38f)
        var curX = w * 0.05f
        val horizonY = h * 0.64f
        for (i in buildingWidths.indices) {
            val bw = w * buildingWidths[i]
            val bh = h * buildingHeights[i]
            drawRect(
                color = Color(0xFF0A1536).copy(alpha = 0.85f),
                topLeft = Offset(curX, horizonY - bh),
                size = Size(bw - 4f, bh)
            )
            curX += bw
        }

        // Checkered flag pattern accents on upper left & right corners
        if (showCheckeredFlags) {
            val rows = 5
            val cols = 6
            val cellSize = w * 0.022f
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if ((r + c) % 2 == 0) {
                        drawRect(
                            color = Color.White.copy(alpha = 0.07f),
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize, cellSize)
                        )
                        drawRect(
                            color = Color.White.copy(alpha = 0.07f),
                            topLeft = Offset(w - (c + 1) * cellSize, r * cellSize),
                            size = Size(cellSize, cellSize)
                        )
                    }
                }
            }
        }

        // Perspective Neon Asphalt Floor
        val roadPath = Path().apply {
            moveTo(0f, horizonY)
            lineTo(w, horizonY)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = roadPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0A132E), Color(0xFF0F1D45), Color(0xFF050A1C)),
                startY = horizonY,
                endY = h
            )
        )

        // Glowing neon speed streaks on road
        drawLine(
            brush = Brush.horizontalGradient(listOf(RacingRed.copy(alpha = 0.65f), Color.Transparent)),
            start = Offset(0f, h * 0.72f),
            end = Offset(w * 0.42f, h * 0.66f),
            strokeWidth = 4f
        )
        drawLine(
            brush = Brush.horizontalGradient(listOf(Color.Transparent, NeonBlue.copy(alpha = 0.65f))),
            start = Offset(w * 0.58f, h * 0.66f),
            end = Offset(w, h * 0.74f),
            strokeWidth = 4f
        )
    }
}
