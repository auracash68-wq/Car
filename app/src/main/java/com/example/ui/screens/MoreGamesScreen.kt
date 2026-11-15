package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MORE_GAMES_LIST
import com.example.model.MoreGameItem
import com.example.ui.components.NeonPanel
import com.example.ui.components.RacingBackdropCanvas
import com.example.ui.components.RacingButton
import com.example.ui.components.RacingButtonStyle
import com.example.ui.components.RacingCard
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.RacingRed
import com.example.ui.theme.RacingYellow
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextWhite

/**
 * SCREEN 11: MORE GAMES
 * Matches reference image 11:
 * - "More Games" heading
 * - 3x2 grid of 6 arcade thumbnails: Bike Racing, Car Stunt, Truck Drive, Stickman Fight, Moto GP, Subway Run
 * - Bottom purple "Back" button
 */
@Composable
fun MoreGamesScreen(
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    var selectedGame by remember { mutableStateOf<MoreGameItem?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("more_games_screen"),
        contentAlignment = Alignment.Center
    ) {
        RacingBackdropCanvas(showCheckeredFlags = false)

        NeonPanel(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .fillMaxHeight(0.94f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "More Games",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = TextWhite,
                        fontSize = 22.sp
                    )
                )

                // 3x2 Grid of Game Cards matching Screen 11 Reference Image
                val topRow = MORE_GAMES_LIST.take(3)
                val bottomRow = MORE_GAMES_LIST.drop(3)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (item in topRow) {
                            MoreGameCard(
                                item = item,
                                onClick = { selectedGame = item },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (item in bottomRow) {
                            MoreGameCard(
                                item = item,
                                onClick = { selectedGame = item },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                }

                // Bottom Purple Glossy Back Button matching Screen 11 Reference Image
                RacingButton(
                    text = "Back",
                    style = RacingButtonStyle.PURPLE,
                    height = 40.dp,
                    modifier = Modifier.width(170.dp),
                    onClick = onBack,
                    testTagName = "more_games_back_button"
                )
            }
        }

        // Game Details / Store Launch Modal
        val activeGame = selectedGame
        if (activeGame != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.76f)),
                contentAlignment = Alignment.Center
            ) {
                NeonPanel(modifier = Modifier.width(380.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = activeGame.title,
                            style = MaterialTheme.typography.headlineMedium.copy(color = activeGame.accentColor)
                        )
                        Text(
                            text = "${activeGame.genre} • Rating ${activeGame.rating}",
                            style = MaterialTheme.typography.bodyLarge.copy(color = TextWhite)
                        )
                        Text(
                            text = "Open Google Play Store to view more arcade titles in this category.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSilver),
                            textAlign = TextAlign.Center
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RacingButton(
                                text = "Open Store",
                                style = RacingButtonStyle.YELLOW,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val gameToOpen = activeGame
                                    selectedGame = null
                                    try {
                                        val uri = Uri.parse("market://search?q=${Uri.encode(gameToOpen.searchQuery)}")
                                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        onShowMessage("Play Store listing for '${gameToOpen.title}' is not configured on this device.")
                                    }
                                },
                                testTagName = "more_game_open_store_button"
                            )
                            RacingButton(
                                text = "Close",
                                style = RacingButtonStyle.BLUE,
                                modifier = Modifier.weight(0.75f),
                                onClick = { selectedGame = null },
                                testTagName = "more_game_close_modal_button"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreGameCard(
    item: MoreGameItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RacingCard(
        modifier = modifier.testTag("more_game_card_${item.id}"),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Custom Illustrated Arcade Thumbnail Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                val w = size.width
                val h = size.height

                // Sky & Ground backdrop
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            item.accentColor.copy(alpha = 0.55f),
                            Color(0xFF0E1D45),
                            Color(0xFF1A2639)
                        )
                    )
                )
                drawRect(
                    color = Color(0xFF263238),
                    topLeft = Offset(0f, h * 0.65f),
                    size = Size(w, h * 0.35f)
                )

                when (item.id) {
                    "bike_racing", "moto_gp" -> {
                        // Sport Motorcycle Silhouette
                        drawCircle(Color(0xFF111827), radius = h * 0.17f, center = Offset(w * 0.32f, h * 0.66f))
                        drawCircle(item.accentColor, radius = h * 0.09f, center = Offset(w * 0.32f, h * 0.66f))
                        drawCircle(Color(0xFF111827), radius = h * 0.17f, center = Offset(w * 0.68f, h * 0.66f))
                        drawCircle(item.accentColor, radius = h * 0.09f, center = Offset(w * 0.68f, h * 0.66f))
                        val bikeFrame = Path().apply {
                            moveTo(w * 0.32f, h * 0.66f)
                            lineTo(w * 0.46f, h * 0.36f)
                            lineTo(w * 0.64f, h * 0.42f)
                            lineTo(w * 0.68f, h * 0.66f)
                            close()
                        }
                        drawPath(bikeFrame, color = item.accentColor)
                    }
                    "car_stunt" -> {
                        // Stunt ramp & airborne car
                        val ramp = Path().apply {
                            moveTo(w * 0.05f, h * 0.72f)
                            lineTo(w * 0.45f, h * 0.42f)
                            lineTo(w * 0.45f, h * 0.72f)
                            close()
                        }
                        drawPath(ramp, color = RacingYellow)
                        drawRoundRect(
                            color = RacingRed,
                            topLeft = Offset(w * 0.52f, h * 0.22f),
                            size = Size(w * 0.34f, h * 0.18f),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                    }
                    "truck_drive" -> {
                        // Heavy Cargo Truck
                        drawRoundRect(
                            color = NeonBlue,
                            topLeft = Offset(w * 0.16f, h * 0.28f),
                            size = Size(w * 0.44f, h * 0.36f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = RacingRed,
                            topLeft = Offset(w * 0.62f, h * 0.38f),
                            size = Size(w * 0.22f, h * 0.26f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawCircle(Color.Black, radius = h * 0.12f, center = Offset(w * 0.28f, h * 0.66f))
                        drawCircle(Color.Black, radius = h * 0.12f, center = Offset(w * 0.72f, h * 0.66f))
                    }
                    "stickman_fight" -> {
                        // Two Dynamic Stickman Fighters with Neon Glow
                        drawCircle(Color.Black, radius = h * 0.09f, center = Offset(w * 0.34f, h * 0.30f))
                        drawLine(Color.Black, Offset(w * 0.34f, h * 0.39f), Offset(w * 0.34f, h * 0.58f), strokeWidth = 5f)
                        drawLine(NeonBlue, Offset(w * 0.34f, h * 0.44f), Offset(w * 0.52f, h * 0.38f), strokeWidth = 5f)

                        drawCircle(Color.Black, radius = h * 0.09f, center = Offset(w * 0.66f, h * 0.30f))
                        drawLine(Color.Black, Offset(w * 0.66f, h * 0.39f), Offset(w * 0.66f, h * 0.58f), strokeWidth = 5f)
                        drawLine(RacingRed, Offset(w * 0.66f, h * 0.44f), Offset(w * 0.48f, h * 0.38f), strokeWidth = 5f)
                    }
                    else -> {
                        // Subway Run Tracks & Train
                        drawRoundRect(
                            color = RacingYellow,
                            topLeft = Offset(w * 0.30f, h * 0.22f),
                            size = Size(w * 0.40f, h * 0.48f),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        drawRoundRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(w * 0.36f, h * 0.28f),
                            size = Size(w * 0.28f, h * 0.16f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = TextWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                ),
                maxLines = 1
            )
        }
    }
}
