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
import android.graphics.Typeface
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

        // Use index to consistently seed background color variation
        val seed = index * 54321L
        val rng = Random(seed)

        // VIBRANT & MIXED PALETTE
        // User wants "mixed colors not on same palette", "bright", "contrast".
        
        // 1. Background Color
        val bgHue = (index * 137.508f + rng.nextFloat() * 20f) % 360f // Base hue
        // High saturation for "bright" look, but maybe distinct brightness
        val bgSat = 0.6f + (rng.nextFloat() * 0.4f) // 60-100% Saturation
        val bgBri = 0.15f + (rng.nextFloat() * 0.35f) // 15-50% Brightness (Keep it somewhat dark for text contrast usually)
        
        val bgColor = Color.HSVToColor(floatArrayOf(bgHue, bgSat, bgBri))
        canvas.drawColor(bgColor)

        // 2. Pattern Color - High Contrast
        // Use Split Complementary or Triadic for "not on same palette" look
        val useTriadic = rng.nextBoolean()
        val accentHue = if (useTriadic) (bgHue + 120f) % 360f else (bgHue + 180f) % 360f
        
        val accentSat = 0.8f + (rng.nextFloat() * 0.2f) // Very distinct
        val accentBri = 0.7f + (rng.nextFloat() * 0.3f) // Bright patterns
        val alpha = 80 // More visible patterns

        paint.color = Color.HSVToColor(alpha, floatArrayOf(accentHue, accentSat, accentBri))
        paint.style = Paint.Style.FILL

        // INCREASED PATTERN VARIETY (12 patterns)
        when(index % 12) {
            0 -> drawGradientMesh(canvas, w, h, paint, rng)
            1 -> drawGeometricConstellation(canvas, w, h, paint, rng)
            2 -> drawSubtleWaves(canvas, w, h, paint, rng)
            3 -> drawModernGrid(canvas, w, h, paint, rng)
            4 -> drawRadialPulse(canvas, w, h, paint, rng)
            5 -> drawHexagonHive(canvas, w, h, paint, rng)
            6 -> drawAbstractShapes(canvas, w, h, paint, rng)
            7 -> drawCyberLines(canvas, w, h, paint, rng)
            8 -> drawSplatter(canvas, w, h, paint, rng)
            9 -> drawCircuitBoard(canvas, w, h, paint, rng)
            10 -> drawBauhaus(canvas, w, h, paint, rng)
            11 -> drawFloatingBubbles(canvas, w, h, paint, rng)
        }

        // Add Motivational Text Overlay
        drawMotivationalText(canvas, w, h, rng, bgColor)
    }

    private fun drawMotivationalText(canvas: Canvas, w: Int, h: Int, rng: Random, bgColor: Int) {
        val quotes = listOf(
            "Comfort is a slow death.",
            "They want you to fail. Don't.",
            "Your potential is wasted on scrolling.",
            "Build the empire, leave the noise.",
            "Be a monster, then learn to control it.",
            "Embrace the suck. Love the grind.",
            "Mediocrity is a disease. Get cured.",
            "Sleep faster. Work harder.",
            "Pain is weakness leaving the body.",
            "Discipline equals freedom.",
            "No one is coming to save you.",
            "Stop being a spectator in your own life.",
            "Do it tired. Do it scared. Do it now.",
            "Excuses sound best to the person making them.",
            "Your future self is watching you right now.",
            "Don't wish for it. Work for it.",
            "Stay hungry. Stay foolish.",
            "Action cures fear.",
            "Focus on the solution, not the problem.",
            "Make them regret doubting you.",
            "Obsession beats talent.",
            "Normalize being obsessed with your goals.",
            "You are your only limit.",
            "Dream big. Work hard. Stay humble.",
            "Small progress is still progress.",
            "Consistency is key.",
            "Trust the process.",
            "Create the life you can't wait to wake up to.",
            "Focus on being productive instead of busy.",
            "Don't stop until you're proud.",
            "Prove them wrong.",
            "Success is the best revenge.",
            "Your only competition is who you were yesterday.",
            "Mindset is everything.",
            "Hustle until your haters ask if you're hiring.",
            "Don't talk, just act. Don't say, just show.",
            "The hard way is the right way.",
            "If it was easy, everyone would do it.",
            "Be the hardest worker in the room.",
            "Don't decrease the goal. Increase the effort.",
            "Your time is limited, don't waste it.",
            "What you do today can improve all your tomorrows.",
            "Success doesn't come to you, you go to it.",
            "The secret of getting ahead is getting started.",
            "It always seems impossible until it's done.",
            "Don't count the days, make the days count.",
            "The best way to predict the future is to create it.",
            "Opportunities don't happen, you create them.",
            "You become what you believe.",
            "Believe you can and you're halfway there.",
            "The only way to do great work is to love what you do.",
            "Reject modernity, embrace masculinity.",
            "Sugar is poison. Don't touch it.",
            "Code represents your mind. Keep it clean.",
            "A man who conquers himself is greater than one who conquers a thousand men in battle."
        )

        val quote = quotes[rng.nextInt(quotes.size)]

        // Calculate contrasting text color
        // If background is dark, use light text. If light, use dark.
        // Since we generate mostly vibrant/dark backgrounds, white/off-white is usually safe,
        // but let's be smart.
        val bgLuminance = Color.luminance(bgColor)
        val baseTextColor = if (bgLuminance > 0.5) Color.BLACK else Color.WHITE
        
        // Add a subtle tint of the primary color to the text for harmony
        val textHSV = FloatArray(3)
        Color.colorToHSV(bgColor, textHSV)
        textHSV[1] = 0.1f // Very low saturation (almost grey/white)
        textHSV[2] = if (bgLuminance > 0.5) 0.2f else 0.95f // Value based on contrast needed
        val harmonyTextColor = Color.HSVToColor(textHSV)


        val textPaint = Paint().apply {
            isAntiAlias = true
            color = harmonyTextColor
            textAlign = Paint.Align.CENTER
            // Use sans-serif-condensed for a modern, sleek look or Serif for elegance
            // Randomly choose between a few good fonts
            typeface = if (rng.nextBoolean()) 
                Typeface.create("sans-serif-condensed", Typeface.BOLD)
            else 
                Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
            
            setShadowLayer(15f, 0f, 0f, if (bgLuminance > 0.5) Color.GRAY else Color.BLACK)
        }

        // Layout Logic
        // Position: CENTER of the screen, as requested.
        val centerX = w / 2f
        
        // Vertical Position:
        // User requested: "text center of the mobile" but also "below date and time".
        // Clock is top (taking ~25%). We place text at 55% to be clearly below but central.
        val centerY = h * 0.55f

        // Word Wrap Logic
        val targetWidth = w * 0.8f // 80% of screen width
        val words = quote.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        
        // Dynamic text sizing based on length
        var textSize = w * 0.065f // Baseline size
        if (quote.length > 50) textSize *= 0.8f // Shrink for long quotes
        if (quote.length < 20) textSize *= 1.2f // Grow for short impacts
        textPaint.textSize = textSize

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (textPaint.measureText(testLine) < targetWidth) {
                currentLine = testLine
            } else {
                lines.add(currentLine)
                currentLine = word
            }
        }
        lines.add(currentLine)

        // Draw Lines
        val lineHeight = textPaint.descent() - textPaint.ascent()
        val totalBlockHeight = lines.size * lineHeight
        var drawY = centerY - (totalBlockHeight / 2f) + -textPaint.ascent() // V-Center text block at target Y

        lines.forEach { line ->
            canvas.drawText(line, centerX, drawY, textPaint)
            drawY += lineHeight
        }
    }

    private fun drawGradientMesh(c: Canvas, w: Int, h: Int, p: Paint, rng: Random) {
        // Large soft circles creating a mesh-like gradient effect
        val count = 8
        for(i in 0 until count) {
            val cx = rng.nextFloat() * w
            val cy = rng.nextFloat() * h
            val r = maxOf(w, h) * (0.3f + rng.nextFloat() * 0.4f)
            c.drawCircle(cx, cy, r, p)
        }
    }

    private fun drawGeometricConstellation(c: Canvas, w: Int, h: Int, p: Paint, rng: Random) {
        val points = mutableListOf<Pair<Float, Float>>()
        val count = 40
        p.style = Paint.Style.FILL
        
        // Generate points
        for(i in 0 until count) {
            points.add(Pair(rng.nextFloat() * w, rng.nextFloat() * h))
        }
        
        // Draw connections if close enough
        p.style = Paint.Style.STROKE
        p.strokeWidth = 2f
        val connectionDist = maxOf(w, h) * 0.15f
        
        for(i in 0 until count) {
            for(j in i+1 until count) {
                val (x1, y1) = points[i]
                val (x2, y2) = points[j]
                val dist = Math.hypot((x2-x1).toDouble(), (y2-y1).toDouble()).toFloat()
                if(dist < connectionDist) {
                    val alphaScale = (1.0f - (dist / connectionDist))
                    p.alpha = (30 * alphaScale).toInt() // Fade out distant connections
                    c.drawLine(x1, y1, x2, y2, p)
                }
            }
        }
    }

    private fun drawSubtleWaves(c: Canvas, w: Int, h: Int, p: Paint, rng: Random) {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 3f
        val path = Path()
        
        val lines = 15
        val stepY = h / lines.toFloat()
        
        for (i in 0..lines + 2) {
            val baseY = i * stepY
            path.reset()
            path.moveTo(0f, baseY)
            
            val amplitude = w * 0.05f
            val frequency = rng.nextFloat() * 2 + 1
            
            for (x in 0..w step 20) {
                 val relX = x / w.toFloat()
                 val yOffset = kotlin.math.sin(relX * Math.PI * 2 * frequency + (i * 0.5f)) * amplitude
                 path.lineTo(x.toFloat(), baseY + yOffset.toFloat())
            }
            c.drawPath(path, p)
        }
    }
    
    private fun drawModernGrid(c: Canvas, w: Int, h: Int, p: Paint, rng: Random) {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 4f
        val gridSize = maxOf(w, h) / 8f
        
        // Isometric-ish pattern
        for (y in -2..(h/gridSize).toInt() + 2) {
            for (x in -2..(w/gridSize).toInt() + 2) {
                if (rng.nextFloat() > 0.7f) { // Random sparsity
                    val px = x * gridSize
                    val py = y * gridSize
                    
                    // Randomly choose horizontal, vertical or diagonal elements
                    when(rng.nextInt(3)) {
                        0 -> c.drawLine(px, py, px + gridSize, py, p)
                        1 -> c.drawLine(px, py, px, py + gridSize, p)
                        2 -> c.drawLine(px, py + gridSize, px + gridSize, py, p)
                    }
                }
            }
        }
    }

    private fun drawRadialPulse(c: Canvas, w: Int, h: Int, p: Paint, rng: Random) {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 2f
        
        val cx = w * (0.2f + rng.nextFloat() * 0.6f)
        val cy = h * (0.2f + rng.nextFloat() * 0.6f)
        
        val rings = 20
        val maxR = maxOf(w, h) * 0.6f
        
        for(i in 1..rings) {
            val r = (i / rings.toFloat()) * maxR
            // Random dashing effect
             if (rng.nextFloat() > 0.3f) {
                 c.drawCircle(cx, cy, r, p)
             }
        }
    }

    private fun drawHexagonHive(c: Canvas, w: Int, h: Int, p: Paint, rng: Random) {
        val radius = maxOf(w, h) * 0.08f
        val xOffset = radius * 1.5f
        val yOffset = radius * 1.732f // sqrt(3)
        
        p.style = Paint.Style.STROKE
        p.strokeWidth = 3f

        for (row in 0..(h/yOffset).toInt() + 1) {
            for (col in 0..(w/xOffset).toInt() + 1) {
                if (rng.nextFloat() > 0.4f) {
                    val cx = col * xOffset + (if (row % 2 == 1) xOffset / 2f else 0f)
                    val cy = row * yOffset
                    
                    val path = Path()
                    for (i in 0..6) {
                        val angle = Math.toRadians(60.0 * i - 30.0)
                        val x = cx + radius * cos(angle).toFloat()
                        val y = cy + radius * sin(angle).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    c.drawPath(path, p)
                }
            }
        }
    }

    private fun drawAbstractShapes(c: Canvas, w: Int, h: Int, p: Paint, rng: Random) {
        p.style = Paint.Style.FILL
        val count = 15
        val originalColor = p.color
        val hsv = FloatArray(3)
        Color.colorToHSV(originalColor, hsv)

        for(i in 0 until count) {
            // Vary color slightly for each shape
            hsv[0] = (hsv[0] + rng.nextFloat() * 40 - 20) % 360f 
            p.color = Color.HSVToColor(100, hsv) // Higher alpha

            val cx = rng.nextFloat() * w
            val cy = rng.nextFloat() * h
            val size = w * (0.1f + rng.nextFloat() * 0.3f)

            when(rng.nextInt(3)) {
                0 -> c.drawCircle(cx, cy, size/2, p)
                1 -> c.drawRect(cx - size/2, cy - size/2, cx + size/2, cy + size/2, p)
                2 -> {
                    val path = Path()
                    path.moveTo(cx, cy - size/2)
                    path.lineTo(cx + size/2, cy + size/2)
                    path.lineTo(cx - size/2, cy + size/2)
                    path.close()
                    c.drawPath(path, p)
                }
            }
        }
        p.color = originalColor // Restore
    }

    private fun drawCyberLines(c: Canvas, w: Int, h: Int, p: Paint, rng: Random) {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 4f
        val lineCount = 25
        
        for(i in 0 until lineCount) {
            val startX = rng.nextFloat() * w
            val startY = rng.nextFloat() * h
            val length = w * (0.2f + rng.nextFloat() * 0.6f)
            val angle = if(rng.nextBoolean()) 45f else -45f
            
            val endX = startX + length * cos(Math.toRadians(angle.toDouble())).toFloat()
            val endY = startY + length * sin(Math.toRadians(angle.toDouble())).toFloat()
            
            p.alpha = 50 + rng.nextInt(100)
            c.drawLine(startX, startY, endX, endY, p)
            
            // Decorative dot at start
            p.style = Paint.Style.FILL
            c.drawCircle(startX, startY, 6f, p)
            p.style = Paint.Style.STROKE
        }
    }

    private fun drawSplatter(c: Canvas, w: Int, h: Int, p: Paint, rng: Random) {
        p.style = Paint.Style.FILL
        val splats = 8
        
        for(i in 0 until splats) {
            val cx = rng.nextFloat() * w
            val cy = rng.nextFloat() * h
            val baseR = w * (0.05f + rng.nextFloat() * 0.1f)
            
            // Main blob
            c.drawCircle(cx, cy, baseR, p)
            
            // Satellites
            val satellites = 5 + rng.nextInt(10)
            for(j in 0 until satellites) {
                val dist = baseR * (1.2f + rng.nextFloat())
                val angle = rng.nextFloat() * Math.PI * 2
                val sx = cx + dist * cos(angle).toFloat()
                val sy = cy + dist * sin(angle).toFloat()
                c.drawCircle(sx, sy, baseR * (0.1f + rng.nextFloat() * 0.3f), p)
            }
        }
    }

    private fun drawCircuitBoard(c: Canvas, w: Int, h: Int, p: Paint, rng: Random) {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 3f
        val traces = 30
        
        for(i in 0 until traces) {
            var cx = rng.nextFloat() * w
            var cy = rng.nextFloat() * h
            val segments = 3 + rng.nextInt(4)
            val path = Path()
            path.moveTo(cx, cy)
            
            // Start dot
            p.style = Paint.Style.FILL
            c.drawCircle(cx, cy, 5f, p)
            p.style = Paint.Style.STROKE
            
            for(j in 0 until segments) {
                val len = w * (0.05f + rng.nextFloat() * 0.1f)
                val dir = rng.nextInt(4) // 0:R, 1:D, 2:L, 3:U
                when(dir) {
                    0 -> cx += len
                    1 -> cy += len
                    2 -> cx -= len
                    3 -> cy -= len
                }
                path.lineTo(cx, cy)
            }
            c.drawPath(path, p)
             // End dot
            p.style = Paint.Style.FILL
            c.drawCircle(cx, cy, 5f, p)
            p.style = Paint.Style.STROKE
        }
    }

    private fun drawBauhaus(c: Canvas, w: Int, h: Int, p: Paint, rng: Random) {
        // Geometric primitives in a grid-less composition
        val elements = 12
        p.style = Paint.Style.FILL
        
        for(i in 0 until elements) {
             val cx = rng.nextFloat() * w
             val cy = rng.nextFloat() * h
             val size = w * (0.1f + rng.nextFloat() * 0.4f)
             
             // Randomly modify alpha for overlay effect
             p.alpha = 50 + rng.nextInt(100)
             
             when(rng.nextInt(4)) {
                 0 -> c.drawRect(cx, cy, cx + size, cy + size, p) // Square
                 1 -> c.drawCircle(cx, cy, size/2, p) // Circle
                 2 -> c.drawRect(cx, cy, cx + size/4, cy + size*2, p) // Tall thin rect
                 3 -> c.drawArc(RectF(cx, cy, cx+size, cy+size), 0f, 180f, true, p) // Semi-circle
             }
        }
    }

    private fun drawFloatingBubbles(c: Canvas, w: Int, h: Int, p: Paint, rng: Random) {
        val bubbles = 40
        p.style = Paint.Style.STROKE
        p.strokeWidth = 2f
        
        for(i in 0 until bubbles) {
            val cx = rng.nextFloat() * w
            val cy = rng.nextFloat() * h
            val r = w * (0.02f + rng.nextFloat() * 0.08f)
            
            c.drawCircle(cx, cy, r, p)
            
            // Highlight shine
            val shinePath = Path()
            shinePath.addArc(RectF(cx-r*0.7f, cy-r*0.7f, cx+r*0.3f, cy+r*0.3f), 180f, 90f)
            c.drawPath(shinePath, p)
        }
    }

    // Deprecated helpers removed or replaced above
}
