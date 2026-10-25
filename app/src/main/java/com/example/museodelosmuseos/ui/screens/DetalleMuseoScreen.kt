package com.example.museodelosmuseos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.museodelosmuseos.repository.FakeMuseoRepository
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Blanco
import com.example.museodelosmuseos.ui.theme.Crema
import com.example.museodelosmuseos.ui.theme.Dorado
import com.example.museodelosmuseos.util.GeoUtils

// Detalle de un museo (desde el mapa o desde un QR "MUSEO_MU01")
@Composable
fun DetalleMuseoScreen(codigo: String, onVolver: () -> Unit) {
    val museo = FakeMuseoRepository.buscarPorId(codigo)
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        IconButton(onClick = onVolver) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = AzulOscuro)
        }

        if (museo == null) {
            Text("Museo no encontrado", color = AzulOscuro, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Código: $codigo", color = AzulOscuro.copy(alpha = 0.7f))
            return@Column
        }

        // Recuadro de la categoría (en lugar de foto)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(colorCategoria(museo.categoria), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(iconoCategoria(museo.categoria), null, tint = Blanco, modifier = Modifier.size(64.dp))
        }

        Spacer(Modifier.height(16.dp))
        Text(museo.nombre, color = AzulOscuro, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("${museo.ciudad}, ${museo.pais}", color = AzulOscuro.copy(alpha = 0.75f), fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Etiqueta(museo.categoria, colorCategoria(museo.categoria))
            Etiqueta(museo.proyecto, AzulOscuro)
        }
        Spacer(Modifier.height(6.dp))
        Estrellas(museo.valoracion, museo.resenas)

        Spacer(Modifier.height(16.dp))
        Text(museo.descripcion, color = AzulOscuro.copy(alpha = 0.85f))

        // ---- Ubicación física ----
        Spacer(Modifier.height(24.dp))
        Text("Ubicación física", color = AzulOscuro, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Place, null, tint = Dorado)
            Spacer(Modifier.width(6.dp))
            Column {
                Text("${museo.ciudad}, ${museo.pais}", color = AzulOscuro)
                Text(
                    "Coordenadas: %.4f, %.4f".format(museo.latitud, museo.longitud),
                    color = AzulOscuro.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
                Text(
                    "A ${GeoUtils.formatearDistancia(GeoUtils.distanciaDesdeOrigenKm(museo.latitud, museo.longitud))} del centro de Santiago",
                    color = AzulOscuro.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }

        // ---- Visita virtual (enlace, distinto de la ubicación) ----
        Spacer(Modifier.height(24.dp))
        Text("Visita virtual", color = AzulOscuro, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(museo.urlVisitaVirtual, color = Color(0xFF2E7D8C), fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { runCatching { uriHandler.openUri(museo.urlVisitaVirtual) } },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro)
        ) {
            Icon(Icons.Filled.Language, null)
            Spacer(Modifier.width(6.dp))
            Text("Abrir visita virtual")
        }
    }
}