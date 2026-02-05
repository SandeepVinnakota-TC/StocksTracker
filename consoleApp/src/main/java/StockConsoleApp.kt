package com.sandeep.consoleapp

import org.json.JSONObject
import java.net.URL
import java.util.Scanner

data class Stock(
    val symbol: String,
    val price: Double,
    val date: String
)

fun main() {
    val scanner = Scanner(System.`in`)
    val apiKey = "JRGJHDRMG2PCJ1AY"

    println("--- Welcome to the Stock Tracker Console ---")

    while (true) {
        print("\nEnter stock symbol (e.g., IBM, AAPL) or 'exit' to quit: ")
        val input = scanner.next()

        if (input.equals("exit", ignoreCase = true)) {
            println("Goodbye!")
            break
        }

        println("Fetching data for $input...")

        try {
            val urlString = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=$input&apikey=$apiKey"
            val jsonText = URL(urlString).readText()
            val jsonObject = JSONObject(jsonText)

            if (jsonObject.has("Global Quote")) {
                val quote = jsonObject.getJSONObject("Global Quote")
                val priceString = quote.getString("05. price")
                val dateString = quote.getString("07. latest trading day")

                val stock = Stock(input.uppercase(), priceString.toDouble(), dateString)
                println("------------------------------")
                println("Symbol: ${stock.symbol}")
                println("Price:  $${stock.price}")
                println("Date:   ${stock.date}")
                println("------------------------------")
            } else {
                println("Error: Stock not found or API limit reached.")
                println("Raw Response: $jsonText")
            }

        } catch (e: Exception) {
            println("Failed to fetch data: ${e.message}")
        }
    }
}