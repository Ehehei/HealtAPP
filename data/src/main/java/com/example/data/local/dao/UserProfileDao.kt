package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: UserProfileEntity)

    @Update
    suspend fun update(entity: UserProfileEntity)

    @Query("DELETE FROM user_profile WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM user_profile WHERE id = :id")
    suspend fun getById(id: String): UserProfileEntity?

    @Query("SELECT * FROM user_profile WHERE id = :id")
    fun observeById(id: String): Flow<UserProfileEntity?>
}
