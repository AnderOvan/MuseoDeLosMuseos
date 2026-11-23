package com.example.museodelosmuseos

import com.example.museodelosmuseos.util.TipoContenidoQr
import com.example.museodelosmuseos.util.ValidadorQr
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidadorQrTest {

    @Test
    fun clasificaProducto() =
        assertEquals(TipoContenidoQr.PRODUCTO, ValidadorQr.clasificarQr("PROD_L001"))

    @Test
    fun clasificaMuseo() =
        assertEquals(TipoContenidoQr.MUSEO, ValidadorQr.clasificarQr("MUSEO_M001"))

    @Test
    fun clasificaPublicacion() =
        assertEquals(TipoContenidoQr.PUBLICACION, ValidadorQr.clasificarQr("PUB_001"))

    @Test
    fun aceptaMinusculasYEspacios() =
        assertEquals(TipoContenidoQr.PRODUCTO, ValidadorQr.clasificarQr("  prod_l001 "))

    @Test
    fun prefijoSinCodigoEsDesconocido() =
        assertEquals(TipoContenidoQr.DESCONOCIDO, ValidadorQr.clasificarQr("PROD_"))

    @Test
    fun textoCualquieraEsDesconocido() =
        assertEquals(TipoContenidoQr.DESCONOCIDO, ValidadorQr.clasificarQr("https://google.com"))

    @Test
    fun extraeElCodigoSinPrefijo() =
        assertEquals("L001", ValidadorQr.analizar("PROD_L001").codigo)
}