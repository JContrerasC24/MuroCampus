package com.tecsup.murocampus.data.ai

import com.tecsup.murocampus.model.Comentario
import com.tecsup.murocampus.model.Post
import com.tecsup.murocampus.model.MensajeAsistente

interface AsistenteAcademicoRepository {
    suspend fun responder(
        pregunta: String,
        publicacion: Post,
        comentarios: List<Comentario>,
        historial: List<MensajeAsistente>,
    ): Result<String>

    suspend fun revisarBorrador(
        titulo: String,
        contenido: String,
        publicaciones: List<Post>,
    ): Result<String>
}
