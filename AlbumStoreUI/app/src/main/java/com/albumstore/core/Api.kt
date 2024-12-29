package com.albumstore.core

import com.albumstore.core.data.remote.Api
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Api {
    private val url = "192.168.0.110:5275"
    private val httpUrl = "http://$url/"
    val wsUrl = "ws://${url}/hubs/albumstore"
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Logs headers, body, and metadata
    }
    private var gson = GsonBuilder().create()

    val retrofit = Retrofit.Builder()
        .baseUrl(httpUrl)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val okHttpClient =  OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
}