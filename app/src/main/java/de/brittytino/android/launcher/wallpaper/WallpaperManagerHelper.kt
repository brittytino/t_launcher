package de.brittytino.android.launcher.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.graphics.ColorUtils
import de.brittytino.android.launcher.preferences.LauncherPreferences
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object WallpaperManagerHelper {

    private const val KEY_WALLPAPER_DATE = "wallpaper_rotation_date"
    private const val KEY_WALLPAPER_INDEX = "wallpaper_rotation_index"

    fun checkAndRotateWallpaper(context: Context) {
        val prefs = LauncherPreferences.getSharedPreferences()
        val lastDate = prefs.getLong(KEY_WALLPAPER_DATE, 0)
        val today = getStartOfDay()

        if (today != lastDate) {
            generateAndSetWallpaper(context, today)
            prefs.edit()
                .putLong(KEY_WALLPAPER_DATE, today)
                .apply()
        }
    }

    fun forceRegenerate(context: Context) {
        generateAndSetWallpaper(context, System.currentTimeMillis())
    }

    fun getCurrentIndex(context: Context): Int {
        return 0
    }

    fun getTotalPatterns(): Int {
        return 8
    }

    fun advanceWallpaper(context: Context): Int {
        forceRegenerate(context)
        return (0..7).random()
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun generateAndSetWallpaper(context: Context, seed: Long) {
        try {
            val metrics = context.resources.displayMetrics
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            WallpaperEngine.generate(canvas, width, height, seed)
            
            WallpaperManager.getInstance(context).setBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private object WallpaperEngine {
    
    data class Palette(
        val background: Int,
        val accent1: Int,
        val accent2: Int,
        val accent3: Int,
        val text: Int,
        val isDark: Boolean
    )

    private const val PATTERN_COUNT = 8

    fun generate(canvas: Canvas, w: Int, h: Int, seed: Long) {
        val rng = Random(seed)
        
        val palette = generateModernPalette(rng)
        canvas.drawColor(palette.background)
        
        val patternIndex = rng.nextInt(PATTERN_COUNT)
        val paint = Paint().apply { isAntiAlias = true }
        
        when(patternIndex) {
            0 -> drawMinimalGradientCircles(canvas, w, h, paint, palette, rng)
            1 -> drawSoftWaves(canvas, w, h, paint, palette, rng)
            2 -> drawCleanGeometry(canvas, w, h, paint, palette, rng)
            3 -> drawGentleDots(canvas, w, h, paint, palette, rng)
            4 -> drawModernArcs(canvas, w, h, paint, palette, rng)
            5 -> drawSoftGrid(canvas, w, h, paint, palette, rng)
            6 -> drawFloatingShapes(canvas, w, h, paint, palette, rng)
            7 -> drawMinimalLines(canvas, w, h, paint, palette, rng)
            else -> drawMinimalGradientCircles(canvas, w, h, paint, palette, rng)
        }
        
        drawInspirationalQuote(canvas, w, h, palette, rng)
        drawSubtleTexture(canvas, w, h, rng, palette.isDark)
    }
    
    private fun drawSubtleTexture(c: Canvas, w: Int, h: Int, rng: Random, isDark: Boolean) {
        val paint = Paint()
        paint.color = if (isDark) Color.WHITE else Color.BLACK
        paint.alpha = 3
        val density = (w * h) / 2000
        for(i in 0 until density) {
            c.drawPoint(rng.nextFloat() * w, rng.nextFloat() * h, paint)
        }
    }

    private fun generateModernPalette(rng: Random): Palette {
        val paletteType = rng.nextInt(6)
        
        val (bg, a1, a2, a3, isDark) = when(paletteType) {
            0 -> { // Deep Ocean
                val bg = Color.rgb(20, 33, 61)
                val a1 = Color.rgb(252, 163, 17)
                val a2 = Color.rgb(229, 80, 57)
                val a3 = Color.rgb(69, 162, 158)
                Tuple5(bg, a1, a2, a3, true)
            }
            1 -> { // Sunset Vibes
                val bg = Color.rgb(255, 94, 77)
                val a1 = Color.rgb(255, 224, 102)
                val a2 = Color.rgb(253, 135, 135)
                val a3 = Color.rgb(255, 159, 67)
                Tuple5(bg, a1, a2, a3, false)
            }
            2 -> { // Purple Night
                val bg = Color.rgb(88, 24, 69)
                val a1 = Color.rgb(199, 0, 57)
                val a2 = Color.rgb(255, 195, 0)
                val a3 = Color.rgb(144, 12, 63)
                Tuple5(bg, a1, a2, a3, true)
            }
            3 -> { // Electric Blue
                val bg = Color.rgb(6, 82, 221)
                val a1 = Color.rgb(255, 107, 107)
                val a2 = Color.rgb(254, 202, 87)
                val a3 = Color.rgb(72, 219, 251)
                Tuple5(bg, a1, a2, a3, true)
            }
            4 -> { // Fresh Green
                val bg = Color.rgb(16, 172, 132)
                val a1 = Color.rgb(255, 211, 42)
                val a2 = Color.rgb(255, 124, 67)
                val a3 = Color.rgb(89, 98, 117)
                Tuple5(bg, a1, a2, a3, true)
            }
            else -> { // Vibrant Magenta
                val bg = Color.rgb(235, 47, 150)
                val a1 = Color.rgb(251, 197, 49)
                val a2 = Color.rgb(131, 56, 236)
                val a3 = Color.rgb(58, 134, 255)
                Tuple5(bg, a1, a2, a3, false)
            }
        }
        
        val lum = ColorUtils.calculateLuminance(bg)
        val textColor = if (lum > 0.5) {
            Color.argb(250, 30, 30, 30)
        } else {
            Color.argb(255, 255, 255, 255)
        }
        
        return Palette(bg, a1, a2, a3, textColor, isDark)
    }

    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)

    // Pattern 1: Minimal Gradient Circles
    private fun drawMinimalGradientCircles(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        val circles = rng.nextInt(4, 8)
        p.style = Paint.Style.FILL
        
        for (i in 0 until circles) {
            val cx = rng.nextFloat() * w
            val cy = rng.nextFloat() * h
            val radius = rng.nextFloat() * 350 + 250
            
            val color = when(i % 3) {
                0 -> pal.accent1
                1 -> pal.accent2
                else -> pal.accent3
            }
            
            p.color = color
            p.alpha = rng.nextInt(60, 110)
            c.drawCircle(cx, cy, radius, p)
        }
    }

    // Pattern 2: Soft Waves
    private fun drawSoftWaves(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        p.style = Paint.Style.FILL
        val waveCount = 5
        
        for (i in 0 until waveCount) {
            val path = Path()
            val yStart = h * (i.toFloat() / waveCount) - 200
            
            path.moveTo(0f, yStart)
            
            for (x in 0..w step 30) {
                val y = yStart + sin(x * 0.008 + i * 2.0) * 120
                path.lineTo(x.toFloat(), y.toFloat())
            }
            
            path.lineTo(w.toFloat(), h.toFloat())
            path.lineTo(0f, h.toFloat())
            path.close()
            
            p.color = when(i % 3) {
                0 -> pal.accent1
                1 -> pal.accent2
                else -> pal.accent3
            }
            p.alpha = 85
            c.drawPath(path, p)
        }
    }

    // Pattern 3: Clean Geometry
    private fun drawCleanGeometry(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        p.style = Paint.Style.FILL
        val shapes = rng.nextInt(6, 12)
        
        for (i in 0 until shapes) {
            val x = rng.nextFloat() * w
            val y = rng.nextFloat() * h
            val size = rng.nextFloat() * 280 + 150
            
            p.color = when(i % 3) {
                0 -> pal.accent1
                1 -> pal.accent2
                else -> pal.accent3
            }
            p.alpha = rng.nextInt(70, 130)
            
            c.save()
            c.translate(x, y)
            c.rotate(rng.nextFloat() * 45)
            
            if (rng.nextBoolean()) {
                c.drawRoundRect(
                    RectF(-size/2, -size/2, size/2, size/2),
                    40f, 40f, p
                )
            } else {
                c.drawCircle(0f, 0f, size/2, p)
            }
            
            c.restore()
        }
    }

    // Pattern 4: Gentle Dots
    private fun drawGentleDots(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        p.style = Paint.Style.FILL
        val spacing = 100
        
        for (x in -spacing until w + spacing step spacing) {
            for (y in -spacing until h + spacing step spacing) {
                if (rng.nextFloat() > 0.35) {
                    val radius = rng.nextFloat() * 50 + 25
                    
                    p.color = when(rng.nextInt(3)) {
                        0 -> pal.accent1
                        1 -> pal.accent2
                        else -> pal.accent3
                    }
                    p.alpha = rng.nextInt(80, 140)
                    
                    val offsetX = rng.nextFloat() * 60 - 30
                    val offsetY = rng.nextFloat() * 60 - 30
                    
                    c.drawCircle(x + offsetX, y + offsetY, radius, p)
                }
            }
        }
    }

    // Pattern 5: Modern Arcs
    private fun drawModernArcs(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 30f
        p.strokeCap = Paint.Cap.ROUND
        
        val arcs = rng.nextInt(8, 15)
        
        for (i in 0 until arcs) {
            val cx = rng.nextFloat() * w
            val cy = rng.nextFloat() * h
            val radius = rng.nextFloat() * 400 + 200
            val startAngle = rng.nextFloat() * 360
            val sweepAngle = rng.nextFloat() * 200 + 80
            
            p.color = when(i % 3) {
                0 -> pal.accent1
                1 -> pal.accent2
                else -> pal.accent3
            }
            p.alpha = rng.nextInt(90, 150)
            
            c.drawArc(
                RectF(cx - radius, cy - radius, cx + radius, cy + radius),
                startAngle, sweepAngle, false, p
            )
        }
    }

    // Pattern 6: Soft Grid
    private fun drawSoftGrid(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 3f
        p.color = pal.accent1
        p.alpha = 60
        
        val spacing = 120
        
        for (x in 0 until w step spacing) {
            c.drawLine(x.toFloat(), 0f, x.toFloat(), h.toFloat(), p)
        }
        
        for (y in 0 until h step spacing) {
            c.drawLine(0f, y.toFloat(), w.toFloat(), y.toFloat(), p)
        }
        
        p.style = Paint.Style.FILL
        val highlights = rng.nextInt(12, 20)
        
        for (i in 0 until highlights) {
            val x = rng.nextFloat() * w
            val y = rng.nextFloat() * h
            
            p.color = when(i % 3) {
                0 -> pal.accent1
                1 -> pal.accent2
                else -> pal.accent3
            }
            p.alpha = rng.nextInt(100, 160)
            
            c.drawCircle(x, y, rng.nextFloat() * 60 + 30, p)
        }
    }

    // Pattern 7: Floating Shapes
    private fun drawFloatingShapes(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        p.style = Paint.Style.FILL
        val shapes = rng.nextInt(8, 16)
        
        for (i in 0 until shapes) {
            c.save()
            
            val x = rng.nextFloat() * w
            val y = rng.nextFloat() * h
            val size = rng.nextFloat() * 200 + 120
            val rotation = rng.nextFloat() * 60
            
            c.translate(x, y)
            c.rotate(rotation)
            
            p.color = when(i % 3) {
                0 -> pal.accent1
                1 -> pal.accent2
                else -> pal.accent3
            }
            p.alpha = rng.nextInt(80, 140)
            
            c.drawRoundRect(
                RectF(-size/2, -size/2, size/2, size/2),
                35f, 35f, p
            )
            
            c.restore()
        }
    }

    // Pattern 8: Minimal Lines
    private fun drawMinimalLines(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        p.style = Paint.Style.STROKE
        p.strokeCap = Paint.Cap.ROUND
        
        val lines = rng.nextInt(10, 18)
        
        for (i in 0 until lines) {
            p.strokeWidth = rng.nextFloat() * 25 + 10
            p.color = when(i % 3) {
                0 -> pal.accent1
                1 -> pal.accent2
                else -> pal.accent3
            }
            p.alpha = rng.nextInt(80, 140)
            
            val x1 = rng.nextFloat() * w
            val y1 = rng.nextFloat() * h
            val length = rng.nextFloat() * 400 + 200
            
            if (rng.nextBoolean()) {
                c.drawLine(x1, y1, x1 + length, y1, p)
            } else {
                c.drawLine(x1, y1, x1, y1 + length, p)
            }
        }
    }

    private val INSPIRATIONAL_QUOTES = listOf(
        "Smile, you’re doing great",
        "Today looks good on you",
        "Soft heart, strong vibes",
        "You’re kind of magical",
        "Confidence is your glow",
        "Take it easy, superstar",
        "You’re someone’s good mood",
        "Chase joy, not stress",
        "You make ordinary cute",
        "Slow down, you’re golden",
        "Pretty calm, pretty capable",
        "You’re allowed to shine",
        "Good things suit you",
        "A little charm goes far",
        "You have main character energy",
        "Gentle progress is still progress",
        "You’re effortlessly cool",
        "Trust yourself, always",
        "You make today brighter",
        "Sweet mind, sharp soul",
        "You’re doing better than you think",
        "Soft smile, strong heart",
        "You bring good vibes",
        "Grace looks great on you",
        "Calm is your superpower",
        "You’re quietly impressive",
        "Warm thoughts only",
        "You glow differently today",
        "Small wins are still wins",
        "You have a lovely presence",
        "Chill, you’ve got this",
        "You’re easy to believe in",
        "Kind looks good on you",
        "You make moments softer",
        "A little sparkle never hurts",
        "You’re someone worth rooting for",
        "Today feels lucky with you in it",
        "You’re beautifully unstoppable",
        "Peace suits you",
        "You’re subtle but powerful",
        "You make effort look cute",
        "Your vibe is comforting",
        "You’re doing just fine",
        "Sweet energy, strong spirit",
        "You’re quietly glowing",
        "You make calm contagious",
        "You’re allowed to take space",
        "You’re charm without trying",
        "Today is on your side"
    )

    private fun drawInspirationalQuote(c: Canvas, w: Int, h: Int, pal: Palette, rng: Random) {
        val quote = INSPIRATIONAL_QUOTES.random(rng)
        
        val textPaint = TextPaint().apply {
            isAntiAlias = true
            color = pal.text
            textSize = 72f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.08f
            if (pal.isDark) {
                setShadowLayer(12f, 0f, 3f, Color.argb(150, 0, 0, 0))
            } else {
                setShadowLayer(10f, 0f, 3f, Color.argb(120, 0, 0, 0))
            }
        }
        
        if (w < 900) textPaint.textSize = 58f
        if (quote.length > 25) textPaint.textSize = 62f
        
        val quadrant = rng.nextInt(4)
        val margin = 100
        val maxTextWidth = w - (margin * 3)
        
        val alignment = when(quadrant) {
            0, 2 -> Layout.Alignment.ALIGN_NORMAL
            else -> Layout.Alignment.ALIGN_OPPOSITE
        }
        
        val builder = StaticLayout.Builder.obtain(quote, 0, quote.length, textPaint, maxTextWidth)
            .setAlignment(alignment)
            .setLineSpacing(8f, 1.15f)
            .setIncludePad(false)
            
        val staticLayout = builder.build()
        val textHeight = staticLayout.height
        
        var x = margin.toFloat()
        var y = margin.toFloat()
        
        when(quadrant) {
            0 -> {
                y = (margin * 2.5f) + 100
            }
            1 -> {
                y = (margin * 2.5f) + 100
            }
            2 -> {
                y = (h - textHeight - margin * 2.5f).toFloat()
            }
            3 -> {
                y = (h - textHeight - margin * 2.5f).toFloat()
            }
        }
        
        c.save()
        c.translate(x, y)
        
        val accentBarPaint = Paint().apply {
            color = pal.accent1
            strokeWidth = 8f
            style = Paint.Style.FILL
        }
        
        val barWidth = 80f
        val barHeight = textHeight + 30f
        
        when(quadrant) {
            0, 2 -> {
                c.drawRoundRect(
                    RectF(-40f, -15f, -40f + barWidth, -15f + barHeight),
                    4f, 4f, accentBarPaint
                )
            }
            1, 3 -> {
                c.drawRoundRect(
                    RectF(maxTextWidth + 40f - barWidth, -15f, maxTextWidth + 40f, -15f + barHeight),
                    4f, 4f, accentBarPaint
                )
            }
        }
        
        staticLayout.draw(c)
        c.restore()
    }
}