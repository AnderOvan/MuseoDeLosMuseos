package com.example.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.repository.ProductoRepositoryRemoto
import com.example.museodelosmuseos.repository.local.ProductoCacheEntity
import com.example.museodelosmuseos.util.CampoProducto
import com.example.museodelosmuseos.util.ValidacionProducto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormularioProductoUiState(
    val esNuevo: Boolean = true,
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val precio: String = "",
    val stock: String = "",
    val categoria: String = "Libro",
    val errores: Map<CampoProducto, String> = emptyMap(), // validación de cada campo
    val guardando: Boolean = false,                        // esperando al backend
    val error: String? = null,                             // fallo de conexión
    val terminado: Boolean = false                         // la pantalla vuelve atrás
)

// CRUD del Administrador sobre el microservicio de productos:
// crear (POST), editar (PUT), desactivar (PUT activo=false) y eliminar (DELETE).
class FormularioProductoViewModel(
    private val productoId: String?,            // null = producto nuevo
    private val local: LocalRepository,
    private val remoto: ProductoRepositoryRemoto = ProductoRepositoryRemoto(local)
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormularioProductoUiState(esNuevo = productoId == null))
    val uiState: StateFlow<FormularioProductoUiState> = _uiState.asStateFlow()

    init {
        // Al editar, el formulario se llena con el producto guardado en la caché
        if (productoId != null) {
            viewModelScope.launch {
                local.observarCatalogo().first().find { it.id == productoId }?.let { p ->
                    _uiState.update {
                        it.copy(
                            id = p.id, titulo = p.titulo, descripcion = p.descripcion,
                            precio = p.precio.toString(), stock = p.stock.toString(), categoria = p.categoria
                        )
                    }
                }
            }
        }
    }

    // ---------- Cambios en los campos (borran el error de ese campo) ----------
    fun onId(v: String) = _uiState.update { it.copy(id = v.uppercase(), errores = it.errores - CampoProducto.ID) }
    fun onTitulo(v: String) = _uiState.update { it.copy(titulo = v, errores = it.errores - CampoProducto.TITULO) }
    fun onDescripcion(v: String) = _uiState.update { it.copy(descripcion = v, errores = it.errores - CampoProducto.DESCRIPCION) }
    fun onPrecio(v: String) = _uiState.update { it.copy(precio = v, errores = it.errores - CampoProducto.PRECIO) }
    fun onStock(v: String) = _uiState.update { it.copy(stock = v, errores = it.errores - CampoProducto.STOCK) }
    fun onCategoria(v: String) = _uiState.update { it.copy(categoria = v) }
    fun limpiarError() = _uiState.update { it.copy(error = null) }

    // POST (nuevo) o PUT (edición)
    fun guardar() {
        viewModelScope.launch {
            val s = _uiState.value
            val idsExistentes = local.observarCatalogo().first().map { it.id }.toSet()
            val errores = ValidacionProducto.validar(
                s.id, s.titulo, s.descripcion, s.precio, s.stock, s.esNuevo, idsExistentes
            )
            if (errores.isNotEmpty()) {
                _uiState.update { it.copy(errores = errores) }
                return@launch
            }

            val producto = ProductoCacheEntity(
                id = s.id.trim().uppercase(),
                titulo = s.titulo.trim(),
                descripcion = s.descripcion.trim(),
                precio = ValidacionProducto.leerPrecio(s.precio)!!,
                imagenUrl = null,
                categoria = s.categoria,
                stock = ValidacionProducto.leerStock(s.stock)!!
            )
            ejecutar { if (s.esNuevo) remoto.crear(producto) else remoto.actualizar(producto) }
        }
    }

    // PUT con activo = false: el producto deja de verse en el catálogo
    fun desactivar() {
        val s = _uiState.value
        val producto = ProductoCacheEntity(
            id = s.id, titulo = s.titulo, descripcion = s.descripcion,
            precio = ValidacionProducto.leerPrecio(s.precio) ?: 0.0, imagenUrl = null,
            categoria = s.categoria, stock = ValidacionProducto.leerStock(s.stock) ?: 0
        )
        viewModelScope.launch { ejecutar { remoto.desactivar(producto) } }
    }

    // DELETE /api/productos/{id}
    fun eliminar() {
        val id = _uiState.value.id
        viewModelScope.launch { ejecutar { remoto.eliminar(id) } }
    }

    // Muestra "guardando", llama al backend y decide: volver atrás o mostrar el error
    private suspend fun ejecutar(llamada: suspend () -> Result<Unit>) {
        _uiState.update { it.copy(guardando = true, error = null) }
        llamada()
            .onSuccess { _uiState.update { it.copy(guardando = false, terminado = true) } }
            .onFailure { e -> _uiState.update { it.copy(guardando = false, error = mensajeDeError(e)) } }
    }

    companion object {
        fun factory(productoId: String?, local: LocalRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                FormularioProductoViewModel(productoId, local) as T
        }
    }
}