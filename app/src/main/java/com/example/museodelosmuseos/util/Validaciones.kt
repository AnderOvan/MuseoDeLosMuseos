package com.example.museodelosmuseos.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

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

class RutVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.length <= 1) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val body = raw.dropLast(1)
        val dv = raw.last()

        val bodyLen = body.length
        val dotPositions = mutableSetOf<Int>()
        for (i in 0 until bodyLen) {
            val distFromRight = bodyLen - 1 - i
            if (distFromRight > 0 && distFromRight % 3 == 0) {
                dotPositions.add(i)
            }
        }

        val formattedBody = StringBuilder()
        val origToTrans = IntArray(raw.length + 1)

        var currentTransformedIndex = 0
        for (i in 0 until bodyLen) {
            origToTrans[i] = currentTransformedIndex
            formattedBody.append(body[i])
            currentTransformedIndex++
            if (i in dotPositions) {
                formattedBody.append('.')
                currentTransformedIndex++
            }
        }

        origToTrans[bodyLen] = currentTransformedIndex
        formattedBody.append('-')
        currentTransformedIndex++
        formattedBody.append(dv)
        currentTransformedIndex++

        origToTrans[raw.length] = currentTransformedIndex

        val transformedStr = formattedBody.toString()
        val transToOrig = IntArray(transformedStr.length + 1)

        var origIdx = 0
        for (i in 0..transformedStr.length) {
            while (origIdx < raw.length && origToTrans[origIdx + 1] <= i) {
                origIdx++
            }
            transToOrig[i] = origIdx
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return origToTrans[offset.coerceIn(0, raw.length)]
            }

            override fun transformedToOriginal(offset: Int): Int {
                return transToOrig[offset.coerceIn(0, transformedStr.length)]
            }
        }

        return TransformedText(AnnotatedString(transformedStr), offsetMapping)
    }
}