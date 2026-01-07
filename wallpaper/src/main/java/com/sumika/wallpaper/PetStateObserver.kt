package com.sumika.wallpaper

import android.content.Context
import android.util.Log
import com.sumika.core.data.WallpaperStateSync
import com.sumika.core.model.GrowthStage
import com.sumika.core.model.PetType
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 壁紙エンジン内でペット状態を監視するオブザーバー
 */
class PetStateObserver(context: Context) {
    
    companion object {
        private const val TAG = "PetStateObserver"
    }
    
    private val sync: WallpaperStateSync? = try {
        WallpaperStateSync(context.applicationContext)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to create WallpaperStateSync", e)
        null
    }
    
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Coroutine exception", throwable)
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)
    
    // 現在の状態をキャッシュ
    @Volatile var currentPetType: PetType = PetType.DOG
        private set
    
    @Volatile var currentVariation: Int = 0
        private set
    
    @Volatile var currentPetName: String = "ポチ"
        private set
    
    @Volatile var currentGrowthStage: GrowthStage = GrowthStage.BABY
        private set
    
    @Volatile var currentGrowthXp: Int = 0
        private set
    
    @Volatile var isFocusing: Boolean = false
        private set
    
    @Volatile var lastGrowthChange: Long = 0L
        private set
    
    @Volatile var homeX: Float? = null
        private set
    
    @Volatile var homeY: Float? = null
        private set
    
    // 状態変更コールバック
    var onPetTypeChanged: ((PetType, Int) -> Unit)? = null
    var onGrowthStageChanged: ((GrowthStage) -> Unit)? = null
    var onFocusingChanged: ((Boolean) -> Unit)? = null
    var onHomeLocationChanged: ((Float, Float) -> Unit)? = null
    
    /**
     * 監視を開始
     */
    fun start() {
        val stateSync = sync ?: run {
            Log.w(TAG, "WallpaperStateSync is null, skipping start")
            return
        }
        
        Log.i(TAG, "Starting pet state observation")
        
        // アクティブペットID監視（PetCatalogから情報を取得）
        scope.launch {
            try {
                stateSync.activePetIdFlow
                    .catch { e -> Log.e(TAG, "activePetIdFlow error", e) }
                    .collectLatest { activePetId ->
                        Log.d(TAG, "Active pet ID changed: $activePetId")
                        
                        val petEntry = activePetId?.let { com.sumika.core.model.PetCatalog.findById(it) }
                        if (petEntry != null) {
                            val newType = petEntry.type
                            val newVariation = petEntry.variation
                            
                            if (currentPetType != newType || currentVariation != newVariation) {
                                currentPetType = newType
                                currentVariation = newVariation
                                currentPetName = petEntry.defaultName
                                onPetTypeChanged?.invoke(newType, newVariation)
                                Log.i(TAG, "Pet changed to: ${petEntry.defaultName} ($newType variation=$newVariation)")
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "activePetIdFlow collection failed", e)
            }
        }
        
        // ペット名監視（レガシーフィールド）
        scope.launch {
            try {
                stateSync.petNameFlow
                    .catch { e -> Log.e(TAG, "petNameFlow error", e) }
                    .collectLatest { name ->
                        currentPetName = name
                    }
            } catch (e: Exception) {
                Log.e(TAG, "petNameFlow collection failed", e)
            }
        }
        
        // 成長段階監視
        scope.launch {
            try {
                stateSync.growthStageFlow
                    .catch { e -> Log.e(TAG, "growthStageFlow error", e) }
                    .collectLatest { stage ->
                        if (currentGrowthStage != stage) {
                            currentGrowthStage = stage
                            onGrowthStageChanged?.invoke(stage)
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "growthStageFlow collection failed", e)
            }
        }
        
        // 成長XP監視
        scope.launch {
            try {
                stateSync.growthXpFlow
                    .catch { e -> Log.e(TAG, "growthXpFlow error", e) }
                    .collectLatest { xp ->
                        currentGrowthXp = xp
                    }
            } catch (e: Exception) {
                Log.e(TAG, "growthXpFlow collection failed", e)
            }
        }
        
        // 集中モード監視
        scope.launch {
            try {
                stateSync.isFocusingFlow
                    .catch { e -> Log.e(TAG, "isFocusingFlow error", e) }
                    .collectLatest { focusing ->
                        if (isFocusing != focusing) {
                            isFocusing = focusing
                            onFocusingChanged?.invoke(focusing)
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "isFocusingFlow collection failed", e)
            }
        }
        
        // 成長変更時刻監視
        scope.launch {
            try {
                stateSync.lastGrowthChangeFlow
                    .catch { e -> Log.e(TAG, "lastGrowthChangeFlow error", e) }
                    .collectLatest { time ->
                        lastGrowthChange = time
                    }
            } catch (e: Exception) {
                Log.e(TAG, "lastGrowthChangeFlow collection failed", e)
            }
        }
        
        // おうち位置監視 (X)
        scope.launch {
            try {
                stateSync.homeXFlow.collectLatest { x ->
                    homeX = x
                    val y = homeY
                    if (x != null && y != null) {
                        onHomeLocationChanged?.invoke(x, y)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "homeXFlow error", e)
            }
        }
        
        // おうち位置監視 (Y)
        scope.launch {
            try {
                stateSync.homeYFlow.collectLatest { y ->
                    homeY = y
                    val x = homeX
                    if (x != null && y != null) {
                        onHomeLocationChanged?.invoke(x, y)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "homeYFlow error", e)
            }
        }
        
        Log.i(TAG, "Pet state observation started")
    }
    
    /**
     * 監視を停止
     */
    fun stop() {
        Log.i(TAG, "Stopping pet state observation")
        try {
            scope.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping observer", e)
        }
    }
}
