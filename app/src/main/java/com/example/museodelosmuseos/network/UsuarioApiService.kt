package com.example.museodelosmuseos.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// ---------- Usuarios en el backend (Spring Boot + MySQL) -> /api/usuarios ----------

// Lo que la app envía al registrarse (la contraseña viaja en texto y el backend la guarda con BCrypt)
data class UsuarioRequestDto(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("rut") val rut: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("contrasena") val contrasena: String,
    @SerializedName("despacho") val despacho: String,
    @SerializedName("rol") val rol: String,          // ADMINISTRADOR, INVESTIGADOR o CLIENTE
    @SerializedName("avatar") val avatar: Int
)

// Lo que responde el backend (nunca devuelve la contraseña)
data class UsuarioRespuestaDto(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("rut") val rut: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("despacho") val despacho: String?,
    @SerializedName("rol") val rol: String,
    @SerializedName("avatar") val avatar: Int,
    @SerializedName("fechaRegistro") val fechaRegistro: Long
)

// Lo que la app envía al iniciar sesión
data class LoginRequestDto(
    @SerializedName("correo") val correo: String,
    @SerializedName("contrasena") val contrasena: String
)

// Cuerpo de error del backend: { "status": 409, "mensaje": "El correo ya está registrado" }
data class ErrorApiDto(
    @SerializedName("status") val status: Int?,
    @SerializedName("mensaje") val mensaje: String?,
    @SerializedName("detalles") val detalles: Map<String, String>?
)

interface UsuarioApiService {

    // POST /api/usuarios -> 201 creado | 409 correo o RUT repetido | 400 datos inválidos
    @POST("api/usuarios")
    suspend fun registrar(@Body usuario: UsuarioRequestDto): UsuarioRespuestaDto

    // POST /api/auth/login -> 200 usuario | 401 credenciales inválidas
    @POST("api/auth/login")
    suspend fun login(@Body datos: LoginRequestDto): UsuarioRespuestaDto

    // GET /api/usuarios
    @GET("api/usuarios")
    suspend fun listar(): List<UsuarioRespuestaDto>
}