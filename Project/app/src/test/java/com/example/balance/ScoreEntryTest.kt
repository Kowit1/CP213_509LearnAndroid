package com.example.balance

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the ScoreEntry data class.
 * Covers: construction, equality, sorting behavior.
 */
class ScoreEntryTest {

    @Test
    fun construction_storesAllFields() {
        val entry = ScoreEntry("Alice", 42, 1000L)
        assertEquals("Alice", entry.playerName)
        assertEquals(42, entry.score)
        assertEquals(1000L, entry.timestamp)
    }

    @Test
    fun equality_sameValues_areEqual() {
        val a = ScoreEntry("Bob", 10, 999L)
        val b = ScoreEntry("Bob", 10, 999L)
        assertEquals(a, b)
    }

    @Test
    fun equality_differentScore_notEqual() {
        val a = ScoreEntry("Bob", 10, 999L)
        val b = ScoreEntry("Bob", 20, 999L)
        assertNotEquals(a, b)
    }

    @Test
    fun equality_differentName_notEqual() {
        val a = ScoreEntry("Alice", 10, 999L)
        val b = ScoreEntry("Bob", 10, 999L)
        assertNotEquals(a, b)
    }

    @Test
    fun sortByScoreDescending() {
        val entries = listOf(
            ScoreEntry("A", 5, 100L),
            ScoreEntry("B", 20, 200L),
            ScoreEntry("C", 10, 300L)
        )
        val sorted = entries.sortedByDescending { it.score }
        assertEquals(20, sorted[0].score)
        assertEquals(10, sorted[1].score)
        assertEquals(5, sorted[2].score)
    }

    @Test
    fun sortByScoreThenTimestamp() {
        val entries = listOf(
            ScoreEntry("A", 10, 100L),
            ScoreEntry("B", 10, 300L),
            ScoreEntry("C", 10, 200L)
        )
        val sorted = entries.sortedWith(
            compareByDescending<ScoreEntry> { it.score }
                .thenByDescending { it.timestamp }
        )
        assertEquals("B", sorted[0].playerName)
        assertEquals("C", sorted[1].playerName)
        assertEquals("A", sorted[2].playerName)
    }

    @Test
    fun copy_createsIndependentInstance() {
        val a = ScoreEntry("Alice", 50, 1000L)
        val b = a.copy(score = 100)
        assertEquals(50, a.score)
        assertEquals(100, b.score)
        assertEquals("Alice", b.playerName)
    }

    @Test
    fun zeroScore_isValid() {
        val entry = ScoreEntry("Player", 0, 0L)
        assertEquals(0, entry.score)
    }

    @Test
    fun emptyName_isValid() {
        val entry = ScoreEntry("", 10, 1000L)
        assertEquals("", entry.playerName)
    }
}
