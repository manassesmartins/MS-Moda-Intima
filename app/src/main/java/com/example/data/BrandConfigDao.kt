package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BrandConfigDao {
    @Query("SELECT * FROM brand_config WHERE id = 'config_singleton' LIMIT 1")
    fun getBrandConfigFlow(): Flow<BrandConfigEntity?>

    @Query("SELECT * FROM brand_config WHERE id = 'config_singleton' LIMIT 1")
    suspend fun getBrandConfig(): BrandConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrandConfig(config: BrandConfigEntity)

    @Query("DELETE FROM brand_config")
    suspend fun deleteBrandConfig()
}
