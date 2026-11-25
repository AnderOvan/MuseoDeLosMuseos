package com.example.museodelosmuseos

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.museodelosmuseos.model.Rol
import com.example.museodelosmuseos.network.ArteApiService
import com.example.museodelosmuseos.network.ConfigDto
import com.example.museodelosmuseos.network.ObraDto
import com.example.museodelosmuseos.network.RespuestaObrasDto
import com.example.museodelosmuseos.repository.ObrasRepository
import com.example.museodelosmuseos.ui.components.BarraNavegacion
import com.example.museodelosmuseos.ui.components.LocalRol
import com.example.museodelosmuseos.ui.components.MensajeErrorRed
import com.example.museodelosmuseos.ui.navigation.Rutas
import com.example.museodelosmuseos.ui.screens.ObrasScreen
import com.example.museodelosmuseos.viewmodel.ObrasViewModel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

// Pruebas instrumentadas de interfaz: se ejecutan en el emulador o en el celular
// y verifican que los componentes visuales (tarjetas, botones y mensajes de error)
// se dibujen y respondan correctamente.
@RunWith(AndroidJUnit4::class)
class ComposeUITest {

    @get:Rule
    val composeRule = createComposeRule()

    // API externa falsa: responde una obra o falla como si no hubiera internet
    private class ArteApiFalsa(private val fallar: Boolean) : ArteApiService {
        override suspend fun listarObras(campos: String, limite: Int): RespuestaObrasDto {
            if (fallar) throw IOException("sin red")
            return RespuestaObrasDto(
                obras = listOf(ObraDto(1, "Water Lilies", "Claude Monet", "1906", null)),
                config = ConfigDto(null)
            )
        }

        override suspend fun buscarObras(busqueda: String, campos: String, limite: Int) =
            listarObras(campos, limite)
    }

    // Espera a que aparezca un texto (la carga es asíncrona)
    private fun esperarTexto(texto: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // ---------- Tarjeta de error reutilizable ----------

    @Test
    fun mensajeErrorRed_muestraElMensajeYReintentarFunciona() {
        var reintentos = 0
        composeRule.setContent {
            MensajeErrorRed(mensaje = "Sin conexión con el servidor", onReintentar = { reintentos++ })
        }

        composeRule.onNodeWithText("Sin conexión con el servidor").assertIsDisplayed()
        composeRule.onNodeWithText("Reintentar").performClick()

        assertTrue(reintentos == 1)
    }

    // ---------- Barra de navegación según el rol ----------

    @Test
    fun barraNavegacion_clienteVeCatalogo() {
        composeRule.setContent {
            CompositionLocalProvider(LocalRol provides Rol.CLIENTE) {
                BarraNavegacion(rutaActual = Rutas.DASHBOARD, onNavegar = {})
            }
        }

        composeRule.onNodeWithText("Catálogo").assertIsDisplayed()
        composeRule.onNodeWithText("Mapa").assertIsDisplayed()
    }

    @Test
    fun barraNavegacion_investigadorNoVeCatalogo() {
        composeRule.setContent {
            CompositionLocalProvider(LocalRol provides Rol.INVESTIGADOR) {
                BarraNavegacion(rutaActual = Rutas.DASHBOARD, onNavegar = {})
            }
        }

        composeRule.onNodeWithText("Mapa").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithText("Catálogo").fetchSemanticsNodes().isEmpty())
    }

    // ---------- Pantalla de la API externa (Obras) ----------

    @Test
    fun obrasScreen_respuestaExitosa_dibujaLaTarjetaDeLaObra() {
        val viewModel = ObrasViewModel(ObrasRepository(ArteApiFalsa(fallar = false)))
        composeRule.setContent { ObrasScreen(viewModel = viewModel, onVolver = {}) }

        esperarTexto("Water Lilies")
        composeRule.onNodeWithText("Water Lilies").assertIsDisplayed()
        composeRule.onNodeWithText("Claude Monet").assertIsDisplayed()
    }

    @Test
    fun obrasScreen_sinInternet_muestraErrorYBotonReintentar() {
        val viewModel = ObrasViewModel(ObrasRepository(ArteApiFalsa(fallar = true)))
        composeRule.setContent { ObrasScreen(viewModel = viewModel, onVolver = {}) }

        esperarTexto("Sin conexión. Revisa tu internet e inténtalo de nuevo.")
        composeRule.onNodeWithText("Reintentar").assertIsDisplayed()
    }
}