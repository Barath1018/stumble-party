package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.game.GameViewModel
import com.example.ui.screens.MainGameScreen
import com.example.ui.theme.MyApplicationTheme
import kotlin.math.atan2

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enforce landscape orientation for a widescreen mobile party game experience
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        enableEdgeToEdge()
        
        // Hide system bars (status bar for time/notifications, navigation bar) to make the app fully immersive fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        
        // Layout behind cutouts/notch as well for true fullscreen
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        setContent {
            MyApplicationTheme {
                val gameViewModel: GameViewModel = viewModel()
                val focusRequester = remember { FocusRequester() }

                // Tracking keyboard state for desktop play/test support
                val activeKeys = remember { mutableSetOf<Key>() }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester)
                        .focusable()
                        .onKeyEvent { event ->
                            val key = event.key
                            val isPressed = event.type == KeyEventType.KeyDown
                            
                            if (isPressed) {
                                activeKeys.add(key)
                            } else {
                                activeKeys.remove(key)
                            }

                            // Calculate joystick movement based on keyboard keys
                            var moveX = 0f
                            var moveY = 0f

                            if (activeKeys.contains(Key.W) || activeKeys.contains(Key.DirectionUp)) moveY -= 1f
                            if (activeKeys.contains(Key.S) || activeKeys.contains(Key.DirectionDown)) moveY += 1f
                            if (activeKeys.contains(Key.A) || activeKeys.contains(Key.DirectionLeft)) moveX -= 1f
                            if (activeKeys.contains(Key.D) || activeKeys.contains(Key.DirectionRight)) moveX += 1f

                            if (moveX != 0f || moveY != 0f) {
                                val angle = atan2(moveY, moveX)
                                gameViewModel.keyboardJoystickAngles["local_player"] = angle
                            } else {
                                gameViewModel.keyboardJoystickAngles["local_player"] = null
                            }

                            // Handle jumping (Space Bar)
                            if (key == Key.Spacebar) {
                                gameViewModel.keyboardJumpPressed["local_player"] = isPressed
                            }

                            // Handle tail grab / action (Shift Key)
                            if (key == Key.ShiftLeft || key == Key.ShiftRight) {
                                gameViewModel.keyboardActionPressed["local_player"] = isPressed
                            }

                            true
                        }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        MainGameScreen(viewModel = gameViewModel)
                    }
                    
                    // Request keyboard focus immediately on launch
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                }
            }
        }
    }
}

