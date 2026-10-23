package com.example.museodelosmuseos.modelo

enum class Rol(val titulo: String, val descripcion: String) {
    ADMINISTRADOR("Administrador", "Gestión completa del sistema"),
    INVESTIGADOR("Investigador", "Acceso a contenidos y proyectos"),
    CLIENTE("Cliente", "Explora, aprende y compra")
}