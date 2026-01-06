package com.sumika.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sumika.MainActivity
import com.sumika.R

/**
 * 集中タイマーサービス
 * Foreground Serviceで精度保証
 */
class FocusTimerService : Service() {
    
    companion object {
        private const val TAG = "FocusTimerService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "focus_timer_channel"
        
        const val ACTION_START = "com.sumika.action.START_TIMER"
        const val ACTION_STOP = "com.sumika.action.STOP_TIMER"
        const val EXTRA_DURATION_MS = "duration_ms"
        
        const val DEFAULT_DURATION_MS = 25 * 60 * 1000L  // 25分
    }
    
    private val binder = LocalBinder()
    private val handler = Handler(Looper.getMainLooper())
    
    private var remainingMs = 0L
    private var isRunning = false
    
    var onTick: ((Long) -> Unit)? = null
    var onComplete: (() -> Unit)? = null
    
    inner class LocalBinder : Binder() {
        fun getService(): FocusTimerService = this@FocusTimerService
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val durationMs = intent.getLongExtra(EXTRA_DURATION_MS, DEFAULT_DURATION_MS)
                startTimer(durationMs)
            }
            ACTION_STOP -> {
                stopTimer()
            }
        }
        return START_NOT_STICKY
    }
    
    private fun startTimer(durationMs: Long) {
        if (isRunning) return
        
        remainingMs = durationMs
        isRunning = true
        
        startForeground(NOTIFICATION_ID, createNotification())
        scheduleNextTick()
        
        Log.d(TAG, "Timer started: ${durationMs}ms")
    }
    
    private fun scheduleNextTick() {
        if (!isRunning) return
        
        handler.postDelayed({
            if (!isRunning) return@postDelayed
            
            remainingMs -= 1000
            onTick?.invoke(remainingMs)
            updateNotification()
            
            if (remainingMs <= 0) {
                onTimerComplete()
            } else {
                scheduleNextTick()
            }
        }, 1000)
    }
    
    private fun onTimerComplete() {
        isRunning = false
        
        // バイブレーション
        vibrate()
        
        // 完了通知
        showCompletionNotification()
        
        // コールバック
        onComplete?.invoke()
        
        // TODO: ペット状態を更新（Repository経由）
        
        Log.d(TAG, "Timer completed!")
        
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }
    
    fun stopTimer() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        Log.d(TAG, "Timer stopped")
    }
    
    fun getRemainingMs(): Long = remainingMs
    fun isTimerRunning(): Boolean = isRunning
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "集中タイマー",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "集中タイマーの進行状況を表示します"
            setShowBadge(false)
        }
        
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
    
    private fun createNotification(): android.app.Notification {
        val minutes = (remainingMs / 1000 / 60).toInt()
        val seconds = ((remainingMs / 1000) % 60).toInt()
        
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("集中中...")
            .setContentText("残り %02d:%02d".format(minutes, seconds))
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
    
    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification())
    }
    
    private fun showCompletionNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎉 集中完了！")
            .setContentText("ペットが喜んでいます！")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID + 1, notification)
    }
    
    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        val effect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)
    }
    
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
