package com.sandeep.stockstracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.sandeep.stockstracker.ui.theme.StocksTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import android.graphics.Color as AndroidColor

@AndroidEntryPoint // <--- SUPER IMPORTANT: This turns on Hilt for this screen
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Force the Status Bar to be Transparent with DARK icons
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                AndroidColor.TRANSPARENT, // Background color (transparent)
                AndroidColor.TRANSPARENT  // Scrim color (transparent)
            )
        )

        setContent { // Starts Jetpack Compose UI
            StocksTrackerTheme { // Applies app theme colors and style
                Surface( // A background container
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StockScreen() // Loads the main screen UI
                }
            }
        }
    }
}