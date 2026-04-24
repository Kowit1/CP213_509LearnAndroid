package com.example.balance

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager

/**
 * ==========================================================
 * MainActivity.kt — Entry point for the Balance Tower game
 * ==========================================================
 *
 * This activity:
 * 1. Sets up fullscreen mode (keeps screen on)
 * 2. Creates and displays the GameView
 * 3. Manages activity lifecycle (pause/resume the game)
 *
 * The game runs entirely inside GameView — no XML layouts needed.
 * We use a custom SurfaceView for all rendering via Canvas.
 */
class MainActivity : Activity() {

    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep the screen on during gameplay
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize settings (needed for gyro sensitivity etc.)
        GameSettings.init(this)

        // Create and set the game view as the content
        gameView = GameView(this)
        gameView.onBackToMenu = { finish() }
        setContentView(gameView)
    }

    /**
     * Pause the game when the activity goes to background.
     * This stops the game thread and unregisters sensors.
     */
    override fun onPause() {
        super.onPause()
        gameView.pause()
    }

    /**
     * Resume the game when the activity comes back.
     * This restarts the game thread and re-registers sensors.
     */
    override fun onResume() {
        super.onResume()
        gameView.resume()
    }

    /**
     * Ensure the game thread is stopped when the activity is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        gameView.pause()
        gameView.releaseResources()
    }
}