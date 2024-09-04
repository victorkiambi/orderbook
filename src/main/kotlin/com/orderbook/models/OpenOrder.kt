package com.orderbook.models

import kotlinx.serialization.Serializable

@Serializable
data class OpenOrder(
    val allowMargin: Boolean,
    val createdAt: String,
    val currencyPair: String,
    val filledPercentage: String,
    val orderId: String,
    val originalQuantity: String,
    val price: String,
    val remainingQuantity: String,
    val side: String,
    val status: String,
    val timeInForce: String,
    val type: String,
    val updatedAt: String
)