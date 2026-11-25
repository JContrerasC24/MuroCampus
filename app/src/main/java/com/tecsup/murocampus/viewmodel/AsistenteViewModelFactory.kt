package com.tecsup.murocampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tecsup.murocampus.data.ai.AsistenteAcademicoRepository

class AsistenteViewModelFactory(
    private val repository: AsistenteAcademicoRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AsistenteViewModel::class.java))
        return AsistenteViewModel(repository) as T
    }
}
