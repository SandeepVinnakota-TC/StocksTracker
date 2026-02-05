package com.sandeep.stockstracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_stats")
data class UsageEntity(
    @PrimaryKey val id: Int = 1, // Always 1, so we just overwrite the same row
    val date: String,            // "yyyy-MM-dd" format
    val apiCalls: Int
)