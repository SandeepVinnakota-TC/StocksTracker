# 📈 StockTracker

A Android application built with **Jetpack Compose** that allows users to track real-time stock prices, manage a personal portfolio, and monitor daily API usage limits.

## ✨ Features

* **Real-time Stock Data:** Fetches live stock quotes using the AlphaVantage API.
* **Portfolio Management:** Add stocks to your watchlist and see them persist locally.
* **Smart Caching:** Uses **Room Database** to save stock data for offline viewing.
* **API Usage Tracking:** Built-in logic to track and limit API calls (25 requests/day) to stay within the free tier limits.
* **Search Functionality:** Search for companies by symbol or name.
* **Modern UI:** Clean, Material 3 design with Dark/Light mode support and edge-to-edge layout.

## 🛠 Tech Stack

* **Language:** Kotlin
* **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Dependency Injection:** [Hilt](https://dagger.dev/hilt/)
* **Networking:** [Retrofit](https://square.github.io/retrofit/) + Gson
* **Local Storage:** [Room Database](https://developer.android.com/training/data-storage/room)
* **Concurrency:** Kotlin Coroutines & Flow
* **API:** [AlphaVantage](https://www.alphavantage.co/)

## 🏗 Architecture

The app follows the **Clean Architecture** principles using the MVVM pattern:

1.  **UI Layer (Compose):** `StockScreen.kt` observes state from the ViewModel.
2.  **ViewModel:** `StockViewModel.kt` manages UI state, handles business logic (like checking the 25-call limit), and communicates with the Repository.
3.  **Data Layer:**
    * **Repository:** `StockRepository.kt` acts as the single source of truth, deciding whether to fetch from Network or Local Database.
    * **Remote:** `StockApi.kt` handles network requests.
    * **Local:** `StockDao.kt` handles database operations.

## 🚀 Getting Started

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/SandeepVinnakota-TC/StockTracker.git](https://github.com/SandeepVinnakota-TC/StockTracker.git)
    ```
2.  **Open in Android Studio:**
    * File > Open > Select the cloned folder.
3.  **API Key Configuration:**
    * The project currently uses a demo API key. For full functionality, obtain a free API key from [AlphaVantage](https://www.alphavantage.co/support/#api-key).
    * Replace the key in `StockApi.kt` or `StockConsoleApp.kt`.
4.  **Run the App:**
    * Build and run on an Emulator or Physical Device.

## 📸 Screenshots

*(You can add screenshots here later by dragging images into your repo)*

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
