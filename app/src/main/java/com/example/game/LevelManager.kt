package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.model.LevelConfig
import com.example.model.ShopIconType
import com.example.model.ShopProduct
import com.example.model.ShopTab

object LevelManager {
    private val skyPalettes = listOf(
        Triple(Color(0xFF1565C0), Color(0xFF64B5F6), Color(0xFF1E3C72)), // Bright Day Sky (matches Screen 4 reference!)
        Triple(Color(0xFF0D47A1), Color(0xFF42A5F5), Color(0xFF162A5A)), // Metro Blue
        Triple(Color(0xFF283593), Color(0xFF5C6BC0), Color(0xFF1A237E)), // Twilight Indigo
        Triple(Color(0xFF4A148C), Color(0xFFFF6F00), Color(0xFF2A0845)), // Sunset Horizon
        Triple(Color(0xFF050A1F), Color(0xFF1A2980), Color(0xFF0B163B))  // Midnight Neon
    )

    private val levelNames = listOf(
        "Metro City Sprint",
        "Sunshine Boulevard",
        "Harbor Bridge Dash",
        "Downtown Overpass",
        "Sunset Expressway",
        "Neon Tunnel Rush",
        "Bayshore Highway",
        "Industrial Circuit",
        "Apex Skyway",
        "Cyber District",
        "Thunder Valley",
        "Coastal Drift",
        "Grand Prix Avenue",
        "Midnight Tuners",
        "Velocity Loop",
        "Emerald Freeway",
        "Turbo Viaduct",
        "Starlight Turnpike",
        "Hyperion Mile",
        "Champion's Finale"
    )

    val levels: List<LevelConfig> = (1..20).map { num ->
        val palette = skyPalettes[(num - 1) % skyPalettes.size]
        val distance = 750f + (num - 1) * 160f
        val targetOvertakes = 3 + ((num - 1) / 2)
        // Base time allowance scales with distance so 3-star time is achievable (~40-60s)
        val threeStarSec = 28f + (num - 1) * 3.8f
        val twoStarSec = threeStarSec * 1.28f
        val trafficDensity = (0.45f + (num - 1) * 0.025f).coerceAtMost(0.92f)
        val obstacleChance = (0.18f + (num - 1) * 0.018f).coerceAtMost(0.50f)
        val baseReward = when {
            num <= 5 -> 100 + (num - 1) * 15
            num <= 12 -> 180 + (num - 6) * 25
            else -> 360 + (num - 13) * 40
        }
        LevelConfig(
            levelNumber = num,
            title = levelNames[num - 1],
            distanceMeters = distance,
            targetOvertakes = targetOvertakes,
            threeStarTimeSec = threeStarSec,
            twoStarTimeSec = twoStarSec,
            trafficDensity = trafficDensity,
            obstacleChance = obstacleChance,
            baseRewardCoins = baseReward,
            skyTopColor = palette.first,
            skyBottomColor = palette.second,
            cityColor = palette.third
        )
    }

    fun getLevelConfig(levelNumber: Int): LevelConfig {
        return levels.getOrElse(levelNumber - 1) { levels.first() }
    }

    fun calculateStars(level: LevelConfig, completionTimeSec: Float, healthPercent: Float): Int {
        return when {
            completionTimeSec <= level.threeStarTimeSec && healthPercent >= 0.4f -> 3
            completionTimeSec <= level.twoStarTimeSec -> 2
            else -> 1
        }
    }

    fun calculateStarReward(stars: Int, levelNumber: Int): Int {
        // Section 5: Bronze: 50 coins, Silver: 100 coins, Gold: 150 coins + level progression scaling
        val tierBase = when (stars) {
            3 -> 150
            2 -> 100
            else -> 50
        }
        return tierBase + (levelNumber - 1) * 10
    }

    val shopProducts: List<ShopProduct> = listOf(
        // COINS TAB — First 3 cards match Screen 9 reference image ("Coin Pack +1,000", "Fuel Pack +Full Tank", "No Ads Remove Ads"), plus Free Daily Bonus
        ShopProduct(
            id = "coin_pack_1000",
            tab = ShopTab.COINS,
            title = "Coin Pack",
            subtitle = "+1,000",
            iconType = ShopIconType.COIN_STACK,
            coinCost = null,
            realMoneyLabel = "৳ 0.99",
            rewardCoins = 1000
        ),
        ShopProduct(
            id = "fuel_pack_full",
            tab = ShopTab.COINS,
            title = "Fuel Pack",
            subtitle = "+Full Tank",
            iconType = ShopIconType.FUEL_CAN,
            coinCost = 250,
            realMoneyLabel = null,
            fuelRefill = true
        ),
        ShopProduct(
            id = "no_ads_bundle",
            tab = ShopTab.COINS,
            title = "No Ads",
            subtitle = "Remove Ads",
            iconType = ShopIconType.NO_ADS,
            coinCost = null,
            realMoneyLabel = "৳ 1.99"
        ),
        // BOOSTERS TAB — Purchasable with in-game coins!
        ShopProduct(
            id = "booster_nitro_3x",
            tab = ShopTab.BOOSTERS,
            title = "Nitro Pack",
            subtitle = "+3 Super Nitro",
            iconType = ShopIconType.NITRO_BOTTLE,
            coinCost = 300,
            realMoneyLabel = null,
            nitroPackCount = 3
        ),
        ShopProduct(
            id = "booster_shield_3x",
            tab = ShopTab.BOOSTERS,
            title = "Armor Shield",
            subtitle = "+3 Crash Shields",
            iconType = ShopIconType.SHIELD_CORE,
            coinCost = 400,
            realMoneyLabel = null,
            shieldPackCount = 3
        ),
        ShopProduct(
            id = "booster_fuel_refill",
            tab = ShopTab.BOOSTERS,
            title = "Racing Fuel",
            subtitle = "100% Octane Tank",
            iconType = ShopIconType.FUEL_CAN,
            coinCost = 200,
            realMoneyLabel = null,
            fuelRefill = true
        ),
        // SPECIAL TAB
        ShopProduct(
            id = "special_pro_bundle",
            tab = ShopTab.SPECIAL,
            title = "Racer Bundle",
            subtitle = "+5 Nitro & +5 Shield",
            iconType = ShopIconType.VIP_PASS,
            coinCost = 950,
            realMoneyLabel = null,
            nitroPackCount = 5,
            shieldPackCount = 5,
            fuelRefill = true
        ),
        ShopProduct(
            id = "special_mega_chest",
            tab = ShopTab.SPECIAL,
            title = "Champion Vault",
            subtitle = "+5,000 Coins",
            iconType = ShopIconType.COIN_CHEST,
            coinCost = null,
            realMoneyLabel = "৳ 3.99",
            rewardCoins = 5000
        ),
        ShopProduct(
            id = "special_remove_ads",
            tab = ShopTab.SPECIAL,
            title = "No Ads VIP",
            subtitle = "Remove Ads + 500 Coins",
            iconType = ShopIconType.NO_ADS,
            coinCost = null,
            realMoneyLabel = "৳ 1.99",
            rewardCoins = 500
        )
    )
}
