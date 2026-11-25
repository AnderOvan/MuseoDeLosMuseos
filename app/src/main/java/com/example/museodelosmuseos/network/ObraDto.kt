package com.example.museodelosmuseos.network

import com.google.gson.annotations.SerializedName

// ---------- DTO de la API externa (Art Institute of Chicago) ----------
// Un DTO (Data Transfer Object) es la forma exacta del JSON que llega por la red.
// @SerializedName une el nombre del campo en el JSON con el nombre en Kotlin.

// Respuesta completa: { "data": [ ... ], "config": { "iiif_url": "..." } }
data class RespuestaObrasDto(
    @SerializedName("data") val obras: List<ObraDto> = emptyList(),
    @SerializedName("config") val config: ConfigDto? = null
)

// Una obra dentro de "data"
data class ObraDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val titulo: String?,
    @SerializedName("artist_display") val artista: String?,
    @SerializedName("date_display") val fecha: String?,
    @SerializedName("image_id") val imagenId: String?
)

// Dirección base para armar la URL de la imagen
data class ConfigDto(
    @SerializedName("iiif_url") val iiifUrl: String?
)