package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MatchHistory
import com.example.game.AnimationState
import com.example.game.GameMode
import com.example.game.GamePlayer
import com.example.game.GameViewModel
import com.example.game.HexTile
import com.example.game.Obstacle
import com.example.game.ObstacleType
import com.example.game.Team
import com.example.game.TileState
import com.example.ui.components.CharacterRenderer
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Vibrant Purple/Orange gradient theme
val GRADIENT_THEME = Brush.linearGradient(
    colors = listOf(
        Color(0xFF4A148C), // Deep Purple
        Color(0xFF8E24AA), // Medium Purple
        Color(0xFFFF5722)  // Deep Orange
    )
)

val ACCENT_ORANGE = Color(0xFFF97316) // Sleek Orange-500
val ACCENT_PINK = Color(0xFFEC4899)   // Sleek Pink-500

// Extension to render the exact Sleek Interface ambient radial glow background
fun Modifier.sleekAmbientBackground(): Modifier = this.drawBehind {
    // Fill deep navy background (#1E1B4B)
    drawRect(color = Color(0xFF1E1B4B))
    
    // Top-left purple-600/30 glow with blur (simulated via radial gradient)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF9333EA).copy(alpha = 0.35f), Color.Transparent),
            center = Offset(0f, 0f),
            radius = size.width * 0.75f
        ),
        radius = size.width * 0.75f,
        center = Offset(0f, 0f)
    )
    
    // Bottom-right pink-600/20 glow with blur
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFDB2777).copy(alpha = 0.25f), Color.Transparent),
            center = Offset(size.width, size.height),
            radius = size.width * 0.75f
        ),
        radius = size.width * 0.75f,
        center = Offset(size.width, size.height)
    )
}

@Composable
fun SleekTopStatusBar(username: String, wins: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Profile Badge (glassmorphism)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.3f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Brush.sweepGradient(listOf(Color(0xFFFACC15), Color(0xFFF97316))))
                    .border(1.5.dp, Color.White, CircleShape)
            ) {
                Text(
                    text = "LV.12",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = username.ifEmpty { "GUEST_BEAN" }.uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
        }

        // Right: Coins and Crowns currency badges
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Points/Stars Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "⭐ ${wins * 250 + 1200} PTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF34D399) // Emerald-400
                )
            }

            // Wins Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "👑 $wins WINS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFBBF24) // Amber-400
                )
            }
        }
    }
}

@Composable
fun SleekLobbyStatusPill(viewModel: GameViewModel) {
    val isOffline by viewModel.isOfflineMode.collectAsState()
    val botDiff by viewModel.botDifficulty.collectAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.2f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        if (isOffline) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFB74D)) // Amber orange pulse
            )
            Text(
                text = "🔌 OFFLINE PLAY: BOTS ($botDiff)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFB74D),
                letterSpacing = 1.sp
            )
        } else {
            // Overlapping small colored player bubbles for live simulation
            Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color(0xFF3B82F6)).border(1.dp, Color.White, CircleShape))
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color(0xFFEC4899)).border(1.dp, Color.White, CircleShape))
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color(0xFF10B981)).border(1.dp, Color.White, CircleShape))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4B5563))
                        .border(1.dp, Color.White, CircleShape)
                ) {
                    Text("+12", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Text(
                text = "🌐 SIMULATED MULTIPLAYER: 16 LOBBY PLAYERS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF34D399),
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun SleekBottomNavigationBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.4f))
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.05f))
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(icon = "Group", label = "Friends", active = false)
        BottomNavItem(icon = "Home", label = "Home", active = true)
        BottomNavItem(icon = "EmojiEvents", label = "Awards", active = false)
        BottomNavItem(icon = "Menu", label = "Menu", active = false)
    }
}

@Composable
fun BottomNavItem(icon: String, label: String, active: Boolean) {
    val contentColor = if (active) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.4f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable { /* decorative item tap */ }
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (active) Color(0xFFFBBF24).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                .border(
                    width = 1.dp,
                    color = if (active) Color(0xFFFBBF24).copy(alpha = 0.5f) else Color.Transparent,
                    shape = RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            val vector = when (icon) {
                "Home" -> Icons.Filled.Home
                "EmojiEvents" -> Icons.Filled.EmojiEvents
                "Group" -> Icons.Filled.Person
                else -> Icons.Filled.Palette
            }
            Icon(
                imageVector = vector,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label.uppercase(),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
fun MainGameScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1E1B4B)
    ) {
        when (gameState) {
            GameMode.LOBBY -> MainLobbyScreen(viewModel)
            GameMode.OBSTACLE_RACE -> GameArenaScreen(viewModel, GameMode.OBSTACLE_RACE)
            GameMode.SURVIVAL_HEXAGON -> GameArenaScreen(viewModel, GameMode.SURVIVAL_HEXAGON)
            GameMode.TEAM_TAIL_TAG -> GameArenaScreen(viewModel, GameMode.TEAM_TAIL_TAG)
            GameMode.WINNER_SCREEN -> WinnerConfettiScreen(viewModel)
        }
    }
}

// LANDSCAPE Side-by-Side Customizer and Main Menu Screen
@Composable
fun MainLobbyScreen(viewModel: GameViewModel) {
    val username by viewModel.username.collectAsState()
    val colorIdx by viewModel.colorIndex.collectAsState()
    val currentHat by viewModel.selectedHat.collectAsState()
    val currentOutfit by viewModel.selectedOutfit.collectAsState()
    val matchHistory by viewModel.matchHistoryFlow.collectAsState()
    val playerProfile by viewModel.playerProfileFlow.collectAsState()

    val isOffline by viewModel.isOfflineMode.collectAsState()
    val botDiff by viewModel.botDifficulty.collectAsState()
    val isMatchmaking by viewModel.isMatchmaking.collectAsState()
    val matchmakingProgress by viewModel.matchmakingProgress.collectAsState()
    val matchmakingStatusText by viewModel.matchmakingStatusText.collectAsState()

    var showLeaderboard by remember { mutableStateOf(false) }

    val hats = listOf("None", "Crown", "Wizard", "Pirate", "Chef", "Cowboy")
    val outfits = listOf("None", "Ninja", "Astronaut", "Superhero", "Dino", "Tuxedo", "Casual")

    // Infinite breathing oscillation for lobby preview
    val infiniteTransition = rememberInfiniteTransition(label = "lobby")
    val previewWobble by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wobble"
    )

    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .sleekAmbientBackground()
    ) {
        if (showLeaderboard) {
            // High Scores Overlay
            LeaderboardOverlay(
                matchHistory = matchHistory,
                wins = playerProfile?.wins ?: 0,
                points = playerProfile?.points ?: 0,
                onDismiss = { showLeaderboard = false }
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Navigation / Status Bar (Sleek Interface)
                SleekTopStatusBar(
                    username = username,
                    wins = playerProfile?.wins ?: 0
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT COLUMN: Title, Name, Join Button, Stats (Scrollable for compact screens)
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(start = 0.dp, top = 2.dp, end = 8.dp, bottom = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
                    ) {
                        Column {
                            Text(
                                text = "STUMBLE PARTY",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "Jelly Capsule Minigames",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.5f),
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Compact Username Input Row (Glassmorphism design)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Edit Username",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            BasicTextField(
                                value = username,
                                onValueChange = { viewModel.setUsername(it) },
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                cursorBrush = SolidColor(Color(0xFF6366F1)),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                decorationBox = { innerTextField ->
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        if (username.isEmpty()) {
                                            Text(
                                                text = "ENTER USERNAME...",
                                                color = Color.White.copy(alpha = 0.35f),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        // Mode & Bot Selector Row side-by-side to save huge vertical space
                        Row(
                            modifier = Modifier.fillMaxWidth(0.95f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mode Selector
                            Row(
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(1.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isOffline) Color(0xFF6366F1) else Color.Transparent)
                                        .clickable {
                                            focusManager.clearFocus()
                                            viewModel.setOfflineMode(true)
                                        }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.WifiOff,
                                            contentDescription = "Offline Mode",
                                            tint = if (isOffline) Color.White else Color.White.copy(alpha = 0.5f),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "OFFLINE",
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isOffline) Color.White else Color.White.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (!isOffline) Color(0xFF6366F1) else Color.Transparent)
                                        .clickable {
                                            focusManager.clearFocus()
                                            viewModel.setOfflineMode(false)
                                        }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Wifi,
                                            contentDescription = "Online Simulated",
                                            tint = if (!isOffline) Color.White else Color.White.copy(alpha = 0.5f),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "ONLINE",
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (!isOffline) Color.White else Color.White.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }

                            // Bot Difficulty Selector Segment
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "BOTS:",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White.copy(alpha = 0.5f),
                                    letterSpacing = 0.5.sp
                                )
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.25f))
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                        .padding(1.dp),
                                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    listOf("Easy", "Normal", "Hard").forEach { diff ->
                                        val isSelected = botDiff == diff
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) Color(0xFFFBBF24) else Color.Transparent)
                                                .clickable {
                                                    focusManager.clearFocus()
                                                    viewModel.setBotDifficulty(diff)
                                                }
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = diff.uppercase(),
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Simulated lobby status pill
                        Box(modifier = Modifier.padding(vertical = 1.dp)) {
                            SleekLobbyStatusPill(viewModel)
                        }

                        // Big Play Match & Leaderboard side-by-side (Saves huge vertical space)
                        Row(
                            modifier = Modifier.fillMaxWidth(0.95f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.startPlayMatch()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.8f)
                                    .height(44.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0xFFFCD34D), Color(0xFFF97316)) // amber-300 to orange-500
                                        ),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(1.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .testTag("play_button")
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Play", modifier = Modifier.size(20.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "PLAY MATCH",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp,
                                    style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                )
                            }

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    showLeaderboard = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)), // Sleek Indigo
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(44.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .testTag("scores_button")
                            ) {
                                Icon(Icons.Filled.Leaderboard, contentDescription = "Leaderboard", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SCORES", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }

                        // Game selection categories label
                        Text(
                            text = "Obstacle Race • Survival • Team Tag",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.35f),
                            letterSpacing = 1.2.sp
                        )
                    }

                    // RIGHT COLUMN: 3D character preview and item carousels (Scrollable for compact screens)
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(start = 8.dp, top = 4.dp, end = 0.dp, bottom = 4.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Pedestal + Character preview Canvas (Enlarged with double-size crisp character)
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .padding(bottom = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw Pedestal cylinder
                                drawOval(
                                    color = Color(0x3F000000),
                                    topLeft = Offset(10f, size.height - 25f),
                                    size = Size(size.width - 20f, 20f)
                                )
                                drawRoundRect(
                                    color = ACCENT_PINK,
                                    topLeft = Offset(15f, size.height - 40f),
                                    size = Size(size.width - 30f, 20f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                                )
                                drawOval(
                                    color = Color(0xFFFFA000),
                                    topLeft = Offset(15f, size.height - 45f),
                                    size = Size(size.width - 30f, 10f)
                                )

                                // Render preview capsule (with DOUBLE radius for exceptional clarity!)
                                val testPlayer = GamePlayer(
                                    id = "preview",
                                    name = username,
                                    colorIndex = colorIdx,
                                    hat = currentHat,
                                    outfit = currentOutfit,
                                    wobblePhase = previewWobble,
                                    vx = 0f,
                                    vy = 0f
                                )
                                val rSize = 56f // Doubled from 28f to be perfectly visible and clear!
                                CharacterRenderer.drawCharacter(
                                    scope = this,
                                    player = testPlayer,
                                    px = size.width / 2f,
                                    py = size.height - 40f - rSize, // pivot based on feet standing on top pedestal surface
                                    radius = rSize
                                )
                            }
                        }

                        // Customizer selectors inside clear, space-efficient glassmorphic card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. Skin Color Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Palette, contentDescription = "Color", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("SKIN:", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.horizontalScroll(rememberScrollState())
                                    ) {
                                        CharacterRenderer.SKINS.forEachIndexed { idx, col ->
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(col)
                                                    .border(
                                                        width = if (colorIdx == idx) 2.5.dp else 1.dp,
                                                        color = if (colorIdx == idx) Color.White else Color.White.copy(alpha = 0.15f),
                                                        shape = CircleShape
                                                    )
                                                    .clickable { viewModel.selectColor(idx) }
                                            )
                                        }
                                    }
                                }

                                // 2. Costume Hat Selector (Premium horizontal scroll)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Person, contentDescription = "Hat", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("HAT:", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.horizontalScroll(rememberScrollState())
                                    ) {
                                        hats.forEach { hat ->
                                            val isSelected = currentHat == hat
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) ACCENT_PINK else Color.White.copy(alpha = 0.08f))
                                                    .border(1.dp, if (isSelected) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                                    .clickable { viewModel.selectHat(hat) }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(hat.uppercase(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                }

                                // 3. Outfit Selector (Premium horizontal scroll)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Palette, contentDescription = "Outfit", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("OUTFIT:", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.horizontalScroll(rememberScrollState())
                                    ) {
                                        outfits.forEach { outfit ->
                                            val isSelected = currentOutfit == outfit
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) ACCENT_PINK else Color.White.copy(alpha = 0.08f))
                                                    .border(1.dp, if (isSelected) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                                    .clickable { viewModel.selectOutfit(outfit) }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(outfit.uppercase(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Bottom Bar navigation buttons mimicking the Sleek mockup layout
                SleekBottomNavigationBar()
            }
        }

        // Matchmaking Queue Overlay
        if (isMatchmaking) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.82f))
                    .clickable(enabled = false) {} // block clickthrough
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B).copy(alpha = 0.92f)),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .width(420.dp)
                        .border(1.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Spinning indicator
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(64.dp)
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                progress = { matchmakingProgress },
                                color = Color(0xFFFBBF24),
                                strokeWidth = 5.dp,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                            Icon(
                                imageVector = Icons.Filled.Language,
                                contentDescription = "Matchmaking",
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = "MATCHMAKING QUEUE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = matchmakingStatusText.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f),
                            letterSpacing = 0.5.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        // Progress Indicator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(matchmakingProgress)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF6366F1), Color(0xFFEC4899))
                                        )
                                    )
                            )
                        }

                        Text(
                            text = "🎮 CONNECTED TO STUMBLE-NET REGION",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

// Full-screen Immersive Canvas-based Arena for the Active Minigames
@Composable
fun GameArenaScreen(viewModel: GameViewModel, mode: GameMode) {
    val players by viewModel.players.collectAsState()
    val obstacles by viewModel.obstacles.collectAsState()
    val hexTiles by viewModel.hexTiles.collectAsState()
    val timer by viewModel.roundTimer.collectAsState()
    val qCount by viewModel.qualifiedCount.collectAsState()
    val targetQ by viewModel.targetQualifiedCount.collectAsState()
    val activeRound by viewModel.activeRoundIndex.collectAsState()
    val isOffline by viewModel.isOfflineMode.collectAsState()
    val botDiff by viewModel.botDifficulty.collectAsState()

    // Smooth local interpolation for camera scrolling on side-scrolling races
    var localCameraX by remember { mutableStateOf(0f) }

    LaunchedEffect(players) {
        val localP = players.firstOrNull { it.id == "local_player" }
        if (localP != null) {
            // Camera centers player horizontally, with left/right clamp
            val targetCamX = localP.x - 400f
            localCameraX = localCameraX + (targetCamX - localCameraX) * 0.15f // smooth interpolate
            if (localCameraX < 0) localCameraX = 0f
            if (localCameraX > 2200f) localCameraX = 2200f
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main full-bleed Game Canvas Drawing
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val mapW = 3000f
            val mapH = size.height

            when (mode) {
                GameMode.OBSTACLE_RACE -> {
                    // Draw scrolling side-scroller obstacle course background
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF03A9F4), Color(0xFFE1F5FE))
                        )
                    )

                    // Side-scroller viewport coordinate transform
                    withTransform({
                        translate(left = -localCameraX, top = 0f)
                    }) {
                        // Draw clouds or scenery decoration
                        drawCircle(Color.White.copy(alpha = 0.6f), 80f, Offset(200f, 100f))
                        drawCircle(Color.White.copy(alpha = 0.6f), 100f, Offset(260f, 110f))
                        drawCircle(Color.White.copy(alpha = 0.6f), 80f, Offset(1200f, 120f))
                        drawCircle(Color.White.copy(alpha = 0.6f), 90f, Offset(1800f, 100f))

                        // Draw Ground base floor line
                        val groundY = mapH - 100f
                        drawRect(
                            color = Color(0xFF263238),
                            topLeft = Offset(0f, groundY),
                            size = Size(mapW, mapH - groundY)
                        )
                        // Decorative neon grid top-edge
                        drawRect(
                            color = Color(0xFF00E676),
                            topLeft = Offset(0f, groundY),
                            size = Size(mapW, 10f)
                        )

                        // Draw dynamic obstacles
                        obstacles.forEach { obs ->
                            when (obs.type) {
                                ObstacleType.MOVING_PLATFORM -> {
                                    drawRoundRect(
                                        color = Color(0xFF90A4AE),
                                        topLeft = Offset(obs.x, obs.y),
                                        size = Size(obs.width, obs.height),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                                        )
                                    // Highlight neon orange strip
                                    drawRect(
                                        color = Color(0xFFFF5722),
                                        topLeft = Offset(obs.x + 10f, obs.y),
                                        size = Size(obs.width - 20f, 6f)
                                    )
                                }
                                ObstacleType.SPINNING_BAR -> {
                                    // Draw pivot center
                                    drawCircle(Color(0xFF37474F), 18f, Offset(obs.x, obs.y))
                                    // Draw hazard warning pattern
                                    val barW = obs.width
                                    val barH = obs.height
                                    rotate(obs.angle * 180f / Math.PI.toFloat(), pivot = Offset(obs.x, obs.y)) {
                                        drawRect(
                                            color = Color(0xFFD50000),
                                            topLeft = Offset(obs.x, obs.y - barH / 2f),
                                            size = Size(barW, barH)
                                        )
                                        // Hazard yellow stripes
                                        for (i in 1..4) {
                                            drawRect(
                                                color = Color(0xFFFFEB3B),
                                                topLeft = Offset(obs.x + i * (barW / 5f), obs.y - barH / 2f),
                                                size = Size(12f, barH)
                                            )
                                        }
                                    }
                                }
                                ObstacleType.BOUNCY_PAD -> {
                                    // Spring coil
                                    drawRect(
                                        color = Color(0xFFCFD8DC),
                                        topLeft = Offset(obs.x + 10f, obs.y + 10f),
                                        size = Size(obs.width - 20f, obs.height - 10f)
                                    )
                                    // Red bouncy cushion top
                                    drawRoundRect(
                                        color = Color(0xFFFF1744),
                                        topLeft = Offset(obs.x, obs.y),
                                        size = Size(obs.width, 14f),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                                    )
                                }
                                ObstacleType.FALLING_BLOCK -> {
                                    // Heavy iron crusher blocks
                                    drawRect(
                                        color = Color(0xFF455A64),
                                        topLeft = Offset(obs.x, obs.y),
                                        size = Size(obs.width, obs.height)
                                    )
                                    drawRect(
                                        color = Color(0xFF263238),
                                        topLeft = Offset(obs.x + 10f, obs.y + 10f),
                                        size = Size(obs.width - 20f, obs.height - 20f)
                                    )
                                    // Angry spikes or hazard label
                                    drawCircle(Color(0xFFFF1744), 16f, Offset(obs.x + obs.width / 2f, obs.y + obs.height - 30f))
                                }
                                ObstacleType.SPEED_PAD -> {
                                    // Neon green speed arrows
                                    drawRect(
                                        color = Color(0xFF00C853),
                                        topLeft = Offset(obs.x, obs.y),
                                        size = Size(obs.width, obs.height)
                                    )
                                    // Arrow lines pointing right
                                    for (i in 0..2) {
                                        val offset = i * 25f
                                        drawRect(
                                            color = Color.White,
                                            topLeft = Offset(obs.x + 10f + offset, obs.y + 2f),
                                            size = Size(6f, obs.height - 4f)
                                        )
                                    }
                                }
                                ObstacleType.STATIC_BOX -> {
                                    drawRoundRect(
                                        color = Color(0xFF78909C),
                                        topLeft = Offset(obs.x, obs.y),
                                        size = Size(obs.width, obs.height),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                                    )
                                }
                                ObstacleType.FINISH_LINE -> {
                                    // Checkerboard pattern block
                                    val sq = 25f
                                    for (i in 0 until (obs.width / sq).toInt()) {
                                        for (j in 0 until (obs.height / sq).toInt()) {
                                            val isBlack = (i + j) % 2 == 0
                                            drawRect(
                                                color = if (isBlack) Color.Black else Color.White,
                                                topLeft = Offset(obs.x + i * sq, obs.y + j * sq),
                                                size = Size(sq, sq)
                                            )
                                        }
                                    }
                                    // Glowing blue finish gate arch
                                    drawRect(
                                        color = Color(0xFF2979FF),
                                        topLeft = Offset(obs.x, obs.y),
                                        size = Size(8f, obs.height)
                                    )
                                }
                            }
                        }

                        // Draw players
                        players.forEach { p ->
                            if (p.isEliminated) return@forEach
                            CharacterRenderer.drawCharacter(
                                scope = this,
                                player = p,
                                px = p.x,
                                py = p.y,
                                radius = p.radius
                            )
                            // Draw floating player username
                            drawFloatingName(this, p.name, p.x, p.y - p.radius * 2f, if (p.id == "local_player") ACCENT_ORANGE else Color.White)
                        }
                    }
                }

                GameMode.SURVIVAL_HEXAGON -> {
                    // Lava magma background
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF3E2723), Color(0xFFBF360C))
                        )
                    )

                    // Draw Hexagonal tile grid platform
                    hexTiles.forEach { tile ->
                        if (tile.state == TileState.FALLEN) return@forEach

                        val tileColor = when (tile.state) {
                            TileState.STABLE -> Color(0xFF00E676)   // Neon Green
                            TileState.WARNING -> Color(0xFFFFD600)  // Golden Yellow
                            TileState.CRITICAL -> Color(0xFFFF1744) // Neon Red
                            else -> Color.Transparent
                        }

                        // Draw 3D-shaded flat hex tile using simple circle polygons
                        val sides = 6
                        val path = Path()
                        for (i in 0 until sides) {
                            val angle = i * (2f * Math.PI.toFloat() / sides)
                            val tx = tile.centerX + cos(angle) * tile.radius
                            val ty = tile.centerY + sin(angle) * tile.radius
                            if (i == 0) path.moveTo(tx, ty) else path.lineTo(tx, ty)
                        }
                        path.close()
                        
                        // Draw hex bottom block depth
                        drawPath(path = path, color = tileColor.copy(alpha = 0.5f))
                        // Draw hex main surface slightly shifted up for isometric projection look!
                        val shiftPath = Path()
                        for (i in 0 until sides) {
                            val angle = i * (2f * Math.PI.toFloat() / sides)
                            val tx = tile.centerX + cos(angle) * (tile.radius - 3f)
                            val ty = tile.centerY + sin(angle) * (tile.radius - 3f) - 6f
                            if (i == 0) shiftPath.moveTo(tx, ty) else shiftPath.lineTo(tx, ty)
                        }
                        shiftPath.close()

                        drawPath(path = shiftPath, color = tileColor)
                        drawPath(path = shiftPath, color = Color.White.copy(alpha = 0.2f), style = Stroke(width = 3f))
                    }

                    // Draw Lava heat rings
                    drawCircle(
                        color = Color(0xFFFF5722).copy(alpha = 0.3f),
                        radius = 320f,
                        center = Offset(400f, 300f),
                        style = Stroke(width = 10f)
                    )

                    // Draw players
                    players.forEach { p ->
                        if (p.isEliminated) return@forEach
                        CharacterRenderer.drawCharacter(
                            scope = this,
                            player = p,
                            px = p.x,
                            py = p.y,
                            radius = p.radius
                        )
                        drawFloatingName(this, p.name, p.x, p.y - p.radius * 2f, if (p.id == "local_player") ACCENT_ORANGE else Color.White)
                    }
                }

                GameMode.TEAM_TAIL_TAG -> {
                    // Cyber athletic neon floor
                    drawRect(Color(0xFF1E1E2F))

                    // Draw outer border fence circle
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = 350f,
                        center = Offset(400f, 300f),
                        style = Stroke(width = 8f)
                    )
                    // Grid lines in arena
                    drawLine(Color(0x22FFFFFF), Offset(50f, 300f), Offset(750f, 300f), strokeWidth = 3f)
                    drawLine(Color(0x22FFFFFF), Offset(400f, 50f), Offset(400f, 550f), strokeWidth = 3f)

                    // Draw team markers inside circle
                    drawCircle(Color(0x22FF1744), radius = 80f, center = Offset(250f, 300f)) // Red home base
                    drawCircle(Color(0x222979FF), radius = 80f, center = Offset(550f, 300f)) // Blue home base

                    // Draw players with team colors glowing underneath
                    players.forEach { p ->
                        if (p.isEliminated) return@forEach
                        val glowCol = if (p.team == Team.RED) Color(0xFFFF1744) else Color(0xFF2979FF)
                        
                        // Glow pedestal indicator
                        drawOval(
                            color = glowCol.copy(alpha = 0.4f),
                            topLeft = Offset(p.x - p.radius, p.y + p.radius * 0.4f),
                            size = Size(p.radius * 2f, p.radius * 0.6f)
                        )

                        CharacterRenderer.drawCharacter(
                            scope = this,
                            player = p,
                            px = p.x,
                            py = p.y,
                            radius = p.radius
                        )
                        drawFloatingName(this, p.name, p.x, p.y - p.radius * 2.2f, if (p.id == "local_player") ACCENT_ORANGE else Color.White)
                    }
                }
                else -> {}
            }
        }

        // HUD OVERLAYS (top row timers and counters)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left Side: Mode Pill & Round Number
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.exitToMain() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .size(44.dp)
                ) {
                    Icon(Icons.Filled.Home, contentDescription = "Home", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "ROUND $activeRound: ${mode.name.replace("_", " ")}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOffline) Color(0xFFFF9800).copy(alpha = 0.85f) else Color(0xFF4CAF50).copy(alpha = 0.85f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isOffline) "🔌 BOTS: $botDiff" else "🌐 LIVE",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Center Top: Round Timer Progress
            Card(
                colors = CardDefaults.cardColors(containerColor = ACCENT_ORANGE),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(width = 120.dp, height = 48.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "$timer s",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Top Right: Target Qualifiers badge
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "QUALIFIED: $qCount / $targetQ",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // CONTROLS OVERLAYS (Landscape Virtual Joystick & Jump buttons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // LEFT SIDE: Touch Virtual Joystick
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(3.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            ) {
                var dragOffset by remember { mutableStateOf(Offset.Zero) }
                val maxDragRadius = 45f

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .offset {
                            IntOffset(
                                (dragOffset.x).toInt(),
                                (dragOffset.y).toInt()
                            )
                        }
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(ACCENT_ORANGE)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    val newOffset = dragOffset + dragAmount
                                    val distance = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                                    if (distance <= maxDragRadius) {
                                        dragOffset = newOffset
                                    } else {
                                        val ratio = maxDragRadius / distance
                                        dragOffset = Offset(newOffset.x * ratio, newOffset.y * ratio)
                                    }
                                    
                                    // Set joystick angles in ViewModel based on radians
                                    val angle = atan2(dragOffset.y, dragOffset.x)
                                    viewModel.keyboardJoystickAngles["local_player"] = angle
                                },
                                onDragEnd = {
                                    dragOffset = Offset.Zero
                                    viewModel.keyboardJoystickAngles["local_player"] = null
                                }
                            )
                        }
                )
            }

            // RIGHT SIDE: Large Action and Jump Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Pink Action / Grab Button (for Team Tail Tag)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(ACCENT_PINK)
                        .clickable {
                            // Touch trigger action
                            viewModel.keyboardActionPressed["local_player"] = true
                            // Auto-release after a brief delay
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                viewModel.keyboardActionPressed["local_player"] = false
                            }, 150)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("GRAB", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Orange Jump Button
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(ACCENT_ORANGE)
                        .clickable {
                            viewModel.keyboardJumpPressed["local_player"] = true
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                viewModel.keyboardJumpPressed["local_player"] = false
                            }, 150)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("JUMP", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }
}

// Float custom graphic names above characters
fun drawFloatingName(scope: androidx.compose.ui.graphics.drawscope.DrawScope, text: String, x: Float, y: Float, color: Color) {
    val canvas = scope.drawContext.canvas.nativeCanvas as android.graphics.Canvas
    val paint = android.graphics.Paint().apply {
        this.textSize = 22f
        this.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        this.textAlign = android.graphics.Paint.Align.CENTER
    }
    
    // Measure text width to size the background capsule perfectly
    val textWidth = paint.measureText(text)
    val paddingX = 12f
    val paddingY = 8f
    
    // Draw background rounded capsule
    val bgPaint = android.graphics.Paint().apply {
        this.color = android.graphics.Color.argb(140, 0, 0, 0) // semi-transparent black
        this.style = android.graphics.Paint.Style.FILL
        this.isAntiAlias = true
    }
    
    val rectLeft = x - textWidth / 2f - paddingX
    val rectRight = x + textWidth / 2f + paddingX
    val rectTop = y - 20f - paddingY
    val rectBottom = y + 6f + paddingY
    
    canvas.drawRoundRect(rectLeft, rectTop, rectRight, rectBottom, 8f, 8f, bgPaint)
    
    // Draw text with the correct custom color
    paint.color = color.toArgb()
    paint.isAntiAlias = true
    canvas.drawText(text, x, y, paint)
}

// Confetti floating victory screen
@Composable
fun WinnerConfettiScreen(viewModel: GameViewModel) {
    val winnerName by viewModel.matchWinnerName.collectAsState()
    val scoreEarned by viewModel.matchPointsEarned.collectAsState()
    val isLocalPlayerWinner by viewModel.isLocalPlayerWinner.collectAsState()
    val activeRound by viewModel.activeRoundIndex.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "victory")
    val confettiRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val DEFEAT_GRADIENT = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF1E293B), // Slate 800
                Color(0xFF334155), // Slate 700
                Color(0xFF475569)  // Slate 600
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isLocalPlayerWinner) GRADIENT_THEME else DEFEAT_GRADIENT),
        contentAlignment = Alignment.Center
    ) {
        // Confetti visual background drawing (Only show for wins)
        if (isLocalPlayerWinner) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val count = 40
                for (i in 0..count) {
                    val phase = (confettiRotation + i * (360f / count)) % 360f
                    val cx = (i * (size.width / count) + sin(phase * 0.05f) * 60f) % size.width
                    val cy = (phase * (size.height / 360f) * 1.5f + i * 15f) % size.height
                    rotate(phase, pivot = Offset(cx, cy)) {
                        drawRect(
                            color = listOf(Color.Yellow, Color.Cyan, Color.Magenta, Color.Green)[i % 4],
                            topLeft = Offset(cx, cy),
                            size = Size(14f, 22f)
                        )
                    }
                }
            }
        }

        // Winning / Elimination Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = if (isLocalPlayerWinner) Icons.Filled.EmojiEvents else Icons.Filled.SentimentVeryDissatisfied,
                contentDescription = if (isLocalPlayerWinner) "Crown" else "Eliminated",
                tint = if (isLocalPlayerWinner) Color(0xFFFFD54F) else Color(0xFF94A3B8),
                modifier = Modifier.size(96.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isLocalPlayerWinner) "VICTORY CROWN!" else "ELIMINATED!",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = winnerName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLocalPlayerWinner) ACCENT_ORANGE else Color(0xFFFBBF24)
                    )
                    Text(
                        text = if (isLocalPlayerWinner) "Champion of Stumble Party!" else "Winner of Stumble Party",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isLocalPlayerWinner) "+$scoreEarned Points Saved" else "Rounds Survived: ${activeRound - 1}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLocalPlayerWinner) Color.Green else Color(0xFF60A5FA)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.exitToMain() },
                colors = ButtonDefaults.buttonColors(containerColor = ACCENT_ORANGE),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Retry")
                Spacer(modifier = Modifier.width(6.dp))
                Text("PLAY AGAIN", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// High Scores List screen overlay panel
@Composable
fun LeaderboardOverlay(
    matchHistory: List<MatchHistory>,
    wins: Int,
    points: Int,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B283A)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .width(480.dp)
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LEADERBOARD STATS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Close", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats summaries
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("TOTAL WINS", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                            Text("$wins Crowns", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ACCENT_ORANGE)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("TOTAL POINTS", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                            Text("$points Pts", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ACCENT_PINK)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("RECORDS HISTORY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(8.dp))

                if (matchHistory.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No matches played yet. Start stumbling now!", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(matchHistory) { record ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Match #${record.id}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                        Text(record.gameSequence, fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val placementText = if (record.rank == 1) "🏆 Champion" else "Eliminated R${record.rank}"
                                        Text(
                                            text = placementText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (record.rank == 1) Color.Yellow else Color.White
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "+${record.scoreEarned} Pts",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Green
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
