package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerSaveData
import com.example.ui.components.NeonPanel
import com.example.ui.components.RacingBackdropCanvas
import com.example.ui.components.RacingButton
import com.example.ui.components.RacingButtonStyle
import com.example.ui.components.RacingIconButton
import com.example.ui.theme.BorderBlueGlow
import com.example.ui.theme.GlossyGreenBottom
import com.example.ui.theme.GlossyGreenTop
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextWhite

/**
 * SCREEN 10: SETTINGS
 * Matches reference image 10:
 * - Back arrow + "Settings" heading
 * - Sound toggle
 * - Music toggle
 * - Controls (< Left / Right >)
 * - Graphics (< High >)
 * - Language (< English >)
 * - Bottom buttons: Privacy Policy | More Games
 */
@Composable
fun SettingsScreen(
    saveData: PlayerSaveData,
    showPrivacyModal: Boolean,
    onToggleSound: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onCycleControls: (Int) -> Unit,
    onCycleGraphics: (Int) -> Unit,
    onCycleLanguage: (Int) -> Unit,
    onOpenPrivacyPolicy: (Boolean) -> Unit,
    onOpenMoreGames: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("settings_screen"),
        contentAlignment = Alignment.Center
    ) {
        RacingBackdropCanvas(showCheckeredFlags = false)

        NeonPanel(
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header Row with Back button and centered "Settings" title
                Box(modifier = Modifier.fillMaxWidth()) {
                    RacingIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart),
                        testTagName = "settings_back_button"
                    )

                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = TextWhite,
                            fontSize = 22.sp
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 1. Sound Toggle Row
                SettingsToggleRow(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    label = "Sound",
                    checked = saveData.soundEnabled,
                    onCheckedChange = onToggleSound,
                    testTagName = "settings_sound_toggle"
                )

                // 2. Music Toggle Row
                SettingsToggleRow(
                    icon = Icons.Filled.MusicNote,
                    label = "Music",
                    checked = saveData.musicEnabled,
                    onCheckedChange = onToggleMusic,
                    testTagName = "settings_music_toggle"
                )

                // 3. Controls Selector Row (< Left / Right >)
                SettingsSelectorRow(
                    icon = Icons.Filled.SportsEsports,
                    label = "Controls",
                    valueText = saveData.controlScheme.displayName,
                    onPrev = { onCycleControls(-1) },
                    onNext = { onCycleControls(1) },
                    testTagPrefix = "settings_controls"
                )

                // 4. Graphics Selector Row (< High >)
                SettingsSelectorRow(
                    icon = Icons.Filled.Tv,
                    label = "Graphics",
                    valueText = saveData.graphicsQuality.displayName,
                    onPrev = { onCycleGraphics(-1) },
                    onNext = { onCycleGraphics(1) },
                    testTagPrefix = "settings_graphics"
                )

                // 5. Language Selector Row (< English >)
                SettingsSelectorRow(
                    icon = Icons.Filled.Language,
                    label = "Language",
                    valueText = saveData.language.displayName,
                    onPrev = { onCycleLanguage(-1) },
                    onNext = { onCycleLanguage(1) },
                    testTagPrefix = "settings_language"
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Action Buttons: Privacy Policy | More Games
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RacingButton(
                        text = "Privacy Policy",
                        style = RacingButtonStyle.DARK_SLATE,
                        height = 40.dp,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenPrivacyPolicy(true) },
                        testTagName = "settings_privacy_button"
                    )
                    RacingButton(
                        text = "More Games",
                        style = RacingButtonStyle.DARK_SLATE,
                        height = 40.dp,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenMoreGames,
                        testTagName = "settings_more_games_button"
                    )
                }
            }
        }

        // Privacy Policy Dialog
        if (showPrivacyModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.78f)),
                contentAlignment = Alignment.Center
            ) {
                NeonPanel(modifier = Modifier.width(420.dp)) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PRIVACY POLICY",
                            style = MaterialTheme.typography.headlineMedium.copy(color = NeonBlue)
                        )
                        Text(
                            text = "Car Racing 2D stores all game progress, unlocked levels, coins, car upgrades, and settings locally on your device using Android Jetpack DataStore. No personal data, location, or contacts are collected or transmitted to external servers.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextWhite,
                                lineHeight = 18.sp
                            )
                        )
                        RacingButton(
                            text = "Close",
                            style = RacingButtonStyle.YELLOW,
                            height = 40.dp,
                            modifier = Modifier.width(150.dp),
                            onClick = { onOpenPrivacyPolicy(false) },
                            testTagName = "close_privacy_button"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTagName: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = TextWhite,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // Custom Glossy Green Pill Toggle matching Screen 10 Reference Image
        val pillShape = RoundedCornerShape(16.dp)
        Box(
            modifier = Modifier
                .width(58.dp)
                .height(28.dp)
                .clip(pillShape)
                .background(
                    if (checked) {
                        Brush.horizontalGradient(listOf(GlossyGreenTop, GlossyGreenBottom))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFF263238), Color(0xFF111827)))
                    }
                )
                .border(1.5.dp, if (checked) Color(0xFF69F0AE) else BorderBlueGlow.copy(alpha = 0.5f), pillShape)
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 4.dp)
                .testTag(testTagName),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(TextWhite)
            )
        }
    }
}

@Composable
private fun SettingsSelectorRow(
    icon: ImageVector,
    label: String,
    valueText: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    testTagPrefix: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = TextWhite,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onPrev)
                    .testTag("${testTagPrefix}_prev"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "❮",
                    color = TextSilver,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(110.dp)
                    .testTag("${testTagPrefix}_value")
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onNext)
                    .testTag("${testTagPrefix}_next"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "❯",
                    color = TextSilver,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
