package com.orderbook

import com.orderbook.models.Order
import kotlin.test.Test


class OrderBookTest {
    @Test
    fun testAddOrder() {
        val orderBookService = OrderBookService()
        val order = Order(
            allowMargin = "true",
            customerOrderId = "1",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "BUY",
            timeInForce = "GTC"
        )
        orderBookService.addOrder(order)
        val orderBook = orderBookService.getOrderBook()
        assert(orderBook.Bids.isNotEmpty())
    }

    @Test
    fun testGetOrderBook() {
        val orderBookService = OrderBookService()
        val orderBook = orderBookService.getOrderBook()
        assert(orderBook.Asks.isEmpty())
        assert(orderBook.Bids.isEmpty())
    }

    @Test
    fun testAddOrderAndCheckOrderBook() {
        val orderBookService = OrderBookService()
        val order = Order(
            allowMargin = "true",
            customerOrderId = "1",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "BUY",
            timeInForce = "GTC"
        )
        orderBookService.addOrder(order)
        val orderBook = orderBookService.getOrderBook()
        assert(orderBook.Bids.isNotEmpty())
        assert(orderBook.Asks.isEmpty())
    }
}