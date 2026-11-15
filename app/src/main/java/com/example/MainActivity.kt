package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.GameViewModel
import com.example.model.AppScreen
import com.example.ui.screens.GarageScreen
import com.example.ui.screens.GameplayScreen
import com.example.ui.screens.LevelSelectionScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.MoreGamesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.CarRacing2DTheme
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.RacingYellow
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        enableEdgeToEdge()
        setContent {
            CarRacing2DTheme {
                CarRacing2DApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CarRacing2DApp(viewModel: GameViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> viewModel.onAppPaused()
                Lifecycle.Event.ON_RESUME -> viewModel.onAppResumed()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isDataLoaded by viewModel.isDataLoaded.collectAsStateWithLifecycle()
    val saveData by viewModel.saveData.collectAsStateWithLifecycle()
    val garageTab by viewModel.garageTab.collectAsStateWithLifecycle()
    val previewCarIndex by viewModel.previewCarIndex.collectAsStateWithLifecycle()
    val shopTab by viewModel.shopTab.collectAsStateWithLifecycle()
    val levelPage by viewModel.levelPage.collectAsStateWithLifecycle()
    val lastRaceResult by viewModel.lastRaceResult.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val showGiftModal by viewModel.showDailyGiftModal.collectAsStateWithLifecycle()
    val showNotificationsModal by viewModel.showNotificationsModal.collectAsStateWithLifecycle()
    val showPrivacyModal by viewModel.showPrivacyPolicyModal.collectAsStateWithLifecycle()

    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            delay(2600L)
            viewModel.clearBanner()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            AppScreen.SPLASH -> {
                SplashScreen(
                    isDataLoaded = isDataLoaded,
                    selectedCarId = saveData.selectedCarId,
                    selectedPaint = saveData.getCarPaint(saveData.selectedCarId),
                    selectedWheel = saveData.getCarWheel(saveData.selectedCarId),
                    onLoadingFinished = { viewModel.onSplashFinished() }
                )
            }

            AppScreen.MAIN_MENU -> {
                MainMenuScreen(
                    saveData = saveData,
                    showGiftModal = showGiftModal,
                    showNotificationsModal = showNotificationsModal,
                    onNavigate = { screen -> viewModel.navigateTo(screen) },
                    onToggleGiftModal = { visible -> viewModel.setDailyGiftModalVisible(visible) },
                    onClaimDailyGift = { viewModel.claimDailyGiftFromModal() },
                    onToggleNotificationsModal = { visible -> viewModel.setNotificationsModalVisible(visible) },
                    onShowMessage = { msg -> viewModel.showBanner(msg) }
                )
            }

            AppScreen.LEVEL_SELECTION -> {
                LevelSelectionScreen(
                    saveData = saveData,
                    currentPage = levelPage,
                    onSelectLevel = { levelNum -> viewModel.startLevel(levelNum) },
                    onPrevPage = { viewModel.prevLevelPage() },
                    onNextPage = { viewModel.nextLevelPage() },
                    onBack = { viewModel.navigateBack() },
                    onHome = { viewModel.navigateHome() },
                    onOpenShop = { viewModel.navigateTo(AppScreen.SHOP) }
                )
            }

            AppScreen.GAMEPLAY -> {
                GameplayScreen(
                    gameEngine = viewModel.gameEngine,
                    controlScheme = saveData.controlScheme,
                    graphicsQuality = saveData.graphicsQuality,
                    nitroBoostersCount = saveData.nitroBoosters,
                    lastRaceResult = lastRaceResult,
                    onPause = { viewModel.pauseGameplay() },
                    onResume = { viewModel.resumeGameplay() },
                    onRestart = { viewModel.restartCurrentLevel() },
                    onNextLevel = { viewModel.startNextUnlockedLevel() },
                    onHome = { viewModel.navigateHome() },
                    onOpenSettings = { viewModel.openSettingsFromPause() },
                    onUseNitroBooster = { viewModel.useNitroBoosterCharge() }
                )
            }

            AppScreen.GARAGE -> {
                GarageScreen(
                    saveData = saveData,
                    currentTab = garageTab,
                    previewCarIndex = previewCarIndex,
                    onSelectTab = { tab -> viewModel.setGarageTab(tab) },
                    onPrevCar = { viewModel.cyclePreviewCar(-1) },
                    onNextCar = { viewModel.cyclePreviewCar(1) },
                    onBuyOrSelectCar = { viewModel.buyOrSelectPreviewCar() },
                    onUpgradeStat = { type -> viewModel.upgradeCarStat(type) },
                    onSelectOrBuyPaint = { paint -> viewModel.selectOrBuyPaint(paint) },
                    onSelectOrBuyWheel = { wheel -> viewModel.selectOrBuyWheel(wheel) },
                    onOpenShop = { viewModel.navigateTo(AppScreen.SHOP) },
                    onHome = { viewModel.navigateHome() },
                    onBack = { viewModel.navigateBack() }
                )
            }

            AppScreen.SHOP -> {
                ShopScreen(
                    saveData = saveData,
                    currentTab = shopTab,
                    onSelectTab = { tab -> viewModel.setShopTab(tab) },
                    onProductClick = { product -> viewModel.handleShopProductClick(product) },
                    onHome = { viewModel.navigateHome() },
                    onBack = { viewModel.navigateBack() }
                )
            }

            AppScreen.SETTINGS -> {
                SettingsScreen(
                    saveData = saveData,
                    showPrivacyModal = showPrivacyModal,
                    onToggleSound = { enabled -> viewModel.toggleSound(enabled) },
                    onToggleMusic = { enabled -> viewModel.toggleMusic(enabled) },
                    onCycleControls = { delta -> viewModel.cycleControlScheme(delta) },
                    onCycleGraphics = { delta -> viewModel.cycleGraphicsQuality(delta) },
                    onCycleLanguage = { delta -> viewModel.cycleLanguage(delta) },
                    onOpenPrivacyPolicy = { visible -> viewModel.setPrivacyPolicyModalVisible(visible) },
                    onOpenMoreGames = { viewModel.navigateTo(AppScreen.MORE_GAMES) },
                    onBack = { viewModel.navigateBack() }
                )
            }

            AppScreen.MORE_GAMES -> {
                MoreGamesScreen(
                    onBack = { viewModel.navigateBack() },
                    onShowMessage = { msg -> viewModel.showBanner(msg) }
                )
            }
        }

        // Top-Center Arcade Status Banner
        AnimatedVisibility(
            visible = statusMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp)
        ) {
            statusMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(NavyDark.copy(alpha = 0.94f))
                        .border(1.5.dp, NeonBlue, RoundedCornerShape(20.dp))
                        .clickable { viewModel.clearBanner() }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = RacingYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }
    }
}
