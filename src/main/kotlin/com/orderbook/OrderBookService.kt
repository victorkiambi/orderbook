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
            SequenceNumber = sequenceCounter.incrementAndGet()
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
                    throw IllegalArgumentException("Invalid order side: ${limitOrder.side}")
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
            val bid = bids.poll()  // Get and remove the highest bid
            val ask = asks.poll()  // Get and remove the lowest ask

            val bidPrice = bid.price.toDouble()
            val askPrice = ask.price.toDouble()
            val bidQuantity = bid.quantity.toDouble()
            val askQuantity = ask.quantity.toDouble()

            // Exit early if no match is possible
            if (bidPrice < askPrice) {
                // Reinsert the bid and ask since they were polled but not matched
                bids.offer(bid)
                asks.offer(ask)
                break
            }

            // Determine the trade quantity (the minimum of the bid and ask quantities)
            val tradeQuantity = minOf(bidQuantity, askQuantity)

            // Exit if trade quantity is invalid (this should rarely happen)
            if (tradeQuantity <= 0.0) {
                logger.info("Invalid trade quantity (<= 0) for bid: $bid and ask: $ask")
                break
            }

            // Create a new trade record
            val trade = Trade(
                price = ask.price,
                quantity = "%.8f".format(tradeQuantity),  // Limit the precision
                takerSide = if (bid.side.uppercase() == "BUY") "SELL" else "BUY",
                tradedAt = Instant.now().toString(),
                currencyPair = bid.pair,
                id = UUID.randomUUID().toString(),
                quoteVolume = "%.8f".format(tradeQuantity * askPrice),  // Volume is based on the ask price
                sequenceId = sequenceCounter.incrementAndGet()
            )
            trades.add(trade)
            logger.info("Trade created: $trade")

            // Update remaining quantities
            val remainingBidQuantity = bidQuantity - tradeQuantity
            val remainingAskQuantity = askQuantity - tradeQuantity

            // If the bid is partially filled, reinsert it with the updated quantity
            if (remainingBidQuantity > 0) {
                val updatedBid = bid.copy(quantity = "%.8f".format(remainingBidQuantity))
                bids.offer(updatedBid)  // Reinsert the partially filled bid
            } else {
                updateOrderStatus(bid.customerOrderId, OrderStatus.FILLED.toString())
            }

            // If the ask is partially filled, reinsert it with the updated quantity
            if (remainingAskQuantity > 0) {
                val updatedAsk = ask.copy(quantity = "%.8f".format(remainingAskQuantity))
                asks.offer(updatedAsk)  // Reinsert the partially filled ask
            } else {
                updateOrderStatus(ask.customerOrderId, OrderStatus.FILLED.toString())
            }
        }
    }


    private fun updateOrderStatus(customerOrderId: String, status: String) {
        val order = orders.find { it.customerOrderId == customerOrderId }
        if (order != null) {
            order.orderStatusType = status
            order.orderUpdatedAt = Instant.now().toString()
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