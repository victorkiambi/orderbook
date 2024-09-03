package com.orderbook

import com.orderbook.models.Order
import com.orderbook.models.OrderBook

class OrderBookService {
    private val asks = mutableListOf<Order>()
    private val bids = mutableListOf<Order>()

    fun addOrder(order: Order) {
        if (order.side.uppercase() == "BUY") {
            bids.add(order)
            bids.sortByDescending { it.price.toDouble() }
        } else if (order.side.uppercase() == "SELL") {
            asks.add(order)
            asks.sortBy { it.price.toDouble() }
        }
    }

    fun getOrderBook(): OrderBook {
        return OrderBook(
            asks, bids,
            LastChange = "",
            SequenceNumber = "1".toLong()
        )
    }
}