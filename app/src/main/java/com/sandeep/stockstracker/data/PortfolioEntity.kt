package com.sandeep.stockstracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio")
data class PortfolioEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)