package com.example.game

enum class AnimationState {
    IDLE, RUN, JUMP, FALL, CELEBRATE, STUMBLE, GRAB
}

enum class Team {
    NONE, RED, BLUE
}

enum class GameMode {
    LOBBY,
    OBSTACLE_RACE,
    SURVIVAL_HEXAGON,
    TEAM_TAIL_TAG,
    WINNER_SCREEN
}

data class GamePlayer(
    val id: String,
    val name: String,
    val colorIndex: Int,
    val hat: String,
    val outfit: String,
    val isBot: Boolean = false,
    val isReady: Boolean = false,
    val isHost: Boolean = false,
    var isEliminated: Boolean = false,
    var isQualified: Boolean = false,
    
    // Position and Physics
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var radius: Float = 22f,
    
    // Gameplay states
    var hasTail: Boolean = false,
    var score: Int = 0,
    var currentAnimation: AnimationState = AnimationState.IDLE,
    var wobblePhase: Float = 0f,
    var stumbleTimer: Float = 0f, // Time remaining in stunned/stumble state
    var team: Team = Team.NONE,
    var finishTime: Float = 0f,   // Track when they cross line in Race
    var originalX: Float = 0f,    // Spawning coordinates
    var originalY: Float = 0f
)

// Represents dynamic objects in Obstacle course
data class Obstacle(
    val id: String,
    val type: ObstacleType,
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    // Movement parameters
    var angle: Float = 0f,
    val rotationSpeed: Float = 0f,
    val rangeX: Float = 0f,
    val rangeY: Float = 0f,
    val speed: Float = 0f,
    val startX: Float = x,
    val startY: Float = y,
    var timePhase: Float = 0f
)

enum class ObstacleType {
    SPINNING_BAR,
    MOVING_PLATFORM,
    BOUNCY_PAD,
    FALLING_BLOCK,
    SPEED_PAD,
    STATIC_BOX,
    FINISH_LINE
}

// Survival Hexagon Tile
data class HexTile(
    val id: Int,
    val row: Int,
    val col: Int,
    val centerX: Float,
    val centerY: Float,
    val radius: Float = 36f,
    var state: TileState = TileState.STABLE,
    var standTime: Float = 0f // Accumulates while player is standing on it
)

enum class TileState {
    STABLE,     // Green
    WARNING,    // Yellow
    CRITICAL,   // Red
    FALLEN      // Vanished
}
