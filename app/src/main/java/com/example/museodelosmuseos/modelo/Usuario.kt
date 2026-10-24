package com.example.museodelosmuseos.modelo

data class Usuario(
    val nombre: String,
    val rut: String,
    val correo: String,
    val contrasena: String,
    val despacho: String = "",
    val rol: Rol = Rol.CLIENTE,
    val avatar: Int = 0
)