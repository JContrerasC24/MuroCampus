package com.tecsup.murocampus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.murocampus.viewmodel.AsistenteViewModel

@Composable
fun PantallaAsistente(viewModel: AsistenteViewModel) {
    val conversacion by viewModel.conversacion.collectAsStateWithLifecycle()
    var pregunta by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Aprende conversando", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(conversacion.tituloPublicacion, color = MaterialTheme.colorScheme.secondary)
                Text("Contexto: publicación y ${conversacion.cantidadComentarios} comentarios.")
            }
        }

        Text(
            "Privacidad: al consultar, se enviará a Gemini tu pregunta y el texto de la publicación y sus comentarios. Evita incluir información personal.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(conversacion.mensajes) { mensaje ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (mensaje.esEstudiante) Arrangement.End else Arrangement.Start,
                ) {
                    Text(
                        text = mensaje.texto,
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .background(
                                color = if (mensaje.esEstudiante) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(18.dp),
                            )
                            .padding(14.dp),
                    )
                }
            }
            if (conversacion.cargando) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator()
                        Text("El asistente está pensando...")
                    }
                }
            }
            conversacion.error?.let { mensaje ->
                item { Text(mensaje, color = MaterialTheme.colorScheme.error) }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = pregunta,
                onValueChange = { pregunta = it.take(1200) },
                modifier = Modifier.weight(1f),
                label = { Text("Escribe tu consulta") },
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (!conversacion.cargando && pregunta.isNotBlank()) {
                        viewModel.enviar(pregunta)
                        pregunta = ""
                    }
                }),
            )
            Button(
                enabled = pregunta.isNotBlank() && !conversacion.cargando,
                onClick = {
                    viewModel.enviar(pregunta)
                    pregunta = ""
                },
            ) { Text("Enviar") }
        }
    }
}
