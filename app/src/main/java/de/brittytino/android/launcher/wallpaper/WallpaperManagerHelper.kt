package de.brittytino.android.launcher.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.graphics.ColorUtils
import de.brittytino.android.launcher.preferences.LauncherPreferences
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

object WallpaperManagerHelper {

    private const val KEY_WALLPAPER_DATE = "wallpaper_rotation_date"
    private const val KEY_WALLPAPER_INDEX = "wallpaper_rotation_index" // Kept for legacy compatibility, but logic changes

    fun checkAndRotateWallpaper(context: Context) {
        val prefs = LauncherPreferences.getSharedPreferences()
        val lastDate = prefs.getLong(KEY_WALLPAPER_DATE, 0)
        val today = getStartOfDay()

        if (today != lastDate) {
            // New day, new purely random generation. 
            // We no longer cycle sequentially. Every day is a fresh roll.
            generateAndSetWallpaper(context, today)
            
            prefs.edit()
                .putLong(KEY_WALLPAPER_DATE, today)
                .apply()
        }
    }

    fun forceRegenerate(context: Context) {
        // Force a new seed based on time including current millis for instant variation
        generateAndSetWallpaper(context, System.currentTimeMillis())
    }

    // Compatibility methods for SettingsFragmentLauncher
    fun getCurrentIndex(context: Context): Int {
        // Since we switched to random generation, index is less relevant. 
        // We return 0 or a generic value.
        return 0
    }

    fun getTotalPatterns(): Int {
        // Generative art doesn't have a fixed count, but we can return relative variety.
        return 10
    }

    fun advanceWallpaper(context: Context): Int {
        forceRegenerate(context)
        // Return a random index to simulate change in UI if needed, or just 0
        return (0..9).random()
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

/**
 * The Dynamic Motivational Wallpaper Engine v2
 * "Unapologetic Focus Edition"
 */
private object WallpaperEngine {
    
    data class Palette(val background: Int, val accent1: Int, val accent2: Int, val text: Int, val isDark: Boolean)

    // Families of generative art
    private const val FAMILY_COUNT = 10

    fun generate(canvas: Canvas, w: Int, h: Int, seed: Long) {
        val rng = Random(seed)
        
        // 1. Generate Bold, Attractive Palette
        val palette = generateDynamicPalette(rng)
        
        canvas.drawColor(palette.background)
        
        // 2. Select a Random Modern Style
        // Focus on clean, neat, and bright aesthetics
        val styleIndex = rng.nextInt(3) // Transitioning to fewer, higher quality styles
        
        val paint = Paint().apply { isAntiAlias = true }
        
        when(styleIndex) {
            0 -> drawMeshGradient(canvas, w, h, paint, palette, rng)
            1 -> drawSoftBlobs(canvas, w, h, paint, palette, rng)
            2 -> drawMinimalistGradients(canvas, w, h, paint, palette, rng)
            else -> drawMeshGradient(canvas, w, h, paint, palette, rng)
        }
        
        // 3. Draw Quote Layer - Refreshing and Motivational
        drawRefreshingQuote(canvas, w, h, palette, rng)
        
        // 4. Subtle Texture Overlay
        drawNoise(canvas, w, h, rng, palette.isDark)
    }
    
    private fun drawNoise(c: Canvas, w: Int, h: Int, rng: Random, isDark: Boolean) {
        val paint = Paint()
        paint.color = if (isDark) Color.WHITE else Color.BLACK
        paint.alpha = 5 // Very subtle
        val dens = (w * h) / 1000 // Density
        for(i in 0 until dens) {
            c.drawPoint(rng.nextFloat() * w, rng.nextFloat() * h, paint)
        }
    }

    private fun generateDynamicPalette(rng: Random): Palette {
        // Bright, vibrant, and clean colors
        val hue = rng.nextFloat() * 360f
        
        // High saturation for vibrant look (60-90%)
        val saturation = 0.6f + rng.nextFloat() * 0.3f
        
        // Higher brightness for "neat and clean" feel (0.5 to 0.95)
        val brightness = 0.5f + rng.nextFloat() * 0.45f
        
        val bgInt = Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
        
        // Calculate Text Color based on Luminance for readability
        val lum = ColorUtils.calculateLuminance(bgInt)
        val isDark = lum < 0.6 // Slightly higher threshold for "darkness" to ensure contrast
        val textInt = if (isDark) {
            Color.WHITE
        } else {
            Color.BLACK
        }
        
        // Accents: Harmonious and bright
        val accentHue1 = (hue + 15f + rng.nextFloat() * 30) % 360f // Analogous
        val accentHue2 = (hue + 180f + (rng.nextFloat() * 40 - 20)) % 360f // Complementary
        
        val a1 = Color.HSVToColor(floatArrayOf(accentHue1, saturation * 0.8f, brightness * 0.9f))
        val a2 = Color.HSVToColor(floatArrayOf(accentHue2, saturation * 0.7f, brightness * 1.0f))
        
        return Palette(bgInt, a1, a2, textInt, isDark)
    }

    // --- PATTERNS ---

    private fun drawMeshGradient(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        val count = rng.nextInt(4, 8)
        for (i in 0 until count) {
            val cx = rng.nextFloat() * w
            val cy = rng.nextFloat() * h
            val radius = rng.nextFloat() * w * 1.5f + w * 0.5f
            
            val color = if (rng.nextBoolean()) pal.accent1 else pal.accent2
            val shader = android.graphics.RadialGradient(
                cx, cy, radius,
                color, Color.TRANSPARENT,
                android.graphics.Shader.TileMode.CLAMP
            )
            
            p.shader = shader
            p.alpha = rng.nextInt(100, 200)
            c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
        }
        p.shader = null
    }

    private fun drawSoftBlobs(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        val count = rng.nextInt(3, 6)
        for (i in 0 until count) {
            val cx = rng.nextFloat() * w
            val cy = rng.nextFloat() * h
            val rx = rng.nextFloat() * w * 0.6f + w * 0.2f
            val ry = rng.nextFloat() * h * 0.6f + h * 0.2f
            
            p.color = if (rng.nextBoolean()) pal.accent1 else pal.accent2
            p.alpha = rng.nextInt(40, 100)
            p.style = Paint.Style.FILL
            
            c.save()
            c.rotate(rng.nextFloat() * 360f, cx, cy)
            c.drawOval(cx - rx, cy - ry, cx + rx, cy + ry, p)
            c.restore()
        }
    }

    private fun drawMinimalistGradients(canvas: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        val x1 = rng.nextFloat() * w
        val y1 = rng.nextFloat() * h
        val x2 = rng.nextFloat() * w
        val y2 = rng.nextFloat() * h
        
        val shader = android.graphics.LinearGradient(
            x1, y1, x2, y2,
            pal.accent1, pal.accent2,
            android.graphics.Shader.TileMode.CLAMP
        )
        
        p.shader = shader
        p.alpha = 150
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
        p.shader = null
        
        // Add one subtle line or simple shape
        p.color = pal.text
        p.alpha = 30
        p.strokeWidth = 2f
        p.style = Paint.Style.STROKE
        if (rng.nextBoolean()) {
            canvas.drawCircle(rng.nextFloat() * w, rng.nextFloat() * h, rng.nextFloat() * 300f + 100f, p)
        } else {
            val y = rng.nextFloat() * h
            canvas.drawLine(0f, y, w.toFloat(), y, p)
        }
    }


    // --- QUOTE ENGINE ---
    
    private val QUOTES = listOf(
        "Make today amazing.",
        "Small steps every day.",
        "Believe in the magic of new beginnings.",
        "Your potential is endless.",
        "Focus on the good.",
        "Kindness costs nothing.",
        "Radiate positivity.",
        "Enjoy the little things.",
        "Stay humble, work hard, be kind.",
        "The best is yet to come.",
        "Dream big, stay focused.",
        "Happiness is a choice.",
        "Keep moving forward.",
        "Be the reason someone smiles today.",
        "Growth is a journey, not a destination.",
        "Every day is a fresh start.",
        "Choose joy.",
        "Your only limit is you.",
        "Stay curious.",
        "Cultivate gratitude.",
        "Progress over perfection.",
        "You are doing great.",
        "Listen to your heart.",
        "Embrace the journey.",
        "One day at a time.",
        "Spread love everywhere you go.",
        "Be yourself, everyone else is taken.",
        "The secret of getting ahead is getting started.",
        "Start each day with a grateful heart.",
        "Great things never come from comfort zones.",
        "Do what makes your soul shine.",
        "You got this.",
        "Breathe and enjoy the moment.",
        "Focus on your goals, the rest is noise.",
        "Adventure awaits.",
        "Life is beautiful.",
        "Everything happens for a reason.",
        "Patience is power.",
        "Keep it simple.",
        "Look for the rainbow in every storm."
    )

    private fun drawRefreshingQuote(c: Canvas, w: Int, h: Int, pal: Palette, rng: Random) {
        val quote = QUOTES.random(rng)
        
        val textPaint = TextPaint().apply {
            isAntiAlias = true
            color = pal.text
            textSize = 70f 
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            if (pal.isDark) {
                setShadowLayer(10f, 0f, 0f, Color.argb(100, 0, 0, 0))
            } else {
                setShadowLayer(10f, 0f, 0f, Color.argb(100, 255, 255, 255))
            }
            letterSpacing = 0.05f
        }
        
        // Check fit
        if (w < 900) textPaint.textSize = 60f
        if (quote.length > 40) textPaint.textSize = 50f
        
        val margin = 120
        val maxTextWidth = w - (margin * 2)
        
        val builder = StaticLayout.Builder.obtain(quote, 0, quote.length, textPaint, maxTextWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.2f)
            .setIncludePad(false)
            
        val staticLayout = builder.build()
        val textH = staticLayout.height
        
        // Centered position for a "clean" look
        val x = margin.toFloat()
        val y = (h / 2f) - (textH / 2f)
        
        c.save()
        c.translate(x, y)
        
        // Subtle decorative line above
        val linePaint = Paint().apply { 
            color = pal.text
            alpha = 60
            strokeWidth = 2f 
        }
        c.drawLine(maxTextWidth * 0.4f, -40f, maxTextWidth * 0.6f, -40f, linePaint)

        staticLayout.draw(c)
        c.restore()
    }
}
