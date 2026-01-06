package com.sumika.wallpaper.engine

import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Surfaceライフサイクル管理
 * 描画可否の判定とスレッドセーフな状態管理を提供
 */
class SurfaceLifecycleManager {
    
    companion object {
        private const val TAG = "SurfaceLifecycle"
    }
    
    enum class State {
        /** Surface未作成 */
        NOT_CREATED,
        /** Surface作成済み、描画可能 */
        READY,
        /** Surface変更中 */
        CHANGING,
        /** Surface破棄済み */
        DESTROYED
    }
    
    private val state = AtomicReference(State.NOT_CREATED)
    private val isVisible = AtomicBoolean(false)
    private val isDrawing = AtomicBoolean(false)
    
    var screenWidth: Int = 0
        private set
    var screenHeight: Int = 0
        private set
    
    /** 描画可能かどうか */
    val canDraw: Boolean
        get() = state.get() == State.READY && isVisible.get()
    
    /** 現在の状態 */
    val currentState: State
        get() = state.get()
    
    /**
     * 描画開始を試行
     * @return 描画を開始できた場合true（排他制御）
     */
    fun tryStartDrawing(): Boolean {
        return if (canDraw && isDrawing.compareAndSet(false, true)) {
            true
        } else {
            false
        }
    }
    
    /**
     * 描画終了をマーク
     */
    fun finishDrawing() {
        isDrawing.set(false)
    }
    
    /**
     * 描画中かどうか
     */
    fun isCurrentlyDrawing(): Boolean = isDrawing.get()
    
    fun onSurfaceCreated() {
        val prev = state.getAndSet(State.READY)
        Log.d(TAG, "Surface created (prev=$prev)")
    }
    
    fun onSurfaceChanged(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        state.set(State.READY)
        Log.d(TAG, "Surface changed: ${width}x${height}")
    }
    
    fun onSurfaceDestroyed() {
        val prev = state.getAndSet(State.DESTROYED)
        isDrawing.set(false)  // 強制的に描画フラグをリセット
        Log.d(TAG, "Surface destroyed (prev=$prev)")
    }
    
    fun onVisibilityChanged(visible: Boolean) {
        val prev = isVisible.getAndSet(visible)
        if (prev != visible) {
            Log.d(TAG, "Visibility: $prev → $visible (canDraw=$canDraw)")
        }
    }
    
    /**
     * 状態をリセット（Engine破棄時）
     */
    fun reset() {
        state.set(State.NOT_CREATED)
        isVisible.set(false)
        isDrawing.set(false)
        screenWidth = 0
        screenHeight = 0
        Log.d(TAG, "Reset")
    }
}
