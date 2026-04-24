package com.example.balance

import kotlin.math.abs
import kotlin.random.Random

/**
 * ==========================================================
 * Tower.kt — Manages the stack of settled blocks
 * ==========================================================
 *
 * The tower is simply a list of blocks that have been stacked.
 * It provides collision detection for incoming blocks and
 * balance checking to determine game-over conditions.
 *
 * COLLISION LOGIC:
 * - A falling block collides with a tower block if:
 *   1. Their horizontal ranges overlap (AABB check)
 *   2. The falling block's bottom reaches the tower block's top
 * - When collision is detected, the falling block "snaps" to
 *   sit exactly on top of the collided block
 *
 * BALANCE CHECK:
 * - We compute how far each block overhangs the one below it
 * - If the overhang exceeds a threshold, the tower is unstable
 * - Specifically: if the center of a block is outside the edges
 *   of the block below it → tower collapses
 *
 * COLLAPSE ANIMATION:
 * - When tower becomes unbalanced, rather than instant game-over,
 *   blocks above the break-point get physics (gravity + rotation)
 *   so they visually topple and fall off screen.
 * - The collapse direction is determined by which side the tower
 *   is leaning toward.
 */
class Tower {
    // List of all blocks in the tower, bottom to top
    val blocks: MutableList<Block> = mutableListOf()

    /** List of blocks that are currently falling/collapsing */
    val collapsingBlocks: MutableList<Block> = mutableListOf()

    /** Whether the tower is currently in collapse animation */
    var isCollapsing: Boolean = false
        private set

    /** The current score = number of stacked blocks */
    val score: Int get() = blocks.size

    /** The Y coordinate of the top of the tower (lowest Y value since Y goes down) */
    val topY: Float
        get() = if (blocks.isEmpty()) Float.MAX_VALUE
                else blocks.minOf { it.y }

    /**
     * Add a block to the tower. Mark it as settled and zero its velocity.
     */
    fun addBlock(block: Block) {
        block.settled = true
        block.vx = 0f
        block.vy = 0f
        block.rotation = 0f
        block.angularVelocity = 0f
        blocks.add(block)
    }

    /**
     * Check if a falling block collides with any block in the tower.
     * Returns the tower block it collided with, or null.
     *
     * COLLISION DETECTION:
     * - We check AABB overlap on X axis
     * - We check if the falling block's bottom is at or past
     *   the tower block's top (within a small tolerance)
     * - We also check the block wasn't already past the tower block
     *   (to avoid tunneling at high speeds)
     */
    fun checkCollision(falling: Block): Block? {
        for (tower in blocks) {
            // Check horizontal overlap (AABB)
            val overlapX = falling.x < tower.right && falling.right > tower.x

            if (overlapX) {
                // Check if falling block's bottom is near or past tower block's top
                val reachedTop = falling.bottom >= tower.y && falling.y < tower.y

                if (reachedTop) {
                    return tower
                }
            }
        }
        return null
    }

    /**
     * Check if the tower is still balanced.
     *
     * BALANCE ALGORITHM:
     * - For each consecutive pair of blocks (bottom to top),
     *   check if the upper block's center is within the lower block's
     *   horizontal bounds (with some tolerance)
     * - If any block's center is outside → tower is unbalanced
     *
     * @param tolerance Extra pixels of tolerance for balance check
     * @return true if tower is balanced, false if it should collapse
     */
    fun isBalanced(tolerance: Float = 10f): Boolean {
        if (blocks.size < 2) return true

        // Sort blocks by Y position (bottom = highest Y first)
        val sorted = blocks.sortedByDescending { it.y }

        for (i in 0 until sorted.size - 1) {
            val lower = sorted[i]       // Block below
            val upper = sorted[i + 1]   // Block above

            // Check if the upper block's center is within the lower block's bounds
            val upperCenter = upper.centerX
            val lowerLeft = lower.x - tolerance
            val lowerRight = lower.right + tolerance

            if (upperCenter < lowerLeft || upperCenter > lowerRight) {
                return false // Tower collapses!
            }
        }
        return true
    }

    /**
     * Find the index where the tower breaks (first unbalanced pair).
     * Returns -1 if tower is balanced.
     *
     * The break point is the index in the sorted (bottom-to-top) list
     * where the block above is no longer supported by the block below.
     */
    fun findBreakPoint(tolerance: Float = 10f): Int {
        if (blocks.size < 2) return -1

        val sorted = blocks.sortedByDescending { it.y }

        for (i in 0 until sorted.size - 1) {
            val lower = sorted[i]
            val upper = sorted[i + 1]

            val upperCenter = upper.centerX
            val lowerLeft = lower.x - tolerance
            val lowerRight = lower.right + tolerance

            if (upperCenter < lowerLeft || upperCenter > lowerRight) {
                return i + 1 // The 'upper' block is the first unsupported block
            }
        }
        return -1
    }

    /**
     * COLLAPSE ANIMATION:
     * Start the tower topple animation. Blocks above the break point
     * are removed from the stable tower and given physics so they
     * fall and rotate off-screen.
     *
     * The collapse direction is based on which side the top block
     * is leaning toward — blocks will topple in that direction.
     */
    fun startCollapse() {
        if (isCollapsing) return

        val sorted = blocks.sortedByDescending { it.y }
        val breakIndex = findBreakPoint()

        if (breakIndex < 0) return

        isCollapsing = true

        // Determine collapse direction: check which side the break-point block leans
        val breakBlock = sorted[breakIndex]
        val supportBlock = sorted[breakIndex - 1]
        val leansRight = breakBlock.centerX > supportBlock.centerX

        // Move all blocks at and above break point into collapsing list
        val collapseSet = sorted.subList(breakIndex, sorted.size).toList()

        for ((index, block) in collapseSet.withIndex()) {
            block.settled = false

            // Give each block a slight velocity and spin in the collapse direction
            // Higher blocks get more dramatic movement
            val force = (index + 1) * 0.5f
            block.vx = if (leansRight) (2f + force) else -(2f + force)
            block.vy = -(1f + Random.nextFloat() * 2f)  // Slight upward pop

            // Angular velocity — blocks spin as they fall
            block.angularVelocity = if (leansRight) (3f + force * 1.5f) else -(3f + force * 1.5f)

            blocks.remove(block)
            collapsingBlocks.add(block)
        }
    }

    /**
     * Update collapsing blocks each frame.
     * Apply gravity, update position and rotation.
     *
     * @param gravity Downward acceleration per frame
     * @return true if collapse animation is still active (blocks on screen)
     */
    fun updateCollapse(gravity: Float, screenHeight: Float, cameraY: Float): Boolean {
        if (!isCollapsing) return false

        val iterator = collapsingBlocks.iterator()
        while (iterator.hasNext()) {
            val block = iterator.next()

            // Apply gravity
            block.vy += gravity

            // Update position
            block.x += block.vx
            block.y += block.vy

            // Update rotation
            block.rotation += block.angularVelocity

            // Remove blocks that have fallen well below the screen
            if (block.y - cameraY > screenHeight + 300f) {
                iterator.remove()
            }
        }

        // Collapse animation ends when all blocks have fallen off screen
        if (collapsingBlocks.isEmpty()) {
            isCollapsing = false
            return false
        }

        return true
    }

    /**
     * Apply a horizontal offset to ALL tower blocks.
     * Used by earthquake event to shake the entire tower.
     */
    fun applyOffset(dx: Float) {
        for (block in blocks) {
            block.x += dx
        }
    }

    /** Reset the tower for a new game */
    fun clear() {
        blocks.clear()
        collapsingBlocks.clear()
        isCollapsing = false
    }
}
