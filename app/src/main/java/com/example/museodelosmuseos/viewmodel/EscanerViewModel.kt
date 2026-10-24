package com.example.museodelosmuseos.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.repository.local.LecturaQrEntity
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.repository.ResultadoQr
import com.example.museodelosmuseos.util.QrLeido
import com.example.museodelosmuseos.util.TipoContenidoQr
import com.example.museodelosmuseos.util.ValidadorQr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Posibles resultados después de leer un QR
sealed interface ResultadoEscaneo {
    data class Guardado(val qr: QrLeido) : ResultadoEscaneo      // Nuevo, guardado en Room
    data class Duplicado(val qr: QrLeido) : ResultadoEscaneo     // Ya escaneado en las últimas 24 h
    data class Invalido(val contenido: String) : ResultadoEscaneo // No es de la app
    data object SinSesion : ResultadoEscaneo                     // No hay usuario activo
}

data class EscanerUiState(
    val resultado: ResultadoEscaneo? = null,
    val historial: List<LecturaQrEntity> = emptyList()
)

class EscanerViewModel(private val repo: LocalRepository) : ViewModel() {

    private val _resultado = MutableStateFlow<ResultadoEscaneo?>(null)

    // Junta el resultado actual con el historial del usuario (se actualiza solo desde Room)
    val uiState: StateFlow<EscanerUiState> = combine(
        _resultado,
        repo.observarHistorialQr()
    ) { resultado, historial ->
        EscanerUiState(resultado, historial)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EscanerUiState())

    // La cámara entrega muchos cuadros por segundo: esto evita procesar el mismo QR una y otra vez
    private var procesando = false
    private var pausaHastaMs = 0L

    fun procesarLectura(contenido: String) {
        val ahora = System.currentTimeMillis()
        // Se ignora si se está guardando otro, si hay una tarjeta abierta o si se acaba de cerrar una
        if (procesando || _resultado.value != null || ahora < pausaHastaMs) return
        procesando = true

        val qr = ValidadorQr.analizar(contenido)

        viewModelScope.launch {
            _resultado.value = if (qr.tipo == TipoContenidoQr.DESCONOCIDO) {
                ResultadoEscaneo.Invalido(qr.contenido)
            } else {
                // Room guarda la lectura del usuario activo, salvo que sea un duplicado de 24 h
                when (repo.registrarLecturaQr(
                    contenidoQr = qr.contenido,
                    tipo = qr.tipo.name,
                    descripcion = "${qr.tipo.titulo}: ${qr.codigo}"
                )) {
                    ResultadoQr.GUARDADA -> ResultadoEscaneo.Guardado(qr)
                    ResultadoQr.DUPLICADA -> ResultadoEscaneo.Duplicado(qr)
                    ResultadoQr.SIN_SESION -> ResultadoEscaneo.SinSesion
                }
            }
            procesando = false
        }
    }

    // Al cerrar la tarjeta se espera 2 s, para que no vuelva a leer el QR que sigue frente a la cámara
    fun cerrarResultado() {
        _resultado.value = null
        pausaHastaMs = System.currentTimeMillis() + 2_000
    }

    companion object {
        fun factory(repo: LocalRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                EscanerViewModel(repo) as T
        }
    }
}