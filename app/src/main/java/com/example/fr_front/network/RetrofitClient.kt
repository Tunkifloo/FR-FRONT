package com.example.fr_front.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Cambia esta URL por la de tu servidor - CORREGIDA
    private const val BASE_URL = "https://fr-api-deploy-production.up.railway.app/" // Agregado protocolo https y trailing slash
    // private const val BASE_URL = "http://10.0.2.2:8000/" // Para emulador Android con servidor local
    // private const val BASE_URL = "http://192.168.1.100:8000/" // Para dispositivo físico con servidor local
    // private const val BASE_URL = "https://tu-servidor.com/" // Para producción

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}