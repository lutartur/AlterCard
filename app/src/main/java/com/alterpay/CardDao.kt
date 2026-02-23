package com.alterpay

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY name ASC")
    fun getAllCards(): Flow<List<Card>>

    @Query("SELECT * FROM cards WHERE id = :id")
    fun getCard(id: Int): Flow<Card?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(card: Card)

    @Delete
    suspend fun delete(card: Card)
}
