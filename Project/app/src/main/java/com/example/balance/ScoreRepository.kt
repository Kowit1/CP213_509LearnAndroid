package com.example.balance

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * ==========================================================
 * ScoreRepository.kt — Persistent high-score storage
 * ==========================================================
 *
 * Stores up to 20 top scores using SharedPreferences with
 * JSON serialization. Each entry records:
 *   - Player name
 *   - Score (number of blocks stacked)
 *   - Timestamp (when the score was achieved)
 *
 * Scores are sorted descending (highest first) and trimmed
 * to MAX_ENTRIES after each insertion.
 */
object ScoreRepository {

    private const val PREFS_NAME = "balance_tower_scores"
    private const val KEY_SCORES = "scores_json"
    private const val MAX_ENTRIES = 20

    private var prefs: SharedPreferences? = null

    /** Must be called once with a Context before use */
    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * Save a new score entry.
     * The list is re-sorted and trimmed to [MAX_ENTRIES].
     */
    fun saveScore(playerName: String, score: Int) {
        val entries = loadScores().toMutableList()
        entries.add(
            ScoreEntry(
                playerName = playerName.ifBlank { "Player" },
                score = score,
                timestamp = System.currentTimeMillis()
            )
        )
        // Sort descending by score, then by most recent
        entries.sortWith(compareByDescending<ScoreEntry> { it.score }
            .thenByDescending { it.timestamp })

        // Keep only the top entries
        val trimmed = entries.take(MAX_ENTRIES)
        persistScores(trimmed)
    }

    /**
     * Load all saved scores, sorted descending.
     */
    fun loadScores(): List<ScoreEntry> {
        val json = prefs?.getString(KEY_SCORES, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                ScoreEntry(
                    playerName = obj.optString("name", "Player"),
                    score = obj.optInt("score", 0),
                    timestamp = obj.optLong("timestamp", 0L)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Check if a score qualifies for the leaderboard.
     */
    fun isHighScore(score: Int): Boolean {
        val entries = loadScores()
        if (entries.size < MAX_ENTRIES) return true
        return score > (entries.lastOrNull()?.score ?: 0)
    }

    /**
     * Clear all stored scores.
     */
    fun clearScores() {
        prefs?.edit()?.remove(KEY_SCORES)?.apply()
    }

    private fun persistScores(entries: List<ScoreEntry>) {
        val array = JSONArray()
        for (entry in entries) {
            val obj = JSONObject().apply {
                put("name", entry.playerName)
                put("score", entry.score)
                put("timestamp", entry.timestamp)
            }
            array.put(obj)
        }
        prefs?.edit()?.putString(KEY_SCORES, array.toString())?.apply()
    }
}

/**
 * Data class representing a single score entry.
 */
data class ScoreEntry(
    val playerName: String,
    val score: Int,
    val timestamp: Long
)
