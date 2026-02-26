package com.sandeep.stockstracker.data

import retrofit2.http.GET
import retrofit2.http.Query

interface StockApi {

    // 1. Search for a stock
    // Matches the Repository call: api.search(query)
    @GET("query?function=SYMBOL_SEARCH")
    suspend fun search(
        @Query("keywords") query: String,
        // We set a default value here so the Repository doesn't need to pass it every time
        @Query("apikey") apiKey: String = "JRGJHDRMG2PCJ1AY"
    ): SearchResponse

    // 2. Get the latest price
    // Matches the Repository call: api.getQuote(symbol)
    @GET("query?function=GLOBAL_QUOTE")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String = "JRGJHDRMG2PCJ1AY"
    ): StockResponse

    @GET("query")
    suspend fun getBatchQuotes(
        @Query("function") function: String = "BATCH_STOCK_QUOTES",
        @Query("symbols") symbols: String,
        @Query("apikey") apiKey: String = "JRGJHDRMG2PCJ1AY"
    ): BatchResponse

}