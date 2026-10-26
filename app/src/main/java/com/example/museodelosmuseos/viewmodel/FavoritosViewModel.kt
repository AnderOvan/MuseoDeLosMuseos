package com.example.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.model.Museo
import com.example.museodelosmuseos.repository.CatalogoQr
import com.example.museodelosmuseos.repository.FakeMuseoRepository
import com.example.museodelosmuseos.repository.FichaQr
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.util.TipoContenidoQr
import com.example.museodelosmuseos.util.ValidadorQr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Museo guardado en la tabla "favoritos" + sus datos completos
data class MuseoFavorito(val museo: Museo, val fechaGuardado: Long)

// Publicación o producto escaneado con QR (viene de la tabla "lecturas_qr")
data class RecursoGuardado(val ficha: FichaQr, val fecha: Long)

enum class PestanaFavoritos { MUSEOS, RECURSOS }

data class FavoritosUiState(
    val museos: List<MuseoFavorito> = emptyList(),
    val recursos: List<RecursoGuardado> = emptyList(),
    val pestana: PestanaFavoritos = PestanaFavoritos.MUSEOS,
    val haySesion: Boolean = false
)

class FavoritosViewModel(private val local: LocalRepository) : ViewModel() {

    private val _pestana = MutableStateFlow(PestanaFavoritos.MUSEOS)

    // Todo viene de Room y es solo del usuario activo: cada perfil ve su propia colección
    val uiState: StateFlow<FavoritosUiState> = combine(
        local.observarFavoritos(),
        local.observarHistorialQr(),
        local.usuarioActivoId,
        _pestana
    ) { favoritos, historial, usuarioId, pestana ->
        FavoritosUiState(
            // Se cruzan los ids guardados en Room con los datos de los museos
            museos = favoritos.mapNotNull { favorito ->
                FakeMuseoRepository.buscarPorId(favorito.museoId)?.let { MuseoFavorito(it, favorito.fechaGuardado) }
            },
            // Recursos = publicaciones y productos escaneados (sin museos, sin repetir)
            recursos = historial
                .filter { it.tipo != TipoContenidoQr.MUSEO.name }
                .mapNotNull { lectura ->
                    CatalogoQr.buscar(ValidadorQr.analizar(lectura.contenidoQr))
                        ?.let { RecursoGuardado(it, lectura.fechaLectura) }
                }
                .distinctBy { it.ficha.tipo to it.ficha.codigo },
            pestana = pestana,
            haySesion = usuarioId != null
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritosUiState())

    fun cambiarPestana(pestana: PestanaFavoritos) {
        _pestana.value = pestana
    }

    // Quita el museo de la tabla "favoritos" de Room
    fun eliminarFavorito(museoId: String) {
        viewModelScope.launch { local.eliminarFavorito(museoId) }
    }

    // Para el botón "Deshacer"
    fun restaurarFavorito(museoId: String) {
        viewModelScope.launch { local.agregarFavorito(museoId) }
    }

    companion object {
        fun factory(local: LocalRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                FavoritosViewModel(local) as T
        }
    }
}