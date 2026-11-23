package com.example.museodelosmuseos.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.ui.theme.AzulOscuro
import com.example.museodelosmuseos.ui.theme.Blanco
import com.example.museodelosmuseos.ui.theme.Crema
import com.example.museodelosmuseos.ui.theme.Dorado
import com.example.museodelosmuseos.viewmodel.EscanerViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val FONDO_ESCANER = Color(0xFF0E1726)

// QR de prueba para el emulador (el botón "Simular escaneo" los recorre en orden)
private val QR_DE_PRUEBA = listOf("PUB_001", "PROD_L001", "MUSEO_MU07", "HOLA_MUNDO")

private val formatoFecha = SimpleDateFormat("dd-MM HH:mm", Locale.getDefault())

private val OPCIONES_QR = BarcodeScannerOptions.Builder()
    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
    .build()

@Composable
fun EscanerScreen(
    onResultado: (estado: String, contenido: String) -> Unit, // abre la pantalla de Resultado
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val factory = remember {
        EscanerViewModel.factory(LocalRepository.crear(context.applicationContext))
    }
    val vm: EscanerViewModel = viewModel(factory = factory)
    val estado by vm.uiState.collectAsStateWithLifecycle()

    // ---- Permiso de cámara (se pide en tiempo de ejecución) ----
    var tienePermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> tienePermiso = concedido }

    LaunchedEffect(Unit) {
        vm.reanudar() // al volver desde el Resultado, espera un momento antes de leer de nuevo
        if (!tienePermiso) lanzadorPermiso.launch(Manifest.permission.CAMERA)
    }

    // ---- Cuando hay un resultado, se abre la pantalla de Resultado del QR ----
    LaunchedEffect(estado.resultado) {
        val resultado = estado.resultado ?: return@LaunchedEffect
        vm.resultadoAtendido()
        onResultado(resultado.estado.name, resultado.contenido)
    }

    // ---- Linterna (CameraX) ----
    var camara by remember { mutableStateOf<Camera?>(null) }
    var linterna by remember { mutableStateOf(false) }
    LaunchedEffect(camara, linterna) {
        camara?.cameraControl?.enableTorch(linterna)
    }

    // ---- Leer un QR desde una foto de la galería (ML Kit) ----
    val lectorGaleria = remember { BarcodeScanning.getClient(OPCIONES_QR) }
    val lanzadorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val imagen = InputImage.fromFilePath(context, uri)
            lectorGaleria.process(imagen).addOnSuccessListener { codigos ->
                val texto = codigos.firstOrNull()?.rawValue
                if (texto != null) {
                    vm.procesarLectura(texto, ignorarPausa = true)
                } else {
                    Toast.makeText(context, "No se encontró un código QR en la imagen", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    var indicePrueba by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FONDO_ESCANER)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        // ---- Encabezado ----
        IconButton(onClick = onVolver, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Blanco)
        }
        Text(
            "Escáner QR",
            color = Blanco,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Escanea códigos de museos, publicaciones o productos.",
            color = Crema.copy(alpha = 0.8f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        // ---- Cámara con marco de escaneo ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (tienePermiso) {
                VistaCamara(
                    onQrDetectado = { vm.procesarLectura(it) },
                    onCamaraLista = { camara = it }
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text("La app necesita la cámara para leer códigos QR.", color = Blanco, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { lanzadorPermiso.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro)
                    ) { Text("Dar permiso") }
                }
            }
            MarcoEscaneo(Modifier.fillMaxSize(0.72f))
        }

        Spacer(Modifier.height(24.dp))

        // ---- Linterna y galería ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BotonRedondo(
                icono = if (linterna) Icons.Filled.FlashlightOn else Icons.Filled.FlashlightOff,
                texto = "Linterna",
                activo = linterna,
                habilitado = camara?.cameraInfo?.hasFlashUnit() == true
            ) { linterna = !linterna }

            BotonRedondo(
                icono = Icons.Filled.PhotoLibrary,
                texto = "Desde galería",
                activo = false,
                habilitado = true
            ) {
                lanzadorGaleria.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "Coloca el código dentro del marco y se escaneará automáticamente.",
            color = Crema,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(Blanco.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                .padding(14.dp)
        )

        // ---- Simulación para el emulador (sin cámara real) ----
        TextButton(
            onClick = {
                vm.procesarLectura(QR_DE_PRUEBA[indicePrueba], ignorarPausa = true)
                indicePrueba = (indicePrueba + 1) % QR_DE_PRUEBA.size
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Simular escaneo: ${QR_DE_PRUEBA[indicePrueba]}", color = Dorado)
        }

        // ---- Últimas lecturas guardadas en SQLite ----
        if (estado.historial.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("Últimas lecturas", color = Blanco, fontWeight = FontWeight.SemiBold)
            estado.historial.take(3).forEach { lectura ->
                Text(
                    "• ${lectura.descripcion} · ${formatoFecha.format(Date(lectura.fechaLectura))}",
                    color = Crema.copy(alpha = 0.75f),
                    fontSize = 12.sp
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

// Esquinas del marco de escaneo dibujadas sobre la cámara
@Composable
private fun MarcoEscaneo(modifier: Modifier) {
    Canvas(modifier) {
        val largo = size.minDimension * 0.18f
        val grosor = 6.dp.toPx()
        val w = size.width
        val h = size.height
        fun linea(desde: Offset, hasta: Offset) =
            drawLine(Color.White, desde, hasta, grosor, StrokeCap.Round)

        linea(Offset(0f, 0f), Offset(largo, 0f)); linea(Offset(0f, 0f), Offset(0f, largo))
        linea(Offset(w, 0f), Offset(w - largo, 0f)); linea(Offset(w, 0f), Offset(w, largo))
        linea(Offset(0f, h), Offset(largo, h)); linea(Offset(0f, h), Offset(0f, h - largo))
        linea(Offset(w, h), Offset(w - largo, h)); linea(Offset(w, h), Offset(w, h - largo))
    }
}

@Composable
private fun BotonRedondo(
    icono: ImageVector,
    texto: String,
    activo: Boolean,
    habilitado: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            enabled = habilitado,
            modifier = Modifier
                .size(56.dp)
                .background(if (activo) Dorado else Blanco.copy(alpha = 0.15f), CircleShape)
        ) {
            Icon(
                icono,
                contentDescription = texto,
                tint = if (activo) AzulOscuro else Blanco.copy(alpha = if (habilitado) 1f else 0.4f)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(texto, color = Blanco.copy(alpha = if (habilitado) 1f else 0.5f), fontSize = 12.sp)
    }
}

// Analiza cada cuadro de la cámara con ML Kit y avisa cuando encuentra un QR
@ExperimentalGetImage
private class QrAnalyzer(private val onQrDetectado: (String) -> Unit) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(OPCIONES_QR)

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val imagen = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(imagen)
            .addOnSuccessListener { codigos -> codigos.firstOrNull()?.rawValue?.let(onQrDetectado) }
            .addOnCompleteListener { imageProxy.close() } // Siempre liberar el cuadro
    }
}

// Vista previa en vivo con CameraX dentro de Compose
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
private fun VistaCamara(
    onQrDetectado: (String) -> Unit,
    onCamaraLista: (Camera) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val proveedorFuture = ProcessCameraProvider.getInstance(ctx)

            proveedorFuture.addListener({
                val proveedor = proveedorFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analisis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(ContextCompat.getMainExecutor(ctx), QrAnalyzer(onQrDetectado))
                    }

                try {
                    proveedor.unbindAll()
                    val camara = proveedor.bindToLifecycle(
                        lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analisis
                    )
                    onCamaraLista(camara) // permite encender la linterna
                } catch (e: Exception) {
                    // El dispositivo no tiene cámara trasera: queda el botón de simulación
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )

    // Libera la cámara al salir de la pantalla
    DisposableEffect(Unit) {
        onDispose { ProcessCameraProvider.getInstance(context).get().unbindAll() }
    }
}