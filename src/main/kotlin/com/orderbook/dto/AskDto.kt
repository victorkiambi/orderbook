package com.orderbook.dto

import kotlinx.serialization.Serializable

@Serializable
data class AskDto (
    val price: String,
    val quantity: String
)

