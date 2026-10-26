package com.example.museodelosmuseos.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.repository.local.ProductoCacheEntity
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Blanco
import com.example.museodelosmuseos.ui.theme.Crema
import com.example.museodelosmuseos.ui.theme.Dorado
import com.example.museodelosmuseos.viewmodel.DetalleProductoViewModel
import com.example.museodelosmuseos.viewmodel.MensajeCompra
import kotlinx.coroutines.delay

private val VERDE_OK = Color(0xFF2E7D32)
private val ROJO_ERROR = Color(0xFFC62828)

private fun fondoProducto(categoria: String): Color = when (categoria) {
    "Curso" -> Color(0xFF1B4965)
    "Libro" -> Color(0xFF8A5A44)
    else -> Color(0xFF5F7A61)
}

private fun iconoDeProducto(categoria: String): ImageVector = when (categoria) {
    "Curso" -> Icons.Filled.School
    "Libro" -> Icons.AutoMirrored.Filled.MenuBook
    else -> Icons.Filled.Inventory2
}

// Detalle extendido según el tipo de artículo
private fun detalleExtendido(producto: ProductoCacheEntity): String = when (producto.categoria) {
    "Curso" -> "Curso 100% en línea. Incluye clases en video, material descargable, " +
            "actividades prácticas y certificado de participación."
    "Libro" -> "Edición impresa a color, con ilustraciones y fuentes de consulta. " +
            "Despacho a todo Chile y al extranjero."
    else -> "Material seleccionado por el equipo de conservación del Museo de los Museos. " +
            "Producto de demostración con precio de prueba."
}

@Composable
fun DetalleProductoScreen(
    viewModel: DetalleProductoViewModel,
    onVolver: () -> Unit,
    onIrAlCarrito: () -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val producto = estado.producto

    // El mensaje (éxito o error) se oculta solo después de unos segundos
    LaunchedEffect(estado.mensaje) {
        if (estado.mensaje != null) {
            delay(3_000)
            viewModel.limpiarMensaje()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema)
    ) {
        if (producto == null) {
            Column(Modifier.padding(24.dp)) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = AzulOscuro)
                }
                Text("Producto no encontrado", color = AzulOscuro, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            return@Box
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ---- Imagen representativa + volver + categoría en dorado ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Brush.verticalGradient(listOf(fondoProducto(producto.categoria), AzulOscuro)))
            ) {
                Icon(
                    iconoDeProducto(producto.categoria),
                    contentDescription = null,
                    tint = Blanco.copy(alpha = 0.9f),
                    modifier = Modifier.size(100.dp).align(Alignment.Center)
                )
                IconButton(
                    onClick = onVolver,
                    modifier = Modifier
                        .padding(12.dp)
                        .background(Blanco, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = AzulOscuro)
                }
                Text(
                    producto.categoria,
                    color = AzulOscuro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .background(Dorado, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Column(Modifier.padding(20.dp)) {
                // ---- Título, precio y disponibilidad ----
                Text(producto.titulo, color = AzulOscuro, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(formatearPrecio(producto.precio), color = AzulOscuro, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(6.dp))
                Disponibilidad(producto, estado.enCarrito)

                // ---- Descripción ----
                Spacer(Modifier.height(16.dp))
                Text("Descripción", color = AzulOscuro, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(producto.descripcion, color = AzulOscuro.copy(alpha = 0.85f))
                Spacer(Modifier.height(6.dp))
                Text(detalleExtendido(producto), color = AzulOscuro.copy(alpha = 0.7f), fontSize = 14.sp)

                // ---- Características ----
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Caracteristica("Categoría", producto.categoria)
                        Caracteristica("Código", producto.id)
                        Caracteristica("Formato", if (estado.esCurso) "Digital (en línea)" else "Físico, con despacho")
                    }
                }

                // ---- Cantidad (los cursos se compran de a uno) ----
                if (!estado.esCurso) {
                    Spacer(Modifier.height(20.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Cantidad", color = AzulOscuro, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        BotonCantidad(Icons.Filled.Remove, "Restar", habilitado = estado.cantidad > 1) {
                            viewModel.decrementarCantidad()
                        }
                        Text(
                            "${estado.cantidad}",
                            color = AzulOscuro,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 18.dp)
                        )
                        BotonCantidad(Icons.Filled.Add, "Sumar", habilitado = estado.cantidad < estado.maximo) {
                            viewModel.incrementarCantidad()
                        }
                    }
                }

                // ---- Agregar al carrito (con feedback visual) ----
                Spacer(Modifier.height(24.dp))
                val exito = estado.mensaje is MensajeCompra.Exito
                val colorBoton by animateColorAsState(
                    targetValue = if (exito) VERDE_OK else Dorado,
                    label = "colorBotonCarrito"
                )
                Button(
                    onClick = viewModel::agregarAlCarrito,
                    enabled = !estado.validando && !estado.agotado,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorBoton,
                        contentColor = if (exito) Blanco else AzulOscuro
                    )
                ) {
                    if (estado.validando) {
                        CircularProgressIndicator(color = AzulOscuro, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Validando disponibilidad...")
                    } else {
                        Icon(if (exito) Icons.Filled.CheckCircle else Icons.Filled.ShoppingCart, null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            when {
                                exito -> "¡Agregado!"
                                estado.agotado && estado.esCurso -> "Ya está en tu carrito"
                                estado.agotado -> "Sin stock disponible"
                                else -> "Agregar al carrito"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // ---- Cursos: solicitar información ----
                if (estado.esCurso) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = viewModel::solicitarInformacion,
                        enabled = !estado.validando,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.MailOutline, null, tint = AzulOscuro)
                        Spacer(Modifier.width(8.dp))
                        Text("Solicitar información", color = AzulOscuro, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (estado.enCarrito > 0) {
                    TextButton(onClick = onIrAlCarrito, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Ver carrito (${estado.enCarrito} en el carrito)", color = AzulOscuro)
                    }
                }
                Spacer(Modifier.height(80.dp)) // espacio para el mensaje flotante
            }
        }

        // ---- Mensaje animado de éxito o error ----
        androidx.compose.animation.AnimatedVisibility(
            visible = estado.mensaje != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            val mensaje = estado.mensaje
            val esError = mensaje is MensajeCompra.Error
            val texto = when (mensaje) {
                is MensajeCompra.Exito -> mensaje.texto
                is MensajeCompra.Error -> mensaje.texto
                null -> ""
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (esError) ROJO_ERROR else VERDE_OK, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Icon(if (esError) Icons.Filled.Error else Icons.Filled.CheckCircle, null, tint = Blanco)
                Spacer(Modifier.width(10.dp))
                Text(texto, color = Blanco, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun Disponibilidad(producto: ProductoCacheEntity, enCarrito: Int) {
    val (texto, color) = when {
        producto.categoria == "Curso" -> "Acceso digital inmediato" to VERDE_OK
        producto.stock == 0 -> "Agotado" to ROJO_ERROR
        producto.stock <= 5 -> "Disponibles: ${producto.stock} unidades (¡últimas!)" to Color(0xFFE65100)
        else -> "Disponibles: ${producto.stock} unidades" to VERDE_OK
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(texto, color = color, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
    if (enCarrito > 0 && producto.categoria != "Curso") {
        Text("Ya tienes $enCarrito en tu carrito", color = AzulOscuro.copy(alpha = 0.6f), fontSize = 12.sp)
    }
}

@Composable
private fun Caracteristica(nombre: String, valor: String) {
    Row {
        Text("$nombre:", color = AzulOscuro.copy(alpha = 0.6f), fontSize = 14.sp, modifier = Modifier.width(90.dp))
        Text(valor, color = AzulOscuro, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BotonCantidad(icono: ImageVector, descripcion: String, habilitado: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier
            .size(40.dp)
            .border(1.5.dp, if (habilitado) AzulOscuro else AzulOscuro.copy(alpha = 0.25f), CircleShape)
    ) {
        Icon(icono, descripcion, tint = if (habilitado) AzulOscuro else AzulOscuro.copy(alpha = 0.3f))
    }
}