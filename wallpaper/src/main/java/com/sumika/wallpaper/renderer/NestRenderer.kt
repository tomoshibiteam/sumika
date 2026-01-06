package com.sumika.wallpaper.renderer

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import com.sumika.core.rhythm.DayNightCycle

/**
 * 寝床の描画
 */
class NestRenderer {
    
    private val dayNightCycle = DayNightCycle()
    
    // 寝床位置（ワールド座標 0.0-1.0）
    var nestX = 0.85f
    var nestY = 0.85f
    
    private val outerPaint = Paint().apply {
        isAntiAlias = true
    }
    
    private val innerPaint = Paint().apply {
        isAntiAlias = true
    }
    
    private val cushionPaint = Paint().apply {
        isAntiAlias = true
    }
    
    private val highlightPaint = Paint().apply {
        isAntiAlias = true
        color = 0x33FFFFFF
    }
    
    /**
     * 寝床を描画
     */
    fun draw(
        canvas: Canvas, 
        screenX: Float, 
        screenY: Float, 
        screenWidth: Int,
        isPetSleeping: Boolean
    ) {
        val nestRadius = screenWidth * 0.1f
        val brightness = dayNightCycle.getAmbientBrightness()
        
        // 時間帯に応じた色
        val baseColor = if (brightness > 0.5f) {
            0xFF8B7355.toInt()  // 明るい茶色
        } else {
            0xFF5D4E3A.toInt()  // 暗い茶色
        }
        
        val innerColor = if (brightness > 0.5f) {
            0xFF6B5344.toInt()
        } else {
            0xFF3D3428.toInt()
        }
        
        val cushionColor = if (isPetSleeping) {
            0xFFE8D5C4.toInt()  // ペットがいる時は温かみのある色
        } else {
            0xFFD4C4B0.toInt()
        }
        
        outerPaint.color = baseColor
        innerPaint.color = innerColor
        cushionPaint.color = cushionColor
        
        // 外枠（籠）
        canvas.drawOval(
            screenX - nestRadius,
            screenY - nestRadius * 0.5f,
            screenX + nestRadius,
            screenY + nestRadius * 0.6f,
            outerPaint
        )
        
        // 内側（くぼみ）
        val innerRadius = nestRadius * 0.85f
        canvas.drawOval(
            screenX - innerRadius,
            screenY - innerRadius * 0.4f,
            screenX + innerRadius,
            screenY + innerRadius * 0.5f,
            innerPaint
        )
        
        // クッション部分
        val cushionRadius = nestRadius * 0.7f
        canvas.drawOval(
            screenX - cushionRadius,
            screenY - cushionRadius * 0.3f,
            screenX + cushionRadius,
            screenY + cushionRadius * 0.4f,
            cushionPaint
        )
        
        // ハイライト（光沢）
        canvas.drawArc(
            screenX - nestRadius * 0.6f,
            screenY - nestRadius * 0.3f,
            screenX - nestRadius * 0.2f,
            screenY,
            200f, 80f, false,
            highlightPaint.apply { strokeWidth = 4f; style = Paint.Style.STROKE }
        )
    }
    
    /**
     * 寝床の位置を設定
     */
    fun setPosition(x: Float, y: Float) {
        nestX = x.coerceIn(0.1f, 0.95f)
        nestY = y.coerceIn(0.5f, 0.95f)
    }
}
