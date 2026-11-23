package com.example.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.repository.local.PreferenciasEntity
import com.example.museodelosmuseos.repository.local.UsuarioEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PerfilUiState(
    val usuario: UsuarioEntity? = null,   // null = sin sesión
    val preferencias: PreferenciasEntity = PreferenciasEntity(usuarioId = 0)
)

class PerfilViewModel(private val local: LocalRepository) : ViewModel() {

    val uiState: StateFlow<PerfilUiState> = combine(
        local.usuarioActivo,
        local.preferencias
    ) { usuario, preferencias ->
        PerfilUiState(usuario, preferencias)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PerfilUiState())

    // Guarda los datos editados en Room; la pantalla se actualiza sola
    fun guardarPerfil(nombre: String, despacho: String, avatar: Int) {
        viewModelScope.launch { local.actualizarPerfil(nombre, despacho, avatar) }
    }

    fun cambiarNotificaciones(activas: Boolean) {
        viewModelScope.launch { local.cambiarPreferencias { it.copy(notificaciones = activas) } }
    }

    fun cambiarTextoGrande(activo: Boolean) {
        viewModelScope.launch { local.cambiarPreferencias { it.copy(textoGrande = activo) } }
    }

    fun cerrarSesion() = local.cerrarSesion()

    companion object {
        fun factory(local: LocalRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PerfilViewModel(local) as T
        }
    }
}