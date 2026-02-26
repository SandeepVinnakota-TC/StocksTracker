package com.sandeep.stockstracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPortfolio(portfolio: PortfolioEntity)

    @Query("SELECT * FROM portfolio")
    fun getAllPortfolios(): Flow<List<PortfolioEntity>>

    // Links a stock to a portfolio
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStockIntoPortfolio(crossRef: PortfolioStocksCrossRef)

    // Joins the tables together to get stocks ONLY for a specific portfolio
    @Query("""
        SELECT * FROM stock_table 
        INNER JOIN portfolio_stock_cross_ref 
        ON stock_table.symbol = portfolio_stock_cross_ref.stockSymbol 
        WHERE portfolio_stock_cross_ref.portfolioId = :portfolioId
    """)
    fun getStocksForPortfolio(portfolioId: Int): Flow<List<StockEntity>>

    // Used by ViewModel (Keeps UI updated)
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

    @Query("UPDATE stock_table SET price = :price, changePercent = :change, previousClose = :prevClose, lastFetchedTimestamp = :time WHERE symbol = :symbol")
    suspend fun updatePrice(symbol: String, price: Double, change: String, prevClose: Double, time: Long)

    @Query("DELETE FROM portfolio_stock_cross_ref WHERE portfolioId = :portfolioId AND stockSymbol = :symbol")
    suspend fun removeStockFromPortfolio(portfolioId: Int, symbol: String)

}