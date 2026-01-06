package com.sumika.core.rhythm

import java.util.Calendar

/**
 * 時間帯
 */
enum class TimeOfDay {
    /** 朝 (6:00-11:59) */
    MORNING,
    /** 昼 (12:00-17:59) */
    AFTERNOON,
    /** 夕方 (18:00-20:59) */
    EVENING,
    /** 夜 (21:00-5:59) */
    NIGHT
}

/**
 * 日内リズム管理
 * 現実時間に基づいてペットの状態を制御
 */
class DayNightCycle {
    
    companion object {
        private const val MORNING_START = 6
        private const val AFTERNOON_START = 12
        private const val EVENING_START = 18
        private const val NIGHT_START = 21
        private const val LATE_NIGHT_END = 6
    }
    
    /**
     * 現在の時間帯を取得
     */
    fun getCurrentTimeOfDay(): TimeOfDay {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return getTimeOfDay(hour)
    }
    
    /**
     * 指定時間の時間帯を取得
     */
    fun getTimeOfDay(hour: Int): TimeOfDay {
        return when (hour) {
            in MORNING_START until AFTERNOON_START -> TimeOfDay.MORNING
            in AFTERNOON_START until EVENING_START -> TimeOfDay.AFTERNOON
            in EVENING_START until NIGHT_START -> TimeOfDay.EVENING
            else -> TimeOfDay.NIGHT
        }
    }
    
    /**
     * 現在の時間（0-23）を取得
     */
    fun getCurrentHour(): Int {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    }
    
    /**
     * 眠気レベルを計算（0.0-1.0）
     * 夜が深まるほど眠気が増す
     */
    fun calculateSleepPressure(): Float {
        val hour = getCurrentHour()
        return when {
            hour >= 23 || hour < 2 -> 0.9f   // 深夜
            hour in 21..22 -> 0.6f           // 夜
            hour in 2..5 -> 1.0f             // 未明（寝ているべき）
            hour in 6..8 -> 0.3f             // 早朝
            hour in 12..14 -> 0.2f           // 昼食後
            else -> 0.1f                      // 通常
        }
    }
    
    /**
     * 寝床に帰るべきかどうか
     */
    fun shouldGoToNest(): Boolean {
        val hour = getCurrentHour()
        return hour >= 22 || hour < 6
    }
    
    /**
     * 起きているべきかどうか
     */
    fun shouldBeAwake(): Boolean {
        val hour = getCurrentHour()
        return hour in 7..21
    }
    
    /**
     * 背景の暗さ係数（夜は暗くなる）
     * @return 0.0（暗い）〜 1.0（明るい）
     */
    fun getAmbientBrightness(): Float {
        val hour = getCurrentHour()
        return when {
            hour in 10..16 -> 1.0f       // 昼間は明るい
            hour in 7..9 -> 0.8f         // 朝
            hour in 17..18 -> 0.7f       // 夕方
            hour in 19..20 -> 0.5f       // 夜の始まり
            hour in 21..22 -> 0.3f       // 夜
            else -> 0.2f                  // 深夜
        }
    }
    
    /**
     * 空の色を取得（グラデーション用）
     */
    fun getSkyColors(): Pair<Int, Int> {
        return when (getCurrentTimeOfDay()) {
            TimeOfDay.MORNING -> 0xFF87CEEB.toInt() to 0xFFFFE4B5.toInt()   // 水色→薄オレンジ
            TimeOfDay.AFTERNOON -> 0xFF4A90D9.toInt() to 0xFF87CEEB.toInt() // 青→水色
            TimeOfDay.EVENING -> 0xFFFF6B6B.toInt() to 0xFF4A4A6A.toInt()   // オレンジ→紫
            TimeOfDay.NIGHT -> 0xFF1A1A2E.toInt() to 0xFF16213E.toInt()     // 暗い青
        }
    }
}
