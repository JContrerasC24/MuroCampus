package com.tecsup.murocampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.murocampus.viewmodel.PostsViewModel
import com.tecsup.murocampus.viewmodel.UiState
import com.tecsup.murocampus.viewmodel.AsistenteViewModel

@Composable
fun PantallaCrear(
    viewModel: PostsViewModel,
    asistenteViewModel: AsistenteViewModel,
    onVolver: () -> Unit,
) {
    var titulo by rememberSaveable { mutableStateOf("") }
    var texto by rememberSaveable { mutableStateOf("") }
    val estado by viewModel.creacion.collectAsStateWithLifecycle()
    val publicacionesState by viewModel.lista.collectAsStateWithLifecycle()
    val publicacionesListas = publicacionesState is UiState.Exito
    val revision by asistenteViewModel.revisionBorrador.collectAsStateWithLifecycle()
    LaunchedEffect(estado) {
        if (estado is UiState.Exito) {
            titulo = ""
            texto = ""
            asistenteViewModel.limpiarRevision()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Nueva publicación")
        Text(
            "Antes de publicar, puedes pedir al asistente que revise la claridad y encuentre temas relacionados. Al hacerlo, se enviarán el borrador y las publicaciones candidatas a Gemini. Evita incluir datos personales.",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it },
            label = { Text("Contenido") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
        )

        Button(
            onClick = {
                val publicaciones = (publicacionesState as? UiState.Exito)?.datos.orEmpty()
                asistenteViewModel.revisarBorrador(titulo, texto, publicaciones)
            },
            enabled = titulo.isNotBlank() && texto.isNotBlank() && publicacionesListas && revision != UiState.Cargando,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                when {
                    !publicacionesListas -> "Cargando publicaciones..."
                    revision == UiState.Cargando -> "Revisando borrador..."
                    else -> "Revisar con IA y buscar similares"
                },
            )
        }
        when (val resultado = revision) {
            UiState.Cargando -> CircularProgressIndicator()
            is UiState.Error -> Text(resultado.mensaje, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            is UiState.Exito -> CardRevision(resultado.datos)
            null -> Unit
        }

        when (val actual = estado) {
            UiState.Cargando -> CircularProgressIndicator()
            is UiState.Error -> {
                Text(actual.mensaje)
                Button(onClick = { viewModel.publicar(titulo, texto) }) { Text("Reintentar") }
            }
            is UiState.Exito -> {
                Text("Publicado con id ${actual.datos.id}")
                Button(onClick = onVolver) { Text("Volver a publicaciones") }
            }
            null -> Unit
        }

        Button(
            onClick = { viewModel.publicar(titulo, texto) },
            enabled = titulo.isNotBlank() && texto.isNotBlank() && estado != UiState.Cargando,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (estado == UiState.Cargando) "Publicando..." else "Publicar")
        }
    }
}
