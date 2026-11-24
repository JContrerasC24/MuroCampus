package com.tecsup.murocampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.murocampus.model.Post
import com.tecsup.murocampus.viewmodel.PostsViewModel
import com.tecsup.murocampus.viewmodel.UiState

@Composable
fun PantallaLista(viewModel: PostsViewModel, onPostClick: (Post) -> Unit = {}) {
    val estado by viewModel.lista.collectAsStateWithLifecycle()
    when (val actual = estado) {
        UiState.Cargando -> Cargando("Cargando publicaciones...")
        is UiState.Error -> ErrorConReintento(actual.mensaje, viewModel::cargar)
        is UiState.Exito -> {
            if (actual.datos.isEmpty()) {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) { Text("No hay publicaciones.") }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(actual.datos, key = { it.id }) { post ->
                        TarjetaPost(
                            usuario = post.userId,
                            titulo = post.title,
                            contenido = post.body,
                            onClick = { onPostClick(post) },
                        )
                    }
                }
            }
        }
    }
}
