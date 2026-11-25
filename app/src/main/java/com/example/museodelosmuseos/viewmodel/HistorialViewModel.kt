package com.example.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.model.EstadoPedido
import com.example.museodelosmuseos.model.Rol
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.repository.PedidoRepositoryRemoto
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
    val esAdmin: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

// Cliente: ve sus solicitudes. Administrador: ve todas y puede cambiar su estado.
// La información oficial viene del backend (GET/PUT/DELETE /api/pedidos).
class HistorialViewModel(
    private val local: LocalRepository,
    private val remoto: PedidoRepositoryRemoto = PedidoRepositoryRemoto()
) : ViewModel() {

    private val _pedidos = MutableStateFlow<List<PedidoEntity>>(emptyList())
    private val _filtro = MutableStateFlow<EstadoPedido?>(null)
    private val _cargando = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HistorialUiState> = combine(
        _pedidos,
        local.usuarioActivo,
        _filtro,
        _cargando,
        _error
    ) { pedidos, usuario, filtro, cargando, error ->
        HistorialUiState(
            pedidos = pedidos.filter { filtro == null || it.estado == filtro },
            totalPedidos = pedidos.size,
            filtro = filtro,
            esAdmin = usuario?.rol == Rol.ADMINISTRADOR,
            isLoading = cargando,
            error = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistorialUiState())

    init {
        cargar()
    }

    // GET /api/pedidos (Administrador: todos) o /api/pedidos?usuarioId=X (Cliente: los suyos)
    // También es el botón "Reintentar".
    fun cargar() {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            val usuario = local.obtenerUsuarioActivo()
            if (usuario == null) {
                _pedidos.value = emptyList()
            } else {
                val filtroUsuario = if (usuario.rol == Rol.ADMINISTRADOR) null else usuario.id
                remoto.listar(filtroUsuario)
                    .onSuccess { lista -> _pedidos.value = lista.sortedByDescending { it.fecha } }
                    .onFailure { e -> _error.value = mensajeDeError(e) }
            }
            _cargando.value = false
        }
    }

    fun filtrar(estado: EstadoPedido?) {
        _filtro.value = estado
    }

    // PUT /api/pedidos/{id}/estado (solo Administrador) -> avisa al cliente -> recarga la lista
    fun cambiarEstado(pedido: PedidoEntity, nuevo: EstadoPedido) {
        if (!uiState.value.esAdmin || pedido.estado == nuevo) return
        viewModelScope.launch {
            remoto.cambiarEstado(pedido.id, nuevo)
                .onSuccess {
                    local.avisarCambioEstado(pedido, nuevo)
                    cargar()
                }
                .onFailure { e -> _error.value = mensajeDeError(e) }
        }
    }

    // DELETE /api/pedidos/{id} -> recarga la lista
    fun eliminar(pedido: PedidoEntity) {
        viewModelScope.launch {
            remoto.eliminar(pedido.id)
                .onSuccess { cargar() }
                .onFailure { e -> _error.value = mensajeDeError(e) }
        }
    }

    companion object {
        fun factory(local: LocalRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HistorialViewModel(local) as T
        }
    }
}