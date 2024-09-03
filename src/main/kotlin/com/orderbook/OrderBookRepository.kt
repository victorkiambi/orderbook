package com.orderbook

import com.orderbook.dto.OrderBookDto

interface OrderBookRepository {
    suspend fun getOrderBook(): OrderBookDto
}