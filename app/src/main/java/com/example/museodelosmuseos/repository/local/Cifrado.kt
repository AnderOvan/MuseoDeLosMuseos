package cl.duoc.museodelosmuseos.repository.local

import java.security.MessageDigest

// Convierte la contraseña en un hash SHA-256.
// En la base se guarda el hash; la contraseña real nunca se guarda.
object Cifrado {
    fun sha256(texto: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(texto.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}