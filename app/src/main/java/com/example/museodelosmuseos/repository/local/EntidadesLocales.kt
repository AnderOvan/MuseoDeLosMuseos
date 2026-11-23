package com.example.museodelosmuseos.repository.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.museodelosmuseos.model.EstadoPedido
import com.example.museodelosmuseos.model.Rol
import com.example.museodelosmuseos.model.TipoNotificacion


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

// Solicitud de compra o inscripción confirmada en el carrito
@Entity(
    tableName = "pedidos",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["usuarioId"])]
)
data class PedidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val clienteNombre: String,      // para que el Administrador vea quién compró
    val numero: String,             // ej: MDM-04821
    val resumen: String,            // ej: "2 × Libro, 1 × Curso"
    val total: Double,
    val destino: String,
    val tipoEnvio: String,
    val estado: EstadoPedido = EstadoPedido.PENDIENTE,
    val fecha: Long = System.currentTimeMillis()
)

// Aviso (push simulado) guardado para cada usuario
@Entity(
    tableName = "notificaciones",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["usuarioId"])]
)
data class NotificacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val tipo: TipoNotificacion,
    val titulo: String,
    val mensaje: String,
    val leida: Boolean = false,
    val fecha: Long = System.currentTimeMillis()
)

// Ajustes de cada usuario (una fila por usuario)
@Entity(
    tableName = "preferencias",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PreferenciasEntity(
    @PrimaryKey val usuarioId: Long,
    val notificaciones: Boolean = true,
    val textoGrande: Boolean = false
)