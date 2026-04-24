package com.example.balance

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.sin
import kotlin.random.Random

/**
 * ==========================================================
 * EventSystem.kt — Manages random game events (Wind & Earthquake)
 * ==========================================================
 *
 * EVENT LOGIC:
 * - Events are triggered randomly every few seconds
 * - Only ONE event can be active at a time
 * - Each event has a limited duration
 *
 * A) WIND EVENT:
 *    - Applies a small constant horizontal force to the falling block
 *    - Random direction (positive = right, negative = left)
 *    - Light intensity so the player can still control the block
 *
 * B) EARTHQUAKE EVENT:
 *    - Shakes the tower left and right using a sine wave oscillation
 *    - The amplitude determines shake intensity
 *    - Triggers device vibration (300-700ms) when earthquake starts
 *    - Uses sine wave: offset = amplitude * sin(frequency * time)
 */

/** Types of events that can occur */
enum class EventType {
    NONE,
    WIND,
    EARTHQUAKE
}

class EventSystem(private val context: Context) {

    // ===== Current event state =====
    var currentEvent: EventType = EventType.NONE
        private set

    // ===== Timing =====
    private var eventTimer: Float = 0f           // Time since last event check
    private var eventDuration: Float = 0f        // How long current event lasts
    private var eventElapsed: Float = 0f         // Time elapsed in current event
    private var cooldownTimer: Float = 0f        // Cooldown between events

    // ===== Wind parameters =====
    var windForce: Float = 0f                    // Horizontal force (px/frame)
        private set

    // ===== Earthquake parameters =====
    private var quakeAmplitude: Float = 0f       // Shake amplitude in pixels
    private var quakeFrequency: Float = 0f       // Oscillation frequency
    var quakeOffset: Float = 0f                  // Current horizontal offset
        private set

    // ===== Configuration =====
    companion object {
        const val MIN_EVENT_INTERVAL = 5f        // Min seconds between events
        const val MAX_EVENT_INTERVAL = 10f       // Max seconds between events
        const val MIN_EVENT_DURATION = 2f        // Min event duration (seconds)
        const val MAX_EVENT_DURATION = 4f        // Max event duration (seconds)
        const val WIND_MIN_FORCE = 2f          // Min wind force
        const val WIND_MAX_FORCE = 6f          // Max wind force
        const val QUAKE_MIN_AMPLITUDE = 3f       // Min earthquake shake
        const val QUAKE_MAX_AMPLITUDE = 8f       // Max earthquake shake
        const val QUAKE_FREQUENCY = 15f          // Oscillation speed
        const val VIBRATION_DURATION_MS = 500L   // Vibration length
    }

    init {
        // Set initial cooldown before first event
        cooldownTimer = Random.nextFloat() * (MAX_EVENT_INTERVAL - MIN_EVENT_INTERVAL) + MIN_EVENT_INTERVAL
    }

    /**
     * Update the event system each frame.
     *
     * @param deltaTime Time elapsed since last frame (in seconds)
     * @return The display text for the current event ("WIND", "EARTHQUAKE", or "")
     */
    fun update(deltaTime: Float): String {
        if (currentEvent == EventType.NONE) {
            // ===== No active event: count down cooldown =====
            cooldownTimer -= deltaTime
            if (cooldownTimer <= 0f) {
                triggerRandomEvent()
            }
            return ""
        } else {
            // ===== Active event: update and check expiration =====
            eventElapsed += deltaTime

            if (eventElapsed >= eventDuration) {
                // Event expired
                endEvent()
                return ""
            }

            // Update event-specific effects
            when (currentEvent) {
                EventType.WIND -> {
                    // Wind force is constant during the event (already set)
                    return "💨 WIND"
                }
                EventType.EARTHQUAKE -> {
                    // Earthquake: compute sine-wave oscillation offset
                    // offset = amplitude * sin(frequency * elapsed_time)
                    quakeOffset = quakeAmplitude * sin(quakeFrequency * eventElapsed)
                    return "🌍 EARTHQUAKE"
                }
                else -> return ""
            }
        }
    }

    /**
     * Trigger a random event (Wind or Earthquake).
     * Called when the cooldown timer expires.
     */
    private fun triggerRandomEvent() {
        // Randomly choose Wind or Earthquake
        currentEvent = if (Random.nextBoolean()) EventType.WIND else EventType.EARTHQUAKE

        // Set random duration
        eventDuration = Random.nextFloat() * (MAX_EVENT_DURATION - MIN_EVENT_DURATION) + MIN_EVENT_DURATION
        eventElapsed = 0f

        when (currentEvent) {
            EventType.WIND -> {
                // Random wind force and direction
                val force = Random.nextFloat() * (WIND_MAX_FORCE - WIND_MIN_FORCE) + WIND_MIN_FORCE
                windForce = if (Random.nextBoolean()) force else -force
            }
            EventType.EARTHQUAKE -> {
                // Random earthquake intensity
                quakeAmplitude = Random.nextFloat() * (QUAKE_MAX_AMPLITUDE - QUAKE_MIN_AMPLITUDE) + QUAKE_MIN_AMPLITUDE
                quakeFrequency = QUAKE_FREQUENCY
                quakeOffset = 0f

                // Trigger device vibration!
                triggerVibration()
            }
            else -> {}
        }
    }

    /**
     * End the current event and reset cooldown.
     */
    private fun endEvent() {
        currentEvent = EventType.NONE
        windForce = 0f
        quakeOffset = 0f
        quakeAmplitude = 0f

        // Set new cooldown for next event
        cooldownTimer = Random.nextFloat() * (MAX_EVENT_INTERVAL - MIN_EVENT_INTERVAL) + MIN_EVENT_INTERVAL
    }

    /**
     * VIBRATION (Android):
     * - Uses VibrationEffect on API 26+ for precise control
     * - Falls back to deprecated vibrate() on older devices
     * - Duration: 300-700ms as specified
     */
    private fun triggerVibration() {
        val duration = Random.nextLong(300, 701) // 300-700ms

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ uses VibratorManager
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                // API 26-30: use Vibrator service directly
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (e: Exception) {
            // Vibration may not be available on all devices/emulators
            e.printStackTrace()
        }
    }

    /** Reset the event system for a new game */
    fun reset() {
        currentEvent = EventType.NONE
        windForce = 0f
        quakeOffset = 0f
        quakeAmplitude = 0f
        eventElapsed = 0f
        cooldownTimer = Random.nextFloat() * (MAX_EVENT_INTERVAL - MIN_EVENT_INTERVAL) + MIN_EVENT_INTERVAL
    }
}
