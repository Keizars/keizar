package org.keizar.android.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.keizar.android.ui.foundation.ProvideCompositionalLocalsForPreview
import org.keizar.android.ui.foundation.launchInBackground
import org.keizar.android.ui.game.mp.room.ConnectingRoomDialog

@Composable
fun AuthEditScene(
    onClickBack: () -> Unit,
    onSuccessPasswordEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = remember { AuthEditViewModel() }
    Scaffold(
        modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Edit Account")
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
            AuthEditPage(
                viewModel = vm,
                onSuccessPasswordEdit = onSuccessPasswordEdit,
                Modifier.fillMaxSize()
            )
        }
    }
}


@Composable
fun AuthEditPage(
    viewModel: AuthEditViewModel,
    onSuccessPasswordEdit: () -> Unit,
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
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        val passwordError by viewModel.passwordError.collectAsStateWithLifecycle()
        val verifyPasswordError by viewModel.verifyPasswordError.collectAsStateWithLifecycle()

        // is password edit
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.password.value,
                onValueChange = { viewModel.setPassword(it) },
                isError = (passwordError != null),
                label = { Text("New Password") },
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

        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.verifyPassword.value,
                onValueChange = { viewModel.setVerifyPassword(it) },
                isError = (verifyPasswordError != null),
                label = { Text("Verify New Password") },
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                ),
                visualTransformation = PasswordVisualTransformation('*')
            )
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
                if (viewModel.isProcessing.compareAndSet(expect = false, update = true)) {
                    viewModel.launchInBackground {
                        val result = try {
                            viewModel.processedUpdate()
                        } finally {
                            viewModel.isProcessing.compareAndSet(expect = true, update = false)
                        }
                        if (result) {
                            withContext(Dispatchers.Main) {
                                onSuccessPasswordEdit()
                            }
                        }
                    }
                }
            },
            enabled = !viewModel.isProcessing.collectAsStateWithLifecycle().value,
            modifier = Modifier.padding(8.dp),
        ) {
            Text("Update Account")
        }
    }
}

@Preview
@Preview(device = Devices.TABLET)
@Composable
private fun PreviewNickname() {
    ProvideCompositionalLocalsForPreview {
        AuthEditScene(
            onClickBack = {},
            onSuccessPasswordEdit = {},
            Modifier
        )
    }
}


