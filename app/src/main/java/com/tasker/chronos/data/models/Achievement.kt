// Plik: data/models/Achievement.kt
package com.tasker.chronos.data.models

enum class AchievementReward {
    NONE,
    CONFETTI_ANIMATION,   // Animacja confetti przy zaznaczaniu
    GOLDEN_THEME,         // Złoty akcent kolorów
    FIRE_STREAK,          // Animacja ognia przy streak
    STAR_BADGE,           // Gwiazdka przy nazwie użytkownika
    RAINBOW_EVENTS,       // Tęczowe kolory eventów
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,           // Emoji ikona
    val requiredPoints: Int,
    val reward: AchievementReward = AchievementReward.NONE,
    val rewardDescription: String = ""
)

object Achievements {
    val all = listOf(
        Achievement(
            id = "first_steps",
            title = "Pierwsze kroki",
            description = "Zdobądź 10 punktów",
            icon = "🌱",
            requiredPoints = 10,
            reward = AchievementReward.NONE,
            rewardDescription = "Odblokowano odznakę początkującego"
        ),
        Achievement(
            id = "habit_starter",
            title = "Nawykowy początek",
            description = "Zdobądź 50 punktów",
            icon = "⚡",
            requiredPoints = 50,
            reward = AchievementReward.CONFETTI_ANIMATION,
            rewardDescription = "Animacja confetti przy zaznaczaniu nawyków"
        ),
        Achievement(
            id = "dedicated",
            title = "Zaangażowany",
            description = "Zdobądź 100 punktów",
            icon = "🔥",
            requiredPoints = 100,
            reward = AchievementReward.FIRE_STREAK,
            rewardDescription = "Animacja ognia przy streak nawyków"
        ),
        Achievement(
            id = "achiever",
            title = "Osiągający",
            description = "Zdobądź 250 punktów",
            icon = "⭐",
            requiredPoints = 250,
            reward = AchievementReward.STAR_BADGE,
            rewardDescription = "Gwiazdka przy Twoim profilu"
        ),
        Achievement(
            id = "master",
            title = "Mistrz nawyków",
            description = "Zdobądź 500 punktów",
            icon = "🏆",
            requiredPoints = 500,
            reward = AchievementReward.GOLDEN_THEME,
            rewardDescription = "Złoty akcent w całej aplikacji"
        ),
        Achievement(
            id = "legend",
            title = "Legenda",
            description = "Zdobądź 1000 punktów",
            icon = "👑",
            requiredPoints = 1000,
            reward = AchievementReward.RAINBOW_EVENTS,
            rewardDescription = "Tęczowe kolory eventów w kalendarzu"
        )
    )

    fun getUnlocked(totalPoints: Int): List<Achievement> =
        all.filter { it.requiredPoints <= totalPoints }

    fun getNextAchievement(totalPoints: Int): Achievement? =
        all.firstOrNull { it.requiredPoints > totalPoints }
}