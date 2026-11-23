package com.example.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.repository.CarritoRepository
import com.example.museodelosmuseos.repository.FakeProductoRepository
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.repository.local.ProductoCacheEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Mensaje breve que la pantalla muestra con una animación
sealed interface MensajeCompra {
    data class Exito(val texto: String) : MensajeCompra
    data class Error(val texto: String) : MensajeCompra
}

data class DetalleProductoUiState(
    val producto: ProductoCacheEntity? = null,
    val cantidad: Int = 1,
    val enCarrito: Int = 0,           // unidades de este producto que ya están en el carrito
    val maximo: Int = 0,              // cuántas se pueden agregar todavía
    val validando: Boolean = false,   // consultando stock "al backend"
    val mensaje: MensajeCompra? = null
) {
    val esCurso: Boolean get() = producto?.categoria == "Curso"
    val agotado: Boolean get() = maximo <= 0
}

// Estado de la compra en curso (se agrupa para el combine)
private data class EstadoCompra(
    val cantidad: Int = 1,
    val validando: Boolean = false,
    val mensaje: MensajeCompra? = null
)

class DetalleProductoViewModel(
    private val productoId: String,
    localRepository: LocalRepository
) : ViewModel() {

    private val _compra = MutableStateFlow(EstadoCompra())

    // Producto desde la caché de Room (si aún no está, desde el catálogo local) + carrito
    val uiState: StateFlow<DetalleProductoUiState> = combine(
        localRepository.observarCatalogo(),
        CarritoRepository.items,
        _compra
    ) { cache, carrito, compra ->
        val producto = cache.find { it.id == productoId } ?: FakeProductoRepository.buscarPorId(productoId)
        val enCarrito = carrito.find { it.producto.id == productoId }?.cantidad ?: 0
        val maximo = when {
            producto == null -> 0
            producto.categoria == "Curso" -> if (enCarrito > 0) 0 else 1 // un curso se compra una vez
            else -> producto.stock - enCarrito
        }
        DetalleProductoUiState(
            producto = producto,
            cantidad = compra.cantidad.coerceIn(1, maxOf(1, maximo)),
            enCarrito = enCarrito,
            maximo = maximo,
            validando = compra.validando,
            mensaje = compra.mensaje
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalleProductoUiState())

    fun incrementarCantidad() {
        val maximo = uiState.value.maximo
        _compra.update { if (it.cantidad < maximo) it.copy(cantidad = it.cantidad + 1) else it }
    }

    fun decrementarCantidad() {
        _compra.update { if (it.cantidad > 1) it.copy(cantidad = it.cantidad - 1) else it }
    }

    // Valida el stock con el "backend" y recién ahí agrega al carrito
    fun agregarAlCarrito() {
        val estado = uiState.value
        val producto = estado.producto ?: return
        if (estado.validando) return

        viewModelScope.launch {
            _compra.update { it.copy(validando = true, mensaje = null) }
            val stock = FakeProductoRepository.consultarStock(producto.id)
            val pedido = estado.enCarrito + estado.cantidad

            val mensaje = when {
                estado.esCurso && estado.enCarrito > 0 ->
                    MensajeCompra.Error("Este curso ya está en tu carrito")
                stock <= 0 ->
                    MensajeCompra.Error("Producto agotado")
                pedido > stock ->
                    MensajeCompra.Error("Solo quedan $stock unidades disponibles")
                else -> {
                    CarritoRepository.agregar(producto, estado.cantidad)
                    MensajeCompra.Exito("Agregado al carrito: ${estado.cantidad} × ${producto.titulo}")
                }
            }
            _compra.update { it.copy(validando = false, mensaje = mensaje, cantidad = 1) }
        }
    }

    // Solo para cursos: simula el envío de una solicitud de información
    fun solicitarInformacion() {
        val producto = uiState.value.producto ?: return
        viewModelScope.launch {
            _compra.update { it.copy(validando = true, mensaje = null) }
            delay(500)
            _compra.update {
                it.copy(
                    validando = false,
                    mensaje = MensajeCompra.Exito("Solicitud enviada: te escribiremos sobre \"${producto.titulo}\"")
                )
            }
        }
    }

    fun limpiarMensaje() = _compra.update { it.copy(mensaje = null) }

    companion object {
        fun factory(productoId: String, localRepository: LocalRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                DetalleProductoViewModel(productoId, localRepository) as T
        }
    }
}