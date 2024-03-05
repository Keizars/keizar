package org.keizar.android.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.keizar.android.R
import org.keizar.android.ui.foundation.launchInBackground
import org.keizar.android.ui.foundation.moveFocusOnEnter
import org.keizar.android.ui.game.mp.room.ConnectingRoomDialog
import kotlin.time.Duration.Companion.seconds

@Composable
fun AuthScene(
    initialIsRegister: Boolean,
    onClickBack: () -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = remember(initialIsRegister) { AuthViewModel(initialIsRegister) }
    Scaffold(
        modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    val isRegister by vm.isRegister.collectAsStateWithLifecycle()
                    Text(text = if (isRegister) "Sign up" else "Log in")
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
                onSuccess = onSuccess,
                Modifier.fillMaxSize()
            )
        }
    }
}


@Composable
fun AuthPage(
    viewModel: AuthViewModel,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    if (isProcessing) {
        ConnectingRoomDialog(
            text = {
                Text(text = "Processing")
            }
        )
    }

    val errorFontSize = 14.sp
    Column(
        modifier = modifier
            .fillMaxSize()
            .focusGroup(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val isRegister by viewModel.isRegister.collectAsStateWithLifecycle()
        val usernameError by viewModel.usernameError.collectAsStateWithLifecycle()
        val passwordError by viewModel.passwordError.collectAsStateWithLifecycle()
        val verifyPasswordError by viewModel.verifyPasswordError.collectAsStateWithLifecycle()

        Box(
            modifier = Modifier
                .padding(bottom = 64.dp)
                .widthIn(max = 256.dp), contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.keizar_icon),
                contentDescription = "Keizar Icon",
                tint = Color.Unspecified
            )
        }

        val focusManager = LocalFocusManager.current

        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.username.value,
                onValueChange = { viewModel.setUsername(it) },
                isError = (usernameError != null),
                label = { Text("Username") },
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.moveFocusOnEnter(),
                supportingText = usernameError?.let {
                    {
                        Text(it)
                    }
                }
            )
        }

        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var hide by rememberSaveable { mutableStateOf(true) }
            OutlinedTextField(
                value = viewModel.password.value,
                onValueChange = { viewModel.setPassword(it) },
                isError = (passwordError != null),
                label = { Text("Password") },
                shape = MaterialTheme.shapes.medium,
                visualTransformation = if (hide) PasswordVisualTransformation('*') else VisualTransformation.None,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Password,
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.moveFocusOnEnter(),
                supportingText = passwordError?.let {
                    {
                        Text(it)
                    }
                },
                trailingIcon = {
                    if (hide) {
                        IconButton(onClick = { hide = false }) {
                            Icon(Icons.Default.VisibilityOff, contentDescription = "Hide")
                        }
                    } else {
                        IconButton(onClick = { hide = true }) {
                            Icon(Icons.Default.Visibility, contentDescription = "Show")
                        }
                    }
                }
            )
        }

        AnimatedVisibility(isRegister) {
            var hide by rememberSaveable { mutableStateOf(true) }
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = viewModel.verifyPassword.value,
                    onValueChange = { viewModel.setVerifyPassword(it) },
                    isError = (verifyPasswordError != null),
                    label = { Text("Verify Password") },
                    shape = MaterialTheme.shapes.medium,
                    visualTransformation = if (hide) PasswordVisualTransformation('*') else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Password,
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.moveFocusOnEnter(),
                    supportingText = verifyPasswordError?.let {
                        {
                            Text(it)
                        }
                    },
                    trailingIcon = {
                        if (hide) {
                            IconButton(onClick = { hide = false }) {
                                Icon(Icons.Default.VisibilityOff, contentDescription = "Hide")
                            }
                        } else {
                            IconButton(onClick = { hide = true }) {
                                Icon(Icons.Default.Visibility, contentDescription = "Show")
                            }
                        }
                    }
                )
            }
        }

        var showTermsDialog by remember { mutableStateOf(false) }
        if (showTermsDialog) {
            AlertDialog(
                onDismissRequest = { showTermsDialog = false },
                dismissButton = {
                    TextButton(onClick = {
                        showTermsDialog = false
                        viewModel.agreementChecked.value = false
                    }) {
                        Text("Disagree")
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showTermsDialog = false
                        viewModel.agreementChecked.value = true
                    }) {
                        Text("Agree")
                    }
                },
                text = {
                    Column {
                        Text(text = "This is a test version of the KEIZÁR app.")
                        Text(
                            text = "By using this app some data regarding games played within this app will be shared anonymously with the publisher.",
                            Modifier.padding(top = 8.dp)
                        )
                    }
                }
            )
        }

        AnimatedVisibility(isRegister) {
            val agreementChecked by viewModel.agreementChecked.collectAsStateWithLifecycle()
            Column(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Checkbox(
                        checked = agreementChecked,
                        onCheckedChange = { viewModel.agreementChecked.value = it }
                    )

                    val text = buildAnnotatedString {
//                        append(
//                            """
//                        I understand that by using this app some data regarding games played within this app will be shared anonymously with the publisher
//                    """.trimIndent()
//                        )
                        append("I agree to the ")
                        pushStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                        pushStringAnnotation("terms", "terms")
                        append("terms and conditions")
                        pop()
                    }
                    ClickableText(text = text, onClick = {
                        if (text.getStringAnnotations("terms", 0, text.length).isEmpty()) {
                            // Clicking "I agree to the"
                            viewModel.agreementChecked.value = !agreementChecked
                        } else {
                            // Clicking "terms and conditions"
                            showTermsDialog = true
                        }
                    })
                }
            }
        }

        if (viewModel.isProcessing.collectAsStateWithLifecycle().value) {
            ConnectingRoomDialog()
        }

        Button(
            onClick = {
                if (viewModel.isRegister.value && !viewModel.agreementChecked.value) {
                    showTermsDialog = true
                    return@Button
                }
                if (viewModel.isProcessing.compareAndSet(expect = false, update = true)) {
                    viewModel.launchInBackground {
                        try {
                            val result = viewModel.proceedLogin()
                            if (result) {
                                withContext(Dispatchers.Main) {
                                    delay(1.seconds)
                                    onSuccess()
                                }
                            }
                        } finally {
                            viewModel.isProcessing.compareAndSet(expect = true, update = false)
                        }
                    }
                }
            },
            enabled = !viewModel.isProcessing.collectAsStateWithLifecycle().value,
            modifier = Modifier.padding(8.dp),
        ) {
            Text(if (isRegister) "Sign up" else "Log in")
        }

        val highlightStyle =
            SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)
        val signUpMessage = remember(highlightStyle) {
            buildAnnotatedString {
                append("Does not have an account? Please ")
                pushStyle(highlightStyle)
                append("sign up")
                pop()
            }
        }

        val loginMessage = remember(highlightStyle) {
            buildAnnotatedString {
                append("Already have an account? Please ")
                pushStyle(highlightStyle)
                append("log in")
                pop()
            }
        }

        ClickableText(
            text = if (!isRegister) signUpMessage else loginMessage,
            onClick = { viewModel.onClickSwitch() },
            style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview
@Preview(fontScale = 2f)
@Preview(device = Devices.TABLET)
@Composable
private fun PreviewLogin() {
    AuthScene(
        initialIsRegister = false,
        onClickBack = {},
        onSuccess = {},
        Modifier
    )
}

@Preview
@Preview(fontScale = 2f)
@Preview(device = Devices.TABLET)
@Composable
private fun PreviewRegister() {
    AuthScene(
        initialIsRegister = true,
        onClickBack = {},
        onSuccess = {},
        Modifier
    )
}