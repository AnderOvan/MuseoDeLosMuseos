package com.example.museodelosmuseos.repository.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Recuerda qué usuario inició sesión, aunque se cierre y se vuelva a abrir la app
class SesionManager private constructor(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("sesion_museo", Context.MODE_PRIVATE)

    private val _usuarioActivoId = MutableStateFlow(
        prefs.getLong(KEY_USUARIO_ID, SIN_USUARIO).takeIf { it != SIN_USUARIO }
    )
    val usuarioActivoId: StateFlow<Long?> = _usuarioActivoId.asStateFlow()

    fun iniciarSesion(usuarioId: Long) {
        prefs.edit().putLong(KEY_USUARIO_ID, usuarioId).apply()
        _usuarioActivoId.value = usuarioId
    }

    fun cerrarSesion() {
        prefs.edit().remove(KEY_USUARIO_ID).apply()
        _usuarioActivoId.value = null
    }

    companion object {
        private const val KEY_USUARIO_ID = "usuario_activo_id"
        private const val SIN_USUARIO = -1L

        @Volatile
        private var INSTANCE: SesionManager? = null

        fun getInstance(context: Context): SesionManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SesionManager(context).also { INSTANCE = it }
            }
    }
}