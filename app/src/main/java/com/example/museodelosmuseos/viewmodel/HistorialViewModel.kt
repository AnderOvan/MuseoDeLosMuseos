package com.example.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.model.EstadoPedido
import com.example.museodelosmuseos.model.Rol
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.repository.local.PedidoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HistorialUiState(
    val pedidos: List<PedidoEntity> = emptyList(),   // ya filtrados por estado
    val totalPedidos: Int = 0,                       // sin filtrar (para el chip "Todos")
    val filtro: EstadoPedido? = null,
    val esAdmin: Boolean = false
)

// Cliente: ve sus solicitudes. Administrador: ve todas y puede cambiar su estado.
class HistorialViewModel(private val local: LocalRepository) : ViewModel() {

    private val _filtro = MutableStateFlow<EstadoPedido?>(null)

    val uiState: StateFlow<HistorialUiState> = combine(
        local.observarPedidos(),
        local.usuarioActivo,
        _filtro
    ) { pedidos, usuario, filtro ->
        HistorialUiState(
            pedidos = pedidos.filter { filtro == null || it.estado == filtro },
            totalPedidos = pedidos.size,
            filtro = filtro,
            esAdmin = usuario?.rol == Rol.ADMINISTRADOR
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistorialUiState())

    fun filtrar(estado: EstadoPedido?) {
        _filtro.value = estado
    }

    // Guarda el nuevo estado en Room; la lista se actualiza sola porque viene de un Flow
    fun cambiarEstado(pedido: PedidoEntity, nuevo: EstadoPedido) {
        viewModelScope.launch { local.cambiarEstadoPedido(pedido, nuevo) }
    }

    companion object {
        fun factory(local: LocalRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HistorialViewModel(local) as T
        }
    }
}