package com.example.museodelosmuseos

import com.example.museodelosmuseos.model.Rol
import com.example.museodelosmuseos.network.ConfigDto
import com.example.museodelosmuseos.network.MuseoDto
import com.example.museodelosmuseos.network.ObraDto
import com.example.museodelosmuseos.network.PedidoDto
import com.example.museodelosmuseos.network.ProductoDto
import com.example.museodelosmuseos.network.RespuestaObrasDto
import com.example.museodelosmuseos.repository.local.UsuarioEntity
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

// Datos falsos que usan las pruebas (lo que "respondería" el servidor)
object DatosDePrueba {

    fun museo(id: String, pais: String = "Chile", lat: Double = -33.44, lon: Double = -70.65) = MuseoDto(
        id = id, nombre = "Museo $id", ciudad = "Santiago", pais = pais,
        latitud = lat, longitud = lon, descripcion = "Descripción", categoria = "Arte",
        proyecto = "Arte Latinoamericano", urlVisitaVirtual = "https://museo.cl",
        valoracion = 4.5, resenas = 100
    )

    fun producto(id: String, activo: Boolean = true, stock: Int = 10) = ProductoDto(
        id = id, titulo = "Producto $id", descripcion = "Descripción", precio = 35.0,
        imagenUrl = null, categoria = "Libro", stock = stock, activo = activo
    )

    fun pedido(id: Long, usuarioId: Long = 5, estado: String = "PENDIENTE") = PedidoDto(
        id = id, usuarioId = usuarioId, clienteNombre = "Cliente", numero = "PED-$id",
        resumen = "1x Libro", total = 39.0, destino = "Chile", tipoEnvio = "Estándar",
        estado = estado, fecha = 1_000L * id
    )

    fun respuestaObras(vararg obras: ObraDto) =
        RespuestaObrasDto(obras = obras.toList(), config = ConfigDto("https://www.artic.edu/iiif/2"))

    fun usuario(id: Long, rol: Rol) = UsuarioEntity(
        id = id, nombre = "Usuario $id", rut = "11111111-1", correo = "u$id@museo.cl",
        claveHash = "hash", despacho = "Santiago", rol = rol
    )

    // Error HTTP como el que lanza Retrofit cuando el servidor responde 4xx/5xx
    fun errorHttp(codigo: Int) = HttpException(Response.error<Any>(codigo, "".toResponseBody(null)))

    // Respuesta de un DELETE
    fun respuestaVacia(codigo: Int = 204): Response<Unit> =
        if (codigo in 200..299) Response.success(codigo, Unit)
        else Response.error(codigo, "".toResponseBody(null))
}