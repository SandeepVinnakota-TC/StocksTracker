package com.sandeep.stockstracker.data

import com.google.gson.annotations.SerializedName

data class StockResponse(
    @SerializedName("Global Quote")
    val globalQuote: StockDto? = null
)

data class StockDto(
    @SerializedName("01. symbol")
    val symbol: String,
    @SerializedName("05. price")
    val price: String,
    @SerializedName("07. latest trading day")
    val tradingDate: String,
    @SerializedName("08. previous close")
    val previousClose: String,
    @SerializedName("10. change percent")
    val changePercent: String
)

data class SearchResponse(
    @SerializedName("bestMatches")
    val bestMatches: List<SearchResultDto>? = null,

    // Error Fields
    @SerializedName("Note")
    val note: String? = null,
    @SerializedName("Information")
    val information: String? = null,
    @SerializedName("Error Message")
    val errorMessage: String? = null
)

data class SearchResultDto(
    @SerializedName("1. symbol")
    val symbol: String,
    @SerializedName("2. name")
    val name: String
)

data class BatchResponse(
    @SerializedName("Stock Quotes")
    val stockQuotes: List<StockDto>? = null,

    @SerializedName("Note")
    val note: String? = null,

    @SerializedName("Information")
    val information: String? = null,

    @SerializedName("Error Message")
    val errorMessage: String? = null
)