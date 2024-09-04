package com.orderbook

import com.orderbook.models.OpenOrder
import com.orderbook.models.Order
import com.orderbook.models.OrderBook
import com.orderbook.models.Trade
import org.slf4j.LoggerFactory
import java.util.*

class OrderBookService {
    private val asks = PriorityQueue<Order>(compareBy { it.price.toDouble() })
    private val bids = PriorityQueue<Order>(compareByDescending { it.price.toDouble() })
    var trades = mutableListOf<Trade>()
    private var openOrders = mutableListOf<OpenOrder>()

    private val logger = LoggerFactory.getLogger(OrderBookService::class.java)


    fun getOrderBook(): OrderBook {
        return OrderBook(
            asks.toMutableList(), bids.toMutableList(),
            LastChange = "",
            SequenceNumber = "1".toLong()
        )
    }

    fun addOrder(order: Order): Trade? {
        if (order.side.uppercase() == "BUY") {
            bids.offer(order)

        } else if (order.side.uppercase() == "SELL") {
            asks.offer(order)
        }
        val trade = matchOrders()
        if (trade != null) {
           addToOpenOrders(order, "PLACED")
        }
        else {
            addToOpenOrders(order, "PARTIALLY_FILLED")
        }
        return trade
    }

    private fun matchOrders(): Trade? {
        if (bids.isEmpty() || asks.isEmpty()) {
            logger.info("No bids or asks")
            return null
        }

        val bid = bids.peek()
        val ask = asks.peek()

        // Early return if no match
        if (bid.price.toDouble() < ask.price.toDouble()) {
            logger.info("No match")
            return null
        }

        val tradeQuantity = minOf(bid.quantity, ask.quantity)
        val trade = Trade(
            bid.customerOrderId,
            ask.customerOrderId,
            bid.price,
            tradeQuantity,
            bid.pair,
            tradedAt = System.currentTimeMillis().toString(),
            takerSide = bid.side,
            sequenceId = 1,
        )

        trades.add(trade)
        logger.info("Trade created: $trade")

        bid.quantity = (bid.quantity.toDouble() - tradeQuantity.toDouble()).toString()
        ask.quantity = (ask.quantity.toDouble() - tradeQuantity.toDouble()).toString()

        // Remove fully fulfilled orders
        if (bid.quantity.toDouble() == 0.0) bids.poll() else addToOpenOrders(bid, "PARTIALLY_FILLED")
        if (ask.quantity.toDouble() == 0.0) asks.poll() else addToOpenOrders(ask, "PARTIALLY_FILLED")

        return trade
    }

    //add order to open orders
    private fun addToOpenOrders(order: Order, status: String) {
        if (order.quantity.toDouble() == 0.0) {
            openOrders.removeIf { it.orderId == order.customerOrderId }
            logger.info("Order fully filled and removed from open orders: ${order.customerOrderId}")
        } else {
            val openOrder = OpenOrder(
                allowMargin = false,
                createdAt = Date().toString(),
                currencyPair = order.pair,
                filledPercentage = "0",
                orderId = order.customerOrderId,
                originalQuantity = order.quantity,
                price = order.price,
                remainingQuantity = order.quantity,
                side = order.side,
                status = status,
                timeInForce = "GTC",
                type = "LIMIT",
                updatedAt = Date().toString()
            )
            openOrders.add(openOrder)
            logger.info("Open order added: $openOrder")
        }
    }
    //get trade history
    fun getTradeHistory(): List<Trade>{
        logger.info("Trade history: $trades")
        return trades
    }

    //get open orders
    fun getOpenOrders(): List<OpenOrder>{
        logger.info("Open orders: $openOrders")
        return openOrders
    }
}