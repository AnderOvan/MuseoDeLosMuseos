package cl.duoc.museodelosmuseos.repository

import android.content.Context
import cl.duoc.museodelosmuseos.model.FavoritoEntity
import cl.duoc.museodelosmuseos.model.LecturaQrEntity
import cl.duoc.museodelosmuseos.model.ProductoCacheEntity
import cl.duoc.museodelosmuseos.model.UsuarioEntity
import cl.duoc.museodelosmuseos.repository.local.AppDatabase
import cl.duoc.museodelosmuseos.repository.local.Cifrado
import cl.duoc.museodelosmuseos.repository.local.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

// Resultado de registrarse o iniciar sesión
sealed interface ResultadoAuth {
    data class Exito(val usuario: UsuarioEntity) : ResultadoAuth
    data class Error(val mensaje: String) : ResultadoAuth
}

// Resultado de guardar un QR escaneado
enum class ResultadoQr { GUARDADA, DUPLICADA, SIN_SESION }

// Punto único de acceso a los datos locales.
// Los ViewModels usan esta clase y nunca tocan Room directamente.
@OptIn(ExperimentalCoroutinesApi::class)
class LocalRepository(
    private val db: AppDatabase,
    private val session: SessionManager
) {
    val usuarioActivoId: StateFlow<Long?> = session.usuarioActivoId

    // Datos del usuario con sesión iniciada (null si nadie inició sesión)
    val usuarioActivo: Flow<UsuarioEntity?> =
        usuarioActivoId.flatMapLatest { id ->
            if (id == null) flowOf(null) else db.usuarioDao().observarPorId(id)
        }

    // ---------- Usuarios ----------

    suspend fun registrarUsuario(
        nombre: String,
        rut: String,
        email: String,
        clave: String,
        direccion: String,
        rol: String = "CLIENTE"
    ): ResultadoAuth {
        val dao = db.usuarioDao()
        val correo = email.trim().lowercase()

        if (dao.buscarPorEmail(correo) != null) {
            return ResultadoAuth.Error("Este correo ya está registrado")
        }
        if (dao.buscarPorRut(rut) != null) {
            return ResultadoAuth.Error("Este RUT ya está registrado")
        }

        val nuevo = UsuarioEntity(
            nombre = nombre.trim(),
            rut = rut,
            email = correo,
            claveHash = Cifrado.sha256(clave),
            direccion = direccion.trim(),
            rol = rol
        )
        val id = dao.insertar(nuevo)
        session.iniciarSesion(id)
        return ResultadoAuth.Exito(nuevo.copy(id = id))
    }

    suspend fun iniciarSesion(email: String, clave: String): ResultadoAuth {
        val dao = db.usuarioDao()
        val correo = email.trim().lowercase()

        if (dao.buscarPorEmail(correo) == null) {
            return ResultadoAuth.Error("No existe una cuenta con este correo")
        }
        val usuario = dao.autenticar(correo, Cifrado.sha256(clave))
            ?: return ResultadoAuth.Error("Contraseña incorrecta")

        session.iniciarSesion(usuario.id)
        return ResultadoAuth.Exito(usuario)
    }

    fun cerrarSesion() = session.cerrarSesion()

    // ---------- Favoritos (solo del usuario activo) ----------

    fun observarFavoritos(): Flow<List<FavoritoEntity>> =
        usuarioActivoId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else db.favoritoDao().observarPorUsuario(id)
        }

    // Devuelve false si no hay sesión iniciada
    suspend fun agregarFavorito(museoId: String): Boolean {
        val id = usuarioActivoId.value ?: return false
        db.favoritoDao().insertar(FavoritoEntity(usuarioId = id, museoId = museoId))
        return true
    }

    suspend fun eliminarFavorito(museoId: String) {
        val id = usuarioActivoId.value ?: return
        db.favoritoDao().eliminar(id, museoId)
    }

    // Si ya era favorito lo quita; si no, lo agrega (para el botón ❤)
    suspend fun alternarFavorito(museoId: String) {
        val id = usuarioActivoId.value ?: return
        if (db.favoritoDao().esFavorito(id, museoId)) eliminarFavorito(museoId)
        else agregarFavorito(museoId)
    }

    // ---------- Historial QR (solo del usuario activo) ----------

    fun observarHistorialQr(): Flow<List<LecturaQrEntity>> =
        usuarioActivoId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else db.lecturaQrDao().observarPorUsuario(id)
        }

    // Guarda la lectura, salvo que el mismo usuario haya escaneado ese QR hace poco
    suspend fun registrarLecturaQr(
        contenidoQr: String,
        tipo: String,
        descripcion: String
    ): ResultadoQr {
        val id = usuarioActivoId.value ?: return ResultadoQr.SIN_SESION
        val dao = db.lecturaQrDao()
        val contenido = contenidoQr.trim()
        val desde = System.currentTimeMillis() - VENTANA_DUPLICADO_MS

        if (dao.existeLecturaReciente(id, contenido, desde)) return ResultadoQr.DUPLICADA

        dao.insertar(
            LecturaQrEntity(
                usuarioId = id,
                contenidoQr = contenido,
                tipo = tipo,
                descripcion = descripcion
            )
        )
        return ResultadoQr.GUARDADA
    }

    // ---------- Caché del catálogo ----------

    suspend fun guardarCatalogo(productos: List<ProductoCacheEntity>) =
        db.productoCacheDao().guardarTodos(productos)

    fun observarCatalogo(): Flow<List<ProductoCacheEntity>> =
        db.productoCacheDao().observarTodos()

    companion object {
        // Un mismo QR no se vuelve a guardar dentro de 24 horas
        private const val VENTANA_DUPLICADO_MS = 24 * 60 * 60 * 1000L

        fun crear(context: Context) = LocalRepository(
            AppDatabase.getInstance(context),
            SessionManager.getInstance(context)
        )
    }
}