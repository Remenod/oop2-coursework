package com.remenod.oop2_coursework.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory<T : ViewModel>(
    private val creator: () -> T
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST") // disable stupid ass JVM warn
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return creator() as T
    }
}
