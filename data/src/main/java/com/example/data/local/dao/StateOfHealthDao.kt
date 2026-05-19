package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.StateOfHealthEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StateOfHealthDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StateOfHealthEntity): Long

    @Update
    suspend fun update(entity: StateOfHealthEntity)

    @Query("DELETE FROM state_of_health WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM state_of_health WHERE id = :id")
    suspend fun getById(id: Long): StateOfHealthEntity?

    @Query("SELECT * FROM state_of_health WHERE userId = :userId ORDER BY dateEpochDay DESC")
    suspend fun getByUserId(userId: String): List<StateOfHealthEntity>

    @Query("SELECT * FROM state_of_health WHERE userId = :userId AND dateEpochDay BETWEEN :from AND :to ORDER BY dateEpochDay DESC")
    suspend fun getByDateRange(userId: String, from: Long, to: Long): List<StateOfHealthEntity>

    @Query("SELECT * FROM state_of_health WHERE userId = :userId ORDER BY dateEpochDay DESC")
    fun observeByUserId(userId: String): Flow<List<StateOfHealthEntity>>
}
