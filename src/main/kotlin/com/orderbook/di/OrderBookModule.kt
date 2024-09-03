package com.orderbook.di

import com.orderbook.OrderBookRepository
import com.orderbook.OrderBookService
import com.orderbook.OrderBookRepositoryImpl

import org.koin.dsl.module

val orderBookModule = module {
    single <OrderBookRepository> { OrderBookRepositoryImpl() }
    single { OrderBookService(get()) }
}