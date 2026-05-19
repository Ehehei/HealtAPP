package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.MedicationIntakeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationIntakeDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: MedicationIntakeEntity): Long

    @Query("DELETE FROM medication_intake WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        "SELECT * FROM medication_intake WHERE userId = :userId " +
            "ORDER BY takenAtMillis DESC LIMIT :limit"
    )
    fun observeByUserId(userId: String, limit: Int): Flow<List<MedicationIntakeEntity>>
}
