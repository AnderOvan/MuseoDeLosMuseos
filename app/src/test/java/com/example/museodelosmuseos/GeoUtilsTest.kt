package com.example.museodelosmuseos

import com.example.museodelosmuseos.util.GeoUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class GeoUtilsTest {

    // Coordenadas de referencia
    private val santiago = -33.4489 to -70.6693
    private val madrid = 40.4168 to -3.7038
    private val ciudadDeMexico = 19.4326 to -99.1332
    private val buenosAires = -34.6037 to -58.3816

    @Test
    fun mismoPunto_distanciaCero() {
        val d = GeoUtils.calcularDistanciaKm(santiago.first, santiago.second, santiago.first, santiago.second)
        assertEquals(0.0, d, 0.001)
    }

    @Test
    fun santiagoMadrid_aproximadamente10700km() {
        val d = GeoUtils.calcularDistanciaKm(santiago.first, santiago.second, madrid.first, madrid.second)
        assertEquals(10_700.0, d, 100.0) // margen de ±100 km
    }

    @Test
    fun santiagoCiudadDeMexico_aproximadamente6600km() {
        val d = GeoUtils.calcularDistanciaKm(santiago.first, santiago.second, ciudadDeMexico.first, ciudadDeMexico.second)
        assertEquals(6_600.0, d, 100.0)
    }

    @Test
    fun santiagoBuenosAires_aproximadamente1140km() {
        val d = GeoUtils.calcularDistanciaKm(santiago.first, santiago.second, buenosAires.first, buenosAires.second)
        assertEquals(1_140.0, d, 30.0)
    }

    @Test
    fun laDistanciaEsLaMismaEnAmbosSentidos() {
        val ida = GeoUtils.calcularDistanciaKm(santiago.first, santiago.second, madrid.first, madrid.second)
        val vuelta = GeoUtils.calcularDistanciaKm(madrid.first, madrid.second, santiago.first, santiago.second)
        assertEquals(ida, vuelta, 0.001)
    }

    @Test
    fun formatea_kilometrosConPuntoDeMiles() =
        assertEquals("1.140 km", GeoUtils.formatearDistancia(1140.4))

    @Test
    fun formatea_menosDeUnKmEnMetros() =
        assertEquals("500 m", GeoUtils.formatearDistancia(0.5))
    @Test
    fun formatea_distanciasCortasConUnDecimal() =
        assertEquals("2,3 km", GeoUtils.formatearDistancia(2.34))
}