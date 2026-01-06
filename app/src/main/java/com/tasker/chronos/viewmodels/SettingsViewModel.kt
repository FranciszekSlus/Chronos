// Plik: viewmodels/SettingsViewModel.kt
package com.tasker.chronos.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tasker.chronos.data.repository.AppSettingsRepository
import com.tasker.chronos.data.models.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppSettingsRepository(application)

    private val _settings = MutableStateFlow(AppSettings())
    val settings = _settings.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getSettings().collect { settings ->
                _settings.value = settings
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setNotificationsEnabled(enabled)
        }
    }

    fun markTutorialAsShown() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setTutorialShown()
        }
    }
}