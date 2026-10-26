package com.example.museodelosmuseos.model

// Un museo registrado en la plataforma
data class Museo(
    val id: String,
    val nombre: String,
    val ciudad: String,
    val pais: String,
    // Ubicación física (para el mapa)
    val latitud: Double,
    val longitud: Double,
    val descripcion: String,
    val categoria: String,        // Arte, Historia o Ciencia
    val proyecto: String,         // Colección temática a la que pertenece
    // Enlace a la visita virtual (distinto de la ubicación física)
    val urlVisitaVirtual: String,
    val valoracion: Double,
    val resenas: Int
)