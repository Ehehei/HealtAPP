package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.StepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StepEntity): Long

    @Update
    suspend fun update(entity: StepEntity)

    @Query("DELETE FROM step_record WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM step_record WHERE id = :id")
    suspend fun getById(id: Long): StepEntity?

    @Query("SELECT * FROM step_record WHERE userId = :userId ORDER BY dateEpochDay DESC")
    suspend fun getByUserId(userId: String): List<StepEntity>

    @Query("SELECT * FROM step_record WHERE userId = :userId AND dateEpochDay BETWEEN :from AND :to ORDER BY dateEpochDay ASC")
    suspend fun getByDateRange(userId: String, from: Long, to: Long): List<StepEntity>

    @Query("SELECT * FROM step_record WHERE userId = :userId ORDER BY dateEpochDay DESC")
    fun observeByUserId(userId: String): Flow<List<StepEntity>>
}
