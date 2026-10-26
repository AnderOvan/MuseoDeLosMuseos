package com.example.museodelosmuseos.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.example.museodelosmuseos.viewmodel.EstadoLectura
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// QR de ejemplo para probar en el emulador sin cámara real
private val QR_DE_PRUEBA = listOf("PROD_L001", "MUSEO_M001", "PUB_001", "HOLA_MUNDO")

private val formatoFecha = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

@Composable
fun EscanerScreen(
    onResultado: (estado: String, contenido: String) -> Unit, // abre ResultadoQrScreen
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

    // Cuando hay un resultado, se abre la pantalla de Resultado del QR
    LaunchedEffect(estado.resultado) {
        val resultado = estado.resultado ?: return@LaunchedEffect
        vm.resultadoAtendido()
        onResultado(resultado.estado.name, resultado.contenido)
    }

    var indicePrueba by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema)
    ) {
        // ---- Barra superior ----
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = AzulOscuro)
            }
            Text("Escanear código QR", color = AzulOscuro, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        // ---- Cámara ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
        ) {
            if (tienePermiso) {
                VistaCamara(onQrDetectado = { vm.procesarLectura(it) })
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("La app necesita la cámara para leer códigos QR.", color = Blanco)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { lanzadorPermiso.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = Dorado, contentColor = AzulOscuro)
                    ) { Text("Dar permiso") }
                }
            }
        }

        // ---- Simulación para el emulador ----
        OutlinedButton(
            onClick = {
                vm.procesarLectura(QR_DE_PRUEBA[indicePrueba], ignorarPausa = true)
                indicePrueba = (indicePrueba + 1) % QR_DE_PRUEBA.size
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Simular escaneo: ${QR_DE_PRUEBA[indicePrueba]}", color = AzulOscuro)
        }

        // ---- Historial del usuario activo (desde Room) ----
        Text(
            "Historial de lecturas",
            color = AzulOscuro,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (estado.historial.isEmpty()) {
                item { Text("Aún no has escaneado códigos.", color = AzulOscuro.copy(alpha = 0.6f)) }
            }
            items(estado.historial, key = { it.id }) { lectura ->
                Text(
                    text = "${lectura.descripcion} · ${formatoFecha.format(Date(lectura.fechaLectura))}  ›",
                    color = AzulOscuro,
                    fontSize = 13.sp,
                    // Tocar una lectura del historial también abre su resultado
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onResultado(EstadoLectura.GUARDADO.name, lectura.contenidoQr) }
                )
            }
        }
    }
}

// Analiza cada cuadro de la cámara con ML Kit y avisa cuando encuentra un QR
@ExperimentalGetImage
private class QrAnalyzer(private val onQrDetectado: (String) -> Unit) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
    )

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
private fun VistaCamara(onQrDetectado: (String) -> Unit) {
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

                proveedor.unbindAll()
                proveedor.bindToLifecycle(
                    lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analisis
                )
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )

    // Libera la cámara al salir de la pantalla
    DisposableEffect(Unit) {
        onDispose { ProcessCameraProvider.getInstance(context).get().unbindAll() }
    }
}