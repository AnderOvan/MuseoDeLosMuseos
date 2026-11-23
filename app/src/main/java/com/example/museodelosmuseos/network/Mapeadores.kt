package com.example.museodelosmuseos.network

import com.example.museodelosmuseos.model.EstadoPedido
import com.example.museodelosmuseos.model.Museo
import com.example.museodelosmuseos.repository.local.PedidoEntity
import com.example.museodelosmuseos.repository.local.ProductoCacheEntity

// Conversión entre los DTO (JSON del backend) y los modelos que ya usa la app.
// Gracias a esto las pantallas no cambian: siguen usando Museo, ProductoCacheEntity y PedidoEntity.

// ---------- Museos ----------
fun MuseoDto.aMuseo() = Museo(
    id = id, nombre = nombre, ciudad = ciudad, pais = pais,
    latitud = latitud, longitud = longitud, descripcion = descripcion,
    categoria = categoria, proyecto = proyecto, urlVisitaVirtual = urlVisitaVirtual,
    valoracion = valoracion, resenas = resenas
)

fun Museo.aDto() = MuseoDto(
    id = id, nombre = nombre, ciudad = ciudad, pais = pais,
    latitud = latitud, longitud = longitud, descripcion = descripcion,
    categoria = categoria, proyecto = proyecto, urlVisitaVirtual = urlVisitaVirtual,
    valoracion = valoracion, resenas = resenas
)

// ---------- Productos (se guardan en la caché de Room) ----------
fun ProductoDto.aEntity() = ProductoCacheEntity(
    id = id, titulo = titulo, descripcion = descripcion, precio = precio,
    imagenUrl = imagenUrl, categoria = categoria, stock = stock
)

fun ProductoCacheEntity.aDto(activo: Boolean = true) = ProductoDto(
    id = id, titulo = titulo, descripcion = descripcion, precio = precio,
    imagenUrl = imagenUrl, categoria = categoria, stock = stock, activo = activo
)

// ---------- Pedidos ----------
fun PedidoDto.aEntity() = PedidoEntity(
    id = id ?: 0L,
    usuarioId = usuarioId,
    clienteNombre = clienteNombre,
    numero = numero,
    resumen = resumen,
    total = total,
    destino = destino,
    tipoEnvio = tipoEnvio,
    // Si el backend manda un estado desconocido, se usa PENDIENTE
    estado = EstadoPedido.entries.find { it.name == estado } ?: EstadoPedido.PENDIENTE,
    fecha = fecha
)

fun PedidoEntity.aDto() = PedidoDto(
    id = if (id == 0L) null else id, // 0 = pedido nuevo: el backend asigna el id
    usuarioId = usuarioId,
    clienteNombre = clienteNombre,
    numero = numero,
    resumen = resumen,
    total = total,
    destino = destino,
    tipoEnvio = tipoEnvio,
    estado = estado.name,
    fecha = fecha
)