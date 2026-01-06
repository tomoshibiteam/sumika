package com.sumika.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.sumika.wallpaper.engine.FrameScheduler
import com.sumika.wallpaper.engine.OffsetManager
import com.sumika.wallpaper.engine.RenderThread
import com.sumika.wallpaper.engine.SurfaceLifecycleManager
import com.sumika.wallpaper.engine.TouchEvent
import com.sumika.wallpaper.engine.TouchHandler
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Sumika ライブ壁紙サービス
 * 
 * 描画は専用スレッド（RenderThread）で行い、メインスレッドをブロックしない。
 * FPSは状態に応じて動的に切り替える（Active:60fps, Idle:30fps, Sleep:10fps）。
 * 
 * 安全性:
 * - Surfaceライフサイクル管理で描画可否を厳密に判定
 * - lockCanvas失敗時の例外を適切にハンドル
 * - 可視/不可視の連打で二重起動しない
 */
class SumikaWallpaperService : WallpaperService() {
    
    companion object {
        private const val TAG = "SumikaWallpaper"
    }
    
    override fun onCreateEngine(): Engine = SumikaEngine()
    
    /**
     * 壁紙エンジン本体
     */
    inner class SumikaEngine : Engine() {
        
        private var renderThread: RenderThread? = null
        private val scheduler = FrameScheduler()
        private val offsetManager = OffsetManager()
        private val lifecycleManager = SurfaceLifecycleManager()
        
        // 二重起動防止フラグ
        private val isDrawLoopRunning = AtomicBoolean(false)
        
        // 仮のペット位置（ワールド座標 0.0-1.0）
        private var petWorldX = 0.5f
        private var petWorldY = 0.7f
        
        // 寝床位置（ワールド座標）
        private val nestWorldX = 0.85f
        private val nestWorldY = 0.85f
        
        // タッチエフェクト用
        private var showHeart = false
        private var heartScreenX = 0f
        private var heartScreenY = 0f
        private var heartAlpha = 0f
        
        // 描画用Paint
        private val petPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
        }
        private val nestPaint = Paint().apply {
            isAntiAlias = true
            color = 0xFF4A4A6A.toInt()
        }
        private val heartPaint = Paint().apply {
            isAntiAlias = true
            color = Color.RED
            textSize = 60f
            textAlign = Paint.Align.CENTER
        }
        private val debugPaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            isAntiAlias = true
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
                
                // タイムアウトチェック（ACTIVE → IDLE）
                scheduler.checkActiveTimeout()
                
                // 描画実行
                performDraw()
                
                // 次フレームをスケジュール
                renderThread?.postDelayed(this, scheduler.frameIntervalMs)
            }
        }
        
        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)
            
            // RenderThreadを起動
            renderThread = RenderThread().apply {
                start()
                looper // Looper準備を待つ
            }
            Log.i(TAG, "Engine created, RenderThread started")
        }
        
        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            lifecycleManager.onSurfaceCreated()
            tryStartDrawLoop()
        }
        
        override fun onSurfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int
        ) {
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
            
            if (visible) {
                tryStartDrawLoop()
            } else {
                stopDrawLoop()
            }
        }
        
        override fun onOffsetsChanged(
            xOffset: Float,
            yOffset: Float,
            xOffsetStep: Float,
            yOffsetStep: Float,
            xPixelOffset: Int,
            yPixelOffset: Int
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
                    Log.d(TAG, "Tap at (${event.x}, ${event.y})")
                    // ♡エフェクト表示
                    showHeart = true
                    heartScreenX = event.x
                    heartScreenY = event.y
                    heartAlpha = 1f
                }
                is TouchEvent.LongPress -> {
                    Log.d(TAG, "Long press at (${event.x}, ${event.y}) - Feed")
                    // TODO: 餌やりエフェクト
                }
                is TouchEvent.DoubleTap -> {
                    Log.d(TAG, "Double tap at (${event.x}, ${event.y}) - Play")
                    // TODO: 遊ぶエフェクト
                }
                is TouchEvent.Swipe -> {
                    Log.d(TAG, "Swipe to (${event.endX}, ${event.endY})")
                    // ペットを誘導移動（ワールド座標に変換）
                    if (screenWidth > 0 && screenHeight > 0) {
                        petWorldX = offsetManager.toWorldX(event.endX, screenWidth)
                            .coerceIn(0.1f, 0.9f)
                        petWorldY = offsetManager.toWorldY(event.endY, screenHeight)
                            .coerceIn(0.1f, 0.9f)
                    }
                }
            }
        }
        
        /**
         * 描画ループの開始を試行（二重起動防止）
         */
        private fun tryStartDrawLoop() {
            if (!lifecycleManager.canDraw) {
                Log.d(TAG, "Cannot start draw loop: canDraw=false")
                return
            }
            
            if (!isDrawLoopRunning.compareAndSet(false, true)) {
                Log.d(TAG, "Draw loop already running, skip")
                return
            }
            
            scheduler.resetTime()
            renderThread?.removeCallbacks(drawRunnable)
            renderThread?.post(drawRunnable)
            Log.i(TAG, "Draw loop started (state=${lifecycleManager.currentState})")
        }
        
        /**
         * 描画ループを停止
         */
        private fun stopDrawLoop() {
            renderThread?.removeCallbacks(drawRunnable)
            isDrawLoopRunning.set(false)
            scheduler.resetTime()
            Log.i(TAG, "Draw loop stopped")
        }
        
        /**
         * 実際の描画処理
         */
        private fun performDraw() {
            if (!lifecycleManager.tryStartDrawing()) {
                return
            }
            
            try {
                val dt = scheduler.calculateDeltaTime()
                update(dt)
                
                val holder = surfaceHolder ?: return
                val canvas: Canvas? = try {
                    holder.lockCanvas()
                } catch (e: IllegalStateException) {
                    Log.w(TAG, "lockCanvas failed (Surface not valid): ${e.message}")
                    null
                } catch (e: Exception) {
                    Log.w(TAG, "lockCanvas failed: ${e.message}")
                    null
                }
                
                if (canvas == null) {
                    return
                }
                
                try {
                    render(canvas, dt)
                } finally {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (e: IllegalStateException) {
                        Log.w(TAG, "unlockCanvas failed (Surface destroyed): ${e.message}")
                    } catch (e: Exception) {
                        Log.w(TAG, "unlockCanvas failed: ${e.message}")
                    }
                }
            } finally {
                lifecycleManager.finishDrawing()
            }
        }
        
        private fun update(dt: Float) {
            // ハートエフェクトのフェードアウト
            if (showHeart) {
                heartAlpha -= dt * 2f  // 0.5秒で消える
                if (heartAlpha <= 0f) {
                    showHeart = false
                    heartAlpha = 0f
                }
            }
        }
        
        private fun render(canvas: Canvas, dt: Float) {
            val screenWidth = lifecycleManager.screenWidth
            val screenHeight = lifecycleManager.screenHeight
            
            // 背景
            canvas.drawColor(0xFF1A1A2E.toInt())
            
            // 寝床描画
            val nestScreenX = offsetManager.toScreenX(nestWorldX, screenWidth)
            val nestScreenY = offsetManager.toScreenY(nestWorldY, screenHeight)
            val nestRadius = screenWidth * 0.06f
            canvas.drawCircle(nestScreenX, nestScreenY, nestRadius, nestPaint)
            
            // ペット描画（仮：白い円）
            val petScreenX = offsetManager.toScreenX(petWorldX, screenWidth)
            val petScreenY = offsetManager.toScreenY(petWorldY, screenHeight)
            val petRadius = screenWidth * 0.08f
            canvas.drawCircle(petScreenX, petScreenY, petRadius, petPaint)
            
            // ハートエフェクト
            if (showHeart) {
                heartPaint.alpha = (heartAlpha * 255).toInt()
                canvas.drawText("♡", heartScreenX, heartScreenY - 50, heartPaint)
            }
            
            // デバッグ情報
            val fps = if (scheduler.frameIntervalMs > 0) 1000 / scheduler.frameIntervalMs else 0
            val debugInfo = buildString {
                append("FPS: $fps (${scheduler.currentState})")
                append(" | Offset: %.2f".format(offsetManager.xOffset))
                append(" | Pages: ${offsetManager.estimatedPageCount}")
            }
            canvas.drawText(debugInfo, 20f, 50f, debugPaint)
            canvas.drawText(
                "Surface: ${lifecycleManager.currentState} | Pet: (%.2f, %.2f)".format(petWorldX, petWorldY),
                20f, 80f, debugPaint
            )
        }
    }
}
