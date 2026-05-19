package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ScreeningRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreeningRecordDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ScreeningRecordEntity): Long

    @Query("DELETE FROM screening_record WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM screening_record WHERE userId = :userId ORDER BY dateEpochDay DESC")
    suspend fun getByUserId(userId: String): List<ScreeningRecordEntity>

    @Query("SELECT * FROM screening_record WHERE userId = :userId ORDER BY dateEpochDay DESC")
    fun observeByUserId(userId: String): Flow<List<ScreeningRecordEntity>>
}
