package com.example.balance

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the Tower class.
 * Covers: adding blocks, score, collision, balance, collapse, offset, clear.
 */
class TowerTest {

    private lateinit var tower: Tower

    private fun block(
        x: Float = 100f, y: Float = 500f,
        w: Float = 180f, h: Float = 50f
    ) = Block(x = x, y = y, width = w, height = h, color = 0xFFFF0000.toInt())

    @Before
    fun setUp() {
        tower = Tower()
    }

    // ── Score ────────────────────────────────────────────

    @Test
    fun emptyTower_scoreIsZero() { assertEquals(0, tower.score) }

    @Test
    fun addBlock_incrementsScore() {
        tower.addBlock(block())
        assertEquals(1, tower.score)
    }

    @Test
    fun addMultipleBlocks_scoreMatchesCount() {
        repeat(5) { tower.addBlock(block(y = 500f - it * 50f)) }
        assertEquals(5, tower.score)
    }

    @Test
    fun addBlock_setsSettledAndZerosVelocity() {
        val b = block().apply { vx = 5f; vy = 10f; rotation = 45f; angularVelocity = 3f }
        tower.addBlock(b)
        assertTrue(b.settled)
        assertEquals(0f, b.vx, 0.001f)
        assertEquals(0f, b.vy, 0.001f)
        assertEquals(0f, b.rotation, 0.001f)
        assertEquals(0f, b.angularVelocity, 0.001f)
    }

    // ── Top-Y ────────────────────────────────────────────

    @Test
    fun emptyTower_topYIsMaxValue() {
        assertEquals(Float.MAX_VALUE, tower.topY, 0.001f)
    }

    @Test
    fun multipleBlocks_topYIsSmallest() {
        tower.addBlock(block(y = 500f))
        tower.addBlock(block(y = 300f))
        assertEquals(300f, tower.topY, 0.001f)
    }

    // ── Collision Detection ──────────────────────────────

    @Test
    fun collision_emptyTower_returnsNull() {
        assertNull(tower.checkCollision(block()))
    }

    @Test
    fun collision_landsOnTowerBlock() {
        val tb = block(x = 100f, y = 500f, w = 180f, h = 50f)
        tower.addBlock(tb)
        // falling.bottom=510 >= 500, falling.y=460 < 500
        val falling = block(x = 120f, y = 460f, w = 180f, h = 50f)
        assertNotNull(tower.checkCollision(falling))
    }

    @Test
    fun collision_noXOverlap_returnsNull() {
        tower.addBlock(block(x = 100f, y = 500f, w = 180f))
        assertNull(tower.checkCollision(block(x = 400f, y = 460f, w = 80f)))
    }

    @Test
    fun collision_aboveTower_returnsNull() {
        tower.addBlock(block(x = 100f, y = 500f, w = 180f, h = 50f))
        // falling.bottom=450 < 500
        assertNull(tower.checkCollision(block(x = 100f, y = 400f, w = 180f, h = 50f)))
    }

    @Test
    fun collision_belowTower_returnsNull() {
        tower.addBlock(block(x = 100f, y = 500f))
        // falling.y=520 >= 500
        assertNull(tower.checkCollision(block(x = 100f, y = 520f)))
    }

    // ── Balance ──────────────────────────────────────────

    @Test
    fun isBalanced_emptyOrSingle_returnsTrue() {
        assertTrue(tower.isBalanced())
        tower.addBlock(block())
        assertTrue(tower.isBalanced())
    }

    @Test
    fun isBalanced_aligned_returnsTrue() {
        tower.addBlock(block(x = 100f, y = 500f))
        tower.addBlock(block(x = 100f, y = 450f))
        assertTrue(tower.isBalanced())
    }

    @Test
    fun isBalanced_extremeOverhang_returnsFalse() {
        tower.addBlock(block(x = 100f, y = 500f, w = 180f))
        tower.addBlock(block(x = 500f, y = 450f, w = 180f))
        assertFalse(tower.isBalanced())
    }

    @Test
    fun isBalanced_customTolerance() {
        tower.addBlock(block(x = 100f, y = 500f, w = 180f))
        tower.addBlock(block(x = 210f, y = 450f, w = 180f))
        // center=300, right=280 → with tol=10 unbalanced, with tol=25 balanced
        assertFalse(tower.isBalanced(tolerance = 10f))
        assertTrue(tower.isBalanced(tolerance = 25f))
    }

    // ── Break-Point ──────────────────────────────────────

    @Test
    fun findBreakPoint_balanced_returnsNegativeOne() {
        tower.addBlock(block(x = 100f, y = 500f))
        tower.addBlock(block(x = 100f, y = 450f))
        assertEquals(-1, tower.findBreakPoint())
    }

    @Test
    fun findBreakPoint_unbalanced_returnsPositive() {
        tower.addBlock(block(x = 100f, y = 500f, w = 180f))
        tower.addBlock(block(x = 500f, y = 450f, w = 180f))
        assertTrue(tower.findBreakPoint() > 0)
    }

    // ── Collapse ─────────────────────────────────────────

    @Test
    fun startCollapse_movesBlocksToCollapsingList() {
        tower.addBlock(block(x = 100f, y = 500f, w = 180f))
        tower.addBlock(block(x = 500f, y = 450f, w = 180f))
        tower.startCollapse()
        assertTrue(tower.isCollapsing)
        assertTrue(tower.collapsingBlocks.isNotEmpty())
    }

    @Test
    fun startCollapse_collapsingBlocksHavePhysics() {
        tower.addBlock(block(x = 100f, y = 500f, w = 180f))
        tower.addBlock(block(x = 500f, y = 450f, w = 180f))
        tower.startCollapse()
        for (b in tower.collapsingBlocks) {
            assertFalse(b.settled)
            assertTrue(b.vx != 0f || b.vy != 0f)
        }
    }

    @Test
    fun startCollapse_balancedTower_doesNothing() {
        tower.addBlock(block(x = 100f, y = 500f))
        tower.addBlock(block(x = 100f, y = 450f))
        tower.startCollapse()
        assertFalse(tower.isCollapsing)
    }

    @Test
    fun updateCollapse_notCollapsing_returnsFalse() {
        assertFalse(tower.updateCollapse(0.25f, 1000f, 0f))
    }

    @Test
    fun updateCollapse_appliesGravityAndPosition() {
        tower.addBlock(block(x = 100f, y = 500f, w = 180f))
        tower.addBlock(block(x = 500f, y = 450f, w = 180f))
        tower.startCollapse()
        val b = tower.collapsingBlocks.first()
        val vyBefore = b.vy
        val yBefore = b.y
        tower.updateCollapse(0.25f, 1000f, 0f)
        assertEquals(vyBefore + 0.25f, b.vy, 0.001f)
    }

    @Test
    fun updateCollapse_removesOffScreenBlocks() {
        tower.addBlock(block(x = 100f, y = 500f, w = 180f))
        tower.addBlock(block(x = 500f, y = 450f, w = 180f))
        tower.startCollapse()
        for (b in tower.collapsingBlocks) b.y = 2000f
        val result = tower.updateCollapse(0.25f, 1000f, 0f)
        assertFalse(result)
        assertTrue(tower.collapsingBlocks.isEmpty())
    }

    // ── Offset (Earthquake) ──────────────────────────────

    @Test
    fun applyOffset_shiftsAllBlocks() {
        tower.addBlock(block(x = 100f, y = 500f))
        tower.addBlock(block(x = 100f, y = 450f))
        tower.applyOffset(5f)
        tower.blocks.forEach { assertEquals(105f, it.x, 0.001f) }
    }

    @Test
    fun applyOffset_negativeShift() {
        tower.addBlock(block(x = 100f, y = 500f))
        tower.applyOffset(-10f)
        assertEquals(90f, tower.blocks[0].x, 0.001f)
    }

    // ── Clear ────────────────────────────────────────────

    @Test
    fun clear_resetsEverything() {
        tower.addBlock(block(x = 100f, y = 500f, w = 180f))
        tower.addBlock(block(x = 500f, y = 450f, w = 180f))
        tower.startCollapse()
        tower.clear()
        assertEquals(0, tower.score)
        assertTrue(tower.blocks.isEmpty())
        assertTrue(tower.collapsingBlocks.isEmpty())
        assertFalse(tower.isCollapsing)
    }

    @Test
    fun clear_canAddBlocksAfterReset() {
        tower.addBlock(block())
        tower.clear()
        tower.addBlock(block(y = 500f))
        assertEquals(1, tower.score)
    }
}
