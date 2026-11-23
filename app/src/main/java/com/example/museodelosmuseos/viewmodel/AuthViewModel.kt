package com.example.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.model.Rol
import com.example.museodelosmuseos.model.Usuario
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.repository.ResultadoAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Une las pantallas de Login y Registro con la base de datos (Room).
// Las pantallas solo muestran datos; este ViewModel decide qué hacer.
class AuthViewModel(private val repo: LocalRepository) : ViewModel() {

    // Mensaje de error que viene de la base de datos (correo repetido, clave incorrecta...)
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Evita que un doble clic registre o inicie sesión dos veces
    private var procesando = false

    fun registrar(usuario: Usuario, onExito: () -> Unit) {
        if (procesando) return
        procesando = true
        _error.value = null

        viewModelScope.launch {
            val resultado = repo.registrarUsuario(
                nombre = usuario.nombre,
                rut = usuario.rut,
                correo = usuario.correo,
                clave = usuario.contrasena,
                despacho = usuario.despacho,
                rol = usuario.rol,
                avatar = usuario.avatar
            )
            procesando = false
            when (resultado) {
                is ResultadoAuth.Exito -> onExito()
                is ResultadoAuth.Error -> _error.value = resultado.mensaje
            }
        }
    }

    fun iniciarSesion(correo: String, clave: String, rol: Rol, onExito: () -> Unit) {
        if (procesando) return
        procesando = true
        _error.value = null

        viewModelScope.launch {
            val resultado = repo.iniciarSesion(correo, clave)
            procesando = false
            when (resultado) {
                is ResultadoAuth.Exito ->
                    // La cuenta existe, pero hay que revisar que sea del tipo elegido
                    if (resultado.usuario.rol == rol) {
                        onExito()
                    } else {
                        repo.cerrarSesion()
                        _error.value = "Esta cuenta no es de tipo ${rol.titulo}"
                    }

                is ResultadoAuth.Error -> _error.value = resultado.mensaje
            }
        }
    }

    fun limpiarError() {
        _error.value = null
    }

    companion object {
        fun factory(repo: LocalRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AuthViewModel(repo) as T
        }
    }
}