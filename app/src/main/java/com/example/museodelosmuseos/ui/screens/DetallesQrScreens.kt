package com.example.museodelosmuseos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Crema
import com.example.museodelosmuseos.ui.theme.Dorado
import com.example.museodelosmuseos.util.TipoContenidoQr

// Detalle de un producto o publicación escaneado (versión provisoria)
@Composable
fun DetalleProductoScreen(codigo: String, tipo: TipoContenidoQr, onVolver: () -> Unit) {
    DetalleBase(
        icono = if (tipo == TipoContenidoQr.PUBLICACION) Icons.Filled.MenuBook else Icons.Filled.ShoppingBag,
        tipo = tipo.titulo,
        codigo = codigo,
        onVolver = onVolver
    )
}

// Detalle de un museo escaneado (versión provisoria)
@Composable
fun DetalleMuseoScreen(codigo: String, onVolver: () -> Unit) {
    DetalleBase(
        icono = Icons.Filled.AccountBalance,
        tipo = TipoContenidoQr.MUSEO.titulo,
        codigo = codigo,
        onVolver = onVolver
    )
}

@Composable
private fun DetalleBase(icono: ImageVector, tipo: String, codigo: String, onVolver: () -> Unit) {
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
        Icon(icono, null, tint = Dorado, modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(16.dp))
        Text(tipo, color = AzulOscuro, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Código: $codigo", color = AzulOscuro.copy(alpha = 0.75f), fontSize = 18.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "Aquí se mostrará el detalle completo cuando se integre el catálogo.",
            color = AzulOscuro.copy(alpha = 0.6f)
        )
    }
}