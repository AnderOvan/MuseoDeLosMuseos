package com.example.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.repository.CarritoRepository
import com.example.museodelosmuseos.repository.ItemCarrito
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.repository.PedidoRepositoryRemoto
import com.example.museodelosmuseos.repository.local.PedidoEntity
import com.example.museodelosmuseos.util.CotizadorEnvios
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TipoEnvio(val titulo: String) {
    ESTANDAR("Estándar"),
    EXPRESS("Express")
}

// Comprobante que devuelve el "backend" al confirmar
data class PedidoConfirmado(
    val numero: String,
    val total: Double,
    val pais: String,
    val tipoEnvio: TipoEnvio,
    val plazo: String
)

data class CotizadorUiState(
    val items: List<ItemCarrito> = emptyList(),
    val pais: String = CotizadorEnvios.PAIS_NACIONAL,
    val tipoEnvio: TipoEnvio = TipoEnvio.ESTANDAR,
    val subtotal: Double = 0.0,
    val envio: Double = 0.0,
    val total: Double = 0.0,
    val plazo: String = "",
    val soloDigital: Boolean = false,       // solo cursos: no hay despacho
    val faltaParaGratis: Double = 0.0,      // cuánto falta para envío gratis en Chile
    val mostrarConfirmacion: Boolean = false,
    val procesando: Boolean = false,
    val pedido: PedidoConfirmado? = null,
    val error: String? = null               // el backend no pudo registrar el pedido
)

private data class FormularioEnvio(
    val pais: String = CotizadorEnvios.PAIS_NACIONAL,
    val tipoEnvio: TipoEnvio = TipoEnvio.ESTANDAR
)

private data class EstadoPago(
    val mostrarConfirmacion: Boolean = false,
    val procesando: Boolean = false,
    val pedido: PedidoConfirmado? = null,
    val error: String? = null
)

class CotizadorViewModel(
    private val local: LocalRepository,
    // Backend de pedidos: "el backend centraliza el pedido y su estado"
    private val remoto: PedidoRepositoryRemoto = PedidoRepositoryRemoto()
) : ViewModel() {

    private val _formulario = MutableStateFlow(FormularioEnvio())
    private val _pago = MutableStateFlow(EstadoPago())

    // Recalcula subtotal, despacho y total cada vez que cambia el carrito o el formulario
    val uiState: StateFlow<CotizadorUiState> = combine(
        CarritoRepository.items,
        _formulario,
        _pago
    ) { items, formulario, pago ->
        val subtotal = items.sumOf { it.subtotal }
        val hayFisicos = items.any { it.producto.categoria != "Curso" }
        val esExpress = formulario.tipoEnvio == TipoEnvio.EXPRESS
        val envio =
            CotizadorEnvios.calcularCostoEnvio(formulario.pais, esExpress, subtotal, hayFisicos)
        val falta = if (hayFisicos && CotizadorEnvios.esNacional(formulario.pais) && !esExpress)
            (CotizadorEnvios.ENVIO_GRATIS_DESDE - subtotal).coerceAtLeast(0.0) else 0.0

        CotizadorUiState(
            items = items,
            pais = formulario.pais,
            tipoEnvio = formulario.tipoEnvio,
            subtotal = subtotal,
            envio = envio,
            total = subtotal + envio,
            plazo = CotizadorEnvios.plazoEntrega(formulario.pais, esExpress),
            soloDigital = items.isNotEmpty() && !hayFisicos,
            faltaParaGratis = falta,
            mostrarConfirmacion = pago.mostrarConfirmacion,
            procesando = pago.procesando,
            pedido = pago.pedido,
            error = pago.error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CotizadorUiState())

    // ---------- Formulario de envío ----------
    fun seleccionarPais(pais: String) = _formulario.update { it.copy(pais = pais) }

    fun seleccionarTipoEnvio(tipo: TipoEnvio) = _formulario.update { it.copy(tipoEnvio = tipo) }

    // ---------- Cantidades ----------
    fun incrementar(item: ItemCarrito) {
        // No se puede pedir más que el stock; los cursos van de a uno
        val maximo = if (item.producto.categoria == "Curso") 1 else item.producto.stock
        if (item.cantidad < maximo) CarritoRepository.cambiarCantidad(
            item.producto.id,
            item.cantidad + 1
        )
    }

    fun decrementar(item: ItemCarrito) {
        if (item.cantidad > 1) CarritoRepository.cambiarCantidad(
            item.producto.id,
            item.cantidad - 1
        )
    }

    fun eliminar(item: ItemCarrito) = CarritoRepository.quitar(item.producto.id)

    // ---------- Pago ----------
    fun solicitarConfirmacion() {
        _pago.update { it.copy(mostrarConfirmacion = true) }
    }

    fun cancelarConfirmacion() {
        _pago.update { it.copy(mostrarConfirmacion = false) }
    }

    // POST /api/pedidos -> si el backend responde bien: copia local + aviso, y se vacía el carrito.
    // Si falla: el carrito queda intacto y se muestra el error para reintentar.
    fun procesarPago() {
        viewModelScope.launch {
            _pago.update { it.copy(procesando = true, error = null) }

            val state = uiState.value
            val usuario = local.obtenerUsuarioActivo()
            if (usuario == null) {
                _pago.update { it.copy(procesando = false, error = "Inicia sesión para enviar la solicitud.") }
                return@launch
            }

            val numPedido = "PED-${(1000..9999).random()}"
            val resumen = state.items.joinToString(", ") { "${it.cantidad}x ${it.producto.titulo}" }
            val tipoEnvio = "${state.tipoEnvio.titulo} (${state.plazo})"

            val nuevo = PedidoEntity(
                usuarioId = usuario.id,
                clienteNombre = usuario.nombre,
                numero = numPedido,
                resumen = resumen,
                total = state.total,
                destino = state.pais,
                tipoEnvio = tipoEnvio
            )

            remoto.crear(nuevo)
                .onSuccess { creado ->
                    // El número oficial lo asigna el backend (MDM-xxxxx); numPedido es solo un respaldo
                    val numeroOficial = creado.numero.ifBlank { numPedido }
                    // Historial local de consulta (SQLite) + notificación al cliente
                    local.registrarPedido(numeroOficial, resumen, state.total, state.pais, tipoEnvio)
                    CarritoRepository.vaciar()
                    _pago.value = EstadoPago(
                        pedido = PedidoConfirmado(
                            numero = numeroOficial,
                            total = state.total,
                            pais = state.pais,
                            tipoEnvio = state.tipoEnvio,
                            plazo = state.plazo
                        )
                    )
                }
                .onFailure { e ->
                    _pago.update {
                        it.copy(procesando = false, mostrarConfirmacion = false, error = mensajeDeError(e))
                    }
                }
        }
    }

    fun limpiarError() = _pago.update { it.copy(error = null) }

    fun cerrarPedido() {
        _pago.value = EstadoPago()
    }

    companion object {
        fun factory(localRepository: LocalRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CotizadorViewModel(localRepository) as T
        }
    }
}