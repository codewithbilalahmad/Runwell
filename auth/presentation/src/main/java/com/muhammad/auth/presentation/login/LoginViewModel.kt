package com.muhammad.auth.presentation.login

import com.muhammad.auth.domain.AuthRepository
import com.muhammad.auth.domain.UserDataValidator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhammad.auth.presentation.R
import com.muhammad.core.domain.util.DataError
import com.muhammad.core.domain.util.Result
import com.muhammad.core.presentation.ui.UiText
import com.muhammad.core.presentation.ui.asUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.muhammad.core.presentation.ui.textAsFlow
import kotlin.math.log

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userDataValidator: UserDataValidator,
) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()
    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    init {
        combine(
            state.value.email.textAsFlow(),
            state.value.password.textAsFlow()
        ) { email, password ->
            _state.update {
                it.copy(
                    canLogin = userDataValidator.isValidEmail(email = email.toString().trim()) && password.isNotEmpty()
                )
            }
        }.launchIn(viewModelScope)
    }
    fun onAction(action : LoginAction){
        when(action){
            LoginAction.OnLoginClick -> login()
            LoginAction.OnTogglePasswordVisibility ->{
                _state.update { it.copy(isPasswordVisible = !state.value.isPasswordVisible) }
            }
            else -> Unit
        }
    }
    private fun  login(){
        viewModelScope.launch {
            _state.update { it.copy(isLoggingIn = true) }
            val result = authRepository.login(
                email = state.value.email.toString().trim(),
                password = state.value.password.toString().trim(),
            )
            _state.update { it.copy(isLoggingIn = false) }
            when(result){
                is Result.Failure -> {
                    if(result.error == DataError.Network.UNAUTHORIZED){
                        _events.send(LoginEvent.Error(
                            UiText.StringResource(R.string.error_email_password_incorrect)
                        ))
                    } else{
                        _events.send(LoginEvent.Error(result.error.asUiText()))
                    }
                }
                is Result.Success -> {
                    _events.send(LoginEvent.LoginSuccess)
                }
            }
        }
    }
}