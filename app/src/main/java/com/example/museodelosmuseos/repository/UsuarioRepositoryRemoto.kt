package com.example.museodelosmuseos.repository

import com.example.museodelosmuseos.model.Usuario
import com.example.museodelosmuseos.network.ErrorApiDto
import com.example.museodelosmuseos.network.LoginRequestDto
import com.example.museodelosmuseos.network.RetrofitInstance
import com.example.museodelosmuseos.network.UsuarioApiService
import com.example.museodelosmuseos.network.UsuarioRequestDto
import com.example.museodelosmuseos.network.UsuarioRespuestaDto
import com.google.gson.Gson
import retrofit2.HttpException

// Registro e inicio de sesión contra el backend (tabla "usuarios" de MySQL).
class UsuarioRepositoryRemoto(
    private val api: UsuarioApiService = RetrofitInstance.usuarioApi
) {

    // POST /api/usuarios
    suspend fun registrar(usuario: Usuario): Result<UsuarioRespuestaDto> = llamadaSegura {
        api.registrar(
            UsuarioRequestDto(
                nombre = usuario.nombre.trim(),
                rut = usuario.rut,
                correo = usuario.correo.trim().lowercase(),
                contrasena = usuario.contrasena,
                despacho = usuario.despacho.trim(),
                rol = usuario.rol.name,
                avatar = usuario.avatar
            )
        )
    }

    // POST /api/auth/login (el backend compara la contraseña con BCrypt)
    suspend fun login(correo: String, clave: String): Result<UsuarioRespuestaDto> = llamadaSegura {
        api.login(LoginRequestDto(correo.trim().lowercase(), clave))
    }
}

// Lee el mensaje que manda el backend en un error HTTP (ej: "El correo ya está registrado").
// Si no hay cuerpo legible, devuelve null.
fun mensajeDelBackend(e: Throwable): String? {
    if (e !is HttpException) return null
    val cuerpo = e.response()?.errorBody()?.string() ?: return null
    return runCatching {
        val error = Gson().fromJson(cuerpo, ErrorApiDto::class.java)
        error.detalles?.values?.joinToString("\n") ?: error.mensaje
    }.getOrNull()
}