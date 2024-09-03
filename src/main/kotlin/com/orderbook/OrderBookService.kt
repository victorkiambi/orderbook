package com.orderbook

import com.orderbook.dto.OrderBookDto

class OrderBookService(
    private val orderBookRepository: OrderBookRepository,
) {
    suspend fun getOrderBook(): OrderBookDto {
        return orderBookRepository.getOrderBook()
    }
}