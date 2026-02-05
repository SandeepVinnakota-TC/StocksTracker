package com.sandeep.stockstracker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sandeep.stockstracker.data.StockEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun StockScreen(
    viewModel: StockViewModel = viewModel()
) {
    // 1. Observe Data
    val portfolio by viewModel.portfolio.collectAsState()
    val searchResults = viewModel.searchResults
    val error = viewModel.errorState

    // 2. Get the Requests Count from ViewModel
    val requestsLeft = viewModel.requestsLeft

    var showDialog by remember { mutableStateOf(false) }

    // 3. Generate Time (Updates when portfolio updates)
    val lastUpdatedTime = remember(portfolio) {
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        dateFormat.format(Date())
    }

    // Observe Toast Message
    val context = LocalContext.current
    val toastMessage = viewModel.toastMessage

    // Show Toast when message changes
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearToast() // Reset so it can show again later
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Search")
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(text = "My Portfolio", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { viewModel.refreshPortfolio() }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }

            // --- INFO ROW (Time Left | Requests Right) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween, // Pushes items to edges
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Time
                Text(
                    text = "Last Updated: $lastUpdatedTime",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                // Right: Requests Left
                // Color changes to Red if requests are low (< 5)
                val limitColor = if (requestsLeft < 5) Color.Red else Color.Gray
                Text(
                    text = "Requests Left: $requestsLeft",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold, // Made bold to see easily
                    color = limitColor
                )
            }

            if (error != null) {
                Text(text = error!!, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
            }

            // Stock List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(portfolio) { stock ->
                    StockCard(stock = stock)
                }
            }
        }

        // Search Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add Stock") },
                text = {
                    Column {
                        var query by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it; viewModel.onSearchQueryChange(it) },
                            label = { Text("Symbol") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Search Results List
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(searchResults) { result ->
                                Text(
                                    text = "${result.symbol} - ${result.name}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectStock(result.symbol, result.name)
                                            showDialog = false
                                        }
                                        .padding(12.dp)
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showDialog = false }) { Text("Close") } }
            )
        }
    }
}

@Composable
fun StockCard(stock: StockEntity) {
    val isDrop = stock.price < stock.previousClose
    val cardColor = if (isDrop) Color.Red else Color(0xFF4CAF50)
    val arrow = if (isDrop) "↓" else "↑"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = stock.symbol, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = stock.companyName, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "$${stock.price}", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "$arrow ${stock.changePercent}", fontSize = 14.sp, color = cardColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}