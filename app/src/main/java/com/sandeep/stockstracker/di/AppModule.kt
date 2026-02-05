package com.sandeep.stockstracker.di

import android.app.Application
import androidx.room.Room
import com.sandeep.stockstracker.data.StockApi
import com.sandeep.stockstracker.data.StockDao
import com.sandeep.stockstracker.data.StockDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // 1. Provide Database
    @Provides
    @Singleton
    fun provideStockDatabase(app: Application): StockDatabase {
        return Room.databaseBuilder(
            app,
            StockDatabase::class.java,
            "stock_database"
        )
            .fallbackToDestructiveMigration() // Handle version updates (like adding UsageEntity)
            .build()
    }

    // 2. Provide DAO (This is what StockRepository needs!)
    @Provides
    fun provideStockDao(db: StockDatabase): StockDao {
        return db.stockDao()
    }

    // 3. Provide API (This is what StockRepository needs!)
    @Provides
    @Singleton
    fun provideStockApi(): StockApi {
        return Retrofit.Builder()
            .baseUrl("https://www.alphavantage.co/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StockApi::class.java)
    }
}