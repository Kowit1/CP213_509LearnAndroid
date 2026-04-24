package com.example.balance

import android.content.Context
import android.content.SharedPreferences

/**
 * ==========================================================
 * GameSettings.kt — Persisted game settings
 * ==========================================================
 *
 * Stores and retrieves user preferences using SharedPreferences:
 * - SFX Volume (0–100)
 * - BGM Volume (0–100)
 * - Gyroscope Sensitivity (0.5–4.0)
 *
 * All settings are saved immediately when changed and
 * persist across app restarts.
 */
object GameSettings {

    private const val PREFS_NAME = "balance_tower_settings"
    private const val KEY_SFX_VOLUME = "sfx_volume"
    private const val KEY_BGM_VOLUME = "bgm_volume"
    private const val KEY_GYRO_SENSITIVITY = "gyro_sensitivity"

    // Default values
    private const val DEFAULT_SFX = 80
    private const val DEFAULT_BGM = 60
    private const val DEFAULT_SENSITIVITY = 2.0f

    private var prefs: SharedPreferences? = null

    /** Must be called once with a Context before accessing settings */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ===== SFX Volume (0–100) =====
    var sfxVolume: Int
        get() = prefs?.getInt(KEY_SFX_VOLUME, DEFAULT_SFX) ?: DEFAULT_SFX
        set(value) {
            prefs?.edit()?.putInt(KEY_SFX_VOLUME, value.coerceIn(0, 100))?.apply()
        }

    // ===== BGM Volume (0–100) =====
    var bgmVolume: Int
        get() = prefs?.getInt(KEY_BGM_VOLUME, DEFAULT_BGM) ?: DEFAULT_BGM
        set(value) {
            prefs?.edit()?.putInt(KEY_BGM_VOLUME, value.coerceIn(0, 100))?.apply()
        }

    // ===== Gyroscope Sensitivity (0.5–4.0) =====
    var gyroSensitivity: Float
        get() = prefs?.getFloat(KEY_GYRO_SENSITIVITY, DEFAULT_SENSITIVITY) ?: DEFAULT_SENSITIVITY
        set(value) {
            prefs?.edit()?.putFloat(KEY_GYRO_SENSITIVITY, value.coerceIn(0.5f, 4.0f))?.apply()
        }

    /** Normalized SFX volume (0.0–1.0) for audio player */
    val sfxVolumeNormalized: Float get() = sfxVolume / 100f

    /** Normalized BGM volume (0.0–1.0) for audio player */
    val bgmVolumeNormalized: Float get() = bgmVolume / 100f
}
