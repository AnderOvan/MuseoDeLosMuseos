package com.example.museodelosmuseos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.repository.FichaQr
import com.example.museodelosmuseos.ui.components.BarraNavegacion
import com.example.museodelosmuseos.ui.navigation.Rutas
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Blanco
import com.example.museodelosmuseos.ui.theme.Crema
import com.example.museodelosmuseos.ui.theme.Dorado
import com.example.museodelosmuseos.viewmodel.FavoritosViewModel
import com.example.museodelosmuseos.viewmodel.MuseoFavorito
import com.example.museodelosmuseos.viewmodel.PestanaFavoritos
import com.example.museodelosmuseos.viewmodel.RecursoGuardado
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ROJO_CORAZON = Color(0xFFD32F2F)
private val COLOR_VIRTUAL_FAV = Color(0xFF2E7D8C)

@Composable
fun FavoritosScreen(
    viewModel: FavoritosViewModel,
    onVerDetalleMuseo: (String) -> Unit,  // id del museo
    onAbrirRecurso: (FichaQr) -> Unit,    // publicación o producto
    onNavegarAMapa: () -> Unit,
    onIrAlEscaner: () -> Unit,
    onNavegar: (String) -> Unit,          // barra inferior
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Crema,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { BarraNavegacion(rutaActual = Rutas.FAVORITOS, onNavegar = onNavegar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // ---- Encabezado + contador ----
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = AzulOscuro)
                }
                Column(Modifier.weight(1f)) {
                    Text("Mis Favoritos", color = AzulOscuro, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${estado.museos.size} museos guardados",
                        color = AzulOscuro.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            }

            // ---- Pestañas: Museos / Recursos ----
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Blanco, RoundedCornerShape(50))
                    .padding(4.dp)
            ) {
                Pestana(
                    texto = "Museos (${estado.museos.size})",
                    seleccionada = estado.pestana == PestanaFavoritos.MUSEOS,
                    modifier = Modifier.weight(1f)
                ) { viewModel.cambiarPestana(PestanaFavoritos.MUSEOS) }
                Pestana(
                    texto = "Recursos (${estado.recursos.size})",
                    seleccionada = estado.pestana == PestanaFavoritos.RECURSOS,
                    modifier = Modifier.weight(1f)
                ) { viewModel.cambiarPestana(PestanaFavoritos.RECURSOS) }
            }
            Spacer(Modifier.height(12.dp))

            when {
                // ---- Museos favoritos ----
                estado.pestana == PestanaFavoritos.MUSEOS && estado.museos.isEmpty() -> ListaVacia(
                    icono = Icons.Filled.FavoriteBorder,
                    mensaje = "Aún no tienes museos favoritos",
                    detalle = "Toca el corazón en el detalle de un museo para guardarlo aquí.",
                    textoBoton = "Explorar en el mapa",
                    onBoton = onNavegarAMapa
                )

                estado.pestana == PestanaFavoritos.MUSEOS -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(estado.museos, key = { it.museo.id }) { favorito ->
                        TarjetaMuseoFavorito(
                            favorito = favorito,
                            onAbrir = { onVerDetalleMuseo(favorito.museo.id) },
                            onVisitaVirtual = { runCatching { uriHandler.openUri(favorito.museo.urlVisitaVirtual) } },
                            onQuitar = {
                                val id = favorito.museo.id
                                viewModel.eliminarFavorito(id)
                                scope.launch {
                                    val resultado = snackbar.showSnackbar(
                                        message = "Se quitó \"${favorito.museo.nombre}\"",
                                        actionLabel = "Deshacer",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (resultado == SnackbarResult.ActionPerformed) viewModel.restaurarFavorito(id)
                                }
                            }
                        )
                    }
                }

                // ---- Recursos guardados (escaneados con QR) ----
                estado.recursos.isEmpty() -> ListaVacia(
                    icono = Icons.Filled.QrCodeScanner,
                    mensaje = "Aún no tienes recursos guardados",
                    detalle = "Escanea el QR de una publicación o producto y aparecerá aquí.",
                    textoBoton = "Ir al escáner",
                    onBoton = onIrAlEscaner
                )

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(estado.recursos, key = { "${it.ficha.tipo}-${it.ficha.codigo}" }) { recurso ->
                        TarjetaRecurso(recurso, onAbrir = { onAbrirRecurso(recurso.ficha) })
                    }
                }
            }
        }
    }
}

@Composable
private fun Pestana(texto: String, seleccionada: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(if (seleccionada) AzulOscuro else Color.Transparent, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            color = if (seleccionada) Blanco else AzulOscuro,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun TarjetaMuseoFavorito(
    favorito: MuseoFavorito,
    onAbrir: () -> Unit,
    onVisitaVirtual: () -> Unit,
    onQuitar: () -> Unit
) {
    val museo = favorito.museo
    Card(
        onClick = onAbrir,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Miniatura con el ícono y color de la categoría
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(colorCategoria(museo.categoria), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(iconoCategoria(museo.categoria), null, tint = Blanco, modifier = Modifier.size(34.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(museo.nombre, color = AzulOscuro, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 2)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Place, null, tint = Dorado, modifier = Modifier.size(14.dp))
                        Text("${museo.ciudad}, ${museo.pais}", color = AzulOscuro.copy(alpha = 0.7f), fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(2.dp))
                    Estrellas(museo.valoracion, museo.resenas)
                }
                // Corazón: al tocarlo se quita de Room
                IconButton(onClick = onQuitar) {
                    Icon(Icons.Filled.Favorite, "Quitar de favoritos", tint = ROJO_CORAZON, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onVisitaVirtual, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Language, null, tint = COLOR_VIRTUAL_FAV, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Visita virtual", color = COLOR_VIRTUAL_FAV, fontSize = 13.sp)
                }
                Button(
                    onClick = onAbrir,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro)
                ) {
                    Text("Ver detalle", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

private val formatoFechaRecurso = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

@Composable
private fun TarjetaRecurso(recurso: RecursoGuardado, onAbrir: () -> Unit) {
    val ficha = recurso.ficha
    Card(
        onClick = onAbrir,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(colorTipoQr(ficha.tipo), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconoTipoQr(ficha.tipo), null, tint = Blanco, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(ficha.titulo, color = AzulOscuro, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2)
                Spacer(Modifier.height(4.dp))
                Etiqueta(ficha.etiqueta, colorTipoQr(ficha.tipo))
                Spacer(Modifier.height(2.dp))
                Text(
                    "Escaneado el ${formatoFechaRecurso.format(Date(recurso.fecha))}",
                    color = AzulOscuro.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, "Abrir", tint = AzulOscuro)
        }
    }
}

@Composable
private fun ListaVacia(
    icono: ImageVector,
    mensaje: String,
    detalle: String,
    textoBoton: String,
    onBoton: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icono, null, tint = Dorado, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text(mensaje, color = AzulOscuro, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(detalle, color = AzulOscuro.copy(alpha = 0.7f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onBoton,
            colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro)
        ) {
            Text(textoBoton, fontWeight = FontWeight.Bold)
        }
    }
}