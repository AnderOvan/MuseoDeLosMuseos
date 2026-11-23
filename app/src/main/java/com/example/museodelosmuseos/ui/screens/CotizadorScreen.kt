package com.example.museodelosmuseos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.repository.ItemCarrito
import com.example.museodelosmuseos.ui.components.BarraNavegacion
import com.example.museodelosmuseos.ui.navigation.Rutas
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Blanco
import com.example.museodelosmuseos.ui.theme.Crema
import com.example.museodelosmuseos.ui.theme.Dorado
import com.example.museodelosmuseos.util.CotizadorEnvios
import com.example.museodelosmuseos.viewmodel.CotizadorUiState
import com.example.museodelosmuseos.viewmodel.CotizadorViewModel
import com.example.museodelosmuseos.viewmodel.PedidoConfirmado
import com.example.museodelosmuseos.viewmodel.TipoEnvio

private val VERDE_COT = Color(0xFF2E7D32)

private fun colorItem(categoria: String): Color = when (categoria) {
    "Curso" -> Color(0xFF1B4965)
    "Libro" -> Color(0xFF8A5A44)
    else -> Color(0xFF5F7A61)
}

private fun iconoItem(categoria: String): ImageVector = when (categoria) {
    "Curso" -> Icons.Filled.School
    "Libro" -> Icons.AutoMirrored.Filled.MenuBook
    else -> Icons.Filled.Inventory2
}

@Composable
fun CotizadorScreen(
    viewModel: CotizadorViewModel,
    onIrAlCatalogo: () -> Unit,
    onVolverAlInicio: () -> Unit,
    onNavegar: (String) -> Unit, // barra inferior
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Crema,
        bottomBar = { BarraNavegacion(rutaActual = Rutas.CARRITO, onNavegar = onNavegar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ---- Encabezado ----
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = AzulOscuro)
                }
                Text(
                    "Carrito & Cotización de Envíos",
                    color = AzulOscuro,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            val pedido = estado.pedido
            when {
                pedido != null -> PedidoExitoso(
                    pedido = pedido,
                    onVolverAlInicio = { viewModel.cerrarPedido(); onVolverAlInicio() },
                    onSeguirComprando = { viewModel.cerrarPedido(); onIrAlCatalogo() }
                )

                estado.items.isEmpty() -> CarritoVacio(onIrAlCatalogo)

                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // ---- Productos del carrito ----
                    items(estado.items, key = { it.producto.id }) { item ->
                        FilaCarrito(
                            item = item,
                            onMas = { viewModel.incrementar(item) },
                            onMenos = { viewModel.decrementar(item) },
                            onEliminar = { viewModel.eliminar(item) }
                        )
                    }

                    // ---- Cotizador de envío ----
                    item { TarjetaEnvio(estado, viewModel) }

                    // ---- Resumen ----
                    item { Resumen(estado) }

                    // ---- Confirmar ----
                    item {
                        Button(
                            onClick = viewModel::solicitarConfirmacion,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro)
                        ) {
                            Icon(Icons.Filled.Lock, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Confirmar pedido", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // ---- Diálogo: muestra el total final antes de enviar la solicitud ----
    if (estado.mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { if (!estado.procesando) viewModel.cancelarConfirmacion() },
            title = { Text("Confirmar pedido", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${estado.items.sumOf { it.cantidad }} artículos · destino: ${estado.pais}")
                    Text("Envío ${estado.tipoEnvio.titulo.lowercase()} (${estado.plazo})")
                    Spacer(Modifier.height(6.dp))
                    Text("Total final: ${formatearPrecio(estado.total)}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (estado.procesando) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = AzulOscuro, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Enviando pedido...")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::procesarPago,
                    enabled = !estado.procesando,
                    colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro)
                ) { Text("Pagar ${formatearPrecio(estado.total)}") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarConfirmacion, enabled = !estado.procesando) {
                    Text("Cancelar", color = AzulOscuro)
                }
            }
        )
    }
}

@Composable
private fun FilaCarrito(item: ItemCarrito, onMas: () -> Unit, onMenos: () -> Unit, onEliminar: () -> Unit) {
    val producto = item.producto
    val esCurso = producto.categoria == "Curso"
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(colorItem(producto.categoria), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconoItem(producto.categoria), null, tint = Blanco, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(producto.titulo, color = AzulOscuro, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2)
                Text(formatearPrecio(producto.precio), color = AzulOscuro.copy(alpha = 0.7f), fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                if (esCurso) {
                    Text("Curso digital · 1 acceso", color = VERDE_COT, fontSize = 12.sp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BotonMini(Icons.Filled.Remove, "Restar", habilitado = item.cantidad > 1, onClick = onMenos)
                        Text("${item.cantidad}", color = AzulOscuro, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp))
                        BotonMini(Icons.Filled.Add, "Sumar", habilitado = item.cantidad < producto.stock, onClick = onMas)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Filled.Delete, "Eliminar", tint = AzulOscuro.copy(alpha = 0.6f))
                }
                Text(formatearPrecio(item.subtotal), color = AzulOscuro, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BotonMini(icono: ImageVector, descripcion: String, habilitado: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier
            .size(30.dp)
            .border(1.dp, if (habilitado) AzulOscuro else AzulOscuro.copy(alpha = 0.25f), CircleShape)
    ) {
        Icon(icono, descripcion, tint = if (habilitado) AzulOscuro else AzulOscuro.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun TarjetaEnvio(estado: CotizadorUiState, viewModel: CotizadorViewModel) {
    var menuAbierto by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Calcular envío", color = AzulOscuro, fontSize = 17.sp, fontWeight = FontWeight.Bold)

            if (estado.soloDigital) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Tu pedido es 100% digital: no tiene costo de despacho.",
                    color = VERDE_COT,
                    fontSize = 13.sp
                )
                return@Column
            }

            // ---- País de destino ----
            Spacer(Modifier.height(10.dp))
            Text("País de destino", color = AzulOscuro.copy(alpha = 0.7f), fontSize = 13.sp)
            Box {
                OutlinedButton(
                    onClick = { menuAbierto = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Public, null, tint = AzulOscuro)
                    Spacer(Modifier.width(8.dp))
                    Text(estado.pais, color = AzulOscuro, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ArrowDropDown, null, tint = AzulOscuro)
                }
                DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                    CotizadorEnvios.PAISES.forEach { pais ->
                        DropdownMenuItem(
                            text = { Text(if (CotizadorEnvios.esNacional(pais)) "$pais (nacional)" else pais) },
                            onClick = {
                                viewModel.seleccionarPais(pais)
                                menuAbierto = false
                            }
                        )
                    }
                }
            }

            // ---- Tipo de envío ----
            Spacer(Modifier.height(12.dp))
            Text("Tipo de envío", color = AzulOscuro.copy(alpha = 0.7f), fontSize = 13.sp)
            TipoEnvio.entries.forEach { tipo ->
                val esExpress = tipo == TipoEnvio.EXPRESS
                val costo = CotizadorEnvios.calcularCostoEnvio(estado.pais, esExpress, estado.subtotal)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.seleccionarTipoEnvio(tipo) }
                        .padding(vertical = 2.dp)
                ) {
                    RadioButton(
                        selected = estado.tipoEnvio == tipo,
                        onClick = { viewModel.seleccionarTipoEnvio(tipo) },
                        colors = RadioButtonDefaults.colors(selectedColor = AzulOscuro)
                    )
                    Icon(Icons.Filled.LocalShipping, null, tint = AzulOscuro.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${tipo.titulo} (${CotizadorEnvios.plazoEntrega(estado.pais, esExpress)})",
                        color = AzulOscuro,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        if (costo == 0.0) "Gratis" else formatearPrecio(costo),
                        color = if (costo == 0.0) VERDE_COT else AzulOscuro,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (estado.faltaParaGratis > 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Te faltan ${formatearPrecio(estado.faltaParaGratis)} para envío estándar gratis en Chile",
                    color = AzulOscuro.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun Resumen(estado: CotizadorUiState) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FilaResumen("Subtotal", formatearPrecio(estado.subtotal))
            FilaResumen(
                "Envío",
                when {
                    estado.soloDigital -> "No aplica"
                    estado.envio == 0.0 -> "Gratis"
                    else -> formatearPrecio(estado.envio)
                }
            )
            HorizontalDivider(color = AzulOscuro.copy(alpha = 0.1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total", color = AzulOscuro, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(formatearPrecio(estado.total), color = Dorado, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun FilaResumen(nombre: String, valor: String) {
    Row {
        Text(nombre, color = AzulOscuro.copy(alpha = 0.7f), modifier = Modifier.weight(1f))
        Text(valor, color = AzulOscuro, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CarritoVacio(onIrAlCatalogo: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.ShoppingCart, null, tint = Dorado, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text("Tu carrito está vacío", color = AzulOscuro, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            "Agrega libros, materiales o cursos desde el catálogo.",
            color = AzulOscuro.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onIrAlCatalogo,
            colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro)
        ) { Text("Ir al Catálogo", fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun PedidoExitoso(pedido: PedidoConfirmado, onVolverAlInicio: () -> Unit, onSeguirComprando: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.CheckCircle, null, tint = VERDE_COT, modifier = Modifier.size(96.dp))
        Spacer(Modifier.height(16.dp))
        Text("¡Pedido confirmado!", color = AzulOscuro, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("N° ${pedido.numero}", color = AzulOscuro.copy(alpha = 0.7f))
        Spacer(Modifier.height(12.dp))
        Text("Total pagado: ${formatearPrecio(pedido.total)}", color = AzulOscuro, fontWeight = FontWeight.SemiBold)
        Text(
            "Destino: ${pedido.pais} · ${pedido.tipoEnvio.titulo} (${pedido.plazo})",
            color = AzulOscuro.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Text("Estado: en preparación", color = VERDE_COT, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onVolverAlInicio,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro)
        ) { Text("Volver al inicio", fontWeight = FontWeight.Bold) }
        TextButton(onClick = onSeguirComprando) { Text("Seguir comprando", color = AzulOscuro) }
    }
}