package com.example.museodelosmuseos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Interfaces de Retrofit para los microservicios propios.
// El backend es un proyecto Spring Boot con un controlador por recurso;
// cada controlador tiene su propia interfaz aquí.

// ---------- Museos ----------
interface MuseoApiService {

    @GET("api/museos")
    suspend fun listar(): List<MuseoDto>

    @GET("api/museos/{id}")
    suspend fun obtener(@Path("id") id: String): MuseoDto

    @POST("api/museos")
    suspend fun crear(@Body museo: MuseoDto): MuseoDto

    @PUT("api/museos/{id}")
    suspend fun actualizar(@Path("id") id: String, @Body museo: MuseoDto): MuseoDto

    // Response<Unit>: el DELETE no devuelve cuerpo, solo el código HTTP (204)
    @DELETE("api/museos/{id}")
    suspend fun eliminar(@Path("id") id: String): Response<Unit>
}

// ---------- Productos y cursos ----------
interface ProductoApiService {

    @GET("api/productos")
    suspend fun listar(): List<ProductoDto>

    // También sirve para validar el stock antes de agregar al carrito
    @GET("api/productos/{id}")
    suspend fun obtener(@Path("id") id: String): ProductoDto

    @POST("api/productos")
    suspend fun crear(@Body producto: ProductoDto): ProductoDto

    @PUT("api/productos/{id}")
    suspend fun actualizar(@Path("id") id: String, @Body producto: ProductoDto): ProductoDto

    @DELETE("api/productos/{id}")
    suspend fun eliminar(@Path("id") id: String): Response<Unit>
}

// ---------- Pedidos / solicitudes de compra ----------
interface PedidoApiService {

    // Sin usuarioId: todos los pedidos (Administrador). Con usuarioId: solo los del cliente.
    @GET("api/pedidos")
    suspend fun listar(@Query("usuarioId") usuarioId: Long? = null): List<PedidoDto>

    @POST("api/pedidos")
    suspend fun crear(@Body pedido: PedidoDto): PedidoDto

    // Solo cambia el estado: { "estado": "ENVIADO" }
    @PUT("api/pedidos/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: Long, @Body cambio: CambioEstadoDto): PedidoDto

    @DELETE("api/pedidos/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>
}