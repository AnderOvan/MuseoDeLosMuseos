package com.example.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.model.Museo
import com.example.museodelosmuseos.repository.MuseoRepository
import com.example.museodelosmuseos.util.GeoUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Museo + su distancia (Haversine) desde el centro de Santiago
data class MuseoConDistancia(
    val museo: Museo,
    val distanciaKm: Double
)

data class MapaUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val todos: List<MuseoConDistancia> = emptyList(),
    val paisSeleccionado: String? = null,     // null = todos los países
    val proyectoSeleccionado: String? = null, // null = todos los proyectos
    val seleccionadoId: String? = null
) {
    val paises: List<String> get() = todos.map { it.museo.pais }.distinct().sorted()
    val proyectos: List<String> get() = todos.map { it.museo.proyecto }.distinct().sorted()
    val sinFiltros: Boolean get() = paisSeleccionado == null && proyectoSeleccionado == null

    // Museos que pasan los filtros: son los que se dibujan en el mapa
    val visibles: List<MuseoConDistancia>
        get() = todos.filter {
            (paisSeleccionado == null || it.museo.pais == paisSeleccionado) &&
                    (proyectoSeleccionado == null || it.museo.proyecto == proyectoSeleccionado)
        }

    val seleccionado: MuseoConDistancia? get() = visibles.find { it.museo.id == seleccionadoId }
}

class MapaViewModel(private val repo: MuseoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MapaUiState())
    val uiState: StateFlow<MapaUiState> = _uiState.asStateFlow()

    init {
        cargarMuseos()
    }

    // Consulta la API (ficticia) y calcula la distancia de cada museo
    fun cargarMuseos() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            repo.obtenerMuseos()
                .onSuccess { lista ->
                    val conDistancia = lista
                        .map { museo ->
                            MuseoConDistancia(
                                museo,
                                GeoUtils.distanciaDesdeOrigenKm(museo.latitud, museo.longitud)
                            )
                        }
                        .sortedBy { it.distanciaKm }
                    _uiState.update { it.copy(cargando = false, todos = conDistancia) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(cargando = false, error = "No se pudieron cargar los museos: ${e.message}")
                    }
                }
        }
    }

    fun mostrarTodos() = _uiState.update {
        it.copy(paisSeleccionado = null, proyectoSeleccionado = null, seleccionadoId = null)
    }

    fun filtrarPorPais(pais: String?) = _uiState.update {
        it.copy(paisSeleccionado = pais, seleccionadoId = null)
    }

    fun filtrarPorProyecto(proyecto: String?) = _uiState.update {
        it.copy(proyectoSeleccionado = proyecto, seleccionadoId = null)
    }

    // Al tocar un marcador se abre su tarjeta (null la cierra)
    fun seleccionarMuseo(id: String?) = _uiState.update { it.copy(seleccionadoId = id) }

    companion object {
        fun factory(repo: MuseoRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MapaViewModel(repo) as T
        }
    }
}