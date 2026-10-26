package com.example.museodelosmuseos

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.museodelosmuseos.repository.FakeMuseoRepository
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.ui.navigation.Rutas
import com.example.museodelosmuseos.ui.screens.BienvenidaScreen
import com.example.museodelosmuseos.ui.screens.DashboardScreen
import com.example.museodelosmuseos.ui.screens.DetalleMuseoScreen
import com.example.museodelosmuseos.ui.screens.DetalleProductoScreen
import com.example.museodelosmuseos.ui.screens.EscanerScreen
import com.example.museodelosmuseos.ui.screens.LoginScreen
import com.example.museodelosmuseos.ui.screens.MapaScreen
import com.example.museodelosmuseos.ui.screens.PantallaPendiente
import com.example.museodelosmuseos.ui.screens.RegistroScreen
import com.example.museodelosmuseos.ui.screens.ResultadoQrScreen
import com.example.museodelosmuseos.util.TipoContenidoQr
import com.example.museodelosmuseos.viewmodel.AuthViewModel
import com.example.museodelosmuseos.viewmodel.DetalleMuseoViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val repo by lazy { LocalRepository.crear(applicationContext) }
    private val authViewModel: AuthViewModel by viewModels { AuthViewModel.factory(repo) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crea las cuentas de Administrador e Investigador si todavía no existen
        lifecycleScope.launch { repo.crearCuentasDePrueba() }

        setContent {
            val navController = rememberNavController()
            val errorAuth by authViewModel.error.collectAsState()
            val usuarioActivo by authViewModel.usuarioActivo.collectAsState()

            // Entra al inicio y borra el historial: "atrás" no vuelve al login
            val irAlDashboard: () -> Unit = {
                navController.navigate(Rutas.DASHBOARD) {
                    popUpTo(Rutas.BIENVENIDA) { inclusive = true }
                }
            }

            // Barra inferior y accesos rápidos: "atrás" siempre vuelve a Inicio
            // y no se apilan pantallas repetidas
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
                            authViewModel.iniciarSesion(correo, contrasena, rol, irAlDashboard)
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
                            authViewModel.registrar(usuario, irAlDashboard)
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

                // ---------- Pantalla 4: Dashboard ----------
                composable(Rutas.DASHBOARD) {
                    usuarioActivo?.let { usuario ->
                        DashboardScreen(
                            nombre = usuario.nombre,
                            rol = usuario.rol,
                            avatar = usuario.avatar,
                            onNavegar = navegarSeccion
                        )
                    }
                }

                // ---------- Mapa de museos ----------
                composable(Rutas.MAPA) {
                    MapaScreen(
                        onVerDetalle = { id -> navController.navigate("${Rutas.DETALLE_MUSEO}/$id") },
                        onNavegar = navegarSeccion,
                        onVolver = { navController.popBackStack() }
                    )
                }

                // ---------- Escáner QR y resultado ----------
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
                    DetalleProductoScreen(
                        codigo = entrada.arguments?.getString("codigo").orEmpty(),
                        tipo = TipoContenidoQr.valueOf(
                            entrada.arguments?.getString("tipo") ?: TipoContenidoQr.PRODUCTO.name
                        ),
                        onVolver = { navController.popBackStack() }
                    )
                }
                composable("${Rutas.DETALLE_MUSEO}/{codigo}") { entrada ->
                    val codigo = entrada.arguments?.getString("codigo").orEmpty()
                    // Cada museo abierto tiene su propio ViewModel (con su id)
                    val detalleViewModel: DetalleMuseoViewModel = viewModel(
                        factory = DetalleMuseoViewModel.factory(codigo, FakeMuseoRepository(), repo)
                    )
                    DetalleMuseoScreen(
                        viewModel = detalleViewModel,
                        onVolver = { navController.popBackStack() },
                        onNavegar = navegarSeccion
                    )
                }

                // ---------- Secciones aún no integradas ----------
                // Reemplaza cada pantalla temporal cuando exista la real
                listOf(
                    Rutas.CATALOGO to "Catálogo",
                    Rutas.FAVORITOS to "Favoritos",
                    Rutas.HISTORIAL to "Historial de solicitudes",
                    Rutas.NOTIFICACIONES to "Notificaciones"
                ).forEach { (ruta, titulo) ->
                    composable(ruta) {
                        PantallaPendiente(titulo, onVolver = { navController.popBackStack() })
                    }
                }

                // Perfil temporal: incluye "Cerrar sesión" para probar los tres roles
                composable(Rutas.PERFIL) {
                    PantallaPendiente(
                        titulo = "Perfil",
                        onVolver = { navController.popBackStack() },
                        onCerrarSesion = {
                            authViewModel.cerrarSesion()
                            navController.navigate(Rutas.LOGIN) {
                                popUpTo(Rutas.DASHBOARD) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}