package com.sumika.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.SystemClock
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.sumika.core.animation.AnimationController
import com.sumika.core.animation.AnimationState
import com.sumika.core.animation.PetBehavior
import com.sumika.core.model.PetType
import com.sumika.wallpaper.engine.FrameScheduler
import com.sumika.wallpaper.engine.OffsetManager
import com.sumika.wallpaper.engine.RenderThread
import com.sumika.wallpaper.engine.SurfaceLifecycleManager
import com.sumika.wallpaper.engine.TouchEvent
import com.sumika.wallpaper.engine.TouchHandler
import com.sumika.wallpaper.renderer.EffectRenderer
import com.sumika.wallpaper.renderer.PetRenderer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Sumika ライブ壁紙サービス
 */
class SumikaWallpaperService : WallpaperService() {
    
    companion object {
        private const val TAG = "SumikaWallpaper"
    }
    
    override fun onCreateEngine(): Engine = SumikaEngine()
    
    inner class SumikaEngine : Engine() {
        
        private var renderThread: RenderThread? = null
        private val scheduler = FrameScheduler()
        private val offsetManager = OffsetManager()
        private val lifecycleManager = SurfaceLifecycleManager()
        
        // アニメーション＆行動
        private val animationController = AnimationController()
        private val petBehavior = PetBehavior(animationController)
        
        // レンダラー
        private var petRenderer: PetRenderer? = null
        private val effectRenderer = EffectRenderer()
        
        // 二重起動防止フラグ
        private val isDrawLoopRunning = AtomicBoolean(false)
        
        // ペット設定
        private var petType = PetType.CAT
        private var petVariation = 0
        
        // 寝床位置（ワールド座標）
        private val nestWorldX = 0.85f
        private val nestWorldY = 0.85f
        
        // 描画用Paint
        private val nestPaint = Paint().apply {
            isAntiAlias = true
            color = 0xFF4A4A6A.toInt()
        }
        private val nestInnerPaint = Paint().apply {
            isAntiAlias = true
            color = 0xFF3A3A5A.toInt()
        }
        private val debugPaint = Paint().apply {
            color = Color.WHITE
            textSize = 24f
            isAntiAlias = true
            setShadowLayer(2f, 1f, 1f, Color.BLACK)
        }
        
        private val touchHandler = TouchHandler { event ->
            handleTouchEvent(event)
        }
        
        private val drawRunnable = object : Runnable {
            override fun run() {
                if (!lifecycleManager.canDraw) {
                    Log.d(TAG, "Draw loop stopping: canDraw=false")
                    isDrawLoopRunning.set(false)
                    return
                }
                
                scheduler.checkActiveTimeout()
                performDraw()
                renderThread?.postDelayed(this, scheduler.frameIntervalMs)
            }
        }
        
        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)
            
            renderThread = RenderThread().apply {
                start()
                looper
            }
            
            petRenderer = PetRenderer(applicationContext).apply {
                loadSprite(petType, petVariation)
            }
            
            Log.i(TAG, "Engine created")
        }
        
        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            lifecycleManager.onSurfaceCreated()
            tryStartDrawLoop()
        }
        
        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            lifecycleManager.onSurfaceChanged(width, height)
        }
        
        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            lifecycleManager.onSurfaceDestroyed()
            stopDrawLoop()
            super.onSurfaceDestroyed(holder)
        }
        
        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            lifecycleManager.onVisibilityChanged(visible)
            
            if (visible) tryStartDrawLoop() else stopDrawLoop()
        }
        
        override fun onOffsetsChanged(
            xOffset: Float, yOffset: Float,
            xOffsetStep: Float, yOffsetStep: Float,
            xPixelOffset: Int, yPixelOffset: Int
        ) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset)
            offsetManager.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep)
        }
        
        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
            touchHandler.onTouchEvent(event)
            scheduler.onInteraction()
        }
        
        override fun onDestroy() {
            Log.i(TAG, "Engine destroying...")
            stopDrawLoop()
            lifecycleManager.reset()
            petRenderer?.release()
            petRenderer = null
            renderThread?.shutdown()
            renderThread = null
            super.onDestroy()
            Log.i(TAG, "Engine destroyed")
        }
        
        private fun handleTouchEvent(event: TouchEvent) {
            val screenWidth = lifecycleManager.screenWidth
            val screenHeight = lifecycleManager.screenHeight
            
            when (event) {
                is TouchEvent.Tap -> {
                    Log.d(TAG, "Tap - Pet")
                    petBehavior.onPet()
                    effectRenderer.addHeartEffect(event.x, event.y)
                }
                is TouchEvent.LongPress -> {
                    Log.d(TAG, "LongPress - Feed")
                    petBehavior.onFeed()
                    effectRenderer.addFoodEffect(event.x, event.y)
                }
                is TouchEvent.DoubleTap -> {
                    Log.d(TAG, "DoubleTap - Play")
                    petBehavior.onPlay()
                    effectRenderer.addPlayEffect(event.x, event.y)
                }
                is TouchEvent.Swipe -> {
                    Log.d(TAG, "Swipe - MoveTo")
                    if (screenWidth > 0 && screenHeight > 0) {
                        val worldX = offsetManager.toWorldX(event.endX, screenWidth)
                        val worldY = offsetManager.toWorldY(event.endY, screenHeight)
                        petBehavior.moveTo(worldX, worldY)
                    }
                }
            }
        }
        
        private fun tryStartDrawLoop() {
            if (!lifecycleManager.canDraw) return
            if (!isDrawLoopRunning.compareAndSet(false, true)) return
            
            scheduler.resetTime()
            renderThread?.removeCallbacks(drawRunnable)
            renderThread?.post(drawRunnable)
            Log.i(TAG, "Draw loop started")
        }
        
        private fun stopDrawLoop() {
            renderThread?.removeCallbacks(drawRunnable)
            isDrawLoopRunning.set(false)
            scheduler.resetTime()
            Log.i(TAG, "Draw loop stopped")
        }
        
        private fun performDraw() {
            if (!lifecycleManager.tryStartDrawing()) return
            
            try {
                val dt = scheduler.calculateDeltaTime()
                val currentTimeMs = SystemClock.elapsedRealtime()
                
                // 更新
                petBehavior.update(dt, currentTimeMs)
                
                // スリープ時はFPS下げる
                if (animationController.state == AnimationState.SLEEP) {
                    scheduler.onSleep()
                } else if (animationController.state == AnimationState.IDLE && 
                           animationController.stateElapsedMs > 5000) {
                    scheduler.onIdle()
                }
                
                val holder = surfaceHolder ?: return
                val canvas: Canvas? = try {
                    holder.lockCanvas()
                } catch (e: Exception) {
                    Log.w(TAG, "lockCanvas failed: ${e.message}")
                    null
                }
                
                canvas ?: return
                
                try {
                    render(canvas)
                } finally {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        Log.w(TAG, "unlockCanvas failed: ${e.message}")
                    }
                }
            } finally {
                lifecycleManager.finishDrawing()
            }
        }
        
        private fun render(canvas: Canvas) {
            val screenWidth = lifecycleManager.screenWidth
            val screenHeight = lifecycleManager.screenHeight
            
            // 背景
            canvas.drawColor(0xFF1A1A2E.toInt())
            
            // 寝床描画
            drawNest(canvas, screenWidth, screenHeight)
            
            // ペット描画
            val petScreenX = offsetManager.toScreenX(petBehavior.posX, screenWidth)
            val petScreenY = offsetManager.toScreenY(petBehavior.posY, screenHeight)
            
            petRenderer?.draw(
                canvas,
                petBehavior,
                animationController,
                petScreenX,
                petScreenY,
                screenWidth,
                screenHeight
            )
            
            // エフェクト描画
            effectRenderer.draw(canvas)
            
            // デバッグ情報
            drawDebugInfo(canvas)
        }
        
        private fun drawNest(canvas: Canvas, screenWidth: Int, screenHeight: Int) {
            val nestScreenX = offsetManager.toScreenX(nestWorldX, screenWidth)
            val nestScreenY = offsetManager.toScreenY(nestWorldY, screenHeight)
            val nestRadius = screenWidth * 0.08f
            
            // 外側
            canvas.drawCircle(nestScreenX, nestScreenY, nestRadius, nestPaint)
            // 内側（くぼみ）
            canvas.drawCircle(nestScreenX, nestScreenY, nestRadius * 0.7f, nestInnerPaint)
        }
        
        private fun drawDebugInfo(canvas: Canvas) {
            val fps = if (scheduler.frameIntervalMs > 0) 1000 / scheduler.frameIntervalMs else 0
            val lines = listOf(
                "FPS: $fps (${scheduler.currentState})",
                "Anim: ${animationController.state} F${animationController.frame}",
                "Pet: (%.2f, %.2f) %s".format(
                    petBehavior.posX, petBehavior.posY,
                    if (petBehavior.facingRight) "→" else "←"
                )
            )
            
            lines.forEachIndexed { index, text ->
                canvas.drawText(text, 16f, 40f + index * 28f, debugPaint)
            }
        }
    }
}
