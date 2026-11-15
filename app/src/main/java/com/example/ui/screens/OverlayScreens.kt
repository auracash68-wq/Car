package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarPaintColor
import com.example.model.CarSpec
import com.example.model.RaceResult
import com.example.model.WheelStyle
import com.example.ui.components.BrokenHeartCanvas
import com.example.ui.components.GoldCoinIcon
import com.example.ui.components.NeonPanel
import com.example.ui.components.RacingButton
import com.example.ui.components.RacingButtonStyle
import com.example.ui.components.ShowroomCarCanvas
import com.example.ui.components.TrophyWithFlagsCanvas
import com.example.ui.theme.BorderBlueGlow
import com.example.ui.theme.DarkText
import com.example.ui.theme.GoldStar
import com.example.ui.theme.RacingRed
import com.example.ui.theme.RacingYellow
import com.example.ui.theme.TextWhite

/**
 * SCREEN 5: PAUSE MENU
 */
@Composable
fun PauseMenuOverlay(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit,
    onSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC04091A))
            .testTag("pause_menu_overlay"),
        contentAlignment = Alignment.Center
    ) {
        NeonPanel(
            modifier = Modifier
                .width(340.dp)
                .padding(vertical = 6.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF162B66))
                        .border(1.5.dp, BorderBlueGlow, RoundedCornerShape(10.dp))
                        .clickable(onClick = onResume)
                        .testTag("pause_close_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close Pause Menu",
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "PAUSED",
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = TextWhite,
                            fontSize = 24.sp,
                            shadow = Shadow(color = Color.Black, offset = Offset(2f, 4f), blurRadius = 6f)
                        )
                    )

                    RacingButton(
                        text = "Resume",
                        style = RacingButtonStyle.GREEN,
                        icon = Icons.Filled.PlayArrow,
                        height = 42.dp,
                        modifier = Modifier.fillMaxWidth(0.78f),
                        onClick = onResume,
                        testTagName = "pause_resume_button"
                    )

                    RacingButton(
                        text = "Restart",
                        style = RacingButtonStyle.BLUE,
                        icon = Icons.Filled.Refresh,
                        height = 40.dp,
                        modifier = Modifier.fillMaxWidth(0.78f),
                        onClick = onRestart,
                        testTagName = "pause_restart_button"
                    )

                    RacingButton(
                        text = "Home",
                        style = RacingButtonStyle.BLUE,
                        icon = Icons.Filled.Home,
                        height = 40.dp,
                        modifier = Modifier.fillMaxWidth(0.78f),
                        onClick = onHome,
                        testTagName = "pause_home_button"
                    )

                    RacingButton(
                        text = "Settings",
                        style = RacingButtonStyle.BLUE,
                        icon = Icons.Filled.Settings,
                        height = 40.dp,
                        modifier = Modifier.fillMaxWidth(0.78f),
                        onClick = onSettings,
                        testTagName = "pause_settings_button"
                    )
                }
            }
        }
    }
}

/**
 * SCREEN 6: WIN SCREEN
 */
@Composable
fun WinScreenOverlay(
    result: RaceResult,
    onNextLevel: () -> Unit,
    onHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD904091A))
            .testTag("win_screen_overlay"),
        contentAlignment = Alignment.Center
    ) {
        NeonPanel(
            modifier = Modifier
                .width(390.dp)
                .padding(vertical = 6.dp),
            borderColor = RacingYellow
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    contentAlignment = Alignment.BottomCenter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(102.dp)
                ) {
                    TrophyWithFlagsCanvas(
                        modifier = Modifier
                            .width(230.dp)
                            .height(98.dp)
                            .align(Alignment.TopCenter)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFF8F00), Color(0xFFFFEA00), Color(0xFFFF8F00))
                                )
                            )
                            .border(2.dp, Color.White, RoundedCornerShape(10.dp))
                            .padding(horizontal = 26.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "YOU WIN!",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = DarkText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                }

                Text(
                    text = "Your Time: ${formatRaceTime(result.completionTimeSec)}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.testTag("win_time_text")
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (s in 1..3) {
                        val earned = s <= result.starsEarned
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Star $s",
                            tint = if (earned) GoldStar else Color(0xFF1E293B),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0B1942))
                        .border(1.dp, BorderBlueGlow, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GoldCoinIcon(sizeDp = 18.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+${result.totalCoinsAwarded} Coins  •  Score: ${result.finalScore}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = RacingYellow,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RacingButton(
                        text = "Next Level",
                        style = RacingButtonStyle.GREEN,
                        height = 42.dp,
                        modifier = Modifier.weight(1f),
                        onClick = onNextLevel,
                        testTagName = "win_next_level_button"
                    )
                    RacingButton(
                        text = "Home",
                        style = RacingButtonStyle.BLUE,
                        height = 42.dp,
                        modifier = Modifier.weight(1f),
                        onClick = onHome,
                        testTagName = "win_home_button"
                    )
                }
            }
        }
    }
}

/**
 * SCREEN 7: LOSE SCREEN
 */
@Composable
fun LoseScreenOverlay(
    result: RaceResult,
    carSpec: CarSpec,
    paint: CarPaintColor,
    wheel: WheelStyle,
    onRetry: () -> Unit,
    onHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD9080614))
            .testTag("lose_screen_overlay"),
        contentAlignment = Alignment.Center
    ) {
        NeonPanel(
            modifier = Modifier
                .width(390.dp)
                .padding(vertical = 6.dp),
            borderColor = RacingRed
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                BrokenHeartCanvas(
                    modifier = Modifier.size(56.dp)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF8E0000), RacingRed, Color(0xFF8E0000))
                            )
                        )
                        .border(2.dp, Color(0xFFFF8A80), RoundedCornerShape(10.dp))
                        .padding(horizontal = 26.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "GAME OVER",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = TextWhite,
                            fontSize = 21.sp,
                            shadow = Shadow(color = Color.Black, offset = Offset(2f, 3f), blurRadius = 4f)
                        )
                    )
                }

                Text(
                    text = "Your Score: ${result.finalScore}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier.testTag("lose_score_text")
                )

                // Crashed Cars Scene matching Screen 7 reference image (player car on left + smoking traffic wreck on right)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(66.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ShowroomCarCanvas(
                        carSpec = carSpec,
                        paint = paint,
                        wheel = wheel,
                        modifier = Modifier
                            .width(150.dp)
                            .height(62.dp)
                            .align(Alignment.CenterStart)
                    )

                    Canvas(
                        modifier = Modifier
                            .width(160.dp)
                            .height(62.dp)
                            .align(Alignment.CenterEnd)
                    ) {
                        val w = size.width
                        val h = size.height
                        // Smoke plumes & fiery sparks
                        drawCircle(
                            color = Color(0xFF37474F).copy(alpha = 0.65f),
                            radius = h * 0.36f,
                            center = Offset(w * 0.45f, h * 0.42f)
                        )
                        drawCircle(
                            color = Color(0xFFFF6D00).copy(alpha = 0.55f),
                            radius = h * 0.22f,
                            center = Offset(w * 0.35f, h * 0.64f)
                        )
                        // Wrecked traffic car silhouette
                        drawRoundRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(w * 0.28f, h * 0.52f),
                            size = Size(w * 0.54f, h * 0.30f),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RacingButton(
                        text = "Retry",
                        style = RacingButtonStyle.GREEN,
                        height = 42.dp,
                        modifier = Modifier.weight(1f),
                        onClick = onRetry,
                        testTagName = "lose_retry_button"
                    )
                    RacingButton(
                        text = "Home",
                        style = RacingButtonStyle.BLUE,
                        height = 42.dp,
                        modifier = Modifier.weight(1f),
                        onClick = onHome,
                        testTagName = "lose_home_button"
                    )
                }
            }
        }
    }
}
