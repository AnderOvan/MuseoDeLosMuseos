package com.example.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.model.Obra
import com.example.museodelosmuseos.repository.ObrasRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Todo lo que la pantalla necesita dibujar, en un solo objeto
data class ObrasUiState(
    val obras: List<Obra> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val busqueda: String = ""
)

class ObrasViewModel(private val repo: ObrasRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ObrasUiState())
    val uiState: StateFlow<ObrasUiState> = _uiState.asStateFlow()

    // Al abrir la pantalla se cargan las obras
    init {
        cargar()
    }

    fun cambiarBusqueda(texto: String) {
        _uiState.update { it.copy(busqueda = texto) }
    }

    // Botón buscar y botón "Reintentar"
    fun cargar() {
        viewModelScope.launch {
            // 1. Estado de carga: muestra el indicador y borra el error anterior
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // 2. Llamada a la API (Retrofit)
                val obras = repo.obtenerObras(_uiState.value.busqueda)
                // 3a. Éxito: guarda los datos
                _uiState.update { it.copy(obras = obras, isLoading = false) }
            } catch (e: CancellationException) {
                throw e // la corrutina se canceló (se cerró la pantalla): no es un error
            } catch (e: Exception) {
                // 3b. Error: guarda un mensaje para mostrarlo en pantalla
                _uiState.update { it.copy(isLoading = false, error = mensajeDeError(e)) }
            }
        }
    }

    companion object {
        fun factory(repo: ObrasRepository = ObrasRepository()) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ObrasViewModel(repo) as T
        }
    }
}

// Traduce la excepción a un mensaje entendible para el usuario
fun mensajeDeError(e: Throwable): String = when (e) {
    is IOException -> "Sin conexión (${e.javaClass.simpleName}: ${e.message})"
    is HttpException -> "El servidor respondió con un error (${e.code()})."
    else -> "Ocurrió un error inesperado al cargar los datos."
}