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
import com.sumika.core.rhythm.DayNightCycle
import com.sumika.core.rhythm.TimeOfDay
import com.sumika.wallpaper.engine.FrameScheduler
import com.sumika.wallpaper.engine.OffsetManager
import com.sumika.wallpaper.engine.RenderThread
import com.sumika.wallpaper.engine.SurfaceLifecycleManager
import com.sumika.wallpaper.engine.TouchEvent
import com.sumika.wallpaper.engine.TouchHandler
import com.sumika.wallpaper.renderer.BackgroundRenderer
import com.sumika.wallpaper.renderer.EffectRenderer
import com.sumika.wallpaper.renderer.NestRenderer
import com.sumika.wallpaper.renderer.PetRenderer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.sqrt

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
        
        // 日内リズム
        private val dayNightCycle = DayNightCycle()
        
        // アニメーション＆行動
        private val animationController = AnimationController()
        private val petBehavior = PetBehavior(animationController)
        
        // レンダラー
        private var petRenderer: PetRenderer? = null
        private val effectRenderer = EffectRenderer()
        private val nestRenderer = NestRenderer()
        private val backgroundRenderer = BackgroundRenderer()
        
        // ペット状態監視
        private var petStateObserver: PetStateObserver? = null
        
        // 二重起動防止フラグ
        private val isDrawLoopRunning = AtomicBoolean(false)
        
        // ペット設定
        private var petType = PetType.CAT
        private var petVariation = 0
        
        // 寝床帰宅中フラグ
        private var isGoingToNest = false
        private var lastRhythmCheck = 0L
        
        // 描画用Paint
        private val debugPaint = Paint().apply {
            color = Color.WHITE
            textSize = 22f
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
            
            // ペット状態監視を開始
            petStateObserver = PetStateObserver(applicationContext).apply {
                onPetTypeChanged = { type, variation ->
                    petType = type
                    petVariation = variation
                    petRenderer?.loadSprite(type, variation)
                    Log.i(TAG, "Pet changed: $type variation=$variation")
                }
                onGrowthStageChanged = { stage ->
                    // レベルアップエフェクト
                    val screenX = offsetManager.toScreenX(petBehavior.posX, lifecycleManager.screenWidth)
                    val screenY = offsetManager.toScreenY(petBehavior.posY, lifecycleManager.screenHeight)
                    effectRenderer.addLevelUpEffect(screenX, screenY)
                    animationController.setState(AnimationState.LEVEL_UP)
                    Log.i(TAG, "Growth stage changed: $stage")
                }
                onFocusingChanged = { focusing ->
                    if (focusing) {
                        animationController.setState(AnimationState.FOCUS)
                    } else if (animationController.state == AnimationState.FOCUS) {
                        animationController.setState(AnimationState.IDLE)
                    }
                    Log.i(TAG, "Focus mode: $focusing")
                }
                onHomeLocationChanged = { x, y ->
                    petBehavior.homeX = x
                    petBehavior.homeY = y
                    Log.i(TAG, "Home location updated: $x, $y")
                }
                start()
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
            petStateObserver?.stop()
            petStateObserver = null
            petRenderer?.release()
            petRenderer = null
            backgroundRenderer.release()
            renderThread?.shutdown()
            renderThread = null
            super.onDestroy()
            Log.i(TAG, "Engine destroyed")
        }
        
        private fun handleTouchEvent(event: TouchEvent) {
            val screenWidth = lifecycleManager.screenWidth
            val screenHeight = lifecycleManager.screenHeight
            
            // 寝ている時はタップで起こす
            if (animationController.state == AnimationState.SLEEP) {
                if (event is TouchEvent.Tap || event is TouchEvent.DoubleTap) {
                    petBehavior.wakeUp()
                    isGoingToNest = false
                    return
                }
            }
            
            when (event) {
                is TouchEvent.Tap -> {
                    petBehavior.onPet()
                    effectRenderer.addHeartEffect(event.x, event.y)
                }
                is TouchEvent.LongPress -> {
                    petBehavior.onFeed()
                    effectRenderer.addFoodEffect(event.x, event.y)
                }
                is TouchEvent.DoubleTap -> {
                    petBehavior.onPlay()
                    effectRenderer.addPlayEffect(event.x, event.y)
                }
                is TouchEvent.Swipe -> {
                    if (screenWidth > 0 && screenHeight > 0) {
                        val worldX = offsetManager.toWorldX(event.endX, screenWidth)
                        val worldY = offsetManager.toWorldY(event.endY, screenHeight)
                        petBehavior.moveTo(worldX, worldY)
                        isGoingToNest = false  // 手動移動で帰宅キャンセル
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
                
                // 日内リズムチェック（1秒ごと）
                if (currentTimeMs - lastRhythmCheck > 1000) {
                    lastRhythmCheck = currentTimeMs
                    checkDayNightRhythm()
                }
                
                // 更新
                petBehavior.update(dt, currentTimeMs)
                
                // 寝床に到着したら寝る
                if (isGoingToNest && isNearNest()) {
                    petBehavior.sleep()
                    isGoingToNest = false
                }
                
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
        
        /**
         * 日内リズムチェック
         */
        private fun checkDayNightRhythm() {
            val shouldSleep = dayNightCycle.shouldGoToNest()
            val isSleeping = animationController.state == AnimationState.SLEEP
            
            if (shouldSleep && !isSleeping && !isGoingToNest) {
                // 寝床へ向かう
                isGoingToNest = true
                petBehavior.moveTo(nestRenderer.nestX, nestRenderer.nestY)
                Log.d(TAG, "Going to nest (time: ${dayNightCycle.getCurrentHour()}:00)")
            } else if (!shouldSleep && isSleeping && dayNightCycle.shouldBeAwake()) {
                // 起きる時間
                petBehavior.wakeUp()
                Log.d(TAG, "Waking up (time: ${dayNightCycle.getCurrentHour()}:00)")
            }
        }
        
        /**
         * 寝床の近くにいるか
         */
        private fun isNearNest(): Boolean {
            val dx = petBehavior.posX - nestRenderer.nestX
            val dy = petBehavior.posY - nestRenderer.nestY
            return sqrt(dx * dx + dy * dy) < 0.05f
        }
        
        private fun render(canvas: Canvas) {
            val screenWidth = lifecycleManager.screenWidth
            val screenHeight = lifecycleManager.screenHeight
            
            // 背景
            backgroundRenderer.draw(canvas, screenWidth, screenHeight)
            
            // 寝床描画
            val nestScreenX = offsetManager.toScreenX(nestRenderer.nestX, screenWidth)
            val nestScreenY = offsetManager.toScreenY(nestRenderer.nestY, screenHeight)
            val isPetSleeping = animationController.state == AnimationState.SLEEP
            nestRenderer.draw(canvas, nestScreenX, nestScreenY, screenWidth, isPetSleeping)
            
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
                screenHeight,
                petStateObserver?.currentGrowthStage ?: com.sumika.core.model.GrowthStage.BABY,
                petStateObserver?.isFocusing ?: false
            )
            
            // エフェクト描画
            effectRenderer.draw(canvas)
            
            // デバッグ情報
            drawDebugInfo(canvas)
        }
        
        private fun drawDebugInfo(canvas: Canvas) {
            val fps = if (scheduler.frameIntervalMs > 0) 1000 / scheduler.frameIntervalMs else 0
            val timeOfDay = dayNightCycle.getCurrentTimeOfDay()
            val hour = dayNightCycle.getCurrentHour()
            
            val lines = listOf(
                "FPS: $fps | ${scheduler.currentState}",
                "Time: $hour:00 ($timeOfDay)",
                "Anim: ${animationController.state}",
                "Pet: (%.2f, %.2f)".format(petBehavior.posX, petBehavior.posY)
            )
            
            lines.forEachIndexed { index, text ->
                canvas.drawText(text, 16f, 36f + index * 26f, debugPaint)
            }
        }
    }
}
