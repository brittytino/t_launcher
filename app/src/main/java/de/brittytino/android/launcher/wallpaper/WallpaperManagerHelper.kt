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
        
        // 2. Select a Random Geometric Family
        // We select purely based on RNG, ensuring uniqueness every time
        val familyIndex = rng.nextInt(FAMILY_COUNT)
        
        val paint = Paint().apply { isAntiAlias = true }
        
        when(familyIndex) {
            0 -> drawBauhaus(canvas, w, h, paint, palette, rng)
            1 -> drawCyberGrid(canvas, w, h, paint, palette, rng)
            2 -> drawHexagonHive(canvas, w, h, paint, palette, rng)
            3 -> drawIsometricBlocks(canvas, w, h, paint, palette, rng)
            4 -> drawRadialFracture(canvas, w, h, paint, palette, rng)
            5 -> drawBrutalistLines(canvas, w, h, paint, palette, rng)
            6 -> drawWaveInterference(canvas, w, h, paint, palette, rng)
            7 -> drawModularSquares(canvas, w, h, paint, palette, rng)
            8 -> drawAbstractShards(canvas, w, h, paint, palette, rng)
            9 -> drawMixedBag(canvas, w, h, paint, palette, rng) // New mixed mode
            else -> drawBauhaus(canvas, w, h, paint, palette, rng)
        }
        
        // 3. Draw Quote Layer - "No Brainrot"
        drawHardHittingQuote(canvas, w, h, palette, rng)
        
        // 4. Texture Overlay for polish
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
        // "Attractive bg color like red, orange, yellow"
        // We use HSV to control saturation and brightness while allowing random hues.
        
        val hue = rng.nextFloat() * 360f
        
        // High saturation for "Attractive" look (70-95%)
        val saturation = 0.7f + rng.nextFloat() * 0.25f
        
        // Brightness: Avoid pure black/white to look "designed". 
        // 0.2 to 0.8 range.
        val brightness = 0.2f + rng.nextFloat() * 0.6f
        
        val bgInt = Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
        
        // Calculate Text Color based on Luminance for readability
        val lum = ColorUtils.calculateLuminance(bgInt)
        val isDark = lum < 0.5
        val textInt = if (isDark) {
            Color.argb(240, 255, 255, 255) // White-ish
        } else {
            Color.argb(230, 20, 20, 20) // Black-ish
        }
        
        // Accents: 
        // Triadic or Complementary to ensure they "match"
        val accentHue1 = (hue + 120f + (rng.nextFloat() * 20 - 10)) % 360f // Triadic variation
        val accentHue2 = (hue + 240f + (rng.nextFloat() * 20 - 10)) % 360f
        
        val a1 = Color.HSVToColor(floatArrayOf(accentHue1, 0.9f, 0.9f))
        val a2 = Color.HSVToColor(floatArrayOf(accentHue2, 0.8f, 0.95f))
        
        return Palette(bgInt, a1, a2, textInt, isDark)
    }

    // --- PATTERNS ---

    private fun drawBauhaus(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        val count = rng.nextInt(3, 8)
        for (i in 0 until count) {
            p.color = if (rng.nextBoolean()) pal.accent1 else pal.accent2
            p.alpha = rng.nextInt(80, 200)
            p.style = Paint.Style.FILL
            
            val shape = rng.nextInt(3)
            val cx = rng.nextFloat() * w
            val cy = rng.nextFloat() * h
            val size = rng.nextFloat() * (w / 2) + 100
            
            when(shape) {
                0 -> c.drawCircle(cx, cy, size / 2, p)
                1 -> { // Rect with potential rotation
                    c.save()
                    c.rotate(rng.nextFloat() * 90, cx, cy)
                    c.drawRect(cx - size/2, cy - size/2, cx + size/2, cy + size/2, p)
                    c.restore()
                }
                2 -> { // Arch
                    c.drawArc(RectF(cx - size, cy - size, cx + size, cy + size), 0f, 180f, true, p)
                }
            }
        }
    }
    
    private fun drawMixedBag(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        // Combination of lines and shapes for maximum uniqueness
        drawCyberGrid(c, w, h, p, pal, rng) // Base
        drawAbstractShards(c, w, h, p, pal, rng) // Overlay
    }

    private fun drawCyberGrid(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        p.strokeWidth = 3f
        p.color = pal.accent1
        p.alpha = 50
        p.style = Paint.Style.STROKE
        
        val gridSize = 120f
        // Perspective twist?
        c.save()
        // c.rotate(10f, w/2f, h/2f) // Maybe too dizzying
        
        for (x in -200..w+200 step gridSize.toInt()) {
            c.drawLine(x.toFloat(), 0f, x.toFloat(), h.toFloat(), p)
        }
        for (y in -200..h+200 step gridSize.toInt()) {
             c.drawLine(0f, y.toFloat(), w.toFloat(), y.toFloat(), p)
        }
        c.restore()
        
        // Glitch Blocks
        p.style = Paint.Style.FILL
        for (i in 0 until 12) {
            p.color = if (rng.nextBoolean()) pal.accent2 else pal.accent1
            p.alpha = rng.nextInt(120, 220)
            val bw = rng.nextInt(50, 400).toFloat()
            val bh = rng.nextInt(20, 80).toFloat()
            val bx = rng.nextFloat() * w
            val by = rng.nextFloat() * h
            c.drawRect(bx, by, bx + bw, by + bh, p)
        }
    }

    private fun drawHexagonHive(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        val size = rng.nextInt(70, 180).toFloat()
        val width = sqrt(3.0) * size
        val height = 2 * size
        val xStep = width
        val yStep = height * 0.75
        
        var row = 0
        for (y in -200..h + 200 step yStep.toInt()) {
            val offset = if (row % 2 == 1) width / 2 else 0.0
            for (x in -200..w + 200 step xStep.toInt()) {
                if (rng.nextFloat() > 0.65) continue 
                
                val cx = x + offset
                val cy = y.toDouble()
                
                // Fill
                p.style = Paint.Style.FILL
                p.color = ColorUtils.blendARGB(pal.background, pal.accent1, 0.1f)
                drawHexagon(c, cx.toFloat(), cy.toFloat(), size * 0.95f, p)
                
                // Stroke
                p.style = Paint.Style.STROKE
                p.strokeWidth = 4f
                p.color = pal.accent1
                p.alpha = 80
                drawHexagon(c, cx.toFloat(), cy.toFloat(), size * 0.95f, p)
                
                // Random Accent
                if (rng.nextFloat() > 0.9) {
                    p.style = Paint.Style.FILL
                    p.color = pal.accent2
                    p.alpha = 200
                    drawHexagon(c, cx.toFloat(), cy.toFloat(), size * 0.95f, p)
                }
            }
            row++
        }
    }
    
    private fun drawHexagon(c: Canvas, x: Float, y: Float, radius: Float, p: Paint) {
        val path = Path()
        for (i in 0 until 6) {
            val angle = Math.toRadians((60 * i - 30).toDouble())
            val px = x + radius * cos(angle).toFloat()
            val py = y + radius * sin(angle).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        c.drawPath(path, p)
    }

    private fun drawIsometricBlocks(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
         val count = rng.nextInt(20, 50)
         for (i in 0 until count) {
             val x = rng.nextFloat() * w
             val y = rng.nextFloat() * h
             val s = rng.nextFloat() * 120 + 40
             
             drawCube(c, x, y, s, p, pal, rng)
         }
    }
    
    private fun drawCube(c: Canvas, x: Float, y: Float, size: Float, p: Paint, pal: Palette, rng: Random) {
        // Top
        val pathTop = Path().apply {
            moveTo(x, y)
            lineTo(x + size, y - size/2)
            lineTo(x + 2*size, y)
            lineTo(x + size, y + size/2)
            close()
        }
        p.style = Paint.Style.FILL
        p.color = pal.accent1
        p.alpha = 220
        c.drawPath(pathTop, p)
        
        // Right
        val pathRight = Path().apply {
            moveTo(x + size, y + size/2)
            lineTo(x + 2*size, y)
            lineTo(x + 2*size, y + size)
            lineTo(x + size, y + size * 1.5f)
            close()
        }
        p.color = pal.accent2
        p.alpha = 180
        c.drawPath(pathRight, p)
        
        // Left - Darker
        val pathLeft = Path().apply {
             moveTo(x, y)
             lineTo(x + size, y + size/2)
             lineTo(x + size, y + size * 1.5f)
             lineTo(x, y + size)
             close()
        }
        p.color = Color.BLACK 
        p.alpha = 80
        c.drawPath(pathLeft, p)
    }

    private fun drawRadialFracture(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        val cx = w / 2f
        val cy = h / 2f
        val maxR = max(w, h).toFloat()
        val spikes = rng.nextInt(12, 40)
        
        p.style = Paint.Style.FILL
        for (i in 0 until spikes) {
            val angleStart = (360f / spikes) * i
            val angleSwivel = (360f / spikes)
            
            p.color = if (i % 2 == 0) pal.accent1 else ColorUtils.blendARGB(pal.background, Color.BLACK, 0.2f)
            if (rng.nextBoolean()) p.color = pal.accent2
            p.alpha = rng.nextInt(40, 180)
            
            c.drawArc(
                RectF(cx - maxR, cy - maxR, cx + maxR, cy + maxR), 
                angleStart, angleSwivel, true, p
            )
        }
        // Center Void or Sun
        p.color = if (pal.isDark) Color.BLACK else Color.WHITE
        p.alpha = 255
        c.drawCircle(cx, cy, 150f, p)
    }

    private fun drawBrutalistLines(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        p.style = Paint.Style.STROKE
        p.strokeCap = Paint.Cap.SQUARE
        
        val count = rng.nextInt(10, 25)
        for (i in 0 until count) {
             val thickness = rng.nextFloat() * 80 + 20
             p.strokeWidth = thickness
             p.color = if (rng.nextBoolean()) pal.accent1 else pal.text
             p.alpha = rng.nextInt(60, 255)
             
             val x1 = rng.nextFloat() * w
             val y1 = rng.nextFloat() * h
             
             if (rng.nextBoolean()) {
                 c.drawLine(x1, 0f, x1, h.toFloat(), p)
             } else {
                 c.drawLine(0f, y1, w.toFloat(), y1, p)
             }
        }
        
        p.style = Paint.Style.FILL
        val circleCount = rng.nextInt(1, 4)
        for (j in 0 until circleCount) {
            p.color = pal.accent2
            c.drawCircle(rng.nextFloat() * w, rng.nextFloat() * h, rng.nextFloat() * 200, p)
        }
    }

    private fun drawWaveInterference(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 3f
        val lines = 60
        val step = h / lines.toFloat()
        
        for (i in 0..lines) {
            val path = Path()
            val yBase = i * step
            
            p.color = pal.accent1 // Use accent instead of white
            p.alpha = 40 + (i * 215 / lines).coerceAtMost(215)
            
            path.moveTo(0f, yBase)
            val freq = 0.01f + rng.nextFloat() * 0.02f
            val phase = rng.nextFloat() * 10
            
            for (x in 0..w step 25) {
                 val amp = 80f + rng.nextFloat() * 40f
                 val y = yBase + sin(x * freq + i * 0.1f + phase) * amp
                 path.lineTo(x.toFloat(), y.toFloat())
            }
            c.drawPath(path, p)
        }
    }

    private fun drawModularSquares(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
         val cols = rng.nextInt(4, 10)
         val size = w / cols.toFloat()
         val rows = (h / size).toInt() + 1
         
         for (r in 0..rows) {
             for (col in 0..cols) {
                 if (rng.nextFloat() > 0.55) continue 
                 
                 val x = col * size
                 val y = r * size
                 val gap = size * 0.1f
                 
                 p.style = Paint.Style.FILL
                 p.color = if (rng.nextFloat() > 0.7) pal.accent2 else pal.accent1
                 p.alpha = rng.nextInt(60, 180)
                 
                 val rect = RectF(x + gap, y + gap, x + size - gap, y + size - gap)
                 c.drawRoundRect(rect, 25f, 25f, p)
                 
                 if (rng.nextFloat() > 0.6) {
                     p.strokeWidth = 5f
                     p.style = Paint.Style.STROKE
                     p.color = pal.text
                     p.alpha = 100
                     c.drawLine(x + size/2, y + size/2, x + size*1.5f, y + size/2, p)
                 }
             }
         }
    }

    private fun drawAbstractShards(c: Canvas, w: Int, h: Int, p: Paint, pal: Palette, rng: Random) {
        val count = rng.nextInt(15, 30)
        p.style = Paint.Style.FILL
        for (i in 0 until count) {
            val path = Path()
            val cx = rng.nextFloat() * w
            val cy = rng.nextFloat() * h
            
            path.moveTo(cx, cy)
            path.lineTo(cx + rng.nextInt(-400, 400), cy + rng.nextInt(-400, 400))
            path.lineTo(cx + rng.nextInt(-400, 400), cy + rng.nextInt(-400, 400))
            path.close()
            
            p.color = if (rng.nextBoolean()) pal.accent1 else pal.accent2
            p.alpha = rng.nextInt(30, 120)
            c.drawPath(path, p)
        }
    }


    // --- QUOTE ENGINE ---
    
    private val QUOTES = listOf(
        // --- ACTION & DISCIPLINE ---
        "DO THE WORK.",
        "STOP WAITING.",
        "NO MORE EXCUSES.",
        "ACTION CURES FEAR.",
        "DISCIPLINE IS FREEDOM.",
        "CONSISTENCY WINS.",
        "WORK IN SILENCE.",
        "RESULTS SPEAK LOUDER.",
        "EXECUTION IS EVERYTHING.",
        "START WHERE YOU ARE.",
        "USE WHAT YOU HAVE.",
        "DO WHAT YOU CAN.",
        "JUST START.",
        "KEEP GOING.",
        "NEVER SETTLE.",
        "EMBRACE THE STRUGGLE.",
        "PAIN IS TEMPORARY.",
        "QUITTING LASTS FOREVER.",
        "SWEAT MORE IN TRAINING.",
        "BLEED LESS IN BATTLE.",
        
        // --- TIME & REALITY ---
        "TIME WAITS FOR NO ONE.",
        "YOU ARE DYING.",
        "MEMENTO MORI.",
        "TEMPUS FUGIT.",
        "TODAY IS A GIFT.",
        "MAKE IT COUNT.",
        "YESTERDAY IS GONE.",
        "TOMORROW IS NOT PROMISED.",
        "THE CLOCK IS TICKING.",
        "LIFE IS SHORT.",
        "DON'T WASTE IT.",
        "YOUR TIME IS LIMITED.",
        "BE PRESENT.",
        "FOCUS ON NOW.",
        "REGRET IS EXPENSIVE.",
        "ACTION IS CHEAP.",
        "LATER OFTEN MEANS NEVER.",
        "ONE DAY OR DAY ONE.",
        "YOU DECIDE.",
        "TICK TOCK.",

        // --- MINDSET & STOICISM ---
        "AMOR FATI.",
        "LOVE YOUR FATE.",
        "OBSTACLE IS THE WAY.",
        "CONTROL YOUR MIND.",
        "MASTER THYSELF.",
        "KNOW THYSELF.",
        "SILENCE IS POWER.",
        "LISTEN MORE.",
        "SPEAK LESS.",
        "THINK DEEPLY.",
        "CHARACTER IS DESTINY.",
        "INTEGRITY MATTERS.",
        "BE THE CHANGE.",
        "LEAD BY EXAMPLE.",
        "OWN YOUR MISTAKES.",
        "FORGIVE YOURSELF.",
        "LET GO.",
        "MOVE ON.",
        "STAY CURIOUS.",
        "ALWAYS LEARNING.",

        // --- HARD TRUTHS ---
        "NO ONE IS COMING.",
        "SAVE YOURSELF.",
        "COMFORT IS A TRAP.",
        "GROWTH REQUIRES PAIN.",
        "AVERAGE IS A CHOICE.",
        "SUCCESS IS RENTED.",
        "RENT IS DUE DAILY.",
        "YOUR PHONE OWNS YOU.",
        "BREAK THE LOOP.",
        "KILL DISTRACTION.",
        "SCROLLING KILLS DREAMS.",
        "CONSUME LESS.",
        "CREATE MORE.",
        "IDEAS ARE CHEAP.",
        "DO IT SCARED.",
        "COURAGE IS ACTION.",
        "FEAR IS A LIAR.",
        "TRUTH LIBERATES.",
        "BE UNDENIABLE.",
        "PROVE THEM WRONG.",

        // --- SHORT MANDATES ---
        "GET UP.",
        "MOVE.",
        "LIFT.",
        "READ.",
        "WRITE.",
        "CODE.",
        "SOLVE.",
        "BUILD.",
        "CREATE.",
        "FIGHT.",
        "WIN.",
        "LIVE.",
        "BREATHE.",
        "OBSERVE.",
        "ACT.",
        "DARE.",
        "BEGIN.",
        "ENDURE.",
        "OVERCOME.",
        "PREVAIL.",

        // --- MATURE WISDOM ---
        "KINDNESS IS STRENGTH.",
        "HUMILITY IS TRUTH.",
        "PATIENCE PAYS.",
        "QUALITY OVER QUANTITY.",
        "LESS IS MORE.",
        "SIMPLICITY IS KEY.",
        "PEACE IS PRIORITY.",
        "PROTECT YOUR ENERGY.",
        "VALUE YOURSELF.",
        "RESPECT OTHERS.",
        "GRATITUDE CHANGES EVERYTHING.",
        "PERSPECTIVE IS KEY.",
        "THIS TOO SHALL PASS.",
        "STORM MAKES YOU STRONGER.",
        "CALM IS A SUPERPOWER.",
        "FOCUS ON WHAT MATTERS.",
        "IGNORE THE NOISE.",
        "CHOOSE YOUR HARD.",
        "BE A GOOD ANCESTOR.",
        "LEAVE A LEGACY."
    )

    private fun drawHardHittingQuote(c: Canvas, w: Int, h: Int, pal: Palette, rng: Random) {
        val quote = QUOTES.random(rng)
        
        val textPaint = TextPaint().apply {
            isAntiAlias = true
            color = pal.text
            textSize = 80f 
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            if (pal.isDark) {
                setShadowLayer(15f, 0f, 0f, Color.BLACK)
            } else {
                setShadowLayer(8f, 0f, 0f, Color.WHITE)
            }
            letterSpacing = 0.04f
        }
        
        // Check fit
        if (w < 900) textPaint.textSize = 65f
        if (quote.length > 60) textPaint.textSize = 55f
        
        // Dynamic Layout
        // 0: Top-Left, 1: Top-Right, 2: Bottom-Left, 3: Bottom-Right
        val quadrant = rng.nextInt(4)
        val margin = 100
        val maxTextWidth = w - (margin * 2)
        
        val alignment = if (quadrant == 1 || quadrant == 3) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL
        
        val builder = StaticLayout.Builder.obtain(quote, 0, quote.length, textPaint, maxTextWidth)
            .setAlignment(alignment)
            .setLineSpacing(0f, 1.1f)
            .setIncludePad(false)
            
        val staticLayout = builder.build()
        val textH = staticLayout.height
        
        var x = margin.toFloat()
        var y = margin.toFloat()
        
        when(quadrant) {
            0 -> { // Top-Left
                y = margin.toFloat() + 200 // Clear clock area
            }
            1 -> { // Top-Right
                y = margin.toFloat() + 200
            }
            2 -> { // Bottom-Left
                y = (h - textH - margin * 3).toFloat()
            }
            3 -> { // Bottom-Right
                y = (h - textH - margin * 3).toFloat()
            }
        }
        
        c.save()
        c.translate(x, y)
        
        // Vertical Accent Bar
        val barPaint = Paint().apply { color = pal.accent1; strokeWidth = 12f }
        // If aligned right (quadrant 1,3), bar is on the right
        if (quadrant == 1 || quadrant == 3) {
            c.drawLine(w - margin * 2f + 40f, 0f, w - margin * 2f + 40f, textH.toFloat(), barPaint)
        } else {
            c.drawLine(-40f, 0f, -40f, textH.toFloat(), barPaint)
        }

        staticLayout.draw(c)
        c.restore()
    }
}
