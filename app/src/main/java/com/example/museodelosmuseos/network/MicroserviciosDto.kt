package com.example.museodelosmuseos.network

import com.google.gson.annotations.SerializedName

// ---------- DTO de los microservicios propios (Spring Boot) ----------
// Esta es la forma EXACTA del JSON que envían y reciben los microservicios.
// El backend debe usar estos mismos nombres de campo.

// Museos -> /api/museos
data class MuseoDto(
    @SerializedName("id") val id: String,                    // "MU01" (coincide con los QR)
    @SerializedName("nombre") val nombre: String,
    @SerializedName("ciudad") val ciudad: String,
    @SerializedName("pais") val pais: String,
    @SerializedName("latitud") val latitud: Double,
    @SerializedName("longitud") val longitud: Double,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("categoria") val categoria: String,      // Arte, Historia o Ciencia
    @SerializedName("proyecto") val proyecto: String,
    @SerializedName("urlVisitaVirtual") val urlVisitaVirtual: String,
    @SerializedName("valoracion") val valoracion: Double,
    @SerializedName("resenas") val resenas: Int
)

// Productos y cursos -> /api/productos
data class ProductoDto(
    @SerializedName("id") val id: String,                    // "C001", "L001", "M001"...
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("precio") val precio: Double,
    @SerializedName("imagenUrl") val imagenUrl: String?,
    @SerializedName("categoria") val categoria: String,      // Curso, Libro o Material
    @SerializedName("stock") val stock: Int,                 // 999 = curso digital
    @SerializedName("activo") val activo: Boolean = true     // false = desactivado por el Administrador
)

// Pedidos -> /api/pedidos
data class PedidoDto(
    @SerializedName("id") val id: Long? = null,              // null al crear: lo asigna el backend
    @SerializedName("usuarioId") val usuarioId: Long,
    @SerializedName("clienteNombre") val clienteNombre: String,
    @SerializedName("numero") val numero: String,            // "MDM-04821"
    @SerializedName("resumen") val resumen: String,          // "2 × Libro, 1 × Curso"
    @SerializedName("total") val total: Double,
    @SerializedName("destino") val destino: String,
    @SerializedName("tipoEnvio") val tipoEnvio: String,
    @SerializedName("estado") val estado: String = "PENDIENTE", // PENDIENTE, CONFIRMADO, ENVIADO, ENTREGADO
    @SerializedName("fecha") val fecha: Long = System.currentTimeMillis()
)

// Cuerpo para cambiar solo el estado de un pedido: { "estado": "ENVIADO" }
data class CambioEstadoDto(
    @SerializedName("estado") val estado: String
)