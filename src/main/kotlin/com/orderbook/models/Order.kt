package com.orderbook.models

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val currencyPair: String,
    val customerOrderId: String,
    val failedReason: String,
    val orderCreatedAt: String,
    val orderId: String,
    val orderSide: String,
    var orderStatusType: String,
    val orderType: String,
    var orderUpdatedAt: String,
    val originalPrice: String,
    val originalQuantity: String,
    val remainingQuantity: String,
    val timeInForce: String
)