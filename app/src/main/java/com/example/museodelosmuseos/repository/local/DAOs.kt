package com.example.museodelosmuseos.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    // ABORT: si el correo o el RUT ya existen, la inserción falla
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE rut = :rut LIMIT 1")
    suspend fun buscarPorRut(rut: String): UsuarioEntity?

    // Devuelve el usuario solo si el correo y la contraseña cifrada coinciden
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND claveHash = :claveHash LIMIT 1")
    suspend fun autenticar(correo: String, claveHash: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): UsuarioEntity?

    // Flow: la pantalla se actualiza sola si cambian los datos del usuario
    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    fun observarPorId(id: Long): Flow<UsuarioEntity?>

    // Perfil: solo se pueden editar estos tres campos (correo y RUT quedan fijos)
    @Query("UPDATE usuarios SET nombre = :nombre, despacho = :despacho, avatar = :avatar WHERE id = :id")
    suspend fun actualizarPerfil(id: Long, nombre: String, despacho: String, avatar: Int)

    // Para que el Administrador pueda enviar un aviso a todos
    @Query("SELECT id FROM usuarios")
    suspend fun listarIds(): List<Long>
}



@Dao
interface FavoritoDao {
    // IGNORE: si el museo ya era favorito, no hace nada
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(favorito: FavoritoEntity): Long

    @Query("DELETE FROM favoritos WHERE usuarioId = :usuarioId AND museoId = :museoId")
    suspend fun eliminar(usuarioId: Long, museoId: String)

    @Query("SELECT * FROM favoritos WHERE usuarioId = :usuarioId ORDER BY fechaGuardado DESC")
    fun observarPorUsuario(usuarioId: Long): Flow<List<FavoritoEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favoritos WHERE usuarioId = :usuarioId AND museoId = :museoId)")
    suspend fun esFavorito(usuarioId: Long, museoId: String): Boolean
}

@Dao
interface LecturaQrDao {
    @Insert
    suspend fun insertar(lectura: LecturaQrEntity): Long

    // true si el usuario ya escaneó este mismo QR desde la fecha "desde"
    @Query(
        "SELECT EXISTS(SELECT 1 FROM lecturas_qr " +
                "WHERE usuarioId = :usuarioId AND contenidoQr = :contenidoQr AND fechaLectura >= :desde)"
    )
    suspend fun existeLecturaReciente(usuarioId: Long, contenidoQr: String, desde: Long): Boolean

    @Query("SELECT * FROM lecturas_qr WHERE usuarioId = :usuarioId ORDER BY fechaLectura DESC")
    fun observarPorUsuario(usuarioId: Long): Flow<List<LecturaQrEntity>>
}

@Dao
interface ProductoCacheDao {
    // Upsert: inserta los productos nuevos y actualiza los que ya existen
    @Upsert
    suspend fun guardarTodos(productos: List<ProductoCacheEntity>)

    @Query("SELECT * FROM productos_cache ORDER BY titulo")
    fun observarTodos(): Flow<List<ProductoCacheEntity>>

    @Query("SELECT * FROM productos_cache WHERE categoria = :categoria ORDER BY titulo")
    fun observarPorCategoria(categoria: String): Flow<List<ProductoCacheEntity>>

    @Query("SELECT * FROM productos_cache WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): ProductoCacheEntity?

    @Query("DELETE FROM productos_cache")
    suspend fun limpiar()
}

@Dao
interface PedidoDao {
    @Insert
    suspend fun insertar(pedido: PedidoEntity): Long

    // Cliente: solo sus pedidos
    @Query("SELECT * FROM pedidos WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    fun observarPorUsuario(usuarioId: Long): Flow<List<PedidoEntity>>

    // Administrador: todos los pedidos recibidos
    @Query("SELECT * FROM pedidos ORDER BY fecha DESC")
    fun observarTodos(): Flow<List<PedidoEntity>>

    // El estado se guarda como texto (PENDIENTE, CONFIRMADO...)
    @Query("UPDATE pedidos SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Long, estado: String)
}

@Dao
interface NotificacionDao {
    @Insert
    suspend fun insertar(notificacion: NotificacionEntity): Long

    @Query("SELECT * FROM notificaciones WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    fun observarPorUsuario(usuarioId: Long): Flow<List<NotificacionEntity>>

    @Query("UPDATE notificaciones SET leida = 1 WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun marcarLeida(id: Long, usuarioId: Long)

    @Query("UPDATE notificaciones SET leida = 1 WHERE usuarioId = :usuarioId")
    suspend fun marcarTodasLeidas(usuarioId: Long)
}

@Dao
interface PreferenciasDao {
    // Upsert: crea la fila la primera vez y la actualiza después
    @Upsert
    suspend fun guardar(preferencias: PreferenciasEntity)

    @Query("SELECT * FROM preferencias WHERE usuarioId = :usuarioId LIMIT 1")
    suspend fun obtener(usuarioId: Long): PreferenciasEntity?

    @Query("SELECT * FROM preferencias WHERE usuarioId = :usuarioId LIMIT 1")
    fun observar(usuarioId: Long): Flow<PreferenciasEntity?>
}
