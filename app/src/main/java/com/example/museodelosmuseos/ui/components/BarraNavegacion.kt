package com.example.museodelosmuseos.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import com.example.museodelosmuseos.model.Rol
import com.example.museodelosmuseos.ui.navigation.Rutas
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Blanco
import com.example.museodelosmuseos.ui.theme.Dorado

// Rol del usuario con sesión iniciada, disponible en cualquier pantalla.
// MainActivity lo entrega con CompositionLocalProvider; por defecto es CLIENTE.
val LocalRol = compositionLocalOf { Rol.CLIENTE }

// Cada destino dice qué roles pueden verlo (por defecto, todos)
private data class Destino(
    val titulo: String,
    val icono: ImageVector,
    val ruta: String,
    val roles: Set<Rol> = Rol.entries.toSet()
)

private val DESTINOS = listOf(
    Destino("Inicio", Icons.Filled.Home, Rutas.DASHBOARD),
    Destino("Mapa", Icons.Filled.Map, Rutas.MAPA),
    // El Investigador no usa el catálogo de compras
    Destino(
        "Catálogo", Icons.AutoMirrored.Filled.MenuBook, Rutas.CATALOGO,
        roles = setOf(Rol.CLIENTE, Rol.ADMINISTRADOR)
    ),
    Destino("Notificaciones", Icons.Filled.Notifications, Rutas.NOTIFICACIONES),
    Destino("Perfil", Icons.Filled.Person, Rutas.PERFIL)
)

// Barra inferior compartida: muestra solo las pestañas del rol actual
@Composable
fun BarraNavegacion(rutaActual: String, onNavegar: (String) -> Unit) {
    val rol = LocalRol.current
    NavigationBar(containerColor = Blanco) {
        DESTINOS.filter { rol in it.roles }.forEach { destino ->
            val activo = destino.ruta == rutaActual
            NavigationBarItem(
                selected = activo,
                onClick = { if (!activo) onNavegar(destino.ruta) },
                icon = { Icon(destino.icono, destino.titulo) },
                label = { Text(destino.titulo, fontSize = 10.sp, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AzulOscuro,
                    selectedTextColor = AzulOscuro,
                    indicatorColor = Dorado.copy(alpha = 0.35f),
                    unselectedIconColor = AzulOscuro.copy(alpha = 0.5f),
                    unselectedTextColor = AzulOscuro.copy(alpha = 0.5f)
                )
            )
        }
    }
}