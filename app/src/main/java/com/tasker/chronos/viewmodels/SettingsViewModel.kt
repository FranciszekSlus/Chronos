// Plik: viewmodels/SettingsViewModel.kt
package com.tasker.chronos.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tasker.chronos.data.models.AppSettings
import com.tasker.chronos.data.repository.AppSettingsRepository
import com.tasker.chronos.utils.DataBackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppSettingsRepository(application)

    private val _settings = MutableStateFlow(AppSettings())
    val settings = _settings.asStateFlow()

    private val _backupMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val backupMessage = _backupMessage.asSharedFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy = _isBusy.asStateFlow()

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

    fun exportBackup(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _isBusy.value = true
            val result = DataBackupManager.exportToUri(getApplication(), uri)
            _isBusy.value = false
            _backupMessage.emit(
                if (result.isSuccess) "Eksport zakończony pomyślnie"
                else "Eksport nieudany: ${result.exceptionOrNull()?.message ?: "błąd"}"
            )
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _isBusy.value = true
            val result = DataBackupManager.importFromUri(getApplication(), uri)
            _isBusy.value = false
            if (result.isSuccess) {
                _backupMessage.emit("Import OK — aplikacja uruchomi się ponownie")
                // DataStore trzyma cache w pamięci — restart ładuje świeże pliki
                kotlinx.coroutines.delay(900)
                exitProcess(0)
            } else {
                _backupMessage.emit(
                    "Import nieudany: ${result.exceptionOrNull()?.message ?: "błąd"}"
                )
            }
        }
    }
}
