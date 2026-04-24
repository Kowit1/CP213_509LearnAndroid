package com.example.balance

/**
 * ==========================================================
 * Block.kt — Represents a single block in the game
 * ==========================================================
 *
 * Each block has a position (x, y), velocity (vx, vy),
 * dimensions (width, height), and a color for rendering.
 *
 * PHYSICS:
 * - x, y represent the TOP-LEFT corner of the block
 * - vx = horizontal velocity (affected by gyro/touch input + wind)
 * - vy = vertical velocity (affected by gravity)
 * - rotation = angle in degrees (used during tower collapse animation)
 * - angularVelocity = rate of rotation change (degrees/frame)
 * - When a block "lands" on the tower, it becomes settled
 *   and its velocities are zeroed out
 */
data class Block(
    var x: Float,                   // Horizontal position (left edge)
    var y: Float,                   // Vertical position (top edge)
    var vx: Float = 0f,             // Horizontal velocity (pixels/frame)
    var vy: Float = 0f,             // Vertical velocity (pixels/frame)
    val width: Float,               // Width of the block
    val height: Float,              // Height of the block
    val color: Int,                 // ARGB color for rendering
    var settled: Boolean = false,   // True once block is part of the tower
    var rotation: Float = 0f,       // Rotation angle in degrees (for collapse)
    var angularVelocity: Float = 0f // Rotation speed (degrees/frame, for collapse)
) {
    /** Center X of the block */
    val centerX: Float get() = x + width / 2f

    /** Center Y of the block */
    val centerY: Float get() = y + height / 2f

    /** Bottom edge of the block */
    val bottom: Float get() = y + height

    /** Right edge of the block */
    val right: Float get() = x + width
}
