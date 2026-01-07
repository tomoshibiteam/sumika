package com.sumika.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumika.core.data.PetStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PetShopState(
    val ownedPets: Set<String> = emptySet(),
    val activePetId: String? = null,
    val isProSubscriber: Boolean = false
)

@HiltViewModel
class PetShopViewModel @Inject constructor(
    private val repository: PetStateRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(PetShopState())
    val state: StateFlow<PetShopState> = _state.asStateFlow()
    
    init {
        viewModelScope.launch {
            combine(
                repository.ownedPetIdsFlow,
                repository.activePetIdFlow,
                repository.isProSubscriberFlow
            ) { ownedPets, activePetId, isPro ->
                PetShopState(
                    ownedPets = ownedPets,
                    activePetId = activePetId,
                    isProSubscriber = isPro
                )
            }.collect {
                _state.value = it
            }
        }
    }
    
    fun selectPet(petId: String) {
        viewModelScope.launch {
            repository.setActivePet(petId)
        }
    }
    
    /**
     * ペットをお迎えする（新規採用）
     */
    fun adoptPet(petId: String, petName: String) {
        viewModelScope.launch {
            // ペットを所有リストに追加し、アクティブにする
            repository.initializePet(petId, petName)
        }
    }
    
    fun subscribeToPro() {
        viewModelScope.launch {
            repository.setProSubscriber(true)
        }
    }
}
