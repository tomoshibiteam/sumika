package com.sumika.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sumika.core.model.GrowthStage
import com.sumika.core.model.PetType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.petDataStore: DataStore<Preferences> by preferencesDataStore(name = "pet_state")

/**
 * ペット状態の永続化
 */
class PetStateRepository(private val context: Context) {
    
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
            prefs[Keys.GROWTH_STAGE] = newStage.name
            
            prefs[Keys.LAST_INTERACTION_AT] = System.currentTimeMillis()
        }
    }
    
    /**
     * ペット初期化
     */
    suspend fun initializePet(type: PetType, variation: Int, name: String) {
        context.petDataStore.edit { prefs ->
            prefs[Keys.PET_TYPE] = type.name
            prefs[Keys.PET_VARIATION] = variation
            prefs[Keys.PET_NAME] = name
            prefs[Keys.GROWTH_STAGE] = GrowthStage.BABY.name
            prefs[Keys.GROWTH_XP] = 0
            prefs[Keys.TOTAL_FOCUS_MINUTES] = 0
            prefs[Keys.FOCUS_SESSIONS_COUNT] = 0
            prefs[Keys.TOTAL_PETS] = 0
            prefs[Keys.TOTAL_FEEDS] = 0
            prefs[Keys.CREATED_AT] = System.currentTimeMillis()
            prefs[Keys.LAST_INTERACTION_AT] = System.currentTimeMillis()
        }
    }
}
