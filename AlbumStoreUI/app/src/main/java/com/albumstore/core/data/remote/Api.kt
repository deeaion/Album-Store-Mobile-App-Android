package com.albumstore.core.data.remote;

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Api {
    private val url = "192.168.0.239:5275"
    private val httpUrl="http://$url/"
    val wsUrl = "ws://$url/hubs/albumstore"
    
    //gson
    private var gson=GsonBuilder().create()
    //interceptor for token
    val tokenInterceptor=TokenInterceptor()
    // client
    val okHttpClient: OkHttpClient =OkHttpClient.Builder().apply { 
        this.addInterceptor(tokenInterceptor)   
    }.build()

    val retrofit = Retrofit.Builder()
        .baseUrl(httpUrl)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}
