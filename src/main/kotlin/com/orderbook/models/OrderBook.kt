package com.orderbook.models
import kotlinx.serialization.Serializable

@Serializable
data class OrderBook(
    val Asks: MutableList<Order>,
    val Bids: MutableList<Order>,
    val LastChange: String,
    val SequenceNumber: Long
)
