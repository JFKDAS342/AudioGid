package com.example.audiogid.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExhibitDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(exhibit: Exhibit): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exhibits: List<Exhibit>): List<Long>

    @Query("SELECT * FROM exhibit ORDER BY id ASC")
    suspend fun getAll(): List<Exhibit>

    @Query("SELECT * FROM exhibit ORDER BY id ASC")
    fun observeAll(): Flow<List<Exhibit>>

    @Query("SELECT * FROM exhibit WHERE id = :id")
    suspend fun getById(id: Long): Exhibit?

    @Query("UPDATE exhibit SET local_path = :localPath WHERE id = :id")
    suspend fun updateLocalPath(id: Long, localPath: String?)

    @Query("SELECT COUNT(*) FROM exhibit")
    suspend fun count(): Int
}
