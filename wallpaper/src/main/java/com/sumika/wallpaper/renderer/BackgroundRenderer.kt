package com.sumika.wallpaper.renderer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import com.sumika.core.model.BackgroundSource
import com.sumika.core.rhythm.DayNightCycle
import com.sumika.core.rhythm.TimeOfDay

/**
 * 背景の描画
 */
class BackgroundRenderer {
    
    private val dayNightCycle = DayNightCycle()
    
    private val gradientPaint = Paint()
    private val overlayPaint = Paint().apply {
        isAntiAlias = true
    }
    
    // キャッシュされた背景画像
    private var cachedBitmap: Bitmap? = null
    private var cachedSource: BackgroundSource? = null
    
    // 星の位置（夜用）
    private val stars = List(50) { 
        Triple(
            Math.random().toFloat(),  // x
            Math.random().toFloat() * 0.6f,  // y (上半分に集中)
            Math.random().toFloat() * 2f + 1f  // size
        )
    }
    
    private val starPaint = Paint().apply {
        isAntiAlias = true
        color = 0xFFFFFFFF.toInt()
    }
    
    /**
     * 背景を描画
     */
    fun draw(canvas: Canvas, screenWidth: Int, screenHeight: Int) {
        // キャッシュされた画像があれば描画
        cachedBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            drawTimeOverlay(canvas, screenWidth, screenHeight)
            return
        }
        
        // デフォルト：時間帯に応じたグラデーション
        drawDefaultBackground(canvas, screenWidth, screenHeight)
    }
    
    /**
     * デフォルト背景（グラデーション）
     */
    private fun drawDefaultBackground(canvas: Canvas, width: Int, height: Int) {
        val (topColor, bottomColor) = dayNightCycle.getSkyColors()
        
        val gradient = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            topColor, bottomColor,
            Shader.TileMode.CLAMP
        )
        gradientPaint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradientPaint)
        
        // 夜は星を描画
        if (dayNightCycle.getCurrentTimeOfDay() == TimeOfDay.NIGHT) {
            drawStars(canvas, width, height)
        }
        
        // 地面
        drawGround(canvas, width, height)
    }
    
    /**
     * 星を描画
     */
    private fun drawStars(canvas: Canvas, width: Int, height: Int) {
        val brightness = dayNightCycle.getAmbientBrightness()
        val alpha = ((1f - brightness) * 255).toInt().coerceIn(0, 200)
        starPaint.alpha = alpha
        
        stars.forEach { (x, y, size) ->
            // 瞬き効果
            val twinkle = (System.currentTimeMillis() / 100 + (x * 1000).toInt()) % 20
            val twinkleAlpha = if (twinkle < 3) alpha / 2 else alpha
            starPaint.alpha = twinkleAlpha
            
            canvas.drawCircle(x * width, y * height, size, starPaint)
        }
    }
    
    /**
     * 地面を描画
     */
    private fun drawGround(canvas: Canvas, width: Int, height: Int) {
        val groundHeight = height * 0.15f
        val groundTop = height - groundHeight
        
        val timeOfDay = dayNightCycle.getCurrentTimeOfDay()
        val groundColor = when (timeOfDay) {
            TimeOfDay.MORNING -> 0xFF7CB342.toInt()   // 明るい緑
            TimeOfDay.AFTERNOON -> 0xFF558B2F.toInt() // 緑
            TimeOfDay.EVENING -> 0xFF33691E.toInt()   // 暗い緑
            TimeOfDay.NIGHT -> 0xFF1B3D1B.toInt()     // 深い緑
        }
        
        overlayPaint.color = groundColor
        canvas.drawRect(0f, groundTop, width.toFloat(), height.toFloat(), overlayPaint)
        
        // 草のライン
        overlayPaint.color = (groundColor and 0x00FFFFFF) or 0x66000000
        val grassLineY = groundTop + 5f
        canvas.drawLine(0f, grassLineY, width.toFloat(), grassLineY, overlayPaint.apply { 
            strokeWidth = 3f 
        })
    }
    
    /**
     * 時間帯に応じたオーバーレイ
     */
    private fun drawTimeOverlay(canvas: Canvas, width: Int, height: Int) {
        val brightness = dayNightCycle.getAmbientBrightness()
        
        // 夜は暗いオーバーレイ
        if (brightness < 0.5f) {
            val alpha = ((1f - brightness) * 0.5f * 255).toInt()
            overlayPaint.color = (alpha shl 24) or 0x000020  // 暗い青
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
        }
    }
    
    /**
     * 背景画像を設定
     */
    fun setBackgroundBitmap(bitmap: Bitmap?, source: BackgroundSource?) {
        cachedBitmap = bitmap
        cachedSource = source
    }
    
    /**
     * リソース解放
     */
    fun release() {
        cachedBitmap = null
        cachedSource = null
    }
}
