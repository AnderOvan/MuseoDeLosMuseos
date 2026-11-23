package com.example.museodelosmuseos.network

import retrofit2.http.GET
import retrofit2.http.Query

// Campos que pedimos a la API: así la respuesta es liviana
private const val CAMPOS_OBRA = "id,title,artist_display,date_display,image_id"

// Interfaz de Retrofit para la API externa del Art Institute of Chicago.
// Retrofit genera el código que hace la petición HTTP; "suspend" permite llamarla desde una corrutina.
interface ArteApiService {

    // GET https://api.artic.edu/api/v1/artworks?fields=...&limit=20
    @GET("artworks")
    suspend fun listarObras(
        @Query("fields") campos: String = CAMPOS_OBRA,
        @Query("limit") limite: Int = 20
    ): RespuestaObrasDto

    // GET https://api.artic.edu/api/v1/artworks/search?q=texto&fields=...&limit=20
    @GET("artworks/search")
    suspend fun buscarObras(
        @Query("q") busqueda: String,
        @Query("fields") campos: String = CAMPOS_OBRA,
        @Query("limit") limite: Int = 20
    ): RespuestaObrasDto
}