package com.sandeep.stockstracker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.stockstracker.data.SearchResultDto
import com.sandeep.stockstracker.data.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {

    val portfolio = repository.portfolio
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var searchResults by mutableStateOf<List<SearchResultDto>>(emptyList())
        private set
    var errorState by mutableStateOf<String?>(null)
        private set

    var toastMessage by mutableStateOf<String?>(null)
        private set

    var apiCallsMade by mutableStateOf(0)
        private set

    val requestsLeft: Int
        get() = (25 - apiCallsMade).coerceAtLeast(0)

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            apiCallsMade = repository.getApiCallCount()
        }
    }

    fun clearToast() {
        toastMessage = null
    }

    fun onSearchQueryChange(query: String) {
        searchJob?.cancel()
        if (query.length < 2) {
            searchResults = emptyList()
            return
        }

        // Pre-check: Don't search if limit is 0
        if (requestsLeft <= 0) {
            toastMessage = "Daily limit reached. Try again tomorrow."
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)
            try {
                // Optimistically increment (will be corrected if limit hit)
                incrementUsage()

                val results = repository.searchStocks(query)
                if (results.isEmpty()) {
                    toastMessage = "No stocks found for '$query'"
                }
                searchResults = results

            } catch (e: Exception) {
                handleError(e) // <--- Use helper function
            }
        }
    }

    fun selectStock(symbol: String, name: String) {
        if (requestsLeft <= 0) {
            toastMessage = "Daily limit reached. Try again tomorrow."
            return
        }

        viewModelScope.launch {
            try {
                errorState = null
                searchResults = emptyList()
                repository.addStock(symbol, name)
                incrementUsage()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun refreshPortfolio() {
        val currentList = portfolio.value
        if (currentList.isEmpty()) return

        // FIX 2: Don't start refresh if we have no requests left
        if (requestsLeft <= 0) {
            toastMessage = "Daily limit reached. Try again tomorrow."
            return
        }

        viewModelScope.launch {
            try {
                // Loop safely: Stop if we hit the limit mid-loop
                for (stock in currentList) {
                    if (requestsLeft <= 0) break // Stop loop if we just hit 0

                    repository.refreshStock(stock) // You might need to update Repository to expose single refresh (see below)
                    incrementUsage()
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    // --- HELPER FUNCTIONS ---

    private fun handleError(e: Exception) {
        // FIX 1: Ignore cancellation errors (stops the bad toast)
        if (e is CancellationException) return

        // FIX 2: If API says limit reached, force counter to 0 (max usage)
        if (e.message?.contains("Limit Reached") == true) {
            apiCallsMade = 25
            toastMessage = "Daily API Limit Reached."
        } else {
            e.printStackTrace()
            toastMessage = e.message ?: "An error occurred"
        }
    }

    private suspend fun incrementUsage() {
        repository.incrementApiCallCount()
        apiCallsMade = repository.getApiCallCount()
    }
}