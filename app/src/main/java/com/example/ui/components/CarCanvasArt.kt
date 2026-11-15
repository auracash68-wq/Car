package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarPaintColor
import com.example.model.CarSpec
import com.example.model.ShopIconType
import com.example.model.TrafficType
import com.example.model.WheelStyle
import com.example.ui.theme.GoldStar
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.RacingRed
import com.example.ui.theme.RacingYellow
import com.example.ui.theme.RussoOneFontFamily
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CarRacing2DLogo(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val carFontSize = if (compact) 24.sp else 32.sp
    val racingFontSize = if (compact) 26.sp else 36.sp
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Checkered flag graphic behind top-right of logo
        Canvas(
            modifier = Modifier
                .size(if (compact) 150.dp else 210.dp, if (compact) 58.dp else 82.dp)
                .align(Alignment.TopCenter)
        ) {
            val w = size.width
            val h = size.height
            // Speed wing lines
            drawLine(
                color = NeonBlue.copy(alpha = 0.7f),
                start = Offset(0f, h * 0.78f),
                end = Offset(w * 0.95f, h * 0.78f),
                strokeWidth = 4f
            )
            // Mini checkered flag on top right
            val flagStartX = w * 0.52f
            val flagStartY = h * 0.04f
            val cols = 6
            val rows = 3
            val cellW = (w * 0.34f) / cols
            val cellH = (h * 0.34f) / rows
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val isWhite = (r + c) % 2 == 0
                    val skewX = (rows - r) * 3.5f
                    drawRect(
                        color = if (isWhite) Color.White else Color(0xFF101828),
                        topLeft = Offset(flagStartX + c * cellW + skewX, flagStartY + r * cellH),
                        size = Size(cellW + 1f, cellH + 1f)
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "CAR",
                    style = TextStyle(
                        fontFamily = RussoOneFontFamily,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        fontSize = carFontSize,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White, Color(0xFFCFD8DC), Color(0xFF90A4AE))
                        ),
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(4f, 6f),
                            blurRadius = 6f
                        )
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Spacer(modifier = Modifier.width(if (compact) 38.dp else 54.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.offset(y = (-6).dp)
            ) {
                Text(
                    text = "RACING",
                    style = TextStyle(
                        fontFamily = RussoOneFontFamily,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        fontSize = racingFontSize,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFF176), Color(0xFFFFD600), Color(0xFFFF6D00))
                        ),
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(4f, 6f),
                            blurRadius = 6f
                        )
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "2D",
                    style = TextStyle(
                        fontFamily = RussoOneFontFamily,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        fontSize = racingFontSize,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFF5252), Color(0xFFD50000), Color(0xFF8E0000))
                        ),
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(4f, 6f),
                            blurRadius = 6f
                        )
                    )
                )
            }
        }
    }
}

@Composable
fun ShowroomCarCanvas(
    carSpec: CarSpec,
    paint: CarPaintColor,
    wheel: WheelStyle,
    modifier: Modifier = Modifier,
    showTireStack: Boolean = false
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Wet asphalt neon reflection glow beneath car
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    NeonBlue.copy(alpha = 0.35f),
                    paint.primaryHex.copy(alpha = 0.22f),
                    Color.Transparent
                ),
                center = Offset(w * 0.52f, h * 0.80f),
                radius = w * 0.45f
            ),
            topLeft = Offset(w * 0.08f, h * 0.62f),
            size = Size(w * 0.86f, h * 0.34f)
        )

        // Floor shadow
        drawOval(
            color = Color.Black.copy(alpha = 0.72f),
            topLeft = Offset(w * 0.14f, h * 0.68f),
            size = Size(w * 0.74f, h * 0.20f)
        )

        // Optional tire stack on left (matching Main Menu reference image)
        if (showTireStack) {
            drawRoundRect(
                color = Color(0xFF1A202C),
                topLeft = Offset(w * 0.04f, h * 0.62f),
                size = Size(w * 0.12f, h * 0.08f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = Color(0xFF2D3748),
                topLeft = Offset(w * 0.05f, h * 0.54f),
                size = Size(w * 0.11f, h * 0.08f),
                cornerRadius = CornerRadius(8f, 8f)
            )
        }

        // Rear Spoiler Wing
        if (carSpec.spoilerStyle >= 1) {
            val spoilerPath = Path().apply {
                moveTo(w * 0.72f, h * 0.35f)
                lineTo(w * 0.88f, h * 0.28f)
                lineTo(w * 0.90f, h * 0.33f)
                lineTo(w * 0.75f, h * 0.41f)
                close()
            }
            drawPath(spoilerPath, color = Color(0xFF111827))
            drawPath(spoilerPath, color = paint.highlightHex.copy(alpha = 0.6f), style = Stroke(width = 2f))
        }

        // Main 3/4 Perspective Supercar Body
        val bodyPath = Path().apply {
            moveTo(w * 0.13f, h * 0.64f) // Front bumper left
            cubicTo(w * 0.11f, h * 0.52f, w * 0.18f, h * 0.44f, w * 0.30f, h * 0.41f) // Front hood curve
            lineTo(w * 0.44f, h * 0.25f) // Windshield pillar
            cubicTo(w * 0.55f, h * 0.22f, w * 0.66f, h * 0.24f, w * 0.76f, h * 0.35f) // Roofline
            lineTo(w * 0.87f, h * 0.43f) // Rear deck
            lineTo(w * 0.88f, h * 0.65f) // Rear quarter panel
            lineTo(w * 0.56f, h * 0.74f) // Side skirt center
            lineTo(w * 0.20f, h * 0.73f) // Front splitter base
            close()
        }
        drawPath(
            path = bodyPath,
            brush = Brush.linearGradient(
                colors = listOf(paint.highlightHex, paint.primaryHex, paint.secondaryHex),
                start = Offset(w * 0.25f, h * 0.22f),
                end = Offset(w * 0.75f, h * 0.75f)
            )
        )
        drawPath(
            path = bodyPath,
            color = Color.White.copy(alpha = 0.35f),
            style = Stroke(width = 2.5f)
        )

        // Hood aerodynamic crease lines
        val hoodPanel = Path().apply {
            moveTo(w * 0.18f, h * 0.54f)
            lineTo(w * 0.42f, h * 0.42f)
            lineTo(w * 0.54f, h * 0.46f)
            lineTo(w * 0.28f, h * 0.60f)
            close()
        }
        drawPath(
            path = hoodPanel,
            brush = Brush.linearGradient(
                colors = listOf(paint.secondaryHex.copy(alpha = 0.75f), paint.primaryHex)
            )
        )

        // Windshield & Side Window Glass
        val windshieldPath = Path().apply {
            moveTo(w * 0.33f, h * 0.41f)
            lineTo(w * 0.45f, h * 0.28f)
            lineTo(w * 0.61f, h * 0.30f)
            lineTo(w * 0.53f, h * 0.44f)
            close()
        }
        drawPath(
            path = windshieldPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0A192F), Color(0xFF1E3A8A), NeonBlue.copy(alpha = 0.45f))
            )
        )

        val sideWindowPath = Path().apply {
            moveTo(w * 0.56f, h * 0.43f)
            lineTo(w * 0.63f, h * 0.30f)
            lineTo(w * 0.75f, h * 0.36f)
            lineTo(w * 0.70f, h * 0.44f)
            close()
        }
        drawPath(
            path = sideWindowPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0A192F), Color(0xFF172554))
            )
        )

        // Front Carbon Grille & Splitter
        val grillePath = Path().apply {
            moveTo(w * 0.15f, h * 0.63f)
            lineTo(w * 0.44f, h * 0.64f)
            lineTo(w * 0.41f, h * 0.72f)
            lineTo(w * 0.17f, h * 0.70f)
            close()
        }
        drawPath(grillePath, color = Color(0xFF0F172A))
        drawPath(grillePath, color = NeonBlue.copy(alpha = 0.5f), style = Stroke(width = 1.5f))

        // Aggressive LED Headlights (Left & Right)
        val leftHeadlight = Path().apply {
            moveTo(w * 0.15f, h * 0.54f)
            lineTo(w * 0.24f, h * 0.53f)
            lineTo(w * 0.21f, h * 0.58f)
            lineTo(w * 0.14f, h * 0.57f)
            close()
        }
        drawPath(leftHeadlight, color = Color.White)

        val rightHeadlight = Path().apply {
            moveTo(w * 0.36f, h * 0.54f)
            lineTo(w * 0.47f, h * 0.51f)
            lineTo(w * 0.45f, h * 0.57f)
            lineTo(w * 0.35f, h * 0.58f)
            close()
        }
        drawPath(rightHeadlight, color = Color.White)
        // Headlight glow flare
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.9f), NeonBlue.copy(alpha = 0.4f), Color.Transparent)
            ),
            radius = w * 0.08f,
            center = Offset(w * 0.41f, h * 0.55f)
        )

        // Side Air Intake & Skirt
        val sideIntake = Path().apply {
            moveTo(w * 0.62f, h * 0.51f)
            lineTo(w * 0.74f, h * 0.47f)
            lineTo(w * 0.72f, h * 0.62f)
            lineTo(w * 0.60f, h * 0.65f)
            close()
        }
        drawPath(sideIntake, color = Color(0xFF111827).copy(alpha = 0.8f))

        // Front-Right Showcase Wheel & Rear-Right Wheel
        drawShowroomWheel(
            center = Offset(w * 0.54f, h * 0.66f),
            radius = h * 0.155f,
            wheel = wheel
        )
        drawShowroomWheel(
            center = Offset(w * 0.80f, h * 0.60f),
            radius = h * 0.135f,
            wheel = wheel
        )
    }
}

private fun DrawScope.drawShowroomWheel(
    center: Offset,
    radius: Float,
    wheel: WheelStyle
) {
    // Tire rubber
    drawCircle(
        color = Color(0xFF0B0F19),
        radius = radius,
        center = center
    )
    drawCircle(
        color = Color(0xFF374151),
        radius = radius * 0.92f,
        center = center,
        style = Stroke(width = radius * 0.12f)
    )
    // Rim ring
    drawCircle(
        color = wheel.rimColor,
        radius = radius * 0.68f,
        center = center,
        style = Stroke(width = radius * 0.10f)
    )
    // Spokes
    val stepAngle = 360f / wheel.spokeCount
    for (i in 0 until wheel.spokeCount) {
        val rad = Math.toRadians((i * stepAngle).toDouble())
        val endX = center.x + (radius * 0.65f) * cos(rad).toFloat()
        val endY = center.y + (radius * 0.65f) * sin(rad).toFloat()
        drawLine(
            color = wheel.rimColor,
            start = center,
            end = Offset(endX, endY),
            strokeWidth = radius * 0.11f,
            cap = StrokeCap.Round
        )
    }
    // Center hub cap
    drawCircle(
        color = RacingRed,
        radius = radius * 0.20f,
        center = center
    )
}

fun DrawScope.drawSideViewPlayerCar(
    x: Float,
    y: Float,
    carWidth: Float,
    carHeight: Float,
    paint: CarPaintColor,
    wheel: WheelStyle,
    wheelRotationDeg: Float,
    isNitroActive: Boolean,
    hasShield: Boolean,
    isInvulnerableFlash: Boolean
) {
    val alpha = if (isInvulnerableFlash) 0.55f else 1f

    // Optional Shield Bubble
    if (hasShield) {
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    NeonBlue.copy(alpha = 0.12f),
                    NeonBlue.copy(alpha = 0.42f),
                    Color.Transparent
                ),
                center = Offset(x + carWidth * 0.5f, y + carHeight * 0.5f),
                radius = carWidth * 0.7f
            ),
            topLeft = Offset(x - carWidth * 0.08f, y - carHeight * 0.18f),
            size = Size(carWidth * 1.16f, carHeight * 1.36f),
            cornerRadius = CornerRadius(carHeight, carHeight)
        )
        drawRoundRect(
            color = NeonBlue.copy(alpha = 0.8f),
            topLeft = Offset(x - carWidth * 0.06f, y - carHeight * 0.14f),
            size = Size(carWidth * 1.12f, carHeight * 1.28f),
            cornerRadius = CornerRadius(carHeight, carHeight),
            style = Stroke(width = 3f)
        )
    }

    // Nitro Exhaust Flame at Rear Left
    if (isNitroActive) {
        val flameLen = carWidth * 0.48f
        val outerFlame = Path().apply {
            moveTo(x + 4f, y + carHeight * 0.62f)
            lineTo(x - flameLen, y + carHeight * 0.48f)
            lineTo(x - flameLen * 0.72f, y + carHeight * 0.68f)
            lineTo(x - flameLen * 1.05f, y + carHeight * 0.82f)
            lineTo(x + 4f, y + carHeight * 0.82f)
            close()
        }
        drawPath(
            path = outerFlame,
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color(0xFFFF6D00), NeonBlue, Color.White),
                startX = x - flameLen,
                endX = x + 4f
            )
        )
    }

    // Road drop shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.45f * alpha),
        topLeft = Offset(x + carWidth * 0.04f, y + carHeight * 0.76f),
        size = Size(carWidth * 0.92f, carHeight * 0.28f)
    )

    // Rear Wing Spoiler
    val wingPath = Path().apply {
        moveTo(x + carWidth * 0.05f, y + carHeight * 0.36f)
        lineTo(x - carWidth * 0.02f, y + carHeight * 0.14f)
        lineTo(x + carWidth * 0.10f, y + carHeight * 0.14f)
        lineTo(x + carWidth * 0.14f, y + carHeight * 0.38f)
        close()
    }
    drawPath(wingPath, color = Color(0xFF111827).copy(alpha = alpha))

    // Sleek Side-View Sports Car Body (matches Screen 4 reference image!)
    val bodyPath = Path().apply {
        moveTo(x + carWidth * 0.03f, y + carHeight * 0.72f) // Rear bumper bottom
        lineTo(x + carWidth * 0.02f, y + carHeight * 0.38f) // Rear tail
        lineTo(x + carWidth * 0.22f, y + carHeight * 0.34f) // Rear deck
        lineTo(x + carWidth * 0.38f, y + carHeight * 0.10f) // Roofline rear
        lineTo(x + carWidth * 0.62f, y + carHeight * 0.10f) // Roofline front
        lineTo(x + carWidth * 0.78f, y + carHeight * 0.36f) // Windshield base
        cubicTo(
            x + carWidth * 0.90f, y + carHeight * 0.39f,
            x + carWidth * 0.98f, y + carHeight * 0.48f,
            x + carWidth * 0.99f, y + carHeight * 0.68f
        ) // Front nose cone
        lineTo(x + carWidth * 0.94f, y + carHeight * 0.75f) // Front splitter
        lineTo(x + carWidth * 0.03f, y + carHeight * 0.75f)
        close()
    }
    drawPath(
        path = bodyPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                paint.highlightHex.copy(alpha = alpha),
                paint.primaryHex.copy(alpha = alpha),
                paint.secondaryHex.copy(alpha = alpha)
            ),
            startY = y,
            endY = y + carHeight * 0.8f
        )
    )
    drawPath(
        path = bodyPath,
        color = Color.White.copy(alpha = 0.4f * alpha),
        style = Stroke(width = 2f)
    )

    // Tinted Cockpit Glass
    val windowPath = Path().apply {
        moveTo(x + carWidth * 0.27f, y + carHeight * 0.34f)
        lineTo(x + carWidth * 0.40f, y + carHeight * 0.15f)
        lineTo(x + carWidth * 0.60f, y + carHeight * 0.15f)
        lineTo(x + carWidth * 0.74f, y + carHeight * 0.35f)
        close()
    }
    drawPath(
        path = windowPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F172A).copy(alpha = alpha),
                Color(0xFF1E3A8A).copy(alpha = alpha),
                NeonBlue.copy(alpha = 0.55f * alpha)
            )
        )
    )

    // Side Racing Stripe & Door Vent
    drawLine(
        color = Color.White.copy(alpha = 0.55f * alpha),
        start = Offset(x + carWidth * 0.14f, y + carHeight * 0.50f),
        end = Offset(x + carWidth * 0.86f, y + carHeight * 0.52f),
        strokeWidth = 3f
    )

    // Headlight & Subtle Light Cone
    drawRoundRect(
        color = Color.White.copy(alpha = alpha),
        topLeft = Offset(x + carWidth * 0.91f, y + carHeight * 0.48f),
        size = Size(carWidth * 0.07f, carHeight * 0.11f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Tail Light
    drawRoundRect(
        color = RacingRed.copy(alpha = alpha),
        topLeft = Offset(x + carWidth * 0.02f, y + carHeight * 0.42f),
        size = Size(carWidth * 0.05f, carHeight * 0.12f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Rotating Wheels (Rear & Front)
    val wheelRadius = carHeight * 0.22f
    val rearWheelCenter = Offset(x + carWidth * 0.23f, y + carHeight * 0.74f)
    val frontWheelCenter = Offset(x + carWidth * 0.77f, y + carHeight * 0.74f)

    drawRotatingSideWheel(rearWheelCenter, wheelRadius, wheel, wheelRotationDeg)
    drawRotatingSideWheel(frontWheelCenter, wheelRadius, wheel, wheelRotationDeg)
}

private fun DrawScope.drawRotatingSideWheel(
    center: Offset,
    radius: Float,
    wheel: WheelStyle,
    rotationDeg: Float
) {
    drawCircle(color = Color(0xFF0B0F19), radius = radius, center = center)
    drawCircle(
        color = Color(0xFF374151),
        radius = radius * 0.86f,
        center = center,
        style = Stroke(width = radius * 0.14f)
    )
    rotate(degrees = rotationDeg, pivot = center) {
        val step = 360f / wheel.spokeCount
        for (i in 0 until wheel.spokeCount) {
            val rad = Math.toRadians((i * step).toDouble())
            drawLine(
                color = wheel.rimColor,
                start = center,
                end = Offset(
                    center.x + radius * 0.65f * cos(rad).toFloat(),
                    center.y + radius * 0.65f * sin(rad).toFloat()
                ),
                strokeWidth = radius * 0.14f
            )
        }
    }
    drawCircle(color = wheel.rimColor, radius = radius * 0.68f, center = center, style = Stroke(width = 2f))
    drawCircle(color = RacingRed, radius = radius * 0.22f, center = center)
}

fun DrawScope.drawTrafficVehicleCanvas(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    type: TrafficType,
    color: Color,
    wheelRotationDeg: Float
) {
    // Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.42f),
        topLeft = Offset(x + w * 0.04f, y + h * 0.76f),
        size = Size(w * 0.92f, h * 0.24f)
    )

    val bodyPath = Path().apply {
        when (type) {
            TrafficType.HEAVY_TRUCK -> {
                moveTo(x + w * 0.02f, y + h * 0.72f)
                lineTo(x + w * 0.02f, y + h * 0.12f)
                lineTo(x + w * 0.68f, y + h * 0.12f)
                lineTo(x + w * 0.68f, y + h * 0.24f)
                lineTo(x + w * 0.88f, y + h * 0.24f)
                lineTo(x + w * 0.97f, y + h * 0.48f)
                lineTo(x + w * 0.97f, y + h * 0.72f)
                close()
            }
            TrafficType.SUV_RACER -> {
                moveTo(x + w * 0.03f, y + h * 0.72f)
                lineTo(x + w * 0.05f, y + h * 0.22f)
                lineTo(x + w * 0.65f, y + h * 0.18f)
                lineTo(x + w * 0.80f, y + h * 0.40f)
                lineTo(x + w * 0.97f, y + h * 0.46f)
                lineTo(x + w * 0.97f, y + h * 0.72f)
                close()
            }
            else -> {
                moveTo(x + w * 0.03f, y + h * 0.72f)
                lineTo(x + w * 0.06f, y + h * 0.38f)
                lineTo(x + w * 0.26f, y + h * 0.16f)
                lineTo(x + w * 0.62f, y + h * 0.16f)
                lineTo(x + w * 0.78f, y + h * 0.38f)
                lineTo(x + w * 0.98f, y + h * 0.48f)
                lineTo(x + w * 0.97f, y + h * 0.72f)
                close()
            }
        }
    }
    drawPath(
        path = bodyPath,
        brush = Brush.verticalGradient(
            colors = listOf(color.copy(alpha = 0.95f), color.copy(red = color.red * 0.65f, green = color.green * 0.65f, blue = color.blue * 0.65f)),
            startY = y,
            endY = y + h
        )
    )
    drawPath(bodyPath, color = Color.White.copy(alpha = 0.35f), style = Stroke(width = 1.5f))

    // Windows
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(x + w * 0.28f, y + h * 0.22f),
        size = Size(w * 0.42f, h * 0.20f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Special details for Taxi or Police
    if (type == TrafficType.CITY_TAXI) {
        drawRoundRect(
            color = Color.Black,
            topLeft = Offset(x + w * 0.40f, y + h * 0.06f),
            size = Size(w * 0.18f, h * 0.10f),
            cornerRadius = CornerRadius(4f, 4f)
        )
    } else if (type == TrafficType.POLICE_CRUISER) {
        drawRoundRect(
            color = RacingRed,
            topLeft = Offset(x + w * 0.40f, y + h * 0.06f),
            size = Size(w * 0.10f, h * 0.09f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = NeonBlue,
            topLeft = Offset(x + w * 0.50f, y + h * 0.06f),
            size = Size(w * 0.10f, h * 0.09f),
            cornerRadius = CornerRadius(4f, 4f)
        )
    }

    // Wheels
    val r = h * 0.20f
    drawRotatingSideWheel(Offset(x + w * 0.24f, y + h * 0.74f), r, WheelStyle.SPORT_ALLOY, wheelRotationDeg)
    drawRotatingSideWheel(Offset(x + w * 0.76f, y + h * 0.74f), r, WheelStyle.SPORT_ALLOY, wheelRotationDeg)
}

@Composable
fun TrophyWithFlagsCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Golden glow burst
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(GoldStar.copy(alpha = 0.45f), Color.Transparent)
            ),
            radius = w * 0.45f,
            center = Offset(w * 0.5f, h * 0.48f)
        )

        // Left & Right Checkered Flags
        drawCheckeredWingFlag(Offset(w * 0.12f, h * 0.22f), Size(w * 0.28f, h * 0.36f), leftSide = true)
        drawCheckeredWingFlag(Offset(w * 0.60f, h * 0.22f), Size(w * 0.28f, h * 0.36f), leftSide = false)

        // Trophy Cup Handles
        drawArc(
            color = GoldStar,
            startAngle = 90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.32f, h * 0.18f),
            size = Size(w * 0.12f, h * 0.28f),
            style = Stroke(width = 6f)
        )
        drawArc(
            color = GoldStar,
            startAngle = -90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.56f, h * 0.18f),
            size = Size(w * 0.12f, h * 0.28f),
            style = Stroke(width = 6f)
        )

        // Trophy Cup Bowl
        val cupPath = Path().apply {
            moveTo(w * 0.37f, h * 0.12f)
            lineTo(w * 0.63f, h * 0.12f)
            cubicTo(w * 0.62f, h * 0.45f, w * 0.55f, h * 0.54f, w * 0.50f, h * 0.56f)
            cubicTo(w * 0.45f, h * 0.54f, w * 0.38f, h * 0.45f, w * 0.37f, h * 0.12f)
            close()
        }
        drawPath(
            path = cupPath,
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFFFFA000), Color(0xFFFFEA00), Color(0xFFFF8F00))
            )
        )

        // Stem & Pedestal
        drawRect(
            color = Color(0xFFFFB300),
            topLeft = Offset(w * 0.47f, h * 0.54f),
            size = Size(w * 0.06f, h * 0.16f)
        )
        drawRoundRect(
            color = Color(0xFFFF8F00),
            topLeft = Offset(w * 0.40f, h * 0.68f),
            size = Size(w * 0.20f, h * 0.08f),
            cornerRadius = CornerRadius(6f, 6f)
        )
    }
}

private fun DrawScope.drawCheckeredWingFlag(topLeft: Offset, flagSize: Size, leftSide: Boolean) {
    val rows = 4
    val cols = 5
    val cw = flagSize.width / cols
    val ch = flagSize.height / rows
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val isWhite = (r + c) % 2 == 0
            val waveY = sin((c + r) * 0.9f) * 3f
            val skewX = if (leftSide) -r * 3f else r * 3f
            drawRect(
                color = if (isWhite) Color.White else Color(0xFF111827),
                topLeft = Offset(topLeft.x + c * cw + skewX, topLeft.y + r * ch + waveY),
                size = Size(cw + 1f, ch + 1f)
            )
        }
    }
}

@Composable
fun BrokenHeartCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Red atmospheric glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(RacingRed.copy(alpha = 0.55f), Color.Transparent)
            ),
            radius = w * 0.5f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // Heart Path
        val heartPath = Path().apply {
            moveTo(w * 0.5f, h * 0.86f)
            cubicTo(w * 0.12f, h * 0.60f, w * 0.08f, h * 0.20f, w * 0.32f, h * 0.16f)
            cubicTo(w * 0.44f, h * 0.14f, w * 0.50f, h * 0.26f, w * 0.50f, h * 0.28f)
            cubicTo(w * 0.50f, h * 0.26f, w * 0.56f, h * 0.14f, w * 0.68f, h * 0.16f)
            cubicTo(w * 0.92f, h * 0.20f, w * 0.88f, h * 0.60f, w * 0.5f, h * 0.86f)
            close()
        }
        drawPath(
            path = heartPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFF5252), Color(0xFFD50000), Color(0xFF7F0000))
            )
        )
        drawPath(heartPath, color = Color(0xFFFF8A80), style = Stroke(width = 3f))

        // Jagged crack down the center
        val crackPath = Path().apply {
            moveTo(w * 0.50f, h * 0.24f)
            lineTo(w * 0.44f, h * 0.42f)
            lineTo(w * 0.55f, h * 0.54f)
            lineTo(w * 0.46f, h * 0.68f)
            lineTo(w * 0.50f, h * 0.86f)
        }
        drawPath(
            path = crackPath,
            color = Color(0xFF1A0005),
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun ShopItemIllustrationCanvas(
    iconType: ShopIconType,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        when (iconType) {
            ShopIconType.COIN_STACK, ShopIconType.COIN_CHEST -> {
                // Stacked 3D gold coins matching Screen 9 Coin Pack
                val stacks = listOf(
                    Triple(w * 0.34f, h * 0.72f, 4),
                    Triple(w * 0.62f, h * 0.74f, 5),
                    Triple(w * 0.48f, h * 0.82f, 3)
                )
                for ((cx, baseY, count) in stacks) {
                    for (i in 0 until count) {
                        val cy = baseY - i * (h * 0.09f)
                        drawOval(
                            color = Color(0xFFFF8F00),
                            topLeft = Offset(cx - w * 0.16f, cy - h * 0.04f),
                            size = Size(w * 0.32f, h * 0.13f)
                        )
                        drawOval(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFFFEA00), Color(0xFFFFC107))
                            ),
                            topLeft = Offset(cx - w * 0.16f, cy - h * 0.07f),
                            size = Size(w * 0.32f, h * 0.12f)
                        )
                    }
                }
            }
            ShopIconType.FUEL_CAN -> {
                // Red jerrycan matching Screen 9 Fuel Pack
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFF5252), Color(0xFFD50000), Color(0xFF8E0000))
                    ),
                    topLeft = Offset(w * 0.24f, h * 0.22f),
                    size = Size(w * 0.52f, h * 0.66f),
                    cornerRadius = CornerRadius(14f, 14f)
                )
                // Handle cutout
                drawRoundRect(
                    color = Color(0xFF0A1435),
                    topLeft = Offset(w * 0.34f, h * 0.28f),
                    size = Size(w * 0.32f, h * 0.10f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                // Yellow spout
                drawRoundRect(
                    color = RacingYellow,
                    topLeft = Offset(w * 0.26f, h * 0.12f),
                    size = Size(w * 0.14f, h * 0.12f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                // Center emblem
                drawLine(
                    color = Color.White.copy(alpha = 0.7f),
                    start = Offset(w * 0.36f, h * 0.48f),
                    end = Offset(w * 0.64f, h * 0.76f),
                    strokeWidth = 5f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.7f),
                    start = Offset(w * 0.64f, h * 0.48f),
                    end = Offset(w * 0.36f, h * 0.76f),
                    strokeWidth = 5f
                )
            }
            ShopIconType.NO_ADS -> {
                // Red prohibition circle with diagonal slash matching Screen 9 No Ads
                val center = Offset(w * 0.5f, h * 0.52f)
                val r = minOf(w, h) * 0.36f
                drawCircle(
                    color = Color(0xFF102048),
                    radius = r,
                    center = center
                )
                drawCircle(
                    color = RacingRed,
                    radius = r,
                    center = center,
                    style = Stroke(width = r * 0.24f)
                )
                drawLine(
                    color = RacingRed,
                    start = Offset(center.x - r * 0.68f, center.y - r * 0.68f),
                    end = Offset(center.x + r * 0.68f, center.y + r * 0.68f),
                    strokeWidth = r * 0.24f
                )
            }
            ShopIconType.NITRO_BOTTLE -> {
                // Blue N2O Cylinder
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF0091EA), NeonBlue, Color(0xFF01579B))
                    ),
                    topLeft = Offset(w * 0.32f, h * 0.24f),
                    size = Size(w * 0.36f, h * 0.64f),
                    cornerRadius = CornerRadius(18f, 18f)
                )
                drawRoundRect(
                    color = RacingYellow,
                    topLeft = Offset(w * 0.42f, h * 0.12f),
                    size = Size(w * 0.16f, h * 0.14f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
            ShopIconType.SHIELD_CORE, ShopIconType.VIP_PASS -> {
                val shield = Path().apply {
                    moveTo(w * 0.5f, h * 0.14f)
                    lineTo(w * 0.80f, h * 0.26f)
                    cubicTo(w * 0.80f, h * 0.62f, w * 0.65f, h * 0.78f, w * 0.5f, h * 0.88f)
                    cubicTo(w * 0.35f, h * 0.78f, w * 0.20f, h * 0.62f, w * 0.20f, h * 0.26f)
                    close()
                }
                drawPath(
                    path = shield,
                    brush = Brush.verticalGradient(
                        colors = listOf(NeonBlue, Color(0xFF2979FF), Color(0xFF0D47A1))
                    )
                )
                drawPath(shield, color = RacingYellow, style = Stroke(width = 4f))
            }
        }
    }
}

@Composable
fun GoldCoinIcon(sizeDp: Dp = 24.dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val r = size.minDimension / 2f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFF59D), Color(0xFFFFD600), Color(0xFFFF8F00))
            ),
            radius = r
        )
        drawCircle(
            color = Color(0xFFFFE082),
            radius = r * 0.76f,
            style = Stroke(width = r * 0.14f)
        )
        drawRoundRect(
            color = Color(0xFFFF6F00),
            topLeft = Offset(center.x - r * 0.14f, center.y - r * 0.42f),
            size = Size(r * 0.28f, r * 0.84f),
            cornerRadius = CornerRadius(4f, 4f)
        )
    }
}
