package com.example.game

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GamePhysics {
    // Constants
    const val GRAVITY = 0.5f
    const val JUMP_FORCE = -11f
    const val MAX_SPEED = 7f
    const val DRAG = 0.85f
    const val BOT_REACTION_RATE = 0.15f // Chance of bot changing input per frame

    // Main Update Function
    fun updateObstacleRace(
        players: List<GamePlayer>,
        obstacles: List<Obstacle>,
        mapWidth: Float = 3000f,
        mapHeight: Float = 600f,
        joystickAngles: Map<String, Float?>, // Player ID -> Joystick angle in radians (null if no joystick)
        playerJumpPressed: Map<String, Boolean>,
        playerActionPressed: Map<String, Boolean>,
        botReactionRate: Float = 0.15f,
        botSpeedFactor: Float = 1.0f
    ) {
        // 1. Update obstacles
        obstacles.forEach { obs ->
            obs.timePhase += 0.05f
            when (obs.type) {
                ObstacleType.MOVING_PLATFORM -> {
                    // Left-right or up-down movement
                    obs.x = obs.startX + cos(obs.timePhase) * obs.rangeX
                    obs.y = obs.startY + sin(obs.timePhase) * obs.rangeY
                }
                ObstacleType.SPINNING_BAR -> {
                    // Update angle
                    obs.angle = (obs.angle + obs.rotationSpeed) % (2f * Math.PI.toFloat())
                }
                ObstacleType.FALLING_BLOCK -> {
                    // Cyclic drop and rise
                    val cycle = obs.timePhase % 8f
                    if (cycle < 2f) {
                        // Standing up high
                        obs.y = obs.startY
                    } else if (cycle < 3f) {
                        // Slamming down
                        val t = cycle - 2f
                        obs.y = obs.startY + obs.rangeY * (t * t) // quadratic slam
                    } else if (cycle < 6f) {
                        // Sitting on ground
                        obs.y = obs.startY + obs.rangeY
                    } else {
                        // Slowly returning
                        val t = (cycle - 6f) / 2f
                        obs.y = obs.startY + obs.rangeY * (1f - t)
                    }
                }
                else -> {}
            }
        }

        // 2. Update players
        players.forEach { p ->
            if (p.isEliminated || p.isQualified) return@forEach

            // Apply Stumble recovery timer
            if (p.stumbleTimer > 0) {
                p.stumbleTimer -= 1f / 60f
                if (p.stumbleTimer <= 0) {
                    p.currentAnimation = AnimationState.IDLE
                }
            }

            // A. Compute Inputs
            var moveX = 0f
            var moveY = 0f
            var wantJump = false

            if (p.isBot) {
                // BOT AI for Obstacle Race: simple seek finish line (at mapWidth - 100)
                // Avoid obstacles, try to jump if approaching a block or gap
                val targetX = mapWidth - 150f
                val dx = targetX - p.x
                
                // Add minor random variation
                if (Math.random() < botReactionRate) {
                    moveX = if (dx > 0) 0.8f * botSpeedFactor else -0.2f * botSpeedFactor
                    // Sideways adjustments
                    val idealY = mapHeight / 2f + sin(p.x * 0.01f) * 80f
                    val dy = idealY - p.y
                    moveY = (if (dy > 20f) 0.4f else if (dy < -20f) -0.4f else 0f) * botSpeedFactor

                    // Bot jump criteria: jump over barriers or gaps
                    val aheadX = p.x + 80f
                    val hitsObstacleAhead = obstacles.any { obs ->
                        obs.type != ObstacleType.FINISH_LINE &&
                        aheadX >= obs.x && aheadX <= obs.x + obs.width &&
                        p.y + p.radius >= obs.y - 100f && p.y - p.radius <= obs.y + obs.height
                    }
                    if (hitsObstacleAhead && p.vy == 0f && Math.random() < 0.6) {
                        wantJump = true
                    }
                } else {
                    // Carry over previous velocity or move slightly right
                    moveX = if (p.vx >= 0) 0.8f * botSpeedFactor else 0.2f * botSpeedFactor
                    moveY = if (p.vy == 0f) 0f else -0.1f * botSpeedFactor
                }
            } else {
                // Real player controls
                val angle = joystickAngles[p.id]
                if (angle != null) {
                    moveX = cos(angle)
                    moveY = sin(angle)
                }
                if (playerJumpPressed[p.id] == true) {
                    wantJump = true
                }
            }

            // If stumbling, control is heavily reduced
            if (p.stumbleTimer > 0f) {
                moveX *= 0.15f
                moveY *= 0.15f
                wantJump = false
                p.currentAnimation = AnimationState.STUMBLE
            }

            // B. Apply Physics forces
            p.vx = (p.vx + moveX * 0.5f) * DRAG
            
            // Speed cap anti-cheat check
            if (sqrt(p.vx * p.vx) > MAX_SPEED) {
                p.vx = if (p.vx > 0) MAX_SPEED else -MAX_SPEED
            }

            // Gravity & Vertical motion
            p.vy += GRAVITY
            p.y += p.vy
            p.x += p.vx

            // Keep in horizontal boundaries
            if (p.x < p.radius) {
                p.x = p.radius
                p.vx = 0f
            }

            // Wobble effect
            if (sqrt(p.vx * p.vx) > 0.1f) {
                p.wobblePhase += 0.25f
                if (p.vy == 0f) {
                    p.currentAnimation = AnimationState.RUN
                }
            } else {
                p.wobblePhase += 0.05f
                if (p.vy == 0f && p.stumbleTimer <= 0) {
                    p.currentAnimation = AnimationState.IDLE
                }
            }

            if (p.vy < -1f) {
                p.currentAnimation = AnimationState.JUMP
            } else if (p.vy > 1f) {
                p.currentAnimation = AnimationState.FALL
            }

            // C. Ground/Platform Collision
            var onGround = false
            val groundY = mapHeight - 100f // Deepest ground level

            if (p.y + p.radius >= groundY) {
                p.y = groundY - p.radius
                p.vy = 0f
                onGround = true
            }

            // Check obstacle platforms
            obstacles.forEach { obs ->
                when (obs.type) {
                    ObstacleType.MOVING_PLATFORM, ObstacleType.STATIC_BOX -> {
                        // Check if landing on top of the block
                        val buffer = 12f
                        if (p.x + p.radius - buffer >= obs.x && p.x - p.radius + buffer <= obs.x + obs.width) {
                            if (p.vy >= 0f && p.y + p.radius >= obs.y && p.y - p.radius <= obs.y + 10f) {
                                p.y = obs.y - p.radius
                                p.vy = 0f
                                onGround = true
                                // Inherit platform motion
                                if (obs.type == ObstacleType.MOVING_PLATFORM) {
                                    p.x += -sin(obs.timePhase) * obs.rangeX * 0.05f // relative movement
                                }
                            }
                        }
                    }
                    ObstacleType.BOUNCY_PAD -> {
                        // High jump launcher
                        if (p.x + p.radius >= obs.x && p.x - p.radius <= obs.x + obs.width) {
                            if (p.y + p.radius >= obs.y && p.y - p.radius <= obs.y + obs.height) {
                                p.vy = JUMP_FORCE * 1.6f
                                p.y = obs.y - p.radius
                                p.currentAnimation = AnimationState.JUMP
                                p.stumbleTimer = 0f // break stumble on spring!
                            }
                        }
                    }
                    ObstacleType.SPEED_PAD -> {
                        // Boost pad
                        if (p.x + p.radius >= obs.x && p.x - p.radius <= obs.x + obs.width) {
                            if (p.y + p.radius >= obs.y && p.y - p.radius <= obs.y + obs.height) {
                                p.vx += 3f // Boost right!
                            }
                        }
                    }
                    ObstacleType.SPINNING_BAR -> {
                        // Interactive hazard: check circle-line collision for spinning bar
                        val barLength = obs.width
                        val endX = obs.x + cos(obs.angle) * barLength
                        val endY = obs.y + sin(obs.angle) * barLength
                        
                        // Distance from player center to line segment (obs.x, obs.y) -> (endX, endY)
                        val dist = getDistanceToSegment(p.x, p.y, obs.x, obs.y, endX, endY)
                        if (dist < p.radius + 6f) {
                            // Hit! Trigger stumble and throw player back
                            p.stumbleTimer = 1.0f // 1 second stumble
                            p.vy = -5f
                            p.vx = -6f * if (p.x > obs.x) -1f else 1f
                            p.currentAnimation = AnimationState.STUMBLE
                        }
                    }
                    ObstacleType.FALLING_BLOCK -> {
                        // Crush hazard: if player is underneath or directly inside
                        if (p.x + p.radius >= obs.x && p.x - p.radius <= obs.x + obs.width) {
                            if (p.y + p.radius >= obs.y && p.y - p.radius <= obs.y + obs.height) {
                                p.stumbleTimer = 1.5f // Long stumble
                                p.vy = 4f
                                p.vx = -5f
                                p.currentAnimation = AnimationState.STUMBLE
                            }
                        }
                    }
                    ObstacleType.FINISH_LINE -> {
                        if (p.x >= obs.x) {
                            p.isQualified = true
                            p.currentAnimation = AnimationState.CELEBRATE
                        }
                    }
                }
            }

            // Jump Action
            if (wantJump && onGround) {
                p.vy = JUMP_FORCE
                p.currentAnimation = AnimationState.JUMP
            }
        }
    }

    // Survival Hexagon Physics simulation
    fun updateSurvivalHexagon(
        players: List<GamePlayer>,
        tiles: List<HexTile>,
        platformRadius: Float = 300f,
        centerPointX: Float = 400f,
        centerPointY: Float = 300f,
        joystickAngles: Map<String, Float?>,
        playerJumpPressed: Map<String, Boolean>,
        elapsedSeconds: Float,
        botReactionRate: Float = 0.15f,
        botSpeedFactor: Float = 1.0f
    ) {
        // Platform shrinking over time: as match gets older, make outer tiles fall away automatically!
        val maxDistAllowed = platformRadius * (1f - (elapsedSeconds / 100f).coerceIn(0f, 0.8f))

        tiles.forEach { tile ->
            val dx = tile.centerX - centerPointX
            val dy = tile.centerY - centerPointY
            val dist = sqrt(dx * dx + dy * dy)
            if (dist > maxDistAllowed && tile.state != TileState.FALLEN) {
                tile.state = TileState.FALLEN
            }

            // Progress warning colors
            if (tile.state == TileState.WARNING) {
                tile.standTime += 1f / 60f
                if (tile.standTime >= 0.8f) {
                    tile.state = TileState.CRITICAL
                }
            } else if (tile.state == TileState.CRITICAL) {
                tile.standTime += 1f / 60f
                if (tile.standTime >= 1.6f) {
                    tile.state = TileState.FALLEN
                }
            }
        }

        // Update players
        players.forEach { p ->
            if (p.isEliminated) return@forEach

            var moveX = 0f
            var moveY = 0f
            var wantJump = false

            if (p.isBot) {
                // BOT AI for Hexagon: find nearest non-fallen tile that is stable, walk towards its center
                if (Math.random() < botReactionRate) {
                    val safeTiles = tiles.filter { it.state != TileState.FALLEN }
                    if (safeTiles.isNotEmpty()) {
                        // Find a tile near us that isn't falling, or just walk towards the closest tile
                        val targetTile = safeTiles.minByOrNull { t ->
                            val dx = t.centerX - p.x
                            val dy = t.centerY - p.y
                            dx * dx + dy * dy
                        }
                        if (targetTile != null) {
                            val dx = targetTile.centerX - p.x
                            val dy = targetTile.centerY - p.y
                            val dist = sqrt(dx * dx + dy * dy)
                            if (dist > 5f) {
                                moveX = dx / dist * 0.7f * botSpeedFactor
                                moveY = dy / dist * 0.7f * botSpeedFactor
                            }
                        }
                    }
                    if (Math.random() < 0.05 && p.vy == 0f) {
                        wantJump = true
                    }
                } else {
                    moveX = (if (p.vx > 0) 0.5f else -0.5f) * botSpeedFactor
                    moveY = (if (p.vy > 0) 0.5f else -0.5f) * botSpeedFactor
                }
            } else {
                val angle = joystickAngles[p.id]
                if (angle != null) {
                    moveX = cos(angle)
                    moveY = sin(angle)
                }
                if (playerJumpPressed[p.id] == true) {
                    wantJump = true
                }
            }

            p.vx = (p.vx + moveX * 0.4f) * DRAG
            p.vy = (p.vy + moveY * 0.4f) * DRAG

            // Speed cap anti-cheat check
            if (sqrt(p.vx * p.vx + p.vy * p.vy) > MAX_SPEED) {
                val totalSpeed = sqrt(p.vx * p.vx + p.vy * p.vy)
                p.vx = (p.vx / totalSpeed) * MAX_SPEED
                p.vy = (p.vy / totalSpeed) * MAX_SPEED
            }

            p.x += p.vx
            p.y += p.vy

            // Standard wobble animations
            val currentSpeed = sqrt(p.vx * p.vx + p.vy * p.vy)
            if (currentSpeed > 0.1f) {
                p.wobblePhase += 0.25f
                p.currentAnimation = AnimationState.RUN
            } else {
                p.wobblePhase += 0.05f
                p.currentAnimation = AnimationState.IDLE
            }

            // Check tile occupancy
            var onAnyTile = false
            tiles.forEach { tile ->
                if (tile.state != TileState.FALLEN) {
                    val dx = p.x - tile.centerX
                    val dy = p.y - tile.centerY
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist < tile.radius + p.radius) {
                        onAnyTile = true
                        // Trigger tile countdown!
                        if (tile.state == TileState.STABLE) {
                            tile.state = TileState.WARNING
                            tile.standTime = 0f
                        }
                    }
                }
            }

            // If fell off or no stable tile near them
            val dxCenter = p.x - centerPointX
            val dyCenter = p.y - centerPointY
            val distFromCenter = sqrt(dxCenter * dxCenter + dyCenter * dyCenter)
            
            if (distFromCenter > platformRadius || !onAnyTile) {
                // Out of platform: ELIMINATED!
                p.isEliminated = true
                p.currentAnimation = AnimationState.FALL
            }
        }
    }

    // Team Tail Tag Physics Simulation
    fun updateTeamTailTag(
        players: List<GamePlayer>,
        arenaRadius: Float = 350f,
        centerPointX: Float = 400f,
        centerPointY: Float = 300f,
        joystickAngles: Map<String, Float?>,
        playerActionPressed: Map<String, Boolean>,
        botReactionRate: Float = 0.15f,
        botSpeedFactor: Float = 1.0f
    ) {
        // Assign teams first if they aren't assigned
        val activePlayers = players.filter { !it.isEliminated }
        if (activePlayers.isNotEmpty() && activePlayers.first().team == Team.NONE) {
            // Split players half-half
            activePlayers.forEachIndexed { idx, p ->
                p.team = if (idx % 2 == 0) Team.RED else Team.BLUE
                p.hasTail = (idx % 4 == 0) // Give 25% of players tails
            }
        }

        // Update players
        activePlayers.forEach { p ->
            var moveX = 0f
            var moveY = 0f
            var wantGrab = false

            if (p.isBot) {
                // BOT AI for Tail Tag
                if (Math.random() < botReactionRate) {
                    if (p.hasTail) {
                        // RUN AWAY from closest opponent of opposite team without tail!
                        val threat = activePlayers.filter { it.team != p.team && !it.hasTail }.minByOrNull { other ->
                            val dx = other.x - p.x
                            val dy = other.y - p.y
                            dx * dx + dy * dy
                        }
                        if (threat != null) {
                            val dx = p.x - threat.x // vector pointing away
                            val dy = p.y - threat.y
                            val dist = sqrt(dx * dx + dy * dy)
                            if (dist > 0.1f) {
                                moveX = dx / dist * 0.8f * botSpeedFactor
                                moveY = dy / dist * 0.8f * botSpeedFactor
                            }
                        } else {
                            // Just wander around center
                            val dx = centerPointX - p.x
                            val dy = centerPointY - p.y
                            val dist = sqrt(dx * dx + dy * dy)
                            if (dist > 50f) {
                                moveX = dx / dist * 0.5f * botSpeedFactor
                                moveY = dy / dist * 0.5f * botSpeedFactor
                            }
                        }
                    } else {
                        // CHASE closest opponent of opposite team WITH tail!
                        val target = activePlayers.filter { it.team != p.team && it.hasTail }.minByOrNull { other ->
                            val dx = other.x - p.x
                            val dy = other.y - p.y
                            dx * dx + dy * dy
                        }
                        if (target != null) {
                            val dx = target.x - p.x
                            val dy = target.y - p.y
                            val dist = sqrt(dx * dx + dy * dy)
                            if (dist > 5f) {
                                moveX = dx / dist * 0.9f * botSpeedFactor
                                moveY = dy / dist * 0.9f * botSpeedFactor
                            }
                            // Proximity grab action
                            if (dist < p.radius + target.radius + 15f && Math.random() < 0.4) {
                                wantGrab = true
                            }
                        } else {
                            // Wander towards center to search
                            val dx = centerPointX - p.x
                            val dy = centerPointY - p.y
                            val dist = sqrt(dx * dx + dy * dy)
                            if (dist > 10f) {
                                moveX = dx / dist * 0.5f * botSpeedFactor
                                moveY = dy / dist * 0.5f * botSpeedFactor
                            }
                        }
                    }
                } else {
                    moveX = (if (p.vx > 0) 0.6f else -0.6f) * botSpeedFactor
                    moveY = (if (p.vy > 0) 0.6f else -0.6f) * botSpeedFactor
                }
            } else {
                val angle = joystickAngles[p.id]
                if (angle != null) {
                    moveX = cos(angle)
                    moveY = sin(angle)
                }
                if (playerActionPressed[p.id] == true) {
                    wantGrab = true
                }
            }

            p.vx = (p.vx + moveX * 0.45f) * DRAG
            p.vy = (p.vy + moveY * 0.45f) * DRAG

            // Speed cap anti-cheat check
            if (sqrt(p.vx * p.vx + p.vy * p.vy) > MAX_SPEED) {
                val totalSpeed = sqrt(p.vx * p.vx + p.vy * p.vy)
                p.vx = (p.vx / totalSpeed) * MAX_SPEED
                p.vy = (p.vy / totalSpeed) * MAX_SPEED
            }

            p.x += p.vx
            p.y += p.vy

            // Keep inside circular arena bounds
            val dxCenter = p.x - centerPointX
            val dyCenter = p.y - centerPointY
            val distFromCenter = sqrt(dxCenter * dxCenter + dyCenter * dyCenter)
            if (distFromCenter + p.radius > arenaRadius) {
                val ratio = (arenaRadius - p.radius) / distFromCenter
                p.x = centerPointX + dxCenter * ratio
                p.y = centerPointY + dyCenter * ratio
                p.vx *= -0.3f
                p.vy *= -0.3f
            }

            // Wobble effect
            val speed = sqrt(p.vx * p.vx + p.vy * p.vy)
            if (speed > 0.1f) {
                p.wobblePhase += 0.25f
                p.currentAnimation = AnimationState.RUN
            } else {
                p.wobblePhase += 0.05f
                p.currentAnimation = AnimationState.IDLE
            }

            // Handle grabbing tails
            if (wantGrab) {
                p.currentAnimation = AnimationState.GRAB
                if (!p.hasTail) {
                    // Try to swipe tail from an opponent of opposite team
                    val target = activePlayers.firstOrNull { other ->
                        other.team != p.team && other.hasTail &&
                        getDistance(p.x, p.y, other.x, other.y) < p.radius + other.radius + 30f
                    }
                    if (target != null) {
                        // Swipe!
                        target.hasTail = false
                        p.hasTail = true
                        p.score += 50 // Points for stealing a tail!
                        p.currentAnimation = AnimationState.CELEBRATE
                    }
                }
            }
        }
    }

    // Mathematical utility helpers
    private fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }

    private fun getDistanceToSegment(px: Float, py: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val l2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)
        if (l2 == 0f) return getDistance(px, py, x1, y1)
        
        // Projection t bounded between 0 and 1
        var t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / l2
        t = t.coerceIn(0f, 1f)
        
        val projX = x1 + t * (x2 - x1)
        val projY = y1 + t * (y2 - y1)
        return getDistance(px, py, projX, projY)
    }
}
