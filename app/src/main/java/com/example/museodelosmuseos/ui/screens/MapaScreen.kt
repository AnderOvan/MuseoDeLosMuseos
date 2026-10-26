package com.example.museodelosmuseos.ui.screens

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.museodelosmuseos.repository.FakeMuseoRepository
import com.example.museodelosmuseos.ui.components.BarraNavegacion
import com.example.museodelosmuseos.ui.navigation.Rutas
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Blanco
import com.example.museodelosmuseos.ui.theme.Dorado
import com.example.museodelosmuseos.viewmodel.MapaViewModel
import com.example.museodelosmuseos.viewmodel.MuseoConDistancia
import org.osmdroid.config.Configuration
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex
import com.example.museodelosmuseos.util.GeoUtils
// Color de cada categoría (marcadores, recuadro de la tarjeta y etiquetas)
fun colorCategoria(categoria: String): Color = when (categoria) {
    "Arte" -> Color(0xFFB5523B)
    "Historia" -> Color(0xFF1B4965)
    "Ciencia" -> Color(0xFF2E7D32)
    else -> Dorado
}

fun iconoCategoria(categoria: String): ImageVector = when (categoria) {
    "Arte" -> Icons.Filled.Palette
    "Ciencia" -> Icons.Filled.Science
    else -> Icons.Filled.AccountBalance
}

private val COLOR_VIRTUAL = Color(0xFF2E7D8C)

// Servidor de mapas de Esri (gratis, sin clave). Su URL usa el orden zoom/y/x,
// por eso se arma a mano en lugar de usar el formato estándar zoom/x/y.
private val MAPA_ESRI = object : OnlineTileSourceBase(
    "EsriWorldStreetMap", 0, 19, 256, "",
    arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/"),
    "Esri, HERE, Garmin, © OpenStreetMap contributors"
) {
    override fun getTileURLString(pMapTileIndex: Long): String =
        baseUrl + MapTileIndex.getZoom(pMapTileIndex) + "/" +
                MapTileIndex.getY(pMapTileIndex) + "/" +
                MapTileIndex.getX(pMapTileIndex)
}
@Composable
fun MapaScreen(
    onVerDetalle: (String) -> Unit,   // recibe el id del museo
    onNavegar: (String) -> Unit,      // barra inferior
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uriHandler = LocalUriHandler.current

    val factory = remember { MapaViewModel.factory(FakeMuseoRepository()) }
    val vm: MapaViewModel = viewModel(factory = factory)
    val estado by vm.uiState.collectAsStateWithLifecycle()

    val mapView = remember {
        val config = Configuration.getInstance()
        config.load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        // OpenStreetMap exige que la app se identifique con un nombre propio
        config.userAgentValue = "MuseoDeLosMuseos/1.0 (proyecto academico Duoc UC)"
        // Carpeta de caché propia: descarta los cuadros "403" guardados en pruebas anteriores
        val carpeta = File(context.cacheDir, "osm_v2")
        config.osmdroidBasePath = carpeta
        config.osmdroidTileCache = File(carpeta, "tiles")

        MapView(context).apply {
            setTileSource(MAPA_ESRI)
            setMultiTouchControls(true) // zoom con dos dedos
            minZoomLevel = 2.0
            maxZoomLevel = 18.0
            controller.setZoom(3.0)
            controller.setCenter(GeoPoint(0.0, -45.0)) // vista de Iberoamérica
        }
    }

    // El mapa sigue el ciclo de vida de la pantalla
    DisposableEffect(lifecycleOwner) {
        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observador)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observador)
            mapView.onDetach()
        }
    }

    // Al tocar un museo, la cámara viaja hacia él
    LaunchedEffect(estado.seleccionadoId) {
        estado.seleccionado?.let {
            mapView.controller.animateTo(GeoPoint(it.museo.latitud, it.museo.longitud), 8.0, 800L)
        }
    }

    // Al cambiar un filtro, la cámara encuadra los museos que quedan visibles
    LaunchedEffect(estado.paisSeleccionado, estado.proyectoSeleccionado, estado.todos.size) {
        val puntos = estado.visibles.map { GeoPoint(it.museo.latitud, it.museo.longitud) }
        when {
            puntos.isEmpty() -> Unit
            estado.sinFiltros -> mapView.controller.animateTo(GeoPoint(0.0, -45.0), 3.0, 800L)
            puntos.size == 1 -> mapView.controller.animateTo(puntos.first(), 8.0, 800L)
            else -> mapView.zoomToBoundingBox(BoundingBox.fromGeoPoints(puntos), true, 150)
        }
    }

    Scaffold(
        bottomBar = { BarraNavegacion(rutaActual = Rutas.MAPA, onNavegar = onNavegar) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Mapa de osmdroid; los marcadores se redibujan cuando cambian filtros o selección
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize(),
                update = { mv ->
                    mv.overlays.clear()
                    mv.overlays.add(Marker(mv).apply {
                        position = GeoPoint(GeoUtils.ORIGEN_LAT, GeoUtils.ORIGEN_LON)
                        title = "Origen: centro de Santiago"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    })
                    estado.visibles.forEach { item ->
                        mv.overlays.add(Marker(mv).apply {
                            position = GeoPoint(item.museo.latitud, item.museo.longitud)
                            title = item.museo.nombre
                            icon = iconoMarcador(
                                mv.context,
                                colorCategoria(item.museo.categoria).toArgb(),
                                seleccionado = item.museo.id == estado.seleccionadoId
                            )
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            setOnMarkerClickListener { _, _ ->
                                vm.seleccionarMuseo(item.museo.id)
                                true
                            }
                        })
                    }
                    mv.invalidate()
                }
            )

            // Fila superior: volver + filtros Todos / País / Proyecto
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onVolver,
                    modifier = Modifier.background(Blanco, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = AzulOscuro)
                }
                val colores = FilterChipDefaults.filterChipColors(
                    containerColor = Blanco,
                    selectedContainerColor = AzulOscuro,
                    selectedLabelColor = Blanco,
                    selectedTrailingIconColor = Blanco
                )
                FilterChip(
                    selected = estado.sinFiltros,
                    onClick = vm::mostrarTodos,
                    label = { Text("Todos") },
                    colors = colores
                )
                FiltroMenu(
                    titulo = "País",
                    textoTodos = "Todos los países",
                    seleccionado = estado.paisSeleccionado,
                    opciones = estado.paises,
                    onElegir = vm::filtrarPorPais,
                    colores = colores
                )
                FiltroMenu(
                    titulo = "Proyecto",
                    textoTodos = "Todos los proyectos",
                    seleccionado = estado.proyectoSeleccionado,
                    opciones = estado.proyectos,
                    onElegir = vm::filtrarPorProyecto,
                    colores = colores
                )
            }

            if (estado.cargando) {
                CircularProgressIndicator(
                    color = AzulOscuro,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            estado.error?.let { mensaje ->
                Card(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco)
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(mensaje, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = vm::cargarMuseos) { Text("Reintentar", color = AzulOscuro) }
                    }
                }
            }

            // Créditos exigidos por la licencia de OpenStreetMap
            Text(
                text = "Mapa: Esri, HERE, Garmin, © OpenStreetMap contributors",
                color = AzulOscuro,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Blanco.copy(alpha = 0.8f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )

            // Tarjeta del museo seleccionado
            estado.seleccionado?.let { item ->
                TarjetaMuseo(
                    item = item,
                    onVerDetalle = { onVerDetalle(item.museo.id) },
                    onVisitaVirtual = { runCatching { uriHandler.openUri(item.museo.urlVisitaVirtual) } },
                    onCerrar = { vm.seleccionarMuseo(null) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

// Chip con menú desplegable (País o Proyecto)
@Composable
private fun FiltroMenu(
    titulo: String,
    textoTodos: String,
    seleccionado: String?,
    opciones: List<String>,
    onElegir: (String?) -> Unit,
    colores: SelectableChipColors
) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = seleccionado != null,
            onClick = { abierto = true },
            label = { Text(seleccionado ?: titulo) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
            colors = colores
        )
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            DropdownMenuItem(
                text = { Text(textoTodos) },
                onClick = { onElegir(null); abierto = false }
            )
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = { onElegir(opcion); abierto = false }
                )
            }
        }
    }
}

@Composable
private fun TarjetaMuseo(
    item: MuseoConDistancia,
    onVerDetalle: () -> Unit,
    onVisitaVirtual: () -> Unit,
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val museo = item.museo
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // "Foto" del museo: recuadro con el ícono de su categoría
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(colorCategoria(museo.categoria), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(iconoCategoria(museo.categoria), null, tint = Blanco, modifier = Modifier.size(38.dp))
                }
                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(museo.nombre, color = AzulOscuro, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Place, null, tint = Dorado, modifier = Modifier.size(16.dp))
                        Text("${museo.ciudad}, ${museo.pais}", color = AzulOscuro.copy(alpha = 0.75f), fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Etiqueta(museo.categoria, colorCategoria(museo.categoria))
                        if (museo.urlVisitaVirtual.isNotBlank()) Etiqueta("Virtual", COLOR_VIRTUAL)
                    }
                    Spacer(Modifier.height(4.dp))
                    Estrellas(museo.valoracion, museo.resenas)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = onCerrar) {
                        Icon(Icons.Filled.Close, "Cerrar", tint = AzulOscuro)
                    }
                    IconButton(
                        onClick = onVerDetalle,
                        modifier = Modifier.background(Dorado, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Ver detalle", tint = AzulOscuro)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Ubicación física y visita virtual van separadas, como pide la pauta
            Text(
                "Ubicación física: a ${GeoUtils.formatearDistancia(item.distanciaKm)} del centro de Santiago",
                color = AzulOscuro,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onVisitaVirtual, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Language, null, tint = COLOR_VIRTUAL)
                Spacer(Modifier.width(6.dp))
                Text("Visita virtual", color = COLOR_VIRTUAL)
            }
        }
    }
}

@Composable
fun Etiqueta(texto: String, color: Color) {
    Text(
        text = texto,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
fun Estrellas(valoracion: Double, resenas: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val llenas = Math.round(valoracion).toInt()
        repeat(5) { i ->
            Icon(
                imageVector = if (i < llenas) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = null,
                tint = Dorado,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(Modifier.width(4.dp))
        Text("%.1f (%d)".format(valoracion, resenas), color = AzulOscuro.copy(alpha = 0.75f), fontSize = 12.sp)
    }
}

// Marcador circular del color de la categoría (más grande si está seleccionado)
private fun iconoMarcador(context: Context, color: Int, seleccionado: Boolean): GradientDrawable {
    val densidad = context.resources.displayMetrics.density
    val tam = ((if (seleccionado) 34 else 26) * densidad).toInt()
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
        setStroke((3 * densidad).toInt(), android.graphics.Color.WHITE)
        setSize(tam, tam)
    }
}