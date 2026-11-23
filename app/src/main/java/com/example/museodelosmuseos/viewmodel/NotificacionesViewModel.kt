package com.example.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.model.Rol
import com.example.museodelosmuseos.model.TipoNotificacion
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.repository.local.NotificacionEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotificacionesUiState(
    val notificaciones: List<NotificacionEntity> = emptyList(),
    val sinLeer: Int = 0,
    val esAdmin: Boolean = false,
    val activas: Boolean = true      // ajuste "Notificaciones" del Perfil
)

// Avisos de prueba que puede enviar el Administrador (push simulado)
private val AVISOS_DE_PRUEBA = listOf(
    Triple(TipoNotificacion.CURSO, "Nuevo curso disponible", "Se abrió la inscripción a un nuevo curso. Revisa el catálogo."),
    Triple(TipoNotificacion.MUSEO, "Museo incorporado", "Se sumó un museo virtual al mapa de Iberoamérica."),
    Triple(TipoNotificacion.AVISO, "Aviso de prueba", "Esta es una notificación push simulada enviada por el Administrador.")
)

class NotificacionesViewModel(private val local: LocalRepository) : ViewModel() {

    val uiState: StateFlow<NotificacionesUiState> = combine(
        local.observarNotificaciones(),
        local.usuarioActivo,
        local.preferencias
    ) { notificaciones, usuario, preferencias ->
        NotificacionesUiState(
            notificaciones = notificaciones,
            sinLeer = notificaciones.count { !it.leida },
            esAdmin = usuario?.rol == Rol.ADMINISTRADOR,
            activas = preferencias.notificaciones
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificacionesUiState())

    fun marcarLeida(notificacion: NotificacionEntity) {
        if (notificacion.leida) return
        viewModelScope.launch { local.marcarNotificacionLeida(notificacion.id) }
    }

    fun marcarTodasLeidas() {
        viewModelScope.launch { local.marcarTodasLeidas() }
    }

    fun enviarAvisoDePrueba() {
        val (tipo, titulo, mensaje) = AVISOS_DE_PRUEBA.random()
        viewModelScope.launch { local.enviarAvisoATodos(tipo, titulo, mensaje) }
    }

    companion object {
        fun factory(local: LocalRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                NotificacionesViewModel(local) as T
        }
    }
}