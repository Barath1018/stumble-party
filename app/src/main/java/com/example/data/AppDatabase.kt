package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey val id: Int = 1,
    val username: String = "WobblyBean",
    val selectedColorIndex: Int = 0,
    val selectedHat: String = "None",
    val selectedOutfit: String = "Ninja",
    val wins: Int = 0,
    val points: Int = 0
)

@Entity(tableName = "match_history")
data class MatchHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val gameSequence: String, // e.g., "Obstacle, Hexagon, Tag"
    val rank: Int,           // e.g., 1 for winner, or round eliminated
    val scoreEarned: Int
)

@Dao
interface PlayerProfileDao {
    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<PlayerProfile?>

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): PlayerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: PlayerProfile)
}

@Dao
interface MatchHistoryDao {
    @Query("SELECT * FROM match_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllMatchesFlow(): Flow<List<MatchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchHistory)

    @Query("SELECT SUM(wins) FROM player_profile")
    fun getTotalWins(): Flow<Int?>
}

@Database(entities = [PlayerProfile::class, MatchHistory::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerProfileDao(): PlayerProfileDao
    abstract fun matchHistoryDao(): MatchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stumble_party_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
