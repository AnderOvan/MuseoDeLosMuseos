package com.example.museodelosmuseos.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Tabla de usuarios. El email y el RUT no se pueden repetir.
@Entity(
    tableName = "usuarios",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["rut"], unique = true)
    ]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val rut: String,
    val email: String,
    val claveHash: String,        // Contraseña cifrada con SHA-256, nunca en texto plano
    val direccion: String,
    val rol: String = "CLIENTE",  // CLIENTE, ADMINISTRADOR o INVESTIGADOR
    val fechaRegistro: Long = System.currentTimeMillis()
)

// Museos favoritos de cada usuario. Un usuario no puede guardar dos veces el mismo museo.
@Entity(
    tableName = "favoritos",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE // Si se borra el usuario, se borran sus favoritos
        )
    ],
    indices = [Index(value = ["usuarioId", "museoId"], unique = true)]
)
data class FavoritoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val museoId: String,
    val fechaGuardado: Long = System.currentTimeMillis()
)

// Historial de códigos QR escaneados por cada usuario
@Entity(
    tableName = "lecturas_qr",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["usuarioId", "contenidoQr"])]
)
data class LecturaQrEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val contenidoQr: String,
    val tipo: String,          // PRODUCTO, PUBLICACION o DESCONOCIDO
    val descripcion: String,
    val fechaLectura: Long = System.currentTimeMillis()
)

// Copia local del catálogo, para mostrarlo sin conexión
@Entity(tableName = "productos_cache")
data class ProductoCacheEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val descripcion: String,
    val precio: Double,
    val imagenUrl: String? = null,
    val categoria: String,     // Libro, Merch o Curso
    val stock: Int
)