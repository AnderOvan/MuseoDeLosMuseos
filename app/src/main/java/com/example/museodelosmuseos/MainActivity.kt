package com.example.museodelosmuseos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.museodelosmuseos.repository.LocalRepository
//import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.ui.navigation.Rutas
import com.example.museodelosmuseos.ui.screens.BienvenidaScreen
import com.example.museodelosmuseos.ui.screens.LoginScreen
import com.example.museodelosmuseos.ui.screens.RegistroScreen
import com.example.museodelosmuseos.viewmodel.AuthViewModel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.museodelosmuseos.ui.screens.DetalleMuseoScreen
import com.example.museodelosmuseos.ui.screens.DetalleProductoScreen
import com.example.museodelosmuseos.ui.screens.EscanerScreen
import com.example.museodelosmuseos.util.TipoContenidoQr

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

            // Entra al dashboard y borra el historial,
            // así el botón "atrás" no vuelve al login
            val irAlDashboard = {
                navController.navigate(Rutas.DASHBOARD) {
                    popUpTo(Rutas.BIENVENIDA) { inclusive = true }
                }
            }

            NavHost(
                navController = navController,
                startDestination = Rutas.BIENVENIDA
            ) {
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
                composable(Rutas.DASHBOARD) {
                    // Dashboard provisorio: solo un acceso al escáner
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Dashboard (pendiente)")
                        Button(onClick = { navController.navigate(Rutas.ESCANER_QR) }) {
                            Text("Escanear código QR")
                        }
                    }
                }

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
                    DetalleMuseoScreen(
                        codigo = entrada.arguments?.getString("codigo").orEmpty(),
                        onVolver = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}