package com.example.museodelosmuseos.util

// Las 3 categorías de negocio que puede tener un QR, más "desconocido"
enum class TipoContenidoQr(val prefijo: String?, val titulo: String) {
    MUSEO("MUSEO_", "Museo / Exposición virtual"),
    PRODUCTO("PROD_", "Producto del catálogo"),
    PUBLICACION("PUB_", "Publicación / Libro digital"),
    DESCONOCIDO(null, "Código no reconocido")
}

// Resultado de analizar un QR: el texto completo, su tipo y el código sin prefijo
// Ejemplo: "PROD_L001" -> tipo PRODUCTO, codigo "L001"
data class QrLeido(
    val contenido: String,
    val tipo: TipoContenidoQr,
    val codigo: String
)

// Lógica pura (sin Android): por eso se puede probar con JUnit
object ValidadorQr {

    // Después del prefijo solo se aceptan letras, números o guiones
    private val CODIGO_VALIDO = Regex("^[A-Z0-9-]+$")

    fun clasificarQr(contenido: String): TipoContenidoQr {
        val limpio = contenido.trim().uppercase()
        return TipoContenidoQr.entries.firstOrNull { tipo ->
            val prefijo = tipo.prefijo ?: return@firstOrNull false
            limpio.startsWith(prefijo) && CODIGO_VALIDO.matches(limpio.removePrefix(prefijo))
        } ?: TipoContenidoQr.DESCONOCIDO
    }

    fun analizar(contenido: String): QrLeido {
        val limpio = contenido.trim().uppercase()
        val tipo = clasificarQr(limpio)
        val codigo = tipo.prefijo?.let { limpio.removePrefix(it) } ?: limpio
        return QrLeido(limpio, tipo, codigo)
    }
}