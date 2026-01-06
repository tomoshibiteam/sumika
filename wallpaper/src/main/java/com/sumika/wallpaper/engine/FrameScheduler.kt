package com.sumika.wallpaper.engine

import android.os.SystemClock
import android.util.Log

/**
 * 描画状態
 */
enum class RenderState {
    /** アクティブ（インタラクション中）: 60fps */
    ACTIVE,
    /** 通常: 30fps */
    IDLE,
    /** 静止/睡眠: 10fps */
    SLEEP
}

/**
 * フレームスケジューラ
 * 状態に応じてFPSを動的に切り替え、dt（デルタタイム）ベースの更新を行う
 */
class FrameScheduler {
    
    companion object {
        private const val TAG = "FrameScheduler"
        
        // 各状態のフレーム間隔 (ms)
        private const val INTERVAL_ACTIVE = 16L   // ~60fps
        private const val INTERVAL_IDLE = 33L     // ~30fps
        private const val INTERVAL_SLEEP = 100L   // ~10fps
        
        // 最大デルタタイム（停止→復帰時の爆発防止）
        private const val MAX_DELTA_TIME = 0.1f   // 100ms
        
        // ACTIVE → IDLE への自動遷移時間
        const val ACTIVE_TIMEOUT_MS = 2000L
    }
    
    private var state = RenderState.IDLE
    private var lastFrameTime = 0L
    private var stateChangeTime = 0L
    
    /** 現在の状態 */
    val currentState: RenderState get() = state
    
    /** 現在のフレーム間隔 (ms) */
    val frameIntervalMs: Long get() = when (state) {
        RenderState.ACTIVE -> INTERVAL_ACTIVE
        RenderState.IDLE -> INTERVAL_IDLE
        RenderState.SLEEP -> INTERVAL_SLEEP
    }
    
    /**
     * インタラクション発生時に呼び出し
     * ACTIVE状態へ遷移
     */
    fun onInteraction() {
        if (state != RenderState.ACTIVE) {
            Log.d(TAG, "State: $state → ACTIVE")
        }
        state = RenderState.ACTIVE
        stateChangeTime = SystemClock.elapsedRealtime()
    }
    
    /**
     * 通常状態へ遷移
     */
    fun onIdle() {
        if (state != RenderState.IDLE) {
            Log.d(TAG, "State: $state → IDLE")
            state = RenderState.IDLE
            stateChangeTime = SystemClock.elapsedRealtime()
        }
    }
    
    /**
     * スリープ状態へ遷移（ペットが寝ている時など）
     */
    fun onSleep() {
        if (state != RenderState.SLEEP) {
            Log.d(TAG, "State: $state → SLEEP")
            state = RenderState.SLEEP
            stateChangeTime = SystemClock.elapsedRealtime()
        }
    }
    
    /**
     * ACTIVE状態のタイムアウトをチェック
     * タイムアウトしていたらIDLEへ自動遷移
     */
    fun checkActiveTimeout() {
        if (state == RenderState.ACTIVE) {
            val elapsed = SystemClock.elapsedRealtime() - stateChangeTime
            if (elapsed > ACTIVE_TIMEOUT_MS) {
                onIdle()
            }
        }
    }
    
    /**
     * デルタタイム（前フレームからの経過時間）を計算
     * 長時間停止からの復帰時は MAX_DELTA_TIME でクランプ
     * 
     * @return 経過時間（秒）
     */
    fun calculateDeltaTime(): Float {
        val now = SystemClock.elapsedRealtime()
        val dt = if (lastFrameTime == 0L) {
            0f
        } else {
            (now - lastFrameTime) / 1000f
        }
        lastFrameTime = now
        return dt.coerceAtMost(MAX_DELTA_TIME)
    }
    
    /**
     * 時間をリセット（描画停止時に呼び出し）
     */
    fun resetTime() {
        lastFrameTime = 0L
    }
}
