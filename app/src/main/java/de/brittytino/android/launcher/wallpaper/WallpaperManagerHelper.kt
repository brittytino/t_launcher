package de.brittytino.android.launcher.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import de.brittytino.android.launcher.preferences.LauncherPreferences
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object WallpaperManagerHelper {

    private const val KEY_WALLPAPER_DATE = "wallpaper_rotation_date"
    private const val KEY_WALLPAPER_INDEX = "wallpaper_rotation_index"
    private const val TOTAL_PATTERNS = 30

    fun checkAndRotateWallpaper(context: Context) {
        val prefs = LauncherPreferences.getSharedPreferences()
        val lastDate = prefs.getLong(KEY_WALLPAPER_DATE, 0)
        val today = getStartOfDay()

        if (today != lastDate) {
            val currentIndex = prefs.getInt(KEY_WALLPAPER_INDEX, 0)
            val nextIndex = (currentIndex + 1) % TOTAL_PATTERNS
            
            applyWallpaper(context, nextIndex)
            
            prefs.edit()
                .putLong(KEY_WALLPAPER_DATE, today)
                .putInt(KEY_WALLPAPER_INDEX, nextIndex)
                .apply()
        }
    }

    fun advanceWallpaper(context: Context): Int {
        val prefs = LauncherPreferences.getSharedPreferences()
        val currentIndex = prefs.getInt(KEY_WALLPAPER_INDEX, 0)
        val nextIndex = (currentIndex + 1) % TOTAL_PATTERNS
        
        applyWallpaper(context, nextIndex)
        
        prefs.edit()
             .putInt(KEY_WALLPAPER_INDEX, nextIndex)
             .apply()
             
        return nextIndex + 1 // Return 1-based index for UI
    }
    
    fun getCurrentIndex(context: Context): Int {
        return LauncherPreferences.getSharedPreferences().getInt(KEY_WALLPAPER_INDEX, 0) + 1
    }

    fun getTotalPatterns(): Int = TOTAL_PATTERNS

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun applyWallpaper(context: Context, index: Int) {
        try {
            val metrics = context.resources.displayMetrics
            // Use a slightly smaller size if memory is an issue, but metrics is best for wallpaper
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            drawPattern(index, canvas, width, height)
            
            WallpaperManager.getInstance(context).setBitmap(bitmap)
            
            // Allow bitmap to be GC'd
            // bitmap.recycle() // WallpaperManager copies it, but we should let it go.
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun drawPattern(index: Int, canvas: Canvas, w: Int, h: Int) {
        val paint = Paint().apply { isAntiAlias = true }
        
        // Deterministic seeding
        val seed = index * 9999L + 12345L
        val rng = Random(seed)
        
        // Background - Dark Friendly
        // Always dark base for consistency, or dark/muted variations
        // Hues: spread out
        val hue = (index * 23.0f) % 360f
        val saturation = rng.nextFloat() * 0.2f + 0.1f // 0.1 - 0.3
        val brightness = 0.15f // Pretty dark
        
        val bgColor = Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
        canvas.drawColor(bgColor)
        
        // Pattern Color
        val patHue = (hue + 180f) % 360f // Complementary
        val patSat = 0.5f
        val patBri = 0.8f
        paint.color = Color.HSVToColor(60, floatArrayOf(patHue, patSat, patBri)) // Low alpha
        paint.style = Paint.Style.FILL

        when(index % 6) { 
            0 -> drawDots(canvas, w, h, paint, rng)
            1 -> drawLines(canvas, w, h, paint, rng)
            2 -> drawCircles(canvas, w, h, paint, rng)
            3 -> drawTriangles(canvas, w, h, paint, rng) 
            4 -> drawHexagons(canvas, w, h, paint, rng)
            5 -> drawCurves(canvas, w, h, paint, rng)
        }
    }
    
    private fun drawDots(c: Canvas, w: Int, h: Int, p: Paint, rng: Random) {
        val spacing = rng.nextInt(50, 150).toFloat()
        val radiusMultiplier = rng.nextFloat() * 0.4f + 0.1f
        val radius = spacing * radiusMultiplier
        
        for (x in 0..w step spacing.toInt()) {
            for (y in 0..h step spacing.toInt()) {
                // Occasional offset or jitter
                val cx = x.toFloat()
                val cy = y.toFloat()
                c.drawCircle(cx, cy, radius, p)
            }
        }
    }

    private fun drawLines(c: Canvas, w: Int, h: Int, p: Paint, rng: Random) {
        p.style = Paint.Style.STROKE
        p.strokeWidth = rng.nextInt(2, 10).toFloat()
        val spacing = rng.nextInt(40, 120).toFloat()
        val angle = rng.nextInt(0, 4) * 45f // 0, 45, 90, 135
        
        c.save()
        c.rotate(angle, w/2f, h/2f)
        
        // Oversize canvas for rotation
        val maxDim = maxOf(w, h) * 2
        for (i in -maxDim..maxDim step spacing.toInt()) {
            c.drawLine(i.toFloat(), -maxDim.toFloat(), i.toFloat(), maxDim.toFloat(), p)
        }
        
        c.restore()
        p.style = Paint.Style.FILL // Reset
    }

    private fun drawCircles(c: Canvas, w: Int, h: Int, p: Paint, rng: Random) {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 3f
        
        val count = 20
        for (i in 0 until count) {
            val cx = rng.nextInt(w).toFloat()
            val cy = rng.nextInt(h).toFloat()
            val r = rng.nextInt(50, 400).toFloat()
            c.drawCircle(cx, cy, r, p)
        }
    }
    
    private fun drawTriangles(c: Canvas, w: Int, h: Int, p: Paint, rng: Random) {
        val size = rng.nextInt(100, 300).toFloat()
        val cols = (w / size).toInt() + 2
        val rows = (h / size).toInt() + 2
        
        val path = Path()
        for (y in 0 until rows) {
            for (x in 0 until cols) {
                 if (rng.nextBoolean()) {
                     val px = x * size
                     val py = y * size
                     path.reset()
                     path.moveTo(px, py)
                     path.lineTo(px + size, py)
                     path.lineTo(px + size/2, py + size)
                     path.close()
                     c.drawPath(path, p)
                 }
            }
        }
    }

    private fun drawHexagons(c: Canvas, w: Int, h: Int, p: Paint, rng: Random) {
         p.style = Paint.Style.STROKE
         p.strokeWidth = 4f
         val radius = rng.nextInt(50, 150).toFloat()
         val width = (radius * 2 * 0.866).toFloat() // sqrt(3)/2
         
         val cols = (w / width).toInt() + 2
         val rows = (h / (radius * 1.5f)).toInt() + 2
         
         for(y in 0 until rows) {
             for(x in 0 until cols) {
                 val cx = x * width + (if (y%2==1) width/2 else 0f)
                 val cy = y * radius * 1.5f
                 drawHexagon(c, cx, cy, radius, p)
             }
         }
    }
    
    private fun drawHexagon(c: Canvas, x: Float, y: Float, r: Float, p: Paint) {
        val path = Path()
        for(i in 0 until 6) {
            val angle = Math.toRadians((60 * i + 30).toDouble())
            val px = x + r * cos(angle).toFloat()
            val py = y + r * sin(angle).toFloat()
            if (i == 0) path.moveTo(px, py)
            else path.lineTo(px, py)
        }
        path.close()
        c.drawPath(path, p)
    }

    private fun drawCurves(c: Canvas, w: Int, h: Int, p: Paint, rng: Random) {
         p.style = Paint.Style.STROKE
         p.strokeWidth = 5f
         val step = 50f
         val path = Path()
         
         for (y in 0..h step 100) {
             path.reset()
             path.moveTo(0f, y.toFloat())
             var x = 0f
             while(x < w) {
                 path.quadTo(x + 50, y.toFloat() + rng.nextInt(-50, 50), x + 100, y.toFloat())
                 x += 100
             }
             c.drawPath(path, p)
         }
    }
}
