package com.orderbook.models

import kotlinx.serialization.Serializable

@Serializable
data class OrderHistory(
    val averagePrice: String,
    val currencyPair: String,
    val failedReason: String,
    val feeCurrency: String,
    val orderCreatedAt: String,
    val orderId: String,
    val orderSide: String,
    val orderStatusType: String,
    val orderType: String,
    val orderUpdatedAt: String,
    val originalPrice: String,
    val originalQuantity: String,
    val remainingQuantity: String,
    val timeInForce: String,
    val total: String,
    val totalFee: String
)