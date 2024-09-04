package com.orderbook

import com.orderbook.models.*
import org.slf4j.LoggerFactory
import java.util.*

class OrderBookService {
    private val asks = PriorityQueue<LimitOrder>(compareBy { it.price.toDouble() })
    private val bids = PriorityQueue<LimitOrder>(compareByDescending { it.price.toDouble() })
    var trades = mutableListOf<Trade>()
    private var openOrders = mutableListOf<OpenOrder>()
    private var orders = mutableListOf<Order>()

    private val logger = LoggerFactory.getLogger(OrderBookService::class.java)


    fun getOrderBook(): OrderBook {
        return OrderBook(
            asks.toMutableList(), bids.toMutableList(),
            LastChange = "",
            SequenceNumber = "1".toLong()
        )
    }

    fun addLimitOrder(limitOrder: LimitOrder): String {
        val orderId = UUID.randomUUID().toString()
        if (limitOrder.side.uppercase() == "BUY") {
            bids.offer(limitOrder)
            addToOrders(limitOrder, orderId, "PLACED")
            addToOpenOrders(limitOrder, orderId, "PLACED")

        } else if (limitOrder.side.uppercase() == "SELL") {
            asks.offer(limitOrder)
            addToOrders(limitOrder, orderId, "PLACED")
            addToOpenOrders(limitOrder, orderId, "PLACED")
        }
        matchOrders()
        return orderId
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

        val orderId = UUID.randomUUID().toString()
        // Remove fully fulfilled orders
        if (bid.quantity.toDouble() == 0.0){
            bids.poll()
            updateOrderStatus(bid.customerOrderId, "FILLED")
        } else
            addToOpenOrders(bid, orderId, "PARTIALLY_FILLED")

        if (ask.quantity.toDouble() == 0.0){
            asks.poll()
            updateOrderStatus(ask.customerOrderId, "FILLED")
        }
        else
            addToOpenOrders(ask, orderId, "PARTIALLY_FILLED")

        return trade
    }

    private fun updateOrderStatus(orderId: String, status: String) {
        val order = orders.find { it.customerOrderId == orderId }
        if (order != null) {
            order.orderStatusType = status
            logger.info("Order status updated: $order")
            removeFromOpenOrders(order.orderId)
        }
    }

    //add order to open orders
    private fun addToOpenOrders(limitOrder: LimitOrder, orderId: String, status: String) {
        val openOrder = OpenOrder(
            allowMargin = false,
            createdAt = Date().toString(),
            currencyPair = limitOrder.pair,
            filledPercentage = "0",
            orderId =orderId,
            originalQuantity = limitOrder.quantity,
            price = limitOrder.price,
            remainingQuantity = limitOrder.quantity,
            side = limitOrder.side,
            status = status,
            timeInForce = "GTC",
            type = "LIMIT",
            updatedAt = Date().toString()
        )
            openOrders.add(openOrder)
            logger.info("Open order added: $openOrder")

    }

    //add order to orders
    private fun addToOrders(limitOrder: LimitOrder, orderId: String, status: String) {
        val order = Order(
            currencyPair = limitOrder.pair,
            orderId = orderId,
            originalQuantity = limitOrder.quantity,
            remainingQuantity = limitOrder.quantity,
            timeInForce = "GTC",
            customerOrderId = limitOrder.customerOrderId,
            failedReason = null.toString(),
            orderCreatedAt = Date().toString(),
            orderSide = limitOrder.side,
            orderStatusType = status,
            orderType = "LIMIT",
            orderUpdatedAt = Date().toString(),
            originalPrice = limitOrder.price
        )
        orders.add(order)
        logger.info("Order added: $order")
    }

    private fun removeFromOpenOrders(orderId: String) {
        openOrders.removeIf { it.orderId == orderId }
        logger.info("Order with orderId: $orderId removed from open orders")
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

    //get orders
    fun getOrders(): List<Order>{
        logger.info("Orders: $orders")
        return orders
    }

    //get order by orderId
    fun getOrderByOrderId(orderId: String): Order? {
        val order = orders.find { it.orderId == orderId }
        logger.info("Order by orderId: $order")
        return order
    }

    fun isCustomerOrderIdUnique(customerOrderId: String): Boolean {
        return orders.none { it.customerOrderId == customerOrderId }
    }
}