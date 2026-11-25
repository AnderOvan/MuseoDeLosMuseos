package com.example.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.model.Rol
import com.example.museodelosmuseos.model.Usuario
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.repository.ResultadoAuth
import com.example.museodelosmuseos.repository.UsuarioRepositoryRemoto
import com.example.museodelosmuseos.repository.mensajeDelBackend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

// Une las pantallas de Login y Registro con el backend (MySQL) y la base local (Room).
// El backend es la fuente oficial; Room guarda una copia para la sesión y el modo sin conexión.
// Las pantallas solo muestran datos; este ViewModel decide qué hacer.
class AuthViewModel(
    private val repo: LocalRepository,
    // Backend: el registro oficial queda en la tabla "usuarios" de MySQL
    private val remoto: UsuarioRepositoryRemoto = UsuarioRepositoryRemoto()
) : ViewModel() {

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
            // 1. Registro en el backend: POST /api/usuarios (MySQL, contraseña con BCrypt)
            val enServidor = remoto.registrar(usuario)
            if (enServidor.isFailure) {
                procesando = false
                val e = enServidor.exceptionOrNull()!!
                // 409 -> "El correo ya está registrado" / "El RUT ya está registrado"
                _error.value = mensajeDelBackend(e) ?: mensajeDeError(e)
                return@launch
            }

            // 2. Copia local en Room: inicia la sesión en el celular
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
                // Ya existía en este celular: se actualiza con los datos del backend
                is ResultadoAuth.Error -> {
                    repo.guardarUsuarioDelBackend(enServidor.getOrThrow(), usuario.contrasena)
                    onExito()
                }
            }
        }
    }

    fun iniciarSesion(correo: String, clave: String, rol: Rol, onExito: () -> Unit) {
        if (procesando) return
        procesando = true
        _error.value = null

        viewModelScope.launch {
            // 1. Login en el backend: POST /api/auth/login (MySQL)
            val enServidor = remoto.login(correo, clave)
            val falla = enServidor.exceptionOrNull()

            val resultado: ResultadoAuth = when {
                // Backend OK: se guarda/actualiza la copia en Room y se inicia la sesión local
                enServidor.isSuccess ->
                    repo.guardarUsuarioDelBackend(enServidor.getOrThrow(), clave)

                // Sin conexión: se intenta con la copia local (modo offline)
                falla is IOException -> repo.iniciarSesion(correo, clave)

                // 401 u otro error del backend: se muestra su mensaje
                else -> ResultadoAuth.Error(mensajeDelBackend(falla!!) ?: mensajeDeError(falla))
            }

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