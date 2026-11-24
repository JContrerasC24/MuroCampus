package com.tecsup.murocampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.murocampus.data.ErrorRepositorio
import com.tecsup.murocampus.data.PostRepository
import com.tecsup.murocampus.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostsViewModel(private val repository: PostRepository) : ViewModel() {
    private val _lista = MutableStateFlow<UiState<List<Post>>>(UiState.Cargando)
    val lista: StateFlow<UiState<List<Post>>> = _lista.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _lista.value = UiState.Cargando
            repository.listar()
                .onSuccess { _lista.value = UiState.Exito(it) }
                .onFailure { _lista.value = UiState.Error(it.comoMensaje()) }
        }
    }

    private fun Throwable.comoMensaje(): String = when (this) {
        is ErrorRepositorio.SinConexion -> message ?: "No hay conexión."
        is ErrorRepositorio.Http -> message ?: "Error HTTP $codigo."
        is ErrorRepositorio.RespuestaInesperada -> message ?: "Respuesta inesperada del servidor."
        else -> "Ocurrió un error inesperado."
    }
}
