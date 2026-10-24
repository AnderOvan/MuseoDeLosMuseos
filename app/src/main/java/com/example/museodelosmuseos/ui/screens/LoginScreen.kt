package com.example.museodelosmuseos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.museodelosmuseos.modelo.Rol
import com.example.museodelosmuseos.ui.theme.*

@Composable
fun LoginScreen(
    onIniciarSesion: (Rol) -> Unit,
    onRegistro: () -> Unit,
    onVolver: () -> Unit
) {
    var rolSeleccionado by remember { mutableStateOf(Rol.CLIENTE) }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var verContrasena by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

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

        Text(
            text = "Iniciar sesión",
            color = AzulOscuro,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Selecciona el tipo de cuenta",
            color = AzulOscuro.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(16.dp))

        Rol.entries.forEach { rol ->
            RolOpcion(
                rol = rol,
                seleccionado = rol == rolSeleccionado,
                onClick = { rolSeleccionado = rol }
            )
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it; error = null },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Filled.Email, null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it; error = null },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Filled.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { verContrasena = !verContrasena }) {
                    Icon(
                        imageVector = if (verContrasena) Icons.Filled.VisibilityOff
                        else Icons.Filled.Visibility,
                        contentDescription = "Mostrar u ocultar contraseña"
                    )
                }
            },
            visualTransformation = if (verContrasena) VisualTransformation.None
            else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (!correo.contains("@")) {
                    error = "Ingresa un correo válido"
                } else if (contrasena.length < 6) {
                    error = "La contraseña debe tener al menos 6 caracteres"
                } else {
                    onIniciarSesion(rolSeleccionado)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Dorado,
                contentColor = AzulOscuro
            )
        ) {
            Text("Iniciar sesión")
        }

        if (rolSeleccionado == Rol.CLIENTE) {
            TextButton(
                onClick = onRegistro,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("¿No tienes cuenta? Regístrate", color = AzulOscuro)
            }
        }
    }
}

@Composable
private fun RolOpcion(rol: Rol, seleccionado: Boolean, onClick: () -> Unit) {
    val icono = when (rol) {
        Rol.ADMINISTRADOR -> Icons.Filled.AdminPanelSettings
        Rol.INVESTIGADOR -> Icons.Filled.Search
        Rol.CLIENTE -> Icons.Filled.Person
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) AzulOscuro else Blanco
        ),
        border = if (seleccionado) BorderStroke(2.dp, Dorado) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, null, tint = Dorado, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = rol.titulo,
                    fontWeight = FontWeight.Bold,
                    color = if (seleccionado) Blanco else AzulOscuro
                )
                Text(
                    text = rol.descripcion,
                    fontSize = 13.sp,
                    color = if (seleccionado) Crema else AzulOscuro.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    LoginScreen(onIniciarSesion = {}, onRegistro = {}, onVolver = {})
}