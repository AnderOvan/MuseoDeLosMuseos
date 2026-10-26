package com.example.museodelosmuseos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.museodelosmuseos.model.Usuario
import com.example.museodelosmuseos.ui.theme.*
import com.example.museodelosmuseos.util.validarContrasena
import com.example.museodelosmuseos.util.validarCorreo
import com.example.museodelosmuseos.util.validarNombre
import com.example.museodelosmuseos.util.formatearRut
import com.example.museodelosmuseos.util.limpiarRut
import com.example.museodelosmuseos.util.validarRut
import com.example.museodelosmuseos.util.validarDespacho
import com.example.museodelosmuseos.util.RutVisualTransformation

private val coloresAvatar = listOf(
    Color(0xFF1B4965),
    Color(0xFF8A5A44),
    Color(0xFF5F7A61),
    Color(0xFF7B5EA7)
)

@Composable
fun RegistroScreen(
    onRegistrar: (Usuario) -> Unit,
    onIrALogin: () -> Unit,
    onVolver: () -> Unit,
    errorExterno: String? = null
) {
    var nombre by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var verContrasena by remember { mutableStateOf(false) }
    var avatar by remember { mutableIntStateOf(0) }
    var intentoEnvio by remember { mutableStateOf(false) }
    var despacho by remember { mutableStateOf("") }

    val errorNombre = validarNombre(nombre)
    val errorRut = validarRut(rut)
    val errorCorreo = validarCorreo(correo)
    val errorContrasena = validarContrasena(contrasena)
    val errorDespacho = validarDespacho(despacho)
    val formularioValido =
        errorNombre == null && errorRut == null && errorCorreo == null && errorContrasena == null && errorDespacho == null


    val mostrarNombre = if (intentoEnvio) errorNombre else null
    val mostrarRut = if (intentoEnvio) errorRut else null
    val mostrarCorreo = if (intentoEnvio) errorCorreo else null
    val mostrarDespacho = if (intentoEnvio) errorDespacho else null
    val mostrarContrasena = if (intentoEnvio) errorContrasena else null

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
            text = "Crear tu cuenta",
            color = AzulOscuro,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Únete a la comunidad de Museo de los Museos",
            color = AzulOscuro.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(24.dp))
        errorExterno?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            leadingIcon = { Icon(Icons.Filled.Person, null) },
            singleLine = true,
            isError = mostrarNombre != null,
            supportingText = { mostrarNombre?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = rut,
            onValueChange = { rut = limpiarRut(it).take(9) },
            label = { Text("RUT") },
            placeholder = { Text("12.345.678-5") },
            leadingIcon = { Icon(Icons.Filled.Badge, null) },
            singleLine = true,
            visualTransformation = RutVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                capitalization = KeyboardCapitalization.Characters
            ),
            isError = mostrarRut != null,
            supportingText = { mostrarRut?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Filled.Email, null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = mostrarCorreo != null,
            supportingText = { mostrarCorreo?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = despacho,
            onValueChange = { despacho = it },
            label = { Text("Dirección de despacho") },
            placeholder = { Text("Av. Ejemplo 1234, Comuna") },
            leadingIcon = { Icon(Icons.Filled.LocalShipping, null) },
            singleLine = true,
            isError = mostrarDespacho != null,
            supportingText = { mostrarDespacho?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
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
            isError = mostrarContrasena != null,
            supportingText = { mostrarContrasena?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Selecciona un avatar",
            color = AzulOscuro,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            coloresAvatar.forEachIndexed { indice, color ->
                val seleccionado = indice == avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (seleccionado) Modifier.border(3.dp, Dorado, CircleShape)
                            else Modifier
                        )
                        .clickable { avatar = indice },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, "Avatar ${indice + 1}", tint = Blanco)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                intentoEnvio = true
                if (formularioValido) {
                    onRegistrar(
                        Usuario(
                            nombre = nombre.trim(),
                            rut = formatearRut(rut),
                            correo = correo.trim(),
                            contrasena = contrasena,
                            despacho = despacho,
                            avatar = avatar
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Dorado,
                contentColor = AzulOscuro
            )
        ) {
            Text("Registrarme")
        }

        TextButton(
            onClick = onIrALogin,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión", color = AzulOscuro)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistroPreview() {
    RegistroScreen(onRegistrar = {}, onIrALogin = {}, onVolver = {})
}