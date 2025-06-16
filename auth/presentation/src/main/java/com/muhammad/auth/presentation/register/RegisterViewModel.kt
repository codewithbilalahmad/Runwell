package com.muhammad.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhammad.auth.domain.AuthRepository
import com.muhammad.auth.domain.UserDataValidator
import com.muhammad.auth.presentation.R
import com.muhammad.core.domain.util.DataError
import com.muhammad.core.domain.util.Result
import com.muhammad.core.presentation.ui.UiText
import com.muhammad.core.presentation.ui.asUiText
import com.muhammad.core.presentation.ui.textAsFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val userDataValidator: UserDataValidator,
    private val repository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()
    private val _events = Channel<RegisterEvent>()
    val events = _events.receiveAsFlow()

    init {
        state.value.email.textAsFlow().onEach { email ->
            val isEmailValid = userDataValidator.isValidEmail(email = email.toString())
            _state.update {
                it.copy(
                    isEmailValid = isEmailValid,
                    canRegister = isEmailValid && state.value.passwordValidationState.isValidPassword && !state.value.isRegistering
                )
            }
        }.launchIn(viewModelScope)
        state.value.password.textAsFlow().onEach { password ->
            val isPasswordValid = userDataValidator.validatePassword(password = password.toString())
            _state.update {
                it.copy(
                    passwordValidationState =isPasswordValid, canRegister = state.value.isEmailValid && isPasswordValid.isValidPassword && !state.value.isRegistering
                )
            }
        }.launchIn(viewModelScope)
    }
    fun onAction(action : RegisterAction){
        when(action){
            RegisterAction.OnRegisterClick -> register()
            RegisterAction.OnTogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !state.value.isPasswordVisible) }
            }
            else -> Unit
        }
    }
    private fun register(){
        viewModelScope.launch {
            _state.update { it.copy(isRegistering = true) }
            val result = repository.register(
                email = state.value.email.toString().trim(),
                password = state.value.password.text.toString().trim()
            )
            _state.update { it.copy(isRegistering = false) }
            when(result){
                is Result.Failure -> {
                    if(result.error == DataError.Network.CONFLICT){
                        _events.send(RegisterEvent.Error(
                            UiText.StringResource(R.string.error_email_exists)
                        ))
                    } else{
                        _events.send(RegisterEvent.Error(
                            result.error.asUiText()
                        ))
                    }
                }
                is Result.Success -> {
                    _events.send(RegisterEvent.RegistrationSuccess)
                }
            }
        }
    }
}