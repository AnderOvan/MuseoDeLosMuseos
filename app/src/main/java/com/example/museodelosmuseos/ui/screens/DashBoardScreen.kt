package com.example.museodelosmuseos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.museodelosmuseos.model.Rol
import com.example.museodelosmuseos.ui.components.BarraNavegacion
import com.example.museodelosmuseos.ui.navigation.Rutas
import com.example.museodelosmuseos.ui.theme.*

// ---------- Datos de demostración (ficticios) ----------
private val museosDestacados = listOf(
    "Museo Nacional de Arte Colonial" to "Bogotá, Colombia",
    "Museo de Arte de Lima" to "Lima, Perú",
    "Museo del Prado Virtual" to "Madrid, España"
)
private val solicitudesRecientes = listOf(
    "#2024-00123" to "Entregado · US$ 84",
    "#2024-00122" to "Enviado · US$ 49",
    "#2024-00121" to "Pendiente · US$ 35"
)
private val registrosMuseologicos = listOf(
    "Museo Nacional de Arte Colonial" to "Publicado",
    "Museo de Arte de Lima" to "En revisión",
    "Museo de Arte Moderno" to "Borrador"
)

private data class Acceso(val etiqueta: String, val icono: ImageVector, val ruta: String)

// Cada rol ve solo los accesos de las pantallas que le corresponden
private fun accesosPara(rol: Rol): List<Acceso> = when (rol) {
    Rol.CLIENTE -> listOf(
        Acceso("Mapa", Icons.Filled.Map, Rutas.MAPA),
        Acceso("Catálogo", Icons.AutoMirrored.Filled.MenuBook, Rutas.CATALOGO),
        Acceso("Escanear QR", Icons.Filled.QrCodeScanner, Rutas.ESCANER_QR),
        Acceso("Favoritos", Icons.Filled.Favorite, Rutas.FAVORITOS),
        Acceso("Solicitudes", Icons.Filled.Receipt, Rutas.HISTORIAL)
    )
    Rol.ADMINISTRADOR -> listOf(
        Acceso("Mapa", Icons.Filled.Map, Rutas.MAPA),
        Acceso("Catálogo", Icons.AutoMirrored.Filled.MenuBook, Rutas.CATALOGO),
        Acceso("Solicitudes", Icons.Filled.Receipt, Rutas.HISTORIAL),
        Acceso("Avisos", Icons.Filled.Notifications, Rutas.NOTIFICACIONES)
    )
    Rol.INVESTIGADOR -> listOf(
        Acceso("Mapa", Icons.Filled.Map, Rutas.MAPA),
        Acceso("Escanear QR", Icons.Filled.QrCodeScanner, Rutas.ESCANER_QR),
        Acceso("Avisos", Icons.Filled.Notifications, Rutas.NOTIFICACIONES)
    )
}

@Composable
fun DashboardScreen(
    nombre: String,
    rol: Rol,
    avatar: Int,
    onNavegar: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        containerColor = Crema,
        bottomBar = { BarraNavegacion(Rutas.DASHBOARD, rol, onNavegar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Encabezado(nombre, rol, avatar) { onNavegar(Rutas.NOTIFICACIONES) }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 6 }
            ) {
                Column(Modifier.padding(16.dp)) {
                    TituloSeccion("Accesos rápidos")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(accesosPara(rol)) { acceso ->
                            AccesoRapido(acceso) { onNavegar(acceso.ruta) }
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // El contenido principal depende del rol
                    when (rol) {
                        Rol.CLIENTE -> ContenidoCliente(onNavegar)
                        Rol.ADMINISTRADOR -> ContenidoAdministrador(onNavegar)
                        Rol.INVESTIGADOR -> ContenidoInvestigador(onNavegar)
                    }
                }
            }
        }
    }
}

@Composable
private fun Encabezado(nombre: String, rol: Rol, avatar: Int, onCampana: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                AzulOscuro,
                RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "¡Hola, ${nombre.substringBefore(" ")}!",
                color = Blanco,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Perfil ${rol.titulo}", color = Dorado, fontSize = 13.sp)
            Text(
                text = "Sigamos explorando el conocimiento del mundo.",
                color = Crema,
                fontSize = 13.sp
            )
        }
        IconButton(onClick = onCampana) {
            Icon(Icons.Filled.Notifications, "Notificaciones", tint = Dorado)
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(coloresAvatar[avatar.coerceIn(0, coloresAvatar.lastIndex)]),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Person, "Tu avatar", tint = Blanco)
        }
    }
}

@Composable
private fun AccesoRapido(acceso: Acceso, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(AzulOscuro),
            contentAlignment = Alignment.Center
        ) {
            Icon(acceso.icono, null, tint = Dorado)
        }
        Spacer(Modifier.height(4.dp))
        Text(acceso.etiqueta, color = AzulOscuro, fontSize = 12.sp)
    }
}

@Composable
private fun TituloSeccion(texto: String, accion: String? = null, onAccion: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = texto,
            color = AzulOscuro,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (accion != null) {
            TextButton(onClick = onAccion) { Text(accion, color = AzulOscuro) }
        }
    }
}

@Composable
private fun TarjetaExplorar(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    fondo: Color,
    colorTexto: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = fondo)
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icono, null, tint = colorTexto)
            Spacer(Modifier.height(8.dp))
            Text(titulo, color = colorTexto, fontWeight = FontWeight.Bold)
            Text(subtitulo, color = colorTexto.copy(alpha = 0.8f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun ItemLista(titulo: String, detalle: String, icono: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Blanco)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AzulOscuro),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = Dorado)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, color = AzulOscuro, fontWeight = FontWeight.SemiBold)
                Text(detalle, color = AzulOscuro.copy(alpha = 0.7f), fontSize = 13.sp)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = AzulOscuro)
        }
    }
}

@Composable
private fun Estadistica(valor: String, etiqueta: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AzulOscuro)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(valor, color = Dorado, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(etiqueta, color = Crema, fontSize = 12.sp)
        }
    }
}

// ---------- Contenido según el rol ----------

@Composable
private fun ContenidoCliente(onNavegar: (String) -> Unit) {
    Column {
        TituloSeccion("Explora")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaExplorar(
                "Cursos", "Aprende con expertos", Icons.Filled.School,
                AzulOscuro, Blanco, Modifier.weight(1f)
            ) { onNavegar(Rutas.CATALOGO) }
            TarjetaExplorar(
                "Productos", "Libros y más", Icons.Filled.ShoppingBag,
                Dorado, AzulOscuro, Modifier.weight(1f)
            ) { onNavegar(Rutas.CATALOGO) }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaExplorar(
                "Museos virtuales", "Recorre Iberoamérica", Icons.Filled.AccountBalance,
                Blanco, AzulOscuro, Modifier.weight(1f)
            ) { onNavegar(Rutas.MAPA) }
            TarjetaExplorar(
                "Novedades", "Exposiciones y eventos", Icons.Filled.Campaign,
                Blanco, AzulOscuro, Modifier.weight(1f)
            ) { onNavegar(Rutas.NOTIFICACIONES) }
        }
        Spacer(Modifier.height(16.dp))

        TituloSeccion("Destacados", "Ver todos") { onNavegar(Rutas.MAPA) }
        museosDestacados.forEach { (museo, lugar) ->
            ItemLista(museo, lugar, Icons.Filled.AccountBalance) { onNavegar(Rutas.MAPA) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ContenidoAdministrador(onNavegar: (String) -> Unit) {
    Column {
        TituloSeccion("Resumen de gestión")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Estadistica("3", "Pendientes", Modifier.weight(1f))
            Estadistica("12", "Productos", Modifier.weight(1f))
            Estadistica("5", "Cursos", Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))

        TituloSeccion("Solicitudes recientes", "Ver todas") { onNavegar(Rutas.HISTORIAL) }
        solicitudesRecientes.forEach { (numero, estado) ->
            ItemLista(numero, estado, Icons.Filled.Receipt) { onNavegar(Rutas.HISTORIAL) }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(8.dp))
        TituloSeccion("Novedades de gestión")
        ItemLista("Nuevo curso cargado", "Curso de Museología Digital", Icons.Filled.Campaign) {
            onNavegar(Rutas.NOTIFICACIONES)
        }
    }
}

@Composable
private fun ContenidoInvestigador(onNavegar: (String) -> Unit) {
    Column {
        TituloSeccion("Mis registros museológicos", "Ver mapa") { onNavegar(Rutas.MAPA) }
        registrosMuseologicos.forEach { (museo, estado) ->
            ItemLista(museo, estado, Icons.Filled.AccountBalance) { onNavegar(Rutas.MAPA) }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { onNavegar(Rutas.ESCANER_QR) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro)
        ) {
            Icon(Icons.Filled.QrCodeScanner, null)
            Spacer(Modifier.width(8.dp))
            Text("Probar un código QR")
        }
    }
}

// Tres vistas previas, para ver cada rol sin tener que iniciar sesión
@Preview(showBackground = true, heightDp = 800)
@Composable
fun DashboardClientePreview() {
    DashboardScreen("María Fernanda López", Rol.CLIENTE, 0) {}
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun DashboardAdministradorPreview() {
    DashboardScreen("Administrador Demo", Rol.ADMINISTRADOR, 1) {}
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun DashboardInvestigadorPreview() {
    DashboardScreen("Investigador Demo", Rol.INVESTIGADOR, 2) {}
}