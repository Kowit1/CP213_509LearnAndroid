package com.example.balance

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

/**
 * ==========================================================
 * AudioManager.kt — Handles BGM and SFX playback
 * ==========================================================
 *
 * BGM  → MediaPlayer  (looping background music)
 * SFX  → SoundPool    (short one-shot sound effects)
 *
 * Volume levels are read from GameSettings each time a sound
 * is played, so changes from the Settings menu take effect
 * immediately.
 */
class AudioManager(private val context: Context) {

    // ===== BGM (MediaPlayer) =====
    private var bgmPlayer: MediaPlayer? = null
    private var bgmResId: Int = 0

    // ===== SFX (SoundPool) =====
    private var soundPool: SoundPool? = null
    private var sfxLandId: Int = 0
    private var sfxGameOverId: Int = 0
    private var sfxToppleId: Int = 0
    private var sfxLoaded = false

    /**
     * Initialize the audio system.
     * Call once when the game screen is created.
     */
    fun init() {
        // --- Setup SoundPool for SFX ---
        val audioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttrs)
            .build()
            .also { pool ->
                pool.setOnLoadCompleteListener { _, _, status ->
                    if (status == 0) sfxLoaded = true
                }
                sfxLandId = pool.load(context, R.raw.sfx_land, 1)
                sfxGameOverId = pool.load(context, R.raw.sfx_gameover, 1)
                sfxToppleId = pool.load(context, R.raw.sfx_topple, 1)
            }

        bgmResId = R.raw.bgm_game
    }

    // =================================================================
    // BGM controls
    // =================================================================

    /** Start or resume the background music (loops forever) */
    fun playBGM() {
        try {
            if (bgmPlayer == null) {
                bgmPlayer = MediaPlayer.create(context, bgmResId)?.apply {
                    isLooping = true
                    val vol = GameSettings.bgmVolumeNormalized
                    setVolume(vol, vol)
                    start()
                }
            } else {
                updateBGMVolume()
                bgmPlayer?.takeIf { !it.isPlaying }?.start()
            }
        } catch (_: Exception) {
            // Gracefully handle missing or corrupt audio file
        }
    }

    /** Pause the BGM (e.g. when activity pauses) */
    fun pauseBGM() {
        try {
            bgmPlayer?.takeIf { it.isPlaying }?.pause()
        } catch (_: Exception) {}
    }

    /** Stop and release the BGM player */
    fun stopBGM() {
        try {
            bgmPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {}
        bgmPlayer = null
    }

    /** Update BGM volume (call when settings change or on resume) */
    fun updateBGMVolume() {
        try {
            val vol = GameSettings.bgmVolumeNormalized
            bgmPlayer?.setVolume(vol, vol)
        } catch (_: Exception) {}
    }

    // =================================================================
    // SFX controls
    // =================================================================

    /** Play the block-landing sound */
    fun playSfxLand() {
        playSfx(sfxLandId)
    }

    /** Play the game-over sound */
    fun playSfxGameOver() {
        playSfx(sfxGameOverId)
    }

    /** Play the tower-toppling sound */
    fun playSfxTopple() {
        playSfx(sfxToppleId)
    }

    private fun playSfx(soundId: Int) {
        if (!sfxLoaded || soundId == 0) return
        try {
            val vol = GameSettings.sfxVolumeNormalized
            soundPool?.play(soundId, vol, vol, 1, 0, 1.0f)
        } catch (_: Exception) {}
    }

    // =================================================================
    // Lifecycle
    // =================================================================

    /** Release all audio resources. Call when the activity is destroyed. */
    fun release() {
        stopBGM()
        try {
            soundPool?.release()
        } catch (_: Exception) {}
        soundPool = null
        sfxLoaded = false
    }
}
