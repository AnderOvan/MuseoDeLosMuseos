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
                    Text("Dashboard (pendiente)")
                }
            }
        }
    }
}