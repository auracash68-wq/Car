package com.example.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.example.audio.SoundEngine
import com.example.model.ALL_CARS
import com.example.model.CarPaintColor
import com.example.model.CarSpec
import com.example.model.FloatingPopup
import com.example.model.GameLoopState
import com.example.model.LevelConfig
import com.example.model.ObstacleType
import com.example.model.ParticleEffect
import com.example.model.RaceResult
import com.example.model.RoadCoin
import com.example.model.RoadObstacle
import com.example.model.TrafficType
import com.example.model.TrafficVehicle
import com.example.model.UpgradeLevels
import com.example.model.WheelStyle
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.RacingRed
import com.example.ui.theme.RacingYellow
import kotlin.math.abs
import kotlin.random.Random

class GameEngine(
    private val soundEngine: SoundEngine,
    private val onRaceCompleted: (RaceResult) -> Unit
) {
    var loopState by mutableStateOf(GameLoopState.MENU)
        private set

    // Increments every physics step to drive smooth 60 FPS Canvas redraws without recomposing HUD buttons
    var frameTick by mutableLongStateOf(0L)
        private set

    var currentLevel by mutableStateOf(LevelManager.getLevelConfig(1))
        private set

    var carSpec by mutableStateOf(ALL_CARS.first())
        private set

    var carPaint by mutableStateOf(CarPaintColor.CRIMSON_RED)
        private set

    var carWheel by mutableStateOf(WheelStyle.SPORT_ALLOY)
        private set

    var upgrades by mutableStateOf(UpgradeLevels())
        private set

    // Dynamic Gameplay Telemetry
    var speedKmh by mutableFloatStateOf(0f)
        private set

    var distanceMeters by mutableFloatStateOf(0f)
        private set

    var elapsedTimeSec by mutableFloatStateOf(0f)
        private set

    var score by mutableIntStateOf(0)
        private set

    var coinsCollected by mutableIntStateOf(0)
        private set

    var overtakesCount by mutableIntStateOf(0)
        private set

    var healthPercent by mutableFloatStateOf(1f)
        private set

    var nitroCharge by mutableFloatStateOf(1f)
        private set

    var isNitroBoosting by mutableStateOf(false)
        private set

    var hasActiveShield by mutableStateOf(false)
        private set

    var invulnerableTimerSec by mutableFloatStateOf(0f)
        private set

    // Player Car Position & Animation
    // laneRatio: 0.0f = Top lane (Lane 0), 0.5f = Middle lane (Lane 1), 1.0f = Bottom lane (Lane 2)
    var playerLaneRatio by mutableFloatStateOf(0.5f)
        private set

    private var targetSnapLaneRatio: Float? = null

    var playerHorizontalRatio by mutableFloatStateOf(0.16f)
        private set

    var wheelRotationDeg by mutableFloatStateOf(0f)
        private set

    var roadScrollPx by mutableFloatStateOf(0f)
        private set

    var cityScrollPx by mutableFloatStateOf(0f)
        private set

    var treesScrollPx by mutableFloatStateOf(0f)
        private set

    // Input States
    var isSteeringUpLeft by mutableStateOf(false)
    var isSteeringDownRight by mutableStateOf(false)
    var isAccelerating by mutableStateOf(false)
    var isBraking by mutableStateOf(false)
    var isNitroPressed by mutableStateOf(false)

    // Active World Entities (standard ArrayLists read during Canvas draw via frameTick to avoid SnapshotStateList churn)
    val trafficVehicles = ArrayList<TrafficVehicle>(12)
    val roadCoins = ArrayList<RoadCoin>(24)
    val roadObstacles = ArrayList<RoadObstacle>(8)
    val particles = ArrayList<ParticleEffect>(40)
    val floatingPopups = ArrayList<FloatingPopup>(12)

    // Effective car stats computed from CarSpec + Upgrades
    var maxSpeedKmh: Float = 240f
        private set
    private var accelRateKmhPerSec: Float = 95f
    private var brakeRateKmhPerSec: Float = 185f
    private var handlingSpeed: Float = 1.55f

    private var nextEntityId = 1
    private var distanceSinceLastTraffic = 0f
    private var distanceSinceLastCoins = 0f
    private var distanceSinceLastObstacle = 0f
    private var engineSoundCooldown = 0f
    private var resultDispatched = false

    fun startRace(
        levelConfig: LevelConfig,
        selectedCar: CarSpec,
        paint: CarPaintColor,
        wheel: WheelStyle,
        carUpgrades: UpgradeLevels,
        startWithShield: Boolean
    ) {
        loopState = GameLoopState.LOADING
        currentLevel = levelConfig
        carSpec = selectedCar
        carPaint = paint
        carWheel = wheel
        upgrades = carUpgrades

        // Compute real gameplay physics stats from car + upgrades
        val speedBonus = (carUpgrades.speed - 1) * 18f + (carUpgrades.engine - 1) * 12f
        maxSpeedKmh = selectedCar.maxKmh + speedBonus
        accelRateKmhPerSec = (88f + selectedCar.baseAcceleration * 68f) +
            (carUpgrades.acceleration - 1) * 16f + (carUpgrades.engine - 1) * 10f
        brakeRateKmhPerSec = (155f + selectedCar.baseBraking * 85f) +
            (carUpgrades.braking - 1) * 22f
        handlingSpeed = (1.25f + selectedCar.baseHandling * 0.75f) +
            (carUpgrades.handling - 1) * 0.18f

        // Reset telemetry
        speedKmh = 70f // Smooth rolling start
        distanceMeters = 0f
        elapsedTimeSec = 0f
        score = 0
        coinsCollected = 0
        overtakesCount = 0
        healthPercent = 1f
        nitroCharge = 1f
        isNitroBoosting = false
        hasActiveShield = startWithShield
        invulnerableTimerSec = 0f
        playerLaneRatio = 0.5f
        targetSnapLaneRatio = null
        playerHorizontalRatio = 0.16f
        wheelRotationDeg = 0f
        roadScrollPx = 0f
        cityScrollPx = 0f
        treesScrollPx = 0f

        isSteeringUpLeft = false
        isSteeringDownRight = false
        isAccelerating = false
        isBraking = false
        isNitroPressed = false
        resultDispatched = false

        trafficVehicles.clear()
        roadCoins.clear()
        roadObstacles.clear()
        particles.clear()
        floatingPopups.clear()

        distanceSinceLastTraffic = 0f
        distanceSinceLastCoins = 0f
        distanceSinceLastObstacle = 0f

        // Pre-populate initial coins & traffic ahead on the road
        spawnCoinCluster(startWorldX = 440f)
        spawnCoinCluster(startWorldX = 880f)
        spawnTrafficWave(startWorldX = 700f)
        spawnTrafficWave(startWorldX = 1180f)

        frameTick = 1L
        loopState = GameLoopState.PLAYING
    }

    fun pauseRace() {
        if (loopState == GameLoopState.PLAYING) {
            loopState = GameLoopState.PAUSED
            resetControls()
        }
    }

    fun resumeRace() {
        if (loopState == GameLoopState.PAUSED) {
            loopState = GameLoopState.PLAYING
        }
    }

    fun stopToMenu() {
        loopState = GameLoopState.MENU
        resetControls()
    }

    fun resetControls() {
        isSteeringUpLeft = false
        isSteeringDownRight = false
        isAccelerating = false
        isBraking = false
        isNitroPressed = false
        isNitroBoosting = false
    }

    fun steerToLane(laneIndex: Int) {
        if (loopState != GameLoopState.PLAYING) return
        val clamped = laneIndex.coerceIn(0, 2)
        targetSnapLaneRatio = clamped * 0.5f
        playerLaneRatio = clamped * 0.5f
    }

    fun stepLane(deltaLane: Int) {
        if (loopState != GameLoopState.PLAYING) return
        val currentNearestLane = when {
            playerLaneRatio < 0.25f -> 0
            playerLaneRatio > 0.75f -> 2
            else -> 1
        }
        val nextLane = (currentNearestLane + deltaLane).coerceIn(0, 2)
        targetSnapLaneRatio = nextLane * 0.5f
    }

    fun refillNitroFromBooster() {
        nitroCharge = 1f
        isNitroBoosting = true
        soundEngine.playNitroBoost()
    }

    /**
     * Player bounding box in virtual road space (X: 0..1000, Y: 0..300 where lanes are 0..100, 100..200, 200..300).
     */
    fun getPlayerHitbox(): Rect {
        val left = playerHorizontalRatio * 1000f + 16f
        val right = playerHorizontalRatio * 1000f + 148f - 16f
        val centerY = (playerLaneRatio * 200f) + 50f
        return Rect(left = left, top = centerY - 26f, right = right, bottom = centerY + 26f)
    }

    fun updateFrame(rawDeltaSec: Float) {
        if (loopState != GameLoopState.PLAYING) return

        // Clamp delta time to prevent physics jumps after frame drops
        val dt = rawDeltaSec.coerceIn(0.001f, 0.05f)
        elapsedTimeSec += dt

        if (invulnerableTimerSec > 0f) {
            invulnerableTimerSec = (invulnerableTimerSec - dt).coerceAtLeast(0f)
        }

        // 1. Speed, Acceleration, Braking & Nitro Physics
        val wantsNitro = isNitroPressed && nitroCharge > 0.04f
        if (wantsNitro && !isNitroBoosting) {
            soundEngine.playNitroBoost()
        }
        isNitroBoosting = wantsNitro

        val effectiveMaxSpeed = if (isNitroBoosting) maxSpeedKmh * 1.35f else maxSpeedKmh
        val cruiseMinSpeed = 75f

        when {
            isNitroBoosting -> {
                nitroCharge = (nitroCharge - dt * 0.30f).coerceAtLeast(0f)
                speedKmh = (speedKmh + accelRateKmhPerSec * 2.2f * dt).coerceAtMost(effectiveMaxSpeed)
                playerHorizontalRatio = (playerHorizontalRatio + dt * 0.12f).coerceAtMost(0.24f)
            }
            isBraking -> {
                speedKmh = (speedKmh - brakeRateKmhPerSec * dt).coerceAtLeast(25f)
                playerHorizontalRatio = (playerHorizontalRatio - dt * 0.14f).coerceAtLeast(0.11f)
            }
            isAccelerating -> {
                speedKmh = (speedKmh + accelRateKmhPerSec * dt).coerceAtMost(effectiveMaxSpeed)
                playerHorizontalRatio = (playerHorizontalRatio + dt * 0.06f).coerceAtMost(0.20f)
                nitroCharge = (nitroCharge + dt * 0.045f).coerceAtMost(1f)
            }
            else -> {
                if (speedKmh < cruiseMinSpeed) {
                    speedKmh = (speedKmh + accelRateKmhPerSec * 0.55f * dt).coerceAtMost(cruiseMinSpeed)
                } else if (speedKmh > effectiveMaxSpeed) {
                    speedKmh = (speedKmh - 60f * dt).coerceAtLeast(effectiveMaxSpeed)
                } else {
                    speedKmh = (speedKmh - 18f * dt).coerceAtLeast(cruiseMinSpeed)
                }
                playerHorizontalRatio += (0.16f - playerHorizontalRatio) * dt * 3f
                nitroCharge = (nitroCharge + dt * 0.06f).coerceAtMost(1f)
            }
        }

        // Periodic engine sound pulse when accelerating
        engineSoundCooldown -= dt
        if ((isAccelerating || isNitroBoosting) && engineSoundCooldown <= 0f) {
            soundEngine.playAcceleratePulse(speedKmh / maxSpeedKmh)
            engineSoundCooldown = 0.22f
        }

        // 2. Vertical Lane Steering & Road Boundary Clamping
        val steerDir = when {
            isSteeringUpLeft && !isSteeringDownRight -> -1f
            isSteeringDownRight && !isSteeringUpLeft -> 1f
            else -> 0f
        }
        if (steerDir != 0f) {
            targetSnapLaneRatio = null
            playerLaneRatio = (playerLaneRatio + steerDir * handlingSpeed * dt).coerceIn(0f, 1f)
        } else {
            val snapTarget = targetSnapLaneRatio ?: when {
                playerLaneRatio < 0.25f -> 0f
                playerLaneRatio > 0.75f -> 1f
                else -> 0.5f
            }
            val diff = snapTarget - playerLaneRatio
            if (abs(diff) < 0.015f) {
                playerLaneRatio = snapTarget
                targetSnapLaneRatio = null
            } else {
                val step = diff.coerceIn(-handlingSpeed * dt * 1.4f, handlingSpeed * dt * 1.4f)
                playerLaneRatio = (playerLaneRatio + step).coerceIn(0f, 1f)
            }
        }

        // 3. World Scroll & Distance Advancement
        val metersPerSec = speedKmh / 3.6f
        val deltaMeters = metersPerSec * dt
        distanceMeters = (distanceMeters + deltaMeters).coerceAtMost(currentLevel.distanceMeters)

        // Convert meters to world scroll units (1 meter = 14 world units)
        val deltaWorldUnits = deltaMeters * 14f
        roadScrollPx = (roadScrollPx + deltaWorldUnits) % 20000f
        treesScrollPx = (treesScrollPx + deltaWorldUnits * 0.65f) % 20000f
        cityScrollPx = (cityScrollPx + deltaWorldUnits * 0.22f) % 20000f
        wheelRotationDeg = (wheelRotationDeg + deltaWorldUnits * 1.4f) % 360f

        score += (deltaMeters * (1.2f + speedKmh / 180f)).toInt()

        // 4. Spawn Traffic, Coins & Obstacles
        distanceSinceLastTraffic += deltaMeters
        distanceSinceLastCoins += deltaMeters
        distanceSinceLastObstacle += deltaMeters

        val trafficIntervalMeters = (42f - currentLevel.trafficDensity * 18f).coerceAtLeast(22f)
        if (distanceSinceLastTraffic >= trafficIntervalMeters &&
            distanceMeters < currentLevel.distanceMeters - 80f
        ) {
            distanceSinceLastTraffic = 0f
            spawnTrafficWave(startWorldX = 1280f)
        }

        if (distanceSinceLastCoins >= 28f && distanceMeters < currentLevel.distanceMeters - 50f) {
            distanceSinceLastCoins = 0f
            spawnCoinCluster(startWorldX = 1220f)
        }

        if (distanceSinceLastObstacle >= 58f && distanceMeters < currentLevel.distanceMeters - 90f) {
            distanceSinceLastObstacle = 0f
            if (Random.nextFloat() <= currentLevel.obstacleChance + 0.25f) {
                spawnObstacle(startWorldX = 1320f)
            }
        }

        // 5. Update Entities & Check Deterministic Bounding-Box Collisions
        val playerBox = getPlayerHitbox()
        val playerWorldX = playerHorizontalRatio * 1000f
        val playerCarWidth = 148f

        // Update Coins
        val coinIter = roadCoins.iterator()
        while (coinIter.hasNext()) {
            val coin = coinIter.next()
            coin.worldX -= deltaWorldUnits
            if (coin.collected) {
                coin.collectAnimProgress += dt * 4.2f
                if (coin.collectAnimProgress >= 1f) {
                    coinIter.remove()
                }
            } else {
                val coinCenterY = (coin.laneIndex * 100f) + 50f
                val coinRect = Rect(
                    left = coin.worldX - 22f,
                    top = coinCenterY - 24f,
                    right = coin.worldX + 22f,
                    bottom = coinCenterY + 24f
                )
                if (playerBox.overlaps(coinRect)) {
                    coin.collected = true
                    coinsCollected += 1
                    score += 25
                    soundEngine.playCoinCollect()
                    spawnParticles(
                        x = playerWorldX + playerCarWidth * 0.6f,
                        y = coin.laneIndex * 0.5f,
                        color = RacingYellow,
                        count = 5
                    )
                    if (floatingPopups.size < 8) {
                        floatingPopups.add(
                            FloatingPopup(
                                x = playerWorldX + playerCarWidth * 0.5f,
                                y = coin.laneIndex * 0.5f,
                                text = "+1 COIN",
                                color = RacingYellow
                            )
                        )
                    }
                } else if (coin.worldX < -120f) {
                    coinIter.remove()
                }
            }
        }

        // Update Obstacles
        val obsIter = roadObstacles.iterator()
        while (obsIter.hasNext()) {
            val obs = obsIter.next()
            obs.worldX -= deltaWorldUnits
            if (!obs.triggered) {
                val obsCenterY = (obs.laneIndex * 100f) + 50f
                val obsRect = Rect(
                    left = obs.worldX - 24f,
                    top = obsCenterY - 22f,
                    right = obs.worldX + 24f,
                    bottom = obsCenterY + 22f
                )
                if (playerBox.overlaps(obsRect)) {
                    obs.triggered = true
                    when (obs.type) {
                        ObstacleType.NITRO_PAD -> {
                            nitroCharge = (nitroCharge + 0.45f).coerceAtMost(1f)
                            speedKmh = (speedKmh + 45f).coerceAtMost(maxSpeedKmh * 1.35f)
                            score += 60
                            soundEngine.playNitroBoost()
                            floatingPopups.add(
                                FloatingPopup(playerWorldX + 60f, playerLaneRatio, "TURBO BOOST!", NeonBlue)
                            )
                        }
                        ObstacleType.OIL_SLICK -> {
                            speedKmh = (speedKmh * 0.72f).coerceAtLeast(45f)
                            soundEngine.playBrakeScreech()
                            floatingPopups.add(
                                FloatingPopup(playerWorldX + 60f, playerLaneRatio, "OIL SLIP!", Color(0xFFFFAB00))
                            )
                        }
                        ObstacleType.ROAD_BARRIER -> {
                            handleCollisionPenalty(damage = 0.25f, label = "BARRIER HIT!")
                        }
                    }
                }
            }
            if (obs.worldX < -150f) {
                obsIter.remove()
            }
        }

        // Update Traffic Vehicles
        val trafficIter = trafficVehicles.iterator()
        while (trafficIter.hasNext()) {
            val tv = trafficIter.next()
            val relativeKmh = (speedKmh - tv.speedKmh)
            val relativeUnits = (relativeKmh / 3.6f) * 14f * dt
            tv.worldX -= relativeUnits

            // Check Overtake
            if (!tv.passedByPlayer && !tv.collided && tv.worldX + tv.widthDp < playerWorldX) {
                tv.passedByPlayer = true
                overtakesCount += 1
                score += 100
                floatingPopups.add(
                    FloatingPopup(
                        x = playerWorldX + 50f,
                        y = playerLaneRatio,
                        text = "+100 OVERTAKE!",
                        color = Color(0xFF00E676)
                    )
                )
            }

            // Check Vehicle Collision using deterministic Rect overlap
            if (!tv.collided && invulnerableTimerSec <= 0f) {
                val tvCenterY = (tv.laneYRatio * 200f) + 50f
                val tvRect = Rect(
                    left = tv.worldX + 14f,
                    top = tvCenterY - 22f,
                    right = tv.worldX + tv.widthDp - 14f,
                    bottom = tvCenterY + 22f
                )
                if (playerBox.overlaps(tvRect)) {
                    tv.collided = true
                    handleCollisionPenalty(damage = 0.34f, label = "CRASH!")
                }
            }

            if (tv.worldX < -280f || tv.worldX > 1900f) {
                trafficIter.remove()
            }
        }

        // 6. Update Particles & Floating Popups
        if (isNitroBoosting && particles.size < 36 && Random.nextFloat() < 0.65f) {
            particles.add(
                ParticleEffect(
                    x = playerWorldX - 8f,
                    y = playerLaneRatio,
                    vx = -320f - Random.nextFloat() * 140f,
                    vy = (Random.nextFloat() - 0.5f) * 0.25f,
                    color = if (Random.nextBoolean()) NeonBlue else Color(0xFFFF6D00),
                    size = 8f + Random.nextFloat() * 6f,
                    life = 0.45f
                )
            )
        }

        val partIter = particles.iterator()
        while (partIter.hasNext()) {
            val p = partIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= dt * 1.8f
            p.alpha = p.life.coerceIn(0f, 1f)
            if (p.life <= 0f) partIter.remove()
        }

        val popIter = floatingPopups.iterator()
        while (popIter.hasNext()) {
            val fp = popIter.next()
            fp.y = (fp.y - dt * 0.30f).coerceAtLeast(-0.2f)
            fp.life -= dt * 1.15f
            if (fp.life <= 0f) popIter.remove()
        }

        frameTick++

        // 7. Check Win / Lose Conditions
        if (healthPercent <= 0.01f && !resultDispatched) {
            triggerRaceEnd(isWin = false)
        } else if (distanceMeters >= currentLevel.distanceMeters && !resultDispatched) {
            triggerRaceEnd(isWin = true)
        }
    }

    private fun handleCollisionPenalty(damage: Float, label: String) {
        soundEngine.playCollision()
        spawnParticles(
            x = playerHorizontalRatio * 1000f + 90f,
            y = playerLaneRatio,
            color = RacingRed,
            count = 10
        )
        if (hasActiveShield) {
            hasActiveShield = false
            invulnerableTimerSec = 1.2f
            floatingPopups.add(
                FloatingPopup(
                    x = playerHorizontalRatio * 1000f + 50f,
                    y = playerLaneRatio,
                    text = "SHIELD ABSORBED!",
                    color = NeonBlue
                )
            )
        } else {
            healthPercent = (healthPercent - damage).coerceAtLeast(0f)
            invulnerableTimerSec = 1.4f
            speedKmh = (speedKmh * 0.48f).coerceAtLeast(35f)
            floatingPopups.add(
                FloatingPopup(
                    x = playerHorizontalRatio * 1000f + 50f,
                    y = playerLaneRatio,
                    text = label,
                    color = RacingRed
                )
            )
        }
    }

    private fun triggerRaceEnd(isWin: Boolean) {
        if (resultDispatched) return
        resultDispatched = true
        resetControls()

        if (isWin) {
            loopState = GameLoopState.LEVEL_COMPLETED
            soundEngine.playVictory()
            val stars = LevelManager.calculateStars(
                level = currentLevel,
                completionTimeSec = elapsedTimeSec,
                healthPercent = healthPercent
            )
            val starBonus = LevelManager.calculateStarReward(stars, currentLevel.levelNumber)
            val totalReward = coinsCollected + starBonus + (overtakesCount * 5)
            val finalScore = score + (stars * 500) + (healthPercent * 300).toInt()
            onRaceCompleted(
                RaceResult(
                    levelNumber = currentLevel.levelNumber,
                    isWin = true,
                    completionTimeSec = elapsedTimeSec,
                    finalScore = finalScore,
                    starsEarned = stars,
                    coinsCollectedInRace = coinsCollected,
                    levelCompletionBonus = starBonus,
                    totalCoinsAwarded = totalReward,
                    overtakes = overtakesCount,
                    targetOvertakes = currentLevel.targetOvertakes
                )
            )
        } else {
            loopState = GameLoopState.GAME_OVER
            soundEngine.playGameOver()
            onRaceCompleted(
                RaceResult(
                    levelNumber = currentLevel.levelNumber,
                    isWin = false,
                    completionTimeSec = elapsedTimeSec,
                    finalScore = score,
                    starsEarned = 0,
                    coinsCollectedInRace = coinsCollected,
                    levelCompletionBonus = 0,
                    totalCoinsAwarded = 0,
                    overtakes = overtakesCount,
                    targetOvertakes = currentLevel.targetOvertakes
                )
            )
        }
    }

    private fun spawnTrafficWave(startWorldX: Float) {
        val allLanes = listOf(0, 1, 2).shuffled()
        // Always leave at least 1 lane open so the player can skillfully pass
        val count = if (Random.nextFloat() < currentLevel.trafficDensity * 0.65f) 2 else 1
        val chosenLanes = allLanes.take(count)

        val trafficColors = listOf(
            Color(0xFF2979FF),
            Color(0xFFFFD600),
            Color(0xFF00E676),
            Color(0xFFECEFF1),
            Color(0xFFAA00FF),
            Color(0xFFFF6D00)
        )

        chosenLanes.forEachIndexed { idx, lane ->
            val type = TrafficType.entries.random()
            val width = if (type == TrafficType.HEAVY_TRUCK) 168f else 136f
            val height = if (type == TrafficType.HEAVY_TRUCK) 64f else 52f
            val baseTrafficSpeed = when (type) {
                TrafficType.HEAVY_TRUCK -> 42f
                TrafficType.CITY_TAXI -> 55f
                TrafficType.SUV_RACER -> 58f
                TrafficType.SPORT_COUPE -> 68f
                TrafficType.POLICE_CRUISER -> 72f
            }
            val color = when (type) {
                TrafficType.CITY_TAXI -> Color(0xFFFFD600)
                TrafficType.POLICE_CRUISER -> Color(0xFF1E293B)
                else -> trafficColors.random()
            }
            trafficVehicles.add(
                TrafficVehicle(
                    id = nextEntityId++,
                    worldX = startWorldX + idx * 175f + Random.nextInt(0, 65),
                    laneIndex = lane,
                    laneYRatio = lane * 0.5f,
                    speedKmh = baseTrafficSpeed,
                    type = type,
                    primaryColor = color,
                    widthDp = width,
                    heightDp = height
                )
            )
        }
    }

    private fun spawnCoinCluster(startWorldX: Float) {
        val baseLane = Random.nextInt(0, 3)
        val cols = Random.nextInt(3, 5)
        val dualRow = Random.nextBoolean()
        val secondaryLane = if (baseLane < 2) baseLane + 1 else baseLane - 1
        for (c in 0 until cols) {
            roadCoins.add(
                RoadCoin(
                    id = nextEntityId++,
                    worldX = startWorldX + c * 52f,
                    laneIndex = baseLane
                )
            )
            if (dualRow) {
                roadCoins.add(
                    RoadCoin(
                        id = nextEntityId++,
                        worldX = startWorldX + c * 52f,
                        laneIndex = secondaryLane
                    )
                )
            }
        }
    }

    private fun spawnObstacle(startWorldX: Float) {
        val lane = Random.nextInt(0, 3)
        val tooCloseToTraffic = trafficVehicles.any {
            it.laneIndex == lane && abs(it.worldX - startWorldX) < 180f
        }
        if (tooCloseToTraffic) return

        val type = when (Random.nextInt(0, 10)) {
            in 0..3 -> ObstacleType.NITRO_PAD
            in 4..7 -> ObstacleType.OIL_SLICK
            else -> ObstacleType.ROAD_BARRIER
        }
        roadObstacles.add(
            RoadObstacle(
                id = nextEntityId++,
                worldX = startWorldX,
                laneIndex = lane,
                type = type
            )
        )
    }

    private fun spawnParticles(x: Float, y: Float, color: Color, count: Int) {
        for (i in 0 until count) {
            if (particles.size >= 40) break
            particles.add(
                ParticleEffect(
                    x = x,
                    y = y,
                    vx = (Random.nextFloat() - 0.5f) * 360f,
                    vy = (Random.nextFloat() - 0.5f) * 0.6f,
                    color = color,
                    size = 6f + Random.nextFloat() * 6f,
                    life = 0.65f
                )
            )
        }
    }
}
