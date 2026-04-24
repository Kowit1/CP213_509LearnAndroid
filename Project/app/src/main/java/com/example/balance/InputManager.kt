package com.example.balance

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * ==========================================================
 * InputManager.kt — Handles gyroscope and touch fallback input
 * ==========================================================
 *
 * GYROSCOPE USAGE:
 * - Registers for TYPE_ACCELEROMETER sensor events
 * - Reads the X-axis acceleration (tilt left/right)
 * - Positive X = device tilted right → block moves right
 * - Negative X = device tilted left → block moves left
 * - We use accelerometer instead of gyroscope because:
 *   1. More devices have accelerometers
 *   2. Accelerometer gives tilt angle directly (gravity component)
 *   3. Gyroscope gives rotation RATE which needs integration
 *
 * TOUCH FALLBACK:
 * - If gyroscope/accelerometer is not available (e.g., emulator),
 *   the system falls back to touch input
 * - Touch drag: dragging left/right moves the block
 * - On-screen buttons: tap left/right arrows to move
 *
 * INPUT SWITCHING:
 * - The system auto-detects if a sensor is available
 * - User can also manually toggle via an on-screen button
 */
class InputManager(private val context: Context) : SensorEventListener {

    // ===== Sensor management =====
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    // ===== Input state =====
    var tiltX: Float = 0f              // Current tilt value (-10 to 10 range)
        private set
    var touchDeltaX: Float = 0f        // Touch drag delta per frame
    var useGyro: Boolean = false       // Whether to use gyro or touch
        private set
    var gyroAvailable: Boolean = false  // Whether device has accelerometer
        private set

    // ===== Touch button state =====
    var leftButtonPressed: Boolean = false
    var rightButtonPressed: Boolean = false

    // ===== Configuration =====
    companion object {
        // TILT_SENSITIVITY is now read from GameSettings.gyroSensitivity
        // so the user can adjust it from the Settings menu
        const val TOUCH_BUTTON_SPEED = 4f   // Speed when using on-screen buttons
        const val TILT_DEAD_ZONE = 0.5f     // Ignore tiny tilts (noise filter)
    }

    /**
     * Initialize the sensor system.
     * Checks if accelerometer is available and registers listener.
     */
    fun initialize() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        gyroAvailable = accelerometer != null
        useGyro = gyroAvailable // Default to gyro if available

        if (gyroAvailable) {
            sensorManager?.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME // ~20ms updates for smooth gameplay
            )
        }
    }

    /**
     * Toggle between gyro and touch input modes.
     */
    fun toggleInputMode() {
        if (gyroAvailable) {
            useGyro = !useGyro
        }
        // If gyro not available, always stay in touch mode
    }

    /**
     * Get the current horizontal input value.
     * Positive = move right, Negative = move left.
     *
     * INPUT PRIORITY:
     * 1. If useGyro → return tilt value (filtered through dead zone)
     * 2. If touch → return touch delta or button input
     */
    fun getHorizontalInput(): Float {
        return if (useGyro) {
            // Apply dead zone to filter sensor noise
            if (kotlin.math.abs(tiltX) < TILT_DEAD_ZONE) {
                0f
            } else {
                // Use sensitivity from settings (adjustable by user)
                tiltX * GameSettings.gyroSensitivity
            }
        } else {
            // Touch input: combine drag and button presses
            var input = touchDeltaX
            if (leftButtonPressed) input -= TOUCH_BUTTON_SPEED
            if (rightButtonPressed) input += TOUCH_BUTTON_SPEED
            input
        }
    }

    // ===== SensorEventListener implementation =====

    /**
     * Called when accelerometer values change.
     *
     * ACCELEROMETER VALUES:
     * - values[0] = X-axis acceleration (left/right tilt)
     * - values[1] = Y-axis acceleration (forward/back tilt)
     * - values[2] = Z-axis acceleration (up/down, ~9.8 when flat)
     *
     * We negate X because Android's accelerometer coordinate system
     * has positive X pointing right, but tilting right gives negative X
     * (gravity pulls the sensor to the left when device tilts right).
     * Actually: tilting the device RIGHT means gravity has a component
     * in the NEGATIVE X direction. We negate to make tilt-right = positive input.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                // Negate X: tilting right → negative sensor X → we want positive game input
                tiltX = -it.values[0]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this game
    }

    /**
     * Clean up sensor listener when game is paused/destroyed.
     */
    fun pause() {
        sensorManager?.unregisterListener(this)
    }

    /**
     * Re-register sensor listener when game resumes.
     */
    fun resume() {
        if (gyroAvailable && useGyro) {
            sensorManager?.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    /**
     * Reset touch input values each frame.
     * Called after input has been consumed.
     */
    fun resetFrameInput() {
        touchDeltaX = 0f
    }
}
