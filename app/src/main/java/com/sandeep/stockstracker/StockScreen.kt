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
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext

@Composable
fun StockScreen(
    viewModel: StockViewModel = viewModel()
) {
    // 1. Observe Data
    val portfolios by viewModel.portfolios.collectAsState()
    val selectedPortfolioId by viewModel.selectedPortfolioId.collectAsState()
    val portfolioStocks by viewModel.portfolio.collectAsState()
    val searchResults = viewModel.searchResults
    val error = viewModel.errorState
    val requestsLeft = viewModel.requestsLeft

    // Dialog states
    var showSearchDialog by remember { mutableStateOf(false) }
    var showNewPortfolioDialog by remember { mutableStateOf(false) }

    val lastUpdatedTime = remember(portfolioStocks) {
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        dateFormat.format(Date())
    }

    val context = LocalContext.current
    val toastMessage = viewModel.toastMessage

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { showSearchDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Stock")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Portfolios", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { viewModel.refreshPortfolio() }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }

            // --- PORTFOLIO TABS ---
            if (portfolios.isNotEmpty()) {
                val selectedIndex =
                    portfolios.indexOfFirst { it.id == selectedPortfolioId }.coerceAtLeast(0)

                ScrollableTabRow(
                    selectedTabIndex = selectedIndex,
                    edgePadding = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    portfolios.forEachIndexed { index, portfolio ->
                        Tab(
                            selected = selectedIndex == index,
                            onClick = { viewModel.selectPortfolio(portfolio.id) },
                            text = { Text(portfolio.name, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                    // The "+ New" Tab to create a portfolio
                    Tab(
                        selected = false,
                        onClick = { showNewPortfolioDialog = true },
                        text = { Text("+ New", color = MaterialTheme.colorScheme.primary) }
                    )
                }
            }

            // --- INFO ROW ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Last Updated: $lastUpdatedTime", fontSize = 12.sp, color = Color.Gray)
                val limitColor = if (requestsLeft < 5) Color.Red else Color.Gray
                Text(
                    text = "Requests Left: $requestsLeft",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = limitColor
                )
            }

            if (error != null) {
                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // --- STOCK LIST ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = portfolioStocks,
                    key = { stock -> stock.symbol }
                ) { stock ->

                    // 1. Create the State
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                // 2. Call ViewModel action!
                                val currentPortfolio = selectedPortfolioId
                                if (currentPortfolio != null) {
                                    viewModel.removeStock(stock.symbol, currentPortfolio)
                                    true // Confirm the swipe
                                } else {
                                    false // Don't swipe if we somehow don't have a portfolio
                                }
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        state = dismissState,
                        enableDismissFromStartToEnd = false, // Only allow right-to-left swipe
                        backgroundContent = {
                            // Red background with the trash icon
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(12))
                                    .background(Color(0xFFC00000))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White
                                )
                            }
                        }
                    ) {
                        StockCard(stock = stock)
                    }
                }
            } // END OF LazyColumn
        } // END OF Column

        // --- SEARCH STOCK DIALOG ---
        if (showSearchDialog) {
            AlertDialog(
                onDismissRequest = { showSearchDialog = false },
                title = { Text("Add Stock to Portfolio") },
                text = {
                    Column {
                        var query by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = query,
                            onValueChange = {
                                query = it; viewModel.onSearchQueryChange(it)
                            },
                            label = { Text("Symbol") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(searchResults) { result ->
                                Text(
                                    text = "${result.symbol} - ${result.name}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectStock(
                                                result.symbol,
                                                result.name
                                            )
                                            showSearchDialog = false
                                        }
                                        .padding(12.dp)
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSearchDialog = false }) { Text("Close") }
                }
            )
        }

        // --- NEW PORTFOLIO DIALOG ---
        if (showNewPortfolioDialog) {
            var portfolioName by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showNewPortfolioDialog = false },
                title = { Text("Create New Portfolio") },
                text = {
                    OutlinedTextField(
                        value = portfolioName,
                        onValueChange = { portfolioName = it },
                        label = { Text("Portfolio Name") },
                        singleLine = true,
                        placeholder = { Text("e.g. Retirement") }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (portfolioName.isNotBlank()) {
                                viewModel.createNewPortfolio(portfolioName)
                                showNewPortfolioDialog = false
                            }
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewPortfolioDialog = false }) { Text("Cancel") }
                }
            )
        }

    } // END OF Scaffold
} // END OF StockScreen


@Composable
fun StockCard(stock: StockEntity) {
    val isDrop = stock.price < stock.previousClose
    val cardColor = if (isDrop) Color.Red else Color(0xFF4CAF50)
    val arrow = if (isDrop) "↓" else "↑"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stock.symbol, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = stock.companyName,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${stock.price}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$arrow ${stock.changePercent}",
                    fontSize = 14.sp,
                    color = cardColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}