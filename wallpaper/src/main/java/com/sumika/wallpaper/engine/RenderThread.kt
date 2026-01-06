package com.sumika.wallpaper.engine

import android.os.Handler
import android.os.HandlerThread
import android.util.Log

/**
 * 描画用の専用スレッド
 * メインスレッドをブロックせずに描画を行う
 */
class RenderThread(name: String = "SumikaRender") : HandlerThread(name) {
    
    private var handler: Handler? = null
    
    companion object {
        private const val TAG = "RenderThread"
    }
    
    override fun onLooperPrepared() {
        super.onLooperPrepared()
        handler = Handler(looper)
        Log.d(TAG, "Render thread looper prepared")
    }
    
    /**
     * タスクを即座に実行
     */
    fun post(task: Runnable): Boolean {
        return handler?.post(task) ?: run {
            Log.w(TAG, "Handler not ready, task dropped")
            false
        }
    }
    
    /**
     * 遅延実行
     */
    fun postDelayed(task: Runnable, delayMs: Long): Boolean {
        return handler?.postDelayed(task, delayMs) ?: run {
            Log.w(TAG, "Handler not ready, delayed task dropped")
            false
        }
    }
    
    /**
     * コールバック除去
     */
    fun removeCallbacks(task: Runnable) {
        handler?.removeCallbacks(task)
    }
    
    /**
     * 全ての保留中タスクを除去
     */
    fun removeAllCallbacks() {
        handler?.removeCallbacksAndMessages(null)
    }
    
    /**
     * スレッドを安全に終了
     */
    fun shutdown() {
        removeAllCallbacks()
        quitSafely()
        Log.d(TAG, "Render thread shutdown")
    }
}
