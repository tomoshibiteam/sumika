package com.sumika.wallpaper.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.SystemClock
import kotlin.math.cos
import kotlin.math.sin

/**
 * タッチエフェクトの描画
 */
class EffectRenderer {
    
    private val effects = mutableListOf<Effect>()
    
    private val heartPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val particlePaint = Paint().apply {
        isAntiAlias = true
    }
    
    sealed class Effect(
        val x: Float,
        val y: Float,
        val startTime: Long,
        val durationMs: Long
    ) {
        val age: Float get() = (SystemClock.elapsedRealtime() - startTime) / durationMs.toFloat()
        val isExpired: Boolean get() = age >= 1f
    }
    
    class HeartEffect(x: Float, y: Float) : Effect(x, y, SystemClock.elapsedRealtime(), 800L)
    class FoodEffect(x: Float, y: Float) : Effect(x, y, SystemClock.elapsedRealtime(), 600L)
    class PlayEffect(x: Float, y: Float) : Effect(x, y, SystemClock.elapsedRealtime(), 500L)
    class ParticleEffect(x: Float, y: Float, val color: Int) : Effect(x, y, SystemClock.elapsedRealtime(), 400L)
    
    /**
     * ♡エフェクトを追加
     */
    fun addHeartEffect(x: Float, y: Float) {
        effects.add(HeartEffect(x, y))
        // パーティクルも追加
        repeat(5) {
            effects.add(ParticleEffect(x, y, 0xFFFF6B6B.toInt()))
        }
    }
    
    /**
     * 餌エフェクトを追加
     */
    fun addFoodEffect(x: Float, y: Float) {
        effects.add(FoodEffect(x, y))
    }
    
    /**
     * 遊ぶエフェクトを追加
     */
    fun addPlayEffect(x: Float, y: Float) {
        effects.add(PlayEffect(x, y))
        repeat(8) {
            effects.add(ParticleEffect(x, y, 0xFFFFD93D.toInt()))
        }
    }
    
    /**
     * エフェクトを描画
     */
    fun draw(canvas: Canvas) {
        val iterator = effects.iterator()
        while (iterator.hasNext()) {
            val effect = iterator.next()
            if (effect.isExpired) {
                iterator.remove()
                continue
            }
            
            when (effect) {
                is HeartEffect -> drawHeart(canvas, effect)
                is FoodEffect -> drawFood(canvas, effect)
                is PlayEffect -> drawPlay(canvas, effect)
                is ParticleEffect -> drawParticle(canvas, effect)
            }
        }
    }
    
    private fun drawHeart(canvas: Canvas, effect: HeartEffect) {
        val progress = effect.age
        val alpha = ((1f - progress) * 255).toInt()
        val scale = 1f + progress * 0.5f
        val yOffset = progress * 80f
        
        heartPaint.color = 0xFFFF6B6B.toInt()
        heartPaint.alpha = alpha
        
        val size = 40f * scale
        val x = effect.x
        val y = effect.y - yOffset
        
        // ハート形状を描画
        val path = Path()
        path.moveTo(x, y - size * 0.3f)
        path.cubicTo(
            x - size * 0.5f, y - size,
            x - size, y - size * 0.3f,
            x, y + size * 0.5f
        )
        path.cubicTo(
            x + size, y - size * 0.3f,
            x + size * 0.5f, y - size,
            x, y - size * 0.3f
        )
        path.close()
        
        canvas.drawPath(path, heartPaint)
    }
    
    private fun drawFood(canvas: Canvas, effect: FoodEffect) {
        val progress = effect.age
        val alpha = ((1f - progress) * 255).toInt()
        val yOffset = progress * 40f
        
        heartPaint.color = 0xFF8B4513.toInt()  // 茶色（餌）
        heartPaint.alpha = alpha
        
        val x = effect.x
        val y = effect.y - yOffset
        val size = 15f
        
        // 餌の形（楕円）
        canvas.drawOval(x - size, y - size * 0.6f, x + size, y + size * 0.6f, heartPaint)
    }
    
    private fun drawPlay(canvas: Canvas, effect: PlayEffect) {
        val progress = effect.age
        val alpha = ((1f - progress) * 255).toInt()
        val scale = 1f + progress * 0.8f
        
        heartPaint.color = 0xFFFFD93D.toInt()  // 黄色（星）
        heartPaint.alpha = alpha
        
        val x = effect.x
        val y = effect.y - progress * 60f
        
        // 星を描画
        drawStar(canvas, x, y, 25f * scale, 5, heartPaint)
    }
    
    private fun drawParticle(canvas: Canvas, effect: ParticleEffect) {
        val progress = effect.age
        val alpha = ((1f - progress) * 255).toInt()
        
        // ランダムな方向に飛び散る
        val angle = (effect.hashCode() % 360) * Math.PI / 180
        val distance = progress * 100f
        val x = effect.x + (cos(angle) * distance).toFloat()
        val y = effect.y + (sin(angle) * distance).toFloat() - progress * 50f
        
        particlePaint.color = effect.color
        particlePaint.alpha = alpha
        
        val size = 8f * (1f - progress * 0.5f)
        canvas.drawCircle(x, y, size, particlePaint)
    }
    
    private fun drawStar(canvas: Canvas, cx: Float, cy: Float, radius: Float, points: Int, paint: Paint) {
        val path = Path()
        val innerRadius = radius * 0.4f
        
        for (i in 0 until points * 2) {
            val r = if (i % 2 == 0) radius else innerRadius
            val angle = Math.PI / 2 + i * Math.PI / points
            val x = cx + (cos(angle) * r).toFloat()
            val y = cy - (sin(angle) * r).toFloat()
            
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }
    
    /**
     * 全エフェクトをクリア
     */
    fun clear() {
        effects.clear()
    }
}
