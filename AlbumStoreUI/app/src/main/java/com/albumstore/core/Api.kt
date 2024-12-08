package com.albumstore.core

import com.albumstore.core.data.remote.Api
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Api {
    private val url = "192.168.0.239:5275"
    private val httpUrl = "http://$url/"
    val wsUrl = "ws://${url}/hubs/albumstore"

    private var gson = GsonBuilder().create()

    val retrofit = Retrofit.Builder()
        .baseUrl(httpUrl)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val okHttpClient = OkHttpClient.Builder()
        .build()
}