package com.example.museodelosmuseos.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.museodelosmuseos.model.FavoritoEntity
import com.example.museodelosmuseos.model.LecturaQrEntity
import com.example.museodelosmuseos.model.ProductoCacheEntity
import com.example.museodelosmuseos.model.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    // ABORT: si el email o el RUT ya existen, la inserción falla
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE rut = :rut LIMIT 1")
    suspend fun buscarPorRut(rut: String): UsuarioEntity?

    // Devuelve el usuario solo si el email y la contraseña cifrada coinciden
    @Query("SELECT * FROM usuarios WHERE email = :email AND claveHash = :claveHash LIMIT 1")
    suspend fun autenticar(email: String, claveHash: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): UsuarioEntity?

    // Flow: la pantalla se actualiza sola si cambian los datos del usuario
    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    fun observarPorId(id: Long): Flow<UsuarioEntity?>
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