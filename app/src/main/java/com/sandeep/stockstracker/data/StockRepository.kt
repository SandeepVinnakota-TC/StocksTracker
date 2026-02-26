package com.sandeep.stockstracker.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class StockRepository @Inject constructor(
    private val api: StockApi,
    private val dao: StockDao
) {

    // 1. Get all portfolios
    val portfolios: Flow<List<PortfolioEntity>> = dao.getAllPortfolios()

    // 2. Get stocks for a SPECIFIC portfolio
    fun getStocksForPortfolio(portfolioId: Int): Flow<List<StockEntity>> {
        return dao.getStocksForPortfolio(portfolioId)
    }

    // 3. Create a new portfolio
    suspend fun createPortfolio(name: String) {
        dao.insertPortfolio(PortfolioEntity(name = name))
    }

    // 4. Search for a stock (Network Call)
    suspend fun searchStocks(query: String): List<SearchResultDto> {
        val response = api.search(query)

        // DEBUG LOG: Print what the API actually said
        if (response.note != null) Log.e("STOCK_DEBUG", "API Note: ${response.note}")
        if (response.information != null) Log.e("STOCK_DEBUG", "API Info: ${response.information}")

        if (response.note != null || response.information != null) {
            Log.e("STOCK_DEBUG", "API Info: ${response.information}")
            throw Exception("Daily API Limit Reached (25/day).")
        }

        if (response.note != null || response.information != null) {
            val msg = response.note ?: response.information ?: "Unknown API Limit"
            throw Exception(msg)
        }

        return response.bestMatches ?: emptyList()
    }

    // 5. Add a stock to portfolio
    suspend fun addStockToPortfolio(symbol: String, companyName: String, portfolioId: Int) {
        // 1. Save the stock into the main stock table
        val entity = StockEntity(
            symbol = symbol,
            companyName = companyName,
            price = 0.0, // Will be updated instantly by refreshBatch
            changePercent = "0%",
            previousClose = 0.0,
            lastFetchedTimestamp = System.currentTimeMillis()
        )
        dao.insertStock(entity)

        // 2. Link the stock to the portfolio
        val crossRef = PortfolioStocksCrossRef(
            portfolioId,
            symbol)
        dao.insertStockIntoPortfolio(crossRef)
    }

    // 6. BATCH UPDATE
    suspend fun refreshBatch(symbols: String) {
        // 1. Call API
        val response = api.getBatchQuotes(symbols = symbols)

        // DEBUG: Check for errors explicitly
        if (response.note != null) Log.e("STOCK_DEBUG", "Batch Note: ${response.note}")
        if (response.information != null) Log.e("STOCK_DEBUG", "Batch Info: ${response.information}")
        if (response.errorMessage != null) Log.e("STOCK_DEBUG", "Batch Error: ${response.errorMessage}")

        // 2. Validate
        if (response.stockQuotes.isNullOrEmpty()) {
            // DEBUG LOG for Batch
            Log.e("STOCK_DEBUG", "Batch response empty. Symbols: $symbols")
            return
        }

        // 3. Map API data to Database Entities
        response.stockQuotes.forEach { quote ->
            // Use the update query to preserve the Company Name
            dao.updatePrice(
                symbol = quote.symbol,
                price = quote.price.toDoubleOrNull() ?: 0.0,
                change = quote.changePercent,
                prevClose = quote.previousClose.toDoubleOrNull() ?: 0.0,
                time = System.currentTimeMillis()
            )
        }
    }

    suspend fun refreshSingleStock(symbol: String) {
        val response = api.getQuote(symbol)

        val quote = response.globalQuote
        if (quote == null || quote.symbol.isEmpty()) {
            Log.e("STOCK_DEBUG", "Empty quote returned for $symbol")
            return
        }

        // Update the database with the fresh price!
        dao.updatePrice(
            symbol = quote.symbol,
            price = quote.price.toDoubleOrNull() ?: 0.0,
            change = quote.changePercent,
            prevClose = quote.previousClose.toDoubleOrNull() ?: 0.0,
            time = System.currentTimeMillis()
        )
    }

    // --- USAGE TRACKING ---
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

    suspend fun removeStockFromPortfolio(symbol: String, portfolioId: Int) {
        dao.removeStockFromPortfolio(portfolioId, symbol)
    }

}