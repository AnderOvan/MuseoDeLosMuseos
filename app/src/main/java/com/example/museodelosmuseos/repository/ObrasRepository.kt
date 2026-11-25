package com.example.museodelosmuseos.repository

import com.example.museodelosmuseos.model.Obra
import com.example.museodelosmuseos.network.ArteApiService
import com.example.museodelosmuseos.network.ObraDto
import com.example.museodelosmuseos.network.RetrofitInstance

// Dirección de imágenes por si la respuesta no la trae
private const val IIIF_POR_DEFECTO = "https://www.artic.edu/iiif/2"

// Repositorio de la API externa: pide las obras y las convierte al modelo de la app.
// Recibe el ApiService por parámetro para que en las pruebas se pueda reemplazar por un mock.
// No guarda nada en Room: así no interfiere con la base de datos local.
class ObrasRepository(
    private val api: ArteApiService = RetrofitInstance.arteApi
) {

    // Sin texto: lista general. Con texto: búsqueda.
    // Si no hay internet o el servidor falla, lanza la excepción y el ViewModel la maneja.
    suspend fun obtenerObras(busqueda: String = ""): List<Obra> {
        val respuesta = if (busqueda.isBlank()) {
            api.listarObras()
        } else {
            api.buscarObras(busqueda.trim())
        }
        val baseImagenes = respuesta.config?.iiifUrl ?: IIIF_POR_DEFECTO
        return respuesta.obras.map { it.aObra(baseImagenes) }
    }
}

// Convierte el DTO (JSON) en el modelo de la app, con textos por defecto si vienen vacíos
fun ObraDto.aObra(baseImagenes: String): Obra = Obra(
    id = id,
    titulo = titulo?.takeIf { it.isNotBlank() } ?: "Sin título",
    artista = artista?.takeIf { it.isNotBlank() } ?: "Artista desconocido",
    fecha = fecha?.takeIf { it.isNotBlank() } ?: "Fecha desconocida",
    // Formato IIIF que recomienda la API: {base}/{image_id}/full/843,/0/default.jpg
    imagenUrl = imagenId?.let { "$baseImagenes/$it/full/843,/0/default.jpg" }
)