package com.tecsup.murocampus.data

import com.tecsup.murocampus.model.Usuario

interface UsuarioRepository {
    suspend fun listar(): Result<List<Usuario>>
}
