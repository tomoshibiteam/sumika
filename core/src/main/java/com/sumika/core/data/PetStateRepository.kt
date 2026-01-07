package com.sumika.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sumika.core.model.GrowthStage
import com.sumika.core.model.PetType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.petDataStore: DataStore<Preferences> by preferencesDataStore(name = "pet_state")

/**
 * ペット状態の永続化
 */
@Singleton
class PetStateRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private object Keys {
        val PET_TYPE = stringPreferencesKey("pet_type")
        val PET_VARIATION = intPreferencesKey("pet_variation")
        val PET_NAME = stringPreferencesKey("pet_name")
        val GROWTH_STAGE = stringPreferencesKey("growth_stage")
        val GROWTH_XP = intPreferencesKey("growth_xp")
        val TOTAL_FOCUS_MINUTES = intPreferencesKey("total_focus_minutes")
        val FOCUS_SESSIONS_COUNT = intPreferencesKey("focus_sessions_count")
        val TOTAL_PETS = intPreferencesKey("total_pets")
        val TOTAL_FEEDS = intPreferencesKey("total_feeds")
        val CREATED_AT = longPreferencesKey("created_at")
        val LAST_INTERACTION_AT = longPreferencesKey("last_interaction_at")
        val IS_FOCUSING = booleanPreferencesKey("is_focusing")
        val LAST_GROWTH_CHANGE = longPreferencesKey("last_growth_change")
        val HOME_X = floatPreferencesKey("home_x")
        val HOME_Y = floatPreferencesKey("home_y")
        
        // 新しいキー: ペット所有管理
        val OWNED_PET_IDS = stringSetPreferencesKey("owned_pet_ids")
        val ACTIVE_PET_ID = stringPreferencesKey("active_pet_id")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val IS_PRO_SUBSCRIBER = booleanPreferencesKey("is_pro_subscriber")
    }
    
    /**
     * ペットタイプを取得
     */
    val petTypeFlow: Flow<PetType> = context.petDataStore.data.map { prefs ->
        val typeName = prefs[Keys.PET_TYPE] ?: PetType.CAT.name
        PetType.valueOf(typeName)
    }
    
    /**
     * ペットバリエーションを取得
     */
    val petVariationFlow: Flow<Int> = context.petDataStore.data.map { prefs ->
        prefs[Keys.PET_VARIATION] ?: 0
    }
    
    /**
     * ペット名を取得
     */
    val petNameFlow: Flow<String> = context.petDataStore.data.map { prefs ->
        prefs[Keys.PET_NAME] ?: "ミケ"
    }
    
    /**
     * 成長段階を取得
     */
    val growthStageFlow: Flow<GrowthStage> = context.petDataStore.data.map { prefs ->
        val stageName = prefs[Keys.GROWTH_STAGE] ?: GrowthStage.BABY.name
        GrowthStage.valueOf(stageName)
    }
    
    /**
     * 成長XPを取得
     */
    val growthXpFlow: Flow<Int> = context.petDataStore.data.map { prefs ->
        prefs[Keys.GROWTH_XP] ?: 0
    }
    
    /**
     * 総集中時間（分）を取得
     */
    val totalFocusMinutesFlow: Flow<Int> = context.petDataStore.data.map { prefs ->
        prefs[Keys.TOTAL_FOCUS_MINUTES] ?: 0
    }
    
    /**
     * 集中セッション数を取得
     */
    val focusSessionsCountFlow: Flow<Int> = context.petDataStore.data.map { prefs ->
        prefs[Keys.FOCUS_SESSIONS_COUNT] ?: 0
    }
    
    /**
     * 集中中かどうか
     */
    val isFocusingFlow: Flow<Boolean> = context.petDataStore.data.map { prefs ->
        prefs[Keys.IS_FOCUSING] ?: false
    }
    
    /**
     * 最後の成長段階変更時刻
     */
    val lastGrowthChangeFlow: Flow<Long> = context.petDataStore.data.map { prefs ->
        prefs[Keys.LAST_GROWTH_CHANGE] ?: 0L
    }
    
    /**
     * おうちのX座標 (0.0 - 1.0)
     */
    val homeXFlow: Flow<Float?> = context.petDataStore.data.map { prefs ->
        prefs[Keys.HOME_X]
    }
    
    /**
     * おうちのY座標 (0.0 - 1.0)
     */
    val homeYFlow: Flow<Float?> = context.petDataStore.data.map { prefs ->
        prefs[Keys.HOME_Y]
    }
    
    /**
     * 所有ペットIDリスト
     */
    val ownedPetIdsFlow: Flow<Set<String>> = context.petDataStore.data.map { prefs ->
        prefs[Keys.OWNED_PET_IDS] ?: emptySet()
    }
    
    /**
     * アクティブペットID
     */
    val activePetIdFlow: Flow<String?> = context.petDataStore.data.map { prefs ->
        prefs[Keys.ACTIVE_PET_ID]
    }
    
    /**
     * オンボーディング完了フラグ
     */
    val onboardingCompletedFlow: Flow<Boolean> = context.petDataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETED] ?: false
    }
    
    /**
     * Proサブスク状態
     */
    val isProSubscriberFlow: Flow<Boolean> = context.petDataStore.data.map { prefs ->
        prefs[Keys.IS_PRO_SUBSCRIBER] ?: false
    }
    
    /**
     * ペットタイプを設定
     */
    suspend fun setPetType(type: PetType) {
        context.petDataStore.edit { prefs ->
            prefs[Keys.PET_TYPE] = type.name
        }
    }
    
    /**
     * ペットバリエーションを設定
     */
    suspend fun setPetVariation(variation: Int) {
        context.petDataStore.edit { prefs ->
            prefs[Keys.PET_VARIATION] = variation
        }
    }
    
    /**
     * ペット名を設定
     */
    suspend fun setPetName(name: String) {
        context.petDataStore.edit { prefs ->
            prefs[Keys.PET_NAME] = name
        }
    }
    
    /**
     * 集中モードを設定
     */
    suspend fun setFocusing(focusing: Boolean) {
        context.petDataStore.edit { prefs ->
            prefs[Keys.IS_FOCUSING] = focusing
        }
    }
    
    /**
     * おうちの位置を設定
     */
    suspend fun setHomePosition(x: Float, y: Float) {
        context.petDataStore.edit { prefs ->
            prefs[Keys.HOME_X] = x
            prefs[Keys.HOME_Y] = y
        }
    }
    
    /**
     * 集中セッション完了時に呼び出し
     */
    suspend fun onFocusSessionCompleted(durationMinutes: Int) {
        context.petDataStore.edit { prefs ->
            val currentMinutes = prefs[Keys.TOTAL_FOCUS_MINUTES] ?: 0
            val currentSessions = prefs[Keys.FOCUS_SESSIONS_COUNT] ?: 0
            val currentXp = prefs[Keys.GROWTH_XP] ?: 0
            
            prefs[Keys.TOTAL_FOCUS_MINUTES] = currentMinutes + durationMinutes
            prefs[Keys.FOCUS_SESSIONS_COUNT] = currentSessions + 1
            
            // 成長XP付与（25分で100XP）
            val xpGain = (durationMinutes * 4)
            prefs[Keys.GROWTH_XP] = currentXp + xpGain
            
            // 成長段階チェック
            val newXp = currentXp + xpGain
            val currentStage = prefs[Keys.GROWTH_STAGE]?.let { GrowthStage.valueOf(it) } ?: GrowthStage.BABY
            val newStage = when {
                newXp >= 2000 && currentStage != GrowthStage.ADULT -> GrowthStage.ADULT
                newXp >= 500 && currentStage == GrowthStage.BABY -> GrowthStage.TEEN
                else -> currentStage
            }
            if (newStage != currentStage) {
                prefs[Keys.LAST_GROWTH_CHANGE] = System.currentTimeMillis()
            }
            prefs[Keys.GROWTH_STAGE] = newStage.name
            
            prefs[Keys.LAST_INTERACTION_AT] = System.currentTimeMillis()
        }
    }
    
    /**
     * ペット初期化（オンボーディング時）
     */
    suspend fun initializePet(petId: String, name: String) {
        context.petDataStore.edit { prefs ->
            // アクティブペットIDを設定
            prefs[Keys.ACTIVE_PET_ID] = petId
            
            // 所有ペットに追加
            val currentOwned = prefs[Keys.OWNED_PET_IDS] ?: emptySet()
            prefs[Keys.OWNED_PET_IDS] = currentOwned + petId
            
            // レガシーフィールドも設定（互換性のため）
            prefs[Keys.PET_NAME] = name
            prefs[Keys.GROWTH_STAGE] = GrowthStage.BABY.name
            prefs[Keys.GROWTH_XP] = 0
            prefs[Keys.TOTAL_FOCUS_MINUTES] = 0
            prefs[Keys.FOCUS_SESSIONS_COUNT] = 0
            prefs[Keys.CREATED_AT] = System.currentTimeMillis()
            prefs[Keys.LAST_INTERACTION_AT] = System.currentTimeMillis()
            
            // オンボーディング完了
            prefs[Keys.ONBOARDING_COMPLETED] = true
        }
    }
    
    /**
     * ペットを所有リストに追加（購入時）
     */
    suspend fun addOwnedPet(petId: String) {
        context.petDataStore.edit { prefs ->
            val currentOwned = prefs[Keys.OWNED_PET_IDS] ?: emptySet()
            prefs[Keys.OWNED_PET_IDS] = currentOwned + petId
        }
    }
    
    /**
     * アクティブペットを変更
     */
    suspend fun setActivePet(petId: String) {
        context.petDataStore.edit { prefs ->
            prefs[Keys.ACTIVE_PET_ID] = petId
        }
    }
    
    /**
     * オンボーディング完了を記録
     */
    suspend fun setOnboardingCompleted() {
        context.petDataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = true
        }
    }
    
    /**
     * Proサブスクリプション状態を設定
     */
    suspend fun setProSubscriber(isPro: Boolean) {
        context.petDataStore.edit { prefs ->
            prefs[Keys.IS_PRO_SUBSCRIBER] = isPro
        }
    }
    
    /**
     * 指定ペットを所有しているか確認
     */
    suspend fun isOwned(petId: String): Boolean {
        val owned = context.petDataStore.data.map { prefs ->
            prefs[Keys.OWNED_PET_IDS] ?: emptySet()
        }
        return owned.map { it.contains(petId) }.first()
    }
}

private suspend fun <T> Flow<T>.first(): T {
    var result: T? = null
    collect { value ->
        result = value
        return@collect
    }
    @Suppress("UNCHECKED_CAST")
    return result as T
}
