package com.example.museodelosmuseos.repository

import com.example.museodelosmuseos.network.ProductoApiService
import com.example.museodelosmuseos.network.RetrofitInstance
import com.example.museodelosmuseos.network.aDto
import com.example.museodelosmuseos.network.aEntity
import com.example.museodelosmuseos.repository.local.ProductoCacheEntity

// Catálogo desde el backend Spring Boot, con caché en Room (tabla productos_cache).
// Flujo: servidor -> Retrofit -> Room -> la pantalla lee Room (funciona aunque se caiga la red).
class ProductoRepositoryRemoto(
    private val local: LocalRepository,
    private val api: ProductoApiService = RetrofitInstance.productoApi
) {

    // GET /api/productos -> guarda solo los activos en la caché. Devuelve cuántos llegaron.
    suspend fun sincronizar(): Result<Int> = llamadaSegura { descargarCatalogo() }

    // GET /api/productos/{id} -> stock real antes de agregar al carrito
    suspend fun consultarStock(id: String): Result<Int> =
        llamadaSegura { api.obtener(id).stock }

    // ---------- CRUD del Administrador ----------
    // Después de cada cambio se vuelve a descargar el catálogo para que la caché quede igual al servidor.

    // POST /api/productos
    suspend fun crear(producto: ProductoCacheEntity): Result<Unit> = llamadaSegura {
        api.crear(producto.aDto())
        descargarCatalogo()
    }

    // PUT /api/productos/{id}
    suspend fun actualizar(producto: ProductoCacheEntity): Result<Unit> = llamadaSegura {
        api.actualizar(producto.id, producto.aDto())
        descargarCatalogo()
    }

    // PUT con activo = false: deja de mostrarse en el catálogo, pero sigue en la base de datos
    suspend fun desactivar(producto: ProductoCacheEntity): Result<Unit> = llamadaSegura {
        api.actualizar(producto.id, producto.aDto(activo = false))
        descargarCatalogo()
    }

    // DELETE /api/productos/{id}
    suspend fun eliminar(id: String): Result<Unit> = llamadaSegura {
        api.eliminar(id).exigirExito()
        descargarCatalogo()
    }

    private suspend fun descargarCatalogo(): Int {
        val activos = api.listar().filter { it.activo }
        local.reemplazarCatalogo(activos.map { it.aEntity() })
        return activos.size
    }
}