package app.olauncher.helper

import android.app.WallpaperManager as AndroidWallpaperManager
import android.content.Context
import android.graphics.*
import java.util.Calendar
import kotlin.math.*
import kotlin.random.Random

object WallpaperManager {

    val styles = listOf(
        "Neon Grid", "Deep Ocean", "Forest Mist", "Sunset City", "Hex Hive",
        "Circuit Board", "Starry Night", "Bamboo", "Abstract Swirl", "Polygons",
        "Matrix Rain", "Retro Wave", "Fluid Blobs", "Bauhaus", "Zen Strings",
        "Pixel Art", "Radial Burst", "Topo Map", "Bricks", "Galaxy Spiral",
        "Cyber Rain", "Northern Lights", "Geometric Hills", "Paper Cutout", "Digital DNA",
        "Void Stare", "Crystal Cave", "Solar Flare", "Dotted Landscape", "Sound Wave",
        "Quantum Mesh", "Origami Birds", "Lava Lamp"
    )

    fun applyWallpaper(context: Context, index: Int) {
        try {
            val idx = index % styles.size
            val bitmap = createWallpaperBitmap(context, idx)
            val wallpaperManager = AndroidWallpaperManager.getInstance(context)
            wallpaperManager.setBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun applyDailyWallpaper(context: Context) {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        applyWallpaper(context, dayOfYear)
    }

    fun getWallpaperCount(): Int = styles.size

    fun getWallpaperName(index: Int): String = styles[index % styles.size]

    private fun createWallpaperBitmap(context: Context, index: Int): Bitmap {
        val metrics = context.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint().apply { isAntiAlias = true }

        // Draw Pattern
        when (index) {
            0 -> drawNeonGrid(canvas, width, height, paint)
            1 -> drawDeepOcean(canvas, width, height, paint)
            2 -> drawForestMist(canvas, width, height, paint)
            3 -> drawSunsetCity(canvas, width, height, paint)
            4 -> drawHexHive(canvas, width, height, paint)
            5 -> drawCircuitBoard(canvas, width, height, paint)
            6 -> drawStarryNight(canvas, width, height, paint)
            7 -> drawBamboo(canvas, width, height, paint)
            8 -> drawAbstractSwirl(canvas, width, height, paint)
            9 -> drawPolygons(canvas, width, height, paint)
            10 -> drawMatrixRain(canvas, width, height, paint)
            11 -> drawRetroWave(canvas, width, height, paint)
            12 -> drawFluidBlobs(canvas, width, height, paint)
            13 -> drawBauhaus(canvas, width, height, paint)
            14 -> drawZenStrings(canvas, width, height, paint)
            15 -> drawPixelArt(canvas, width, height, paint)
            16 -> drawRadialBurst(canvas, width, height, paint)
            17 -> drawTopoMap(canvas, width, height, paint)
            18 -> drawBricks(canvas, width, height, paint)
            19 -> drawGalaxySpiral(canvas, width, height, paint)
            20 -> drawCyberRain(canvas, width, height, paint)
            21 -> drawNorthernLights(canvas, width, height, paint)
            22 -> drawGeometricHills(canvas, width, height, paint)
            23 -> drawPaperCutout(canvas, width, height, paint)
            24 -> drawDigitalDNA(canvas, width, height, paint)
            25 -> drawVoidStare(canvas, width, height, paint)
            26 -> drawCrystalCave(canvas, width, height, paint)
            27 -> drawSolarFlare(canvas, width, height, paint)
            28 -> drawDottedLandscape(canvas, width, height, paint)
            29 -> drawSoundWave(canvas, width, height, paint)
            30 -> drawQuantumMesh(canvas, width, height, paint)
            31 -> drawOrigamiBirds(canvas, width, height, paint)
            32 -> drawLavaLamp(canvas, width, height, paint)
            else -> drawNeonGrid(canvas, width, height, paint)
        }

        // Apply Dark Scrim for White Text Readability
        val scrimPaint = Paint()
        scrimPaint.shader = LinearGradient(0f, 0f, 0f, height.toFloat(),
            Color.parseColor("#44000000"), // Top slight dark
            Color.parseColor("#88000000"), // Bottom heavy dark
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), scrimPaint)

        return bitmap
    }

    // --- ARTISTIC GENERATORS ---
    // (Previous 20 preserved/enhanced + 10 new)

    private fun drawNeonGrid(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.parseColor("#050510"))
        p.color = Color.MAGENTA
        p.style = Paint.Style.STROKE
        p.strokeWidth = 2f
        val hZ = h * 0.6f
        val cx = w / 2f
        for (i in -15..15) { c.drawLine(cx + i * 80f, hZ, cx + i * 400f, h.toFloat(), p) }
        var y = h.toFloat()
        while(y > hZ) { c.drawLine(0f, y, w.toFloat(), y, p); y -= (y-hZ)*0.1f + 2f }
        p.style = Paint.Style.FILL
        p.shader = LinearGradient(0f, hZ-300, 0f, hZ, Color.YELLOW, Color.RED, Shader.TileMode.CLAMP)
        c.drawCircle(cx, hZ-100, 150f, p)
        p.shader = null
    }
    private fun drawDeepOcean(c: Canvas, w: Int, h: Int, p: Paint) {
        p.shader = LinearGradient(0f,0f,0f,h.toFloat(), Color.parseColor("#001133"), Color.BLACK, Shader.TileMode.CLAMP)
        c.drawRect(0f,0f,w.toFloat(),h.toFloat(),p); p.shader=null
        p.color=Color.parseColor("#44FFFFFF"); p.style=Paint.Style.FILL
        val r=Random(1); repeat(60){c.drawCircle(r.nextFloat()*w, r.nextFloat()*h, r.nextFloat()*20+2, p)}
    }
    private fun drawForestMist(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.parseColor("#CFD8DC"))
        val rnd=Random(2); val layers=6
        for(i in 0 until layers){
            val lev=i.toFloat()/layers; val yB=h-(h*0.7f*(1-lev))
            p.color=Color.rgb((30+i*20),(40+i*20),(40+i*20)); p.style=Paint.Style.FILL
            val path=Path(); path.moveTo(0f,h.toFloat()); path.lineTo(0f,yB)
            var x=0f
            while(x<w){ val ph=rnd.nextFloat()*150+50; x+=rnd.nextFloat()*80+20; path.lineTo(x,yB-ph); x+=rnd.nextFloat()*80+20; path.lineTo(x,yB) }
            path.lineTo(w.toFloat(),h.toFloat()); path.close(); c.drawPath(path,p)
        }
    }
    private fun drawSunsetCity(c: Canvas, w: Int, h: Int, p: Paint) {
        p.shader=LinearGradient(0f,0f,0f,h*0.8f, Color.parseColor("#2C3E50"), Color.parseColor("#FD746C"), Shader.TileMode.CLAMP)
        c.drawRect(0f,0f,w.toFloat(),h.toFloat(),p); p.shader=null
        p.color=Color.BLACK; val rnd=Random(3); var x=0f
        while(x<w){ val bw=rnd.nextFloat()*120+40; val bh=rnd.nextFloat()*(h*0.3f)+50; c.drawRect(x,h-bh,x+bw,h.toFloat(),p); x+=bw }
    }
    private fun drawHexHive(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.parseColor("#121212")); p.style=Paint.Style.STROKE; p.color=Color.parseColor("#BB86FC"); p.strokeWidth=2f
        val sz=80f; val hO=sz*sqrt(3.0).toFloat(); val cols=(w/sz+2).toInt(); val rows=(h/hO+2).toInt()
        for(r in 0..rows){ for(co in 0..cols){
            val x=co*sz*1.5f; var y=r*hO; if(co%2!=0)y+=hO/2f
            val path=Path(); for(i in 0..5){ val a=Math.toRadians((60*i).toDouble()); if(i==0)path.moveTo(x+sz*0.6f*cos(a).toFloat(),y+sz*0.6f*sin(a).toFloat()) else path.lineTo(x+sz*0.6f*cos(a).toFloat(),y+sz*0.6f*sin(a).toFloat()) }; path.close(); c.drawPath(path,p)
        }}
    }
    private fun drawCircuitBoard(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.parseColor("#001500")); p.color=Color.GREEN; p.strokeWidth=3f
        val rnd=Random(5); repeat(40){ val sx=rnd.nextFloat()*w; val sy=rnd.nextFloat()*h; val ex=rnd.nextFloat()*w; c.drawLine(sx,sy,ex,sy,p); c.drawCircle(sx,sy,5f,p); c.drawCircle(ex,sy,5f,p)}
    }
    private fun drawStarryNight(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.BLACK); p.style=Paint.Style.FILL; val rnd=Random(6)
        repeat(700){ p.color=Color.WHITE; p.alpha=rnd.nextInt(200)+55; c.drawCircle(rnd.nextFloat()*w, rnd.nextFloat()*h, rnd.nextFloat()*2.5f, p)}
    }
    private fun drawBamboo(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.parseColor("#E8F5E9")); val rnd=Random(7)
        repeat(15){ p.color=Color.rgb(46,125,50); val x=rnd.nextFloat()*w; var y=h.toFloat(); while(y>0){ val sh=rnd.nextFloat()*250+100; c.drawRect(x-10,y-sh,x+10,y-4,p); y-=sh}}
    }
    private fun drawAbstractSwirl(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.LTGRAY); p.style=Paint.Style.STROKE; p.color=Color.DKGRAY; p.alpha=40; val cx=w/2f; val cy=h/2f
        for(i in 0..720 step 4){ val dx=cx+w*cos(Math.toRadians(i.toDouble())).toFloat(); val dy=cy+h*sin(Math.toRadians(i.toDouble())).toFloat(); c.drawLine(cx,cy,dx,dy,p)}
    }
    private fun drawPolygons(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.parseColor("#263238")); p.style=Paint.Style.FILL; val rnd=Random(9)
        repeat(50){ p.color=Color.rgb(rnd.nextInt(50),rnd.nextInt(100)+50,rnd.nextInt(100)+100); p.alpha=80; val path=Path().apply{moveTo(rnd.nextFloat()*w,rnd.nextFloat()*h); lineTo(rnd.nextFloat()*w,rnd.nextFloat()*h); lineTo(rnd.nextFloat()*w,rnd.nextFloat()*h); close()}; c.drawPath(path,p)}
    }
    private fun drawMatrixRain(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.BLACK); p.color=Color.GREEN; p.textSize=28f; val rnd=Random(10)
        repeat(60){ val x=rnd.nextFloat()*w; var y=rnd.nextFloat()*h; repeat(rnd.nextInt(15)+5){ c.drawText("${rnd.nextInt(2)}",x,y,p); y+=30f}}
    }
    private fun drawRetroWave(c: Canvas, w: Int, h: Int, p: Paint) {
        val hZ=h*0.65f; p.shader=LinearGradient(0f,0f,0f,hZ,Color.parseColor("#4B0082"),Color.parseColor("#FF8C00"),Shader.TileMode.CLAMP); c.drawRect(0f,0f,w.toFloat(),hZ,p); p.shader=null
        p.color=Color.YELLOW; c.drawCircle(w/2f,hZ-50,250f,p)
        p.color=Color.MAGENTA; p.strokeWidth=3f; for(i in -15..15){c.drawLine(w/2f+i*50f,hZ,w/2f+i*200f,h.toFloat(),p)}; var y=hZ; while(y<h){c.drawLine(0f,y,w.toFloat(),y,p); y+=(y-hZ)*0.15f+5f}
    }
    private fun drawFluidBlobs(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.WHITE); val rnd=Random(12); val cols=listOf(Color.CYAN,Color.MAGENTA,Color.YELLOW)
        repeat(15){ p.color=cols[rnd.nextInt(cols.size)]; p.alpha=120; c.drawCircle(rnd.nextFloat()*w,rnd.nextFloat()*h,rnd.nextFloat()*300+50,p)}
    }
    private fun drawBauhaus(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.parseColor("#F0F0F0")); val rnd=Random(13)
        p.color=Color.RED; c.drawRect(rnd.nextFloat()*w,rnd.nextFloat()*h,rnd.nextFloat()*w+200,rnd.nextFloat()*h+200,p)
        p.color=Color.BLUE; c.drawCircle(rnd.nextFloat()*w,rnd.nextFloat()*h,120f,p)
        p.color=Color.YELLOW; val pa=Path().apply{moveTo(rnd.nextFloat()*w,rnd.nextFloat()*h); lineTo(rnd.nextFloat()*w,rnd.nextFloat()*h); lineTo(rnd.nextFloat()*w,rnd.nextFloat()*h); close()}; c.drawPath(pa,p)
    }
    private fun drawZenStrings(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.parseColor("#F5F5F5")); p.color=Color.BLACK; p.alpha=30; val rnd=Random(14)
        repeat(200){ c.drawLine(0f,rnd.nextFloat()*h,w.toFloat(),rnd.nextFloat()*h,p)}
    }
    private fun drawPixelArt(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.DKGRAY); val sz=60f; val cols=(w/sz).toInt(); val rows=(h/sz).toInt(); val rnd=Random(15)
        for(r in 0..rows) for(co in 0..cols) if(rnd.nextBoolean()){ p.color=if(rnd.nextBoolean()) Color.GREEN else Color.parseColor("#004400"); c.drawRect(co*sz,r*sz,(co+1)*sz,(r+1)*sz,p)}
    }
    private fun drawRadialBurst(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.RED); p.color=Color.YELLOW; val cx=w/2f; val cy=h/2f; val mr=max(w,h).toFloat()
        for(i in 0..360 step 15){ val pth=Path(); pth.moveTo(cx,cy); pth.lineTo(cx+mr*cos(Math.toRadians(i.toDouble())).toFloat(),cy+mr*sin(Math.toRadians(i.toDouble())).toFloat()); pth.lineTo(cx+mr*cos(Math.toRadians(i+7.0)).toFloat(),cy+mr*sin(Math.toRadians(i+7.0)).toFloat()); pth.close(); c.drawPath(pth,p)}
    }
    private fun drawTopoMap(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.parseColor("#263238")); p.color=Color.GRAY; p.style=Paint.Style.STROKE; val rnd=Random(17)
        repeat(8){ val cx=rnd.nextFloat()*w; val cy=rnd.nextFloat()*h; var r=30f; repeat(15){c.drawOval(cx-r,cy-r/1.5f,cx+r,cy+r/1.5f,p); r+=30+rnd.nextFloat()*20}}
    }
    private fun drawBricks(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.parseColor("#795548")); p.color=Color.parseColor("#4E342E"); p.style=Paint.Style.STROKE; p.strokeWidth=6f
        var y=0f; var r=0; while(y<h){var x=if(r%2==0)0f else -100f; while(x<w){c.drawRect(x,y,x+200,y+80,p); x+=200}; y+=80; r++}
    }
    private fun drawGalaxySpiral(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.BLACK); p.style=Paint.Style.FILL; val cx=w/2f; val cy=h/2f; val rnd=Random(19); var a=0.0; var r=10.0
        while(r<max(w,h)){p.color=Color.WHITE; p.alpha=255-(r/max(w,h)*220).toInt(); c.drawCircle((cx+r*cos(a)).toFloat(),(cy+r*sin(a)).toFloat(),rnd.nextFloat()*5+1,p); a+=0.15; r+=0.8}
    }

    // --- NEW 10+ STYLES ---

    private fun drawCyberRain(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.parseColor("#000000"))
        p.style = Paint.Style.STROKE
        val rnd = Random(20)
        repeat(100) {
            p.color = Color.rgb(0, 255, rnd.nextInt(255))
            p.alpha = rnd.nextInt(200)
            val x = rnd.nextFloat() * w
            val y = rnd.nextFloat() * h
            val len = rnd.nextFloat() * 100 + 50
            c.drawLine(x, y, x, y + len, p)
        }
    }

    private fun drawNorthernLights(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.parseColor("#101820"))
        p.style = Paint.Style.FILL
        val rnd = Random(21)
        repeat(5) {
            val path = Path()
            path.moveTo(0f, rnd.nextFloat() * h)
            var x = 0f
            while (x < w) {
                x += 100
                path.lineTo(x, rnd.nextFloat() * h)
            }
            path.lineTo(w.toFloat(), h.toFloat())
            path.lineTo(0f, h.toFloat())
            path.close()
            p.shader = LinearGradient(0f, 0f, 0f, h.toFloat(), 
                Color.TRANSPARENT, Color.rgb(rnd.nextInt(50), 255, rnd.nextInt(255)), Shader.TileMode.CLAMP)
            c.drawPath(path, p)
        }
        p.shader = null
    }

    private fun drawGeometricHills(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.parseColor("#FFCCBC"))
        val rnd = Random(22)
        var y = h * 0.3f
        while (y < h + 200) {
            p.color = Color.rgb(rnd.nextInt(100), rnd.nextInt(50), rnd.nextInt(50))
            val path = Path()
            path.moveTo(0f, h.toFloat())
            path.lineTo(0f, y)
            path.lineTo(w/2f, y - 100)
            path.lineTo(w.toFloat(), y + 50)
            path.lineTo(w.toFloat(), h.toFloat())
            path.close()
            c.drawPath(path, p)
            y += 150
        }
    }

    private fun drawPaperCutout(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.WHITE)
        val rnd = Random(23)
        val colors = listOf("#E1BEE7", "#C5CAE9", "#B2DFDB", "#FFCCBC")
        var inset = 0f
        colors.forEach { col ->
            p.color = Color.parseColor(col)
            p.setShadowLayer(10f, 5f, 5f, Color.GRAY)
            c.drawRoundRect(inset, inset, w - inset, h - inset, 50f, 50f, p)
            inset += 80f
        }
        p.clearShadowLayer()
    }

    private fun drawDigitalDNA(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.parseColor("#0D47A1"))
        p.style = Paint.Style.STROKE
        p.strokeWidth = 5f
        p.color = Color.CYAN
        val cx = w/2f
        var y = 0f
        val period = 100f
        while(y < h) {
            val offset = sin(y / period) * 200
            c.drawCircle(cx + offset, y, 10f, p)
            c.drawCircle(cx - offset, y, 10f, p)
            c.drawLine(cx + offset, y, cx - offset, y, p)
            y += 30
        }
    }

    private fun drawVoidStare(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.BLACK)
        p.style = Paint.Style.STROKE
        p.color = Color.WHITE
        p.strokeWidth = 2f
        val cx = w/2f
        val cy = h/2f
        val maxR = max(w,h) * 0.8f
        var r = 0f
        while (r < maxR) {
            c.drawCircle(cx, cy, r, p)
            r += r * 0.2f + 10f
        }
    }

    private fun drawCrystalCave(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.parseColor("#4A148C"))
        p.style = Paint.Style.FILL
        val rnd = Random(26)
        repeat(20) {
            p.color = Color.argb(100, 255, 255, 255)
            val path = Path()
            val startX = rnd.nextFloat() * w
            val startY = h.toFloat()
            path.moveTo(startX, startY)
            path.lineTo(startX - 50, startY - rnd.nextFloat() * 400)
            path.lineTo(startX + 50, startY - rnd.nextFloat() * 400) // Triangle spike
            path.close()
            c.drawPath(path, p)
        }
    }

    private fun drawSolarFlare(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.parseColor("#BF360C"))
        p.color = Color.YELLOW
        p.alpha = 50
        val rnd = Random(27)
        repeat(100) {
            c.drawCircle(rnd.nextFloat()*w, rnd.nextFloat()*h, rnd.nextFloat()*100, p)
        }
    }

    private fun drawDottedLandscape(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.BLACK)
        p.color = Color.WHITE
        val gap = 40f
        for(x in 0 until (w/gap).toInt()) {
            for(y in 0 until (h/gap).toInt()) {
                val size = (sin(x * 0.1) * cos(y * 0.1) * 10 + 2).absoluteValue.toFloat()
                c.drawCircle(x*gap, y*gap, size, p)
            }
        }
    }

    private fun drawSoundWave(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.parseColor("#212121"))
        p.color = Color.GREEN
        p.strokeWidth = 5f
        val cy = h/2f
        var x = 0f
        val rnd = Random(29)
        while(x < w) {
            val amp = rnd.nextFloat() * 300
            c.drawLine(x, cy - amp, x, cy + amp, p)
            x += 10
        }
    }
    
    // 30
    private fun drawQuantumMesh(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.BLACK)
        p.color = Color.CYAN
        p.alpha = 60
        val rnd = Random(30)
        val points = List(30) { PointF(rnd.nextFloat()*w, rnd.nextFloat()*h) }
        points.forEach { p1 ->
            points.forEach { p2 ->
                val dist = hypot(p1.x - p2.x, p1.y - p2.y)
                if (dist < 300) {
                    c.drawLine(p1.x, p1.y, p2.x, p2.y, p)
                }
            }
        }
    }

    private fun drawOrigamiBirds(c: Canvas, w: Int, h: Int, p: Paint) {
        c.drawColor(Color.parseColor("#81D4FA"))
        p.color = Color.WHITE
        p.style = Paint.Style.FILL
        val rnd = Random(31)
        repeat(15) {
            val path = Path()
            val x = rnd.nextFloat() * w
            val y = rnd.nextFloat() * h
            path.moveTo(x, y)
            path.lineTo(x + 40, y + 20)
            path.lineTo(x, y + 10)
            path.lineTo(x - 40, y + 20)
            path.close()
            c.drawPath(path, p)
        }
    }

    private fun drawLavaLamp(c: Canvas, w: Int, h: Int, p: Paint) {
        p.shader = LinearGradient(0f, 0f, 0f, h.toFloat(), Color.RED, Color.YELLOW, Shader.TileMode.CLAMP)
        c.drawRect(0f,0f,w.toFloat(),h.toFloat(),p)
        p.shader = null
        p.color = Color.parseColor("#AAFF0000") // Darker blobs
        val rnd = Random(32)
        repeat(8) {
             c.drawCircle(rnd.nextFloat()*w, rnd.nextFloat()*h, rnd.nextFloat()*200 + 50, p)
        }
    }
}
