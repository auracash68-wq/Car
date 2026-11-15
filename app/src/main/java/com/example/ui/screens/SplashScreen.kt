package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ALL_CARS
import com.example.model.CarPaintColor
import com.example.model.WheelStyle
import com.example.ui.components.CarRacing2DLogo
import com.example.ui.components.RacingBackdropCanvas
import com.example.ui.components.ShowroomCarCanvas
import com.example.ui.theme.BorderBlueGlow
import com.example.ui.theme.GlossyYellowBottom
import com.example.ui.theme.GlossyYellowTop
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.TextWhite

@Composable
fun SplashScreen(
    isDataLoaded: Boolean,
    selectedCarId: String,
    selectedPaint: CarPaintColor,
    selectedWheel: WheelStyle,
    onLoadingFinished: () -> Unit
) {
    val progress = remember { Animatable(0.08f) }

    LaunchedEffect(isDataLoaded) {
        progress.animateTo(
            targetValue = 0.65f,
            animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)
        )
        if (isDataLoaded) {
            progress.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )
            onLoadingFinished()
        }
    }

    val carSpec = remember(selectedCarId) {
        ALL_CARS.find { it.id == selectedCarId } ?: ALL_CARS.first()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("splash_screen")
    ) {
        RacingBackdropCanvas(showCheckeredFlags = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            CarRacing2DLogo(
                modifier = Modifier.padding(top = 4.dp)
            )

            ShowroomCarCanvas(
                carSpec = carSpec,
                paint = selectedPaint,
                wheel = selectedWheel,
                modifier = Modifier
                    .fillMaxWidth(0.58f)
                    .weight(1f)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(0.62f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF091434))
                        .border(1.5.dp, BorderBlueGlow.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.value.coerceIn(0.05f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(GlossyYellowBottom, GlossyYellowTop, Color(0xFFFFF59D))
                                )
                            )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Loading... ${(progress.value * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.testTag("splash_loading_text")
                )
            }
        }
    }
}
