package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val db: AppDatabase) {
    val playerProfileFlow: Flow<PlayerProfile?> = db.playerProfileDao().getProfileFlow()
    val matchHistoryFlow: Flow<List<MatchHistory>> = db.matchHistoryDao().getAllMatchesFlow()

    suspend fun getProfile(): PlayerProfile {
        return db.playerProfileDao().getProfile() ?: PlayerProfile()
    }

    suspend fun saveProfile(profile: PlayerProfile) {
        db.playerProfileDao().saveProfile(profile)
    }

    suspend fun recordMatch(match: MatchHistory) {
        db.matchHistoryDao().insertMatch(match)
        // If they won (rank == 1), increment profile wins and add points!
        val currentProfile = getProfile()
        val bonusWins = if (match.rank == 1) 1 else 0
        val updatedProfile = currentProfile.copy(
            wins = currentProfile.wins + bonusWins,
            points = currentProfile.points + match.scoreEarned
        )
        db.playerProfileDao().saveProfile(updatedProfile)
    }
}
