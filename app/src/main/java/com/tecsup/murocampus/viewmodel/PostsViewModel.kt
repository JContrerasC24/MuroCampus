package com.tecsup.murocampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.murocampus.data.ErrorRepositorio
import com.tecsup.murocampus.data.PostRepository
import com.tecsup.murocampus.model.Post
import com.tecsup.murocampus.model.Comentario
import com.tecsup.murocampus.model.NuevoPost
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
    private val _usuarioFiltro = MutableStateFlow<Int?>(null)
    val usuarioFiltro: StateFlow<Int?> = _usuarioFiltro.asStateFlow()
    private val _creacion = MutableStateFlow<UiState<Post>?>(null)
    val creacion: StateFlow<UiState<Post>?> = _creacion.asStateFlow()
    private var listaJob: Job? = null
    private var solicitudLista = 0

    private val _detalle = MutableStateFlow<UiState<DetalleUi>>(UiState.Cargando)
    val detalle: StateFlow<UiState<DetalleUi>> = _detalle.asStateFlow()
    private var detalleJob: Job? = null
    private var solicitudDetalle = 0
    private var ultimoDetalleId: Int? = null

    init {
        cargar()
    }

    fun cargar() {
        val filtro = _usuarioFiltro.value
        val solicitud = ++solicitudLista
        listaJob?.cancel()
        listaJob = viewModelScope.launch {
            _lista.value = UiState.Cargando
            val resultado = if (filtro == null) repository.listar() else repository.porUsuario(filtro)
            if (solicitud != solicitudLista) return@launch
            resultado
                .onSuccess { _lista.value = UiState.Exito(it) }
                .onFailure { _lista.value = UiState.Error(it.comoMensaje()) }
        }
    }

    fun filtrar(userId: Int?) {
        _usuarioFiltro.value = userId
        cargar()
    }

    fun publicar(titulo: String, texto: String) {
        if (titulo.isBlank() || texto.isBlank() || _creacion.value == UiState.Cargando) return
        viewModelScope.launch {
            _creacion.value = UiState.Cargando
            repository.crear(NuevoPost(userId = 1, title = titulo.trim(), body = texto.trim()))
                .onSuccess { _creacion.value = UiState.Exito(it) }
                .onFailure { _creacion.value = UiState.Error(it.comoMensaje()) }
        }
    }

    fun limpiarCreacion() {
        _creacion.value = null
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

    fun cerrarDetalle() {
        solicitudDetalle++
        detalleJob?.cancel()
        detalleJob = null
        ultimoDetalleId = null
    }

    private fun Throwable.comoMensaje(): String = when (this) {
        is ErrorRepositorio.SinConexion -> message ?: "No hay conexión."
        is ErrorRepositorio.Http -> message ?: "Error HTTP $codigo."
        is ErrorRepositorio.RespuestaInesperada -> message ?: "Respuesta inesperada del servidor."
        else -> "Ocurrió un error inesperado."
    }
}

data class DetalleUi(val post: Post, val comentarios: List<Comentario>)
