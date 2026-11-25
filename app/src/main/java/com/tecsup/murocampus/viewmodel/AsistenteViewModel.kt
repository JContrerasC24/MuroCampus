package com.tecsup.murocampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.murocampus.data.ai.AsistenteAcademicoRepository
import com.tecsup.murocampus.model.Post
import com.tecsup.murocampus.model.MensajeAsistente
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConversacionAsistente(
    val mensajes: List<MensajeAsistente> = emptyList(),
    val tituloPublicacion: String = "",
    val cantidadComentarios: Int = 0,
    val cargando: Boolean = false,
    val error: String? = null,
)

class AsistenteViewModel(private val repository: AsistenteAcademicoRepository) : ViewModel() {
    private val _conversacion = MutableStateFlow(ConversacionAsistente())
    val conversacion: StateFlow<ConversacionAsistente> = _conversacion.asStateFlow()

    private val _revisionBorrador = MutableStateFlow<UiState<String>?>(null)
    val revisionBorrador: StateFlow<UiState<String>?> = _revisionBorrador.asStateFlow()

    private var publicacion: com.tecsup.murocampus.model.Post? = null
    private var comentarios: List<com.tecsup.murocampus.model.Comentario> = emptyList()
    private var chatJob: Job? = null
    private var revisionJob: Job? = null
    private var versionChat = 0
    private var versionRevision = 0

    fun vincular(detalle: DetalleUi) {
        if (publicacion?.id == detalle.post.id) return
        chatJob?.cancel()
        versionChat++
        publicacion = detalle.post
        comentarios = detalle.comentarios
        _conversacion.value = ConversacionAsistente(
            tituloPublicacion = "#${detalle.post.id} · ${detalle.post.title}",
            cantidadComentarios = detalle.comentarios.size,
            mensajes = listOf(MensajeAsistente(
                "¡Hola! Soy el Asistente Académico de MuroCampus. Puedo ayudarte a entender esta publicación y sus comentarios.",
                esEstudiante = false,
            )),
        )
    }

    fun enviar(pregunta: String) {
        val post = publicacion ?: return
        val texto = pregunta.trim().take(1200)
        if (texto.isBlank() || _conversacion.value.cargando) return
        val version = ++versionChat
        chatJob?.cancel()
        val historia = _conversacion.value.mensajes
        _conversacion.value = _conversacion.value.copy(
            mensajes = historia + MensajeAsistente(texto, esEstudiante = true),
            cargando = true,
            error = null,
        )
        chatJob = viewModelScope.launch {
            try {
                repository.responder(texto, post, comentarios, historia)
                    .onSuccess { respuesta ->
                        if (version == versionChat) {
                            _conversacion.value = _conversacion.value.copy(
                                mensajes = _conversacion.value.mensajes + MensajeAsistente(respuesta, esEstudiante = false),
                                cargando = false,
                                error = null,
                            )
                        }
                    }
                    .onFailure { error ->
                        if (version == versionChat) {
                            _conversacion.value = _conversacion.value.copy(
                                cargando = false,
                                error = error.mensajeAmigable(),
                            )
                        }
                    }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            }
        }
    }

    fun revisarBorrador(titulo: String, contenido: String, publicaciones: List<Post>) {
        if (titulo.isBlank() || contenido.isBlank()) return
        val version = ++versionRevision
        revisionJob?.cancel()
        _revisionBorrador.value = UiState.Cargando
        revisionJob = viewModelScope.launch {
            repository.revisarBorrador(titulo.trim(), contenido.trim(), publicaciones)
                .onSuccess { if (version == versionRevision) _revisionBorrador.value = UiState.Exito(it) }
                .onFailure { if (version == versionRevision) _revisionBorrador.value = UiState.Error(it.mensajeAmigable()) }
        }
    }

    fun limpiarRevision() {
        versionRevision++
        revisionJob?.cancel()
        revisionJob = null
        _revisionBorrador.value = null
    }

    override fun onCleared() {
        versionChat++
        versionRevision++
        chatJob?.cancel()
        revisionJob?.cancel()
        super.onCleared()
    }

    private fun Throwable.mensajeAmigable(): String = when {
        message?.contains("Default FirebaseApp", ignoreCase = true) == true ||
            message?.contains("FirebaseApp", ignoreCase = true) == true ->
            "El asistente necesita conectarse a un proyecto Firebase AI Logic. Revisa la configuración de la app."
        message?.contains("App Check", ignoreCase = true) == true ->
            "Firebase necesita validar esta instalación. Registra el token de depuración de App Check y vuelve a intentarlo."
        else -> message?.takeIf(String::isNotBlank) ?: "No se pudo contactar al asistente. Comprueba tu conexión y reintenta."
    }
}
