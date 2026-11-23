package com.example.museodelosmuseos.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.model.Rol
import com.example.museodelosmuseos.repository.local.UsuarioEntity
import com.example.museodelosmuseos.ui.components.BarraNavegacion
import com.example.museodelosmuseos.ui.navigation.Rutas
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Blanco
import com.example.museodelosmuseos.ui.theme.Crema
import com.example.museodelosmuseos.ui.theme.Dorado
import com.example.museodelosmuseos.util.validarDespacho
import com.example.museodelosmuseos.util.validarNombre
import com.example.museodelosmuseos.viewmodel.PerfilViewModel
import kotlinx.coroutines.launch

// Mismos colores de avatar que usa la pantalla de Registro
private val COLORES_AVATAR_PERFIL = listOf(
    Color(0xFF1B4965),
    Color(0xFF8A5A44),
    Color(0xFF5F7A61),
    Color(0xFF7B5EA7)
)

// Acceso directo del perfil: texto, ícono y ruta
private data class AccesoPerfil(val titulo: String, val icono: ImageVector, val ruta: String)

// Cada rol ve solo las opciones que le corresponden
private fun accesosDe(rol: Rol): List<AccesoPerfil> = when (rol) {
    Rol.CLIENTE -> listOf(
        AccesoPerfil("Mis solicitudes", Icons.Filled.Receipt, Rutas.HISTORIAL),
        AccesoPerfil("Mis favoritos", Icons.Filled.Favorite, Rutas.FAVORITOS),
        AccesoPerfil("Mi carrito", Icons.Filled.ShoppingCart, Rutas.CARRITO)
    )
    Rol.INVESTIGADOR -> listOf(
        AccesoPerfil("Escáner QR", Icons.Filled.QrCodeScanner, Rutas.ESCANER_QR),
        AccesoPerfil("Mapa de museos", Icons.Filled.Map, Rutas.MAPA)
    )
    Rol.ADMINISTRADOR -> listOf(
        AccesoPerfil("Gestionar solicitudes", Icons.Filled.Receipt, Rutas.HISTORIAL),
        AccesoPerfil("Catálogo", Icons.Filled.Inventory2, Rutas.CATALOGO)
    )
}

@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel,
    onNavegar: (String) -> Unit,        // barra inferior y accesos
    onCambiarUsuario: () -> Unit,
    onCerrarSesion: () -> Unit,
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val usuario = estado.usuario
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Los permisos se vuelven a revisar cada vez que se vuelve desde los Ajustes del teléfono
    var revision by remember { mutableIntStateOf(0) }
    LifecycleResumeEffect(Unit) {
        revision++
        onPauseOrDispose { }
    }
    val permisoCamara = remember(revision) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    val permisoUbicacion = remember(revision) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    val abrirAjustes = {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
            )
        }
        Unit
    }

    Scaffold(
        containerColor = Crema,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { BarraNavegacion(rutaActual = Rutas.PERFIL, onNavegar = onNavegar) }
    ) { padding ->
        if (usuario == null) {
            // Pasa un instante al cerrar sesión, o si se abre sin haber iniciado
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.Person, null, tint = Dorado, modifier = Modifier.size(80.dp))
                Spacer(Modifier.height(12.dp))
                Text("No hay una sesión iniciada", color = AzulOscuro, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onCambiarUsuario,
                    colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro)
                ) { Text("Iniciar sesión", fontWeight = FontWeight.Bold) }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ---- Cabecera ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulOscuro, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .padding(start = 8.dp, end = 20.dp, top = 8.dp, bottom = 24.dp)
            ) {
                Column {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Dorado)
                    }
                    Row(Modifier.padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AvatarCircular(usuario.avatar, 64)
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(usuario.nombre, color = Blanco, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(usuario.correo, color = Crema, fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            Etiqueta(usuario.rol.titulo, Dorado)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ---- Datos personales (formulario con validación) ----
                SeccionPerfil("Datos personales") {
                    FormularioPerfil(
                        usuario = usuario,
                        onGuardar = { nombre, despacho, avatar ->
                            viewModel.guardarPerfil(nombre, despacho, avatar)
                            scope.launch { snackbar.showSnackbar("Perfil actualizado") }
                        }
                    )
                }

                // ---- Accesos según el rol ----
                SeccionPerfil("Accesos") {
                    accesosDe(usuario.rol).forEach { acceso ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavegar(acceso.ruta) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(acceso.icono, null, tint = Dorado)
                            Spacer(Modifier.width(12.dp))
                            Text(acceso.titulo, color = AzulOscuro, modifier = Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = AzulOscuro.copy(alpha = 0.5f))
                        }
                    }
                }

                // ---- Preferencias (se guardan en Room) ----
                SeccionPerfil("Preferencias") {
                    FilaSwitch(
                        icono = Icons.Filled.Notifications,
                        titulo = "Notificaciones",
                        detalle = "Recibir avisos de cursos, museos y pedidos",
                        activo = estado.preferencias.notificaciones,
                        onCambio = viewModel::cambiarNotificaciones
                    )
                    FilaSwitch(
                        icono = Icons.Filled.TextFields,
                        titulo = "Texto grande",
                        detalle = "Aumenta el tamaño de las letras en toda la app",
                        activo = estado.preferencias.textoGrande,
                        onCambio = viewModel::cambiarTextoGrande
                    )
                }

                // ---- Permisos ----
                SeccionPerfil("Permisos") {
                    // El Administrador no usa el escáner, así que no ve el permiso de cámara
                    if (usuario.rol != Rol.ADMINISTRADOR) {
                        FilaPermiso(Icons.Filled.CameraAlt, "Cámara", permisoCamara, abrirAjustes)
                    }
                    FilaPermiso(Icons.Filled.LocationOn, "Ubicación", permisoUbicacion, abrirAjustes)
                }

                // ---- Sesión ----
                OutlinedButton(
                    onClick = {
                        viewModel.cerrarSesion()
                        onCambiarUsuario()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.SwitchAccount, null, tint = AzulOscuro)
                    Spacer(Modifier.width(8.dp))
                    Text("Cambiar de usuario", color = AzulOscuro)
                }
                Button(
                    onClick = {
                        viewModel.cerrarSesion()
                        onCerrarSesion()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC62828),
                        contentColor = Blanco
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar sesión", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun FormularioPerfil(
    usuario: UsuarioEntity,
    onGuardar: (nombre: String, despacho: String, avatar: Int) -> Unit
) {
    // remember(usuario.id): se llenan una vez con los datos guardados y luego los edita el usuario
    var nombre by remember(usuario.id) { mutableStateOf(usuario.nombre) }
    var despacho by remember(usuario.id) { mutableStateOf(usuario.despacho) }
    var avatar by remember(usuario.id) { mutableIntStateOf(usuario.avatar) }
    var intentoGuardar by remember(usuario.id) { mutableStateOf(false) }

    // La dirección de despacho solo la usa el Cliente
    val pideDespacho = usuario.rol == Rol.CLIENTE

    val errorNombre = validarNombre(nombre)
    val errorDespacho = if (pideDespacho) validarDespacho(despacho) else null
    val hayCambios = nombre.trim() != usuario.nombre ||
            (pideDespacho && despacho.trim() != usuario.despacho) ||
            avatar != usuario.avatar

    OutlinedTextField(
        value = nombre,
        onValueChange = { nombre = it },
        label = { Text("Nombre completo") },
        leadingIcon = { Icon(Icons.Filled.Person, null) },
        singleLine = true,
        isError = intentoGuardar && errorNombre != null,
        supportingText = { if (intentoGuardar) errorNombre?.let { Text(it) } },
        modifier = Modifier.fillMaxWidth()
    )

    // Correo y RUT identifican la cuenta: solo se muestran
    OutlinedTextField(
        value = usuario.correo,
        onValueChange = {},
        label = { Text("Correo electrónico") },
        leadingIcon = { Icon(Icons.Filled.Email, null) },
        singleLine = true,
        enabled = false,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = usuario.rut,
        onValueChange = {},
        label = { Text("RUT") },
        leadingIcon = { Icon(Icons.Filled.Badge, null) },
        singleLine = true,
        enabled = false,
        modifier = Modifier.fillMaxWidth()
    )

    if (pideDespacho) {
        OutlinedTextField(
            value = despacho,
            onValueChange = { despacho = it },
            label = { Text("Dirección de despacho") },
            leadingIcon = { Icon(Icons.Filled.LocalShipping, null) },
            singleLine = true,
            isError = intentoGuardar && errorDespacho != null,
            supportingText = { if (intentoGuardar) errorDespacho?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(Modifier.height(8.dp))
    Text("Avatar", color = AzulOscuro, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        COLORES_AVATAR_PERFIL.indices.forEach { indice ->
            AvatarCircular(
                indice = indice,
                tamano = 52,
                seleccionado = indice == avatar,
                onClick = { avatar = indice }
            )
        }
    }

    Spacer(Modifier.height(12.dp))
    Button(
        onClick = {
            intentoGuardar = true
            if (errorNombre == null && errorDespacho == null) {
                onGuardar(nombre, despacho, avatar)
            }
        },
        enabled = hayCambios,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro)
    ) {
        Icon(Icons.Filled.Save, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("Guardar cambios", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AvatarCircular(
    indice: Int,
    tamano: Int,
    seleccionado: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val color = COLORES_AVATAR_PERFIL.getOrElse(indice) { COLORES_AVATAR_PERFIL.first() }
    Box(
        modifier = Modifier
            .size(tamano.dp)
            .clip(CircleShape)
            .background(color)
            .then(if (seleccionado) Modifier.border(3.dp, Dorado, CircleShape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Person, "Avatar ${indice + 1}", tint = Blanco, modifier = Modifier.size((tamano / 2).dp))
    }
}

@Composable
private fun SeccionPerfil(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, color = AzulOscuro, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            contenido()
        }
    }
}

@Composable
private fun FilaSwitch(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    activo: Boolean,
    onCambio: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, null, tint = Dorado)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(titulo, color = AzulOscuro, fontWeight = FontWeight.SemiBold)
            Text(detalle, color = AzulOscuro.copy(alpha = 0.6f), fontSize = 12.sp)
        }
        Switch(
            checked = activo,
            onCheckedChange = onCambio,
            colors = SwitchDefaults.colors(checkedTrackColor = AzulOscuro, checkedThumbColor = Dorado)
        )
    }
}

@Composable
private fun FilaPermiso(icono: ImageVector, nombre: String, concedido: Boolean, onAjustes: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, null, tint = Dorado)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(nombre, color = AzulOscuro, fontWeight = FontWeight.SemiBold)
            Text(
                if (concedido) "Concedido" else "No concedido",
                color = if (concedido) Color(0xFF2E7D32) else Color(0xFFC62828),
                fontSize = 12.sp
            )
        }
        TextButton(onClick = onAjustes) { Text("Ajustes", color = AzulOscuro) }
    }
}