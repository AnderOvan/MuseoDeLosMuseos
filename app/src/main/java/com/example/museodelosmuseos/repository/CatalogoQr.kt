package com.example.museodelosmuseos.repository

import com.example.museodelosmuseos.util.QrLeido
import com.example.museodelosmuseos.util.TipoContenidoQr

// Ficha que se muestra al escanear un QR (museo, producto o publicación)
data class FichaQr(
    val tipo: TipoContenidoQr,
    val codigo: String,
    val titulo: String,
    val etiqueta: String,      // "Museo", "Libro", "Merch", "Curso" o "Publicación"
    val fuente: String,        // Quién la publica o dónde está
    val descripcion: String,
    val precioUsd: Double? = null
)

// Catálogo local de productos y publicaciones (Control 2: datos simulados, sin backend).
// Los museos se toman de FakeMuseoRepository.
object CatalogoQr {

    // Los productos salen del catálogo de la tienda: así el QR y el catálogo muestran lo mismo
    val PRODUCTOS: List<FichaQr> = FakeProductoRepository.PRODUCTOS.map {
        FichaQr(TipoContenidoQr.PRODUCTO, it.id, it.titulo, it.categoria,
            "Tienda Museo de los Museos", it.descripcion, it.precio)
    }

    val PUBLICACIONES = listOf(
        FichaQr(TipoContenidoQr.PUBLICACION, "001", "El Arte Colonial en Iberoamérica", "Publicación",
            "Museo de los Museos", "Libro digital sobre la historia del arte colonial en la región iberoamericana."),
        FichaQr(TipoContenidoQr.PUBLICACION, "002", "Patrimonio precolombino de los Andes", "Publicación",
            "Museo de los Museos", "Revista digital con investigaciones sobre las culturas andinas."),
        FichaQr(TipoContenidoQr.PUBLICACION, "003", "Museos y memoria", "Publicación",
            "Museo de los Museos", "Ensayos sobre el rol de los museos en la memoria de los pueblos.")
    )

    // Busca la ficha del QR leído. Devuelve null si el código no existe en el catálogo.
    fun buscar(qr: QrLeido): FichaQr? = when (qr.tipo) {
        TipoContenidoQr.MUSEO -> FakeMuseoRepository.buscarPorId(qr.codigo)?.let {
            FichaQr(TipoContenidoQr.MUSEO, it.id, it.nombre, "Museo", "${it.ciudad}, ${it.pais}", it.descripcion)
        }
        TipoContenidoQr.PRODUCTO -> PRODUCTOS.find { it.codigo == qr.codigo }
        TipoContenidoQr.PUBLICACION -> PUBLICACIONES.find { it.codigo == qr.codigo }
        TipoContenidoQr.DESCONOCIDO -> null
    }
}