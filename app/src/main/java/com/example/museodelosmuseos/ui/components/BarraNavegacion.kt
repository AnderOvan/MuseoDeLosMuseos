package com.example.museodelosmuseos.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import com.example.museodelosmuseos.model.Rol
import com.example.museodelosmuseos.ui.navigation.Rutas
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Blanco
import com.example.museodelosmuseos.ui.theme.Dorado

private data class ItemNavegacion(
    val ruta: String,
    val etiqueta: String,
    val icono: ImageVector
)

// Barra inferior compartida por las pantallas principales.
// rutaActual marca cuál de los cinco botones está seleccionado.
@Composable
fun BarraNavegacion(
    rutaActual: String,
    onNavegar: (String) -> Unit,
    rol: Rol = Rol.CLIENTE
) {
    val items = listOf(
        ItemNavegacion(Rutas.DASHBOARD, "Inicio", Icons.Filled.Home),
        ItemNavegacion(Rutas.MAPA, "Mapa", Icons.Filled.Map),
        // El catálogo es de Administrador y Cliente; el Investigador escanea
        if (rol == Rol.INVESTIGADOR) {
            ItemNavegacion(Rutas.ESCANER_QR, "Escanear", Icons.Filled.QrCodeScanner)
        } else {
            ItemNavegacion(Rutas.CATALOGO, "Catálogo", Icons.AutoMirrored.Filled.MenuBook)
        },
        ItemNavegacion(Rutas.NOTIFICACIONES, "Notificaciones", Icons.Filled.Notifications),
        ItemNavegacion(Rutas.PERFIL, "Perfil", Icons.Filled.Person)
    )

    NavigationBar(containerColor = Blanco) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.ruta == rutaActual,
                onClick = { if (item.ruta != rutaActual) onNavegar(item.ruta) },
                icon = { Icon(item.icono, contentDescription = item.etiqueta) },
                label = { Text(item.etiqueta, fontSize = 11.sp, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AzulOscuro,
                    selectedTextColor = AzulOscuro,
                    indicatorColor = Dorado,
                    unselectedIconColor = AzulOscuro.copy(alpha = 0.6f),
                    unselectedTextColor = AzulOscuro.copy(alpha = 0.6f)
                )
            )
        }
    }
}