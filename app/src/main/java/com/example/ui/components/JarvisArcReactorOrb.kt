package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.live.ZoyaState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisArcReactorOrb(
    modifier: Modifier = Modifier,
    state: ZoyaState = ZoyaState.IDLE
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ReactorTransition")

    // Smooth 60FPS continuous 360-degree rotation (Clockwise)
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    ZoyaState.LISTENING -> 2500
                    ZoyaState.THINKING -> 1500
                    ZoyaState.SPEAKING -> 2000
                    ZoyaState.IDLE -> 6000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinAngle"
    )

    // Counter-rotation angle for middle HUD ring (Counter-clockwise)
    val counterSpinAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    ZoyaState.LISTENING -> 3000
                    ZoyaState.THINKING -> 1800
                    ZoyaState.SPEAKING -> 2200
                    ZoyaState.IDLE -> 7500
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "counterSpinAngle"
    )

    // Breathing / Pulse animation for core scale
    val corePulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = when (state) {
            ZoyaState.LISTENING -> 1.18f
            ZoyaState.THINKING -> 1.10f
            ZoyaState.SPEAKING -> 1.25f
            ZoyaState.IDLE -> 1.05f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    ZoyaState.SPEAKING -> 350
                    ZoyaState.LISTENING -> 600
                    ZoyaState.THINKING -> 800
                    ZoyaState.IDLE -> 2000
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corePulseScale"
    )

    // Expanding energy pulse wave ring progress
    val pulseWaveProgress by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state == ZoyaState.SPEAKING) 800 else 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseWaveProgress"
    )

    // State Color Schemes
    val (primaryColor, secondaryColor, coreColor) = when (state) {
        ZoyaState.IDLE -> Triple(
            Color(0xFF00E5FF), // Cyan
            Color(0xFF0091EA), // Deep Blue
            Color(0xFFE0F7FA)  // Soft White/Cyan
        )
        ZoyaState.LISTENING -> Triple(
            Color(0xFFFF4081), // Vibrant Neon Pink
            Color(0xFF7C4DFF), // Electric Purple
            Color(0xFFFF80AB)  // Light Pink
        )
        ZoyaState.THINKING -> Triple(
            Color(0xFFFFD54F), // Amber Gold
            Color(0xFFFF9100), // Solar Orange
            Color(0xFFFFF8E1)  // Light Warm Yellow
        )
        ZoyaState.SPEAKING -> Triple(
            Color(0xFF00E676), // Emerald Green
            Color(0xFF00E5FF), // Aqua Cyan
            Color(0xFFE8F5E9)  // Bright Soft Green
        )
    }

    Box(
        modifier = modifier.size(310.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f
            val baseRadius = maxRadius * 0.42f
            val currentCoreRadius = baseRadius * corePulseScale

            // 1. Ambient Background Halo Energy Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.45f * (corePulseScale - 0.1f)),
                        secondaryColor.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = maxRadius * 1.1f
                ),
                radius = maxRadius * 1.1f,
                center = centerOffset
            )

            // 2. Audio-Reactive Energy Waves (Expanding Circles)
            if (state == ZoyaState.SPEAKING || state == ZoyaState.LISTENING) {
                val waveRadius = baseRadius + (maxRadius - baseRadius) * pulseWaveProgress
                val waveAlpha = (1f - pulseWaveProgress).coerceIn(0f, 0.8f)
                drawCircle(
                    color = primaryColor.copy(alpha = waveAlpha),
                    center = centerOffset,
                    radius = waveRadius,
                    style = Stroke(width = 2.5.dp.toPx())
                )
                drawCircle(
                    color = secondaryColor.copy(alpha = waveAlpha * 0.5f),
                    center = centerOffset,
                    radius = waveRadius * 0.85f,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // 3. Outer Rotating Technical HUD Tick Ring (Clockwise)
            rotate(spinAngle, centerOffset) {
                val outerHudRadius = maxRadius * 0.88f
                drawCircle(
                    color = primaryColor.copy(alpha = 0.35f),
                    center = centerOffset,
                    radius = outerHudRadius,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Render 36 Ticks around the perimeter
                for (i in 0 until 36) {
                    val angleDeg = i * 10f
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    val isMajor = i % 3 == 0

                    val innerR = outerHudRadius - (if (isMajor) 12.dp.toPx() else 6.dp.toPx())
                    val outerR = outerHudRadius

                    val startX = centerOffset.x + innerR * cos(angleRad).toFloat()
                    val startY = centerOffset.y + innerR * sin(angleRad).toFloat()
                    val endX = centerOffset.x + outerR * cos(angleRad).toFloat()
                    val endY = centerOffset.y + outerR * sin(angleRad).toFloat()

                    drawLine(
                        color = if (isMajor) primaryColor else primaryColor.copy(alpha = 0.4f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.dp.toPx()
                    )
                }

                // 4 Major Outer Segment Arcs
                for (a in 0 until 4) {
                    val startAngle = a * 90f + 15f
                    drawArc(
                        color = primaryColor,
                        startAngle = startAngle,
                        sweepAngle = 45f,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - outerHudRadius, centerOffset.y - outerHudRadius),
                        size = Size(outerHudRadius * 2, outerHudRadius * 2),
                        style = Stroke(width = 3.5.dp.toPx())
                    )
                }
            }

            // 4. Middle Counter-Rotating Segmented Ring (Counter-Clockwise)
            rotate(counterSpinAngle, centerOffset) {
                val midRadius = maxRadius * 0.72f
                val arcSize = Size(midRadius * 2, midRadius * 2)
                val topLeft = Offset(centerOffset.x - midRadius, centerOffset.y - midRadius)

                drawCircle(
                    color = secondaryColor.copy(alpha = 0.25f),
                    center = centerOffset,
                    radius = midRadius,
                    style = Stroke(width = 1.dp.toPx())
                )

                // 8 Power Coils / Arcs
                for (i in 0 until 8) {
                    val startA = i * 45f + 8f
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(primaryColor, secondaryColor, Color.Transparent),
                            center = centerOffset
                        ),
                        startAngle = startA,
                        sweepAngle = 28f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = 5.dp.toPx())
                    )
                }
            }

            // 5. Inner Rotating Tri-Coil Arc Assembly
            rotate(spinAngle * 1.5f, centerOffset) {
                val innerCoilRadius = maxRadius * 0.55f
                val topLeft = Offset(centerOffset.x - innerCoilRadius, centerOffset.y - innerCoilRadius)
                val arcSize = Size(innerCoilRadius * 2, innerCoilRadius * 2)

                for (c in 0 until 3) {
                    val startA = c * 120f + 10f
                    drawArc(
                        color = primaryColor,
                        startAngle = startA,
                        sweepAngle = 75f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = 6.dp.toPx())
                    )
                }
            }

            // 6. Central Reactor Arc Core (3D Spherical Plasma Core)
            // Glowing Core Outer Layer
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreColor.copy(alpha = 0.95f),
                        primaryColor.copy(alpha = 0.85f),
                        secondaryColor.copy(alpha = 0.60f),
                        Color.Black.copy(alpha = 0.8f)
                    ),
                    center = Offset(centerOffset.x - currentCoreRadius * 0.25f, centerOffset.y - currentCoreRadius * 0.25f),
                    radius = currentCoreRadius * 1.2f
                ),
                radius = currentCoreRadius,
                center = centerOffset
            )

            // Core 3D Glass Reflection Highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.65f),
                center = Offset(centerOffset.x - currentCoreRadius * 0.35f, centerOffset.y - currentCoreRadius * 0.35f),
                radius = currentCoreRadius * 0.28f
            )

            // Core Concentric Inner Ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.8f),
                center = centerOffset,
                radius = currentCoreRadius * 0.6f,
                style = Stroke(width = 2.dp.toPx())
            )

            // Concentric Center Node
            drawCircle(
                color = Color.White,
                center = centerOffset,
                radius = currentCoreRadius * 0.22f
            )
        }
    }
}
