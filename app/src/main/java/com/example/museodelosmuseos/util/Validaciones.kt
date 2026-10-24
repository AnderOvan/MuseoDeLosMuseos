package com.example.museodelosmuseos.util

private val patronCorreo = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

fun validarNombre(nombre: String): String? =
    if (nombre.isBlank()) "Ingresa tu nombre completo" else null

fun validarCorreo(correo: String): String? =
    if (!patronCorreo.matches(correo)) "Ingresa un correo válido" else null

fun validarContrasena(contrasena: String): String? =
    if (contrasena.length < 6) "La contraseña debe tener al menos 6 caracteres" else null

fun limpiarRut(rut: String): String =
    rut.filter { it.isDigit() || it == 'k' || it == 'K' }.uppercase()

fun validarRut(rut: String): String? {
    val limpio = limpiarRut(rut)
    if (limpio.length < 8) return "Ingresa un RUT válido"

    val cuerpo = limpio.dropLast(1)
    val dv = limpio.last()
    if (!cuerpo.all { it.isDigit() }) return "Ingresa un RUT válido"

    var suma = 0
    var multiplicador = 2
    for (digito in cuerpo.reversed()) {
        suma += digito.digitToInt() * multiplicador
        multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
    }

    val resto = 11 - (suma % 11)
    val esperado = when (resto) {
        11 -> '0'
        10 -> 'K'
        else -> resto.digitToChar()
    }
    return if (dv == esperado) null else "El RUT no es válido"
}

fun formatearRut(rut: String): String {
    val limpio = limpiarRut(rut)
    if (limpio.length <= 1) return limpio
    val cuerpo = limpio.dropLast(1)
    val dv = limpio.last()
    val conPuntos = cuerpo.reversed().chunked(3).joinToString(".").reversed()
    return "$conPuntos-$dv"
}

fun validarDespacho(despacho: String): String? =
    if (despacho.trim().length < 5) "Ingresa un dirección de despacho válida" else null