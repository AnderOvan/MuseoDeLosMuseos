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
                    Text("Pantalla de login (pendiente)")
                }
            }
        }
    }
}