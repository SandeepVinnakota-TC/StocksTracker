package com.sandeep.stockstracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "portfolio_stock_cross_ref",
    primaryKeys = ["portfolioId", "stockSymbol"]
)

data class PortfolioStocksCrossRef(
    val portfolioId: Int,
    val stockSymbol: String
)