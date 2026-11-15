package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ALL_CARS
import com.example.model.CarPaintColor
import com.example.model.ControlScheme
import com.example.model.GameLanguage
import com.example.model.GraphicsQuality
import com.example.model.LevelProgress
import com.example.model.UpgradeLevels
import com.example.model.UpgradeType
import com.example.model.WheelStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "car_racing_2d_prefs")

data class PlayerSaveData(
    val coins: Int = 5000,
    val maxUnlockedLevel: Int = 1,
    val selectedCarId: String = "red_viper",
    val unlockedCarIds: Set<String> = setOf("red_viper"),
    val unlockedPaintIds: Set<String> = setOf(CarPaintColor.CRIMSON_RED.id),
    val unlockedWheelIds: Set<String> = setOf(WheelStyle.SPORT_ALLOY.id),
    val selectedPaintByCar: Map<String, CarPaintColor> = emptyMap(),
    val selectedWheelByCar: Map<String, WheelStyle> = emptyMap(),
    val upgradesByCar: Map<String, UpgradeLevels> = emptyMap(),
    val levelProgressMap: Map<Int, LevelProgress> = emptyMap(),
    val nitroBoosters: Int = 3,
    val shieldBoosters: Int = 1,
    val fuelPercent: Int = 100,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val controlScheme: ControlScheme = ControlScheme.LEFT_RIGHT,
    val graphicsQuality: GraphicsQuality = GraphicsQuality.HIGH,
    val language: GameLanguage = GameLanguage.ENGLISH,
    val lastDailyGiftClaimEpochMs: Long = 0L
) {
    fun getCarUpgrades(carId: String): UpgradeLevels {
        return upgradesByCar[carId] ?: UpgradeLevels()
    }

    fun getCarPaint(carId: String): CarPaintColor {
        val defaultPaint = ALL_CARS.find { it.id == carId }?.defaultPaint ?: CarPaintColor.CRIMSON_RED
        return selectedPaintByCar[carId] ?: defaultPaint
    }

    fun getCarWheel(carId: String): WheelStyle {
        return selectedWheelByCar[carId] ?: WheelStyle.SPORT_ALLOY
    }

    fun getLevelProgress(levelNumber: Int): LevelProgress {
        return levelProgressMap[levelNumber] ?: LevelProgress(
            levelNumber = levelNumber,
            isUnlocked = levelNumber <= maxUnlockedLevel
        )
    }
}

class SaveManager(private val context: Context) {

    companion object {
        private val KEY_COINS = intPreferencesKey("coins")
        private val KEY_MAX_UNLOCKED_LEVEL = intPreferencesKey("max_unlocked_level")
        private val KEY_SELECTED_CAR = stringPreferencesKey("selected_car_id")
        private val KEY_UNLOCKED_CARS = stringSetPreferencesKey("unlocked_car_ids")
        private val KEY_UNLOCKED_PAINTS = stringSetPreferencesKey("unlocked_paint_ids")
        private val KEY_UNLOCKED_WHEELS = stringSetPreferencesKey("unlocked_wheel_ids")
        private val KEY_NITRO_BOOSTERS = intPreferencesKey("nitro_boosters")
        private val KEY_SHIELD_BOOSTERS = intPreferencesKey("shield_boosters")
        private val KEY_FUEL_PERCENT = intPreferencesKey("fuel_percent")
        private val KEY_SOUND = booleanPreferencesKey("sound_enabled")
        private val KEY_MUSIC = booleanPreferencesKey("music_enabled")
        private val KEY_CONTROLS = stringPreferencesKey("control_scheme")
        private val KEY_GRAPHICS = stringPreferencesKey("graphics_quality")
        private val KEY_LANGUAGE = stringPreferencesKey("game_language")
        private val KEY_LAST_GIFT_MS = longPreferencesKey("last_daily_gift_ms")

        private fun keyLevelStars(level: Int) = intPreferencesKey("level_${level}_stars")
        private fun keyLevelBestTime(level: Int) = floatPreferencesKey("level_${level}_best_time")
        private fun keyLevelBestScore(level: Int) = intPreferencesKey("level_${level}_best_score")
        private fun keyLevelCompleted(level: Int) = booleanPreferencesKey("level_${level}_completed")

        private fun keyCarUpgrade(carId: String, type: UpgradeType) =
            intPreferencesKey("upgrade_${carId}_${type.key}")

        private fun keyCarPaint(carId: String) = stringPreferencesKey("paint_${carId}")
        private fun keyCarWheel(carId: String) = stringPreferencesKey("wheel_${carId}")
    }

    val saveDataFlow: Flow<PlayerSaveData> = context.dataStore.data.map { prefs ->
        val coins = (prefs[KEY_COINS] ?: 5000).coerceAtLeast(0)
        val maxUnlocked = (prefs[KEY_MAX_UNLOCKED_LEVEL] ?: 1).coerceIn(1, 20)
        val unlockedCars = (prefs[KEY_UNLOCKED_CARS] ?: setOf("red_viper")) + "red_viper"
        val selectedCar = prefs[KEY_SELECTED_CAR]?.takeIf { it in unlockedCars } ?: "red_viper"

        val defaultUnlockedPaints = ALL_CARS.filter { it.id in unlockedCars }.map { it.defaultPaint.id }.toSet()
        val unlockedPaints = (prefs[KEY_UNLOCKED_PAINTS] ?: emptySet()) + defaultUnlockedPaints + CarPaintColor.CRIMSON_RED.id
        val unlockedWheels = (prefs[KEY_UNLOCKED_WHEELS] ?: emptySet()) + WheelStyle.SPORT_ALLOY.id

        val upgradesMap = ALL_CARS.associate { car ->
            val levels = UpgradeLevels(
                engine = (prefs[keyCarUpgrade(car.id, UpgradeType.ENGINE)] ?: 1).coerceIn(1, 5),
                speed = (prefs[keyCarUpgrade(car.id, UpgradeType.SPEED)] ?: 1).coerceIn(1, 5),
                handling = (prefs[keyCarUpgrade(car.id, UpgradeType.HANDLING)] ?: 1).coerceIn(1, 5),
                acceleration = (prefs[keyCarUpgrade(car.id, UpgradeType.ACCELERATION)] ?: 1).coerceIn(1, 5),
                braking = (prefs[keyCarUpgrade(car.id, UpgradeType.BRAKING)] ?: 1).coerceIn(1, 5)
            )
            car.id to levels
        }

        val paintsMap = ALL_CARS.associate { car ->
            val paintId = prefs[keyCarPaint(car.id)]
            val paint = CarPaintColor.entries.find { it.id == paintId } ?: car.defaultPaint
            car.id to paint
        }

        val wheelsMap = ALL_CARS.associate { car ->
            val wheelId = prefs[keyCarWheel(car.id)]
            val wheel = WheelStyle.entries.find { it.id == wheelId } ?: WheelStyle.SPORT_ALLOY
            car.id to wheel
        }

        val levelsMap = (1..20).associateWith { lvl ->
            val stars = (prefs[keyLevelStars(lvl)] ?: 0).coerceIn(0, 3)
            val bestTime = prefs[keyLevelBestTime(lvl)] ?: 0f
            val bestScore = prefs[keyLevelBestScore(lvl)] ?: 0
            val completed = prefs[keyLevelCompleted(lvl)] ?: (stars > 0)
            LevelProgress(
                levelNumber = lvl,
                isUnlocked = lvl <= maxUnlocked,
                isCompleted = completed,
                stars = stars,
                bestTimeSec = bestTime,
                bestScore = bestScore
            )
        }

        val controlScheme = ControlScheme.entries.find { it.name == prefs[KEY_CONTROLS] }
            ?: ControlScheme.LEFT_RIGHT
        val graphicsQuality = GraphicsQuality.entries.find { it.name == prefs[KEY_GRAPHICS] }
            ?: GraphicsQuality.HIGH
        val language = GameLanguage.entries.find { it.name == prefs[KEY_LANGUAGE] }
            ?: GameLanguage.ENGLISH

        PlayerSaveData(
            coins = coins,
            maxUnlockedLevel = maxUnlocked,
            selectedCarId = selectedCar,
            unlockedCarIds = unlockedCars,
            unlockedPaintIds = unlockedPaints,
            unlockedWheelIds = unlockedWheels,
            selectedPaintByCar = paintsMap,
            selectedWheelByCar = wheelsMap,
            upgradesByCar = upgradesMap,
            levelProgressMap = levelsMap,
            nitroBoosters = (prefs[KEY_NITRO_BOOSTERS] ?: 3).coerceAtLeast(0),
            shieldBoosters = (prefs[KEY_SHIELD_BOOSTERS] ?: 1).coerceAtLeast(0),
            fuelPercent = (prefs[KEY_FUEL_PERCENT] ?: 100).coerceIn(0, 100),
            soundEnabled = prefs[KEY_SOUND] ?: true,
            musicEnabled = prefs[KEY_MUSIC] ?: true,
            controlScheme = controlScheme,
            graphicsQuality = graphicsQuality,
            language = language,
            lastDailyGiftClaimEpochMs = prefs[KEY_LAST_GIFT_MS] ?: 0L
        )
    }

    suspend fun addCoins(amount: Int) {
        if (amount <= 0) return
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_COINS] ?: 5000).coerceAtLeast(0)
            prefs[KEY_COINS] = current + amount
        }
    }

    suspend fun spendCoins(amount: Int): Boolean {
        if (amount < 0) return false
        var success = false
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_COINS] ?: 5000).coerceAtLeast(0)
            if (current >= amount) {
                prefs[KEY_COINS] = current - amount
                success = true
            }
        }
        return success
    }

    suspend fun recordLevelWin(
        levelNumber: Int,
        starsEarned: Int,
        completionTimeSec: Float,
        score: Int,
        totalCoinsReward: Int
    ) {
        context.dataStore.edit { prefs ->
            val currentCoins = (prefs[KEY_COINS] ?: 5000).coerceAtLeast(0)
            prefs[KEY_COINS] = currentCoins + totalCoinsReward.coerceAtLeast(0)

            val currentMax = (prefs[KEY_MAX_UNLOCKED_LEVEL] ?: 1).coerceIn(1, 20)
            if (levelNumber >= currentMax && levelNumber < 20) {
                prefs[KEY_MAX_UNLOCKED_LEVEL] = (levelNumber + 1).coerceAtMost(20)
            }

            prefs[keyLevelCompleted(levelNumber)] = true
            val prevStars = prefs[keyLevelStars(levelNumber)] ?: 0
            if (starsEarned > prevStars) {
                prefs[keyLevelStars(levelNumber)] = starsEarned.coerceIn(1, 3)
            }
            val prevTime = prefs[keyLevelBestTime(levelNumber)] ?: 0f
            if (prevTime <= 0f || completionTimeSec < prevTime) {
                prefs[keyLevelBestTime(levelNumber)] = completionTimeSec
            }
            val prevScore = prefs[keyLevelBestScore(levelNumber)] ?: 0
            if (score > prevScore) {
                prefs[keyLevelBestScore(levelNumber)] = score
            }
        }
    }

    suspend fun unlockAndSelectCar(carId: String, price: Int): Boolean {
        var purchased = false
        context.dataStore.edit { prefs ->
            val unlocked = (prefs[KEY_UNLOCKED_CARS] ?: setOf("red_viper")) + "red_viper"
            if (carId in unlocked) {
                prefs[KEY_SELECTED_CAR] = carId
                purchased = true
            } else {
                val currentCoins = (prefs[KEY_COINS] ?: 5000).coerceAtLeast(0)
                if (currentCoins >= price) {
                    prefs[KEY_COINS] = currentCoins - price
                    prefs[KEY_UNLOCKED_CARS] = unlocked + carId
                    prefs[KEY_SELECTED_CAR] = carId
                    purchased = true
                }
            }
        }
        return purchased
    }

    suspend fun selectUnlockedCar(carId: String): Boolean {
        var selected = false
        context.dataStore.edit { prefs ->
            val unlocked = (prefs[KEY_UNLOCKED_CARS] ?: setOf("red_viper")) + "red_viper"
            if (carId in unlocked) {
                prefs[KEY_SELECTED_CAR] = carId
                selected = true
            }
        }
        return selected
    }

    suspend fun purchaseUpgrade(carId: String, type: UpgradeType, cost: Int): Boolean {
        var upgraded = false
        context.dataStore.edit { prefs ->
            val unlocked = (prefs[KEY_UNLOCKED_CARS] ?: setOf("red_viper")) + "red_viper"
            if (carId !in unlocked) return@edit
            val currentLvl = (prefs[keyCarUpgrade(carId, type)] ?: 1).coerceIn(1, 5)
            val currentCoins = (prefs[KEY_COINS] ?: 5000).coerceAtLeast(0)
            if (currentLvl < 5 && currentCoins >= cost) {
                prefs[KEY_COINS] = currentCoins - cost
                prefs[keyCarUpgrade(carId, type)] = currentLvl + 1
                upgraded = true
            }
        }
        return upgraded
    }

    suspend fun unlockOrSelectPaint(carId: String, paint: CarPaintColor): Boolean {
        var ok = false
        context.dataStore.edit { prefs ->
            val unlockedPaints = (prefs[KEY_UNLOCKED_PAINTS] ?: emptySet()) + CarPaintColor.CRIMSON_RED.id
            if (paint.id in unlockedPaints || paint.price == 0) {
                prefs[keyCarPaint(carId)] = paint.id
                ok = true
            } else {
                val coins = (prefs[KEY_COINS] ?: 5000).coerceAtLeast(0)
                if (coins >= paint.price) {
                    prefs[KEY_COINS] = coins - paint.price
                    prefs[KEY_UNLOCKED_PAINTS] = unlockedPaints + paint.id
                    prefs[keyCarPaint(carId)] = paint.id
                    ok = true
                }
            }
        }
        return ok
    }

    suspend fun unlockOrSelectWheel(carId: String, wheel: WheelStyle): Boolean {
        var ok = false
        context.dataStore.edit { prefs ->
            val unlockedWheels = (prefs[KEY_UNLOCKED_WHEELS] ?: emptySet()) + WheelStyle.SPORT_ALLOY.id
            if (wheel.id in unlockedWheels || wheel.price == 0) {
                prefs[keyCarWheel(carId)] = wheel.id
                ok = true
            } else {
                val coins = (prefs[KEY_COINS] ?: 5000).coerceAtLeast(0)
                if (coins >= wheel.price) {
                    prefs[KEY_COINS] = coins - wheel.price
                    prefs[KEY_UNLOCKED_WHEELS] = unlockedWheels + wheel.id
                    prefs[keyCarWheel(carId)] = wheel.id
                    ok = true
                }
            }
        }
        return ok
    }

    suspend fun purchaseShopBooster(
        coinCost: Int,
        rewardCoins: Int,
        addNitro: Int,
        addShield: Int,
        refillFuel: Boolean
    ): Boolean {
        var ok = false
        context.dataStore.edit { prefs ->
            val coins = (prefs[KEY_COINS] ?: 5000).coerceAtLeast(0)
            if (coins >= coinCost) {
                prefs[KEY_COINS] = coins - coinCost + rewardCoins
                if (addNitro > 0) {
                    val cur = (prefs[KEY_NITRO_BOOSTERS] ?: 3).coerceAtLeast(0)
                    prefs[KEY_NITRO_BOOSTERS] = cur + addNitro
                }
                if (addShield > 0) {
                    val cur = (prefs[KEY_SHIELD_BOOSTERS] ?: 1).coerceAtLeast(0)
                    prefs[KEY_SHIELD_BOOSTERS] = cur + addShield
                }
                if (refillFuel) {
                    prefs[KEY_FUEL_PERCENT] = 100
                }
                ok = true
            }
        }
        return ok
    }

    suspend fun consumeNitroBooster(): Boolean {
        var consumed = false
        context.dataStore.edit { prefs ->
            val cur = (prefs[KEY_NITRO_BOOSTERS] ?: 3).coerceAtLeast(0)
            if (cur > 0) {
                prefs[KEY_NITRO_BOOSTERS] = cur - 1
                consumed = true
            }
        }
        return consumed
    }

    suspend fun claimDailyGift(rewardCoins: Int, bonusNitro: Int): Boolean {
        var claimed = false
        context.dataStore.edit { prefs ->
            val now = System.currentTimeMillis()
            val last = prefs[KEY_LAST_GIFT_MS] ?: 0L
            // Allow claiming if never claimed or at least 5 minutes have elapsed for easy playtesting
            if (now - last >= 5 * 60 * 1000L || last == 0L) {
                val coins = (prefs[KEY_COINS] ?: 5000).coerceAtLeast(0)
                prefs[KEY_COINS] = coins + rewardCoins
                val nitro = (prefs[KEY_NITRO_BOOSTERS] ?: 3).coerceAtLeast(0)
                prefs[KEY_NITRO_BOOSTERS] = nitro + bonusNitro
                prefs[KEY_LAST_GIFT_MS] = now
                claimed = true
            }
        }
        return claimed
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SOUND] = enabled }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_MUSIC] = enabled }
    }

    suspend fun setControlScheme(scheme: ControlScheme) {
        context.dataStore.edit { it[KEY_CONTROLS] = scheme.name }
    }

    suspend fun setGraphicsQuality(quality: GraphicsQuality) {
        context.dataStore.edit { it[KEY_GRAPHICS] = quality.name }
    }

    suspend fun setLanguage(language: GameLanguage) {
        context.dataStore.edit { it[KEY_LANGUAGE] = language.name }
    }
}
