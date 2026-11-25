package com.example.museodelosmuseos.repository

import com.example.museodelosmuseos.model.EstadoPedido
import com.example.museodelosmuseos.network.CambioEstadoDto
import com.example.museodelosmuseos.network.PedidoApiService
import com.example.museodelosmuseos.network.RetrofitInstance
import com.example.museodelosmuseos.network.aDto
import com.example.museodelosmuseos.network.aEntity
import com.example.museodelosmuseos.repository.local.PedidoEntity

// Pedidos (solicitudes de compra) en el backend Spring Boot.
// "El backend centraliza el pedido y su estado": aquí está la información oficial.
class PedidoRepositoryRemoto(
    private val api: PedidoApiService = RetrofitInstance.pedidoApi
) {

    // GET /api/pedidos  o  GET /api/pedidos?usuarioId=5
    // null = todos (Administrador); con id = solo los del cliente
    suspend fun listar(usuarioId: Long?): Result<List<PedidoEntity>> =
        llamadaSegura { api.listar(usuarioId).map { it.aEntity() } }

    // POST /api/pedidos -> devuelve el pedido con el id que asignó el backend
    suspend fun crear(pedido: PedidoEntity): Result<PedidoEntity> =
        llamadaSegura { api.crear(pedido.aDto()).aEntity() }

    // PUT /api/pedidos/{id}/estado  { "estado": "ENVIADO" }
    suspend fun cambiarEstado(id: Long, nuevo: EstadoPedido): Result<PedidoEntity> =
        llamadaSegura { api.cambiarEstado(id, CambioEstadoDto(nuevo.name)).aEntity() }

    // DELETE /api/pedidos/{id}
    suspend fun eliminar(id: Long): Result<Unit> =
        llamadaSegura { api.eliminar(id).exigirExito() }
}