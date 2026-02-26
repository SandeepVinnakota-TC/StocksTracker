package com.sandeep.stockstracker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.stockstracker.data.PortfolioEntity
import com.sandeep.stockstracker.data.SearchResultDto
import com.sandeep.stockstracker.data.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {

    // --- PORTFOLIO STATE ---

    val portfolios = repository.portfolios
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPortfolioId = MutableStateFlow<Int?>(null)
    val selectedPortfolioId: StateFlow<Int?> = _selectedPortfolioId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val portfolio = _selectedPortfolioId.flatMapLatest { id ->
        if (id != null) {
            repository.getStocksForPortfolio(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- UI STATE ---

    var searchResults by mutableStateOf<List<SearchResultDto>>(emptyList())
        private set
    var errorState by mutableStateOf<String?>(null)
        private set
    var toastMessage by mutableStateOf<String?>(null)
        private set
    var apiCallsMade by mutableIntStateOf(0)
        private set

    val requestsLeft: Int
        get() = (25 - apiCallsMade).coerceAtLeast(0)

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            apiCallsMade = repository.getApiCallCount()
        }

        viewModelScope.launch {
            repository.portfolios.collect { ports ->
                if (ports.isEmpty()) {
                    repository.createPortfolio("Main Portfolio")
                } else if (_selectedPortfolioId.value == null) {
                    _selectedPortfolioId.value = ports.first().id
                }
            }
        }
    }

    // --- INTENTS (User Actions) ---

    fun selectPortfolio(portfolioId: Int) {
        _selectedPortfolioId.value = portfolioId
    }

    fun createNewPortfolio(name: String) {
        viewModelScope.launch {
            repository.createPortfolio(name)
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

        if (requestsLeft <= 0) {
            toastMessage = "Daily limit reached. Try again tomorrow."
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)
            try {
                incrementUsage()
                val results = repository.searchStocks(query)
                if (results.isEmpty()) {
                    toastMessage = "No stocks found for '$query'"
                }
                searchResults = results
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun selectStock(symbol: String, name: String) {
        val currentPortfolioId = _selectedPortfolioId.value

        if (currentPortfolioId == null) {
            toastMessage = "Please select a portfolio first"
            return
        }
        if (requestsLeft <= 0) {
            toastMessage = "Daily limit reached. Try again tomorrow."
            return
        }

        viewModelScope.launch {
            try {
                errorState = null
                searchResults = emptyList()

                // Add to the specific portfolio
                repository.addStockToPortfolio(symbol, name, currentPortfolioId)

                // Fetch the price immediately so it doesn't stay at $0.0!
                repository.refreshSingleStock(symbol)
                incrementUsage()

                toastMessage = "$symbol added and price updated!"
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun refreshPortfolio() {
        val currentList = portfolio.value
        if (currentList.isEmpty()) return

        if (requestsLeft < currentList.size) {
            toastMessage = "Not enough daily limit left to refresh all."
            return
        }

        viewModelScope.launch {
            try {
                currentList.forEach { stock ->
                    repository.refreshSingleStock(stock.symbol)
                    incrementUsage()
                }
                toastMessage = "Portfolio updated"
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    // NEW: Action called by the SwipeToDismiss UI
    fun removeStock(symbol: String, portfolioId: Int) {
        viewModelScope.launch {
            try {
                repository.removeStockFromPortfolio(symbol, portfolioId)
                toastMessage = "$symbol removed"
            } catch (e: Exception) {
                toastMessage = "Failed to remove $symbol"
            }
        }
    }

    private fun handleError(e: Exception) {
        if (e is CancellationException) return
        e.printStackTrace()
        val errorMessage = e.message ?: "Unknown Error"

        if (errorMessage.contains("500 calls per day") || errorMessage.contains("daily limit")) {
            apiCallsMade = 25
            toastMessage = "Daily API Limit Reached."
        } else if (errorMessage.contains("frequency") || errorMessage.contains("call frequency")) {
            toastMessage = "Please try after a minute."
        } else {
            toastMessage = errorMessage
        }
    }

    private suspend fun incrementUsage() {
        repository.incrementApiCallCount()
        apiCallsMade = repository.getApiCallCount()
    }
}