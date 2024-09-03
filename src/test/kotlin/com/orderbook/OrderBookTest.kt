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

    @Test
    fun testAddOrderAndCheckTradeHistory() {
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
        val tradeHistory = orderBookService.getTradeHistory()
        assert(tradeHistory.isEmpty())
    }

    @Test
    fun testAddOrderAndCheckTradeHistoryWithMatch() {
        val orderBookService = OrderBookService()
        val order1 = Order(
            allowMargin = "true",
            customerOrderId = "1",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "SELL",
            timeInForce = "GTC"
        )
        val order2 = Order(
            allowMargin = "true",
            customerOrderId = "2",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "BUY",
            timeInForce = "GTC"
        )
        orderBookService.addOrder(order1)
        orderBookService.addOrder(order2)
        val tradeHistory = orderBookService.getTradeHistory()
        assert(tradeHistory.isNotEmpty())
    }

    @Test
    fun testAddOrderAndCheckTradeHistoryWithNoMatch() {
        val orderBookService = OrderBookService()
        val order1 = Order(
            allowMargin = "true",
            customerOrderId = "1",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "BUY",
            timeInForce = "GTC"
        )
        val order2 = Order(
            allowMargin = "true",
            customerOrderId = "2",
            pair = "BTCUSD",
            postOnly = true,
            price = "10001",
            quantity = "1",
            side = "SELL",
            timeInForce = "GTC"
        )
        orderBookService.addOrder(order1)
        orderBookService.addOrder(order2)
        val tradeHistory = orderBookService.getTradeHistory()
        assert(tradeHistory.isEmpty())
    }

    @Test
    fun testAddOrderAndCheckOrderBookWithMatch() {
        val orderBookService = OrderBookService()
        val order1 = Order(
            allowMargin = "true",
            customerOrderId = "1",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "SELL",
            timeInForce = "GTC"
        )
        val order2 = Order(
            allowMargin = "true",
            customerOrderId = "2",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "BUY",
            timeInForce = "GTC"
        )
        orderBookService.addOrder(order1)
        orderBookService.addOrder(order2)
        val orderBook = orderBookService.getOrderBook()
        assert(orderBook.Bids.isEmpty())
        assert(orderBook.Asks.isEmpty())
    }

    @Test
    fun testAddOrderAndCheckOrderBookWithNoMatch() {
        val orderBookService = OrderBookService()
        val order1 = Order(
            allowMargin = "true",
            customerOrderId = "1",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "BUY",
            timeInForce = "GTC"
        )
        val order2 = Order(
            allowMargin = "true",
            customerOrderId = "2",
            pair = "BTCUSD",
            postOnly = true,
            price = "10001",
            quantity = "1",
            side = "SELL",
            timeInForce = "GTC"
        )
        orderBookService.addOrder(order1)
        orderBookService.addOrder(order2)
        val orderBook = orderBookService.getOrderBook()
        assert(orderBook.Bids.isNotEmpty())
        assert(orderBook.Asks.isNotEmpty())
    }
}