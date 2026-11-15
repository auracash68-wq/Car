package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEngine
import com.example.data.PlayerSaveData
import com.example.data.SaveManager
import com.example.model.ALL_CARS
import com.example.model.AppScreen
import com.example.model.CarPaintColor
import com.example.model.ControlScheme
import com.example.model.GameLanguage
import com.example.model.GameLoopState
import com.example.model.GarageTab
import com.example.model.GraphicsQuality
import com.example.model.RaceResult
import com.example.model.ShopProduct
import com.example.model.ShopTab
import com.example.model.UpgradeType
import com.example.model.WheelStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val saveManager = SaveManager(application.applicationContext)
    val soundEngine = SoundEngine()

    private val _isDataLoaded = MutableStateFlow(false)
    val isDataLoaded: StateFlow<Boolean> = _isDataLoaded.asStateFlow()

    val saveData: StateFlow<PlayerSaveData> = saveManager.saveDataFlow
        .onEach { data ->
            _isDataLoaded.value = true
            soundEngine.updateSettings(sound = data.soundEnabled, music = data.musicEnabled)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = PlayerSaveData()
        )

    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val screenBackStack = ArrayDeque<AppScreen>()

    // Garage state
    private val _garageTab = MutableStateFlow(GarageTab.CARS)
    val garageTab: StateFlow<GarageTab> = _garageTab.asStateFlow()

    private val _previewCarIndex = MutableStateFlow(0)
    val previewCarIndex: StateFlow<Int> = _previewCarIndex.asStateFlow()

    // Shop state
    private val _shopTab = MutableStateFlow(ShopTab.COINS)
    val shopTab: StateFlow<ShopTab> = _shopTab.asStateFlow()

    // Level Selection pagination (8 levels per page across 20 levels = 3 pages)
    private val _levelPage = MutableStateFlow(0)
    val levelPage: StateFlow<Int> = _levelPage.asStateFlow()

    // Last race result for Win / Lose Screens
    private val _lastRaceResult = MutableStateFlow<RaceResult?>(null)
    val lastRaceResult: StateFlow<RaceResult?> = _lastRaceResult.asStateFlow()

    // Status Banner / Toast Message inside UI
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Modals (Daily Gift, Notifications, Privacy Policy)
    private val _showDailyGiftModal = MutableStateFlow(false)
    val showDailyGiftModal: StateFlow<Boolean> = _showDailyGiftModal.asStateFlow()

    private val _showNotificationsModal = MutableStateFlow(false)
    val showNotificationsModal: StateFlow<Boolean> = _showNotificationsModal.asStateFlow()

    private val _showPrivacyPolicyModal = MutableStateFlow(false)
    val showPrivacyPolicyModal: StateFlow<Boolean> = _showPrivacyPolicyModal.asStateFlow()

    // Track whether current race reward has already been recorded to prevent duplicates
    private var completedRaceRewardRecorded = false

    val gameEngine = GameEngine(
        soundEngine = soundEngine,
        onRaceCompleted = { result ->
            handleRaceCompleted(result)
        }
    )

    fun onSplashFinished() {
        _currentScreen.value = AppScreen.MAIN_MENU
        screenBackStack.clear()
        soundEngine.startMusicLoop()
    }

    fun navigateTo(screen: AppScreen) {
        soundEngine.playButtonClick()
        val current = _currentScreen.value
        if (current == screen) return
        if (current != AppScreen.SPLASH) {
            screenBackStack.addLast(current)
        }
        if (screen == AppScreen.GARAGE) {
            val selectedId = saveData.value.selectedCarId
            val idx = ALL_CARS.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
            _previewCarIndex.value = idx
        }
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        soundEngine.playButtonClick()
        val current = _currentScreen.value
        if (current == AppScreen.GAMEPLAY) {
            if (gameEngine.loopState == GameLoopState.PLAYING) {
                gameEngine.pauseRace()
                return true
            } else if (gameEngine.loopState == GameLoopState.PAUSED) {
                gameEngine.resumeRace()
                return true
            }
        }
        val prev = screenBackStack.removeLastOrNull()
        return if (prev != null) {
            if (current == AppScreen.GAMEPLAY) {
                gameEngine.stopToMenu()
            }
            _currentScreen.value = prev
            true
        } else if (current != AppScreen.MAIN_MENU && current != AppScreen.SPLASH) {
            _currentScreen.value = AppScreen.MAIN_MENU
            true
        } else {
            false
        }
    }

    fun navigateHome() {
        soundEngine.playButtonClick()
        gameEngine.stopToMenu()
        screenBackStack.clear()
        _lastRaceResult.value = null
        _currentScreen.value = AppScreen.MAIN_MENU
    }

    fun openSettingsFromPause() {
        navigateTo(AppScreen.SETTINGS)
    }

    fun startLevel(levelNumber: Int) {
        val data = saveData.value
        if (levelNumber > data.maxUnlockedLevel) {
            showBanner("Complete Level ${levelNumber - 1} first to unlock Level $levelNumber!")
            return
        }
        soundEngine.playButtonClick()
        completedRaceRewardRecorded = false
        _lastRaceResult.value = null

        val levelConfig = LevelManager.getLevelConfig(levelNumber)
        val carSpec = ALL_CARS.find { it.id == data.selectedCarId } ?: ALL_CARS.first()
        val paint = data.getCarPaint(carSpec.id)
        val wheel = data.getCarWheel(carSpec.id)
        val upgrades = data.getCarUpgrades(carSpec.id)
        val hasShield = data.shieldBoosters > 0

        gameEngine.startRace(
            levelConfig = levelConfig,
            selectedCar = carSpec,
            paint = paint,
            wheel = wheel,
            carUpgrades = upgrades,
            startWithShield = hasShield
        )

        if (_currentScreen.value != AppScreen.GAMEPLAY) {
            screenBackStack.addLast(_currentScreen.value)
            _currentScreen.value = AppScreen.GAMEPLAY
        }
    }

    fun restartCurrentLevel() {
        soundEngine.playButtonClick()
        startLevel(gameEngine.currentLevel.levelNumber)
    }

    fun startNextUnlockedLevel() {
        soundEngine.playButtonClick()
        val nextLevel = (gameEngine.currentLevel.levelNumber + 1).coerceAtMost(20)
        startLevel(nextLevel)
    }

    fun pauseGameplay() {
        soundEngine.playButtonClick()
        gameEngine.pauseRace()
    }

    fun resumeGameplay() {
        soundEngine.playButtonClick()
        gameEngine.resumeRace()
    }

    fun useNitroBoosterCharge() {
        if (gameEngine.loopState != GameLoopState.PLAYING) return
        if (gameEngine.nitroCharge > 0.2f) {
            gameEngine.isNitroPressed = true
            return
        }
        viewModelScope.launch {
            if (saveManager.consumeNitroBooster()) {
                gameEngine.refillNitroFromBooster()
                showBanner("Nitro Tank Refilled!")
            } else {
                showBanner("Nitro recharging! Buy extra Nitro Packs in Shop.")
            }
        }
    }

    private fun handleRaceCompleted(result: RaceResult) {
        _lastRaceResult.value = result
        if (result.isWin && !completedRaceRewardRecorded) {
            completedRaceRewardRecorded = true
            viewModelScope.launch {
                saveManager.recordLevelWin(
                    levelNumber = result.levelNumber,
                    starsEarned = result.starsEarned,
                    completionTimeSec = result.completionTimeSec,
                    score = result.finalScore,
                    totalCoinsReward = result.totalCoinsAwarded
                )
            }
        }
    }

    // Level Selection Pagination
    fun nextLevelPage() {
        soundEngine.playButtonClick()
        _levelPage.value = (_levelPage.value + 1).coerceAtMost(2)
    }

    fun prevLevelPage() {
        soundEngine.playButtonClick()
        _levelPage.value = (_levelPage.value - 1).coerceAtLeast(0)
    }

    // Garage Actions
    fun setGarageTab(tab: GarageTab) {
        soundEngine.playButtonClick()
        _garageTab.value = tab
    }

    fun cyclePreviewCar(delta: Int) {
        soundEngine.playButtonClick()
        val count = ALL_CARS.size
        _previewCarIndex.value = (_previewCarIndex.value + delta + count) % count
    }

    fun buyOrSelectPreviewCar() {
        soundEngine.playButtonClick()
        val car = ALL_CARS.getOrElse(_previewCarIndex.value) { ALL_CARS.first() }
        val data = saveData.value
        viewModelScope.launch {
            if (car.id in data.unlockedCarIds) {
                saveManager.selectUnlockedCar(car.id)
                showBanner("${car.name} equipped for racing!")
            } else {
                val ok = saveManager.unlockAndSelectCar(car.id, car.price)
                if (ok) {
                    soundEngine.playCoinCollect()
                    showBanner("Unlocked & Selected ${car.name}!")
                } else {
                    showBanner("Need ${car.price} coins to unlock ${car.name}!")
                }
            }
        }
    }

    fun upgradeCarStat(type: UpgradeType) {
        soundEngine.playButtonClick()
        val car = ALL_CARS.getOrElse(_previewCarIndex.value) { ALL_CARS.first() }
        val data = saveData.value
        if (car.id !in data.unlockedCarIds) {
            showBanner("Unlock ${car.name} first before upgrading!")
            return
        }
        val currentLvl = data.getCarUpgrades(car.id).getLevel(type)
        if (currentLvl >= 5) {
            showBanner("${type.label} is already at MAX Level 5!")
            return
        }
        val cost = type.baseCost * currentLvl
        viewModelScope.launch {
            val ok = saveManager.purchaseUpgrade(car.id, type, cost)
            if (ok) {
                soundEngine.playCoinCollect()
                showBanner("${type.label} upgraded to Level ${currentLvl + 1}!")
            } else {
                showBanner("Insufficient coins! Need $cost coins.")
            }
        }
    }

    fun selectOrBuyPaint(paint: CarPaintColor) {
        soundEngine.playButtonClick()
        val car = ALL_CARS.getOrElse(_previewCarIndex.value) { ALL_CARS.first() }
        if (car.id !in saveData.value.unlockedCarIds) {
            showBanner("Unlock ${car.name} first to customize paint!")
            return
        }
        viewModelScope.launch {
            val ok = saveManager.unlockOrSelectPaint(car.id, paint)
            if (ok) {
                showBanner("${paint.label} paint applied to ${car.name}!")
            } else {
                showBanner("Need ${paint.price} coins for ${paint.label} paint!")
            }
        }
    }

    fun selectOrBuyWheel(wheel: WheelStyle) {
        soundEngine.playButtonClick()
        val car = ALL_CARS.getOrElse(_previewCarIndex.value) { ALL_CARS.first() }
        if (car.id !in saveData.value.unlockedCarIds) {
            showBanner("Unlock ${car.name} first to customize wheels!")
            return
        }
        viewModelScope.launch {
            val ok = saveManager.unlockOrSelectWheel(car.id, wheel)
            if (ok) {
                showBanner("${wheel.label} rims equipped on ${car.name}!")
            } else {
                showBanner("Need ${wheel.price} coins for ${wheel.label} rims!")
            }
        }
    }

    // Shop Actions
    fun setShopTab(tab: ShopTab) {
        soundEngine.playButtonClick()
        _shopTab.value = tab
    }

    fun handleShopProductClick(product: ShopProduct) {
        soundEngine.playButtonClick()
        if (product.isFreeDailyReward) {
            viewModelScope.launch {
                val claimed = saveManager.claimDailyGift(rewardCoins = product.rewardCoins, bonusNitro = 1)
                if (claimed) {
                    soundEngine.playCoinCollect()
                    showBanner("Claimed Free Crate: +${product.rewardCoins} Coins & +1 Nitro!")
                } else {
                    showBanner("Free Crate cooling down! Come back after a race.")
                }
            }
            return
        }

        val coinCost = product.coinCost
        if (coinCost != null) {
            viewModelScope.launch {
                val ok = saveManager.purchaseShopBooster(
                    coinCost = coinCost,
                    rewardCoins = product.rewardCoins,
                    addNitro = product.nitroPackCount,
                    addShield = product.shieldPackCount,
                    refillFuel = product.fuelRefill
                )
                if (ok) {
                    soundEngine.playCoinCollect()
                    showBanner("Purchased ${product.title} (${product.subtitle})!")
                } else {
                    showBanner("Insufficient Coins! Need $coinCost coins.")
                }
            }
        } else {
            // Real-money product: per Section 3 Screen 9, do not pretend real-money purchases succeeded;
            // show clear Google Play Billing integration notice.
            showBanner("Google Play Billing is not configured in this build. Use Free Crate or race rewards to earn coins!")
        }
    }

    // Settings & Modals
    fun toggleSound(enabled: Boolean) {
        soundEngine.playButtonClick()
        viewModelScope.launch { saveManager.setSoundEnabled(enabled) }
    }

    fun toggleMusic(enabled: Boolean) {
        soundEngine.playButtonClick()
        viewModelScope.launch { saveManager.setMusicEnabled(enabled) }
    }

    fun cycleControlScheme(delta: Int) {
        soundEngine.playButtonClick()
        val list = ControlScheme.entries
        val curIdx = list.indexOf(saveData.value.controlScheme).coerceAtLeast(0)
        val next = list[(curIdx + delta + list.size) % list.size]
        viewModelScope.launch { saveManager.setControlScheme(next) }
    }

    fun cycleGraphicsQuality(delta: Int) {
        soundEngine.playButtonClick()
        val list = GraphicsQuality.entries
        val curIdx = list.indexOf(saveData.value.graphicsQuality).coerceAtLeast(0)
        val next = list[(curIdx + delta + list.size) % list.size]
        viewModelScope.launch { saveManager.setGraphicsQuality(next) }
    }

    fun cycleLanguage(delta: Int) {
        soundEngine.playButtonClick()
        val list = GameLanguage.entries
        val curIdx = list.indexOf(saveData.value.language).coerceAtLeast(0)
        val next = list[(curIdx + delta + list.size) % list.size]
        viewModelScope.launch { saveManager.setLanguage(next) }
    }

    fun setDailyGiftModalVisible(visible: Boolean) {
        soundEngine.playButtonClick()
        _showDailyGiftModal.value = visible
    }

    fun claimDailyGiftFromModal() {
        viewModelScope.launch {
            val claimed = saveManager.claimDailyGift(rewardCoins = 300, bonusNitro = 1)
            _showDailyGiftModal.value = false
            if (claimed) {
                soundEngine.playCoinCollect()
                showBanner("Daily Gift Claimed: +300 Coins & +1 Nitro Booster!")
            } else {
                showBanner("Daily Gift already claimed recently! Play a race and check back soon.")
            }
        }
    }

    fun setNotificationsModalVisible(visible: Boolean) {
        soundEngine.playButtonClick()
        _showNotificationsModal.value = visible
    }

    fun setPrivacyPolicyModalVisible(visible: Boolean) {
        soundEngine.playButtonClick()
        _showPrivacyPolicyModal.value = visible
    }

    fun showBanner(message: String) {
        _statusMessage.value = message
    }

    fun clearBanner() {
        _statusMessage.value = null
    }

    fun onAppPaused() {
        soundEngine.setForegroundState(false)
        if (_currentScreen.value == AppScreen.GAMEPLAY && gameEngine.loopState == GameLoopState.PLAYING) {
            gameEngine.pauseRace()
        }
    }

    fun onAppResumed() {
        soundEngine.setForegroundState(true)
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.stopMusicLoop()
    }
}
