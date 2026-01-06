package com.sumika.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sumika.core.data.PetStateRepository
import com.sumika.core.model.GrowthStage
import com.sumika.core.model.PetType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
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
        viewModelScope.launch {
            combine(
                repository.petTypeFlow,
                repository.petVariationFlow,
                repository.petNameFlow,
                repository.growthStageFlow,
                repository.growthXpFlow
            ) { type, variation, name, stage, xp ->
                val xpToNext = when (stage) {
                    GrowthStage.BABY -> 500
                    GrowthStage.TEEN -> 2000
                    GrowthStage.ADULT -> Int.MAX_VALUE
                }
                HomeState(
                    petType = type,
                    petVariation = variation,
                    petName = name,
                    growthStage = stage,
                    growthXp = xp,
                    xpToNextStage = xpToNext
                )
            }.collect { combined ->
                _state.value = combined
            }
        }
        
        viewModelScope.launch {
            repository.totalFocusMinutesFlow.collect { minutes ->
                _state.value = _state.value.copy(totalFocusMinutes = minutes)
            }
        }
        
        viewModelScope.launch {
            repository.focusSessionsCountFlow.collect { count ->
                _state.value = _state.value.copy(focusSessionsCount = count)
            }
        }
    }
    
    fun setPetType(type: PetType) {
        viewModelScope.launch {
            repository.setPetType(type)
        }
    }
    
    fun setPetVariation(variation: Int) {
        viewModelScope.launch {
            repository.setPetVariation(variation)
        }
    }
    
    fun setPetName(name: String) {
        viewModelScope.launch {
            repository.setPetName(name)
        }
    }
}
