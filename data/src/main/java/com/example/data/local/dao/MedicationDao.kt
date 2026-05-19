package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: MedicationEntity): Long

    @Update
    suspend fun update(entity: MedicationEntity)

    @Query("DELETE FROM medication WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM medication WHERE id = :id")
    suspend fun getById(id: Long): MedicationEntity?

    @Query("SELECT * FROM medication WHERE userId = :userId ORDER BY name ASC")
    suspend fun getByUserId(userId: String): List<MedicationEntity>

    @Query("SELECT * FROM medication WHERE userId = :userId ORDER BY name ASC")
    fun observeByUserId(userId: String): Flow<List<MedicationEntity>>
}
