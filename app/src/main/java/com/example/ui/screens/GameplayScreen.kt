package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameEngine
import com.example.model.ControlScheme
import com.example.model.GameLoopState
import com.example.model.GraphicsQuality
import com.example.model.ObstacleType
import com.example.model.RaceResult
import com.example.ui.components.GoldCoinIcon
import com.example.ui.components.drawSideViewPlayerCar
import com.example.ui.components.drawTrafficVehicleCanvas
import com.example.ui.theme.BorderBlueGlow
import com.example.ui.theme.GoldStar
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.RacingRed
import com.example.ui.theme.RacingYellow
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextWhite
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun GameplayScreen(
    gameEngine: GameEngine,
    controlScheme: ControlScheme,
    graphicsQuality: GraphicsQuality,
    nitroBoostersCount: Int,
    lastRaceResult: RaceResult?,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onNextLevel: () -> Unit,
    onHome: () -> Unit,
    onOpenSettings: () -> Unit,
    onUseNitroBooster: () -> Unit,
    enableFrameLoop: Boolean = true
) {
    // Handle Android Back button during gameplay: pauses instead of accidentally exiting
    BackHandler {
        when (gameEngine.loopState) {
            GameLoopState.PLAYING -> onPause()
            GameLoopState.PAUSED -> onResume()
            else -> onHome()
        }
    }

    // Ensure controls reset if Composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            gameEngine.resetControls()
        }
    }

    // 60 FPS Frame-based Game Loop using delta time
    // Never runs when PAUSED, LEVEL_COMPLETED, or GAME_OVER
    var lastFrameNanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(gameEngine.loopState, enableFrameLoop) {
        if (enableFrameLoop && gameEngine.loopState == GameLoopState.PLAYING) {
            lastFrameNanos = 0L
            while (enableFrameLoop && gameEngine.loopState == GameLoopState.PLAYING) {
                withFrameNanos { frameTimeNanos ->
                    if (lastFrameNanos != 0L) {
                        val dtSeconds = (frameTimeNanos - lastFrameNanos) / 1_000_000_000f
                        gameEngine.updateFrame(dtSeconds)
                    }
                    lastFrameNanos = frameTimeNanos
                }
            }
        } else {
            lastFrameNanos = 0L
        }
    }

    val level = gameEngine.currentLevel

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF102040))
            .testTag("gameplay_screen")
    ) {
        // 1. 2D Side-Scrolling Racing World Canvas (reads frameTick inside drawScope so outer Box doesn't recompose)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(controlScheme, gameEngine.loopState) {
                    if (gameEngine.loopState == GameLoopState.PLAYING) {
                        detectTapGestures { tapOffset ->
                            val roadTop = size.height * 0.50f
                            val roadBottom = size.height * 0.88f
                            if (tapOffset.y in roadTop..roadBottom) {
                                val relY = (tapOffset.y - roadTop) / (roadBottom - roadTop)
                                val laneIdx = when {
                                    relY < 0.33f -> 0
                                    relY < 0.66f -> 1
                                    else -> 2
                                }
                                gameEngine.steerToLane(laneIdx)
                            }
                        }
                    }
                }
        ) {
            // Observe frameTick inside DrawScope for zero-recomposition 60 FPS rendering
            val tick = gameEngine.frameTick
            if (tick < 0L) return@Canvas

            val w = size.width
            val h = size.height
            val horizonY = h * 0.49f
            val roadTopY = h * 0.52f
            val roadBottomY = h * 0.89f
            val roadHeight = roadBottomY - roadTopY

            // Sky Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(level.skyTopColor, level.skyBottomColor, Color(0xFFB3E5FC)),
                    startY = 0f,
                    endY = horizonY
                ),
                size = Size(w, horizonY)
            )

            // Fluffy Sky Clouds (when Medium/High graphics)
            if (graphicsQuality != GraphicsQuality.LOW) {
                val cloudScroll = (gameEngine.cityScrollPx * 0.35f) % w
                for (i in -1..3) {
                    val cx = (i * (w * 0.36f) - cloudScroll + w) % (w * 1.35f) - w * 0.15f
                    val cy = h * (0.14f + (i % 2) * 0.06f)
                    drawOval(
                        color = Color.White.copy(alpha = 0.45f),
                        topLeft = Offset(cx, cy),
                        size = Size(w * 0.16f, h * 0.08f)
                    )
                    drawOval(
                        color = Color.White.copy(alpha = 0.55f),
                        topLeft = Offset(cx + w * 0.04f, cy - h * 0.03f),
                        size = Size(w * 0.12f, h * 0.09f)
                    )
                }
            }

            // Parallax Far City Skyline (Skyscrapers matching Screen 4 Reference Image)
            drawParallaxCitySkyline(
                w = w,
                horizonY = horizonY,
                scrollPx = gameEngine.cityScrollPx,
                cityColor = level.cityColor,
                highDetail = graphicsQuality == GraphicsQuality.HIGH
            )

            // Mid-ground Colorful Brick Buildings, Green Trees & Streetlamps
            drawParallaxBuildingsAndTrees(
                w = w,
                horizonY = horizonY,
                roadTopY = roadTopY,
                scrollPx = gameEngine.treesScrollPx,
                highDetail = graphicsQuality != GraphicsQuality.LOW
            )

            // Highway Curb Top (Red & White Alternating Stripes)
            drawHighwayCurb(
                w = w,
                y = roadTopY - h * 0.018f,
                curbHeight = h * 0.018f,
                scrollPx = gameEngine.roadScrollPx
            )

            // Multi-lane Asphalt Road Surface
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2C3545), Color(0xFF1F2735), Color(0xFF181F2B)),
                    startY = roadTopY,
                    endY = roadBottomY
                ),
                topLeft = Offset(0f, roadTopY),
                size = Size(w, roadHeight)
            )

            // 3 Highway Lanes with Dashed White Dividers
            val lane1Y = roadTopY + roadHeight * 0.333f
            val lane2Y = roadTopY + roadHeight * 0.666f
            drawDashedLaneDivider(w, lane1Y, gameEngine.roadScrollPx)
            drawDashedLaneDivider(w, lane2Y, gameEngine.roadScrollPx)

            // High-speed Nitro Wind Streaks on Road
            if (gameEngine.isNitroBoosting) {
                for (i in 0..4) {
                    val streakX = ((w - ((gameEngine.roadScrollPx * 2.2f + i * 240f) % w)))
                    val streakY = roadTopY + (i * 0.21f) * roadHeight + 12f
                    drawLine(
                        color = NeonBlue.copy(alpha = 0.45f),
                        start = Offset(streakX, streakY),
                        end = Offset(streakX + 95f, streakY),
                        strokeWidth = 3f
                    )
                }
            }

            // Finish Line Checkered Strip when approaching target distance
            val remainingMeters = level.distanceMeters - gameEngine.distanceMeters
            if (remainingMeters < 75f) {
                val finishX = (gameEngine.playerHorizontalRatio * w) + (remainingMeters * 14f) * (w / 1000f)
                if (finishX in -60f..(w + 60f)) {
                    drawFinishLineBanner(finishX, roadTopY, roadHeight)
                }
            }

            // Road Obstacles (Nitro Pads, Oil Slicks, Barriers)
            val scaleX = w / 1000f
            for (i in 0 until gameEngine.roadObstacles.size) {
                val obs = gameEngine.roadObstacles[i]
                val ox = obs.worldX * scaleX
                val oy = roadTopY + (obs.laneIndex * 0.333f + 0.166f) * roadHeight
                when (obs.type) {
                    ObstacleType.NITRO_PAD -> {
                        drawRoundRect(
                            brush = Brush.horizontalGradient(listOf(NeonBlue, Color.White, NeonBlue)),
                            topLeft = Offset(ox - 28f, oy - 14f),
                            size = Size(56f, 28f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }
                    ObstacleType.OIL_SLICK -> {
                        drawOval(
                            color = Color(0xFF0B0E14).copy(alpha = 0.85f),
                            topLeft = Offset(ox - 32f, oy - 14f),
                            size = Size(64f, 28f)
                        )
                    }
                    ObstacleType.ROAD_BARRIER -> {
                        drawRoundRect(
                            color = RacingRed,
                            topLeft = Offset(ox - 16f, oy - 22f),
                            size = Size(32f, 44f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(ox - 14f, oy - 8f),
                            size = Size(28f, 14f),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                    }
                }
            }

            // Collectible Golden Coins on the Road (matching Screen 4 Reference Image)
            for (i in 0 until gameEngine.roadCoins.size) {
                val coin = gameEngine.roadCoins[i]
                val cx = coin.worldX * scaleX
                val baseCy = roadTopY + (coin.laneIndex * 0.333f + 0.166f) * roadHeight
                val cy = if (coin.collected) baseCy - coin.collectAnimProgress * 45f else baseCy
                val alpha = if (coin.collected) (1f - coin.collectAnimProgress).coerceIn(0f, 1f) else 1f
                val coinRadius = h * 0.028f
                drawCircle(
                    color = Color(0xFFFF8F00).copy(alpha = alpha),
                    radius = coinRadius,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color(0xFFFFEA00).copy(alpha = alpha),
                    radius = coinRadius * 0.80f,
                    center = Offset(cx, cy)
                )
                drawRect(
                    color = Color(0xFFFF6F00).copy(alpha = alpha),
                    topLeft = Offset(cx - coinRadius * 0.16f, cy - coinRadius * 0.45f),
                    size = Size(coinRadius * 0.32f, coinRadius * 0.90f)
                )
            }

            // Traffic Vehicles
            for (i in 0 until gameEngine.trafficVehicles.size) {
                val tv = gameEngine.trafficVehicles[i]
                val tx = tv.worldX * scaleX
                val carW = tv.widthDp * scaleX
                val carH = roadHeight * 0.30f
                val ty = roadTopY + tv.laneYRatio * (roadHeight * 0.666f) + roadHeight * 0.02f
                drawTrafficVehicleCanvas(
                    x = tx,
                    y = ty,
                    w = carW,
                    h = carH,
                    type = tv.type,
                    color = tv.primaryColor,
                    wheelRotationDeg = gameEngine.wheelRotationDeg
                )
            }

            // Player's Side-View Sports Car with subtle suspension bounce
            val playerX = gameEngine.playerHorizontalRatio * w
            val playerW = 148f * scaleX
            val playerH = roadHeight * 0.31f
            val suspensionBounce = sin(gameEngine.roadScrollPx * 0.045f) * 1.5f
            val playerY = roadTopY + gameEngine.playerLaneRatio * (roadHeight * 0.666f) +
                roadHeight * 0.015f + suspensionBounce
            val invulnFlash = gameEngine.invulnerableTimerSec > 0f &&
                ((gameEngine.invulnerableTimerSec * 12f).toInt() % 2 == 0)

            drawSideViewPlayerCar(
                x = playerX,
                y = playerY,
                carWidth = playerW,
                carHeight = playerH,
                paint = gameEngine.carPaint,
                wheel = gameEngine.carWheel,
                wheelRotationDeg = gameEngine.wheelRotationDeg,
                isNitroActive = gameEngine.isNitroBoosting,
                hasShield = gameEngine.hasActiveShield,
                isInvulnerableFlash = invulnFlash
            )

            // Particle Effects (Nitro Sparks, Coin Bursts, Crash Debris)
            if (graphicsQuality != GraphicsQuality.LOW) {
                for (i in 0 until gameEngine.particles.size) {
                    val p = gameEngine.particles[i]
                    val px = p.x * scaleX
                    val py = roadTopY + p.y * (roadHeight * 0.666f) + playerH * 0.6f
                    drawCircle(
                        color = p.color.copy(alpha = p.alpha),
                        radius = p.size,
                        center = Offset(px, py)
                    )
                }
            }

            // Highway Curb Bottom & Grass Verge
            drawHighwayCurb(
                w = w,
                y = roadBottomY,
                curbHeight = h * 0.018f,
                scrollPx = gameEngine.roadScrollPx
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF14281D), Color(0xFF0A1410)),
                    startY = roadBottomY + h * 0.018f,
                    endY = h
                ),
                topLeft = Offset(0f, roadBottomY + h * 0.018f),
                size = Size(w, h - (roadBottomY + h * 0.018f))
            )
        }

        // Floating Action Popups (+1 COIN, +100 OVERTAKE!, TURBO BOOST!, CRASH!)
        FloatingPopupsLayer(gameEngine = gameEngine)

        // 2. Top & Bottom Gameplay HUD Layer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Isolated Top HUD Bar so timer/speed changes only recompose the HUD header
            GameplayTopHud(
                gameEngine = gameEngine,
                onPause = onPause
            )

            // Bottom Touch Controls matching Screen 4 Reference Image:
            // Left: Circular `<` and `>` Steering Buttons
            // Right: Nitro Button + Metallic Brake Pedal + Metallic Accelerate Pedal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Left Steering Controls (< and >)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SteeringCircleButton(
                        symbol = "❮",
                        subLabel = if (controlScheme == ControlScheme.UP_DOWN) "UP" else "LEFT",
                        isActive = gameEngine.isSteeringUpLeft,
                        onTapStep = { gameEngine.stepLane(-1) },
                        onPressChange = { pressed ->
                            gameEngine.isSteeringUpLeft = pressed
                        },
                        testTagName = "steer_left_button"
                    )
                    SteeringCircleButton(
                        symbol = "❯",
                        subLabel = if (controlScheme == ControlScheme.UP_DOWN) "DOWN" else "RIGHT",
                        isActive = gameEngine.isSteeringDownRight,
                        onTapStep = { gameEngine.stepLane(1) },
                        onPressChange = { pressed ->
                            gameEngine.isSteeringDownRight = pressed
                        },
                        testTagName = "steer_right_button"
                    )
                }

                // Right Pedals & Nitro Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    NitroControlButton(
                        nitroCharge = gameEngine.nitroCharge,
                        boostersCount = nitroBoostersCount,
                        isActive = gameEngine.isNitroBoosting,
                        onPressChange = { pressed ->
                            if (pressed && gameEngine.nitroCharge <= 0.15f) {
                                onUseNitroBooster()
                            } else {
                                gameEngine.isNitroPressed = pressed
                            }
                        }
                    )

                    MetallicPedalButton(
                        label = "BRAKE",
                        isTallAccelerator = false,
                        isPressed = gameEngine.isBraking,
                        onPressChange = { pressed ->
                            gameEngine.isBraking = pressed
                        },
                        testTagName = "brake_button"
                    )

                    MetallicPedalButton(
                        label = "GAS",
                        isTallAccelerator = true,
                        isPressed = gameEngine.isAccelerating,
                        onPressChange = { pressed ->
                            gameEngine.isAccelerating = pressed
                        },
                        testTagName = "accelerate_button"
                    )
                }
            }
        }

        // 3. Overlays: Screen 5 (PAUSE MENU), Screen 6 (WIN SCREEN), Screen 7 (LOSE SCREEN)
        when (gameEngine.loopState) {
            GameLoopState.PAUSED -> {
                PauseMenuOverlay(
                    onResume = onResume,
                    onRestart = onRestart,
                    onHome = onHome,
                    onSettings = onOpenSettings
                )
            }
            GameLoopState.LEVEL_COMPLETED -> {
                val res = lastRaceResult ?: RaceResult(
                    levelNumber = level.levelNumber,
                    isWin = true,
                    completionTimeSec = gameEngine.elapsedTimeSec,
                    finalScore = gameEngine.score,
                    starsEarned = 3,
                    coinsCollectedInRace = gameEngine.coinsCollected,
                    levelCompletionBonus = 150,
                    totalCoinsAwarded = 150 + gameEngine.coinsCollected,
                    overtakes = gameEngine.overtakesCount,
                    targetOvertakes = level.targetOvertakes
                )
                WinScreenOverlay(
                    result = res,
                    onNextLevel = onNextLevel,
                    onHome = onHome
                )
            }
            GameLoopState.GAME_OVER -> {
                val res = lastRaceResult ?: RaceResult(
                    levelNumber = level.levelNumber,
                    isWin = false,
                    completionTimeSec = gameEngine.elapsedTimeSec,
                    finalScore = gameEngine.score,
                    starsEarned = 0,
                    coinsCollectedInRace = gameEngine.coinsCollected,
                    levelCompletionBonus = 0,
                    totalCoinsAwarded = 0,
                    overtakes = gameEngine.overtakesCount,
                    targetOvertakes = level.targetOvertakes
                )
                LoseScreenOverlay(
                    result = res,
                    carSpec = gameEngine.carSpec,
                    paint = gameEngine.carPaint,
                    wheel = gameEngine.carWheel,
                    onRetry = onRestart,
                    onHome = onHome
                )
            }
            else -> Unit
        }
    }
}

@Composable
private fun FloatingPopupsLayer(gameEngine: GameEngine) {
    val tick = gameEngine.frameTick
    if (tick < 0L) return
    Box(modifier = Modifier.fillMaxSize()) {
        for (i in 0 until gameEngine.floatingPopups.size) {
            val fp = gameEngine.floatingPopups[i]
            Text(
                text = fp.text,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = fp.color.copy(alpha = fp.life.coerceIn(0f, 1f)),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    shadow = Shadow(color = Color.Black, offset = Offset(1f, 2f), blurRadius = 3f)
                ),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset {
                        IntOffset(
                            x = (fp.x * 0.45f).roundToInt(),
                            y = ((fp.y - 0.35f) * 140f).roundToInt()
                        )
                    }
            )
        }
    }
}

@Composable
private fun GameplayTopHud(
    gameEngine: GameEngine,
    onPause: () -> Unit
) {
    val level = gameEngine.currentLevel
    val formattedTimer = formatRaceTime(gameEngine.elapsedTimeSec)
    val progressFraction = (gameEngine.distanceMeters / level.distanceMeters.coerceAtLeast(1f))
        .coerceIn(0f, 1f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left HUD Pill (Timer + Mission Trophy Counter + Coins)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(NavyDark.copy(alpha = 0.88f))
                .border(1.5.dp, BorderBlueGlow.copy(alpha = 0.85f), RoundedCornerShape(22.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Timer,
                    contentDescription = "Race Timer",
                    tint = NeonBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formattedTimer,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.testTag("hud_timer_text")
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = "Mission Overtakes",
                    tint = GoldStar,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${gameEngine.overtakesCount}/${level.targetOvertakes}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.testTag("hud_mission_text")
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                GoldCoinIcon(sizeDp = 16.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${gameEngine.coinsCollected}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = RacingYellow,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Center Compact Level Progress & Health Indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(150.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LVL ${level.levelNumber}",
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Car Health",
                        tint = if (gameEngine.healthPercent > 0.35f) RacingRed else RacingYellow,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${(gameEngine.healthPercent * 100).toInt()}%",
                        color = TextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(NavyDark.copy(alpha = 0.85f))
                    .border(1.dp, BorderBlueGlow.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(listOf(NeonBlue, RacingYellow))
                        )
                )
            }
        }

        // Right HUD Pill: Speedometer ("120 KM/H") + Pause Button ("||")
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(NavyDark.copy(alpha = 0.88f))
                .border(1.5.dp, BorderBlueGlow.copy(alpha = 0.85f), RoundedCornerShape(22.dp))
                .padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${gameEngine.speedKmh.toInt()}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = TextWhite,
                    fontSize = 18.sp
                ),
                modifier = Modifier.testTag("hud_speed_text")
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "KM/H",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSilver,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E3C8C))
                    .border(1.dp, NeonBlue, CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onPause() })
                    }
                    .testTag("hud_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Pause,
                    contentDescription = "Pause Game",
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SteeringCircleButton(
    symbol: String,
    subLabel: String,
    isActive: Boolean,
    onTapStep: () -> Unit,
    onPressChange: (Boolean) -> Unit,
    testTagName: String
) {
    val borderColor = if (isActive) RacingYellow else BorderBlueGlow
    Box(
        modifier = Modifier
            .size(62.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = if (isActive) {
                        listOf(Color(0xFF2979FF), Color(0xFF0D2558))
                    } else {
                        listOf(Color(0xFF263859), Color(0xFF0F1A36))
                    }
                )
            )
            .border(2.5.dp, borderColor, CircleShape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    onTapStep()
                    onPressChange(true)
                    waitForUpOrCancellation()
                    onPressChange(false)
                }
            }
            .testTag(testTagName),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = symbol,
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = subLabel,
                color = TextSilver,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MetallicPedalButton(
    label: String,
    isTallAccelerator: Boolean,
    isPressed: Boolean,
    onPressChange: (Boolean) -> Unit,
    testTagName: String
) {
    val width = if (isTallAccelerator) 56.dp else 52.dp
    val height = if (isTallAccelerator) 82.dp else 58.dp
    val shape = RoundedCornerShape(10.dp)
    val borderColor = if (isPressed) RacingYellow else Color(0xFFCFD8DC)

    Box(
        modifier = Modifier
            .size(width, height)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isPressed) {
                        listOf(Color(0xFF90A4AE), Color(0xFF37474F))
                    } else {
                        listOf(Color(0xFFECEFF1), Color(0xFF90A4AE), Color(0xFF455A64))
                    }
                )
            )
            .border(2.dp, borderColor, shape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    onPressChange(true)
                    waitForUpOrCancellation()
                    onPressChange(false)
                }
            }
            .testTag(testTagName),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val barCount = if (isTallAccelerator) 4 else 2
            repeat(barCount) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1C2630))
                )
            }
            Text(
                text = label,
                color = Color(0xFF0D1B2A),
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun NitroControlButton(
    nitroCharge: Float,
    boostersCount: Int,
    isActive: Boolean,
    onPressChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isActive) {
                        listOf(Color(0xFFFFEA00), Color(0xFFFF3D00))
                    } else {
                        listOf(NeonBlue, Color(0xFF004ECB))
                    }
                )
            )
            .border(2.dp, if (isActive) RacingYellow else TextWhite, CircleShape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    onPressChange(true)
                    waitForUpOrCancellation()
                    onPressChange(false)
                }
            }
            .testTag("nitro_button"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Bolt,
                contentDescription = "Nitro Boost",
                tint = TextWhite,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "${(nitroCharge * 100).toInt()}%",
                color = TextWhite,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold
            )
            if (boostersCount > 0) {
                Text(
                    text = "x$boostersCount",
                    color = RacingYellow,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun DrawScope.drawParallaxCitySkyline(
    w: Float,
    horizonY: Float,
    scrollPx: Float,
    cityColor: Color,
    highDetail: Boolean
) {
    val tileWidth = w * 0.65f
    val offset = scrollPx % tileWidth
    val heights = floatArrayOf(0.55f, 0.72f, 0.48f, 0.80f, 0.62f, 0.75f, 0.52f, 0.68f)
    val bw = tileWidth / heights.size

    for (tile in -1..2) {
        val baseX = tile * tileWidth - offset
        for (i in heights.indices) {
            val bx = baseX + i * bw
            val bh = horizonY * heights[i]
            val by = horizonY - bh
            drawRect(
                color = cityColor.copy(alpha = 0.75f),
                topLeft = Offset(bx, by),
                size = Size(bw - 4f, bh)
            )
            if (highDetail) {
                val winCols = 2
                val winRows = 4
                val ww = (bw - 14f) / winCols
                val wh = (bh * 0.6f) / winRows
                for (r in 0 until winRows) {
                    for (c in 0 until winCols) {
                        drawRect(
                            color = Color(0xFF90CAF9).copy(alpha = 0.32f),
                            topLeft = Offset(bx + 5f + c * (ww + 3f), by + 8f + r * (wh + 4f)),
                            size = Size(ww, wh * 0.6f)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawParallaxBuildingsAndTrees(
    w: Float,
    horizonY: Float,
    roadTopY: Float,
    scrollPx: Float,
    highDetail: Boolean
) {
    drawRect(
        color = Color(0xFF2E7D32),
        topLeft = Offset(0f, horizonY - 6f),
        size = Size(w, roadTopY - horizonY + 6f)
    )

    val unitWidth = 170f
    val totalPatternWidth = unitWidth * 8f
    val modScroll = scrollPx % totalPatternWidth

    for (k in -2..12) {
        val x = k * unitWidth - modScroll
        if (x < -unitWidth || x > w + unitWidth) continue
        val idx = ((k % 8) + 8) % 8

        if (idx % 2 == 0) {
            val bWidth = 92f
            val bHeight = horizonY * 0.42f
            val bTop = horizonY - bHeight
            val brickColor = if (idx % 4 == 0) Color(0xFFD84315) else Color(0xFFF57C00)
            drawRoundRect(
                color = brickColor,
                topLeft = Offset(x, bTop),
                size = Size(bWidth, bHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )
            if (highDetail) {
                for (r in 0..2) {
                    for (c in 0..2) {
                        drawRect(
                            color = Color(0xFFFFF59D),
                            topLeft = Offset(x + 12f + c * 24f, bTop + 12f + r * 20f),
                            size = Size(14f, 12f)
                        )
                    }
                }
            }
        }

        val treeX = x + 115f
        drawRect(
            color = Color(0xFF4E342E),
            topLeft = Offset(treeX - 5f, horizonY - 26f),
            size = Size(10f, 28f)
        )
        drawCircle(
            color = Color(0xFF43A047),
            radius = 22f,
            center = Offset(treeX, horizonY - 34f)
        )
        drawCircle(
            color = Color(0xFF66BB6A),
            radius = 16f,
            center = Offset(treeX - 6f, horizonY - 40f)
        )
    }
}

private fun DrawScope.drawHighwayCurb(
    w: Float,
    y: Float,
    curbHeight: Float,
    scrollPx: Float
) {
    val segW = 44f
    val offset = scrollPx % (segW * 2f)
    var curX = -offset - segW * 2f
    var toggle = false
    while (curX < w + segW) {
        drawRect(
            color = if (toggle) RacingRed else Color.White,
            topLeft = Offset(curX, y),
            size = Size(segW + 1f, curbHeight)
        )
        curX += segW
        toggle = !toggle
    }
}

private fun DrawScope.drawDashedLaneDivider(
    w: Float,
    y: Float,
    scrollPx: Float
) {
    val dashLen = 56f
    val gapLen = 44f
    val step = dashLen + gapLen
    val offset = scrollPx % step
    var curX = -offset - step
    while (curX < w + step) {
        drawRoundRect(
            color = Color.White.copy(alpha = 0.78f),
            topLeft = Offset(curX, y - 2.5f),
            size = Size(dashLen, 5f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        curX += step
    }
}

private fun DrawScope.drawFinishLineBanner(
    x: Float,
    roadTopY: Float,
    roadHeight: Float
) {
    val cols = 3
    val rows = 8
    val cw = 14f
    val ch = roadHeight / rows
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            drawRect(
                color = if ((r + c) % 2 == 0) Color.White else Color.Black,
                topLeft = Offset(x + c * cw, roadTopY + r * ch),
                size = Size(cw, ch)
            )
        }
    }
}

fun formatRaceTime(seconds: Float): String {
    val totalCentis = (seconds * 100).toInt().coerceAtLeast(0)
    val mins = (totalCentis / 6000)
    val secs = (totalCentis % 6000) / 100
    val centis = totalCentis % 100
    return String.format(Locale.US, "%02d:%02d.%02d", mins, secs, centis)
}
