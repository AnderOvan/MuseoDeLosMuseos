package com.example.museodelosmuseos.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.museodelosmuseos.repository.CatalogoQr
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Blanco
import com.example.museodelosmuseos.ui.theme.Crema
import com.example.museodelosmuseos.ui.theme.Dorado
import com.example.museodelosmuseos.util.QrLeido
import com.example.museodelosmuseos.util.TipoContenidoQr
import com.example.museodelosmuseos.util.ValidadorQr
import com.example.museodelosmuseos.viewmodel.EstadoLectura

private val VERDE_EXITO = Color(0xFF2E9E6B)
private val ROJO_ERROR = Color(0xFFC62828)

// Color e ícono de cada tipo de contenido (también los usa el detalle del producto)
fun colorTipoQr(tipo: TipoContenidoQr): Color = when (tipo) {
    TipoContenidoQr.MUSEO -> Color(0xFF1B4965)
    TipoContenidoQr.PRODUCTO -> Color(0xFFB5523B)
    TipoContenidoQr.PUBLICACION -> Color(0xFF2E7D8C)
    TipoContenidoQr.DESCONOCIDO -> Color.Gray
}

fun iconoTipoQr(tipo: TipoContenidoQr): ImageVector = when (tipo) {
    TipoContenidoQr.MUSEO -> Icons.Filled.AccountBalance
    TipoContenidoQr.PRODUCTO -> Icons.Filled.ShoppingBag
    TipoContenidoQr.PUBLICACION -> Icons.AutoMirrored.Filled.MenuBook
    TipoContenidoQr.DESCONOCIDO -> Icons.Filled.QrCode
}

@Composable
fun ResultadoQrScreen(
    estado: String,       // GUARDADO, DUPLICADO, INVALIDO o SIN_SESION
    contenido: String,    // texto leído en el QR
    onVerDetalle: (QrLeido) -> Unit,
    onVolverAEscanear: () -> Unit
) {
    val qr = remember(contenido) { ValidadorQr.analizar(contenido) }
    val ficha = remember(qr) { CatalogoQr.buscar(qr) }
    val exito = ficha != null &&
            (estado == EstadoLectura.GUARDADO.name || estado == EstadoLectura.DUPLICADO.name)

    // ---- Animación de lectura: el círculo "rebota" y luego aparece la ficha ----
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val escala by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "escalaCheck"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth()) {
            IconButton(onClick = onVolverAEscanear) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = AzulOscuro)
            }
        }

        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(96.dp)
                .scale(escala)
                .background(if (exito) VERDE_EXITO else ROJO_ERROR, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (exito) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                tint = Blanco,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = when {
                exito -> "¡Código identificado!"
                estado == EstadoLectura.SIN_SESION.name -> "Inicia sesión"
                else -> "Código no reconocido"
            },
            color = AzulOscuro,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = when {
                exito -> "Hemos encontrado la siguiente información:"
                estado == EstadoLectura.SIN_SESION.name -> "Necesitas una cuenta para guardar tus lecturas."
                else -> "Este código no corresponde a un museo, publicación o producto del catálogo."
            },
            color = AzulOscuro.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        if (exito && ficha != null) {
            androidx.compose.animation.AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { it / 4 }
            ) {
                Column {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Blanco),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Imagen referencial del contenido
                                Box(
                                    modifier = Modifier
                                        .size(84.dp)
                                        .background(colorTipoQr(ficha.tipo), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(iconoTipoQr(ficha.tipo), null, tint = Blanco, modifier = Modifier.size(42.dp))
                                }
                                Spacer(Modifier.width(14.dp))
                                Column {
                                    Text(ficha.titulo, color = AzulOscuro, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(6.dp))
                                    Etiqueta(ficha.etiqueta, colorTipoQr(ficha.tipo))
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(ficha.fuente, color = AzulOscuro, fontWeight = FontWeight.SemiBold)
                            Text(ficha.descripcion, color = AzulOscuro.copy(alpha = 0.8f), fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (estado == EstadoLectura.DUPLICADO.name)
                            "Ya estaba en tu historial: no se volvió a guardar."
                        else
                            "Guardado en tu historial.",
                        color = AzulOscuro.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onVerDetalle(qr) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro)
            ) {
                Text("Ver detalle", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        } else {
            // Muestra qué se leyó, para que el usuario entienda el error
            Text(
                "Código leído: $contenido",
                color = AzulOscuro,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Blanco, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            )
            Spacer(Modifier.height(24.dp))
        }

        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onVolverAEscanear,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.Refresh, null, tint = AzulOscuro)
            Spacer(Modifier.width(8.dp))
            Text("Volver a escanear", color = AzulOscuro, fontWeight = FontWeight.SemiBold)
        }
    }
}