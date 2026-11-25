package com.example.museodelosmuseos

import com.example.museodelosmuseos.util.CampoProducto
import com.example.museodelosmuseos.util.ValidacionProducto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Reglas del formulario de productos del Administrador (lógica pura, sin mocks)
class ValidacionProductoTest {

    private fun validar(
        id: String = "L004",
        titulo: String = "Guía de Museos",
        descripcion: String = "Descripción",
        precio: String = "25",
        stock: String = "10",
        esNuevo: Boolean = true,
        existentes: Set<String> = setOf("L001")
    ) = ValidacionProducto.validar(id, titulo, descripcion, precio, stock, esNuevo, existentes)

    @Test
    fun datosCorrectos_noHayErrores() {
        assertTrue(validar().isEmpty())
    }

    @Test
    fun codigoConFormatoIncorrecto_esError() {
        assertTrue(CampoProducto.ID in validar(id = "LIBRO1"))
        assertTrue(CampoProducto.ID in validar(id = ""))
    }

    @Test
    fun codigoRepetido_esErrorSoloAlCrear() {
        assertTrue(CampoProducto.ID in validar(id = "L001", esNuevo = true))
        assertTrue(CampoProducto.ID !in validar(id = "L001", esNuevo = false)) // al editar es el mismo
    }

    @Test
    fun precioCeroOTexto_esError() {
        assertTrue(CampoProducto.PRECIO in validar(precio = "0"))
        assertTrue(CampoProducto.PRECIO in validar(precio = "abc"))
    }

    @Test
    fun stockNegativoODecimal_esError() {
        assertTrue(CampoProducto.STOCK in validar(stock = "-1"))
        assertTrue(CampoProducto.STOCK in validar(stock = "2.5"))
    }

    @Test
    fun tituloCortoYDescripcionVacia_sonErrores() {
        val errores = validar(titulo = "AB", descripcion = " ")
        assertTrue(CampoProducto.TITULO in errores)
        assertTrue(CampoProducto.DESCRIPCION in errores)
    }

    @Test
    fun leerPrecio_aceptaComaYPunto() {
        assertEquals(25.5, ValidacionProducto.leerPrecio("25,5")!!, 0.0)
        assertEquals(25.5, ValidacionProducto.leerPrecio(" 25.5 ")!!, 0.0)
        assertNull(ValidacionProducto.leerPrecio("veinte"))
    }
}