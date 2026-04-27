package com.example.balance

import org.junit.Assert.*
import org.junit.Test

/**
 * ==========================================================
 * BlockTest.kt — Unit tests for the Block data class
 * ==========================================================
 *
 * Tests cover:
 *   - Computed properties (centerX, centerY, bottom, right)
 *   - Default values (velocity, settled, rotation)
 *   - Position mutation (simulating movement)
 *   - Data class equality and copy behavior
 */
class BlockTest {

    // ─── Helper ──────────────────────────────────────────

    private fun block(
        x: Float = 0f, y: Float = 0f,
        w: Float = 180f, h: Float = 50f
    ) = Block(x = x, y = y, width = w, height = h, color = 0xFFFF0000.toInt())

    // ─── Computed Properties ─────────────────────────────

    @Test
    fun centerX_isHalfWidth() {
        val b = block(x = 100f, w = 200f)
        assertEquals(200f, b.centerX, 0.001f)
    }

    @Test
    fun centerY_isHalfHeight() {
        val b = block(y = 50f, h = 40f)
        assertEquals(70f, b.centerY, 0.001f)
    }

    @Test
    fun bottom_isYPlusHeight() {
        val b = block(y = 100f, h = 50f)
        assertEquals(150f, b.bottom, 0.001f)
    }

    @Test
    fun right_isXPlusWidth() {
        val b = block(x = 30f, w = 180f)
        assertEquals(210f, b.right, 0.001f)
    }

    @Test
    fun computedProperties_atOrigin() {
        val b = block(x = 0f, y = 0f, w = 100f, h = 100f)
        assertEquals(50f, b.centerX, 0.001f)
        assertEquals(50f, b.centerY, 0.001f)
        assertEquals(100f, b.bottom, 0.001f)
        assertEquals(100f, b.right, 0.001f)
    }

    // ─── Default Values ──────────────────────────────────

    @Test
    fun defaults_velocityIsZero() {
        val b = block()
        assertEquals(0f, b.vx, 0.001f)
        assertEquals(0f, b.vy, 0.001f)
    }

    @Test
    fun defaults_notSettled() {
        val b = block()
        assertFalse(b.settled)
    }

    @Test
    fun defaults_rotationIsZero() {
        val b = block()
        assertEquals(0f, b.rotation, 0.001f)
        assertEquals(0f, b.angularVelocity, 0.001f)
    }

    // ─── Mutation ────────────────────────────────────────

    @Test
    fun positionCanBeMutated() {
        val b = block(x = 10f, y = 20f)
        b.x += 5f
        b.y += 10f
        assertEquals(15f, b.x, 0.001f)
        assertEquals(30f, b.y, 0.001f)
    }

    @Test
    fun velocityCanBeMutated() {
        val b = block()
        b.vx = 3.5f
        b.vy = -2.0f
        assertEquals(3.5f, b.vx, 0.001f)
        assertEquals(-2.0f, b.vy, 0.001f)
    }

    @Test
    fun settledCanBeSet() {
        val b = block()
        assertFalse(b.settled)
        b.settled = true
        assertTrue(b.settled)
    }

    @Test
    fun rotationCanBeMutated() {
        val b = block()
        b.rotation = 45f
        b.angularVelocity = 5f
        assertEquals(45f, b.rotation, 0.001f)
        assertEquals(5f, b.angularVelocity, 0.001f)
    }

    @Test
    fun simulateOneFrameOfMovement() {
        val b = block(x = 100f, y = 0f)
        b.vx = 2f
        b.vy = 3f

        // Simulate one frame
        b.x += b.vx
        b.y += b.vy

        assertEquals(102f, b.x, 0.001f)
        assertEquals(3f, b.y, 0.001f)
    }

    // ─── Data Class Behavior ─────────────────────────────

    @Test
    fun equality_sameProperties() {
        val a = Block(10f, 20f, 0f, 0f, 100f, 50f, 0xFF0000)
        val b = Block(10f, 20f, 0f, 0f, 100f, 50f, 0xFF0000)
        assertEquals(a, b)
    }

    @Test
    fun equality_differentPosition() {
        val a = block(x = 10f)
        val b = block(x = 20f)
        assertNotEquals(a, b)
    }

    @Test
    fun copy_createsIndependentInstance() {
        val a = block(x = 10f)
        val b = a.copy(x = 50f)
        assertEquals(10f, a.x, 0.001f)
        assertEquals(50f, b.x, 0.001f)
    }

    // ─── Edge Cases ──────────────────────────────────────

    @Test
    fun negativePosition_computedPropertiesStillWork() {
        val b = block(x = -100f, y = -200f, w = 50f, h = 30f)
        assertEquals(-75f, b.centerX, 0.001f)
        assertEquals(-185f, b.centerY, 0.001f)
        assertEquals(-170f, b.bottom, 0.001f)
        assertEquals(-50f, b.right, 0.001f)
    }

    @Test
    fun verySmallBlock() {
        val b = block(x = 0f, y = 0f, w = 1f, h = 1f)
        assertEquals(0.5f, b.centerX, 0.001f)
        assertEquals(0.5f, b.centerY, 0.001f)
    }

    @Test
    fun veryLargeBlock() {
        val b = block(x = 0f, y = 0f, w = 10000f, h = 10000f)
        assertEquals(5000f, b.centerX, 0.001f)
        assertEquals(10000f, b.right, 0.001f)
    }
}
