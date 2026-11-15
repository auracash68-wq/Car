package com.example.model

import androidx.compose.ui.graphics.Color

enum class AppScreen {
    SPLASH,
    MAIN_MENU,
    LEVEL_SELECTION,
    GAMEPLAY,
    GARAGE,
    SHOP,
    SETTINGS,
    MORE_GAMES
}

enum class GameLoopState {
    MENU,
    LOADING,
    PLAYING,
    PAUSED,
    LEVEL_COMPLETED,
    GAME_OVER
}

enum class GarageTab {
    CARS,
    UPGRADE,
    CUSTOMIZE
}

enum class ShopTab {
    COINS,
    BOOSTERS,
    SPECIAL
}

enum class ControlScheme(val displayName: String) {
    LEFT_RIGHT("Left / Right"),
    UP_DOWN("Up / Down"),
    TOUCH_LANE("Touch Lane")
}

enum class GraphicsQuality(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High")
}

enum class GameLanguage(val displayName: String) {
    ENGLISH("English"),
    SPANISH("Español"),
    FRENCH("Français"),
    GERMAN("Deutsch")
}

enum class CarPaintColor(
    val id: String,
    val label: String,
    val primaryHex: Color,
    val secondaryHex: Color,
    val highlightHex: Color,
    val price: Int
) {
    CRIMSON_RED("crimson_red", "Racing Red", Color(0xFFE50914), Color(0xFF8E0000), Color(0xFFFF6F60), 0),
    NEON_BLUE("neon_blue", "Cobalt Neon", Color(0xFF00B0FF), Color(0xFF005ECB), Color(0xFF80D8FF), 350),
    SOLAR_YELLOW("solar_yellow", "Solar Gold", Color(0xFFFFD600), Color(0xFFFF6D00), Color(0xFFFFFF8D), 450),
    VENOM_GREEN("venom_green", "Venom Lime", Color(0xFF00E676), Color(0xFF007E33), Color(0xFFB9F6CA), 550),
    CYBER_PURPLE("cyber_purple", "Cyber Violet", Color(0xFFAA00FF), Color(0xFF4A148C), Color(0xFFEA80FC), 650),
    PEARL_WHITE("pearl_white", "Glacier Pearl", Color(0xFFECEFF1), Color(0xFF90A4AE), Color(0xFFFFFFFF), 800)
}

enum class WheelStyle(
    val id: String,
    val label: String,
    val rimColor: Color,
    val spokeCount: Int,
    val price: Int
) {
    SPORT_ALLOY("sport_alloy", "Sport Alloy", Color(0xFFCFD8DC), 5, 0),
    GOLD_TURBO("gold_turbo", "Gold Apex", Color(0xFFFFD600), 6, 400),
    NEON_CYBER("neon_cyber", "Cyan Pulse", Color(0xFF00E5FF), 8, 600),
    CARBON_BLADE("carbon_blade", "Crimson Forged", Color(0xFFFF1744), 10, 750)
}

enum class UpgradeType(val key: String, val label: String, val baseCost: Int) {
    ENGINE("engine", "Engine", 250),
    SPEED("speed", "Speed", 300),
    HANDLING("handling", "Handling", 200),
    ACCELERATION("acceleration", "Acceleration", 250),
    BRAKING("braking", "Braking", 200)
}

data class UpgradeLevels(
    val engine: Int = 1,
    val speed: Int = 1,
    val handling: Int = 1,
    val acceleration: Int = 1,
    val braking: Int = 1
) {
    fun getLevel(type: UpgradeType): Int = when (type) {
        UpgradeType.ENGINE -> engine
        UpgradeType.SPEED -> speed
        UpgradeType.HANDLING -> handling
        UpgradeType.ACCELERATION -> acceleration
        UpgradeType.BRAKING -> braking
    }

    fun increment(type: UpgradeType): UpgradeLevels = when (type) {
        UpgradeType.ENGINE -> copy(engine = (engine + 1).coerceAtMost(5))
        UpgradeType.SPEED -> copy(speed = (speed + 1).coerceAtMost(5))
        UpgradeType.HANDLING -> copy(handling = (handling + 1).coerceAtMost(5))
        UpgradeType.ACCELERATION -> copy(acceleration = (acceleration + 1).coerceAtMost(5))
        UpgradeType.BRAKING -> copy(braking = (braking + 1).coerceAtMost(5))
    }
}

data class CarSpec(
    val id: String,
    val name: String,
    val subtitle: String,
    val price: Int,
    val defaultPaint: CarPaintColor,
    val baseSpeed: Float,        // 0.4f..1.0f normalized
    val baseHandling: Float,     // 0.4f..1.0f normalized
    val baseAcceleration: Float, // 0.4f..1.0f normalized
    val baseBraking: Float,      // 0.4f..1.0f normalized
    val maxKmh: Int,
    val spoilerStyle: Int = 1
)

val ALL_CARS = listOf(
    CarSpec(
        id = "red_viper",
        name = "Crimson Viper GT",
        subtitle = "Street Tuned Twin-Turbo",
        price = 0,
        defaultPaint = CarPaintColor.CRIMSON_RED,
        baseSpeed = 0.55f,
        baseHandling = 0.60f,
        baseAcceleration = 0.52f,
        baseBraking = 0.58f,
        maxKmh = 240,
        spoilerStyle = 1
    ),
    CarSpec(
        id = "cobalt_phantom",
        name = "Cobalt Phantom X",
        subtitle = "Aero Track Interceptor",
        price = 1800,
        defaultPaint = CarPaintColor.NEON_BLUE,
        baseSpeed = 0.68f,
        baseHandling = 0.72f,
        baseAcceleration = 0.65f,
        baseBraking = 0.66f,
        maxKmh = 275,
        spoilerStyle = 2
    ),
    CarSpec(
        id = "solar_huracan",
        name = "Solar Apex V10",
        subtitle = "Mid-Engine Precision Supercar",
        price = 3200,
        defaultPaint = CarPaintColor.SOLAR_YELLOW,
        baseSpeed = 0.78f,
        baseHandling = 0.80f,
        baseAcceleration = 0.76f,
        baseBraking = 0.75f,
        maxKmh = 310,
        spoilerStyle = 2
    ),
    CarSpec(
        id = "venom_rs",
        name = "Venom Blade GT-R",
        subtitle = "Carbon Fiber Track Monster",
        price = 4800,
        defaultPaint = CarPaintColor.VENOM_GREEN,
        baseSpeed = 0.88f,
        baseHandling = 0.86f,
        baseAcceleration = 0.85f,
        baseBraking = 0.84f,
        maxKmh = 345,
        spoilerStyle = 3
    ),
    CarSpec(
        id = "cyber_valkyrie",
        name = "Cyber Valkyrie Hyper",
        subtitle = "1,500 HP Prototype Hypercar",
        price = 7500,
        defaultPaint = CarPaintColor.CYBER_PURPLE,
        baseSpeed = 0.96f,
        baseHandling = 0.94f,
        baseAcceleration = 0.95f,
        baseBraking = 0.92f,
        maxKmh = 390,
        spoilerStyle = 3
    )
)

data class LevelConfig(
    val levelNumber: Int,
    val title: String,
    val distanceMeters: Float,
    val targetOvertakes: Int,
    val threeStarTimeSec: Float,
    val twoStarTimeSec: Float,
    val trafficDensity: Float,
    val obstacleChance: Float,
    val baseRewardCoins: Int,
    val skyTopColor: Color,
    val skyBottomColor: Color,
    val cityColor: Color
)

data class LevelProgress(
    val levelNumber: Int,
    val isUnlocked: Boolean,
    val isCompleted: Boolean = false,
    val stars: Int = 0,
    val bestTimeSec: Float = 0f,
    val bestScore: Int = 0
)

data class ShopProduct(
    val id: String,
    val tab: ShopTab,
    val title: String,
    val subtitle: String,
    val iconType: ShopIconType,
    val coinCost: Int?,           // null if real-money / free reward
    val realMoneyLabel: String?,  // e.g. "৳ 0.99" matching reference image
    val rewardCoins: Int = 0,
    val nitroPackCount: Int = 0,
    val shieldPackCount: Int = 0,
    val fuelRefill: Boolean = false,
    val isFreeDailyReward: Boolean = false
)

enum class ShopIconType {
    COIN_STACK,
    COIN_CHEST,
    FUEL_CAN,
    NO_ADS,
    NITRO_BOTTLE,
    SHIELD_CORE,
    VIP_PASS
}

data class MoreGameItem(
    val id: String,
    val title: String,
    val genre: String,
    val rating: String,
    val accentColor: Color,
    val searchQuery: String
)

val MORE_GAMES_LIST = listOf(
    MoreGameItem("bike_racing", "Bike Racing", "Extreme Stunt & Moto", "4.8 ★", Color(0xFF00E676), "Bike Racing 2D Game"),
    MoreGameItem("car_stunt", "Car Stunt", "Mega Ramp 3D Loops", "4.7 ★", Color(0xFFFF9100), "Car Stunt Racing Game"),
    MoreGameItem("truck_drive", "Truck Drive", "Heavy Cargo Highway", "4.6 ★", Color(0xFF2979FF), "Truck Simulator Highway"),
    MoreGameItem("stickman_fight", "Stickman Fight", "Shadow Arena Combat", "4.8 ★", Color(0xFF00E5FF), "Stickman Fight Arcade"),
    MoreGameItem("moto_gp", "Moto GP", "Superbike Circuit Pro", "4.9 ★", Color(0xFFFF1744), "Moto GP Racing"),
    MoreGameItem("subway_run", "Subway Run", "Endless City Dash", "4.7 ★", Color(0xFFFFD600), "Subway Runner Dash")
)

enum class TrafficType {
    SPORT_COUPE,
    CITY_TAXI,
    HEAVY_TRUCK,
    POLICE_CRUISER,
    SUV_RACER
}

data class TrafficVehicle(
    val id: Int,
    var worldX: Float,       // Distance ahead/behind relative to screen
    var laneIndex: Int,      // 0, 1, 2 (3 highway lanes)
    var laneYRatio: Float,   // Smooth vertical lane ratio 0f..1f
    val speedKmh: Float,
    val type: TrafficType,
    val primaryColor: Color,
    val widthDp: Float,
    val heightDp: Float,
    var passedByPlayer: Boolean = false,
    var collided: Boolean = false
)

data class RoadCoin(
    val id: Int,
    var worldX: Float,
    val laneIndex: Int,
    var collected: Boolean = false,
    var collectAnimProgress: Float = 0f
)

enum class ObstacleType {
    OIL_SLICK,
    ROAD_BARRIER,
    NITRO_PAD
}

data class RoadObstacle(
    val id: Int,
    var worldX: Float,
    val laneIndex: Int,
    val type: ObstacleType,
    var triggered: Boolean = false
)

data class ParticleEffect(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var size: Float,
    var alpha: Float = 1f,
    var life: Float = 1f
)

data class FloatingPopup(
    var x: Float,
    var y: Float,
    val text: String,
    val color: Color,
    var life: Float = 1f
)

data class RaceResult(
    val levelNumber: Int,
    val isWin: Boolean,
    val completionTimeSec: Float,
    val finalScore: Int,
    val starsEarned: Int,
    val coinsCollectedInRace: Int,
    val levelCompletionBonus: Int,
    val totalCoinsAwarded: Int,
    val overtakes: Int,
    val targetOvertakes: Int
)
