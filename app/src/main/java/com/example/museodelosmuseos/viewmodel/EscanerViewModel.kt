package com.example.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.repository.CatalogoQr
import com.example.museodelosmuseos.repository.LocalRepository
import com.example.museodelosmuseos.repository.ResultadoQr
import com.example.museodelosmuseos.repository.local.LecturaQrEntity
import com.example.museodelosmuseos.util.ValidadorQr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Cómo terminó una lectura (se envía a la pantalla de Resultado)
enum class EstadoLectura { GUARDADO, DUPLICADO, INVALIDO, SIN_SESION }

data class ResultadoLectura(val estado: EstadoLectura, val contenido: String)

data class EscanerUiState(
    val resultado: ResultadoLectura? = null,
    val historial: List<LecturaQrEntity> = emptyList()
)

class EscanerViewModel(private val repo: LocalRepository) : ViewModel() {

    private val _resultado = MutableStateFlow<ResultadoLectura?>(null)

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

    fun procesarLectura(contenido: String, ignorarPausa: Boolean = false) {
        val ahora = System.currentTimeMillis()
        if (contenido.isBlank() || procesando || _resultado.value != null) return
        if (!ignorarPausa && ahora < pausaHastaMs) return
        procesando = true

        // 1) Clasifica el texto  2) valida que exista en el catálogo
        val qr = ValidadorQr.analizar(contenido)
        val ficha = CatalogoQr.buscar(qr)

        viewModelScope.launch {
            val estado = if (ficha == null) {
                EstadoLectura.INVALIDO // no se guarda en el historial
            } else {
                // 3) Room guarda la lectura del usuario activo, salvo que sea un duplicado de 24 h
                when (repo.registrarLecturaQr(
                    contenidoQr = qr.contenido,
                    tipo = qr.tipo.name,
                    descripcion = "${qr.tipo.titulo}: ${ficha.titulo}"
                )) {
                    ResultadoQr.GUARDADA -> EstadoLectura.GUARDADO
                    ResultadoQr.DUPLICADA -> EstadoLectura.DUPLICADO
                    ResultadoQr.SIN_SESION -> EstadoLectura.SIN_SESION
                }
            }
            _resultado.value = ResultadoLectura(estado, qr.contenido)
            procesando = false
        }
    }

    // La pantalla ya abrió el resultado: se limpia y se pausa un momento la cámara
    fun resultadoAtendido() {
        _resultado.value = null
        pausaHastaMs = System.currentTimeMillis() + 3_000
    }

    // Al volver al escáner, espera un poco para no leer otra vez el QR que sigue frente a la cámara
    fun reanudar() {
        pausaHastaMs = maxOf(pausaHastaMs, System.currentTimeMillis() + 2_500)
    }

    companion object {
        fun factory(repo: LocalRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                EscanerViewModel(repo) as T
        }
    }
}