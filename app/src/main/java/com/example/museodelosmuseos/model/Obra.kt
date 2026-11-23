package com.example.museodelosmuseos.model

// Obra de arte tal como la usa la app (ya sin detalles del JSON)
data class Obra(
    val id: Int,
    val titulo: String,
    val artista: String,
    val fecha: String,
    val imagenUrl: String?
)