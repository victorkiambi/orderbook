package com.orderbook.di

import java.net.http.HttpClient

fun provideHttpClient(): HttpClient {
    return HttpClient.newBuilder().build()
}