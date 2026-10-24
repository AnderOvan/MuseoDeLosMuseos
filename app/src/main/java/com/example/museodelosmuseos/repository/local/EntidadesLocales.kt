package com.example.museodelosmuseos.repository.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.museodelosmuseos.model.Rol


@Entity(
    tableName = "usuarios",
    indices = [
        Index(value = ["correo"], unique = true),
        Index(value = ["rut"], unique = true)
    ]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val rut: String,
    val correo: String,
    val claveHash: String,
    val despacho: String,
    val rol: Rol = Rol.CLIENTE,
    val avatar: Int = 0,
    val fechaRegistro: Long = System.currentTimeMillis()
)


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