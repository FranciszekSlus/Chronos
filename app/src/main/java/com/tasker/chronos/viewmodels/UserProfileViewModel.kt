// Plik: viewmodels/UserProfileViewModel.kt
package com.tasker.chronos.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tasker.chronos.data.repository.UserProfileRepository
import com.tasker.chronos.data.models.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserProfileRepository(application)

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile = _userProfile.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getUserProfile().collect { profile ->
                _userProfile.value = profile
            }
        }
    }

    fun resetPoints() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetPoints()
        }
    }
}