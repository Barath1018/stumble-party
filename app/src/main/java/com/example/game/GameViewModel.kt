package com.example.game

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PlayerProfile as ProfileEntity
import com.example.data.GameRepository
import com.example.data.MatchHistory
import com.example.ui.components.CharacterRenderer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository

    // Database Streams
    val playerProfileFlow: StateFlow<com.example.data.PlayerProfile?>
    val matchHistoryFlow: StateFlow<List<MatchHistory>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = GameRepository(db)
        playerProfileFlow = repository.playerProfileFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
        matchHistoryFlow = repository.matchHistoryFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Load initial values or create a default profile
        viewModelScope.launch {
            val p = repository.getProfile()
            _username.value = p.username
            _colorIndex.value = p.selectedColorIndex
            _selectedHat.value = p.selectedHat
            _selectedOutfit.value = p.selectedOutfit
        }
    }

    // Customizer UI State
    private val _username = MutableStateFlow("WobblyBean")
    val username = _username.asStateFlow()

    private val _colorIndex = MutableStateFlow(0)
    val colorIndex = _colorIndex.asStateFlow()

    private val _selectedHat = MutableStateFlow("Crown")
    val selectedHat = _selectedHat.asStateFlow()

    private val _selectedOutfit = MutableStateFlow("Ninja")
    val selectedOutfit = _selectedOutfit.asStateFlow()

    // Game Session States
    private val _gameState = MutableStateFlow(GameMode.LOBBY)
    val gameState = _gameState.asStateFlow()

    private val _players = MutableStateFlow<List<GamePlayer>>(emptyList())
    val players = _players.asStateFlow()

    private val _obstacles = MutableStateFlow<List<Obstacle>>(emptyList())
    val obstacles = _obstacles.asStateFlow()

    private val _hexTiles = MutableStateFlow<List<HexTile>>(emptyList())
    val hexTiles = _hexTiles.asStateFlow()

    private val _roundTimer = MutableStateFlow(90)
    val roundTimer = _roundTimer.asStateFlow()

    private val _qualifiedCount = MutableStateFlow(0)
    val qualifiedCount = _qualifiedCount.asStateFlow()

    private val _targetQualifiedCount = MutableStateFlow(5)
    val targetQualifiedCount = _targetQualifiedCount.asStateFlow()

    private val _activeRoundIndex = MutableStateFlow(1) // Round 1, 2, 3
    val activeRoundIndex = _activeRoundIndex.asStateFlow()

    private val _lobbyCode = MutableStateFlow("STUMBLE-5829")
    val lobbyCode = _lobbyCode.asStateFlow()

    private val _matchWinnerName = MutableStateFlow("")
    val matchWinnerName = _matchWinnerName.asStateFlow()

    private val _isLocalPlayerWinner = MutableStateFlow(false)
    val isLocalPlayerWinner = _isLocalPlayerWinner.asStateFlow()

    private val _matchPointsEarned = MutableStateFlow(0)
    val matchPointsEarned = _matchPointsEarned.asStateFlow()

    // Offline and Bot Mode Settings
    private val _isOfflineMode = MutableStateFlow(true)
    val isOfflineMode = _isOfflineMode.asStateFlow()

    private val _botDifficulty = MutableStateFlow("Normal") // Easy, Normal, Hard
    val botDifficulty = _botDifficulty.asStateFlow()

    // Simulated Matchmaking States
    private val _isMatchmaking = MutableStateFlow(false)
    val isMatchmaking = _isMatchmaking.asStateFlow()

    private val _matchmakingProgress = MutableStateFlow(0f)
    val matchmakingProgress = _matchmakingProgress.asStateFlow()

    private val _matchmakingStatusText = MutableStateFlow("SEARCHING FOR MATCH...")
    val matchmakingStatusText = _matchmakingStatusText.asStateFlow()

    // Network manager
    private var socketManager: SocketManager? = null
    val isNetworkConnected: Boolean
        get() = socketManager?.isCurrentlyConnected() ?: false

    // Game loop control
    private var gameLoopJob: Job? = null
    private var timeSeconds = 0f

    // Keyboard inputs for desktops / development checks
    val keyboardJoystickAngles = mutableMapOf<String, Float?>()
    val keyboardJumpPressed = mutableMapOf<String, Boolean>()
    val keyboardActionPressed = mutableMapOf<String, Boolean>()

    fun setUsername(name: String) {
        _username.value = name
        saveProfileToDb()
    }

    fun selectColor(idx: Int) {
        _colorIndex.value = idx
        saveProfileToDb()
    }

    fun selectHat(hat: String) {
        _selectedHat.value = hat
        saveProfileToDb()
    }

    fun selectOutfit(outfit: String) {
        _selectedOutfit.value = outfit
        saveProfileToDb()
    }

    private fun saveProfileToDb() {
        viewModelScope.launch {
            repository.saveProfile(
                com.example.data.PlayerProfile(
                    username = _username.value,
                    selectedColorIndex = _colorIndex.value,
                    selectedHat = _selectedHat.value,
                    selectedOutfit = _selectedOutfit.value
                )
            )
        }
    }

    fun setOfflineMode(offline: Boolean) {
        _isOfflineMode.value = offline
    }

    fun setBotDifficulty(difficulty: String) {
        _botDifficulty.value = difficulty
    }

    fun startPlayMatch() {
        if (_isOfflineMode.value) {
            // Instant play in Offline Mode
            createOrJoinLobby()
            startFirstRound()
        } else {
            // Trigger a beautiful simulated online matchmaking queue
            viewModelScope.launch {
                _isMatchmaking.value = true
                _matchmakingProgress.value = 0f
                _matchmakingStatusText.value = "CONNECTING TO REGIONAL GAME SERVER..."
                delay(600)
                
                _matchmakingStatusText.value = "SEARCHING FOR PLAYERS (1/16)..."
                _matchmakingProgress.value = 0.2f
                delay(600)
                
                _matchmakingStatusText.value = "FOUND ROOM! SYNCING PLAYERS..."
                _matchmakingProgress.value = 0.45f
                delay(500)
                
                _matchmakingStatusText.value = "WAITING FOR OPPONENTS JOINING (9/16)..."
                _matchmakingProgress.value = 0.65f
                delay(600)
                
                _matchmakingStatusText.value = "SYNCING PLAYER DESIGNS (15/16)..."
                _matchmakingProgress.value = 0.85f
                delay(500)
                
                _matchmakingStatusText.value = "MATCHMAKING COMPLETED! ALL READY (16/16)"
                _matchmakingProgress.value = 1.0f
                delay(400)
                
                _isMatchmaking.value = false
                createOrJoinLobby()
                startFirstRound()
            }
        }
    }

    // Connect to WebSocket Server if available
    fun connectToLobby(serverUrl: String = "ws://echo.websocket.org") {
        socketManager = SocketManager { event, data ->
            // Handle server-side multiplayer synching if specified
            Log.d("LobbyNetwork", "Event: $event, Data: $data")
        }
        socketManager?.connect(serverUrl)
    }

    fun createOrJoinLobby() {
        // Generate random lobby code
        val randomNum = (1000..9999).random()
        _lobbyCode.value = "PARTY-$randomNum"
        _gameState.value = GameMode.LOBBY

        // Setup Player List with user + 15 customizable bots to make matchmaking fast and lively!
        val list = mutableListOf<GamePlayer>()
        
        // Add current user
        list.add(
            GamePlayer(
                id = "local_player",
                name = _username.value,
                colorIndex = _colorIndex.value,
                hat = _selectedHat.value,
                outfit = _selectedOutfit.value,
                isHost = true,
                isReady = true
            )
        )

        // Bot names list
        val botNames = listOf(
            "WobbleBot", "JellyBean", "StumbleGuy", "CrownSeeker", "BounceMister",
            "SpeedyCapsule", "DinoRun", "TuxedoRunner", "Astronut", "CheesyChef",
            "MightyMascot", "TumbleWeed", "PiratePip", "CowboyCore", "SlippySlide"
        )

        botNames.forEachIndexed { index, name ->
            list.add(
                GamePlayer(
                    id = "bot_$index",
                    name = name,
                    colorIndex = (0..5).random(),
                    hat = listOf("None", "Crown", "Wizard", "Pirate", "Chef", "Cowboy").random(),
                    outfit = listOf("None", "Ninja", "Astronaut", "Superhero", "Dino", "Tuxedo", "Casual").random(),
                    isBot = true,
                    isReady = (Math.random() < 0.8) // Some are ready
                )
            )
        }

        _players.value = list
    }

    fun togglePlayerReady(playerId: String) {
        val updated = _players.value.map {
            if (it.id == playerId) it.copy(isReady = !it.isReady) else it
        }
        _players.value = updated
    }

    // Transition to dynamic rounds
    fun startFirstRound() {
        _isLocalPlayerWinner.value = false
        _activeRoundIndex.value = 1
        _gameState.value = GameMode.OBSTACLE_RACE
        setupObstacleRace()
        startGameLoop()
    }

    private fun setupObstacleRace() {
        _roundTimer.value = if (_isOfflineMode.value) 5 else 90
        timeSeconds = 0f
        _qualifiedCount.value = 0
        
        // Target is 50% of starting active players
        val activePlayersCount = _players.value.count { !it.isEliminated }
        _targetQualifiedCount.value = (activePlayersCount / 2).coerceAtLeast(1)

        // Spawning players at start line with scattered offsets
        _players.value.forEachIndexed { index, p ->
            p.isEliminated = false
            p.isQualified = false
            p.vx = 0f
            p.vy = 0f
            p.x = 50f + (index % 4) * 30f
            p.y = 450f - (index / 4) * 25f
            p.originalX = p.x
            p.originalY = p.y
            p.currentAnimation = AnimationState.IDLE
        }

        // Build obstacles map
        val list = mutableListOf<Obstacle>()
        
        // Spinning bars
        list.add(Obstacle("spin_1", ObstacleType.SPINNING_BAR, x = 400f, y = 300f, width = 90f, height = 10f, rotationSpeed = 0.04f))
        list.add(Obstacle("spin_2", ObstacleType.SPINNING_BAR, x = 750f, y = 350f, width = 100f, height = 10f, rotationSpeed = -0.05f))
        
        // Moving platforms
        list.add(Obstacle("move_1", ObstacleType.MOVING_PLATFORM, x = 1100f, y = 450f, width = 150f, height = 30f, rangeX = 120f, speed = 0.04f))
        list.add(Obstacle("move_2", ObstacleType.MOVING_PLATFORM, x = 1500f, y = 350f, width = 150f, height = 30f, rangeY = 100f, speed = 0.05f))

        // Bouncy Launch pads
        list.add(Obstacle("bounce_1", ObstacleType.BOUNCY_PAD, x = 600f, y = 480f, width = 50f, height = 20f))
        list.add(Obstacle("bounce_2", ObstacleType.BOUNCY_PAD, x = 1800f, y = 480f, width = 50f, height = 20f))

        // Falling blocks
        list.add(Obstacle("fall_1", ObstacleType.FALLING_BLOCK, x = 2100f, y = 100f, width = 80f, height = 250f, rangeY = 200f))
        list.add(Obstacle("fall_2", ObstacleType.FALLING_BLOCK, x = 2300f, y = 100f, width = 80f, height = 250f, rangeY = 200f))

        // Speed Boost pads
        list.add(Obstacle("boost_1", ObstacleType.SPEED_PAD, x = 1300f, y = 490f, width = 80f, height = 10f))
        list.add(Obstacle("boost_2", ObstacleType.SPEED_PAD, x = 2500f, y = 490f, width = 80f, height = 10f))

        // Solid structures
        list.add(Obstacle("box_1", ObstacleType.STATIC_BOX, x = 950f, y = 420f, width = 100f, height = 80f))
        list.add(Obstacle("box_2", ObstacleType.STATIC_BOX, x = 1950f, y = 420f, width = 100f, height = 80f))

        // Finish Line triggers qualifying
        list.add(Obstacle("finish", ObstacleType.FINISH_LINE, x = 2800f, y = 100f, width = 50f, height = 400f))

        _obstacles.value = list
    }

    private fun setupSurvivalHexagon() {
        _roundTimer.value = if (_isOfflineMode.value) 5 else 60
        timeSeconds = 0f
        
        val activePlayers = _players.value.filter { !it.isEliminated }
        _targetQualifiedCount.value = (activePlayers.size / 2).coerceAtLeast(1)

        // Spawning in circular cluster around center
        val cx = 400f
        val cy = 300f
        activePlayers.forEachIndexed { index, p ->
            p.isEliminated = false
            p.isQualified = false
            p.vx = 0f
            p.vy = 0f
            val angle = index * (2f * Math.PI.toFloat() / activePlayers.size)
            p.x = cx + cos(angle) * 120f
            p.y = cy + sin(angle) * 120f
            p.currentAnimation = AnimationState.IDLE
        }

        // Build Hex Tiles
        val tileList = mutableListOf<HexTile>()
        var tileId = 1
        val startRow = -4
        val endRow = 4
        val radius = 35f
        
        for (r in startRow..endRow) {
            val cols = 8 - Math.abs(r)
            val startC = -cols / 2
            for (c in 0 until cols) {
                val colIndex = startC + c
                // Isometric hex positioning
                val tx = cx + colIndex * radius * 1.75f + (if (Math.abs(r) % 2 != 0) radius * 0.85f else 0f)
                val ty = cy + r * radius * 1.5f
                
                tileList.add(
                    HexTile(
                        id = tileId++,
                        row = r,
                        col = colIndex,
                        centerX = tx,
                        centerY = ty,
                        radius = radius
                    )
                )
            }
        }

        _hexTiles.value = tileList
    }

    private fun setupTeamTailTag() {
        _roundTimer.value = if (_isOfflineMode.value) 5 else 60
        timeSeconds = 0f
        
        val activePlayers = _players.value.filter { !it.isEliminated }
        
        // Reset flags
        activePlayers.forEachIndexed { index, p ->
            p.isEliminated = false
            p.isQualified = false
            p.vx = 0f
            p.vy = 0f
            p.score = 0
            
            // Spawn inside the circular map
            val angle = index * (2f * Math.PI.toFloat() / activePlayers.size)
            p.x = 400f + cos(angle) * 200f
            p.y = 300f + sin(angle) * 200f
            
            p.team = if (index % 2 == 0) Team.RED else Team.BLUE
            p.hasTail = (index % 3 == 0) // 33% have tails initially
            p.currentAnimation = AnimationState.IDLE
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (true) {
                delay(16) // roughly 60 fps
                timeSeconds += 0.016f

                val currentDiff = _botDifficulty.value
                val reactionRate = when (currentDiff) {
                    "Easy" -> 0.08f
                    "Hard" -> 0.28f
                    else -> 0.16f // Normal
                }
                val speedFactor = when (currentDiff) {
                    "Easy" -> 0.7f
                    "Hard" -> 1.15f
                    else -> 0.95f // Normal
                }

                when (_gameState.value) {
                    GameMode.OBSTACLE_RACE -> {
                        GamePhysics.updateObstacleRace(
                            players = _players.value,
                            obstacles = _obstacles.value,
                            joystickAngles = keyboardJoystickAngles,
                            playerJumpPressed = keyboardJumpPressed,
                            playerActionPressed = keyboardActionPressed,
                            botReactionRate = reactionRate,
                            botSpeedFactor = speedFactor
                        )

                        // Update qualified count
                        val qualCount = _players.value.count { it.isQualified }
                        _qualifiedCount.value = qualCount

                        // If qualified quota met or time runs out, move to next round
                        val activePlayers = _players.value.filter { !it.isEliminated }
                        val finishedAll = activePlayers.all { it.isQualified || it.isEliminated }

                        if (qualCount >= _targetQualifiedCount.value || finishedAll || _roundTimer.value <= 0) {
                            if (_roundTimer.value <= 0 || finishedAll) {
                                val localPlayer = _players.value.find { it.id == "local_player" }
                                if (localPlayer != null && !localPlayer.isEliminated) {
                                    localPlayer.isQualified = true
                                }
                            }
                            eliminateUnqualifiedPlayers()
                            transitionToNextRound()
                        }
                    }

                    GameMode.SURVIVAL_HEXAGON -> {
                        GamePhysics.updateSurvivalHexagon(
                            players = _players.value,
                            tiles = _hexTiles.value,
                            joystickAngles = keyboardJoystickAngles,
                            playerJumpPressed = keyboardJumpPressed,
                            elapsedSeconds = timeSeconds,
                            botReactionRate = reactionRate,
                            botSpeedFactor = speedFactor
                        )

                        val activeRemaining = _players.value.filter { !it.isEliminated }
                        val localPlayer = _players.value.find { it.id == "local_player" }
                        val localPlayerEliminated = localPlayer == null || localPlayer.isEliminated
                        
                        // Last standing wins, or target qualifier count achieved, or local player is eliminated
                        if (activeRemaining.size <= _targetQualifiedCount.value || activeRemaining.size <= 1 || _roundTimer.value <= 0 || localPlayerEliminated) {
                            if (_roundTimer.value <= 0) {
                                val lp = _players.value.find { it.id == "local_player" }
                                if (lp != null) {
                                    lp.isEliminated = false
                                    lp.isQualified = true
                                }
                            }
                            val finalActiveRemaining = _players.value.filter { !it.isEliminated }
                            finalActiveRemaining.forEach { it.isQualified = true }
                            transitionToNextRound()
                        }
                    }

                    GameMode.TEAM_TAIL_TAG -> {
                        GamePhysics.updateTeamTailTag(
                            players = _players.value,
                            joystickAngles = keyboardJoystickAngles,
                            playerActionPressed = keyboardActionPressed,
                            botReactionRate = reactionRate,
                            botSpeedFactor = speedFactor
                        )

                        if (_roundTimer.value <= 0) {
                            determineWinningTeam()
                        }
                    }
                    else -> {}
                }

                // Handle round ticking
                if (timeSeconds >= 1f) {
                    timeSeconds = 0f
                    if (_roundTimer.value > 0) {
                        _roundTimer.value = _roundTimer.value - 1
                    }
                }
            }
        }
    }

    private fun eliminateUnqualifiedPlayers() {
        _players.value.forEach { p ->
            if (!p.isQualified && !p.isEliminated) {
                p.isEliminated = true
            }
        }
    }

    private fun transitionToNextRound() {
        val remainingPlayers = _players.value.filter { !it.isEliminated }
        val localPlayer = _players.value.find { it.id == "local_player" }
        
        // If the local player is eliminated, the game ends for them!
        if (localPlayer == null || localPlayer.isEliminated) {
            val winner = remainingPlayers.firstOrNull() ?: _players.value.firstOrNull { it.id != "local_player" } ?: _players.value.first()
            endGameSession(winner)
            return
        }
        
        if (remainingPlayers.size <= 1) {
            // Crown final winner!
            val winner = remainingPlayers.firstOrNull() ?: _players.value.first()
            endGameSession(winner)
            return
        }

        if (_activeRoundIndex.value == 1) {
            // Proceed to Survival Hexagon
            _activeRoundIndex.value = 2
            _gameState.value = GameMode.SURVIVAL_HEXAGON
            setupSurvivalHexagon()
        } else if (_activeRoundIndex.value == 2) {
            // Proceed to Tail Tag
            _activeRoundIndex.value = 3
            _gameState.value = GameMode.TEAM_TAIL_TAG
            setupTeamTailTag()
        } else {
            // Already round 3, find tail winners
            determineWinningTeam()
        }
    }

    private fun determineWinningTeam() {
        gameLoopJob?.cancel()
        val activePlayers = _players.value.filter { !it.isEliminated }
        
        val redTails = activePlayers.count { it.team == Team.RED && it.hasTail }
        val blueTails = activePlayers.count { it.team == Team.BLUE && it.hasTail }

        val localPlayer = _players.value.find { it.id == "local_player" }
        val winningTeam = if (localPlayer != null) {
            localPlayer.team
        } else {
            if (redTails >= blueTails) Team.RED else Team.BLUE
        }

        // Eliminate the losing team!
        _players.value.forEach { p ->
            if (p.team != winningTeam && !p.isEliminated) {
                p.isEliminated = true
            }
        }

        val survivors = _players.value.filter { !it.isEliminated }
        val finalLocalPlayer = survivors.find { it.id == "local_player" }
        if (finalLocalPlayer != null) {
            endGameSession(finalLocalPlayer)
        } else if (survivors.size > 1) {
            // If multiple survivors on winning team, winner is the one with highest score
            val topSurvivor = survivors.maxByOrNull { it.score } ?: survivors.first()
            endGameSession(topSurvivor)
        } else {
            val fallbackWinner = survivors.firstOrNull() ?: _players.value.firstOrNull { it.id != "local_player" } ?: _players.value.first()
            endGameSession(fallbackWinner)
        }
    }

    private fun endGameSession(winner: GamePlayer) {
        gameLoopJob?.cancel()
        _isLocalPlayerWinner.value = (winner.id == "local_player")
        _gameState.value = GameMode.WINNER_SCREEN
        _matchWinnerName.value = winner.name

        // Score tracking
        var scoreEarned = 0
        if (winner.id == "local_player") {
            scoreEarned += 100 // won match!
        } else {
            // Award score based on rounds survived
            scoreEarned += _activeRoundIndex.value * 30
        }
        _matchPointsEarned.value = scoreEarned

        // Save to Database MatchHistory using Repository Pattern!
        viewModelScope.launch {
            repository.recordMatch(
                MatchHistory(
                    gameSequence = "Obstacle, Hexagon, Tag",
                    rank = if (winner.id == "local_player") 1 else _activeRoundIndex.value + 1,
                    scoreEarned = scoreEarned
                )
            )
        }
    }

    fun exitToMain() {
        gameLoopJob?.cancel()
        _gameState.value = GameMode.LOBBY
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
        socketManager?.disconnect()
    }
}
