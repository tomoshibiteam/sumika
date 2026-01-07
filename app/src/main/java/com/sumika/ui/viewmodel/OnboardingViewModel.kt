package com.sumika.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumika.core.data.PetStateRepository
import com.sumika.core.model.PetCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val selectedPetId: String? = null,
    val petName: String = ""
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: PetStateRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()
    
    fun selectPet(petId: String) {
        _state.value = _state.value.copy(
            selectedPetId = petId,
            // デフォルト名を設定
            petName = PetCatalog.findById(petId)?.defaultName ?: ""
        )
    }
    
    fun setPetName(name: String) {
        _state.value  = _state.value.copy(petName = name)
    }
    
    suspend fun completeOnboarding() {
        val petId = _state.value.selectedPetId ?: return
        val petName = _state.value.petName.ifBlank {
            PetCatalog.findById(petId)?.defaultName ?: "ペット"
        }
        
        repository.initializePet(petId, petName)
    }
}
