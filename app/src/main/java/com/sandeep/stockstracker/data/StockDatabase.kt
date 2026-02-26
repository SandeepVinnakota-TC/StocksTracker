package com.sandeep.stockstracker.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [
    StockEntity::class,
    UsageEntity::class,
    PortfolioEntity::class,
    PortfolioStocksCrossRef::class],
    version = 3,
    exportSchema = false
    )
abstract class StockDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
}