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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerSaveData
import com.example.ui.components.BottomActionBar
import com.example.ui.components.GameHeader
import com.example.ui.components.LevelCard
import com.example.ui.components.NeonPanel
import com.example.ui.components.RacingBackdropCanvas
import com.example.ui.components.RacingButton
import com.example.ui.components.RacingButtonStyle
import com.example.ui.components.RacingIconButton
import com.example.ui.theme.BorderBlueGlow
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextWhite

@Composable
fun LevelSelectionScreen(
    saveData: PlayerSaveData,
    currentPage: Int,
    onSelectLevel: (Int) -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onOpenShop: () -> Unit
) {
    BackHandler(onBack = onBack)

    // 8 levels per page matching the 4x2 grid in Screen 3 reference image (20 levels total across 3 pages)
    val startLevel = currentPage * 8 + 1
    val endLevel = (startLevel + 7).coerceAtMost(20)
    val pageLevels = (startLevel..endLevel).toList()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("level_selection_screen"),
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
                    onCoinAddClick = onOpenShop,
                    title = "Select Level",
                    rightContent = {
                        RacingIconButton(
                            icon = Icons.Filled.Home,
                            contentDescription = "Home",
                            onClick = onHome,
                            testTagName = "level_home_button"
                        )
                    }
                )

                // Center 4x2 Level Grid with Left & Right Page Arrows matching Screen 3 reference image
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Page Arrow
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(NavyDark.copy(alpha = if (currentPage > 0) 0.85f else 0.35f))
                            .border(
                                1.5.dp,
                                BorderBlueGlow.copy(alpha = if (currentPage > 0) 0.9f else 0.3f),
                                CircleShape
                            )
                            .clickable(enabled = currentPage > 0, onClick = onPrevPage)
                            .testTag("level_prev_page_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "❮",
                            color = if (currentPage > 0) TextWhite else TextSilver.copy(alpha = 0.4f),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 4x2 Grid of LevelCards
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val topRow = pageLevels.take(4)
                        val bottomRow = pageLevels.drop(4)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (lvl in topRow) {
                                val prog = saveData.getLevelProgress(lvl)
                                LevelCard(
                                    progress = prog,
                                    onClick = { onSelectLevel(lvl) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                            }
                            repeat(4 - topRow.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (lvl in bottomRow) {
                                val prog = saveData.getLevelProgress(lvl)
                                LevelCard(
                                    progress = prog,
                                    onClick = { onSelectLevel(lvl) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                            }
                            repeat(4 - bottomRow.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Right Page Arrow
                    val hasNextPage = currentPage < 2
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(NavyDark.copy(alpha = if (hasNextPage) 0.85f else 0.35f))
                            .border(
                                1.5.dp,
                                BorderBlueGlow.copy(alpha = if (hasNextPage) 0.9f else 0.3f),
                                CircleShape
                            )
                            .clickable(enabled = hasNextPage, onClick = onNextPage)
                            .testTag("level_next_page_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "❯",
                            color = if (hasNextPage) TextWhite else TextSilver.copy(alpha = 0.4f),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                BottomActionBar {
                    RacingButton(
                        text = "Back",
                        style = RacingButtonStyle.BLUE,
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        height = 40.dp,
                        modifier = Modifier.width(120.dp),
                        onClick = onBack,
                        testTagName = "level_back_button"
                    )
                    Text(
                        text = "Page ${currentPage + 1} / 3 • Unlocked: ${saveData.maxUnlockedLevel} / 20",
                        color = TextSilver,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
