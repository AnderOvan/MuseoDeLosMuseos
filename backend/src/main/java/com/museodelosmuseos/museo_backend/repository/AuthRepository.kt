package com.example.museodelosmuseos.repository

import com.example.museodelosmuseos.api.RetrofitService
import com.example.museodelosmuseos.api.dto.LoginRequestDto
import com.example.museodelosmuseos.api.dto.UsuarioRequestDto
import com.example.museodelosmuseos.api.dto.UsuarioResponseDto
import com.example.museodelosmuseos.api.mensajeDeError
import com.example.museodelosmuseos.model.Rol
import com.example.museodelosmuseos.repository.local.UsuarioEntity
import kotlin.coroutines.cancellation.CancellationException

// Registro e inicio de sesión contra el microservicio.
// El resultado se guarda en Room (caché) y se deja la sesión iniciada.
class AuthRepository(
    private val api: RetrofitService,
    private val local: LocalRepository
) {

    suspend fun registrar(
        nombre: String,
        rut: String,
        correo: String,
        clave: String,
        despacho: String,
        rol: Rol,
        avatar: Int
    ): ResultadoAuth = try {
        val respuesta = api.registrar(
            UsuarioRequestDto(
                nombre = nombre.trim(),
                rut = rut,
                correo = correo.trim().lowercase(),
                contrasena = clave,
                despacho = despacho.trim(),
                rol = rol,
                avatar = avatar
            )
        )
        val usuario = respuesta.aEntidad()
        local.guardarSesionRemota(usuario, esNuevo = true)
        ResultadoAuth.Exito(usuario)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        ResultadoAuth.Error(mensajeDeError(e))
    }

    suspend fun iniciarSesion(correo: String, clave: String): ResultadoAuth = try {
        val respuesta = api.login(LoginRequestDto(correo.trim().lowercase(), clave))
        val usuario = respuesta.aEntidad()
        local.guardarSesionRemota(usuario, esNuevo = false)
        ResultadoAuth.Exito(usuario)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        ResultadoAuth.Error(mensajeDeError(e))
    }

    // El backend no devuelve la contraseña, por eso claveHash queda vacío
    private fun UsuarioResponseDto.aEntidad() = UsuarioEntity(
        id = id,
        nombre = nombre,
        rut = rut,
        correo = correo,
        claveHash = "",
        despacho = despacho ?: "",
        rol = rol ?: Rol.CLIENTE,
        avatar = avatar,
        fechaRegistro = fechaRegistro
    )
}