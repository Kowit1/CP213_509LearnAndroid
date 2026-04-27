package com.example.balance

import android.app.Activity
import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin
import kotlin.random.Random

/** Simple star data for twinkling background */
private data class ScoreboardStar(val x: Float, val y: Float, val size: Float, val twinkleOffset: Float)

/**
 * ==========================================================
 * ScoreboardActivity.kt — High-score display screen
 * ==========================================================
 *
 * Displays the top-20 scores in a beautiful Canvas-drawn UI
 * matching the game's aesthetic. Features:
 *   - Animated star background (same as game)
 *   - Trophy header with glow effect
 *   - Medal badges for top 3 (🥇🥈🥉)
 *   - Scrollable score list
 *   - "BACK" button to return to menu
 *   - "CLEAR" button to reset all scores
 */
class ScoreboardActivity : Activity() {

    private lateinit var scoreView: ScoreboardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        GameSettings.init(this)
        ScoreRepository.init(this)

        scoreView = ScoreboardView(this)
        setContentView(scoreView)
    }

    override fun onResume() {
        super.onResume()
        scoreView.resumeAnimation()
    }

    override fun onPause() {
        super.onPause()
        scoreView.pauseAnimation()
    }

    // =========================================================
    // ScoreboardView — Custom Canvas-drawn scoreboard
    // =========================================================
    inner class ScoreboardView(private val activity: ScoreboardActivity) : View(activity), Runnable {

        private var animThread: Thread? = null
        @Volatile private var running = false
        private var animTime = 0f

        // Screen metrics
        private var sw = 0f
        private var sh = 0f

        // Scroll state
        private var scrollY = 0f
        private var maxScrollY = 0f
        private var lastTouchY = 0f
        private var isDragging = false
        private var scrollVelocity = 0f

        // Button areas
        private val backBtnRect = RectF()
        private val clearBtnRect = RectF()

        // Score data
        private var scores: List<ScoreEntry> = emptyList()

        // Stars
        private val starRng = Random(99)
        private var stars: List<ScoreboardStar> = emptyList()

        // Layout constants
        private val HEADER_HEIGHT = 280f
        private val ROW_HEIGHT = 100f
        private val ROW_MARGIN = 12f
        private val SIDE_PADDING = 40f
        private val BOTTOM_PADDING = 160f

        // Date formatter
        private val dateFormat = SimpleDateFormat("dd MMM yyyy  HH:mm", Locale.getDefault())

        // ===== Paints =====
        private val bgPaint = Paint()
        private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }

        private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 72f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(16f, 0f, 0f, Color.rgb(241, 196, 15))
        }
        private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(189, 195, 199)
            textSize = 30f
            textAlign = Paint.Align.CENTER
        }
        private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(150, 189, 195, 199)
            textSize = 40f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }

        // Row paints
        private val rowBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rankPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        private val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(241, 196, 15)
            textSize = 40f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        private val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(140, 189, 195, 199)
            textSize = 22f
        }

        // Button paints
        private val btnPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val btnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 40f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        // Medal colors
        private val goldColor = Color.rgb(255, 215, 0)
        private val silverColor = Color.rgb(192, 192, 192)
        private val bronzeColor = Color.rgb(205, 127, 50)

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            sw = w.toFloat()
            sh = h.toFloat()

            // Generate stars
            stars = (0 until 80).map {
                ScoreboardStar(
                    starRng.nextFloat() * sw,
                    starRng.nextFloat() * sh,
                    starRng.nextFloat() * 2.5f + 0.5f,
                    starRng.nextFloat() * 6.28f
                )
            }

            // Button positions (at the bottom)
            val btnW = 200f
            val btnH = 65f
            val btnY = sh - 100f
            backBtnRect.set(
                sw / 2f - btnW - 20f, btnY,
                sw / 2f - 20f, btnY + btnH
            )
            clearBtnRect.set(
                sw / 2f + 20f, btnY,
                sw / 2f + btnW + 20f, btnY + btnH
            )

            refreshScores()
        }

        private fun refreshScores() {
            scores = ScoreRepository.loadScores()
            val contentHeight = HEADER_HEIGHT + scores.size * (ROW_HEIGHT + ROW_MARGIN) + BOTTOM_PADDING
            maxScrollY = (contentHeight - sh + 120f).coerceAtLeast(0f)
            scrollY = scrollY.coerceIn(0f, maxScrollY)
        }

        fun resumeAnimation() {
            refreshScores()
            running = true
            animThread = Thread(this)
            animThread?.start()
        }

        fun pauseAnimation() {
            running = false
            try { animThread?.join(500) } catch (_: Exception) {}
        }

        override fun run() {
            while (running) {
                animTime += 0.016f

                // Smooth decelerate scroll
                if (!isDragging && kotlin.math.abs(scrollVelocity) > 0.5f) {
                    scrollY = (scrollY - scrollVelocity).coerceIn(0f, maxScrollY)
                    scrollVelocity *= 0.92f
                }

                postInvalidate()
                try { Thread.sleep(16) } catch (_: Exception) {}
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (sw == 0f) return

            drawBackground(canvas)
            drawHeader(canvas)
            drawScoreList(canvas)
            drawButtons(canvas)
        }

        private fun drawBackground(canvas: Canvas) {
            val grad = LinearGradient(
                0f, 0f, 0f, sh,
                Color.rgb(15, 12, 41), Color.rgb(48, 43, 99),
                Shader.TileMode.CLAMP
            )
            bgPaint.shader = grad
            canvas.drawRect(0f, 0f, sw, sh, bgPaint)

            for (star in stars) {
                val twinkle = 0.5f + 0.5f * sin(animTime * 2f + star.twinkleOffset).toFloat()
                starPaint.alpha = (twinkle * 220).toInt()
                canvas.drawCircle(star.x, star.y, star.size * twinkle, starPaint)
            }
        }

        private fun drawHeader(canvas: Canvas) {
            val headerY = HEADER_HEIGHT * 0.4f - scrollY

            // Trophy icon with bounce
            val bounce = sin(animTime * 1.5f).toFloat() * 6f
            titlePaint.textSize = 72f
            canvas.drawText("🏆 SCOREBOARD", sw / 2f, headerY + bounce, titlePaint)

            // Subtitle
            val countText = if (scores.isEmpty()) "No scores yet" else "Top ${scores.size} scores"
            canvas.drawText(countText, sw / 2f, headerY + 45f, subtitlePaint)

            // Divider line with gradient
            val dividerY = headerY + 70f
            val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    SIDE_PADDING, dividerY, sw - SIDE_PADDING, dividerY,
                    Color.argb(0, 241, 196, 15),
                    Color.rgb(241, 196, 15),
                    Shader.TileMode.MIRROR
                )
                strokeWidth = 2f
            }
            canvas.drawLine(SIDE_PADDING, dividerY, sw - SIDE_PADDING, dividerY, dividerPaint)
        }

        private fun drawScoreList(canvas: Canvas) {
            if (scores.isEmpty()) {
                val emptyY = HEADER_HEIGHT + 100f - scrollY
                canvas.drawText("Play the game to set a score!", sw / 2f, emptyY, emptyPaint)
                canvas.drawText("🎮", sw / 2f, emptyY + 60f, emptyPaint.apply { textSize = 60f })
                emptyPaint.textSize = 40f
                return
            }

            // Clip to avoid drawing over header/buttons
            canvas.save()
            canvas.clipRect(0f, HEADER_HEIGHT - 40f, sw, sh - 130f)

            for ((index, entry) in scores.withIndex()) {
                val rowTop = HEADER_HEIGHT + index * (ROW_HEIGHT + ROW_MARGIN) - scrollY
                val rowBottom = rowTop + ROW_HEIGHT

                // Skip rows outside visible area
                if (rowBottom < HEADER_HEIGHT - 40f || rowTop > sh) continue

                drawScoreRow(canvas, index, entry, rowTop)
            }

            canvas.restore()
        }

        private fun drawScoreRow(canvas: Canvas, index: Int, entry: ScoreEntry, top: Float) {
            val left = SIDE_PADDING
            val right = sw - SIDE_PADDING
            val rowRect = RectF(left, top, right, top + ROW_HEIGHT)

            // Row background — top 3 get special colors
            val bgColor = when (index) {
                0 -> Color.argb(50, 255, 215, 0)    // Gold tint
                1 -> Color.argb(40, 192, 192, 192)  // Silver tint
                2 -> Color.argb(40, 205, 127, 50)   // Bronze tint
                else -> Color.argb(30, 255, 255, 255)
            }
            rowBgPaint.color = bgColor
            canvas.drawRoundRect(rowRect, 16f, 16f, rowBgPaint)

            // Row border
            val borderColor = when (index) {
                0 -> Color.argb(100, 255, 215, 0)
                1 -> Color.argb(80, 192, 192, 192)
                2 -> Color.argb(80, 205, 127, 50)
                else -> Color.argb(30, 255, 255, 255)
            }
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
                color = borderColor
            }
            canvas.drawRoundRect(rowRect, 16f, 16f, borderPaint)

            // Rank badge
            val rankCenterX = left + 45f
            val rankCenterY = top + ROW_HEIGHT / 2f
            val rankRadius = 24f

            when (index) {
                0 -> {
                    // Gold medal
                    val medalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        shader = RadialGradient(
                            rankCenterX, rankCenterY, rankRadius,
                            Color.rgb(255, 235, 59), goldColor,
                            Shader.TileMode.CLAMP
                        )
                    }
                    canvas.drawCircle(rankCenterX, rankCenterY, rankRadius, medalPaint)
                    rankPaint.color = Color.rgb(120, 80, 0)
                    canvas.drawText("1", rankCenterX, rankCenterY + 14f, rankPaint)
                }
                1 -> {
                    // Silver medal
                    val medalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        shader = RadialGradient(
                            rankCenterX, rankCenterY, rankRadius,
                            Color.rgb(230, 230, 230), silverColor,
                            Shader.TileMode.CLAMP
                        )
                    }
                    canvas.drawCircle(rankCenterX, rankCenterY, rankRadius, medalPaint)
                    rankPaint.color = Color.rgb(80, 80, 80)
                    canvas.drawText("2", rankCenterX, rankCenterY + 14f, rankPaint)
                }
                2 -> {
                    // Bronze medal
                    val medalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        shader = RadialGradient(
                            rankCenterX, rankCenterY, rankRadius,
                            Color.rgb(235, 170, 100), bronzeColor,
                            Shader.TileMode.CLAMP
                        )
                    }
                    canvas.drawCircle(rankCenterX, rankCenterY, rankRadius, medalPaint)
                    rankPaint.color = Color.rgb(100, 60, 20)
                    canvas.drawText("3", rankCenterX, rankCenterY + 14f, rankPaint)
                }
                else -> {
                    // Normal rank number
                    rankPaint.color = Color.argb(180, 189, 195, 199)
                    canvas.drawText("${index + 1}", rankCenterX, rankCenterY + 14f, rankPaint)
                }
            }

            // Player name
            val nameX = left + 90f
            val nameY = top + 42f
            namePaint.color = when (index) {
                0 -> goldColor
                1 -> silverColor
                2 -> bronzeColor
                else -> Color.WHITE
            }
            canvas.drawText(entry.playerName, nameX, nameY, namePaint)

            // Date
            val dateStr = dateFormat.format(Date(entry.timestamp))
            canvas.drawText(dateStr, nameX, nameY + 32f, datePaint)

            // Score (right-aligned)
            val scoreX = right - 20f
            scorePaint.color = when (index) {
                0 -> goldColor
                1 -> silverColor
                2 -> bronzeColor
                else -> Color.rgb(241, 196, 15)
            }
            canvas.drawText("${entry.score}", scoreX, top + ROW_HEIGHT / 2f + 14f, scorePaint)

            // "blocks" label under score
            val blocksLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(100, 189, 195, 199)
                textSize = 18f
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("blocks", scoreX, top + ROW_HEIGHT / 2f + 34f, blocksLabelPaint)
        }

        private fun drawButtons(canvas: Canvas) {
            // Semi-transparent bar behind buttons
            val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, sh - 140f, 0f, sh,
                    Color.argb(0, 15, 12, 41), Color.argb(240, 15, 12, 41),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, sh - 140f, sw, sh, barPaint)

            // Back button
            drawButton(canvas, backBtnRect, "◀  BACK",
                Color.rgb(52, 152, 219), Color.rgb(41, 128, 185))

            // Clear button
            drawButton(canvas, clearBtnRect, "🗑  CLEAR",
                Color.rgb(231, 76, 60), Color.rgb(192, 57, 43))
        }

        private fun drawButton(canvas: Canvas, rect: RectF, text: String, colorTop: Int, colorBot: Int) {
            btnPaint.shader = LinearGradient(
                rect.left, rect.top, rect.left, rect.bottom,
                colorTop, colorBot, Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(rect, 16f, 16f, btnPaint)

            val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = Color.argb(60, 255, 255, 255)
            }
            canvas.drawRoundRect(rect, 16f, 16f, border)

            canvas.drawText(text, rect.centerX(), rect.centerY() + 14f, btnTextPaint)
        }

        // =========================================================
        // TOUCH HANDLING — Scroll + buttons
        // =========================================================
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = true
                    lastTouchY = y
                    scrollVelocity = 0f

                    // Check buttons
                    if (backBtnRect.contains(x, y)) {
                        activity.finish()
                        return true
                    }
                    if (clearBtnRect.contains(x, y)) {
                        ScoreRepository.clearScores()
                        refreshScores()
                        return true
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isDragging) {
                        val delta = lastTouchY - y
                        scrollVelocity = -delta
                        scrollY = (scrollY + delta).coerceIn(0f, maxScrollY)
                        lastTouchY = y
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isDragging = false
                }
            }
            return true
        }
    }
}
