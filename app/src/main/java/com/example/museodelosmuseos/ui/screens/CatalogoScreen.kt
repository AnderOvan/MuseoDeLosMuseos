package com.example.museodelosmuseos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.repository.local.ProductoCacheEntity
import com.example.museodelosmuseos.ui.components.BarraNavegacion
import com.example.museodelosmuseos.ui.navigation.Rutas
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Blanco
import com.example.museodelosmuseos.ui.theme.Crema
import com.example.museodelosmuseos.ui.theme.Dorado
import com.example.museodelosmuseos.viewmodel.CatalogoViewModel
import com.example.museodelosmuseos.viewmodel.CategoriaCatalogo

// Color e ícono de la "imagen" de cada categoría
private fun colorProducto(categoria: String): Color = when (categoria) {
    "Curso" -> Color(0xFF1B4965)
    "Libro" -> Color(0xFF8A5A44)
    else -> Color(0xFF5F7A61)
}

private fun iconoProducto(categoria: String): ImageVector = when (categoria) {
    "Curso" -> Icons.Filled.School
    "Libro" -> Icons.AutoMirrored.Filled.MenuBook
    else -> Icons.Filled.Inventory2
}

// 49.0 -> "US$ 49" | 22.5 -> "US$ 22,50"
fun formatearPrecio(precio: Double): String =
    if (precio % 1.0 == 0.0) "US$ %.0f".format(precio) else "US$ %.2f".format(precio)

// Texto de disponibilidad según el stock
private fun disponibilidad(producto: ProductoCacheEntity): Pair<String, Color> = when {
    producto.categoria == "Curso" -> "Acceso digital" to Color(0xFF2E7D32)
    producto.stock == 0 -> "Agotado" to Color(0xFFC62828)
    producto.stock <= 5 -> "Últimas ${producto.stock} unidades" to Color(0xFFE65100)
    else -> "Disponible" to Color(0xFF2E7D32)
}

@Composable
fun CatalogoScreen(
    viewModel: CatalogoViewModel,
    onNavegarADetalle: (String) -> Unit, // recibe el id del producto
    onNavegar: (String) -> Unit          // barra inferior
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Crema,
        bottomBar = { BarraNavegacion(rutaActual = Rutas.CATALOGO, onNavegar = onNavegar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // ---- Encabezado + contador ----
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Catálogo & Cursos",
                    color = AzulOscuro,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${estado.productos.size} de ${estado.total} artículos",
                    color = AzulOscuro.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }

            // ---- Búsqueda ----
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryCambia,
                placeholder = { Text("Buscar productos, cursos...") },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = AzulOscuro) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryCambia("") }) {
                            Icon(Icons.Filled.Close, "Borrar búsqueda")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Blanco,
                    unfocusedContainerColor = Blanco,
                    focusedBorderColor = AzulOscuro
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // ---- Chips de categoría ----
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoriaCatalogo.entries.forEach { categoria ->
                    FilterChip(
                        selected = estado.categoria == categoria,
                        onClick = { viewModel.onCategoria(categoria) },
                        label = { Text(categoria.titulo) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Blanco,
                            selectedContainerColor = AzulOscuro,
                            selectedLabelColor = Blanco
                        )
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            when {
                estado.sincronizando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AzulOscuro)
                }

                estado.productos.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron artículos", color = AzulOscuro.copy(alpha = 0.6f))
                }

                // ---- Cuadrícula de 2 columnas ----
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(estado.productos, key = { it.id }) { producto ->
                        TarjetaProducto(producto, onClick = { onNavegarADetalle(producto.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaProducto(producto: ProductoCacheEntity, onClick: () -> Unit) {
    val (textoDisponible, colorDisponible) = disponibilidad(producto)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // "Imagen" del producto: recuadro de color con el ícono de su categoría
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(colorProducto(producto.categoria)),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconoProducto(producto.categoria), null, tint = Blanco, modifier = Modifier.size(44.dp))
                // Etiqueta de categoría en dorado
                Text(
                    producto.categoria,
                    color = AzulOscuro,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Dorado, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Column(Modifier.padding(12.dp)) {
                Text(
                    producto.titulo,
                    color = AzulOscuro,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(40.dp)
                )
                Text(textoDisponible, color = colorDisponible, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        formatearPrecio(producto.precio),
                        color = AzulOscuro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    // Botón para ir al detalle
                    IconButton(
                        onClick = onClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Dorado, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Ver detalle", tint = AzulOscuro, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}