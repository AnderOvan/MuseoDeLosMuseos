package com.example.museodelosmuseos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Crema
import com.example.museodelosmuseos.ui.theme.Dorado

// Sección que todavía no está integrada al proyecto
@Composable
fun PantallaPendiente(
    titulo: String,
    onVolver: () -> Unit,
    onCerrarSesion: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(titulo, color = AzulOscuro, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Esta sección aún no está integrada", color = AzulOscuro.copy(alpha = 0.7f))
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onVolver,
            colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro)
        ) {
            Text("Volver")
        }
        if (onCerrarSesion != null) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onCerrarSesion) {
                Text("Cerrar sesión", color = AzulOscuro)
            }
        }
    }
}