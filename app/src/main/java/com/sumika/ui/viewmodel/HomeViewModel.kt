package com.sumika.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sumika.core.data.PetStateRepository
import com.sumika.core.model.GrowthStage
import com.sumika.core.model.PetCatalog
import com.sumika.core.model.PetType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

data class HomeState(
    val petType: PetType = PetType.CAT,
    val petVariation: Int = 0,
    val petName: String = "ミケ",
    val growthStage: GrowthStage = GrowthStage.BABY,
    val growthXp: Int = 0,
    val xpToNextStage: Int = 500,
    val totalFocusMinutes: Int = 0,
    val focusSessionsCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    
    private val repository = PetStateRepository(application)
    
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()
    
    init {
        // アクティブペットIDからペット情報を取得
        viewModelScope.launch {
            combine(
                repository.activePetIdFlow,
                repository.growthStageFlow,
                repository.growthXpFlow,
                repository.totalFocusMinutesFlow,
                repository.focusSessionsCountFlow
            ) { activePetId, stage, xp, focusMinutes, sessionCount ->
                // activePetIdからカタログ情報を取得
                Log.d("HomeViewModel", "activePetId: $activePetId")
                val petEntry = activePetId?.let { PetCatalog.findById(it) }
                Log.d("HomeViewModel", "petEntry: ${petEntry?.defaultName} (${petEntry?.type})")
                
                val xpToNext = when (stage) {
                    GrowthStage.BABY -> 500
                    GrowthStage.TEEN -> 2000
                    GrowthStage.ADULT -> Int.MAX_VALUE
                }
                
                HomeState(
                    petType = petEntry?.type ?: PetType.CAT,
                    petVariation = petEntry?.variation ?: 0,
                    petName = petEntry?.defaultName ?: "ペット",
                    growthStage = stage,
                    growthXp = xp,
                    xpToNextStage = xpToNext,
                    totalFocusMinutes = focusMinutes,
                    focusSessionsCount = sessionCount
                )
            }.collect { newState ->
                Log.d("HomeViewModel", "New state: ${newState.petName} (${newState.petType})") 
                _state.value = newState
            }
        }
    }
}
