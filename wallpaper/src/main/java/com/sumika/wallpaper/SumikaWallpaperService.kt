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
import com.sumika.wallpaper.engine.TouchEvent
import com.sumika.wallpaper.engine.TouchHandler

/**
 * Sumika ライブ壁紙サービス
 * 
 * 描画は専用スレッド（RenderThread）で行い、メインスレッドをブロックしない。
 * FPSは状態に応じて動的に切り替える（Active:60fps, Idle:30fps, Sleep:10fps）。
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
        
        private val renderThread = RenderThread()
        private val scheduler = FrameScheduler()
        private val offsetManager = OffsetManager()
        
        private var visible = false
        private var surfaceReady = false
        private var screenWidth = 0
        private var screenHeight = 0
        
        // 仮のペット位置（画面比率 0.0-1.0）
        private var petX = 0.5f
        private var petY = 0.7f
        
        // タッチエフェクト用
        private var showHeart = false
        private var heartX = 0f
        private var heartY = 0f
        private var heartAlpha = 0f
        
        // 描画用Paint
        private val petPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
        }
        private val heartPaint = Paint().apply {
            isAntiAlias = true
            color = Color.RED
            textSize = 60f
            textAlign = Paint.Align.CENTER
        }
        private val debugPaint = Paint().apply {
            color = Color.WHITE
            textSize = 32f
            isAntiAlias = true
        }
        
        private val touchHandler = TouchHandler { event ->
            handleTouchEvent(event)
        }
        
        private val drawRunnable = object : Runnable {
            override fun run() {
                if (!visible || !surfaceReady) return
                
                // タイムアウトチェック（ACTIVE → IDLE）
                scheduler.checkActiveTimeout()
                
                // 描画実行
                draw()
                
                // 次フレームをスケジュール
                renderThread.postDelayed(this, scheduler.frameIntervalMs)
            }
        }
        
        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)
            renderThread.start()
            // Looperの準備を待つ
            renderThread.looper
            Log.d(TAG, "Engine created")
        }
        
        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            surfaceReady = true
            Log.d(TAG, "Surface created")
            startDrawingIfReady()
        }
        
        override fun onSurfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            screenWidth = width
            screenHeight = height
            Log.d(TAG, "Surface changed: ${width}x${height}")
        }
        
        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            surfaceReady = false
            stopDrawing()
            super.onSurfaceDestroyed(holder)
            Log.d(TAG, "Surface destroyed")
        }
        
        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            this.visible = visible
            Log.d(TAG, "Visibility changed: $visible")
            
            if (visible) {
                startDrawingIfReady()
            } else {
                stopDrawing()
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
            stopDrawing()
            renderThread.shutdown()
            super.onDestroy()
            Log.d(TAG, "Engine destroyed")
        }
        
        private fun handleTouchEvent(event: TouchEvent) {
            when (event) {
                is TouchEvent.Tap -> {
                    Log.d(TAG, "Tap at (${event.x}, ${event.y})")
                    // ♡エフェクト表示
                    showHeart = true
                    heartX = event.x
                    heartY = event.y
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
                    Log.d(TAG, "Swipe from (${event.startX}, ${event.startY}) to (${event.endX}, ${event.endY})")
                    // ペットを誘導移動
                    petX = (event.endX / screenWidth).coerceIn(0.1f, 0.9f)
                    petY = (event.endY / screenHeight).coerceIn(0.1f, 0.9f)
                }
            }
        }
        
        private fun startDrawingIfReady() {
            if (visible && surfaceReady) {
                scheduler.resetTime()
                renderThread.removeCallbacks(drawRunnable)
                renderThread.post(drawRunnable)
                Log.d(TAG, "Drawing started")
            }
        }
        
        private fun stopDrawing() {
            renderThread.removeCallbacks(drawRunnable)
            scheduler.resetTime()
            Log.d(TAG, "Drawing stopped")
        }
        
        private fun draw() {
            val dt = scheduler.calculateDeltaTime()
            
            // 状態更新
            update(dt)
            
            // 描画
            val holder = surfaceHolder ?: return
            val canvas: Canvas? = try {
                holder.lockCanvas()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to lock canvas: ${e.message}")
                null
            }
            
            canvas ?: return
            
            try {
                render(canvas, dt)
            } finally {
                try {
                    holder.unlockCanvasAndPost(canvas)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to unlock canvas: ${e.message}")
                }
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
            // 背景
            canvas.drawColor(0xFF1A1A2E.toInt())
            
            // ペット描画（仮：白い円）
            val petScreenX = offsetManager.toScreenX(petX, screenWidth)
            val petScreenY = offsetManager.toScreenY(petY, screenHeight)
            val petRadius = screenWidth * 0.08f
            canvas.drawCircle(petScreenX, petScreenY, petRadius, petPaint)
            
            // 寝床描画（仮：右下に小さい円）
            val nestX = offsetManager.toScreenX(0.85f, screenWidth)
            val nestY = offsetManager.toScreenY(0.85f, screenHeight)
            val nestPaint = Paint().apply {
                color = 0xFF4A4A6A.toInt()
                isAntiAlias = true
            }
            canvas.drawCircle(nestX, nestY, petRadius * 0.8f, nestPaint)
            
            // ハートエフェクト
            if (showHeart) {
                heartPaint.alpha = (heartAlpha * 255).toInt()
                canvas.drawText("♡", heartX, heartY - 50, heartPaint)
            }
            
            // デバッグ情報
            val fps = if (scheduler.frameIntervalMs > 0) 1000 / scheduler.frameIntervalMs else 0
            canvas.drawText(
                "FPS: $fps (${scheduler.currentState})",
                20f, 60f,
                debugPaint
            )
        }
    }
}
