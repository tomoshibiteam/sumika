package com.sumika.wallpaper.engine

import android.view.MotionEvent

/**
 * タッチイベントの種類
 */
sealed class TouchEvent {
    /** シングルタップ（撫でる） */
    data class Tap(val x: Float, val y: Float) : TouchEvent()
    
    /** 長押し（餌やり） */
    data class LongPress(val x: Float, val y: Float) : TouchEvent()
    
    /** ダブルタップ（遊ぶ） */
    data class DoubleTap(val x: Float, val y: Float) : TouchEvent()
    
    /** スワイプ（誘導移動） */
    data class Swipe(
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float
    ) : TouchEvent()
}

/**
 * タッチイベントを解析してTouchEventに変換
 */
class TouchHandler(
    private val onTouchEvent: (TouchEvent) -> Unit
) {
    companion object {
        private const val LONG_PRESS_TIMEOUT = 500L
        private const val DOUBLE_TAP_TIMEOUT = 300L
        private const val SWIPE_THRESHOLD = 100f
    }
    
    private var touchDownTime = 0L
    private var touchDownX = 0f
    private var touchDownY = 0f
    private var lastTapTime = 0L
    private var lastTapX = 0f
    private var lastTapY = 0f
    private var isLongPressTriggered = false
    
    /**
     * MotionEventを処理
     * @return イベントが処理された場合true
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchDownTime = System.currentTimeMillis()
                touchDownX = event.x
                touchDownY = event.y
                isLongPressTriggered = false
            }
            
            MotionEvent.ACTION_UP -> {
                val duration = System.currentTimeMillis() - touchDownTime
                val dx = event.x - touchDownX
                val dy = event.y - touchDownY
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                
                when {
                    // 長押し判定（すでにトリガー済みならスキップ）
                    duration >= LONG_PRESS_TIMEOUT && !isLongPressTriggered -> {
                        // 長押しはmoveで判定済みなのでここでは何もしない
                    }
                    
                    // スワイプ判定
                    distance >= SWIPE_THRESHOLD -> {
                        onTouchEvent(TouchEvent.Swipe(touchDownX, touchDownY, event.x, event.y))
                    }
                    
                    // ダブルタップ判定
                    else -> {
                        val timeSinceLastTap = System.currentTimeMillis() - lastTapTime
                        val distanceFromLastTap = kotlin.math.sqrt(
                            (event.x - lastTapX) * (event.x - lastTapX) +
                            (event.y - lastTapY) * (event.y - lastTapY)
                        )
                        
                        if (timeSinceLastTap < DOUBLE_TAP_TIMEOUT && distanceFromLastTap < 100f) {
                            onTouchEvent(TouchEvent.DoubleTap(event.x, event.y))
                            lastTapTime = 0L  // リセット
                        } else {
                            // シングルタップ（次のタップがダブルタップになる可能性があるため記録）
                            onTouchEvent(TouchEvent.Tap(event.x, event.y))
                            lastTapTime = System.currentTimeMillis()
                            lastTapX = event.x
                            lastTapY = event.y
                        }
                    }
                }
            }
            
            MotionEvent.ACTION_MOVE -> {
                // 長押し判定
                val duration = System.currentTimeMillis() - touchDownTime
                val dx = event.x - touchDownX
                val dy = event.y - touchDownY
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                
                if (duration >= LONG_PRESS_TIMEOUT && distance < SWIPE_THRESHOLD && !isLongPressTriggered) {
                    isLongPressTriggered = true
                    onTouchEvent(TouchEvent.LongPress(touchDownX, touchDownY))
                }
            }
        }
        return true
    }
}
