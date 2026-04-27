package com.example.balance

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.abs
import kotlin.random.Random

/**
 * ==========================================================
 * GameView.kt — Main game rendering and update loop
 * ==========================================================
 *
 * This is a custom SurfaceView that runs the game loop in a
 * separate thread for smooth ~60 FPS rendering.
 *
 * GAME LOOP (runs at ~60 FPS):
 * 1. Apply input (gyro or touch) → move falling block horizontally
 * 2. Apply gravity → increase falling block's downward velocity
 * 3. Apply event effects (wind force / earthquake shake)
 * 4. Update positions of all moving objects
 * 5. Check collisions (falling block vs tower / ground)
 * 6. Check game over (tower balance / block fell off screen)
 * 7. Draw everything to the canvas
 *
 * RENDERING:
 * - Background gradient (dark sky)
 * - Ground platform
 * - Tower blocks (stacked)
 * - Falling block (with glow effect)
 * - Collapsing blocks (rotating and falling)
 * - UI overlay (score, event text, input mode, buttons)
 * - Camera follows the tower upward as it grows
 *
 * FEATURES:
 * - Random spawn position: blocks fall from random X positions
 * - Tower topple animation: when unbalanced, blocks above the
 *   break-point visually topple and fall off before game over
 */
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback, Runnable {

    // ===== Game thread =====
    private var gameThread: Thread? = null
    @Volatile private var isRunning = false

    // ===== Game objects =====
    private val tower = Tower()
    private val eventSystem = EventSystem(context)
    val inputManager = InputManager(context)
    val audioManager = AudioManager(context)
    private var fallingBlock: Block? = null

    // ===== Game state =====
    private var gameOver = false
    private var isCollapsePhase = false  // True while tower topple animation plays
    private var collapseDelayTimer = 0f  // Delay before showing game-over after collapse
    private var scoreSaved = false       // Flag to avoid saving score multiple times
    private var eventText = ""
    private var cameraY = 0f           // Camera offset for scrolling up
    private var screenWidth = 0f
    private var screenHeight = 0f
    private var groundY = 0f           // Y position of the ground

    // ===== Paints (reusable for performance) =====
    private val blockPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val blockBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.argb(100, 255, 255, 255)
    }
    private val groundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 48f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }
    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 72f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        setShadowLayer(6f, 3f, 3f, Color.BLACK)
    }
    private val eventTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 64f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(8f, 0f, 0f, Color.BLACK)
    }
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(120, 255, 255, 255)
    }
    private val buttonTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 56f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 0, 0, 0)
    }
    private val gameOverTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 96f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(10f, 0f, 0f, Color.RED)
    }
    private val restartTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(200, 255, 255, 255)
        textSize = 48f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.OUTER)
    }

    // ===== UI button areas =====
    private val leftButtonRect = RectF()
    private val rightButtonRect = RectF()
    private val modeButtonRect = RectF()
    private val backButtonRect = RectF()        // In-game back button (top-left)
    private val menuButtonGameOver = RectF()    // Game-over "Back to Menu" button

    /** Callback to return to the menu (set by the Activity) */
    var onBackToMenu: (() -> Unit)? = null

    // ===== Block configuration =====
    companion object {
        const val BLOCK_WIDTH = 180f
        const val BLOCK_HEIGHT = 50f
        const val GRAVITY = 0.25f            // Downward acceleration per frame
        const val MAX_FALL_SPEED = 12f       // Terminal velocity
        const val GROUND_HEIGHT = 100f       // Height of ground platform
        const val CAMERA_SMOOTH = 0.05f      // Camera follow smoothness
        const val TARGET_FPS = 60
        const val FRAME_TIME_MS = 1000L / TARGET_FPS
        const val COLLAPSE_DELAY = 1.5f      // Seconds to show collapse before game-over
        const val SPAWN_MARGIN = 40f         // Margin from screen edges for spawn
    }

    // ===== Block colors (vibrant palette) =====
    private val blockColors = intArrayOf(
        Color.rgb(231, 76, 60),    // Red
        Color.rgb(230, 126, 34),   // Orange
        Color.rgb(241, 196, 15),   // Yellow
        Color.rgb(46, 204, 113),   // Green
        Color.rgb(52, 152, 219),   // Blue
        Color.rgb(155, 89, 182),   // Purple
        Color.rgb(26, 188, 156),   // Teal
        Color.rgb(236, 100, 75),   // Coral
        Color.rgb(52, 73, 94),     // Dark Blue
        Color.rgb(22, 160, 133),   // Dark Teal
    )

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    // ===== SurfaceHolder callbacks =====

    override fun surfaceCreated(holder: SurfaceHolder) {
        screenWidth = width.toFloat()
        screenHeight = height.toFloat()
        groundY = screenHeight - GROUND_HEIGHT

        // Setup UI button positions
        val btnSize = 100f
        val btnMargin = 30f
        val btnY = screenHeight - GROUND_HEIGHT - btnSize - btnMargin

        leftButtonRect.set(btnMargin, btnY, btnMargin + btnSize, btnY + btnSize)
        rightButtonRect.set(
            screenWidth - btnMargin - btnSize, btnY,
            screenWidth - btnMargin, btnY + btnSize
        )
        modeButtonRect.set(
            screenWidth - 200f, 30f,
            screenWidth - 20f, 90f
        )

        // Back button (top-left, below score area)
        backButtonRect.set(30f, 110f, 170f, 165f)

        // Game-over menu button (centered, below restart text)
        val menuBtnW = 320f
        val menuBtnH = 70f
        menuButtonGameOver.set(
            screenWidth / 2f - menuBtnW / 2f,
            screenHeight / 2f + 160f,
            screenWidth / 2f + menuBtnW / 2f,
            screenHeight / 2f + 160f + menuBtnH
        )

        // Setup ground paint gradient
        groundPaint.shader = LinearGradient(
            0f, groundY, 0f, screenHeight,
            Color.rgb(44, 62, 80), Color.rgb(30, 40, 50),
            Shader.TileMode.CLAMP
        )

        // Initialize input, audio, and score storage, then start game
        inputManager.initialize()
        audioManager.init()
        ScoreRepository.init(context)
        startGame()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        screenWidth = width.toFloat()
        screenHeight = height.toFloat()
        groundY = screenHeight - GROUND_HEIGHT
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopGame()
    }

    // ===== Game lifecycle =====

    private fun startGame() {
        gameOver = false
        isCollapsePhase = false
        collapseDelayTimer = 0f
        scoreSaved = false
        tower.clear()
        eventSystem.reset()
        cameraY = 0f
        spawnBlock()

        // Start background music
        audioManager.playBGM()

        isRunning = true
        gameThread = Thread(this)
        gameThread?.start()
    }

    private fun stopGame() {
        isRunning = false
        try {
            gameThread?.join(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun restartGame() {
        gameOver = false
        isCollapsePhase = false
        collapseDelayTimer = 0f
        scoreSaved = false
        tower.clear()
        eventSystem.reset()
        cameraY = 0f
        spawnBlock()

        // Restart background music
        audioManager.playBGM()
    }

    /**
     * BLOCK SHAPES — Different block types for variety.
     * Each shape has a width and height. Some are easier to stack
     * (wide, short) while others are trickier (narrow, tall).
     *
     * Shape types:
     *  - SMALL:    Compact block, easy to place precisely
     *  - NORMAL:   Standard block, balanced difficulty
     *  - WIDE:     Easy to land on, but creates overhang risk
     *  - NARROW:   Hard to place, but fits in tight spaces
     *  - LARGE:    Big and heavy-looking, creates a wide platform
     *  - TINY:     Very small, challenging to stack on
     */
    private data class BlockShape(val width: Float, val height: Float)

    private val blockShapes = arrayOf(
        BlockShape(120f, 40f),     // SMALL
        BlockShape(180f, 50f),     // NORMAL
        BlockShape(240f, 35f),     // WIDE
        BlockShape(90f, 65f),      // NARROW (tall)
        BlockShape(210f, 55f),     // LARGE
        BlockShape(70f, 45f),      // TINY
        BlockShape(40f, 120f),
        BlockShape(50f, 180f),
        BlockShape(35f, 240f),
        BlockShape(65f, 90f),
        BlockShape(55f, 210f),
        BlockShape(45f, 70f),
    )

    /**
     * RANDOM SPAWN — Varied block shapes and positions.
     *
     * Each block gets a randomly selected shape (width × height)
     * and a random X position across the screen, forcing the player
     * to adapt their strategy to different block sizes.
     *
     * Wide blocks are easier to land but may overhang.
     * Narrow blocks are precise but harder to stack.
     */
    private fun spawnBlock() {
        val color = blockColors[tower.score % blockColors.size]

        // Pick a random block shape
        val shape = blockShapes[Random.nextInt(blockShapes.size)]
        val blockW = shape.width
        val blockH = shape.height

        // Random X position across the screen (with margin)
        val minX = SPAWN_MARGIN
        val maxX = screenWidth - blockW - SPAWN_MARGIN
        val startX = if (maxX > minX) {
            Random.nextFloat() * (maxX - minX) + minX
        } else {
            screenWidth / 2f - blockW / 2f
        }

        // Spawn above the visible area (accounting for camera)
        val spawnY = cameraY - blockH - 20f

        fallingBlock = Block(
            x = startX,
            y = spawnY,
            vx = 0f,
            vy = 0f,
            width = blockW,
            height = blockH,
            color = color
        )
    }

    // ===== Main game loop =====

    /**
     * GAME LOOP — Runs on a separate thread at ~60 FPS.
     *
     * Each iteration:
     * 1. Record start time
     * 2. Update game state (physics, input, events, collisions)
     * 3. Draw everything to canvas
     * 4. Sleep to maintain target FPS
     */
    override fun run() {
        while (isRunning) {
            val startTime = System.currentTimeMillis()

            if (!gameOver) {
                update()
            }
            draw()

            // Frame rate control: sleep to maintain ~60 FPS
            val elapsed = System.currentTimeMillis() - startTime
            val sleepTime = FRAME_TIME_MS - elapsed
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    // ===== Update logic =====

    /**
     * PHYSICS UPDATE — Called each frame (~60 times/second)
     *
     * Order of operations:
     * 1. If in collapse phase → update collapse animation → check if done
     * 2. Get input (gyro tilt or touch)
     * 3. Apply input to falling block's horizontal velocity
     * 4. Apply gravity to falling block's vertical velocity
     * 5. Apply wind force (if wind event active)
     * 6. Apply earthquake shake (if earthquake event active)
     * 7. Update falling block position
     * 8. Check collision with tower or ground
     * 9. Check if block fell off screen → game over
     * 10. Check tower balance → start collapse if unstable
     * 11. Update camera to follow tower growth
     * 12. Update event system timer
     */
    private fun update() {
        val deltaTime = 1f / TARGET_FPS

        // === COLLAPSE ANIMATION PHASE ===
        // If tower is currently collapsing, update the animation
        // and don't process normal gameplay until it finishes
        if (isCollapsePhase) {
            val stillCollapsing = tower.updateCollapse(GRAVITY, screenHeight, cameraY)

            // Count down the delay timer before showing game-over
            collapseDelayTimer += deltaTime
            if (collapseDelayTimer >= COLLAPSE_DELAY || !stillCollapsing) {
                gameOver = true
                audioManager.playSfxGameOver()
                audioManager.pauseBGM()
                saveScoreIfNeeded()
                isCollapsePhase = false
            }
            return // Skip normal update during collapse
        }

        val block = fallingBlock ?: return

        // === 1. Get horizontal input ===
        val input = inputManager.getHorizontalInput()

        // === 2. Apply input to horizontal velocity ===
        block.vx = input

        // === 3. Apply gravity to vertical velocity ===
        // Gravity increases downward speed each frame
        block.vy += GRAVITY
        if (block.vy > MAX_FALL_SPEED) block.vy = MAX_FALL_SPEED

        // === 4. Apply wind force (horizontal push) ===
        if (eventSystem.currentEvent == EventType.WIND) {
            block.vx += eventSystem.windForce
        }

        // === 5. Apply earthquake (shake tower) ===
        if (eventSystem.currentEvent == EventType.EARTHQUAKE) {
            tower.applyOffset(eventSystem.quakeOffset * 0.1f) // Scale down for subtlety
        }

        // === 6. Update falling block position ===
        block.x += block.vx
        block.y += block.vy

        // === 7. Check collision with tower blocks ===
        val collidedBlock = tower.checkCollision(block)
        if (collidedBlock != null) {
            // Snap block to sit on top of the collided block
            block.y = collidedBlock.y - block.height
            block.vx = 0f
            block.vy = 0f
            tower.addBlock(block)

            // Check balance after adding block
            if (!tower.isBalanced()) {
                // START COLLAPSE ANIMATION instead of instant game-over
                // The tower topples visually before the game-over screen appears
                tower.startCollapse()
                isCollapsePhase = true
                audioManager.playSfxTopple()
                collapseDelayTimer = 0f
                fallingBlock = null
                return
            }

            // Play land sound and spawn next block
            audioManager.playSfxLand()
            spawnBlock()
        }

        // === 7b. Check collision with ground ===
        // Only the FIRST block can land on the ground (to start the tower).
        // After that, hitting the ground means you missed the tower → game over.
        if (block.bottom >= groundY - cameraY) {
            if (tower.blocks.isEmpty()) {
                // First block — land on ground to start the tower
                block.y = groundY - cameraY - block.height
                block.vx = 0f
                block.vy = 0f
                tower.addBlock(block)
                audioManager.playSfxLand()
                spawnBlock()
            } else {
                // Missed the tower — game over!
                gameOver = true
                audioManager.playSfxGameOver()
                audioManager.pauseBGM()
                saveScoreIfNeeded()
                return
            }
        }

        // === 8. Check if block fell off screen (left or right) ===
        if (block.x + block.width < -100f || block.x > screenWidth + 100f) {
            // Block went off screen → game over
            gameOver = true
            audioManager.playSfxGameOver()
            audioManager.pauseBGM()
            saveScoreIfNeeded()
            return
        }

        // === 9. Check if block fell below ground (missed everything) ===
        if (block.y - cameraY > screenHeight + 200f) {
            gameOver = true
            audioManager.playSfxGameOver()
            audioManager.pauseBGM()
            saveScoreIfNeeded()
            return
        }

        // === 10. Update camera to follow tower ===
        // Camera smoothly scrolls up as tower gets taller
        if (tower.blocks.isNotEmpty()) {
            val targetCameraY = (groundY - tower.topY) - screenHeight * 0.6f
            if (targetCameraY > 0) {
                cameraY += (targetCameraY - cameraY) * CAMERA_SMOOTH
            }
        }

        // === 11. Update event system ===
        eventText = eventSystem.update(deltaTime)

        // === 12. Reset frame-specific input ===
        inputManager.resetFrameInput()
    }

    // ===== Drawing =====

    /**
     * Draw the entire game scene to the canvas.
     */
    private fun draw() {
        val canvas: Canvas
        try {
            canvas = holder.lockCanvas() ?: return
        } catch (e: Exception) {
            return
        }

        try {
            // --- Background (gradient sky) ---
            drawBackground(canvas)

            // --- Save canvas state and apply camera transform ---
            canvas.save()
            canvas.translate(0f, cameraY)

            // --- Draw ground ---
            drawGround(canvas)

            // --- Draw tower blocks ---
            drawTower(canvas)

            // --- Draw collapsing blocks (toppling animation) ---
            drawCollapsingBlocks(canvas)

            // --- Draw falling block ---
            fallingBlock?.let { drawBlock(canvas, it, true) }

            // --- Restore canvas (remove camera offset) ---
            canvas.restore()

            // --- Draw UI overlay (not affected by camera) ---
            drawUI(canvas)

            // --- Draw game over screen ---
            if (gameOver) {
                drawGameOver(canvas)
            }
        } finally {
            try {
                holder.unlockCanvasAndPost(canvas)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Draw gradient background */
    private fun drawBackground(canvas: Canvas) {
        val bgGradient = LinearGradient(
            0f, 0f, 0f, screenHeight,
            Color.rgb(15, 12, 41),     // Deep purple-black at top
            Color.rgb(48, 43, 99),     // Purple at bottom
            Shader.TileMode.CLAMP
        )
        val bgPaint = Paint().apply { shader = bgGradient }
        canvas.drawRect(0f, 0f, screenWidth, screenHeight, bgPaint)

        // Draw some stars
        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 255, 255, 255)
        }
        val starSeed = 42 // Fixed seed for consistent star positions
        val rng = Random(starSeed)
        for (i in 0 until 50) {
            val sx = rng.nextFloat() * screenWidth
            val sy = rng.nextFloat() * screenHeight * 0.6f
            val sr = rng.nextFloat() * 2f + 0.5f
            canvas.drawCircle(sx, sy, sr, starPaint)
        }
    }

    /** Draw the ground platform */
    private fun drawGround(canvas: Canvas) {
        // Ground fill
        canvas.drawRect(0f, groundY, screenWidth, groundY + GROUND_HEIGHT + 500f, groundPaint)

        // Ground top line (bright accent)
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(46, 204, 113)
            strokeWidth = 4f
        }
        canvas.drawLine(0f, groundY, screenWidth, groundY, linePaint)
    }

    /** Draw all tower blocks */
    private fun drawTower(canvas: Canvas) {
        for (block in tower.blocks) {
            drawBlock(canvas, block, false)
        }
    }

    /**
     * COLLAPSE ANIMATION RENDERING:
     * Draw blocks that are currently falling/toppling as part of
     * the tower collapse animation. Each block is drawn with its
     * current rotation, creating a realistic toppling effect.
     */
    private fun drawCollapsingBlocks(canvas: Canvas) {
        for (block in tower.collapsingBlocks) {
            drawBlock(canvas, block, false, useRotation = true)
        }
    }

    /**
     * Draw a single block with optional glow effect and rotation.
     *
     * @param canvas The canvas to draw on
     * @param block The block to draw
     * @param isActive True if this is the currently falling block (adds glow)
     * @param useRotation True to apply block's rotation angle (for collapse)
     */
    private fun drawBlock(canvas: Canvas, block: Block, isActive: Boolean, useRotation: Boolean = false) {
        val rect = RectF(block.x, block.y, block.right, block.bottom)

        // If block has rotation (collapse animation), rotate canvas around block center
        if (useRotation && block.rotation != 0f) {
            canvas.save()
            canvas.rotate(block.rotation, block.centerX, block.centerY)
        }

        // Draw glow for active block
        if (isActive) {
            glowPaint.color = Color.argb(60, Color.red(block.color), Color.green(block.color), Color.blue(block.color))
            canvas.drawRoundRect(rect, 6f, 6f, glowPaint)
        }

        // Draw block fill
        blockPaint.color = block.color
        canvas.drawRoundRect(rect, 6f, 6f, blockPaint)

        // Draw highlight (top edge shine)
        val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                block.x, block.y, block.x, block.y + block.height * 0.4f,
                Color.argb(80, 255, 255, 255), Color.argb(0, 255, 255, 255),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, 6f, 6f, highlightPaint)

        // Draw border
        canvas.drawRoundRect(rect, 6f, 6f, blockBorderPaint)

        // Restore canvas if rotated
        if (useRotation && block.rotation != 0f) {
            canvas.restore()
        }
    }

    /** Draw UI elements (score, event text, buttons) */
    private fun drawUI(canvas: Canvas) {
        // Score display (top-left)
        canvas.drawText("Score: ${tower.score}", 30f, 80f, scorePaint)

        // Back to Menu button (top-left, below score)
        val backBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(140, 255, 255, 255)
        }
        canvas.drawRoundRect(backButtonRect, 12f, 12f, backBtnPaint)
        val backBtnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(3f, 0f, 0f, Color.BLACK)
        }
        canvas.drawText("◀ MENU", backButtonRect.centerX(), backButtonRect.centerY() + 10f, backBtnTextPaint)

        // Input mode indicator (top-right)
        val modeText = if (inputManager.useGyro) "🎮 GYRO" else "👆 TOUCH"
        canvas.drawRoundRect(modeButtonRect, 12f, 12f, buttonPaint)
        canvas.drawText(modeText, modeButtonRect.centerX(), modeButtonRect.centerY() + 15f, buttonTextPaint.apply { textSize = 32f })

        // Event text (center of screen)
        if (eventText.isNotEmpty()) {
            val eventColor = when (eventSystem.currentEvent) {
                EventType.WIND -> Color.rgb(52, 152, 219)       // Blue for wind
                EventType.EARTHQUAKE -> Color.rgb(231, 76, 60)  // Red for earthquake
                else -> Color.WHITE
            }
            eventTextPaint.color = eventColor
            canvas.drawText(eventText, screenWidth / 2f, 180f, eventTextPaint)
        }

        // Show "COLLAPSING!" text during tower collapse animation
        if (isCollapsePhase) {
            val collapsePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(255, 69, 58)
                textSize = 80f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                setShadowLayer(10f, 0f, 0f, Color.RED)
            }
            canvas.drawText("💥 TOPPLING!", screenWidth / 2f, screenHeight / 2f, collapsePaint)
        }

        // Draw on-screen control buttons (only in touch mode)
        if (!inputManager.useGyro) {
            // Left button
            canvas.drawRoundRect(leftButtonRect, 16f, 16f, buttonPaint)
            canvas.drawText("◀", leftButtonRect.centerX(), leftButtonRect.centerY() + 20f, buttonTextPaint.apply { textSize = 56f })

            // Right button
            canvas.drawRoundRect(rightButtonRect, 16f, 16f, buttonPaint)
            canvas.drawText("▶", rightButtonRect.centerX(), rightButtonRect.centerY() + 20f, buttonTextPaint.apply { textSize = 56f })
        }

        // Gyro availability indicator
        val gyroStatusText = if (inputManager.gyroAvailable) "Sensor: ✓" else "Sensor: ✗ (Touch mode)"
        val statusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (inputManager.gyroAvailable) Color.rgb(46, 204, 113) else Color.rgb(231, 76, 60)
            textSize = 28f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        canvas.drawText(gyroStatusText, 30f, screenHeight - GROUND_HEIGHT - 20f, statusPaint)
    }

    /** Draw game over overlay */
    private fun drawGameOver(canvas: Canvas) {
        // Darken the screen
        canvas.drawRect(0f, 0f, screenWidth, screenHeight, overlayPaint)

        // Game Over text
        canvas.drawText("GAME OVER", screenWidth / 2f, screenHeight / 2f - 60f, gameOverTextPaint)

        // Final score
        val scoreFinalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(241, 196, 15)
            textSize = 72f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(6f, 0f, 0f, Color.BLACK)
        }
        canvas.drawText("Score: ${tower.score}", screenWidth / 2f, screenHeight / 2f + 40f, scoreFinalPaint)

        // Tap to restart
        canvas.drawText("Tap to Restart", screenWidth / 2f, screenHeight / 2f + 120f, restartTextPaint)

        // Back to Menu button
        val menuBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                menuButtonGameOver.left, menuButtonGameOver.top,
                menuButtonGameOver.left, menuButtonGameOver.bottom,
                Color.rgb(52, 152, 219), Color.rgb(41, 128, 185),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(menuButtonGameOver, 16f, 16f, menuBtnPaint)
        val menuBtnBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.argb(80, 255, 255, 255)
        }
        canvas.drawRoundRect(menuButtonGameOver, 16f, 16f, menuBtnBorderPaint)
        val menuBtnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 40f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(4f, 0f, 0f, Color.BLACK)
        }
        canvas.drawText("BACK TO MENU", menuButtonGameOver.centerX(), menuButtonGameOver.centerY() + 14f, menuBtnTextPaint)
    }

    // ===== Touch input handling =====

    /**
     * TOUCH INPUT HANDLING:
     * - Handles both drag gestures and button taps
     * - Drag: moves the falling block left/right (in touch mode)
     * - Button taps: for on-screen left/right buttons
     * - Mode toggle: tap the mode button to switch gyro/touch
     * - Game over: tap anywhere to restart
     */
    private var lastTouchX = 0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x

                // Check back-to-menu button (always available)
                if (backButtonRect.contains(event.x, event.y)) {
                    post { onBackToMenu?.invoke() }
                    return true
                }

                // Check if game over
                if (gameOver) {
                    // Check "Back to Menu" button on game-over screen
                    if (menuButtonGameOver.contains(event.x, event.y)) {
                        post { onBackToMenu?.invoke() }
                        return true
                    }
                    // Tap anywhere else to restart
                    restartGame()
                    return true
                }

                // Don't process input during collapse animation
                if (isCollapsePhase) return true

                // Check mode toggle button
                if (modeButtonRect.contains(event.x, event.y)) {
                    inputManager.toggleInputMode()
                    return true
                }

                // Check left/right buttons (touch mode only)
                if (!inputManager.useGyro) {
                    if (leftButtonRect.contains(event.x, event.y)) {
                        inputManager.leftButtonPressed = true
                    }
                    if (rightButtonRect.contains(event.x, event.y)) {
                        inputManager.rightButtonPressed = true
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (!inputManager.useGyro && !gameOver && !isCollapsePhase) {
                    // Touch drag: compute delta and apply
                    val deltaX = event.x - lastTouchX
                    inputManager.touchDeltaX = deltaX * 0.3f // Scale down for control
                    lastTouchX = event.x
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                inputManager.leftButtonPressed = false
                inputManager.rightButtonPressed = false
                inputManager.touchDeltaX = 0f
            }
        }

        return true
    }

    // ===== Score saving =====

    /**
     * Save the current score to the repository (once per game).
     * Only saves if the player stacked at least 1 block.
     */
    private fun saveScoreIfNeeded() {
        if (scoreSaved) return
        scoreSaved = true
        val finalScore = tower.score
        if (finalScore > 0) {
            ScoreRepository.saveScore("Player", finalScore)
        }
    }

    // ===== Lifecycle =====

    fun pause() {
        isRunning = false
        inputManager.pause()
        audioManager.pauseBGM()
        try {
            gameThread?.join(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun resume() {
        inputManager.resume()
        audioManager.updateBGMVolume()
        if (!gameOver) audioManager.playBGM()
        if (!isRunning && !gameOver && holder.surface.isValid) {
            isRunning = true
            gameThread = Thread(this)
            gameThread?.start()
        }
    }

    /** Release all resources */
    fun releaseResources() {
        audioManager.release()
    }
}
