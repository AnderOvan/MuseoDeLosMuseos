package com.example.museodelosmuseos

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.museodelosmuseos.repository.FakeMuseoRepository
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.ui.navigation.Rutas
import com.example.museodelosmuseos.ui.screens.BienvenidaScreen
import com.example.museodelosmuseos.ui.screens.CatalogoScreen
import com.example.museodelosmuseos.ui.screens.DetalleMuseoScreen
import com.example.museodelosmuseos.ui.screens.DetalleProductoScreen
import com.example.museodelosmuseos.ui.screens.EscanerScreen
import com.example.museodelosmuseos.ui.screens.HomeScreen
import com.example.museodelosmuseos.ui.screens.LoginScreen
import com.example.museodelosmuseos.ui.screens.MapaScreen
import com.example.museodelosmuseos.ui.screens.HistorialScreen
import com.example.museodelosmuseos.ui.screens.NotificacionesScreen
import com.example.museodelosmuseos.ui.screens.PerfilScreen
import com.example.museodelosmuseos.ui.screens.RegistroScreen
import com.example.museodelosmuseos.ui.screens.ResultadoQrScreen
import com.example.museodelosmuseos.util.TipoContenidoQr
import com.example.museodelosmuseos.viewmodel.AuthViewModel
import com.example.museodelosmuseos.viewmodel.CatalogoViewModel
import com.example.museodelosmuseos.viewmodel.DetalleMuseoViewModel
import com.example.museodelosmuseos.viewmodel.HomeViewModel
import com.example.museodelosmuseos.ui.screens.DetallePublicacionScreen
import com.example.museodelosmuseos.viewmodel.DetalleProductoViewModel
import com.example.museodelosmuseos.ui.screens.FavoritosScreen
import com.example.museodelosmuseos.viewmodel.FavoritosViewModel
import com.example.museodelosmuseos.ui.screens.CotizadorScreen
import com.example.museodelosmuseos.viewmodel.CotizadorViewModel
import com.example.museodelosmuseos.viewmodel.HistorialViewModel
import com.example.museodelosmuseos.viewmodel.NotificacionesViewModel
import com.example.museodelosmuseos.viewmodel.PerfilViewModel
import com.example.museodelosmuseos.repository.local.PreferenciasEntity
import kotlinx.coroutines.launch
import com.example.museodelosmuseos.model.Rol
import com.example.museodelosmuseos.ui.components.LocalRol

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Acceso a la base de datos local (Room)
        val localRepository = LocalRepository.crear(applicationContext)

        // Crea (una sola vez) las cuentas de prueba de Administrador e Investigador
        lifecycleScope.launch { localRepository.sembrarCuentasDemo() }

        setContent {
            val navController = rememberNavController()
            val authViewModel: AuthViewModel =
                viewModel(factory = AuthViewModel.factory(localRepository))
            val errorAuth by authViewModel.error.collectAsState()
            // Rol del usuario con sesión iniciada; sin sesión se asume Cliente
            val usuarioActivo by localRepository.usuarioActivo.collectAsState(initial = null)
            val rolActual = usuarioActivo?.rol ?: Rol.CLIENTE

            // Ajuste "Texto grande" del Perfil: agranda las letras de toda la app
            val preferencias by localRepository.preferencias
                .collectAsState(initial = PreferenciasEntity(usuarioId = 0))
            val densidad = LocalDensity.current
            val densidadApp = Density(
                density = densidad.density,
                fontScale = if (preferencias.textoGrande) densidad.fontScale * 1.25f else densidad.fontScale
            )

            // Entra al inicio y borra el historial,
            // así el botón "atrás" no vuelve al login
            val irAlDashboard = {
                navController.navigate(Rutas.DASHBOARD) {
                    popUpTo(Rutas.BIENVENIDA) { inclusive = true }
                }
            }

            // Navegación de la barra inferior y de los accesos del inicio:
            // "atrás" siempre vuelve a Inicio y no se apilan pantallas repetidas
            val navegarSeccion: (String) -> Unit = { ruta ->
                if (ruta == Rutas.DASHBOARD) {
                    navController.popBackStack(Rutas.DASHBOARD, inclusive = false)
                } else {
                    navController.navigate(ruta) {
                        popUpTo(Rutas.DASHBOARD)
                        launchSingleTop = true
                    }
                }
            }

            CompositionLocalProvider(
                LocalDensity provides densidadApp,
                LocalRol provides rolActual
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Rutas.BIENVENIDA
                ) {
                    // ---------- Acceso ----------
                    composable(Rutas.BIENVENIDA) {
                        BienvenidaScreen(
                            onComenzar = { navController.navigate(Rutas.LOGIN) },
                            onYaTengoCuenta = { navController.navigate(Rutas.LOGIN) }
                        )
                    }
                    composable(Rutas.LOGIN) {
                        LoginScreen(
                            onIniciarSesion = { correo, contrasena, rol ->
                                authViewModel.iniciarSesion(correo, contrasena, rol) { irAlDashboard() }
                            },
                            onRegistro = {
                                authViewModel.limpiarError()
                                navController.navigate(Rutas.REGISTRO)
                            },
                            onVolver = {
                                authViewModel.limpiarError()
                                navController.popBackStack()
                            },
                            errorExterno = errorAuth
                        )
                    }
                    composable(Rutas.REGISTRO) {
                        RegistroScreen(
                            onRegistrar = { usuario ->
                                authViewModel.registrar(usuario) { irAlDashboard() }
                            },
                            onIrALogin = {
                                authViewModel.limpiarError()
                                navController.popBackStack()
                            },
                            onVolver = {
                                authViewModel.limpiarError()
                                navController.popBackStack()
                            },
                            errorExterno = errorAuth
                        )
                    }

                    // ---------- Pantalla principal ----------
                    composable(Rutas.DASHBOARD) {
                        val homeViewModel: HomeViewModel =
                            viewModel(factory = HomeViewModel.factory(localRepository))
                        val nombre by homeViewModel.nombreUsuario.collectAsState()

                        HomeScreen(
                            nombreUsuario = nombre,
                            onNavegar = navegarSeccion
                        )
                    }

                    // ---------- Mapa de museos (Recurso nativo 2) ----------
                    composable(Rutas.MAPA) {
                        MapaScreen(
                            onVerDetalle = { id -> navController.navigate("${Rutas.DETALLE_MUSEO}/$id") },
                            onNavegar = navegarSeccion,
                            onVolver = { navController.popBackStack() }
                        )
                    }

                    // ---------- Catálogo de productos y cursos ----------
                    composable(Rutas.CATALOGO) {
                        val catalogoViewModel: CatalogoViewModel =
                            viewModel(factory = CatalogoViewModel.factory(localRepository))
                        CatalogoScreen(
                            viewModel = catalogoViewModel,
                            onNavegarADetalle = { id ->
                                navController.navigate("${Rutas.DETALLE_PRODUCTO}/${TipoContenidoQr.PRODUCTO.name}/$id")
                            },
                            onNavegar = navegarSeccion
                        )
                    }

                    // ---------- Escáner QR (Recurso nativo 1) y resultado ----------
                    composable(Rutas.ESCANER_QR) {
                        EscanerScreen(
                            onResultado = { estado, contenido ->
                                // Uri.encode: el texto del QR puede traer "/" u otros símbolos
                                navController.navigate("${Rutas.RESULTADO_QR}/$estado/${Uri.encode(contenido)}")
                            },
                            onVolver = { navController.popBackStack() }
                        )
                    }
                    composable("${Rutas.RESULTADO_QR}/{estado}/{contenido}") { entrada ->
                        ResultadoQrScreen(
                            estado = entrada.arguments?.getString("estado").orEmpty(),
                            contenido = entrada.arguments?.getString("contenido").orEmpty(),
                            onVerDetalle = { qr ->
                                when (qr.tipo) {
                                    TipoContenidoQr.MUSEO ->
                                        navController.navigate("${Rutas.DETALLE_MUSEO}/${qr.codigo}")
                                    TipoContenidoQr.PRODUCTO, TipoContenidoQr.PUBLICACION ->
                                        navController.navigate("${Rutas.DETALLE_PRODUCTO}/${qr.tipo.name}/${qr.codigo}")
                                    TipoContenidoQr.DESCONOCIDO -> Unit
                                }
                            },
                            onVolverAEscanear = { navController.popBackStack() }
                        )
                    }

                    // ---------- Detalles ----------
                    composable("${Rutas.DETALLE_PRODUCTO}/{tipo}/{codigo}") { entrada ->
                        val codigo = entrada.arguments?.getString("codigo").orEmpty()
                        val tipo = TipoContenidoQr.valueOf(
                            entrada.arguments?.getString("tipo") ?: TipoContenidoQr.PRODUCTO.name
                        )
                        if (tipo == TipoContenidoQr.PUBLICACION) {
                            // Las publicaciones (QR "PUB_") tienen su propia ficha
                            DetallePublicacionScreen(
                                codigo = codigo,
                                tipo = tipo,
                                onVolver = { navController.popBackStack() }
                            )
                        } else {
                            // Productos y cursos: detalle con cantidad y carrito
                            val detalleProductoViewModel: DetalleProductoViewModel = viewModel(
                                factory = DetalleProductoViewModel.factory(codigo, localRepository)
                            )
                            DetalleProductoScreen(
                                viewModel = detalleProductoViewModel,
                                onVolver = { navController.popBackStack() },
                                onIrAlCarrito = { navegarSeccion(Rutas.CARRITO) }
                            )
                        }
                    }
                    composable("${Rutas.DETALLE_MUSEO}/{codigo}") { entrada ->
                        val codigo = entrada.arguments?.getString("codigo").orEmpty()
                        // Cada museo abierto tiene su propio ViewModel (con su id)
                        val detalleViewModel: DetalleMuseoViewModel = viewModel(
                            factory = DetalleMuseoViewModel.factory(codigo, FakeMuseoRepository(), localRepository)
                        )
                        DetalleMuseoScreen(
                            viewModel = detalleViewModel,
                            onVolver = { navController.popBackStack() },
                            onNavegar = navegarSeccion
                        )
                    }
                    // ---------- Favoritos (Room, separados por usuario) ----------
                    composable(Rutas.FAVORITOS) {
                        val favoritosViewModel: FavoritosViewModel =
                            viewModel(factory = FavoritosViewModel.factory(localRepository))
                        FavoritosScreen(
                            viewModel = favoritosViewModel,
                            onVerDetalleMuseo = { id -> navController.navigate("${Rutas.DETALLE_MUSEO}/$id") },
                            onAbrirRecurso = { ficha ->
                                navController.navigate("${Rutas.DETALLE_PRODUCTO}/${ficha.tipo.name}/${ficha.codigo}")
                            },
                            onNavegarAMapa = { navegarSeccion(Rutas.MAPA) },
                            onIrAlEscaner = { navegarSeccion(Rutas.ESCANER_QR) },
                            onNavegar = navegarSeccion,
                            onVolver = { navController.popBackStack() }
                        )
                    }
                    // ---------- Carrito y cotización de envíos ----------
                    composable(Rutas.CARRITO) {
                        val cotizadorViewModel: CotizadorViewModel =
                            viewModel(factory = CotizadorViewModel.factory(localRepository))
                        CotizadorScreen(
                            viewModel = cotizadorViewModel,
                            onIrAlCatalogo = { navegarSeccion(Rutas.CATALOGO) },
                            onVolverAlInicio = { navegarSeccion(Rutas.DASHBOARD) },
                            onNavegar = navegarSeccion,
                            onVolver = { navController.popBackStack() }
                        )
                    }

                    // ---------- Historial y gestión de solicitudes ----------
                    composable(Rutas.HISTORIAL) {
                        val historialViewModel: HistorialViewModel =
                            viewModel(factory = HistorialViewModel.factory(localRepository))
                        HistorialScreen(
                            viewModel = historialViewModel,
                            onIrAlCatalogo = { navegarSeccion(Rutas.CATALOGO) },
                            onNavegar = navegarSeccion,
                            onVolver = { navController.popBackStack() }
                        )
                    }

                    // ---------- Notificaciones ----------
                    composable(Rutas.NOTIFICACIONES) {
                        val notificacionesViewModel: NotificacionesViewModel =
                            viewModel(factory = NotificacionesViewModel.factory(localRepository))
                        NotificacionesScreen(
                            viewModel = notificacionesViewModel,
                            onAbrirDestino = navegarSeccion,
                            onNavegar = navegarSeccion,
                            onVolver = { navController.popBackStack() }
                        )
                    }

                    // ---------- Perfil y configuración ----------
                    composable(Rutas.PERFIL) {
                        val perfilViewModel: PerfilViewModel =
                            viewModel(factory = PerfilViewModel.factory(localRepository))
                        PerfilScreen(
                            viewModel = perfilViewModel,
                            onNavegar = navegarSeccion,
                            // La sesión ya se cerró en el ViewModel; aquí solo se limpia el historial de pantallas
                            onCambiarUsuario = {
                                navController.navigate(Rutas.LOGIN) {
                                    popUpTo(Rutas.DASHBOARD) { inclusive = true }
                                }
                            },
                            onCerrarSesion = {
                                navController.navigate(Rutas.BIENVENIDA) {
                                    popUpTo(Rutas.DASHBOARD) { inclusive = true }
                                }
                            },
                            onVolver = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}