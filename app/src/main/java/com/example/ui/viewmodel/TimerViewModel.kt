package com.example.ui.viewmodel

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.UnecApp
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PomodoroMode(val minutes: Int) {
    FOCUS(25),
    SHORT_BREAK(5),
    LONG_BREAK(15)
}

data class TimerUiState(
    val mode: PomodoroMode = PomodoroMode.FOCUS,
    val remainingMillis: Long = PomodoroMode.FOCUS.minutes * 60_000L,
    val totalMillis: Long = PomodoroMode.FOCUS.minutes * 60_000L,
    val isRunning: Boolean = false
) {
    val progress: Float
        get() = if (totalMillis <= 0L) 0f
        else (1f - remainingMillis.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)
}

class TimerViewModel(
    application: Application,
    private val studyRepo: StudyRepository
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(TimerUiState())
    val state: StateFlow<TimerUiState> = _state.asStateFlow()

    private var endAtMillis: Long = 0L
    private var tickJob: Job? = null

    fun setMode(mode: PomodoroMode) {
        tickJob?.cancel()
        val total = mode.minutes * 60_000L
        _state.value = TimerUiState(
            mode = mode,
            remainingMillis = total,
            totalMillis = total,
            isRunning = false
        )
    }

    fun toggleStartPause() {
        val st = _state.value
        if (st.isRunning) {
            tickJob?.cancel()
            val left = (endAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
            _state.value = st.copy(isRunning = false, remainingMillis = left)
        } else {
            val base = if (st.remainingMillis <= 0L) st.totalMillis else st.remainingMillis
            endAtMillis = System.currentTimeMillis() + base
            _state.value = st.copy(isRunning = true, remainingMillis = base)
            startTicking()
        }
    }

    fun reset() {
        tickJob?.cancel()
        val total = _state.value.mode.minutes * 60_000L
        _state.value = _state.value.copy(
            remainingMillis = total,
            totalMillis = total,
            isRunning = false
        )
    }

    fun skipToNext() {
        val current = _state.value.mode
        val next = when (current) {
            PomodoroMode.FOCUS -> PomodoroMode.SHORT_BREAK
            PomodoroMode.SHORT_BREAK -> PomodoroMode.FOCUS
            PomodoroMode.LONG_BREAK -> PomodoroMode.FOCUS
        }
        setMode(next)
    }

    private fun startTicking() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (_state.value.isRunning) {
                val left = (endAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
                _state.value = _state.value.copy(remainingMillis = left)
                if (left <= 0L) {
                    onIntervalCompleted()
                    break
                }
                delay(250L)
            }
        }
    }

    private suspend fun onIntervalCompleted() {
        val completedMode = _state.value.mode
        _state.value = _state.value.copy(isRunning = false, remainingMillis = 0L)
        if (completedMode == PomodoroMode.FOCUS) {
            studyRepo.recordPomodoroFocusCompleted(completedMode.minutes)
        }
        postCompletionNotification(completedMode)
        val nextMode = if (completedMode == PomodoroMode.FOCUS) PomodoroMode.SHORT_BREAK else PomodoroMode.FOCUS
        setMode(nextMode)
    }

    private fun postCompletionNotification(completedMode: PomodoroMode) {
        val ctx = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        val title = ctx.getString(R.string.app_name)
        val body = if (completedMode == PomodoroMode.FOCUS) {
            "25 min Focus complete! Time for a break."
        } else {
            "Break finished! Ready for the next focus session."
        }
        val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
        val notif = NotificationCompat.Builder(ctx, UnecApp.TIMER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(1001, notif)
    }

    override fun onCleared() {
        tickJob?.cancel()
        super.onCleared()
    }
}
