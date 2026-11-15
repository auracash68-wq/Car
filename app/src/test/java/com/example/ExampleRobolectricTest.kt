package com.example

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.audio.SoundEngine
import com.example.data.PlayerSaveData
import com.example.data.SaveManager
import com.example.game.GameEngine
import com.example.game.LevelManager
import com.example.model.ALL_CARS
import com.example.model.CarPaintColor
import com.example.model.ControlScheme
import com.example.model.GameLanguage
import com.example.model.GameLoopState
import com.example.model.GarageTab
import com.example.model.GraphicsQuality
import com.example.model.RaceResult
import com.example.model.RoadCoin
import com.example.model.ShopTab
import com.example.model.TrafficType
import com.example.model.TrafficVehicle
import com.example.model.UpgradeLevels
import com.example.model.UpgradeType
import com.example.model.WheelStyle
import com.example.ui.screens.GarageScreen
import com.example.ui.screens.GameplayScreen
import com.example.ui.screens.LevelSelectionScreen
import com.example.ui.screens.LoseScreenOverlay
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.MoreGamesScreen
import com.example.ui.screens.PauseMenuOverlay
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WinScreenOverlay
import com.example.ui.theme.CarRacing2DTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w800dp-h450dp-land")
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `verify app name, 20 levels, and reward calculations`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Car Racing 2D", appName)
        assertEquals(20, LevelManager.levels.size)

        // Verify Bronze (50), Silver (100), Gold (150) base rewards for Level 1
        assertEquals(50, LevelManager.calculateStarReward(1, 1))
        assertEquals(100, LevelManager.calculateStarReward(2, 1))
        assertEquals(150, LevelManager.calculateStarReward(3, 1))
    }

    @Test
    fun `verify game engine physics, upgrades effect, pause, coin collection, and win condition`() {
        val soundEngine = SoundEngine().apply {
            soundEnabled = false
            musicEnabled = false
        }
        var capturedResult: RaceResult? = null
        var completionCount = 0
        val engine = GameEngine(soundEngine) { res ->
            capturedResult = res
            completionCount++
        }

        val level1 = LevelManager.getLevelConfig(1)
        // Start with base upgrades
        engine.startRace(
            levelConfig = level1,
            selectedCar = ALL_CARS.first(),
            paint = CarPaintColor.CRIMSON_RED,
            wheel = WheelStyle.SPORT_ALLOY,
            carUpgrades = UpgradeLevels(1, 1, 1, 1, 1),
            startWithShield = false
        )
        val baseMaxSpeed = engine.maxSpeedKmh

        // Start with upgraded car and verify maxSpeedKmh increases
        engine.startRace(
            levelConfig = level1,
            selectedCar = ALL_CARS.first(),
            paint = CarPaintColor.CRIMSON_RED,
            wheel = WheelStyle.SPORT_ALLOY,
            carUpgrades = UpgradeLevels(engine = 4, speed = 5, handling = 3, acceleration = 4, braking = 3),
            startWithShield = false
        )
        assertTrue(engine.maxSpeedKmh > baseMaxSpeed)
        assertEquals(GameLoopState.PLAYING, engine.loopState)

        val initialSpeed = engine.speedKmh
        engine.isAccelerating = true
        engine.isSteeringUpLeft = true
        repeat(20) {
            engine.updateFrame(0.03f)
        }
        assertTrue(engine.speedKmh > initialSpeed)
        assertTrue(engine.playerLaneRatio in 0f..1f)

        // Test braking decreases speed
        val highSpeed = engine.speedKmh
        engine.isAccelerating = false
        engine.isBraking = true
        repeat(15) {
            engine.updateFrame(0.03f)
        }
        assertTrue(engine.speedKmh < highSpeed)
        engine.isBraking = false

        // Test Pause stops timer, physics, and traffic
        engine.pauseRace()
        assertEquals(GameLoopState.PAUSED, engine.loopState)
        val pausedTime = engine.elapsedTimeSec
        val pausedDist = engine.distanceMeters
        engine.updateFrame(0.05f)
        assertEquals(pausedTime, engine.elapsedTimeSec, 0.0001f)
        assertEquals(pausedDist, engine.distanceMeters, 0.0001f)

        // Resume and test deterministic Rect coin collection without duplicates
        engine.resumeRace()
        assertEquals(GameLoopState.PLAYING, engine.loopState)
        engine.steerToLane(1)
        engine.roadCoins.clear()
        val coinsBefore = engine.coinsCollected
        engine.roadCoins.add(RoadCoin(id = 999, worldX = 210f, laneIndex = 1))
        engine.updateFrame(0.02f)
        assertEquals(coinsBefore + 1, engine.coinsCollected)
        // Updating again must NOT collect the same coin twice
        engine.updateFrame(0.02f)
        assertEquals(coinsBefore + 1, engine.coinsCollected)

        // Advance to finish line and verify Win triggers strictly once
        engine.isNitroPressed = true
        repeat(800) {
            engine.trafficVehicles.clear()
            engine.roadObstacles.clear()
            if (engine.loopState == GameLoopState.PLAYING) {
                engine.updateFrame(0.04f)
            }
        }
        assertEquals(GameLoopState.LEVEL_COMPLETED, engine.loopState)
        assertNotNull(capturedResult)
        assertTrue(capturedResult!!.isWin)
        assertEquals(1, completionCount)
    }

    @Test
    fun `verify shield absorption and vehicle collision game over`() {
        val soundEngine = SoundEngine().apply {
            soundEnabled = false
            musicEnabled = false
        }
        var capturedResult: RaceResult? = null
        val engine = GameEngine(soundEngine) { res ->
            capturedResult = res
        }

        engine.startRace(
            levelConfig = LevelManager.getLevelConfig(1),
            selectedCar = ALL_CARS.first(),
            paint = CarPaintColor.CRIMSON_RED,
            wheel = WheelStyle.SPORT_ALLOY,
            carUpgrades = UpgradeLevels(),
            startWithShield = true
        )
        engine.steerToLane(1)

        // First collision should be absorbed by active shield without losing health
        engine.trafficVehicles.clear()
        engine.trafficVehicles.add(
            TrafficVehicle(
                id = 400,
                worldX = 190f,
                laneIndex = 1,
                laneYRatio = 0.5f,
                speedKmh = 40f,
                type = TrafficType.SPORT_COUPE,
                primaryColor = Color.Blue,
                widthDp = 140f,
                heightDp = 52f
            )
        )
        engine.updateFrame(0.02f)
        assertFalse(engine.hasActiveShield)
        assertEquals(1.0f, engine.healthPercent, 0.01f)

        // Expire invulnerability timer
        repeat(35) {
            engine.trafficVehicles.clear()
            engine.updateFrame(0.05f)
        }

        // Now 3 unshielded collisions trigger GAME_OVER
        repeat(3) { hitIdx ->
            engine.trafficVehicles.clear()
            engine.trafficVehicles.add(
                TrafficVehicle(
                    id = 500 + hitIdx,
                    worldX = 190f,
                    laneIndex = 1,
                    laneYRatio = 0.5f,
                    speedKmh = 40f,
                    type = TrafficType.HEAVY_TRUCK,
                    primaryColor = Color.Red,
                    widthDp = 150f,
                    heightDp = 60f
                )
            )
            engine.updateFrame(0.02f)
            repeat(35) {
                engine.trafficVehicles.clear()
                if (engine.loopState == GameLoopState.PLAYING) {
                    engine.updateFrame(0.05f)
                }
            }
        }

        assertEquals(GameLoopState.GAME_OVER, engine.loopState)
        assertNotNull(capturedResult)
        assertFalse(capturedResult!!.isWin)
    }

    @Test
    fun `verify SaveManager DataStore persistence for levels, garage, upgrades, and settings`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val saveManager = SaveManager(context)

        val initial = saveManager.saveDataFlow.first()
        assertEquals(5000, initial.coins)
        assertEquals(1, initial.maxUnlockedLevel)
        assertEquals("red_viper", initial.selectedCarId)

        // Complete Level 1 -> unlocks Level 2 and awards coins
        saveManager.recordLevelWin(
            levelNumber = 1,
            starsEarned = 3,
            completionTimeSec = 24.5f,
            score = 3200,
            totalCoinsReward = 200
        )
        val afterWin = saveManager.saveDataFlow.first()
        assertEquals(5200, afterWin.coins)
        assertEquals(2, afterWin.maxUnlockedLevel)
        assertEquals(3, afterWin.getLevelProgress(1).stars)

        // Unlock second car (Cobalt Phantom X, price 1800)
        val boughtCar = saveManager.unlockAndSelectCar("cobalt_phantom", 1800)
        assertTrue(boughtCar)
        val afterCarBuy = saveManager.saveDataFlow.first()
        assertEquals(3400, afterCarBuy.coins)
        assertEquals("cobalt_phantom", afterCarBuy.selectedCarId)

        // Purchase Speed upgrade for Cobalt Phantom X (cost 300)
        val upgraded = saveManager.purchaseUpgrade("cobalt_phantom", UpgradeType.SPEED, 300)
        assertTrue(upgraded)
        val afterUpgrade = saveManager.saveDataFlow.first()
        assertEquals(3100, afterUpgrade.coins)
        assertEquals(2, afterUpgrade.getCarUpgrades("cobalt_phantom").speed)

        // Prevent purchase when coins are insufficient
        val failedBuy = saveManager.unlockAndSelectCar("cyber_valkyrie", 99999)
        assertFalse(failedBuy)

        // Persist Settings
        saveManager.setSoundEnabled(false)
        saveManager.setControlScheme(ControlScheme.UP_DOWN)
        saveManager.setGraphicsQuality(GraphicsQuality.MEDIUM)
        saveManager.setLanguage(GameLanguage.SPANISH)

        val afterSettings = saveManager.saveDataFlow.first()
        assertFalse(afterSettings.soundEnabled)
        assertEquals(ControlScheme.UP_DOWN, afterSettings.controlScheme)
        assertEquals(GraphicsQuality.MEDIUM, afterSettings.graphicsQuality)
        assertEquals(GameLanguage.SPANISH, afterSettings.language)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w840dp-h390dp-land")
    fun `verify all 11 screens render and respond in landscape aspect ratio`() {
        val sampleSave = PlayerSaveData()
        val soundEngine = SoundEngine().apply {
            soundEnabled = false
            musicEnabled = false
        }
        val engine = GameEngine(soundEngine) {}
        engine.startRace(
            levelConfig = LevelManager.getLevelConfig(1),
            selectedCar = ALL_CARS.first(),
            paint = CarPaintColor.CRIMSON_RED,
            wheel = WheelStyle.SPORT_ALLOY,
            carUpgrades = UpgradeLevels(),
            startWithShield = false
        )
        val sampleResult = RaceResult(1, true, 32.4f, 2850, 3, 25, 150, 175, 4, 3)

        val currentScreenIdx = androidx.compose.runtime.mutableIntStateOf(1)
        composeTestRule.setContent {
            CarRacing2DTheme {
                when (currentScreenIdx.intValue) {
                    1 -> SplashScreen(
                        isDataLoaded = false,
                        selectedCarId = "red_viper",
                        selectedPaint = CarPaintColor.CRIMSON_RED,
                        selectedWheel = WheelStyle.SPORT_ALLOY,
                        onLoadingFinished = {}
                    )
                    2 -> MainMenuScreen(
                        saveData = sampleSave,
                        showGiftModal = false,
                        showNotificationsModal = false,
                        onNavigate = {},
                        onToggleGiftModal = {},
                        onClaimDailyGift = {},
                        onToggleNotificationsModal = {},
                        onShowMessage = {}
                    )
                    3 -> LevelSelectionScreen(
                        saveData = sampleSave,
                        currentPage = 0,
                        onSelectLevel = {},
                        onPrevPage = {},
                        onNextPage = {},
                        onBack = {},
                        onHome = {},
                        onOpenShop = {}
                    )
                    4 -> GameplayScreen(
                        gameEngine = engine,
                        controlScheme = ControlScheme.LEFT_RIGHT,
                        graphicsQuality = GraphicsQuality.HIGH,
                        nitroBoostersCount = 3,
                        lastRaceResult = sampleResult,
                        onPause = {},
                        onResume = {},
                        onRestart = {},
                        onNextLevel = {},
                        onHome = {},
                        onOpenSettings = {},
                        onUseNitroBooster = {},
                        enableFrameLoop = false
                    )
                    5 -> PauseMenuOverlay(onResume = {}, onRestart = {}, onHome = {}, onSettings = {})
                    6 -> WinScreenOverlay(result = sampleResult, onNextLevel = {}, onHome = {})
                    7 -> LoseScreenOverlay(
                        result = sampleResult.copy(isWin = false),
                        carSpec = ALL_CARS.first(),
                        paint = CarPaintColor.CRIMSON_RED,
                        wheel = WheelStyle.SPORT_ALLOY,
                        onRetry = {},
                        onHome = {}
                    )
                    8 -> GarageScreen(
                        saveData = sampleSave,
                        currentTab = GarageTab.CARS,
                        previewCarIndex = 0,
                        onSelectTab = {},
                        onPrevCar = {},
                        onNextCar = {},
                        onBuyOrSelectCar = {},
                        onUpgradeStat = {},
                        onSelectOrBuyPaint = {},
                        onSelectOrBuyWheel = {},
                        onOpenShop = {},
                        onHome = {},
                        onBack = {}
                    )
                    9 -> ShopScreen(
                        saveData = sampleSave,
                        currentTab = ShopTab.COINS,
                        onSelectTab = {},
                        onProductClick = {},
                        onHome = {},
                        onBack = {}
                    )
                    10 -> SettingsScreen(
                        saveData = sampleSave,
                        showPrivacyModal = false,
                        onToggleSound = {},
                        onToggleMusic = {},
                        onCycleControls = {},
                        onCycleGraphics = {},
                        onCycleLanguage = {},
                        onOpenPrivacyPolicy = {},
                        onOpenMoreGames = {},
                        onBack = {}
                    )
                    11 -> MoreGamesScreen(onBack = {}, onShowMessage = {})
                }
            }
        }

        // 1. Splash
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("splash_screen").assertIsDisplayed()

        // 2. Main Menu
        composeTestRule.runOnUiThread { currentScreenIdx.intValue = 2 }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("main_menu_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("play_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("garage_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("shop_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("more_games_button").assertIsDisplayed()

        // 3. Level Selection
        composeTestRule.runOnUiThread { currentScreenIdx.intValue = 3 }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("level_selection_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("level_card_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("level_card_2").assertIsDisplayed()

        // 4. Gameplay
        composeTestRule.runOnUiThread { currentScreenIdx.intValue = 4 }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("gameplay_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("steer_left_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("steer_right_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("accelerate_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("brake_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nitro_button").assertIsDisplayed()

        // 5. Pause Overlay
        composeTestRule.runOnUiThread { currentScreenIdx.intValue = 5 }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("pause_menu_overlay").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pause_resume_button").assertIsDisplayed()

        // 6. Win Overlay
        composeTestRule.runOnUiThread { currentScreenIdx.intValue = 6 }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("win_screen_overlay").assertIsDisplayed()
        composeTestRule.onNodeWithTag("win_next_level_button").assertIsDisplayed()

        // 7. Lose Overlay
        composeTestRule.runOnUiThread { currentScreenIdx.intValue = 7 }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("lose_screen_overlay").assertIsDisplayed()
        composeTestRule.onNodeWithTag("lose_retry_button").assertIsDisplayed()

        // 8. Garage
        composeTestRule.runOnUiThread { currentScreenIdx.intValue = 8 }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("garage_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("garage_buy_select_button").assertIsDisplayed()

        // 9. Shop
        composeTestRule.runOnUiThread { currentScreenIdx.intValue = 9 }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("shop_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("shop_free_crate_button").assertIsDisplayed()

        // 10. Settings
        composeTestRule.runOnUiThread { currentScreenIdx.intValue = 10 }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_sound_toggle").assertIsDisplayed()

        // 11. More Games
        composeTestRule.runOnUiThread { currentScreenIdx.intValue = 11 }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("more_games_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("more_game_card_bike_racing").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("more_game_close_modal_button").performClick()
    }
}
