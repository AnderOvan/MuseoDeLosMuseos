package com.example.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.repository.FakeProductoRepository
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.repository.local.ProductoCacheEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.Normalizer

// Chips de categoría: el texto que se muestra y el valor guardado en la base
enum class CategoriaCatalogo(val titulo: String, val valor: String?) {
    TODOS("Todos", null),
    CURSOS("Cursos", "Curso"),
    LIBROS("Libros", "Libro"),
    MATERIALES("Materiales", "Material")
}

data class CatalogoUiState(
    val categoria: CategoriaCatalogo = CategoriaCatalogo.TODOS,
    val productos: List<ProductoCacheEntity> = emptyList(), // ya filtrados
    val total: Int = 0,                                     // artículos en la caché
    val sincronizando: Boolean = true
)

class CatalogoViewModel(private val local: LocalRepository) : ViewModel() {

    // El texto de búsqueda va aparte para que el campo responda al instante al escribir
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _categoria = MutableStateFlow(CategoriaCatalogo.TODOS)
    private val _sincronizando = MutableStateFlow(true)

    // Lee la caché de Room y aplica búsqueda + categoría cada vez que algo cambia
    val uiState: StateFlow<CatalogoUiState> = combine(
        local.observarCatalogo(),
        _query,
        _categoria,
        _sincronizando
    ) { todos, query, categoria, sincronizando ->
        val texto = normalizar(query.trim())
        val filtrados = todos.filter { producto ->
            (categoria.valor == null || producto.categoria == categoria.valor) &&
                    (texto.isEmpty() ||
                            normalizar(producto.titulo).contains(texto) ||
                            normalizar(producto.descripcion).contains(texto))
        }
        CatalogoUiState(
            categoria = categoria,
            productos = filtrados,
            total = todos.size,
            sincronizando = sincronizando && todos.isEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogoUiState())

    init {
        // Precarga el catálogo en la tabla productos_cache (simula la sincronización)
        viewModelScope.launch {
            FakeProductoRepository.sincronizar(local)
            _sincronizando.value = false
        }
    }

    fun onQueryCambia(texto: String) {
        _query.value = texto
    }

    fun onCategoria(categoria: CategoriaCatalogo) {
        _categoria.value = categoria
    }

    companion object {
        // "Curaduría" y "curaduria" se consideran iguales al buscar
        private fun normalizar(texto: String): String =
            Normalizer.normalize(texto.lowercase(), Normalizer.Form.NFD)
                .replace(Regex("\\p{M}"), "")

        fun factory(local: LocalRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CatalogoViewModel(local) as T
        }
    }
}