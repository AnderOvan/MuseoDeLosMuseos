package com.example.museodelosmuseos.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt

// Cálculos geográficos puros (sin Android): se pueden probar con JUnit
object GeoUtils {

    // Punto de origen para medir distancias: Santiago de Chile
    const val ORIGEN_LAT = -33.4489
    const val ORIGEN_LON = -70.6693

    private const val RADIO_TIERRA_KM = 6371.0

    // Formato chileno: punto como separador de miles (10.690 km)
    private val formatoMiles = DecimalFormat(
        "#,##0",
        DecimalFormatSymbols().apply { groupingSeparator = '.' }
    )

    // Un decimal con coma (2,3 km)
    private val formatoDecimal = DecimalFormat(
        "0.0",
        DecimalFormatSymbols().apply { decimalSeparator = ',' }
    )

    // Fórmula de Haversine: distancia en línea recta sobre la superficie de la Tierra
    fun calcularDistanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return RADIO_TIERRA_KM * c
    }

    // Distancia desde el centro de Santiago hasta un punto
    fun distanciaDesdeOrigenKm(lat: Double, lon: Double): Double =
        calcularDistanciaKm(ORIGEN_LAT, ORIGEN_LON, lat, lon)

    // 0.5 -> "500 m" | 2.34 -> "2,3 km" | 1140.4 -> "1.140 km"
    fun formatearDistancia(distanciaKm: Double): String = when {
        distanciaKm < 1.0 -> "${(distanciaKm * 1000).roundToLong()} m"
        distanciaKm < 10.0 -> "${formatoDecimal.format(distanciaKm)} km"
        else -> "${formatoMiles.format(distanciaKm.roundToLong())} km"
    }
}