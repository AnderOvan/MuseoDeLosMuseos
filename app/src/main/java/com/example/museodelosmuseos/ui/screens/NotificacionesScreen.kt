package com.example.museodelosmuseos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.model.TipoNotificacion
import com.example.museodelosmuseos.repository.local.NotificacionEntity
import com.example.museodelosmuseos.ui.components.BarraNavegacion
import com.example.museodelosmuseos.ui.navigation.Rutas
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Blanco
import com.example.museodelosmuseos.ui.theme.Crema
import com.example.museodelosmuseos.ui.theme.Dorado
import com.example.museodelosmuseos.viewmodel.NotificacionesViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val formatoFechaAviso = SimpleDateFormat("dd-MM HH:mm", Locale.getDefault())

// A qué pantalla lleva cada tipo de aviso (null = no lleva a ninguna)
private fun rutaDeAviso(tipo: TipoNotificacion): String? = when (tipo) {
    TipoNotificacion.PEDIDO -> Rutas.HISTORIAL
    TipoNotificacion.CURSO -> Rutas.CATALOGO
    TipoNotificacion.MUSEO -> Rutas.MAPA
    TipoNotificacion.AVISO -> null
}

private fun iconoDeAviso(tipo: TipoNotificacion): ImageVector = when (tipo) {
    TipoNotificacion.PEDIDO -> Icons.Filled.Receipt
    TipoNotificacion.CURSO -> Icons.AutoMirrored.Filled.MenuBook
    TipoNotificacion.MUSEO -> Icons.Filled.AccountBalance
    TipoNotificacion.AVISO -> Icons.Filled.Campaign
}

private fun colorDeAviso(tipo: TipoNotificacion): Color = when (tipo) {
    TipoNotificacion.PEDIDO -> Color(0xFF1565C0)
    TipoNotificacion.CURSO -> Color(0xFF7B5EA7)
    TipoNotificacion.MUSEO -> Color(0xFF2E7D8C)
    TipoNotificacion.AVISO -> Color(0xFFC9A961)
}

@Composable
fun NotificacionesScreen(
    viewModel: NotificacionesViewModel,
    onAbrirDestino: (String) -> Unit,   // ruta de la pantalla relacionada
    onNavegar: (String) -> Unit,        // barra inferior
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Crema,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { BarraNavegacion(rutaActual = Rutas.NOTIFICACIONES, onNavegar = onNavegar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // ---- Encabezado ----
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = AzulOscuro)
                }
                Column(Modifier.weight(1f)) {
                    Text("Notificaciones", color = AzulOscuro, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (estado.sinLeer == 0) "Todo al día" else "${estado.sinLeer} sin leer",
                        color = AzulOscuro.copy(alpha = 0.6f), fontSize = 13.sp
                    )
                }
                TextButton(onClick = viewModel::marcarTodasLeidas, enabled = estado.sinLeer > 0) {
                    Text("Marcar todas", color = AzulOscuro)
                }
            }

            // ---- Solo Administrador: avisos de prueba ----
            if (estado.esAdmin) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.enviarAvisoDePrueba()
                        scope.launch { snackbar.showSnackbar("Aviso de prueba enviado") }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro)
                ) {
                    Icon(Icons.Filled.Campaign, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Enviar aviso de prueba", fontWeight = FontWeight.Bold)
                }
            }

            // ---- Aviso si el usuario apagó las notificaciones en el Perfil ----
            if (!estado.activas) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Las notificaciones nuevas están desactivadas. Puedes activarlas en Perfil.",
                    color = AzulOscuro,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Dorado.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                )
            }
            Spacer(Modifier.height(12.dp))

            if (estado.notificaciones.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.NotificationsNone, null, tint = Dorado, modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No tienes notificaciones",
                        color = AzulOscuro, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                    )
                    Text(
                        "Aquí aparecerán avisos de cursos, museos y cambios de tus pedidos.",
                        color = AzulOscuro.copy(alpha = 0.7f), textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(estado.notificaciones, key = { it.id }) { aviso ->
                        TarjetaAviso(
                            aviso = aviso,
                            onClick = {
                                viewModel.marcarLeida(aviso)
                                rutaDeAviso(aviso.tipo)?.let(onAbrirDestino)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaAviso(aviso: NotificacionEntity, onClick: () -> Unit) {
    val color = colorDeAviso(aviso.tipo)
    val destino = rutaDeAviso(aviso.tipo)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (aviso.leida) Blanco.copy(alpha = 0.65f) else Blanco),
        border = if (aviso.leida) null else BorderStroke(1.5.dp, Dorado),
        elevation = CardDefaults.cardElevation(defaultElevation = if (aviso.leida) 0.dp else 2.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconoDeAviso(aviso.tipo), null, tint = Blanco, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    aviso.titulo,
                    color = AzulOscuro,
                    fontWeight = if (aviso.leida) FontWeight.Normal else FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(aviso.mensaje, color = AzulOscuro.copy(alpha = 0.75f), fontSize = 13.sp)
                Text(
                    formatoFechaAviso.format(Date(aviso.fecha)),
                    color = AzulOscuro.copy(alpha = 0.5f), fontSize = 11.sp
                )
            }
            if (destino != null) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "Abrir", tint = AzulOscuro)
            }
        }
    }
}