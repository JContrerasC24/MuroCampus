package com.tecsup.murocampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.tecsup.murocampus.ui.theme.MuroCampusTheme
import com.tecsup.murocampus.data.PostRepositoryRemote
import com.tecsup.murocampus.data.remote.RetrofitClient
import com.tecsup.murocampus.ui.PantallaLista
import com.tecsup.murocampus.ui.PantallaDetalle
import com.tecsup.murocampus.viewmodel.PostsViewModel
import com.tecsup.murocampus.viewmodel.PostsViewModelFactory

class MainActivity : ComponentActivity() {
    private val postsViewModel: PostsViewModel by viewModels {
        PostsViewModelFactory(PostRepositoryRemote(RetrofitClient.api))
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MuroCampusTheme {
                var idSeleccionado by rememberSaveable { mutableStateOf<Int?>(null) }
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(if (idSeleccionado == null) "MuroCampus" else "Detalle de publicación") },
                            navigationIcon = {
                                if (idSeleccionado != null) {
                                    TextButton(onClick = { idSeleccionado = null }) { Text("Volver") }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        )
                    },
                ) { padding ->
                    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        if (idSeleccionado == null) {
                            PantallaLista(postsViewModel, onPostClick = { post ->
                                idSeleccionado = post.id
                                postsViewModel.abrir(post.id)
                            })
                        } else {
                            androidx.activity.compose.BackHandler {
                                idSeleccionado = null
                            }
                            PantallaDetalle(postsViewModel)
                        }
                    }
                }
            }
        }
    }
}
