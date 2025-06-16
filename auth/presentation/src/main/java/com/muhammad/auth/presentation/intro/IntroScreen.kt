package com.muhammad.auth.presentation.intro

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhammad.core.presentation.designsystem.LogoIcon
import com.muhammad.auth.presentation.R
import com.muhammad.core.presentation.designsystem.components.GradientBackground
import com.muhammad.core.presentation.designsystem.components.RunwellActionButton
import com.muhammad.core.presentation.designsystem.components.RunwellOutlinedActionButton

@Composable
fun IntroScreen(onSignUpClick : () -> Unit, onSignInClick : () -> Unit) {
    IntroScreenContent {action ->
        when(action){
            IntroAction.OnSignInClick -> onSignInClick()
            IntroAction.OnSignUpClick -> onSignUpClick()
        }
    }
}

@Composable
fun IntroScreenContent(onAction: (IntroAction) -> Unit) {
    GradientBackground{
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), contentAlignment = Alignment.Center
        ) {
            RunwellLogoVertical()
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = stringResource(R.string.welcome_to_runwell),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.runwell_description),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(32.dp))
            RunwellOutlinedActionButton(
                text = stringResource(R.string.sign_in),
                isLoading = false,
                onClick = {
                    onAction(IntroAction.OnSignInClick)
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(32.dp))
            RunwellActionButton(
                isLoading = false,
                text = stringResource(R.string.sign_up),
                modifier = Modifier.fillMaxWidth()
            ) {
                onAction(IntroAction.OnSignUpClick)
            }
        }
    }
}

@Composable
fun RunwellLogoVertical(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = LogoIcon,
            contentDescription = "Logo",
            tint = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.runwell),
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}