package com.example.museodelosmuseos.repository

import android.content.Context
import com.example.museodelosmuseos.model.EstadoPedido
import com.example.museodelosmuseos.model.Rol
import com.example.museodelosmuseos.model.TipoNotificacion
import com.example.museodelosmuseos.repository.local.AppDatabase
import com.example.museodelosmuseos.repository.local.Cifrado

// Importaciones llamadas desde la clase Entidades Locales
import com.example.museodelosmuseos.repository.local.FavoritoEntity
import com.example.museodelosmuseos.repository.local.LecturaQrEntity
import com.example.museodelosmuseos.repository.local.NotificacionEntity
import com.example.museodelosmuseos.repository.local.PedidoEntity
import com.example.museodelosmuseos.repository.local.PreferenciasEntity
import com.example.museodelosmuseos.repository.local.ProductoCacheEntity
import com.example.museodelosmuseos.repository.local.UsuarioEntity

import com.example.museodelosmuseos.repository.local.SesionManager

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

// Resultado de registrarse o iniciar sesión
sealed interface ResultadoAuth {
    data class Exito(val usuario: UsuarioEntity) : ResultadoAuth
    data class Error(val mensaje: String) : ResultadoAuth
}

// Resultado de guardar un QR escaneado
enum class ResultadoQr { GUARDADA, DUPLICADA, SIN_SESION }

// Cuenta ficticia para probar los perfiles Administrador e Investigador
private class CuentaDemo(
    val nombre: String,
    val rut: String,
    val correo: String,
    val clave: String,
    val rol: Rol,
    val avatar: Int
)

private val CUENTAS_DEMO = listOf(
    CuentaDemo("Admin Museo", "11.111.111-1", "admin@museo.cl", "admin123", Rol.ADMINISTRADOR, 0),
    CuentaDemo("Investigadora Museo", "22.222.222-2", "investigador@museo.cl", "invest123", Rol.INVESTIGADOR, 2)
)

// Punto único de acceso a los datos locales.
// Los ViewModels usan esta clase y nunca tocan Room directamente.
@OptIn(ExperimentalCoroutinesApi::class)
class LocalRepository(
    private val db: AppDatabase,
    private val sesion: SesionManager
) {
    val usuarioActivoId: StateFlow<Long?> = sesion.usuarioActivoId

    // Datos del usuario con sesión iniciada (null si nadie inició sesión)
    val usuarioActivo: Flow<UsuarioEntity?> =
        usuarioActivoId.flatMapLatest { id ->
            if (id == null) flowOf(null) else db.usuarioDao().observarPorId(id)
        }

    // ---------- Usuarios ----------

    suspend fun registrarUsuario(
        nombre: String,
        rut: String,
        correo: String,
        clave: String,
        despacho: String,
        rol: Rol = Rol.CLIENTE,
        avatar: Int = 0
    ): ResultadoAuth {
        val dao = db.usuarioDao()
        val correoLimpio = correo.trim().lowercase()

        if (dao.buscarPorCorreo(correoLimpio) != null) {
            return ResultadoAuth.Error("Este correo ya está registrado")
        }
        if (dao.buscarPorRut(rut) != null) {
            return ResultadoAuth.Error("Este RUT ya está registrado")
        }

        val nuevo = UsuarioEntity(
            nombre = nombre.trim(),
            rut = rut,
            correo = correoLimpio,
            claveHash = Cifrado.sha256(clave),
            despacho = despacho.trim(),
            rol = rol,
            avatar = avatar
        )
        val id = dao.insertar(nuevo)
        crearNotificacionesDeBienvenida(id, rol)
        sesion.iniciarSesion(id)
        return ResultadoAuth.Exito(nuevo.copy(id = id))
    }

    suspend fun iniciarSesion(correo: String, clave: String): ResultadoAuth {
        val dao = db.usuarioDao()
        val correoLimpio = correo.trim().lowercase()

        if (dao.buscarPorCorreo(correoLimpio) == null) {
            return ResultadoAuth.Error("No existe una cuenta con este correo")
        }
        val usuario = dao.autenticar(correoLimpio, Cifrado.sha256(clave))
            ?: return ResultadoAuth.Error("Contraseña incorrecta")

        sesion.iniciarSesion(usuario.id)
        return ResultadoAuth.Exito(usuario)
    }

    fun cerrarSesion() = sesion.cerrarSesion()

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

    // ---------- Cuentas de prueba (Administrador e Investigador) ----------

    // Se llama al abrir la app. Si las cuentas ya existen, no hace nada.
    suspend fun sembrarCuentasDemo() {
        val dao = db.usuarioDao()
        CUENTAS_DEMO.forEach { cuenta ->
            if (dao.buscarPorCorreo(cuenta.correo) == null) {
                runCatching {
                    val id = dao.insertar(
                        UsuarioEntity(
                            nombre = cuenta.nombre,
                            rut = cuenta.rut,
                            correo = cuenta.correo,
                            claveHash = Cifrado.sha256(cuenta.clave),
                            despacho = "Av. Demo 123, Santiago",
                            rol = cuenta.rol,
                            avatar = cuenta.avatar
                        )
                    )
                    crearNotificacionesDeBienvenida(id, cuenta.rol)
                }
            }
        }
    }

    // ---------- Perfil y preferencias ----------

    // Solo cambia nombre, dirección de despacho y avatar del usuario activo
    suspend fun actualizarPerfil(nombre: String, despacho: String, avatar: Int) {
        val id = usuarioActivoId.value ?: return
        db.usuarioDao().actualizarPerfil(id, nombre.trim(), despacho.trim(), avatar)
    }

    // Ajustes del usuario activo (si aún no tiene, se usan los valores por defecto)
    val preferencias: Flow<PreferenciasEntity> =
        usuarioActivoId.flatMapLatest { id ->
            if (id == null) flowOf(PreferenciasEntity(usuarioId = 0))
            else db.preferenciasDao().observar(id).map { it ?: PreferenciasEntity(usuarioId = id) }
        }

    suspend fun cambiarPreferencias(cambio: (PreferenciasEntity) -> PreferenciasEntity) {
        val id = usuarioActivoId.value ?: return
        val dao = db.preferenciasDao()
        val actual = dao.obtener(id) ?: PreferenciasEntity(usuarioId = id)
        dao.guardar(cambio(actual))
    }

    // ---------- Pedidos ----------

    // Administrador: todos los pedidos. Cliente: solo los suyos.
    fun observarPedidos(): Flow<List<PedidoEntity>> =
        usuarioActivo.flatMapLatest { usuario ->
            when {
                usuario == null -> flowOf(emptyList<PedidoEntity>())
                usuario.rol == Rol.ADMINISTRADOR -> db.pedidoDao().observarTodos()
                else -> db.pedidoDao().observarPorUsuario(usuario.id)
            }
        }

    // Guarda el pedido del usuario activo (estado inicial: Pendiente) y le avisa
    suspend fun registrarPedido(
        numero: String,
        resumen: String,
        total: Double,
        destino: String,
        tipoEnvio: String
    ) {
        val id = usuarioActivoId.value ?: return
        val usuario = db.usuarioDao().obtenerPorId(id) ?: return
        db.pedidoDao().insertar(
            PedidoEntity(
                usuarioId = id,
                clienteNombre = usuario.nombre,
                numero = numero,
                resumen = resumen,
                total = total,
                destino = destino,
                tipoEnvio = tipoEnvio
            )
        )
        crearNotificacion(
            id, TipoNotificacion.PEDIDO,
            "Pedido $numero recibido",
            "Tu solicitud quedó en estado Pendiente. Te avisaremos cuando cambie."
        )
    }

    // Solo el Administrador puede cambiar el estado; el cliente recibe un aviso
    suspend fun cambiarEstadoPedido(pedido: PedidoEntity, nuevo: EstadoPedido) {
        val idActivo = usuarioActivoId.value ?: return
        val activo = db.usuarioDao().obtenerPorId(idActivo) ?: return
        if (activo.rol != Rol.ADMINISTRADOR || pedido.estado == nuevo) return

        db.pedidoDao().actualizarEstado(pedido.id, nuevo.name)
        crearNotificacion(
            pedido.usuarioId, TipoNotificacion.PEDIDO,
            "Pedido ${pedido.numero}: ${nuevo.titulo}",
            "El estado de tu pedido cambió de ${pedido.estado.titulo} a ${nuevo.titulo}."
        )
    }

    // ---------- Notificaciones (solo del usuario activo) ----------

    fun observarNotificaciones(): Flow<List<NotificacionEntity>> =
        usuarioActivoId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else db.notificacionDao().observarPorUsuario(id)
        }

    suspend fun marcarNotificacionLeida(notificacionId: Long) {
        val id = usuarioActivoId.value ?: return
        db.notificacionDao().marcarLeida(notificacionId, id)
    }

    suspend fun marcarTodasLeidas() {
        val id = usuarioActivoId.value ?: return
        db.notificacionDao().marcarTodasLeidas(id)
    }

    // Aviso de prueba del Administrador: llega a todos los usuarios que tengan las notificaciones activas
    suspend fun enviarAvisoATodos(tipo: TipoNotificacion, titulo: String, mensaje: String) {
        val idActivo = usuarioActivoId.value ?: return
        val activo = db.usuarioDao().obtenerPorId(idActivo) ?: return
        if (activo.rol != Rol.ADMINISTRADOR) return
        db.usuarioDao().listarIds().forEach { crearNotificacion(it, tipo, titulo, mensaje) }
    }

    // Respeta el ajuste "Notificaciones" del perfil: si está apagado, no guarda el aviso
    private suspend fun crearNotificacion(
        usuarioId: Long,
        tipo: TipoNotificacion,
        titulo: String,
        mensaje: String
    ) {
        val preferencias = db.preferenciasDao().obtener(usuarioId) ?: PreferenciasEntity(usuarioId)
        if (!preferencias.notificaciones) return
        db.notificacionDao().insertar(
            NotificacionEntity(usuarioId = usuarioId, tipo = tipo, titulo = titulo, mensaje = mensaje)
        )
    }

    // Avisos iniciales distintos según el rol
    private suspend fun crearNotificacionesDeBienvenida(usuarioId: Long, rol: Rol) {
        crearNotificacion(
            usuarioId, TipoNotificacion.AVISO,
            "¡Bienvenido/a a Museo de los Museos!",
            "Explora el mapa de museos virtuales y el catálogo de cursos y productos."
        )
        when (rol) {
            Rol.CLIENTE -> crearNotificacion(
                usuarioId, TipoNotificacion.CURSO,
                "Nuevos cursos disponibles",
                "Revisa los cursos de museología y gestión de audiencias en el catálogo."
            )
            Rol.INVESTIGADOR -> crearNotificacion(
                usuarioId, TipoNotificacion.MUSEO,
                "Museos por revisar",
                "Hay fichas de museos virtuales nuevas para revisar en el mapa."
            )
            Rol.ADMINISTRADOR -> crearNotificacion(
                usuarioId, TipoNotificacion.PEDIDO,
                "Panel de gestión",
                "Aquí verás las solicitudes recibidas para actualizar su estado."
            )
        }
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
            SesionManager.getInstance(context)
        )
    }
}