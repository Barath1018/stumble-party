package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope as ComposeDrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.game.AnimationState
import com.example.game.GamePlayer
import com.example.game.Team
import kotlin.math.cos
import kotlin.math.sin

object CharacterRenderer {

    // Palette mapping for indexes
    val SKINS = listOf(
        Color(0xFFFF4081), // 0: Pink
        Color(0xFFFF6D00), // 1: Orange
        Color(0xFFFFD600), // 2: Yellow
        Color(0xFF00E676), // 3: Green
        Color(0xFF00B0FF), // 4: Cyan Blue
        Color(0xFFAA00FF)  // 5: Purple
    )

    fun getSkinColor(index: Int): Color {
        return SKINS.getOrElse(index) { SKINS[0] }
    }

    fun drawCharacter(
        scope: ComposeDrawScope,
        player: GamePlayer,
        px: Float,
        py: Float,
        radius: Float = 22f
    ) {
        val baseColor = getSkinColor(player.colorIndex)
        val phase = player.wobblePhase
        val isStumbling = player.stumbleTimer > 0f

        // Calculate dynamic stretch/squish scale factors
        var scaleX = 1.0f
        var scaleY = 1.0f
        var rotationAngle = 0f

        when (player.currentAnimation) {
            AnimationState.RUN -> {
                // Running squish and stretch wobble
                scaleX = 1f + 0.08f * sin(phase)
                scaleY = 1f - 0.08f * sin(phase)
                rotationAngle = player.vx * 3f // tilt into direction of movement
            }
            AnimationState.JUMP -> {
                scaleX = 0.85f
                scaleY = 1.15f
            }
            AnimationState.FALL -> {
                scaleX = 1.1f
                scaleY = 0.9f
            }
            AnimationState.CELEBRATE -> {
                scaleX = 1f + 0.1f * sin(phase * 1.5f)
                scaleY = 1f + 0.15f * cos(phase * 1.5f)
                rotationAngle = sin(phase) * 10f
            }
            AnimationState.STUMBLE -> {
                scaleX = 1.15f
                scaleY = 0.8f
                rotationAngle = if (player.vx >= 0) 40f else -40f
            }
            else -> {
                // Idle gentle wobble breathing
                scaleX = 1.0f + 0.03f * sin(phase * 0.5f)
                scaleY = 1.0f - 0.03f * sin(phase * 0.5f)
            }
        }

        scope.withTransform({
            // Scale and rotate around player's feet
            translate(left = px, top = py)
            scale(scaleX, scaleY, pivot = Offset(0f, radius))
            rotate(rotationAngle, pivot = Offset(0f, 0f))
        }) {
            // Draw simple Drop Shadow underneath
            drawOval(
                color = Color(0x3F000000),
                topLeft = Offset(-radius * 1.1f, radius * 0.8f),
                size = Size(radius * 2.2f, radius * 0.4f)
            )

            // Draw Outer Superhero Cape first (if applicable)
            if (player.outfit == "Superhero") {
                val capePath = Path().apply {
                    moveTo(-radius * 0.4f, -radius * 0.4f)
                    lineTo(-radius * 1.8f - player.vx * 3f, radius * 1.2f)
                    lineTo(radius * 0.2f - player.vx * 3f, radius * 1.4f)
                    lineTo(radius * 0.4f, -radius * 0.4f)
                    close()
                }
                drawPath(capePath, color = Color(0xFFD50000))
            }

            // Draw Dino Spikes (if applicable)
            if (player.outfit == "Dino") {
                for (i in 0..2) {
                    val spikeY = -radius * 0.3f + i * (radius * 0.6f)
                    val spikePath = Path().apply {
                        moveTo(-radius * 0.8f, spikeY)
                        lineTo(-radius * 1.3f, spikeY + radius * 0.2f)
                        lineTo(-radius * 0.8f, spikeY + radius * 0.4f)
                        close()
                    }
                    drawPath(spikePath, color = Color(0xFF2E7D32))
                }
            }

            // Main Capsule Body
            val rectWidth = radius * 2f
            val rectHeight = radius * 2.8f
            drawRoundRect(
                color = baseColor,
                topLeft = Offset(-radius, -radius * 1.6f),
                size = Size(rectWidth, rectHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
            )

            // 3D Highlight layer (overlay white with alpha)
            drawRoundRect(
                color = Color(0x33FFFFFF),
                topLeft = Offset(-radius * 0.8f, -radius * 1.4f),
                size = Size(radius * 0.6f, radius * 1.2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.3f, radius * 0.3f)
            )

            // Outfits / Costumes
            when (player.outfit) {
                "Ninja" -> {
                    // Draw Black Ninja mask covering face except eyes
                    drawRoundRect(
                        color = Color(0xFF212121),
                        topLeft = Offset(-radius, -radius * 0.6f),
                        size = Size(radius * 2f, radius * 1.2f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.2f, radius * 0.2f)
                    )
                    // Red Headband
                    drawRect(
                        color = Color(0xFFD50000),
                        topLeft = Offset(-radius, -radius * 1.0f),
                        size = Size(radius * 2f, radius * 0.25f)
                    )
                }
                "Astronaut" -> {
                    // Blue visor
                    drawRoundRect(
                        color = Color(0xFF00E5FF),
                        topLeft = Offset(-radius * 0.8f, -radius * 0.8f),
                        size = Size(radius * 1.6f, radius * 1.3f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.4f, radius * 0.4f)
                    )
                    // Silver collar
                    drawRoundRect(
                        color = Color(0xFFECEFF1),
                        topLeft = Offset(-radius * 0.9f, radius * 0.6f),
                        size = Size(radius * 1.8f, radius * 0.4f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.1f, radius * 0.1f)
                    )
                }
                "Casual" -> {
                    // Striped tee shirt
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(-radius * 0.95f, radius * 0.1f),
                        size = Size(radius * 1.9f, radius * 1.0f)
                    )
                    // Orange stripes
                    for (i in 0..2) {
                        drawRect(
                            color = Color(0xFFFF5722),
                            topLeft = Offset(-radius * 0.95f, radius * (0.1f + i * 0.35f)),
                            size = Size(radius * 1.9f, radius * 0.12f)
                        )
                    }
                }
                "Tuxedo" -> {
                    // Black jacket outlines
                    drawRect(
                        color = Color(0xFF1A1A1A),
                        topLeft = Offset(-radius * 0.95f, radius * 0.1f),
                        size = Size(radius * 1.9f, radius * 1.0f)
                    )
                    // White shirt V-neck
                    val shirtPath = Path().apply {
                        moveTo(-radius * 0.4f, radius * 0.1f)
                        lineTo(radius * 0.4f, radius * 0.1f)
                        lineTo(0f, radius * 0.7f)
                        close()
                    }
                    drawPath(shirtPath, color = Color.White)
                    // Tiny Red Bowtie
                    val leftTie = Path().apply {
                        moveTo(-radius * 0.25f, radius * 0.2f)
                        lineTo(0f, radius * 0.3f)
                        lineTo(-radius * 0.25f, radius * 0.4f)
                        close()
                    }
                    val rightTie = Path().apply {
                        moveTo(radius * 0.25f, radius * 0.2f)
                        lineTo(0f, radius * 0.3f)
                        lineTo(radius * 0.25f, radius * 0.4f)
                        close()
                    }
                    drawPath(leftTie, color = Color.Red)
                    drawPath(rightTie, color = Color.Red)
                }
                else -> {}
            }

            // Draw Eyes (Cartoony, follow velocity direction)
            val lookOffsetX = (player.vx * 1.2f).coerceIn(-radius * 0.3f, radius * 0.3f)
            val lookOffsetY = (player.vy * 0.8f).coerceIn(-radius * 0.2f, radius * 0.2f)

            // Left Eye
            drawCircle(
                color = Color.White,
                radius = radius * 0.3f,
                center = Offset(-radius * 0.4f + lookOffsetX, -radius * 0.3f + lookOffsetY)
            )
            drawCircle(
                color = Color.Black,
                radius = radius * 0.15f,
                center = Offset(-radius * 0.4f + lookOffsetX * 1.4f, -radius * 0.3f + lookOffsetY * 1.4f)
            )

            // Right Eye
            drawCircle(
                color = Color.White,
                radius = radius * 0.3f,
                center = Offset(radius * 0.4f + lookOffsetX, -radius * 0.3f + lookOffsetY)
            )
            drawCircle(
                color = Color.Black,
                radius = radius * 0.15f,
                center = Offset(radius * 0.4f + lookOffsetX * 1.4f, -radius * 0.3f + lookOffsetY * 1.4f)
            )

            // Cheerful mouth
            val mouthPath = Path().apply {
                moveTo(-radius * 0.15f, radius * 0.1f)
                quadraticBezierTo(0f, radius * 0.25f, radius * 0.15f, radius * 0.1f)
            }
            drawPath(
                path = mouthPath,
                color = Color.Black,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )

            // Draw Hats
            when (player.hat) {
                "Crown" -> {
                    val crownPath = Path().apply {
                        moveTo(-radius * 0.7f, -radius * 1.5f)
                        lineTo(-radius * 0.9f, -radius * 2.1f)
                        lineTo(-radius * 0.3f, -radius * 1.7f)
                        lineTo(0f, -radius * 2.3f)
                        lineTo(radius * 0.3f, -radius * 1.7f)
                        lineTo(radius * 0.9f, -radius * 2.1f)
                        lineTo(radius * 0.7f, -radius * 1.5f)
                        close()
                    }
                    drawPath(crownPath, color = Color(0xFFFFD54F))
                    // Crown band
                    drawRect(
                        color = Color(0xFFFFA000),
                        topLeft = Offset(-radius * 0.7f, -radius * 1.58f),
                        size = Size(radius * 1.4f, radius * 0.12f)
                    )
                    // Ruby jewel in center
                    drawCircle(
                        color = Color.Red,
                        radius = radius * 0.08f,
                        center = Offset(0f, -radius * 1.8f)
                    )
                }
                "Wizard" -> {
                    // Wizard cone hat
                    val wizardCone = Path().apply {
                        moveTo(-radius * 0.8f, -radius * 1.5f)
                        lineTo(0f, -radius * 2.6f)
                        lineTo(radius * 0.8f, -radius * 1.5f)
                        close()
                    }
                    drawPath(wizardCone, color = Color(0xFF3F51B5))
                    // Stars on hat
                    drawCircle(Color(0xFFFFEB3B), radius * 0.08f, Offset(-radius * 0.2f, -radius * 1.8f))
                    drawCircle(Color(0xFFFFEB3B), radius * 0.08f, Offset(radius * 0.2f, -radius * 2.1f))
                    // Brim
                    drawRoundRect(
                        color = Color(0xFF303F9F),
                        topLeft = Offset(-radius * 1.0f, -radius * 1.6f),
                        size = Size(radius * 2f, radius * 0.2f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.1f, radius * 0.1f)
                    )
                }
                "Pirate" -> {
                    // Pirate Tricorn
                    val hatPath = Path().apply {
                        moveTo(-radius * 1.1f, -radius * 1.4f)
                        quadraticBezierTo(0f, -radius * 2.3f, radius * 1.1f, -radius * 1.4f)
                        quadraticBezierTo(0f, -radius * 1.2f, -radius * 1.1f, -radius * 1.4f)
                        close()
                    }
                    drawPath(hatPath, color = Color(0xFF111111))
                    // White Skull Emblem
                    drawCircle(Color.White, radius * 0.12f, Offset(0f, -radius * 1.55f))
                }
                "Chef" -> {
                    // Fluffy chef hat
                    drawCircle(Color.White, radius * 0.5f, Offset(0f, -radius * 2.0f))
                    drawCircle(Color.White, radius * 0.4f, Offset(-radius * 0.4f, -radius * 1.8f))
                    drawCircle(Color.White, radius * 0.4f, Offset(radius * 0.4f, -radius * 1.8f))
                    // Base band
                    drawRoundRect(
                        color = Color(0xFFECEFF1),
                        topLeft = Offset(-radius * 0.6f, -radius * 1.6f),
                        size = Size(radius * 1.2f, radius * 0.3f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.05f, radius * 0.05f)
                    )
                }
                "Cowboy" -> {
                    // Brown cowboy hat brim
                    drawRoundRect(
                        color = Color(0xFF795548),
                        topLeft = Offset(-radius * 1.3f, -radius * 1.55f),
                        size = Size(radius * 2.6f, radius * 0.18f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.2f, radius * 0.2f)
                    )
                    // Top dome
                    val domePath = Path().apply {
                        moveTo(-radius * 0.7f, -radius * 1.5f)
                        lineTo(-radius * 0.6f, -radius * 2.1f)
                        quadraticBezierTo(0f, -radius * 2.3f, radius * 0.6f, -radius * 2.1f)
                        lineTo(radius * 0.7f, -radius * 1.5f)
                        close()
                    }
                    drawPath(domePath, color = Color(0xFF5D4037))
                }
                else -> {}
            }

            // Draw Tail (If player has tail in Team Tail Tag!)
            if (player.hasTail) {
                val tailColor = if (player.team == Team.RED) Color(0xFFE53935) else if (player.team == Team.BLUE) Color(0xFF1E88E5) else Color(0xFFFFB300)
                val tailPath = Path().apply {
                    moveTo(-radius * 0.2f, radius * 0.4f)
                    quadraticBezierTo(
                        -radius * 1.2f - player.vx * 2f,
                        radius * 0.6f + sin(phase) * 6f,
                        -radius * 1.5f - player.vx * 2f,
                        radius * 0.2f + sin(phase) * 8f
                    )
                }
                drawPath(
                    path = tailPath,
                    color = tailColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
                // Fluffy ball at end of tail
                drawCircle(
                    color = Color.White,
                    radius = radius * 0.22f,
                    center = Offset(-radius * 1.5f - player.vx * 2f, radius * 0.2f + sin(phase) * 8f)
                )
            }

            // If stumbling, draw circling stars around head
            if (isStumbling) {
                val starPhase = phase * 1.2f
                for (i in 0..2) {
                    val angle = starPhase + i * (2f * Math.PI.toFloat() / 3f)
                    val starX = cos(angle) * radius * 0.9f
                    val starY = -radius * 1.8f + sin(angle) * radius * 0.2f
                    drawCircle(
                        color = Color(0xFFFFEB3B),
                        radius = radius * 0.1f,
                        center = Offset(starX, starY)
                    )
                }
            }
        }
    }
}
