package com.example.smartshop.ui.viewmodel.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartshop.domain.model.User
import com.example.smartshop.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel  @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    /** LOGIN **/
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = repo.login(email, password)

            _loading.value = false

            if (result != null) {
                _user.value = result  // Login OK
            } else {
                _error.value = "Email ou mot de passe incorrect"
            }
        }
    }

    /** REGISTER **/
    fun register(email: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = repo.register(email, password)

            _loading.value = false

            if (result != null) {
                _user.value = result  // Compte créé OK
            } else {
                _error.value = "Impossible de créer le compte"
            }
        }
    }

    /** RESET des erreurs **/
    fun clearError() {
        _error.value = null
    }


    fun logout() {
        repo.logout()
        _user.value = null
    }

}