package com.example.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.model.Museo
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.repository.MuseoRepository
import com.example.museodelosmuseos.util.GeoUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetalleMuseoUiState(
    val cargando: Boolean = true,
    val museo: Museo? = null,
    val distanciaKm: Double = 0.0,
    val esFavorito: Boolean = false, // viene de la tabla "favoritos" de Room
    val haySesion: Boolean = false
)

class DetalleMuseoViewModel(
    private val museoId: String,
    private val museoRepository: MuseoRepository,
    private val localRepository: LocalRepository
) : ViewModel() {

    private val _museo = MutableStateFlow<Museo?>(null)
    private val _cargando = MutableStateFlow(true)

    // Junta los datos del museo con los favoritos del usuario activo.
    // Si se agrega o quita el favorito en Room, la pantalla se actualiza sola.
    val uiState: StateFlow<DetalleMuseoUiState> = combine(
        _museo,
        _cargando,
        localRepository.observarFavoritos(),
        localRepository.usuarioActivoId
    ) { museo, cargando, favoritos, usuarioId ->
        DetalleMuseoUiState(
            cargando = cargando,
            museo = museo,
            distanciaKm = museo?.let { GeoUtils.distanciaDesdeOrigenKm(it.latitud, it.longitud) } ?: 0.0,
            esFavorito = favoritos.any { it.museoId == museoId },
            haySesion = usuarioId != null
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalleMuseoUiState())

    init {
        // Busca el museo en la API (ficticia)
        viewModelScope.launch {
            museoRepository.obtenerMuseos().onSuccess { lista ->
                _museo.value = lista.find { it.id == museoId }
            }
            _cargando.value = false
        }
    }

    // Agrega o quita el museo de los favoritos del usuario activo (tabla FavoritoEntity)
    fun alternarFavorito(museoId: String) {
        viewModelScope.launch { localRepository.alternarFavorito(museoId) }
    }

    companion object {
        fun factory(
            museoId: String,
            museoRepository: MuseoRepository,
            localRepository: LocalRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                DetalleMuseoViewModel(museoId, museoRepository, localRepository) as T
        }
    }
}