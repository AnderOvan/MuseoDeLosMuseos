package com.example.museodelosmuseos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.museodelosmuseos.model.Obra
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Blanco
import com.example.museodelosmuseos.ui.theme.Crema
import com.example.museodelosmuseos.ui.theme.Dorado
import com.example.museodelosmuseos.viewmodel.ObrasViewModel

// Pantalla de la API externa: obras del Art Institute of Chicago.
// Reacciona a los tres estados del ViewModel: cargando, error y datos.
@Composable
fun ObrasScreen(viewModel: ObrasViewModel, onVolver: () -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema)
    ) {
        // ---------- Encabezado ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulOscuro, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .padding(start = 8.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Blanco)
                }
                Column {
                    Text("Obras destacadas", color = Blanco, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Art Institute of Chicago · API externa", color = Dorado, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(12.dp))

            // Buscador: al presionar la lupa o "buscar" en el teclado se llama a la API
            OutlinedTextField(
                value = estado.busqueda,
                onValueChange = viewModel::cambiarBusqueda,
                placeholder = { Text("Buscar obras (ej: monet, cat)") },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = viewModel::cargar) {
                        Icon(Icons.Filled.Search, "Buscar", tint = AzulOscuro)
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.cargar() }),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Blanco,
                    unfocusedContainerColor = Blanco,
                    focusedBorderColor = Dorado
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            )
        }

        // ---------- Contenido según el estado ----------
        when {
            estado.isLoading -> Cargando()
            estado.error != null -> MensajeError(estado.error!!, onReintentar = viewModel::cargar)
            estado.obras.isEmpty() -> SinResultados()
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(estado.obras, key = { it.id }) { obra -> TarjetaObra(obra) }
            }
        }
    }
}

@Composable
private fun TarjetaObra(obra: Obra) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AzulOscuro.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                if (obra.imagenUrl != null) {
                    // Coil descarga y muestra la imagen desde internet
                    AsyncImage(
                        model = obra.imagenUrl,
                        contentDescription = obra.titulo,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Filled.Palette, null, tint = Dorado, modifier = Modifier.size(36.dp))
                }
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    obra.titulo, color = AzulOscuro, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    obra.artista, color = AzulOscuro.copy(alpha = 0.7f), fontSize = 13.sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(obra.fecha, color = Dorado, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun Cargando() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Dorado)
            Spacer(Modifier.height(12.dp))
            Text("Cargando obras...", color = AzulOscuro)
        }
    }
}

@Composable
private fun MensajeError(mensaje: String, onReintentar: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.CloudOff, null, tint = AzulOscuro.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(12.dp))
            Text(mensaje, color = AzulOscuro, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onReintentar,
                colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro)
            ) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun SinResultados() {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            "No se encontraron obras. Prueba con otra búsqueda.",
            color = AzulOscuro, textAlign = TextAlign.Center
        )
    }
}