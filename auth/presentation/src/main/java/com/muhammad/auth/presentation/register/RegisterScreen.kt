package com.muhammad.auth.presentation.register

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muhammad.auth.domain.UserDataValidator
import com.muhammad.auth.presentation.R
import com.muhammad.core.presentation.designsystem.CheckIcon
import com.muhammad.core.presentation.designsystem.CrossIcon
import com.muhammad.core.presentation.designsystem.EmailIcon
import com.muhammad.core.presentation.designsystem.Poppins
import com.muhammad.core.presentation.designsystem.RunwellDarkRed
import com.muhammad.core.presentation.designsystem.RunwellGreen
import com.muhammad.core.presentation.designsystem.components.GradientBackground
import com.muhammad.core.presentation.designsystem.components.RunwellActionButton
import com.muhammad.core.presentation.designsystem.components.RunwellPasswordTextField
import com.muhammad.core.presentation.designsystem.components.RunwellTextField
import com.muhammad.core.presentation.ui.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegisterScreen(
    onSignInClick: () -> Unit,
    onSuccessfulRegistration: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val keyboardController= LocalSoftwareKeyboardController.current
    ObserveAsEvents(viewModel.events) {event ->
        when(event){
            is RegisterEvent.Error -> {
                keyboardController?.hide()
                Toast.makeText(context, event.error.asString(context), Toast.LENGTH_LONG).show()
            }
            is RegisterEvent.RegistrationSuccess ->{
                keyboardController?.hide()
                Toast.makeText(context, R.string.registration_successful, Toast.LENGTH_LONG).show()
                onSuccessfulRegistration()
            }
        }
    }
    RegisterScreenContent(state = state, onAction = {action ->
        when(action){
            is RegisterAction.OnLoginClick -> onSignInClick()
            else -> Unit
        }
        viewModel.onAction(action)
    })
}

@Composable
private fun RegisterScreenContent(
    modifier: Modifier = Modifier,
    state: RegisterState,
    onAction: (RegisterAction) -> Unit,
) {
    val autoFillManager = LocalAutofillManager.current
    GradientBackground {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .padding(top = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.create_account),
                style = MaterialTheme.typography.headlineMedium
            )
            val annotatedString = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontFamily = Poppins, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    append(stringResource(R.string.already_have_an_account) + " ")
                    pushStringAnnotation(
                        tag = "clickable_text",
                        annotation = stringResource(R.string.login)
                    )
                    withStyle(
                        style = SpanStyle(
                            fontFamily = Poppins,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append(stringResource(R.string.login))
                    }
                }
            }
            ClickableText(text = annotatedString) { offset ->
                annotatedString.getStringAnnotations(
                    tag = "clickable_text", start = offset, end = offset
                ).firstOrNull()?.let {
                    onAction(RegisterAction.OnLoginClick)
                }
            }
            Spacer(Modifier.height(48.dp))
            RunwellTextField(
                state = state.email,
                startIcon = EmailIcon,
                endIcon = null,
                keyboardType = KeyboardType.Email,
                hint = stringResource(R.string.example_email),
                title = stringResource(R.string.email),
                modifier = Modifier.fillMaxWidth(), type = ContentType.EmailAddress
            )
            Spacer(Modifier.height(16.dp))
            RunwellPasswordTextField(
                state = state.password, type = ContentType.NewPassword,
                isPasswordVisible = state.isPasswordVisible,
                onTogglePasswordVisibility = {
                    onAction(RegisterAction.OnTogglePasswordVisibility)
                },
                hint = stringResource(R.string.password),
                title = stringResource(R.string.password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            PasswordRequirement(
                text = stringResource(
                    id = R.string.at_least_x_characters,
                    UserDataValidator.MIN_PASSWORD_LENGTH
                ), isValid = state.passwordValidationState.hasMinLength
            )
            Spacer(Modifier.height(4.dp))
            PasswordRequirement(
                text = stringResource(
                    id = R.string.at_least_one_number
                ), isValid = state.passwordValidationState.hasNumber
            )
            Spacer(Modifier.height(4.dp))
            PasswordRequirement(
                text = stringResource(
                    id = R.string.contains_lowercase_char
                ), isValid = state.passwordValidationState.hasLowerCaseCharacter
            )
            Spacer(Modifier.height(4.dp))
            PasswordRequirement(
                text = stringResource(
                    id = R.string.contains_uppercase_char
                ), isValid = state.passwordValidationState.hasUpperCaseCharacter
            )
            Spacer(Modifier.height(32.dp))
            RunwellActionButton(
                text = stringResource(R.string.register),
                isLoading = state.isRegistering,
                enabled = state.canRegister,
                modifier = Modifier.fillMaxWidth()
            ) {
                autoFillManager?.commit()
                onAction(RegisterAction.OnRegisterClick)
            }
        }
    }
}

@Composable
fun PasswordRequirement(modifier: Modifier = Modifier, text: String, isValid: Boolean) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        val icon = if (isValid) CheckIcon else CrossIcon
        val tint = if (isValid) RunwellGreen else RunwellDarkRed
        Icon(imageVector = icon, contentDescription = null, tint = tint)
        Spacer(Modifier.width(16.dp))
        Text(text = text, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}