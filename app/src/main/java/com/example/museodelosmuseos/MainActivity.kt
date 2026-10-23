package com.example.museodelosmuseos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.museodelosmuseos.ui.navigation.Rutas
import com.example.museodelosmuseos.ui.screens.BienvenidaScreen
import com.example.museodelosmuseos.ui.screens.LoginScreen
import com.example.museodelosmuseos.ui.screens.RegistroScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

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
                        onIniciarSesion = { navController.navigate(Rutas.DASHBOARD) },
                        onRegistro = { navController.navigate(Rutas.REGISTRO) },
                        onVolver = { navController.popBackStack() }
                    )
                }
                composable(Rutas.REGISTRO) {
                    RegistroScreen(
                        onRegistrar = { navController.popBackStack() },
                        onIrALogin = { navController.popBackStack() },
                        onVolver = { navController.popBackStack() }
                    )
                }
                composable(Rutas.DASHBOARD) {
                    Text("Dashboard (pendiente)")
                }
            }
        }
    }
}