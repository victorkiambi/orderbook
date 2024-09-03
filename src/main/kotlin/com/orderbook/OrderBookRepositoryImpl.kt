package com.orderbook

import com.orderbook.dto.OrderBookDto
import com.orderbook.models.OrderBook
import com.orderbook.models.toOrderBookDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class OrderBookRepositoryImpl: OrderBookRepository {
    private val client = HttpClient.newBuilder().build()
    override suspend fun getOrderBook(): OrderBookDto {
        val request = HttpRequest.newBuilder()
            .uri(URI("https://api.valr.com/v1/public/BTCZAR/orderbook"))
            .build()

        val response = withContext(Dispatchers.IO) {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        }

        val result = Json.decodeFromString<OrderBook>(response.body())
        return result.toOrderBookDto()
    }

}