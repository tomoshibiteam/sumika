package com.sumika.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
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
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.sumika.MainActivity

/**
 * 集中タイマーサービス
 * Foreground Serviceで精度保証
 * 
 * Android 14+ (targetSdk 34+) 対応:
 * - foregroundServiceType を SPECIAL_USE として宣言
 * - POST_NOTIFICATIONS 権限チェックとフォールバック
 */
class FocusTimerService : Service() {
    
    companion object {
        private const val TAG = "FocusTimerService"
        private const val NOTIFICATION_ID = 1001
        private const val COMPLETION_NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "focus_timer_channel"
        
        const val ACTION_START = "com.sumika.action.START_TIMER"
        const val ACTION_STOP = "com.sumika.action.STOP_TIMER"
        const val EXTRA_DURATION_MS = "duration_ms"
        
        const val DEFAULT_DURATION_MS = 25 * 60 * 1000L  // 25分
        
        /**
         * 通知権限が付与されているかチェック
         */
        fun hasNotificationPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true  // Android 12以下は権限不要
            }
        }
    }
    
    private val binder = LocalBinder()
    private val handler = Handler(Looper.getMainLooper())
    
    private var remainingMs = 0L
    private var isRunning = false
    private var hasNotificationPermission = false
    
    var onTick: ((Long) -> Unit)? = null
    var onComplete: (() -> Unit)? = null
    
    inner class LocalBinder : Binder() {
        fun getService(): FocusTimerService = this@FocusTimerService
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        hasNotificationPermission = hasNotificationPermission(this)
        Log.i(TAG, "Service created, notification permission: $hasNotificationPermission")
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
        if (isRunning) {
            Log.w(TAG, "Timer already running, ignoring start request")
            return
        }
        
        remainingMs = durationMs
        isRunning = true
        
        // Foreground Service開始
        // Android 14+では foregroundServiceType の指定が必須
        try {
            val notification = createProgressNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+: foregroundServiceType を明示的に指定
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            Log.i(TAG, "Timer started: ${durationMs}ms, foreground service active")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground: ${e.message}")
            // フォールバック: バックグラウンドで続行（精度は落ちる）
        }
        
        scheduleNextTick()
    }
    
    private fun scheduleNextTick() {
        if (!isRunning) return
        
        handler.postDelayed({
            if (!isRunning) return@postDelayed
            
            remainingMs -= 1000
            onTick?.invoke(remainingMs)
            
            // 通知更新（権限がある場合のみ）
            if (hasNotificationPermission) {
                updateProgressNotification()
            }
            
            if (remainingMs <= 0) {
                onTimerComplete()
            } else {
                scheduleNextTick()
            }
        }, 1000)
    }
    
    private fun onTimerComplete() {
        isRunning = false
        Log.i(TAG, "Timer completed!")
        
        // バイブレーション
        vibrate()
        
        // 完了通知（権限がある場合のみ）
        if (hasNotificationPermission) {
            showCompletionNotification()
        }
        
        // コールバック
        onComplete?.invoke()
        
        // TODO: ペット状態を更新（Repository経由）
        
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }
    
    fun stopTimer() {
        if (!isRunning) return
        
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        Log.i(TAG, "Timer stopped by user")
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
    
    private fun createProgressNotification(): android.app.Notification {
        val minutes = (remainingMs / 1000 / 60).toInt()
        val seconds = ((remainingMs / 1000) % 60).toInt()
        
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, FocusTimerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎯 集中中...")
            .setContentText("残り %02d:%02d".format(minutes, seconds))
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "停止", stopPendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .build()
    }
    
    private fun updateProgressNotification() {
        if (!hasNotificationPermission) return
        
        try {
            val manager = NotificationManagerCompat.from(this)
            manager.notify(NOTIFICATION_ID, createProgressNotification())
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission revoked during timer")
            hasNotificationPermission = false
        }
    }
    
    private fun showCompletionNotification() {
        if (!hasNotificationPermission) return
        
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎉 集中完了！")
            .setContentText("ペットが喜んでいます！成長XPを獲得しました。")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        try {
            val manager = NotificationManagerCompat.from(this)
            manager.notify(COMPLETION_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Cannot show completion notification: ${e.message}")
        }
    }
    
    private fun vibrate() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                manager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            
            val effect = VibrationEffect.createWaveform(
                longArrayOf(0, 300, 200, 300),  // パターン: 振動-休止-振動
                -1  // 繰り返しなし
            )
            vibrator.vibrate(effect)
        } catch (e: Exception) {
            Log.w(TAG, "Vibration failed: ${e.message}")
        }
    }
    
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        Log.i(TAG, "Service destroyed")
        super.onDestroy()
    }
}
