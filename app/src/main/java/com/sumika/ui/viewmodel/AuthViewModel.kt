package com.sumika.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumika.core.data.AuthRepository
import com.sumika.core.data.AuthResult
import com.sumika.core.data.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 認証画面の状態
 */
data class AuthState(
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * 認証ViewModel
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()
    
    init {
        // 認証状態を監視
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _state.value = _state.value.copy(
                    isAuthenticated = user != null,
                    user = user
                )
            }
        }
    }
    
    /**
     * ログイン
     */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = authRepository.signIn(email, password)) {
                is AuthResult.Success -> {
                    _state.value = _state.value.copy(
                        isAuthenticated = true,
                        user = result.user,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                is AuthResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
    
    /**
     * 新規登録
     */
    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = authRepository.signUp(email, password, displayName)) {
                is AuthResult.Success -> {
                    _state.value = _state.value.copy(
                        isAuthenticated = true,
                        user = result.user,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                is AuthResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
    
    /**
     * ログアウト
     */
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _state.value = AuthState()
        }
    }
    
    /**
     * エラーメッセージをクリア
     */
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
