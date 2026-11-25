package com.example.museodelosmuseos.repository

import com.example.museodelosmuseos.model.Museo
import com.example.museodelosmuseos.network.MuseoApiService
import com.example.museodelosmuseos.network.RetrofitInstance
import com.example.museodelosmuseos.network.aDto
import com.example.museodelosmuseos.network.aMuseo

// Implementación REAL de MuseoRepository: llama al backend Spring Boot con Retrofit.
// Reemplaza a FakeMuseoRepository sin cambiar los ViewModels, porque ambos
// cumplen la misma interfaz (MuseoRepository).
class MuseoRepositoryRemoto(
    private val api: MuseoApiService = RetrofitInstance.museoApi
) : MuseoRepository {

    // GET /api/museos
    override suspend fun obtenerMuseos(): Result<List<Museo>> =
        llamadaSegura { api.listar().map { it.aMuseo() } }

    // GET /api/museos/{id}
    suspend fun obtenerMuseo(id: String): Result<Museo> =
        llamadaSegura { api.obtener(id).aMuseo() }

    // POST /api/museos (Investigador registra una ficha)
    suspend fun crearMuseo(museo: Museo): Result<Museo> =
        llamadaSegura { api.crear(museo.aDto()).aMuseo() }

    // PUT /api/museos/{id} (Investigador corrige una ficha)
    suspend fun actualizarMuseo(museo: Museo): Result<Museo> =
        llamadaSegura { api.actualizar(museo.id, museo.aDto()).aMuseo() }

    // DELETE /api/museos/{id}
    suspend fun eliminarMuseo(id: String): Result<Unit> =
        llamadaSegura { api.eliminar(id).exigirExito() }
}