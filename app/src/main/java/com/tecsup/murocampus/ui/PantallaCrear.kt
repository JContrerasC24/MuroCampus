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

@Composable
fun PantallaCrear(viewModel: PostsViewModel, onVolver: () -> Unit) {
    var titulo by rememberSaveable { mutableStateOf("") }
    var texto by rememberSaveable { mutableStateOf("") }
    val estado by viewModel.creacion.collectAsStateWithLifecycle()
    LaunchedEffect(estado) {
        if (estado is UiState.Exito) {
            titulo = ""
            texto = ""
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Nueva publicación")
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
