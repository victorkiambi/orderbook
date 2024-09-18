package com.orderbook

import com.orderbook.models.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class OrderBookService {
    private val asks = PriorityQueue<LimitOrder>(compareBy { it.price.toDouble() })
    private val bids = PriorityQueue<LimitOrder>(compareByDescending { it.price.toDouble() })
    var trades = mutableListOf<Trade>()
    private var openOrders = mutableListOf<OpenOrder>()
    private var orders = mutableListOf<Order>()

    private val logger = LoggerFactory.getLogger(OrderBookService::class.java)
    private val sequenceCounter = AtomicLong(0)
    private val mutex = Mutex()

    fun getOrderBook(): OrderBook {
        return OrderBook(
            Asks = asks.map { limitOrderToAsk(it) }.toMutableList(),
            Bids = bids.map { limitOrderToBid(it) }.toMutableList(),
            LastChange = "",
            SequenceNumber = "1".toLong()
        )
    }

    private fun limitOrderToAsk(limitOrder: LimitOrder): Ask {
        return Ask(
            currencyPair = limitOrder.pair,
            orderCount = 1,  // Assuming each LimitOrder counts as one ask
            price = limitOrder.price,
            quantity = limitOrder.quantity,
            side = limitOrder.side
        )
    }

    private fun limitOrderToBid(limitOrder: LimitOrder): Bid {
        return Bid(
            currencyPair = limitOrder.pair,
            orderCount = 1,  // Assuming each LimitOrder counts as one bid
            price = limitOrder.price,
            quantity = limitOrder.quantity,
            side = limitOrder.side
        )
    }


    suspend fun addLimitOrder(limitOrder: LimitOrder): String {
        val orderId = UUID.randomUUID().toString()
        mutex.withLock {
            when (limitOrder.side.uppercase()) {
                "BUY" -> processOrder(limitOrder, orderId, bids)
                "SELL" -> processOrder(limitOrder, orderId, asks)
                else -> {
                    logger.error("Invalid order side")
                }
            }
            matchOrders()
        }
        return orderId
    }

    private fun processOrder(limitOrder: LimitOrder, orderId: String, queue: PriorityQueue<LimitOrder>) {
        queue.offer(limitOrder)
        addToOrders(limitOrder, orderId, OrderStatus.PLACED.toString())
        addToOpenOrders(limitOrder, orderId, limitOrder.quantity, OrderStatus.PLACED.toString())
    }


    private fun matchOrders() {
        while (bids.isNotEmpty() && asks.isNotEmpty()) {
            val bid = bids.peek()  // Get the highest bid
            val ask = asks.peek()  // Get the lowest ask

            // Early exit if there are no matching orders
            if (bid.price.toDouble() < ask.price.toDouble()) break

            // Determine the trade quantity (the minimum of the bid and ask quantities)
            val tradeQuantity = minOf(bid.quantity.toDouble(), ask.quantity.toDouble())

            // Handle cases where trade quantity is invalid
            if (tradeQuantity <= 0.0) {
                logger.info("Trade quantity is less than or equal to 0")
                break
            }

            // Create a new trade record
            val trade = Trade(
                price = ask.price,
                quantity = tradeQuantity.toString(),
                takerSide = if (bid.side == "BUY") "SELL" else "BUY",
                tradedAt = Instant.now().toString(),
                currencyPair = bid.pair,
                id = UUID.randomUUID().toString(),
                quoteVolume = (tradeQuantity * ask.price.toDouble()).toString(),
                sequenceId = sequenceCounter.incrementAndGet()
            )

            trades.add(trade)
            logger.info("Trade created: $trade")

            // Update the quantities of the bid and ask
            val updatedBidQuantity = bid.quantity.toDouble() - tradeQuantity
            val updatedAskQuantity = ask.quantity.toDouble() - tradeQuantity

            // Remove fully matched bid and ask from the queues
            bids.poll()
            asks.poll()

            // If bid is partially filled, reinsert it with the updated quantity
            if (updatedBidQuantity == 0.0) {
                updateOrderStatus(bid.customerOrderId, OrderStatus.FILLED.toString())
            } else {
                updateOrderStatus(bid.customerOrderId, OrderStatus.PARTIALLY_FILLED.toString())
                val updatedBid = bid.copy(quantity = updatedBidQuantity.toString())
                bids.offer(updatedBid)  // Reinsert the partially filled bid
            }

            // If ask is partially filled, reinsert it with the updated quantity
            if (updatedAskQuantity == 0.0) {
                updateOrderStatus(ask.customerOrderId, OrderStatus.FILLED.toString())
            } else {
                updateOrderStatus(ask.customerOrderId, OrderStatus.PARTIALLY_FILLED.toString())
                val updatedAsk = ask.copy(quantity = updatedAskQuantity.toString())
                asks.offer(updatedAsk)  // Reinsert the partially filled ask
            }
        }
    }

    private fun updateOrderStatus(customerOrderId: String, status: String) {
        val order = orders.find { it.customerOrderId == customerOrderId }
        if (order != null) {
            order.orderStatusType = status
            order.orderUpdatedAt = Date().toString()
            logger.info("Order status updated: customerOrderId=$customerOrderId, newStatus=$status")
            if (status == OrderStatus.FILLED.toString()) {
                removeFromOpenOrders(order.orderId)
            }
        }
    }

    //add order to open orders
    private fun addToOpenOrders(limitOrder: LimitOrder, orderId: String, remainingQuantity: String, status: String) {
        val openOrder = OpenOrder(
            allowMargin = false,
            createdAt = Instant.now().toString(),
            currencyPair = limitOrder.pair,
            filledPercentage = (100 - (remainingQuantity.toDouble() / limitOrder.quantity.toDouble())*100).toString(),
            orderId = orderId,
            originalQuantity = limitOrder.quantity,
            price = limitOrder.price,
            remainingQuantity = remainingQuantity,
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
            orderCreatedAt = Instant.now().toString(),
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