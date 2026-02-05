package com.sandeep.stockstracker.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class StockRepository @Inject constructor(
    private val api: StockApi,
    private val dao: StockDao
) {

    // 1. Get all stocks from the Database to show on screen
    val portfolio: Flow<List<StockEntity>> = dao.getAllStocks()

    // 2. Search for a stock (Network Call)
    suspend fun searchStocks(query: String): List<SearchResultDto> {
        val response = api.search(query)

        // Check if API said "Limit Reached"
        if (response.note != null || response.information != null) {
            throw Exception("Daily API Limit Reached (25/day).")
        }

        return response.bestMatches ?: emptyList()
    }

    // 3. Add a stock to portfolio (Network -> Database)
    suspend fun addStock(symbol: String, companyName: String) {
        val response = api.getQuote(symbol)

        // SAFETY CHECK: Throw a clear error if data is missing
        val quote = response.globalQuote ?: throw Exception("API Limit Reached or Invalid Stock")

        val entity = StockEntity(
            symbol = quote.symbol,
            companyName = companyName,
            price = quote.price.toDoubleOrNull() ?: 0.0,
            changePercent = quote.changePercent,
            previousClose = quote.previousClose.toDoubleOrNull() ?: 0.0,
            lastFetchedTimestamp = System.currentTimeMillis()
        )
        dao.insertStock(entity)
    }

    // 4. Refresh a SINGLE stock (Used by ViewModel loop)
    suspend fun refreshStock(stock: StockEntity) {
        val response = api.getQuote(stock.symbol)

        // Check for limit error here too
        if (response.globalQuote == null) {
            throw Exception("Daily API Limit Reached.")
        }

        val quote = response.globalQuote
        val updatedStock = stock.copy(
            price = quote.price.toDoubleOrNull() ?: 0.0,
            changePercent = quote.changePercent,
            previousClose = quote.previousClose.toDoubleOrNull() ?: 0.0,
            lastFetchedTimestamp = System.currentTimeMillis()
        )
        dao.insertStock(updatedStock)
    }

    // --- USAGE TRACKING (For the Counter) ---

    suspend fun incrementApiCallCount() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentStats = dao.getUsageStats()

        val newCount = if (currentStats != null && currentStats.date == today) {
            currentStats.apiCalls + 1
        } else {
            1 // New day, start at 1
        }

        dao.saveUsageStats(UsageEntity(date = today, apiCalls = newCount))
    }

    suspend fun getApiCallCount(): Int {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val stats = dao.getUsageStats()

        return if (stats != null && stats.date == today) {
            stats.apiCalls
        } else {
            0
        }
    }
}