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
        val paletteType = rng.nextInt(5)
        
        val (bg, a1, a2, a3, isDark) = when(paletteType) {
            0 -> { // Warm sunset
                val bg = Color.rgb(255, 159, 122)
                val a1 = Color.rgb(255, 205, 148)
                val a2 = Color.rgb(255, 236, 179)
                val a3 = Color.rgb(252, 182, 159)
                Tuple5(bg, a1, a2, a3, false)
            }
            1 -> { // Ocean breeze
                val bg = Color.rgb(135, 206, 235)
                val a1 = Color.rgb(173, 216, 230)
                val a2 = Color.rgb(176, 224, 230)
                val a3 = Color.rgb(100, 149, 237)
                Tuple5(bg, a1, a2, a3, false)
            }
            2 -> { // Fresh mint
                val bg = Color.rgb(152, 251, 152)
                val a1 = Color.rgb(144, 238, 144)
                val a2 = Color.rgb(173, 255, 173)
                val a3 = Color.rgb(119, 221, 119)
                Tuple5(bg, a1, a2, a3, false)
            }
            3 -> { // Lavender dream
                val bg = Color.rgb(230, 190, 255)
                val a1 = Color.rgb(216, 191, 216)
                val a2 = Color.rgb(221, 160, 221)
                val a3 = Color.rgb(238, 210, 238)
                Tuple5(bg, a1, a2, a3, false)
            }
            else -> { // Golden hour
                val bg = Color.rgb(255, 218, 121)
                val a1 = Color.rgb(255, 239, 186)
                val a2 = Color.rgb(255, 228, 148)
                val a3 = Color.rgb(255, 204, 92)
                Tuple5(bg, a1, a2, a3, false)
            }
        }
        
        val lum = ColorUtils.calculateLuminance(bg)
        val textColor = if (lum > 0.5) {
            Color.argb(240, 40, 40, 40)
        } else {
            Color.argb(250, 255, 255, 255)
        }
        
        return Palette(bg, a1, a2, a3, textColor, isDark)
    }

    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)

    // Pattern 1: Minimal Gradient Circles
    private fun drawMinimalGradientCircles(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        val circles = rng.nextInt(3, 6)
        p.style = Paint.Style.FILL
        
        for (i in 0 until circles) {
            val cx = rng.nextFloat() * w
            val cy = rng.nextFloat() * h
            val radius = rng.nextFloat() * 300 + 200
            
            val color = when(i % 3) {
                0 -> pal.accent1
                1 -> pal.accent2
                else -> pal.accent3
            }
            
            p.color = color
            p.alpha = rng.nextInt(40, 80)
            c.drawCircle(cx, cy, radius, p)
        }
    }

    // Pattern 2: Soft Waves
    private fun drawSoftWaves(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        p.style = Paint.Style.FILL
        val waveCount = 4
        
        for (i in 0 until waveCount) {
            val path = Path()
            val yStart = h * (i.toFloat() / waveCount)
            
            path.moveTo(0f, yStart)
            
            for (x in 0..w step 50) {
                val y = yStart + sin(x * 0.01 + i * 1.5) * 80
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
            p.alpha = 60
            c.drawPath(path, p)
        }
    }

    // Pattern 3: Clean Geometry
    private fun drawCleanGeometry(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        p.style = Paint.Style.FILL
        val shapes = rng.nextInt(4, 8)
        
        for (i in 0 until shapes) {
            val x = rng.nextFloat() * w
            val y = rng.nextFloat() * h
            val size = rng.nextFloat() * 200 + 100
            
            p.color = when(i % 3) {
                0 -> pal.accent1
                1 -> pal.accent2
                else -> pal.accent3
            }
            p.alpha = rng.nextInt(50, 90)
            
            if (rng.nextBoolean()) {
                c.drawRoundRect(
                    RectF(x - size/2, y - size/2, x + size/2, y + size/2),
                    30f, 30f, p
                )
            } else {
                c.drawCircle(x, y, size/2, p)
            }
        }
    }

    // Pattern 4: Gentle Dots
    private fun drawGentleDots(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        p.style = Paint.Style.FILL
        val spacing = 120
        
        for (x in 0 until w step spacing) {
            for (y in 0 until h step spacing) {
                if (rng.nextFloat() > 0.4) {
                    val radius = rng.nextFloat() * 30 + 10
                    
                    p.color = when(rng.nextInt(3)) {
                        0 -> pal.accent1
                        1 -> pal.accent2
                        else -> pal.accent3
                    }
                    p.alpha = rng.nextInt(60, 100)
                    
                    val offsetX = rng.nextFloat() * 40 - 20
                    val offsetY = rng.nextFloat() * 40 - 20
                    
                    c.drawCircle(x + offsetX, y + offsetY, radius, p)
                }
            }
        }
    }

    // Pattern 5: Modern Arcs
    private fun drawModernArcs(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 20f
        p.strokeCap = Paint.Cap.ROUND
        
        val arcs = rng.nextInt(5, 10)
        
        for (i in 0 until arcs) {
            val cx = rng.nextFloat() * w
            val cy = rng.nextFloat() * h
            val radius = rng.nextFloat() * 300 + 100
            val startAngle = rng.nextFloat() * 360
            val sweepAngle = rng.nextFloat() * 180 + 60
            
            p.color = when(i % 3) {
                0 -> pal.accent1
                1 -> pal.accent2
                else -> pal.accent3
            }
            p.alpha = rng.nextInt(70, 120)
            
            c.drawArc(
                RectF(cx - radius, cy - radius, cx + radius, cy + radius),
                startAngle, sweepAngle, false, p
            )
        }
    }

    // Pattern 6: Soft Grid
    private fun drawSoftGrid(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 2f
        p.color = pal.accent1
        p.alpha = 30
        
        val spacing = 150
        
        for (x in 0 until w step spacing) {
            c.drawLine(x.toFloat(), 0f, x.toFloat(), h.toFloat(), p)
        }
        
        for (y in 0 until h step spacing) {
            c.drawLine(0f, y.toFloat(), w.toFloat(), y.toFloat(), p)
        }
        
        p.style = Paint.Style.FILL
        val highlights = rng.nextInt(8, 15)
        
        for (i in 0 until highlights) {
            val x = rng.nextFloat() * w
            val y = rng.nextFloat() * h
            
            p.color = when(i % 3) {
                0 -> pal.accent1
                1 -> pal.accent2
                else -> pal.accent3
            }
            p.alpha = rng.nextInt(50, 90)
            
            c.drawCircle(x, y, rng.nextFloat() * 40 + 20, p)
        }
    }

    // Pattern 7: Floating Shapes
    private fun drawFloatingShapes(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        p.style = Paint.Style.FILL
        val shapes = rng.nextInt(6, 12)
        
        for (i in 0 until shapes) {
            c.save()
            
            val x = rng.nextFloat() * w
            val y = rng.nextFloat() * h
            val size = rng.nextFloat() * 150 + 80
            val rotation = rng.nextFloat() * 45
            
            c.translate(x, y)
            c.rotate(rotation)
            
            p.color = when(i % 3) {
                0 -> pal.accent1
                1 -> pal.accent2
                else -> pal.accent3
            }
            p.alpha = rng.nextInt(40, 80)
            
            c.drawRoundRect(
                RectF(-size/2, -size/2, size/2, size/2),
                25f, 25f, p
            )
            
            c.restore()
        }
    }

    // Pattern 8: Minimal Lines
    private fun drawMinimalLines(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        p.style = Paint.Style.STROKE
        p.strokeCap = Paint.Cap.ROUND
        
        val lines = rng.nextInt(8, 15)
        
        for (i in 0 until lines) {
            p.strokeWidth = rng.nextFloat() * 15 + 5
            p.color = when(i % 3) {
                0 -> pal.accent1
                1 -> pal.accent2
                else -> pal.accent3
            }
            p.alpha = rng.nextInt(50, 90)
            
            val x1 = rng.nextFloat() * w
            val y1 = rng.nextFloat() * h
            val x2 = rng.nextFloat() * w
            val y2 = rng.nextFloat() * h
            
            c.drawLine(x1, y1, x2, y2, p)
        }
    }

    private val INSPIRATIONAL_QUOTES = listOf(
        "Today is a new beginning",
        "Believe in yourself",
        "Make today amazing",
        "Choose joy",
        "You are capable of amazing things",
        "Dream big, work hard",
        "Be kind to yourself",
        "Progress over perfection",
        "Stay positive",
        "Embrace the journey",
        "You are enough",
        "Create your own sunshine",
        "Be the light",
        "Small steps every day",
        "Breathe and believe",
        "Trust the process",
        "Keep growing",
        "You've got this",
        "Stay focused",
        "Be present",
        "Choose gratitude",
        "Find your balance",
        "Celebrate small wins",
        "Stay curious",
        "Be authentic",
        "Spread kindness",
        "Take it one day at a time",
        "Your best is enough",
        "Keep moving forward",
        "Appreciate this moment"
    )

    private fun drawInspirationalQuote(c: Canvas, w: Int, h: Int, pal: Palette, rng: Random) {
        val quote = INSPIRATIONAL_QUOTES.random(rng)
        
        val textPaint = TextPaint().apply {
            isAntiAlias = true
            color = pal.text
            textSize = 60f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            letterSpacing = 0.05f
            if (pal.isDark) {
                setShadowLayer(10f, 0f, 2f, Color.argb(100, 0, 0, 0))
            } else {
                setShadowLayer(8f, 0f, 2f, Color.argb(80, 255, 255, 255))
            }
        }
        
        if (w < 900) textPaint.textSize = 48f
        if (quote.length > 25) textPaint.textSize = 52f
        
        val margin = 80
        val maxTextWidth = w - (margin * 2)
        
        val builder = StaticLayout.Builder.obtain(quote, 0, quote.length, textPaint, maxTextWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.2f)
            .setIncludePad(false)
            
        val staticLayout = builder.build()
        val textHeight = staticLayout.height
        
        val y = (h - textHeight) / 2f
        val x = margin.toFloat()
        
        c.save()
        c.translate(x, y)
        staticLayout.draw(c)
        c.restore()
    }
}