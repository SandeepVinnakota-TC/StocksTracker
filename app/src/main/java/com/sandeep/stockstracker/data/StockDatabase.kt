package com.sandeep.stockstracker.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [StockEntity::class, UsageEntity::class], version = 2)
abstract class StockDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
}