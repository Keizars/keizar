package org.keizar.android.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.keizar.android.R
import org.keizar.android.ui.foundation.launchInBackground

@Composable
fun AuthScene(
    initialIsRegister: Boolean,
    onClickBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = remember(initialIsRegister) { AuthViewModel(initialIsRegister) }
    Scaffold(
        modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    val isRegister by vm.isRegister.collectAsStateWithLifecycle()
                    Text(text = if (isRegister) "Register" else "Login")
                },
                navigationIcon = {
                    IconButton(onClick = onClickBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                }
            )
        },
    ) { contentPadding ->
        Column(
            Modifier
                .padding(contentPadding)
                .padding(16.dp)
        ) {
            AuthPage(
                viewModel = vm,
                Modifier.fillMaxSize()
            )
        }
    }
}


@Composable
fun AuthPage(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val errorFontSize = 14.sp
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        val isRegister by viewModel.isRegister.collectAsStateWithLifecycle()
        val usernameError by viewModel.usernameError.collectAsStateWithLifecycle()
        val passwordError by viewModel.passwordError.collectAsStateWithLifecycle()
        val verifyPasswordError by viewModel.verifyPasswordError.collectAsStateWithLifecycle()

        Box(modifier = Modifier.padding(bottom = 64.dp), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = R.drawable.keizar_icon),
                contentDescription = "Keizar Icon",
                tint = Color.Unspecified
            )
        }

        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.username.value,
                onValueChange = { viewModel.setUsername(it) },
                isError = (usernameError != null),
                label = { Text("Username") },
                shape = RoundedCornerShape(8.dp)
            )
        }
        AnimatedVisibility(usernameError != null) {
            usernameError?.let {
                Text(
                    text = it,
                    fontSize = errorFontSize,
                    color = Color.Red,
                )
            }
        }

        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.password.value,
                onValueChange = { viewModel.setPassword(it) },
                isError = (passwordError != null),
                label = { Text("Password") },
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                ),
                visualTransformation = PasswordVisualTransformation('*')
            )
        }
        AnimatedVisibility(passwordError != null) {
            passwordError?.let {
                Text(
                    text = it,
                    fontSize = errorFontSize,
                    color = Color.Red,
                )
            }
        }

        AnimatedVisibility(isRegister) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = viewModel.verifyPassword.value,
                    onValueChange = { viewModel.setVerifyPassword(it) },
                    isError = (verifyPasswordError != null),
                    label = { Text("Verify Password") },
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                    ),
                    visualTransformation = PasswordVisualTransformation('*')
                )
            }
        }
        AnimatedVisibility(verifyPasswordError != null) {
            verifyPasswordError?.let {
                Text(
                    text = it,
                    fontSize = errorFontSize,
                    color = Color.Red,
                )
            }
        }

        Button(
            onClick = {
                println("Click Login: ${viewModel.isProcessing.value}")
                if (viewModel.isProcessing.compareAndSet(expect = false, update = true)) {
                    viewModel.launchInBackground {
                        try {
                            viewModel.proceedLogin()
                        } finally {
                            viewModel.isProcessing.compareAndSet(expect = true, update = false)
                        }
                    }
                }
            },
            enabled = !viewModel.isProcessing.collectAsStateWithLifecycle().value,
            modifier = Modifier.padding(10.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(if (isRegister) "Sign up" else "Login")
        }

        val highlightColor = MaterialTheme.colorScheme.secondary
        val signUpMessage = remember(highlightColor) {
            buildAnnotatedString {
                append("Does not have an account? Please ")
                pushStyle(SpanStyle(color = highlightColor))
                append("sign up")
                pop()
            }
        }

        val loginMessage = remember(highlightColor) {
            buildAnnotatedString {
                append("Already have an account? Please ")
                pushStyle(SpanStyle(color = highlightColor))
                append("login")
                pop()
            }
        }

        ClickableText(
            text = if (!isRegister) signUpMessage else loginMessage,
            onClick = { viewModel.onClickSwitch() },
            style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onBackground),
        )
    }
}

@Preview
@Composable
private fun PreviewAuthScreen() {
    AuthScene(
        initialIsRegister = false,
        onClickBack = {},
        Modifier
    )
}