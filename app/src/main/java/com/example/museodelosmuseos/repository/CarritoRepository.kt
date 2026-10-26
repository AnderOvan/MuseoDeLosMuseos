package com.example.museodelosmuseos.repository

import com.example.museodelosmuseos.repository.local.ProductoCacheEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Un producto dentro del carrito con la cantidad elegida
data class ItemCarrito(
    val producto: ProductoCacheEntity,
    val cantidad: Int
) {
    val subtotal: Double get() = producto.precio * cantidad
}

// Carrito global de la app: lo comparten el Detalle, el Carrito y el Cotizador.
// Vive en memoria mientras la app está abierta (el pedido se confirma en el Carrito).
object CarritoRepository {

    private val _items = MutableStateFlow<List<ItemCarrito>>(emptyList())
    val items: StateFlow<List<ItemCarrito>> = _items.asStateFlow()

    fun cantidadDe(productoId: String): Int =
        _items.value.find { it.producto.id == productoId }?.cantidad ?: 0

    // Si el producto ya está, suma la cantidad; si no, lo agrega
    fun agregar(producto: ProductoCacheEntity, cantidad: Int) {
        _items.update { lista ->
            if (lista.any { it.producto.id == producto.id }) {
                lista.map { if (it.producto.id == producto.id) it.copy(cantidad = it.cantidad + cantidad) else it }
            } else {
                lista + ItemCarrito(producto, cantidad)
            }
        }
    }

    fun quitar(productoId: String) {
        _items.update { lista -> lista.filterNot { it.producto.id == productoId } }
    }

    fun vaciar() {
        _items.value = emptyList()
    }
}