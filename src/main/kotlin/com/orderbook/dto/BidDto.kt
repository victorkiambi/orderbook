package com.orderbook.dto
import kotlinx.serialization.Serializable

@Serializable
data class BidDto(
    val price: String,
    val quantity: String
)
