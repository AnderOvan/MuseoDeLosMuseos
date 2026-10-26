package com.example.museodelosmuseos.util

// Reglas ficticias de despacho (no se conecta a empresas de transporte reales).
// Los montos están en dólares (US$), igual que los precios del catálogo.
object CotizadorEnvios {

    const val PAIS_NACIONAL = "Chile"

    val PAISES = listOf("Chile", "Argentina", "Brasil", "Colombia", "España", "México", "Perú")

    const val NACIONAL_ESTANDAR = 4.0
    const val NACIONAL_EXPRESS = 8.0
    const val ENVIO_GRATIS_DESDE = 50.0   // Chile: estándar gratis desde este subtotal
    const val INTERNACIONAL_ESTANDAR = 12.0
    const val INTERNACIONAL_EXPRESS = 25.0

    fun esNacional(pais: String): Boolean = pais == PAIS_NACIONAL

    // Costo del despacho según destino, modalidad y monto de la compra
    fun calcularCostoEnvio(
        pais: String,
        esExpress: Boolean,
        subtotal: Double,
        tieneProductosFisicos: Boolean = true // cursos y productos digitales no pagan despacho
    ): Double = when {
        !tieneProductosFisicos -> 0.0
        esNacional(pais) && !esExpress && subtotal >= ENVIO_GRATIS_DESDE -> 0.0
        esNacional(pais) -> if (esExpress) NACIONAL_EXPRESS else NACIONAL_ESTANDAR
        else -> if (esExpress) INTERNACIONAL_EXPRESS else INTERNACIONAL_ESTANDAR
    }

    fun plazoEntrega(pais: String, esExpress: Boolean): String = when {
        esNacional(pais) && esExpress -> "1-2 días hábiles"
        esNacional(pais) -> "3-5 días hábiles"
        esExpress -> "3-5 días hábiles"
        else -> "7-14 días hábiles"
    }
}