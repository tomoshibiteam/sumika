package com.sumika.core.model

import java.util.UUID

/**
 * ペットのメインデータモデル
 */
data class Pet(
    val id: String = UUID.randomUUID().toString(),
    val type: PetType,
    val variation: Int,  // 0, 1, 2
    val name: String,
    val growthStage: GrowthStage = GrowthStage.BABY,
    val growthXp: Int = 0,
    val personality: Personality = Personality(),
    val vitals: Vitals = Vitals(),
    val motionState: MotionState = MotionState.IDLE,
    val positionX: Float = 0.5f,  // 0.0-1.0 (画面比率)
    val positionY: Float = 0.8f,  // 0.0-1.0 (画面比率)
    val createdAt: Long = System.currentTimeMillis(),
    val lastInteractionAt: Long = System.currentTimeMillis()
) {
    companion object {
        /** 成長に必要なXP閾値 */
        const val XP_TO_TEEN = 100
        const val XP_TO_ADULT = 300
    }
    
    /** 次の成長段階への進捗率 (0.0-1.0) */
    val growthProgress: Float get() = when (growthStage) {
        GrowthStage.BABY -> growthXp.toFloat() / XP_TO_TEEN
        GrowthStage.TEEN -> (growthXp - XP_TO_TEEN).toFloat() / (XP_TO_ADULT - XP_TO_TEEN)
        GrowthStage.ADULT -> 1f
    }
    
    /** 成長可能かどうか */
    fun canEvolve(): Boolean = when (growthStage) {
        GrowthStage.BABY -> growthXp >= XP_TO_TEEN
        GrowthStage.TEEN -> growthXp >= XP_TO_ADULT
        GrowthStage.ADULT -> false
    }
}
