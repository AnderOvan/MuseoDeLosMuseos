package com.example.museodelosmuseos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Blanco
import com.example.museodelosmuseos.ui.theme.Crema
import com.example.museodelosmuseos.ui.theme.Dorado
import com.example.museodelosmuseos.util.CampoProducto
import com.example.museodelosmuseos.util.ValidacionProducto
import com.example.museodelosmuseos.viewmodel.FormularioProductoViewModel

// Formulario del Administrador: alta, modificación, desactivación y eliminación de productos.
// Cada acción llama al microservicio de productos y, si sale bien, vuelve al catálogo.
@Composable
fun FormularioProductoScreen(viewModel: FormularioProductoViewModel, onVolver: () -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmarEliminar by remember { mutableStateOf(false) }

    // Cuando el backend confirma la operación, se vuelve al catálogo (ya sincronizado)
    LaunchedEffect(estado.terminado) {
        if (estado.terminado) onVolver()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema)
            .verticalScroll(rememberScrollState())
    ) {
        // ---------- Encabezado ----------
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulOscuro, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Blanco)
            }
            Column {
                Text(
                    if (estado.esNuevo) "Nuevo producto" else "Editar producto",
                    color = Blanco, fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
                Text("Administrador · microservicio de productos", color = Dorado, fontSize = 12.sp)
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // El código no se puede cambiar al editar (es la clave en la base de datos)
            Campo("Código (ej: L004)", estado.id, viewModel::onId, estado.errores[CampoProducto.ID],
                habilitado = estado.esNuevo)
            Campo("Título", estado.titulo, viewModel::onTitulo, estado.errores[CampoProducto.TITULO])
            Campo("Descripción", estado.descripcion, viewModel::onDescripcion,
                estado.errores[CampoProducto.DESCRIPCION], unaLinea = false)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Campo("Precio (US$)", estado.precio, viewModel::onPrecio, estado.errores[CampoProducto.PRECIO],
                    teclado = KeyboardType.Decimal, modifier = Modifier.weight(1f))
                Campo("Stock", estado.stock, viewModel::onStock, estado.errores[CampoProducto.STOCK],
                    teclado = KeyboardType.Number, modifier = Modifier.weight(1f))
            }

            Text("Categoría", color = AzulOscuro, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValidacionProducto.CATEGORIAS.forEach { categoria ->
                    FilterChip(
                        selected = estado.categoria == categoria,
                        onClick = { viewModel.onCategoria(categoria) },
                        label = { Text(categoria) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Blanco,
                            selectedContainerColor = AzulOscuro,
                            selectedLabelColor = Blanco
                        )
                    )
                }
            }
            Text("Los cursos digitales usan stock 999 (sin límite).",
                color = AzulOscuro.copy(alpha = 0.6f), fontSize = 12.sp)

            Spacer(Modifier.height(6.dp))

            // ---------- Guardar: POST (nuevo) o PUT (edición) ----------
            Button(
                onClick = viewModel::guardar,
                enabled = !estado.guardando,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (estado.guardando) {
                    CircularProgressIndicator(color = AzulOscuro, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Icon(Icons.Filled.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (estado.esNuevo) "Crear producto" else "Guardar cambios", fontWeight = FontWeight.Bold)
                }
            }

            // ---------- Solo al editar: desactivar y eliminar ----------
            if (!estado.esNuevo) {
                OutlinedButton(
                    onClick = viewModel::desactivar,
                    enabled = !estado.guardando,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.VisibilityOff, null, tint = AzulOscuro)
                    Spacer(Modifier.width(8.dp))
                    Text("Desactivar (ocultar del catálogo)", color = AzulOscuro)
                }
                TextButton(
                    onClick = { confirmarEliminar = true },
                    enabled = !estado.guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text("Eliminar producto", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    // Confirmación antes de borrar
    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text("Eliminar ${estado.id}", fontWeight = FontWeight.Bold) },
            text = { Text("Se borrará \"${estado.titulo}\" del servidor. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminar = false
                    viewModel.eliminar()
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar", color = AzulOscuro) }
            }
        )
    }

    // Error del backend (sin conexión, 404, 500...)
    estado.error?.let { mensaje ->
        AlertDialog(
            onDismissRequest = viewModel::limpiarError,
            title = { Text("No se pudo completar la operación", fontWeight = FontWeight.Bold) },
            text = { Text(mensaje) },
            confirmButton = {
                TextButton(onClick = viewModel::limpiarError) { Text("Entendido", color = AzulOscuro) }
            }
        )
    }
}

// Campo de texto con su mensaje de error debajo
@Composable
private fun Campo(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    unaLinea: Boolean = true,
    teclado: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        enabled = habilitado,
        singleLine = unaLinea,
        minLines = if (unaLinea) 1 else 3,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Blanco,
            unfocusedContainerColor = Blanco,
            disabledContainerColor = Blanco.copy(alpha = 0.6f),
            focusedBorderColor = AzulOscuro
        ),
        modifier = modifier.fillMaxWidth()
    )
}