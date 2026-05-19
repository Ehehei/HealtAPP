package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.WeightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WeightEntity): Long

    @Update
    suspend fun update(entity: WeightEntity)

    @Query("DELETE FROM weight_record WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM weight_record WHERE id = :id")
    suspend fun getById(id: Long): WeightEntity?

    @Query("SELECT * FROM weight_record WHERE userId = :userId ORDER BY dateEpochDay ASC")
    suspend fun getByUserId(userId: String): List<WeightEntity>

    @Query("SELECT * FROM weight_record WHERE userId = :userId AND dateEpochDay BETWEEN :from AND :to ORDER BY dateEpochDay ASC")
    suspend fun getByDateRange(userId: String, from: Long, to: Long): List<WeightEntity>

    @Query("SELECT * FROM weight_record WHERE userId = :userId ORDER BY dateEpochDay ASC")
    fun observeByUserId(userId: String): Flow<List<WeightEntity>>
}
