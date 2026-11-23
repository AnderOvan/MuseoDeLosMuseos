package com.example.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.repository.LocalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// Entrega a la pantalla principal el nombre del usuario con sesión iniciada (desde Room)
class HomeViewModel(repo: LocalRepository) : ViewModel() {

    // Solo el primer nombre: "Cristóbal Pizarro" -> "Cristóbal". Sin sesión: "Visitante"
    val nombreUsuario: StateFlow<String> = repo.usuarioActivo
        .map { usuario ->
            usuario?.nombre?.trim()?.substringBefore(" ")?.ifBlank { null } ?: "Visitante"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Visitante")

    companion object {
        fun factory(repo: LocalRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(repo) as T
        }
    }
}