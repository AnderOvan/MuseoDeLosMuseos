package com.example.museodelosmuseos

import com.example.museodelosmuseos.DatosDePrueba.errorHttp
import com.example.museodelosmuseos.DatosDePrueba.museo
import com.example.museodelosmuseos.DatosDePrueba.pedido
import com.example.museodelosmuseos.DatosDePrueba.producto
import com.example.museodelosmuseos.DatosDePrueba.respuestaObras
import com.example.museodelosmuseos.DatosDePrueba.respuestaVacia
import com.example.museodelosmuseos.model.EstadoPedido
import com.example.museodelosmuseos.network.ArteApiService
import com.example.museodelosmuseos.network.CambioEstadoDto
import com.example.museodelosmuseos.network.MuseoApiService
import com.example.museodelosmuseos.network.ObraDto
import com.example.museodelosmuseos.network.PedidoApiService
import com.example.museodelosmuseos.network.ProductoApiService
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.repository.MuseoRepositoryRemoto
import com.example.museodelosmuseos.repository.ObrasRepository
import com.example.museodelosmuseos.repository.PedidoRepositoryRemoto
import com.example.museodelosmuseos.repository.ProductoRepositoryRemoto
import com.example.museodelosmuseos.repository.local.PedidoEntity
import com.example.museodelosmuseos.repository.local.ProductoCacheEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException

// Pruebas de los repositorios: el ApiService (la red) se reemplaza por un mock de MockK.
// coEvery  -> define qué "responde el servidor"
// coVerify -> comprueba que el repositorio llamó al endpoint correcto
class RepositoryTest {

    // ---------- API externa: ObrasRepository ----------

    @Test
    fun obras_sinBusqueda_usaListarYArmaLaUrlDeLaImagen() = runTest {
        val api = mockk<ArteApiService>()
        coEvery { api.listarObras(any(), any()) } returns respuestaObras(
            ObraDto(1, "Water Lilies", "Claude Monet", "1906", "abc123")
        )

        val obras = ObrasRepository(api).obtenerObras("")

        assertEquals(1, obras.size)
        assertEquals("Water Lilies", obras[0].titulo)
        assertEquals("https://www.artic.edu/iiif/2/abc123/full/843,/0/default.jpg", obras[0].imagenUrl)
        coVerify(exactly = 1) { api.listarObras(any(), any()) }
        coVerify(exactly = 0) { api.buscarObras(any(), any(), any()) }
    }

    @Test
    fun obras_conBusqueda_usaBuscarYCompletaDatosVacios() = runTest {
        val api = mockk<ArteApiService>()
        coEvery { api.buscarObras("monet", any(), any()) } returns respuestaObras(
            ObraDto(2, null, "", null, null)
        )

        val obra = ObrasRepository(api).obtenerObras("  monet  ").first()

        assertEquals("Sin título", obra.titulo)
        assertEquals("Artista desconocido", obra.artista)
        assertEquals("Fecha desconocida", obra.fecha)
        assertNull(obra.imagenUrl) // sin image_id no hay imagen
        coVerify { api.buscarObras("monet", any(), any()) } // el texto llega sin espacios
    }

    // ---------- Microservicio de museos ----------

    @Test
    fun museos_servidorResponde_devuelveExitoConLosMuseos() = runTest {
        val api = mockk<MuseoApiService>()
        coEvery { api.listar() } returns listOf(museo("MU01"), museo("MU02", pais = "Perú"))

        val resultado = MuseoRepositoryRemoto(api).obtenerMuseos()

        assertTrue(resultado.isSuccess)
        val museos = resultado.getOrThrow()
        assertEquals(listOf("MU01", "MU02"), museos.map { it.id })
        assertEquals("Perú", museos[1].pais)
    }

    @Test
    fun museos_sinConexion_devuelveFalloSinCerrarLaApp() = runTest {
        val api = mockk<MuseoApiService>()
        coEvery { api.listar() } throws IOException("sin red")

        val resultado = MuseoRepositoryRemoto(api).obtenerMuseos()

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is IOException)
    }

    @Test
    fun museos_eliminarConError404_devuelveFallo() = runTest {
        val api = mockk<MuseoApiService>()
        coEvery { api.eliminar("MU99") } returns respuestaVacia(404)

        val resultado = MuseoRepositoryRemoto(api).eliminarMuseo("MU99")

        assertTrue(resultado.exceptionOrNull() is HttpException)
    }

    // ---------- Microservicio de productos (con caché en Room) ----------

    @Test
    fun productos_sincronizar_guardaSoloLosActivosEnLaCache() = runTest {
        val api = mockk<ProductoApiService>()
        val local = mockk<LocalRepository>(relaxed = true)
        coEvery { api.listar() } returns listOf(producto("L001"), producto("L002", activo = false))
        val guardados = slot<List<ProductoCacheEntity>>()
        coEvery { local.reemplazarCatalogo(capture(guardados)) } returns Unit

        val resultado = ProductoRepositoryRemoto(local, api).sincronizar()

        assertEquals(1, resultado.getOrThrow())
        assertEquals(listOf("L001"), guardados.captured.map { it.id }) // el inactivo no se guarda
    }

    @Test
    fun productos_consultarStock_devuelveElStockDelServidor() = runTest {
        val api = mockk<ProductoApiService>()
        coEvery { api.obtener("L001") } returns producto("L001", stock = 3)

        val stock = ProductoRepositoryRemoto(mockk(relaxed = true), api).consultarStock("L001")

        assertEquals(3, stock.getOrThrow())
    }

    @Test
    fun productos_desactivar_enviaActivoFalseYVuelveASincronizar() = runTest {
        val api = mockk<ProductoApiService>()
        val local = mockk<LocalRepository>(relaxed = true)
        coEvery { api.actualizar(any(), any()) } returns producto("L001", activo = false)
        coEvery { api.listar() } returns emptyList()
        val entidad = ProductoCacheEntity("L001", "Libro", "Desc", 35.0, null, "Libro", 10)

        val resultado = ProductoRepositoryRemoto(local, api).desactivar(entidad)

        assertTrue(resultado.isSuccess)
        coVerify { api.actualizar("L001", match { !it.activo }) }
        coVerify { api.listar() } // la caché se actualiza después del cambio
    }

    @Test
    fun productos_crearConErrorDelServidor_devuelveFalloYNoSincroniza() = runTest {
        val api = mockk<ProductoApiService>()
        coEvery { api.crear(any()) } throws errorHttp(500)
        val entidad = ProductoCacheEntity("L009", "Nuevo", "Desc", 10.0, null, "Libro", 5)

        val resultado = ProductoRepositoryRemoto(mockk(relaxed = true), api).crear(entidad)

        assertTrue(resultado.isFailure)
        coVerify(exactly = 0) { api.listar() }
    }

    // ---------- Microservicio de pedidos ----------

    @Test
    fun pedidos_crear_enviaIdNuloYRecibeElIdDelBackend() = runTest {
        val api = mockk<PedidoApiService>()
        coEvery { api.crear(any()) } returns pedido(id = 42)
        val nuevo = PedidoEntity(
            usuarioId = 5, clienteNombre = "Cliente", numero = "PED-42", resumen = "1x Libro",
            total = 39.0, destino = "Chile", tipoEnvio = "Estándar"
        )

        val creado = PedidoRepositoryRemoto(api).crear(nuevo).getOrThrow()

        assertEquals(42L, creado.id)
        coVerify { api.crear(match { it.id == null && it.estado == "PENDIENTE" }) }
    }

    @Test
    fun pedidos_listarComoCliente_filtraPorUsuarioYConvierteElEstado() = runTest {
        val api = mockk<PedidoApiService>()
        coEvery { api.listar(5) } returns listOf(pedido(1, estado = "ENVIADO"), pedido(2, estado = "RARO"))

        val pedidos = PedidoRepositoryRemoto(api).listar(5).getOrThrow()

        assertEquals(EstadoPedido.ENVIADO, pedidos[0].estado)
        assertEquals(EstadoPedido.PENDIENTE, pedidos[1].estado) // estado desconocido -> PENDIENTE
    }

    @Test
    fun pedidos_cambiarEstado_enviaElNombreDelEstado() = runTest {
        val api = mockk<PedidoApiService>()
        coEvery { api.cambiarEstado(7, any()) } returns pedido(7, estado = "ENTREGADO")

        val actualizado = PedidoRepositoryRemoto(api).cambiarEstado(7, EstadoPedido.ENTREGADO).getOrThrow()

        assertEquals(EstadoPedido.ENTREGADO, actualizado.estado)
        coVerify { api.cambiarEstado(7, CambioEstadoDto("ENTREGADO")) }
    }

    @Test
    fun pedidos_eliminar_exitoCon204() = runTest {
        val api = mockk<PedidoApiService>()
        coEvery { api.eliminar(3) } returns respuestaVacia(204)

        assertTrue(PedidoRepositoryRemoto(api).eliminar(3).isSuccess)
    }
}