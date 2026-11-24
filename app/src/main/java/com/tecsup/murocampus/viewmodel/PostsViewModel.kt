package com.tecsup.murocampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.murocampus.data.ErrorRepositorio
import com.tecsup.murocampus.data.PostRepository
import com.tecsup.murocampus.model.Post
import com.tecsup.murocampus.model.Comentario
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostsViewModel(private val repository: PostRepository) : ViewModel() {
    private val _lista = MutableStateFlow<UiState<List<Post>>>(UiState.Cargando)
    val lista: StateFlow<UiState<List<Post>>> = _lista.asStateFlow()

    private val _detalle = MutableStateFlow<UiState<DetalleUi>>(UiState.Cargando)
    val detalle: StateFlow<UiState<DetalleUi>> = _detalle.asStateFlow()
    private var detalleJob: Job? = null
    private var solicitudDetalle = 0
    private var ultimoDetalleId: Int? = null

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

    fun abrir(id: Int) {
        ultimoDetalleId = id
        val solicitud = ++solicitudDetalle
        detalleJob?.cancel()
        detalleJob = viewModelScope.launch {
            _detalle.value = UiState.Cargando
            coroutineScope {
                val post = async { repository.detalle(id) }
                val comentarios = async { repository.comentarios(id) }
                val resultadoPost = post.await()
                val resultadoComentarios = comentarios.await()
                if (solicitud != solicitudDetalle) return@coroutineScope
                val postObtenido = resultadoPost.getOrNull()
                val comentariosObtenidos = resultadoComentarios.getOrNull()
                _detalle.value = if (postObtenido != null && comentariosObtenidos != null) {
                    UiState.Exito(DetalleUi(postObtenido, comentariosObtenidos))
                } else {
                    val error = resultadoPost.exceptionOrNull() ?: resultadoComentarios.exceptionOrNull()
                    UiState.Error(error?.comoMensaje() ?: "Respuesta inesperada del servidor.")
                }
            }
        }
    }

    fun reintentarDetalle() {
        ultimoDetalleId?.let(::abrir)
    }

    private fun Throwable.comoMensaje(): String = when (this) {
        is ErrorRepositorio.SinConexion -> message ?: "No hay conexión."
        is ErrorRepositorio.Http -> message ?: "Error HTTP $codigo."
        is ErrorRepositorio.RespuestaInesperada -> message ?: "Respuesta inesperada del servidor."
        else -> "Ocurrió un error inesperado."
    }
}

data class DetalleUi(val post: Post, val comentarios: List<Comentario>)
