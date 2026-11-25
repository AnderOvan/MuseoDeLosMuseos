package com.example.museodelosmuseos

import com.example.museodelosmuseos.DatosDePrueba.errorHttp
import com.example.museodelosmuseos.DatosDePrueba.museo
import com.example.museodelosmuseos.DatosDePrueba.pedido
import com.example.museodelosmuseos.DatosDePrueba.producto
import com.example.museodelosmuseos.DatosDePrueba.respuestaObras
import com.example.museodelosmuseos.DatosDePrueba.respuestaVacia
import com.example.museodelosmuseos.DatosDePrueba.usuario
import com.example.museodelosmuseos.model.EstadoPedido
import com.example.museodelosmuseos.model.Rol
import com.example.museodelosmuseos.network.ArteApiService
import com.example.museodelosmuseos.network.MuseoApiService
import com.example.museodelosmuseos.network.ObraDto
import com.example.museodelosmuseos.network.PedidoApiService
import com.example.museodelosmuseos.network.ProductoApiService
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.repository.MuseoRepositoryRemoto
import com.example.museodelosmuseos.repository.ObrasRepository
import com.example.museodelosmuseos.repository.PedidoRepositoryRemoto
import com.example.museodelosmuseos.repository.ProductoRepositoryRemoto
import com.example.museodelosmuseos.repository.local.ProductoCacheEntity
import com.example.museodelosmuseos.util.CampoProducto
import com.example.museodelosmuseos.viewmodel.CatalogoViewModel
import com.example.museodelosmuseos.viewmodel.FormularioProductoViewModel
import com.example.museodelosmuseos.viewmodel.HistorialViewModel
import com.example.museodelosmuseos.viewmodel.MapaViewModel
import com.example.museodelosmuseos.viewmodel.ObrasViewModel
import com.example.museodelosmuseos.viewmodel.mensajeDeError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

// Pruebas de los ViewModels: se valida cómo cambia el StateFlow (datos, carga y error)
// cuando el servidor responde bien o falla. La red se simula con MockK.
@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {

    // Reemplaza Dispatchers.Main (no existe en pruebas unitarias)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ---------- ObrasViewModel (API externa) ----------

    @Test
    fun obras_respuestaExitosa_muestraLasObrasYTerminaLaCarga() = runTest {
        val api = mockk<ArteApiService>()
        coEvery { api.listarObras(any(), any()) } returns respuestaObras(
            ObraDto(1, "Water Lilies", "Monet", "1906", null),
            ObraDto(2, "The Bedroom", "Van Gogh", "1889", null)
        )

        val vm = ObrasViewModel(ObrasRepository(api)) // init carga las obras

        val estado = vm.uiState.value
        assertEquals(2, estado.obras.size)
        assertFalse(estado.isLoading)
        assertNull(estado.error)
    }

    @Test
    fun obras_sinInternet_muestraMensajeDeErrorYListaVacia() = runTest {
        val api = mockk<ArteApiService>()
        coEvery { api.listarObras(any(), any()) } throws IOException()

        val vm = ObrasViewModel(ObrasRepository(api))

        val estado = vm.uiState.value
        assertEquals("Sin conexión. Revisa tu internet e inténtalo de nuevo.", estado.error)
        assertTrue(estado.obras.isEmpty())
        assertFalse(estado.isLoading)
    }

    @Test
    fun obras_buscar_usaElTextoEscritoYReintentarLimpiaElError() = runTest {
        val api = mockk<ArteApiService>()
        coEvery { api.listarObras(any(), any()) } throws IOException()
        coEvery { api.buscarObras("cat", any(), any()) } returns respuestaObras(
            ObraDto(3, "Cat", "Anónimo", "1900", null)
        )
        val vm = ObrasViewModel(ObrasRepository(api))

        vm.cambiarBusqueda("cat")
        vm.cargar()

        assertEquals("Cat", vm.uiState.value.obras.single().titulo)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun mensajeDeError_traduceCadaTipoDeFallo() {
        assertEquals("Sin conexión. Revisa tu internet e inténtalo de nuevo.", mensajeDeError(IOException()))
        assertEquals("El servidor respondió con un error (500).", mensajeDeError(errorHttp(500)))
        assertEquals("Ocurrió un error inesperado al cargar los datos.", mensajeDeError(IllegalStateException()))
    }

    // ---------- MapaViewModel (microservicio de museos) ----------

    @Test
    fun mapa_servidorResponde_ordenaPorDistanciaYPermiteFiltrar() = runTest {
        val api = mockk<MuseoApiService>()
        coEvery { api.listar() } returns listOf(
            museo("LEJOS", pais = "México", lat = 19.43, lon = -99.13),
            museo("CERCA", pais = "Chile", lat = -33.44, lon = -70.65)
        )

        val vm = MapaViewModel(MuseoRepositoryRemoto(api))

        assertEquals(listOf("CERCA", "LEJOS"), vm.uiState.value.todos.map { it.museo.id })
        assertFalse(vm.uiState.value.cargando)

        vm.filtrarPorPais("México")
        assertEquals(listOf("LEJOS"), vm.uiState.value.visibles.map { it.museo.id })
    }

    @Test
    fun mapa_errorDelServidor_muestraErrorYNoMuseos() = runTest {
        val api = mockk<MuseoApiService>()
        coEvery { api.listar() } throws errorHttp(503)

        val vm = MapaViewModel(MuseoRepositoryRemoto(api))

        assertEquals("El servidor respondió con un error (503).", vm.uiState.value.error)
        assertTrue(vm.uiState.value.todos.isEmpty())
    }

    // ---------- CatalogoViewModel (microservicio de productos + caché Room) ----------

    @Test
    fun catalogo_sincronizacionFallida_muestraError() = runTest {
        val local = mockk<LocalRepository>(relaxed = true)
        every { local.observarCatalogo() } returns flowOf(emptyList())
        val api = mockk<ProductoApiService>()
        coEvery { api.listar() } throws IOException()

        val vm = CatalogoViewModel(local, ProductoRepositoryRemoto(local, api))
        // El uiState solo se calcula mientras alguien lo observa (como la pantalla)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        assertEquals("Sin conexión. Revisa tu internet e inténtalo de nuevo.", vm.uiState.value.error)
    }

    @Test
    fun catalogo_sincronizacionExitosa_guardaEnRoomSinError() = runTest {
        val local = mockk<LocalRepository>(relaxed = true)
        every { local.observarCatalogo() } returns flowOf(emptyList())
        val api = mockk<ProductoApiService>()
        coEvery { api.listar() } returns listOf(producto("L001"))

        val vm = CatalogoViewModel(local, ProductoRepositoryRemoto(local, api))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        assertNull(vm.uiState.value.error)
        coVerify { local.reemplazarCatalogo(match { it.single().id == "L001" }) }
    }

    // ---------- HistorialViewModel (microservicio de pedidos) ----------

    @Test
    fun historial_administrador_veTodosLosPedidos() = runTest {
        val admin = usuario(1, Rol.ADMINISTRADOR)
        val local = mockk<LocalRepository>(relaxed = true)
        every { local.usuarioActivo } returns flowOf(admin)
        coEvery { local.obtenerUsuarioActivo() } returns admin
        val api = mockk<PedidoApiService>()
        coEvery { api.listar(null) } returns listOf(pedido(1), pedido(2, usuarioId = 9))

        val vm = HistorialViewModel(local, PedidoRepositoryRemoto(api))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        assertEquals(2, vm.uiState.value.totalPedidos)
        assertTrue(vm.uiState.value.esAdmin)
        coVerify { api.listar(null) } // sin filtro de usuario
    }

    @Test
    fun historial_cliente_soloPideSusPedidos() = runTest {
        val cliente = usuario(5, Rol.CLIENTE)
        val local = mockk<LocalRepository>(relaxed = true)
        every { local.usuarioActivo } returns flowOf(cliente)
        coEvery { local.obtenerUsuarioActivo() } returns cliente
        val api = mockk<PedidoApiService>()
        coEvery { api.listar(5) } returns listOf(pedido(1))

        HistorialViewModel(local, PedidoRepositoryRemoto(api))

        coVerify { api.listar(5) }
    }

    @Test
    fun historial_adminCambiaEstado_llamaAlBackendYAvisaAlCliente() = runTest {
        val admin = usuario(1, Rol.ADMINISTRADOR)
        val local = mockk<LocalRepository>(relaxed = true)
        every { local.usuarioActivo } returns flowOf(admin)
        coEvery { local.obtenerUsuarioActivo() } returns admin
        val api = mockk<PedidoApiService>()
        coEvery { api.listar(null) } returns listOf(pedido(7))
        coEvery { api.cambiarEstado(7, any()) } returns pedido(7, estado = "ENVIADO")

        val vm = HistorialViewModel(local, PedidoRepositoryRemoto(api))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        val elPedido = vm.uiState.value.pedidos.single()

        vm.cambiarEstado(elPedido, EstadoPedido.ENVIADO)

        coVerify { api.cambiarEstado(7, any()) }
        coVerify { local.avisarCambioEstado(elPedido, EstadoPedido.ENVIADO) }
    }

    @Test
    fun historial_eliminarFallido_muestraError() = runTest {
        val admin = usuario(1, Rol.ADMINISTRADOR)
        val local = mockk<LocalRepository>(relaxed = true)
        every { local.usuarioActivo } returns flowOf(admin)
        coEvery { local.obtenerUsuarioActivo() } returns admin
        val api = mockk<PedidoApiService>()
        coEvery { api.listar(null) } returns listOf(pedido(3))
        coEvery { api.eliminar(3) } returns respuestaVacia(404)

        val vm = HistorialViewModel(local, PedidoRepositoryRemoto(api))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        vm.eliminar(vm.uiState.value.pedidos.single())

        assertEquals("El servidor respondió con un error (404).", vm.uiState.value.error)
    }

    // ---------- FormularioProductoViewModel (CRUD del Administrador) ----------

    private fun localConCatalogo(vararg ids: String): LocalRepository {
        val local = mockk<LocalRepository>(relaxed = true)
        every { local.observarCatalogo() } returns flowOf(
            ids.map { ProductoCacheEntity(it, "Producto $it", "Desc", 10.0, null, "Libro", 5) }
        )
        return local
    }

    @Test
    fun formulario_camposInvalidos_muestraErroresYNoLlamaAlBackend() = runTest {
        val local = localConCatalogo()
        val api = mockk<ProductoApiService>(relaxed = true)
        val vm = FormularioProductoViewModel(null, local, ProductoRepositoryRemoto(local, api))

        vm.guardar() // todo vacío

        val errores = vm.uiState.value.errores
        assertTrue(CampoProducto.ID in errores)
        assertTrue(CampoProducto.PRECIO in errores)
        coVerify(exactly = 0) { api.crear(any()) }
    }

    @Test
    fun formulario_productoNuevoValido_haceElPostYTermina() = runTest {
        val local = localConCatalogo("L001")
        val api = mockk<ProductoApiService>()
        coEvery { api.crear(any()) } returns producto("L004")
        coEvery { api.listar() } returns listOf(producto("L001"), producto("L004"))
        val vm = FormularioProductoViewModel(null, local, ProductoRepositoryRemoto(local, api))

        vm.onId("l004")
        vm.onTitulo("Guía de Museos de Perú")
        vm.onDescripcion("Guía ilustrada")
        vm.onPrecio("25,5")
        vm.onStock("8")
        vm.guardar()

        assertTrue(vm.uiState.value.terminado)
        coVerify { api.crear(match { it.id == "L004" && it.precio == 25.5 && it.stock == 8 }) }
    }

    @Test
    fun formulario_editarYEliminarSinConexion_muestraError() = runTest {
        val local = localConCatalogo("L001")
        val api = mockk<ProductoApiService>()
        coEvery { api.eliminar("L001") } throws IOException()
        val vm = FormularioProductoViewModel("L001", local, ProductoRepositoryRemoto(local, api))

        assertFalse(vm.uiState.value.esNuevo)
        assertEquals("Producto L001", vm.uiState.value.titulo) // se llenó desde la caché

        vm.eliminar()

        assertFalse(vm.uiState.value.terminado)
        assertEquals("Sin conexión. Revisa tu internet e inténtalo de nuevo.", vm.uiState.value.error)
    }
}