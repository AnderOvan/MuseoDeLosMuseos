package com.example.museodelosmuseos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.museodelosmuseos.repository.CatalogoQr
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Blanco
import com.example.museodelosmuseos.ui.theme.Crema
import com.example.museodelosmuseos.ui.theme.Dorado
import com.example.museodelosmuseos.util.QrLeido
import com.example.museodelosmuseos.util.TipoContenidoQr

// Ficha de un producto o publicación (se abre desde el Resultado del QR).
// El detalle de museos está en DetalleMuseoScreen.kt
@Composable
fun DetallePublicacionScreen(codigo: String, tipo: TipoContenidoQr, onVolver: () -> Unit) {
    val ficha = CatalogoQr.buscar(QrLeido(contenido = "", tipo = tipo, codigo = codigo))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema)
            .padding(24.dp)
    ) {
        IconButton(onClick = onVolver) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = AzulOscuro)
        }
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(colorTipoQr(tipo), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(iconoTipoQr(tipo), null, tint = Blanco, modifier = Modifier.size(64.dp))
        }
        Spacer(Modifier.height(16.dp))

        if (ficha == null) {
            Text("Contenido no encontrado", color = AzulOscuro, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Código: $codigo", color = AzulOscuro.copy(alpha = 0.7f))
            return@Column
        }

        Text(ficha.titulo, color = AzulOscuro, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Etiqueta(ficha.etiqueta, colorTipoQr(tipo))
        Spacer(Modifier.height(10.dp))
        Text(ficha.fuente, color = AzulOscuro, fontWeight = FontWeight.SemiBold)
        Text(ficha.descripcion, color = AzulOscuro.copy(alpha = 0.85f))

        ficha.precioUsd?.let { precio ->
            Spacer(Modifier.height(16.dp))
            Text("USD %.2f".format(precio), color = Dorado, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Código: ${tipo.prefijo}$codigo",
            color = AzulOscuro.copy(alpha = 0.5f),
            fontSize = 12.sp
        )
    }
}