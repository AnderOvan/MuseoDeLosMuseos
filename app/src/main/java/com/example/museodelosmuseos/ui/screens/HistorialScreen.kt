package com.example.museodelosmuseos.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.model.EstadoPedido
import com.example.museodelosmuseos.repository.local.PedidoEntity
import com.example.museodelosmuseos.ui.components.BarraNavegacion
import com.example.museodelosmuseos.ui.navigation.Rutas
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Blanco
import com.example.museodelosmuseos.ui.theme.Crema
import com.example.museodelosmuseos.ui.theme.Dorado
import com.example.museodelosmuseos.viewmodel.HistorialViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val formatoFechaPedido = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

private fun colorDeEstado(estado: EstadoPedido): Color = when (estado) {
    EstadoPedido.PENDIENTE -> Color(0xFFE65100)
    EstadoPedido.CONFIRMADO -> Color(0xFF1565C0)
    EstadoPedido.ENVIADO -> Color(0xFF2E7D8C)
    EstadoPedido.ENTREGADO -> Color(0xFF2E7D32)
}

@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel,
    onIrAlCatalogo: () -> Unit,
    onNavegar: (String) -> Unit,   // barra inferior
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Crema,
        bottomBar = { BarraNavegacion(rutaActual = Rutas.HISTORIAL, onNavegar = onNavegar) }
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
                    Text(
                        if (estado.esAdmin) "Solicitudes recibidas" else "Mis solicitudes",
                        color = AzulOscuro, fontSize = 24.sp, fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${estado.totalPedidos} en total",
                        color = AzulOscuro.copy(alpha = 0.6f), fontSize = 13.sp
                    )
                }
            }

            // ---- Filtro por estado ----
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChipEstado("Todos", estado.filtro == null) { viewModel.filtrar(null) }
                EstadoPedido.entries.forEach { e ->
                    ChipEstado(e.titulo, estado.filtro == e) { viewModel.filtrar(e) }
                }
            }
            Spacer(Modifier.height(12.dp))

            if (estado.pedidos.isEmpty()) {
                HistorialVacio(
                    hayFiltro = estado.filtro != null,
                    esAdmin = estado.esAdmin,
                    onIrAlCatalogo = onIrAlCatalogo
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(estado.pedidos, key = { it.id }) { pedido ->
                        TarjetaPedido(
                            pedido = pedido,
                            esAdmin = estado.esAdmin,
                            onCambiarEstado = { nuevo -> viewModel.cambiarEstado(pedido, nuevo) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipEstado(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(texto) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AzulOscuro,
            selectedLabelColor = Blanco,
            containerColor = Blanco,
            labelColor = AzulOscuro
        )
    )
}

@Composable
private fun TarjetaPedido(
    pedido: PedidoEntity,
    esAdmin: Boolean,
    onCambiarEstado: (EstadoPedido) -> Unit
) {
    // El color cambia con una animación cuando el Administrador actualiza el estado
    val color by animateColorAsState(colorDeEstado(pedido.estado), label = "colorEstado")
    var menuAbierto by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .animateContentSize()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Pedido ${pedido.numero}", color = AzulOscuro, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        formatoFechaPedido.format(Date(pedido.fecha)),
                        color = AzulOscuro.copy(alpha = 0.6f), fontSize = 12.sp
                    )
                }
                Text(
                    pedido.estado.titulo,
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(color.copy(alpha = 0.12f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            if (esAdmin) {
                Spacer(Modifier.height(6.dp))
                Text("Cliente: ${pedido.clienteNombre}", color = AzulOscuro, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(6.dp))
            Text(pedido.resumen, color = AzulOscuro.copy(alpha = 0.85f), fontSize = 14.sp)
            Text(
                "Destino: ${pedido.destino} · ${pedido.tipoEnvio}",
                color = AzulOscuro.copy(alpha = 0.6f), fontSize = 12.sp
            )

            // Avance del pedido: Pendiente -> Confirmado -> Enviado -> Entregado
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { (pedido.estado.ordinal + 1) / EstadoPedido.entries.size.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = color,
                trackColor = color.copy(alpha = 0.15f)
            )

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total", color = AzulOscuro.copy(alpha = 0.7f), fontSize = 13.sp)
                Spacer(Modifier.weight(1f))
                Text(formatearPrecio(pedido.total), color = AzulOscuro, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            // Solo el Administrador puede cambiar el estado
            if (esAdmin) {
                Spacer(Modifier.height(8.dp))
                Box {
                    OutlinedButton(onClick = { menuAbierto = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Edit, null, modifier = Modifier.size(18.dp), tint = AzulOscuro)
                        Spacer(Modifier.width(6.dp))
                        Text("Cambiar estado", color = AzulOscuro)
                    }
                    DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                        EstadoPedido.entries.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion.titulo) },
                                onClick = {
                                    menuAbierto = false
                                    onCambiarEstado(opcion)
                                },
                                // Marca con un check el estado actual
                                leadingIcon = {
                                    if (opcion == pedido.estado) {
                                        Icon(Icons.Filled.Check, null, tint = colorDeEstado(opcion))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorialVacio(hayFiltro: Boolean, esAdmin: Boolean, onIrAlCatalogo: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Receipt, null, tint = Dorado, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            when {
                hayFiltro -> "No hay solicitudes con este estado"
                esAdmin -> "Aún no hay solicitudes"
                else -> "Aún no tienes solicitudes"
            },
            color = AzulOscuro, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
        )
        if (!hayFiltro) {
            Spacer(Modifier.height(6.dp))
            Text(
                if (esAdmin) "Cuando un cliente confirme un pedido, aparecerá aquí."
                else "Cuando confirmes un pedido en el carrito, lo verás aquí.",
                color = AzulOscuro.copy(alpha = 0.7f), textAlign = TextAlign.Center
            )
            if (!esAdmin) {
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onIrAlCatalogo,
                    colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro)
                ) { Text("Ir al catálogo", fontWeight = FontWeight.Bold) }
            }
        }
    }
}