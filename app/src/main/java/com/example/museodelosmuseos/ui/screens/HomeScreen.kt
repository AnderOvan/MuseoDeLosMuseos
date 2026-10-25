package com.example.museodelosmuseos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.museodelosmuseos.ui.navigation.Rutas
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Blanco
import com.example.museodelosmuseos.ui.theme.Crema
import com.example.museodelosmuseos.ui.theme.Dorado

// Un botón de la pantalla: texto, ícono y a qué ruta lleva
private data class Acceso(val titulo: String, val icono: ImageVector, val ruta: String)

// Una tarjeta de la sección "Explora"
private data class Exploracion(
    val titulo: String,
    val subtitulo: String,
    val icono: ImageVector,
    val ruta: String
)

private val ACCESOS_RAPIDOS = listOf(
    Acceso("Mapa", Icons.Filled.Map, Rutas.MAPA),
    Acceso("Catálogo", Icons.AutoMirrored.Filled.MenuBook, Rutas.CATALOGO),
    Acceso("Escáner QR", Icons.Filled.QrCodeScanner, Rutas.ESCANER_QR),
    Acceso("Favoritos", Icons.Filled.Favorite, Rutas.FAVORITOS),
    Acceso("Carrito", Icons.Filled.ShoppingCart, Rutas.CARRITO)
)

private val EXPLORA = listOf(
    Exploracion("Cursos", "Aprende con expertos", Icons.Filled.School, Rutas.CATALOGO),
    Exploracion("Productos", "Libros y más", Icons.Filled.LocalLibrary, Rutas.CATALOGO),
    Exploracion("Museos Virtuales", "Recorre Iberoamérica", Icons.Filled.AccountBalance, Rutas.MAPA),
    Exploracion("Novedades", "Exposiciones y eventos", Icons.Filled.Event, Rutas.NOTIFICACIONES)
)

private val BARRA_INFERIOR = listOf(
    Acceso("Inicio", Icons.Filled.Home, Rutas.DASHBOARD),
    Acceso("Mapa", Icons.Filled.Map, Rutas.MAPA),
    Acceso("Catálogo", Icons.AutoMirrored.Filled.MenuBook, Rutas.CATALOGO),
    Acceso("Notificaciones", Icons.Filled.Notifications, Rutas.NOTIFICACIONES),
    Acceso("Perfil", Icons.Filled.Person, Rutas.PERFIL)
)

@Composable
fun HomeScreen(
    nombreUsuario: String,
    onNavegar: (String) -> Unit
) {
    Scaffold(
        containerColor = Crema,
        bottomBar = { BarraInferior(onNavegar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Encabezado(
                nombre = nombreUsuario,
                onNotificaciones = { onNavegar(Rutas.NOTIFICACIONES) }
            )

            Spacer(Modifier.height(20.dp))
            TituloSeccion("Accesos rápidos")

            // Fila con scroll horizontal por si la pantalla es angosta
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ACCESOS_RAPIDOS.forEach { acceso ->
                    BotonCircular(acceso) { onNavegar(acceso.ruta) }
                }
            }

            Spacer(Modifier.height(24.dp))
            TituloSeccion("Explora")

            // Grid 2x2 hecho con filas (evita conflictos de scroll con LazyGrid)
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EXPLORA.chunked(2).forEach { fila ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        fila.forEach { item ->
                            TarjetaExplora(item, Modifier.weight(1f)) { onNavegar(item.ruta) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            BannerDestacado(onExplorar = { onNavegar(Rutas.MAPA) })
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Encabezado(nombre: String, onNotificaciones: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AzulOscuro, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar con la inicial del nombre
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Dorado, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nombre.firstOrNull()?.uppercase() ?: "V",
                    color = AzulOscuro,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("¡Hola, $nombre!", color = Blanco, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Sigamos explorando el patrimonio de Iberoamérica", color = Crema, fontSize = 14.sp)
            }
            IconButton(onClick = onNotificaciones) {
                Icon(Icons.Filled.Notifications, "Notificaciones", tint = Dorado)
            }
        }
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        color = AzulOscuro,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
    )
}

@Composable
private fun BotonCircular(acceso: Acceso, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(AzulOscuro, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(acceso.icono, null, tint = Dorado, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = acceso.titulo,
            color = AzulOscuro,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun TarjetaExplora(item: Exploracion, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(item.icono, null, tint = Dorado, modifier = Modifier.size(32.dp))
            Column {
                Text(item.titulo, color = AzulOscuro, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(item.subtitulo, color = AzulOscuro.copy(alpha = 0.65f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun BannerDestacado(onExplorar: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AzulOscuro)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Patrimonio que nos une", color = Dorado, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Descubre museos de toda Iberoamérica desde tu celular.",
                    color = Crema,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onExplorar,
                    colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro)
                ) {
                    Text("Explorar museos")
                }
            }
            Spacer(Modifier.width(12.dp))
            Icon(Icons.Filled.Public, null, tint = Dorado, modifier = Modifier.size(64.dp))
        }
    }
}

@Composable
private fun BarraInferior(onNavegar: (String) -> Unit) {
    NavigationBar(containerColor = Blanco) {
        BARRA_INFERIOR.forEach { destino ->
            val activo = destino.ruta == Rutas.DASHBOARD // Estamos en Inicio
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

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    HomeScreen(nombreUsuario = "Cristóbal", onNavegar = {})
}