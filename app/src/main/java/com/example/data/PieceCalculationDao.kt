package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PieceCalculationDao {
    @Query("SELECT * FROM piece_calculations ORDER BY id ASC")
    fun getAllCalculations(): Flow<List<PieceCalculationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(entity: PieceCalculationEntity): Long

    @Query("DELETE FROM piece_calculations WHERE id = :id")
    suspend fun deleteCalculationById(id: Long)

    @Query("DELETE FROM piece_calculations")
    suspend fun deleteAll()
}
