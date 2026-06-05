package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductModelDao {
    @Query("SELECT * FROM product_models ORDER BY name ASC")
    fun getAllProductModels(): Flow<List<ProductModelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductModel(model: ProductModelEntity): Long

    @Query("DELETE FROM product_models WHERE id = :id")
    suspend fun deleteProductModelById(id: Long)

    @Query("DELETE FROM product_models")
    suspend fun deleteAll()
}
