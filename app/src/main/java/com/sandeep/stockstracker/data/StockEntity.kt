package com.sandeep.stockstracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_table")
data class StockEntity(
    @PrimaryKey
    val symbol: String, // Unique ID e.g. "IBM"

    val companyName: String, // e.g. "International Business Machines"

    val price: Double,

    val changePercent: String,

    val previousClose: Double,

    val lastFetchedTimestamp: Long = System.currentTimeMillis() // When did we last check happened?
)