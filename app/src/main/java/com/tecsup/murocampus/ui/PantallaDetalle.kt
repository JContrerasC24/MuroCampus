package com.tecsup.murocampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.murocampus.viewmodel.PostsViewModel
import com.tecsup.murocampus.viewmodel.UiState
import com.tecsup.murocampus.viewmodel.DetalleUi

@Composable
fun PantallaDetalle(viewModel: PostsViewModel, onAsistente: (DetalleUi) -> Unit = {}) {
    val estado by viewModel.detalle.collectAsStateWithLifecycle()
    when (val actual = estado) {
        UiState.Cargando -> Cargando("Cargando publicación...")
        is UiState.Error -> ErrorConReintento(actual.mensaje, viewModel::reintentarDetalle)
        is UiState.Exito -> Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(actual.datos.post.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Usuario ${actual.datos.post.userId}", color = MaterialTheme.colorScheme.primary)
            Text(actual.datos.post.body, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = { onAsistente(actual.datos) }, modifier = Modifier.fillMaxWidth()) {
                Text("Consultar al Asistente Académico")
            }
            Text("Comentarios (${actual.datos.comentarios.size})", style = MaterialTheme.typography.titleLarge)
            actual.datos.comentarios.forEach { comentario ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(comentario.name, fontWeight = FontWeight.Bold)
                    Text(comentario.email, color = Color(0xFF1565C0))
                    Text(comentario.body)
                }
            }
        }
    }
}
