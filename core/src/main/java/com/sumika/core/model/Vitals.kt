package com.sumika.core.model

/**
 * 短期的な生理状態（揮発性/日次でリセット可能）
 * 
 * 全ての値は 0.0〜1.0 の範囲
 */
data class Vitals(
    /** 満腹度: 0=空腹, 1=満腹 */
    val satiety: Float = 0.7f,
    
    /** 眠気（睡眠圧）: 0=覚醒, 1=限界 */
    val sleepPressure: Float = 0.0f,
    
    /** 機嫌: 0=不満, 1=上機嫌 */
    val happiness: Float = 0.7f
) {
    init {
        require(satiety in 0f..1f) { "satiety must be 0.0-1.0" }
        require(sleepPressure in 0f..1f) { "sleepPressure must be 0.0-1.0" }
        require(happiness in 0f..1f) { "happiness must be 0.0-1.0" }
    }
    
    /** 空腹かどうか */
    val isHungry: Boolean get() = satiety < 0.3f
    
    /** 眠いかどうか */
    val isSleepy: Boolean get() = sleepPressure > 0.7f
    
    /** 機嫌が悪いかどうか */
    val isUnhappy: Boolean get() = happiness < 0.3f
}
