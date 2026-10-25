package com.example.museodelosmuseos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.ui.navigation.Rutas
import com.example.museodelosmuseos.ui.screens.BienvenidaScreen
import com.example.museodelosmuseos.ui.screens.DetalleMuseoScreen
import com.example.museodelosmuseos.ui.screens.DetalleProductoScreen
import com.example.museodelosmuseos.ui.screens.EscanerScreen
import com.example.museodelosmuseos.ui.screens.HomeScreen
import com.example.museodelosmuseos.ui.screens.LoginScreen
import com.example.museodelosmuseos.ui.screens.MapaScreen
import com.example.museodelosmuseos.ui.screens.PantallaPendiente
import com.example.museodelosmuseos.ui.screens.RegistroScreen
import com.example.museodelosmuseos.util.TipoContenidoQr
import com.example.museodelosmuseos.viewmodel.AuthViewModel
import com.example.museodelosmuseos.viewmodel.HomeViewModel
import com.example.museodelosmuseos.repository.FakeMuseoRepository
import com.example.museodelosmuseos.viewmodel.DetalleMuseoViewModel
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Acceso a la base de datos local (Room)
        val localRepository = LocalRepository.crear(applicationContext)

        setContent {
            val navController = rememberNavController()
            val authViewModel: AuthViewModel =
                viewModel(factory = AuthViewModel.factory(localRepository))
            val errorAuth by authViewModel.error.collectAsState()

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

                // ---------- Escáner QR (Recurso nativo 1) y detalles ----------
                composable(Rutas.ESCANER_QR) {
                    EscanerScreen(
                        onVerDetalle = { qr ->
                            when (qr.tipo) {
                                TipoContenidoQr.MUSEO ->
                                    navController.navigate("${Rutas.DETALLE_MUSEO}/${qr.codigo}")
                                TipoContenidoQr.PRODUCTO, TipoContenidoQr.PUBLICACION ->
                                    navController.navigate("${Rutas.DETALLE_PRODUCTO}/${qr.tipo.name}/${qr.codigo}")
                                TipoContenidoQr.DESCONOCIDO -> Unit
                            }
                        },
                        onVolver = { navController.popBackStack() }
                    )
                }
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
                        factory = DetalleMuseoViewModel.factory(codigo, FakeMuseoRepository(), localRepository)
                    )
                    DetalleMuseoScreen(
                        viewModel = detalleViewModel,
                        onVolver = { navController.popBackStack() },
                        onNavegar = navegarSeccion
                    )
                }

                // ---------- Secciones aún no integradas (pantalla temporal) ----------
                listOf(
                    Rutas.CATALOGO to "Catálogo",
                    Rutas.FAVORITOS to "Favoritos",
                    Rutas.CARRITO to "Carrito",
                    Rutas.NOTIFICACIONES to "Notificaciones",
                    Rutas.PERFIL to "Perfil"
                ).forEach { (ruta, titulo) ->
                    composable(ruta) {
                        PantallaPendiente(titulo = titulo, onVolver = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}