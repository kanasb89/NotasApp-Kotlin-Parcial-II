package com.notasapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notasapp.data.model.LoginResponse
import com.notasapp.data.repository.LoginRepository
import com.notasapp.utils.Resource
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: LoginRepository) : ViewModel() {

    private val _loginState = MutableLiveData<Resource<LoginResponse>>()
    val loginState: LiveData<Resource<LoginResponse>> = _loginState

    fun login(email: String, password: String) {
        if (_loginState.value is Resource.Loading) return

        _loginState.value = Resource.Loading
        viewModelScope.launch {
            _loginState.value = repository.login(email, password)
        }
    }
}
