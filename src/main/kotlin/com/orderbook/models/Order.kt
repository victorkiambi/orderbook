package com.orderbook.models

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val allowMargin: String,
    val customerOrderId: String,
    val pair: String,
    val postOnly: Boolean,
    var price: String,
    var quantity: String,
    val side: String,
    val timeInForce: String
)