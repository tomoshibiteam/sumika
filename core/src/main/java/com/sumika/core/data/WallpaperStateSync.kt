package com.sumika.core.data

import android.content.Context
import com.sumika.core.model.GrowthStage
import com.sumika.core.model.PetType
import kotlinx.coroutines.flow.Flow

/**
 * 壁紙とアプリ間でペット状態を共有するための同期クラス
 * PetStateRepository のラッパーとして機能
 */
class WallpaperStateSync(context: Context) {
    
    private val repository = PetStateRepository(context)
    
    /**
     * ペットタイプ
     */
    val petTypeFlow: Flow<PetType> = repository.petTypeFlow
    
    /**
     * ペットバリエーション
     */
    val petVariationFlow: Flow<Int> = repository.petVariationFlow
    
    /**
     * ペット名
     */
    val petNameFlow: Flow<String> = repository.petNameFlow
    
    /**
     * 成長段階
     */
    val growthStageFlow: Flow<GrowthStage> = repository.growthStageFlow
    
    /**
     * 成長XP
     */
    val growthXpFlow: Flow<Int> = repository.growthXpFlow
    
    /**
     * 集中中かどうか
     */
    val isFocusingFlow: Flow<Boolean> = repository.isFocusingFlow
    
    /**
     * 最後の成長段階変更時刻（レベルアップ検知用）
     */
    val lastGrowthChangeFlow: Flow<Long> = repository.lastGrowthChangeFlow
    
    /**
     * おうちのX座標
     */
    val homeXFlow: Flow<Float?> = repository.homeXFlow
    
    /**
     * おうちのY座標
     */
    val homeYFlow: Flow<Float?> = repository.homeYFlow
}
