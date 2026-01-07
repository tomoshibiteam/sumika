package com.sumika.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sumika.core.data.PetStateRepository
import com.sumika.service.FocusTimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FocusTimerState(
    val isRunning: Boolean = false,
    val remainingMs: Long = 25 * 60 * 1000L,
    val totalMs: Long = 25 * 60 * 1000L,
    val sessionsToday: Int = 0,
    val totalFocusMinutes: Int = 0
)

@HiltViewModel
class FocusTimerViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    
    private val repository = PetStateRepository(application)
    
    private val _state = MutableStateFlow(FocusTimerState())
    val state: StateFlow<FocusTimerState> = _state.asStateFlow()
    
    private var timerService: FocusTimerService? = null
    private var isBound = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val service = (binder as FocusTimerService.LocalBinder).getService()
            timerService = service
            isBound = true
            
            // コールバック設定
            service.onTick = { remainingMs ->
                _state.value = _state.value.copy(
                    remainingMs = remainingMs,
                    isRunning = true
                )
            }
            
            service.onComplete = {
                viewModelScope.launch {
                    onTimerCompleted()
                }
            }
            
            // 現在の状態を同期
            _state.value = _state.value.copy(
                isRunning = service.isTimerRunning(),
                remainingMs = service.getRemainingMs()
            )
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            isBound = false
        }
    }
    
    init {
        // 統計を読み込み
        viewModelScope.launch {
            repository.focusSessionsCountFlow.collect { count ->
                _state.value = _state.value.copy(sessionsToday = count)
            }
        }
        viewModelScope.launch {
            repository.totalFocusMinutesFlow.collect { minutes ->
                _state.value = _state.value.copy(totalFocusMinutes = minutes)
            }
        }
    }
    
    fun bindService() {
        val context = getApplication<Application>()
        val intent = Intent(context, FocusTimerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    fun unbindService() {
        if (isBound) {
            getApplication<Application>().unbindService(serviceConnection)
            isBound = false
        }
    }
    
    fun startTimer(durationMinutes: Int = 25) {
        val context = getApplication<Application>()
        val durationMs = durationMinutes * 60 * 1000L
        
        _state.value = _state.value.copy(
            totalMs = durationMs,
            remainingMs = durationMs,
            isRunning = true
        )
        
        // 集中モードを壁紙に通知
        viewModelScope.launch {
            repository.setFocusing(true)
        }
        
        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_START
            putExtra(FocusTimerService.EXTRA_DURATION_MS, durationMs)
        }
        context.startService(intent)
    }
    
    fun stopTimer() {
        val context = getApplication<Application>()
        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_STOP
        }
        context.startService(intent)
        
        // 集中モードを解除
        viewModelScope.launch {
            repository.setFocusing(false)
        }
        
        _state.value = _state.value.copy(
            isRunning = false,
            remainingMs = _state.value.totalMs
        )
    }
    
    private suspend fun onTimerCompleted() {
        val durationMinutes = (_state.value.totalMs / 1000 / 60).toInt()
        repository.onFocusSessionCompleted(durationMinutes)
        repository.setFocusing(false)
        
        _state.value = _state.value.copy(
            isRunning = false,
            remainingMs = _state.value.totalMs
        )
    }
    
    override fun onCleared() {
        unbindService()
        super.onCleared()
    }
}
