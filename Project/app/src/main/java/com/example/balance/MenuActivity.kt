package com.example.balance

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

/**
 * ==========================================================
 * MenuActivity.kt — Main menu screen (launcher activity)
 * ==========================================================
 *
 * Displays a beautiful Canvas-drawn menu matching the game's
 * aesthetic. Features:
 *   - Animated star background
 *   - Game title "BALANCE TOWER" with glow effect
 *   - "START GAME" button
 *   - "SETTINGS" button
 *   - Settings overlay with sliders for SFX, BGM, Sensitivity
 *
 * The menu is drawn on a custom View using Canvas, keeping
 * the same visual style as the game itself.
 */
class MenuActivity : Activity() {

    private lateinit var menuView: MenuView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize settings
        GameSettings.init(this)

        menuView = MenuView(this)
        setContentView(menuView)
    }

    override fun onResume() {
        super.onResume()
        menuView.resumeAnimation()
    }

    override fun onPause() {
        super.onPause()
        menuView.pauseAnimation()
    }

    fun startGame() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun openScoreboard() {
        val intent = Intent(this, ScoreboardActivity::class.java)
        startActivity(intent)
    }

    // =========================================================
    // MenuView — Custom Canvas-drawn menu
    // =========================================================
    inner class MenuView(private val activity: MenuActivity) : View(activity), Runnable {

        private var animThread: Thread? = null
        @Volatile private var running = false
        private var animTime = 0f

        // Screen metrics
        private var sw = 0f
        private var sh = 0f

        // Button rectangles
        private val startBtnRect = RectF()
        private val scoreboardBtnRect = RectF()
        private val settingsBtnRect = RectF()
        private val backBtnRect = RectF()

        // Settings state
        private var showSettings = false

        // Slider rects and values
        private val sfxSliderRect = RectF()
        private val bgmSliderRect = RectF()
        private val sensSliderRect = RectF()
        private var draggingSlider: String? = null  // "sfx", "bgm", "sens", or null

        // Stars (fixed positions)
        private val starRng = Random(77)

        private var stars: List<Star> = emptyList()

        // ===== Paints =====
        private val bgPaint = Paint()
        private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 100f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(20f, 0f, 0f, Color.rgb(155, 89, 182))
        }
        private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(189, 195, 199)
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        private val btnPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val btnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(200, 10, 8, 30)
        }
        private val settingsTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 64f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(8f, 0f, 0f, Color.rgb(52, 152, 219))
        }
        private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(189, 195, 199)
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 36f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        private val sliderBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(80, 255, 255, 255)
        }
        private val sliderFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val sliderKnobPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            setShadowLayer(6f, 0f, 0f, Color.WHITE)
        }
        private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }

        // Decorative tower blocks for the menu

        private var menuBlocks: List<MenuBlock> = emptyList()

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            sw = w.toFloat()
            sh = h.toFloat()

            // Button positions
            val btnW = 320f
            val btnH = 80f
            val centerX = sw / 2f
            val startY = sh * 0.50f

            startBtnRect.set(centerX - btnW / 2, startY, centerX + btnW / 2, startY + btnH)
            scoreboardBtnRect.set(centerX - btnW / 2, startY + 110f, centerX + btnW / 2, startY + 110f + btnH)
            settingsBtnRect.set(centerX - btnW / 2, startY + 220f, centerX + btnW / 2, startY + 220f + btnH)

            // Back button (for settings)
            backBtnRect.set(centerX - btnW / 2, sh * 0.82f, centerX + btnW / 2, sh * 0.82f + btnH)

            // Slider positions
            val sliderLeft = sw * 0.1f
            val sliderRight = sw * 0.9f
            val sliderH = 20f
            val settingsBaseY = sh * 0.32f

            sfxSliderRect.set(sliderLeft, settingsBaseY, sliderRight, settingsBaseY + sliderH)
            bgmSliderRect.set(sliderLeft, settingsBaseY + 130f, sliderRight, settingsBaseY + 130f + sliderH)
            sensSliderRect.set(sliderLeft, settingsBaseY + 260f, sliderRight, settingsBaseY + 260f + sliderH)

            // Generate stars
            stars = (0 until 80).map {
                Star(
                    starRng.nextFloat() * sw,
                    starRng.nextFloat() * sh,
                    starRng.nextFloat() * 2.5f + 0.5f,
                    starRng.nextFloat() * 6.28f
                )
            }

            // Generate decorative tower blocks
            val blockColors = intArrayOf(
                Color.rgb(231, 76, 60), Color.rgb(230, 126, 34),
                Color.rgb(241, 196, 15), Color.rgb(46, 204, 113),
                Color.rgb(52, 152, 219), Color.rgb(155, 89, 182)
            )
            val towerX = sw * 0.5f
            val groundLevel = sh * 0.88f
            menuBlocks = (0 until 5).map { i ->
                val bw = 100f + Random.nextFloat() * 80f
                MenuBlock(
                    towerX - bw / 2f + Random.nextFloat() * 20f - 10f,
                    groundLevel - (i + 1) * 45f,
                    bw, 40f,
                    blockColors[i % blockColors.size]
                )
            }
        }

        fun resumeAnimation() {
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
                postInvalidate()
                try { Thread.sleep(16) } catch (_: Exception) {}
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (sw == 0f) return

            drawBackground(canvas)

            if (showSettings) {
                drawSettingsScreen(canvas)
            } else {
                drawMainMenu(canvas)
            }
        }

        private fun drawBackground(canvas: Canvas) {
            // Gradient sky
            val grad = LinearGradient(
                0f, 0f, 0f, sh,
                Color.rgb(15, 12, 41), Color.rgb(48, 43, 99),
                Shader.TileMode.CLAMP
            )
            bgPaint.shader = grad
            canvas.drawRect(0f, 0f, sw, sh, bgPaint)

            // Twinkling stars
            for (star in stars) {
                val twinkle = 0.5f + 0.5f * sin(animTime * 2f + star.twinkleOffset).toFloat()
                starPaint.alpha = (twinkle * 220).toInt()
                canvas.drawCircle(star.x, star.y, star.size * twinkle, starPaint)
            }

            // Ground
            val groundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, sh * 0.88f, 0f, sh,
                    Color.rgb(44, 62, 80), Color.rgb(30, 40, 50),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, sh * 0.88f, sw, sh, groundPaint)

            // Ground line
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(46, 204, 113)
                strokeWidth = 3f
            }
            canvas.drawLine(0f, sh * 0.88f, sw, sh * 0.88f, linePaint)

            // Decorative tower blocks (wobble slightly)
            for ((i, b) in menuBlocks.withIndex()) {
                val wobble = sin(animTime * 1.5f + i * 0.5f).toFloat() * 1.5f
                canvas.save()
                canvas.rotate(wobble, b.x + b.w / 2, b.y + b.h)
                val bPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = b.color }
                canvas.drawRoundRect(b.x, b.y, b.x + b.w, b.y + b.h, 6f, 6f, bPaint)
                // Highlight
                val hlPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        b.x, b.y, b.x, b.y + b.h * 0.4f,
                        Color.argb(60, 255, 255, 255), Color.argb(0, 255, 255, 255),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRoundRect(b.x, b.y, b.x + b.w, b.y + b.h, 6f, 6f, hlPaint)
                canvas.restore()
            }
        }

        private fun drawMainMenu(canvas: Canvas) {
            // Title with bounce effect
            val titleY = sh * 0.25f + sin(animTime * 1.2f).toFloat() * 8f
            titlePaint.textSize = 90f
            canvas.drawText("BALANCE", sw / 2f, titleY, titlePaint)
            titlePaint.textSize = 100f
            canvas.drawText("TOWER", sw / 2f, titleY + 90f, titlePaint)

            // Subtitle
            canvas.drawText("Stack · Balance · Survive", sw / 2f, titleY + 140f, subtitlePaint)

            // START GAME button
            drawButton(canvas, startBtnRect, "START GAME",
                Color.rgb(46, 204, 113), Color.rgb(39, 174, 96))

            // SCOREBOARD button
            drawButton(canvas, scoreboardBtnRect, "SCOREBOARD",
                Color.rgb(241, 196, 15), Color.rgb(211, 166, 0))

            // SETTINGS button
            drawButton(canvas, settingsBtnRect, "SETTINGS",
                Color.rgb(52, 152, 219), Color.rgb(41, 128, 185))
        }

        private fun drawButton(canvas: Canvas, rect: RectF, text: String, colorTop: Int, colorBot: Int) {
            // Button gradient
            btnPaint.shader = LinearGradient(
                rect.left, rect.top, rect.left, rect.bottom,
                colorTop, colorBot, Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(rect, 16f, 16f, btnPaint)

            // Button border glow
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = Color.argb(60, 255, 255, 255)
            }
            canvas.drawRoundRect(rect, 16f, 16f, borderPaint)

            // Button text
            canvas.drawText(text, rect.centerX(), rect.centerY() + 16f, btnTextPaint)
        }

        // =========================================================
        // SETTINGS SCREEN
        // =========================================================
        private fun drawSettingsScreen(canvas: Canvas) {
            // Dark overlay
            canvas.drawRect(0f, 0f, sw, sh, overlayPaint)

            // Title
            canvas.drawText("⚙ SETTINGS", sw / 2f, sh * 0.18f, settingsTitlePaint)

            // === SFX Volume ===
            val sfxY = sfxSliderRect.top
            canvas.drawText("SFX Volume", sfxSliderRect.left, sfxY - 20f, labelPaint)
            canvas.drawText("${GameSettings.sfxVolume}%", sfxSliderRect.right, sfxY - 20f, valuePaint)
            drawSlider(canvas, sfxSliderRect, GameSettings.sfxVolume / 100f,
                Color.rgb(46, 204, 113))

            // === BGM Volume ===
            val bgmY = bgmSliderRect.top
            canvas.drawText("BGM Volume", bgmSliderRect.left, bgmY - 20f, labelPaint)
            canvas.drawText("${GameSettings.bgmVolume}%", bgmSliderRect.right, bgmY - 20f, valuePaint)
            drawSlider(canvas, bgmSliderRect, GameSettings.bgmVolume / 100f,
                Color.rgb(52, 152, 219))

            // === Gyro Sensitivity ===
            val sensY = sensSliderRect.top
            canvas.drawText("Gyro Sensitivity", sensSliderRect.left, sensY - 20f, labelPaint)
            // Map 0.5–4.0 to display
            val sensDisplay = String.format("%.1f", GameSettings.gyroSensitivity)
            canvas.drawText(sensDisplay, sensSliderRect.right, sensY - 20f, valuePaint)
            // Normalize: 0.5→0.0, 4.0→1.0
            val sensNorm = (GameSettings.gyroSensitivity - 0.5f) / 3.5f
            drawSlider(canvas, sensSliderRect, sensNorm,
                Color.rgb(155, 89, 182))

            // Description text
            val descPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(120, 255, 255, 255)
                textSize = 24f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                "Higher sensitivity = faster block movement with tilt",
                sw / 2f, sensSliderRect.bottom + 40f, descPaint
            )

            // BACK button
            drawButton(canvas, backBtnRect, "✓  DONE",
                Color.rgb(231, 76, 60), Color.rgb(192, 57, 43))
        }

        private fun drawSlider(canvas: Canvas, rect: RectF, fraction: Float, fillColor: Int) {
            val f = fraction.coerceIn(0f, 1f)

            // Background track
            canvas.drawRoundRect(rect, rect.height() / 2, rect.height() / 2, sliderBgPaint)

            // Filled portion
            val fillRect = RectF(rect.left, rect.top, rect.left + rect.width() * f, rect.bottom)
            sliderFillPaint.color = fillColor
            canvas.drawRoundRect(fillRect, rect.height() / 2, rect.height() / 2, sliderFillPaint)

            // Knob
            val knobX = rect.left + rect.width() * f
            val knobY = rect.centerY()
            canvas.drawCircle(knobX, knobY, 18f, sliderKnobPaint)

            // Inner circle
            val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fillColor }
            canvas.drawCircle(knobX, knobY, 10f, innerPaint)
        }

        // =========================================================
        // TOUCH HANDLING
        // =========================================================
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (showSettings) {
                        // Check slider touches
                        if (isNearSlider(sfxSliderRect, x, y)) {
                            draggingSlider = "sfx"
                            updateSlider("sfx", x)
                        } else if (isNearSlider(bgmSliderRect, x, y)) {
                            draggingSlider = "bgm"
                            updateSlider("bgm", x)
                        } else if (isNearSlider(sensSliderRect, x, y)) {
                            draggingSlider = "sens"
                            updateSlider("sens", x)
                        } else if (backBtnRect.contains(x, y)) {
                            showSettings = false
                        }
                    } else {
                        if (startBtnRect.contains(x, y)) {
                            activity.startGame()
                        } else if (scoreboardBtnRect.contains(x, y)) {
                            activity.openScoreboard()
                        } else if (settingsBtnRect.contains(x, y)) {
                            showSettings = true
                        }
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    draggingSlider?.let { updateSlider(it, x) }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    draggingSlider = null
                }
            }
            return true
        }

        private fun isNearSlider(rect: RectF, x: Float, y: Float): Boolean {
            // Allow touching 30px above and below the slider for easier interaction
            return x >= rect.left - 20f && x <= rect.right + 20f &&
                    y >= rect.top - 30f && y <= rect.bottom + 30f
        }

        private fun updateSlider(slider: String, x: Float) {
            val rect = when (slider) {
                "sfx" -> sfxSliderRect
                "bgm" -> bgmSliderRect
                "sens" -> sensSliderRect
                else -> return
            }
            val fraction = ((x - rect.left) / rect.width()).coerceIn(0f, 1f)

            when (slider) {
                "sfx" -> GameSettings.sfxVolume = (fraction * 100).toInt()
                "bgm" -> GameSettings.bgmVolume = (fraction * 100).toInt()
                "sens" -> {
                    // Map 0.0–1.0 → 0.5–4.0
                    GameSettings.gyroSensitivity = 0.5f + fraction * 3.5f
                }
            }
            postInvalidate()
        }
    }
}

private class Star(val x: Float, val y: Float, val size: Float, val twinkleOffset: Float)
private class MenuBlock(val x: Float, val y: Float, val w: Float, val h: Float, val color: Int)
