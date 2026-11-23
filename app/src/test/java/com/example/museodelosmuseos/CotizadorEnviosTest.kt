package com.example.museodelosmuseos

import com.example.museodelosmuseos.util.CotizadorEnvios
import org.junit.Assert.assertEquals
import org.junit.Test

class CotizadorEnviosTest {

    @Test
    fun chileEstandar_cuesta4() =
        assertEquals(4.0, CotizadorEnvios.calcularCostoEnvio("Chile", esExpress = false, subtotal = 30.0), 0.001)

    @Test
    fun chileExpress_cuesta8() =
        assertEquals(8.0, CotizadorEnvios.calcularCostoEnvio("Chile", esExpress = true, subtotal = 30.0), 0.001)

    @Test
    fun chileEstandar_esGratisDesde50() =
        assertEquals(0.0, CotizadorEnvios.calcularCostoEnvio("Chile", esExpress = false, subtotal = 50.0), 0.001)

    @Test
    fun chileExpress_seCobraAunqueSupere50() =
        assertEquals(8.0, CotizadorEnvios.calcularCostoEnvio("Chile", esExpress = true, subtotal = 120.0), 0.001)

    @Test
    fun internacionalEstandar_cuesta12() =
        assertEquals(12.0, CotizadorEnvios.calcularCostoEnvio("Perú", esExpress = false, subtotal = 30.0), 0.001)

    @Test
    fun internacionalExpress_cuesta25() =
        assertEquals(25.0, CotizadorEnvios.calcularCostoEnvio("España", esExpress = true, subtotal = 30.0), 0.001)

    @Test
    fun internacional_noTieneEnvioGratis() =
        assertEquals(12.0, CotizadorEnvios.calcularCostoEnvio("México", esExpress = false, subtotal = 200.0), 0.001)

    @Test
    fun soloCursosDigitales_noPaganDespacho() =
        assertEquals(
            0.0,
            CotizadorEnvios.calcularCostoEnvio("Colombia", esExpress = true, subtotal = 49.0, tieneProductosFisicos = false),
            0.001
        )
}