package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BloodPressureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodPressureDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BloodPressureEntity): Long

    @Update
    suspend fun update(entity: BloodPressureEntity)

    @Query("DELETE FROM blood_pressure WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM blood_pressure WHERE id = :id")
    suspend fun getById(id: Long): BloodPressureEntity?

    @Query("SELECT * FROM blood_pressure WHERE userId = :userId ORDER BY dateEpochMillis DESC")
    suspend fun getByUserId(userId: String): List<BloodPressureEntity>

    @Query("SELECT * FROM blood_pressure WHERE userId = :userId AND dateEpochMillis BETWEEN :from AND :to ORDER BY dateEpochMillis DESC")
    suspend fun getByDateRange(userId: String, from: Long, to: Long): List<BloodPressureEntity>

    @Query("SELECT * FROM blood_pressure WHERE userId = :userId ORDER BY dateEpochMillis DESC")
    fun observeByUserId(userId: String): Flow<List<BloodPressureEntity>>
}
