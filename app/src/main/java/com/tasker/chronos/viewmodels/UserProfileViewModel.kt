// Plik: viewmodels/UserProfileViewModel.kt
package com.tasker.chronos.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tasker.chronos.data.models.Achievements
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

    // ✅ NOWE: Nowo odblokowane osiągnięcie (do pokazania animacji)
    private val _newlyUnlocked = MutableStateFlow<com.tasker.chronos.data.models.Achievement?>(null)
    val newlyUnlocked = _newlyUnlocked.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getUserProfile().collect { profile ->
                val oldPoints = _userProfile.value.totalPoints
                _userProfile.value = profile

                // ✅ Sprawdź nowe osiągnięcia po zmianie punktów
                if (profile.totalPoints > oldPoints) {
                    checkNewAchievements(profile)
                }
            }
        }
    }

    private suspend fun checkNewAchievements(profile: UserProfile) {
        val newlyUnlockedAchievement = Achievements.all.firstOrNull { achievement ->
            achievement.requiredPoints <= profile.totalPoints &&
                    !profile.unlockedAchievements.contains(achievement.id)
        }

        newlyUnlockedAchievement?.let { achievement ->
            repository.unlockAchievement(achievement.id)
            _newlyUnlocked.value = achievement
        }
    }

    fun clearNewlyUnlocked() {
        _newlyUnlocked.value = null
    }

    fun resetPoints() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetPoints()
        }
    }
}