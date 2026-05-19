package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BodyPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyPhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BodyPhotoEntity): Long

    @Update
    suspend fun update(entity: BodyPhotoEntity)

    @Query("DELETE FROM body_photo WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM body_photo WHERE id = :id")
    suspend fun getById(id: Long): BodyPhotoEntity?

    @Query("SELECT * FROM body_photo WHERE userId = :userId ORDER BY dateEpochMillis DESC")
    suspend fun getByUserId(userId: String): List<BodyPhotoEntity>

    @Query("SELECT * FROM body_photo WHERE userId = :userId AND type = :type ORDER BY dateEpochMillis DESC")
    suspend fun getByType(userId: String, type: String): List<BodyPhotoEntity>

    @Query("SELECT * FROM body_photo WHERE userId = :userId AND dateEpochMillis BETWEEN :from AND :to ORDER BY dateEpochMillis DESC")
    suspend fun getByDateRange(userId: String, from: Long, to: Long): List<BodyPhotoEntity>

    @Query("SELECT * FROM body_photo WHERE userId = :userId ORDER BY dateEpochMillis DESC")
    fun observeByUserId(userId: String): Flow<List<BodyPhotoEntity>>

    @Query("SELECT * FROM body_photo WHERE userId = :userId AND type = :type ORDER BY dateEpochMillis DESC")
    fun observeByType(userId: String, type: String): Flow<List<BodyPhotoEntity>>
}
