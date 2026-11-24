package com.tecsup.murocampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FilterChip
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
    val filtro by viewModel.usuarioFiltro.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        FiltrosUsuarios(seleccionado = filtro, onSeleccion = viewModel::filtrar)
        when (val actual = estado) {
            UiState.Cargando -> Cargando("Cargando publicaciones...", Modifier.weight(1f))
            is UiState.Error -> ErrorConReintento(actual.mensaje, viewModel::cargar, Modifier.weight(1f))
            is UiState.Exito -> {
                if (actual.datos.isEmpty()) {
                    Column(
                        Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) { Text("No hay publicaciones.") }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
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
}

@Composable
private fun FiltrosUsuarios(seleccionado: Int?, onSeleccion: (Int?) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = seleccionado == null,
                onClick = { onSeleccion(null) },
                label = { Text("Todos") },
            )
        }
        items(10) { indice ->
            val usuario = indice + 1
            FilterChip(
                selected = seleccionado == usuario,
                onClick = { onSeleccion(usuario) },
                label = { Text("Usuario $usuario") },
            )
        }
    }
}
