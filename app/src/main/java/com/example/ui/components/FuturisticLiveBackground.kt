package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.live.ZoyaState
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    var x: Float,
    var y: Float,
    val radius: Float,
    val speedY: Float,
    val speedX: Float,
    val alpha: Float,
    val color: Color
)

@Composable
fun FuturisticLiveBackground(
    modifier: Modifier = Modifier,
    state: ZoyaState = ZoyaState.IDLE
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundTransition")

    // Slow continuous time loop for floating particles & energy gradient shift
    val timeAnimation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "timeAnimation"
    )

    // Pulse factor for background aura glow
    val auraPulse = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraPulse"
    )

    // Scanning line animation Y offset
    val scanLineProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanLineProgress"
    )

    // Generate 30 persistent particles
    val particles = remember {
        val colors = listOf(
            Color(0xFF00E5FF),
            Color(0xFF7C4DFF),
            Color(0xFFFF4081),
            Color(0xFF69F0AE),
            Color(0xFFFFD180)
        )
        List(30) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 3f + 1.5f,
                speedY = Random.nextFloat() * 0.0008f + 0.0003f,
                speedX = Random.nextFloat() * 0.0004f - 0.0002f,
                alpha = Random.nextFloat() * 0.5f + 0.2f,
                color = colors[Random.nextInt(colors.size)]
            )
        }
    }

    // Dynamic state accent color for ambient radial glow
    val stateGlowColor = when (state) {
        ZoyaState.IDLE -> Color(0xFF00E5FF)
        ZoyaState.LISTENING -> Color(0xFFFF4081)
        ZoyaState.THINKING -> Color(0xFFFF9100)
        ZoyaState.SPEAKING -> Color(0xFF00E676)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Deep Cosmic Radial Gradient Base
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF120C24),
                    Color(0xFF080512),
                    Color(0xFF040209)
                ),
                center = Offset(width * 0.5f, height * 0.45f),
                radius = width * 1.5f
            )
        )

        // 2. Animated Ambient Energy Plasma Glow
        val auraRadius = width * (0.8f + auraPulse.value * 0.2f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    stateGlowColor.copy(alpha = auraPulse.value * 0.18f),
                    Color(0xFF7C4DFF).copy(alpha = auraPulse.value * 0.08f),
                    Color.Transparent
                ),
                center = Offset(width * 0.5f, height * 0.42f),
                radius = auraRadius
            ),
            radius = auraRadius
        )

        // 3. Faint Tech Grid Lines
        val gridStep = 60.dp.toPx()
        val gridAlpha = 0.04f
        var x = 0f
        while (x < width) {
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = gridAlpha),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f
            )
            x += gridStep
        }
        var y = 0f
        while (y < height) {
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = gridAlpha),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
            y += gridStep
        }

        // 4. Moving Laser Scanline
        val currentScanY = height * scanLineProgress.value
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF00E5FF).copy(alpha = 0.15f),
                    Color.Transparent
                )
            ),
            start = Offset(0f, currentScanY),
            end = Offset(width, currentScanY),
            strokeWidth = 2.dp.toPx()
        )

        // 5. Floating Glowing Micro-Particles
        val t = timeAnimation.value
        particles.forEach { p ->
            val py = ((p.y - t * p.speedY * 100f) % 1.0f + 1.0f) % 1.0f
            val px = (p.x + sin(t * 6.28318f + p.y * 10f) * 0.05f) % 1.0f

            val actualX = px * width
            val actualY = py * height

            drawCircle(
                color = p.color.copy(alpha = p.alpha * (0.6f + sin(t * 12.56f) * 0.4f)),
                center = Offset(actualX, actualY),
                radius = p.radius.dp.toPx()
            )
        }

        // 6. HUD Corner Frame Brackets ([ ])
        val bracketSize = 24.dp.toPx()
        val margin = 16.dp.toPx()
        val strokeW = 1.5.dp.toPx()
        val hudColor = Color(0xFF00E5FF).copy(alpha = 0.35f)

        // Top Left
        val pathTL = Path().apply {
            moveTo(margin, margin + bracketSize)
            lineTo(margin, margin)
            lineTo(margin + bracketSize, margin)
        }
        drawPath(pathTL, color = hudColor, style = Stroke(width = strokeW))

        // Top Right
        val pathTR = Path().apply {
            moveTo(width - margin - bracketSize, margin)
            lineTo(width - margin, margin)
            lineTo(width - margin, margin + bracketSize)
        }
        drawPath(pathTR, color = hudColor, style = Stroke(width = strokeW))

        // Bottom Left
        val pathBL = Path().apply {
            moveTo(margin, height - margin - bracketSize)
            lineTo(margin, height - margin)
            lineTo(margin + bracketSize, height - margin)
        }
        drawPath(pathBL, color = hudColor, style = Stroke(width = strokeW))

        // Bottom Right
        val pathBR = Path().apply {
            moveTo(width - margin - bracketSize, height - margin)
            lineTo(width - margin, height - margin)
            lineTo(width - margin, height - margin - bracketSize)
        }
        drawPath(pathBR, color = hudColor, style = Stroke(width = strokeW))
    }
}
