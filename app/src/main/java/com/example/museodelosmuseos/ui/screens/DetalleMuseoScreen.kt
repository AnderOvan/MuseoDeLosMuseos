package com.example.museodelosmuseos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.model.Museo
import com.example.museodelosmuseos.ui.components.BarraNavegacion
import com.example.museodelosmuseos.ui.navigation.Rutas
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Blanco
import com.example.museodelosmuseos.ui.theme.Crema
import com.example.museodelosmuseos.ui.theme.Dorado
import com.example.museodelosmuseos.util.GeoUtils
import com.example.museodelosmuseos.viewmodel.DetalleMuseoViewModel

private val ROJO_FAVORITO = Color(0xFFD32F2F)
private val COLOR_ENLACE = Color(0xFF2E7D8C)

// Detalle de un museo (se abre desde el mapa o desde un QR "MUSEO_MU01")
@Composable
fun DetalleMuseoScreen(
    viewModel: DetalleMuseoViewModel,
    onVolver: () -> Unit,
    onNavegar: (String) -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    Scaffold(
        containerColor = Crema,
        bottomBar = { BarraNavegacion(rutaActual = Rutas.MAPA, onNavegar = onNavegar) }
    ) { padding ->
        val museo = estado.museo
        when {
            estado.cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AzulOscuro)
            }

            museo == null -> Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                BotonVolver(onVolver)
                Spacer(Modifier.height(16.dp))
                Text("Museo no encontrado", color = AzulOscuro, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // ---- Imagen referencial (banner con el ícono de la categoría) ----
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .background(
                            Brush.verticalGradient(listOf(colorCategoria(museo.categoria), AzulOscuro))
                        )
                ) {
                    Icon(
                        iconoCategoria(museo.categoria),
                        contentDescription = null,
                        tint = Blanco.copy(alpha = 0.9f),
                        modifier = Modifier.size(96.dp).align(Alignment.Center)
                    )
                    Box(Modifier.padding(12.dp)) { BotonVolver(onVolver) }
                }

                Column(Modifier.padding(20.dp)) {
                    // ---- Nombre + corazón de favorito ----
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            museo.nombre,
                            color = AzulOscuro,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.alternarFavorito(museo.id) },
                            enabled = estado.haySesion
                        ) {
                            Icon(
                                imageVector = if (estado.esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = ROJO_FAVORITO,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    // ---- Ubicación ----
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Place, null, tint = Dorado, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${museo.ciudad}, ${museo.pais}", color = AzulOscuro.copy(alpha = 0.8f), fontSize = 15.sp)
                    }

                    // ---- Clasificación ----
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Etiqueta(museo.categoria, colorCategoria(museo.categoria))
                        Etiqueta(museo.proyecto, AzulOscuro)
                        if (museo.urlVisitaVirtual.isNotBlank()) Etiqueta("Virtual", COLOR_ENLACE)
                    }

                    // ---- Calificación ----
                    Spacer(Modifier.height(8.dp))
                    Calificacion(museo)

                    // ---- Descripción ----
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "${museo.descripcion} Recorre sus colecciones desde cualquier lugar: " +
                                "explora salas virtuales, exposiciones y material educativo.",
                        color = AzulOscuro.copy(alpha = 0.85f),
                        fontSize = 15.sp
                    )

                    // ---- Ubicación física (distinta del enlace virtual) ----
                    Spacer(Modifier.height(20.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Blanco),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Ubicación física", color = AzulOscuro, fontWeight = FontWeight.Bold)
                            Text(
                                "Coordenadas: %.4f, %.4f".format(museo.latitud, museo.longitud),
                                color = AzulOscuro.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                            Text(
                                "A ${GeoUtils.formatearDistancia(estado.distanciaKm)} del centro de Santiago",
                                color = AzulOscuro.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                        }
                    }

                    // ---- Acción principal: recorrido virtual ----
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { runCatching { uriHandler.openUri(museo.urlVisitaVirtual) } },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro)
                    ) {
                        Text("Ver proyecto virtual", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null)
                    }

                    // ---- Acción secundaria: favoritos (Room) ----
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { viewModel.alternarFavorito(museo.id) },
                        enabled = estado.haySesion,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            if (estado.esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            null,
                            tint = ROJO_FAVORITO
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (estado.esFavorito) "Quitar de favoritos" else "Guardar en favoritos",
                            color = AzulOscuro,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (!estado.haySesion) {
                        Text(
                            "Inicia sesión para guardar favoritos",
                            color = AzulOscuro.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun BotonVolver(onVolver: () -> Unit) {
    IconButton(onClick = onVolver, modifier = Modifier.background(Blanco, CircleShape)) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = AzulOscuro)
    }
}

// "★★★★★ 4,8 (120 reseñas)"
@Composable
private fun Calificacion(museo: Museo) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val llenas = Math.round(museo.valoracion).toInt()
        repeat(5) { i ->
            Icon(
                if (i < llenas) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = null,
                tint = Dorado,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            "%.1f (%d reseñas)".format(museo.valoracion, museo.resenas),
            color = AzulOscuro.copy(alpha = 0.75f),
            fontSize = 14.sp
        )
    }
}