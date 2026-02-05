package com.sandeep.stockstracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // <--- This annotation is the key!
class StockApplication : Application()