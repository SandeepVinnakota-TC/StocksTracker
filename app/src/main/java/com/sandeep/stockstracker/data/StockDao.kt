package com.sandeep.stockstracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {

    // Existing Flow method (Keeps UI updated)
    @Query("SELECT * FROM stock_table")
    fun getAllStocks(): Flow<List<StockEntity>>

    // Used by Repository for refreshing
    @Query("SELECT * FROM stock_table")
    suspend fun getAllStocksList(): List<StockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: StockEntity)

    @Query("SELECT * FROM usage_stats WHERE id = 1")
    suspend fun getUsageStats(): UsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUsageStats(stats: UsageEntity)
}