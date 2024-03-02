package org.keizar.android.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
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
    initIsPasswordEdit: Boolean,
    onClickBack: () -> Unit,
    onSuccessPasswordEdit: () -> Unit,
    onSuccessNicknameEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = remember(initIsPasswordEdit) { AuthEditViewModel(initIsPasswordEdit) }
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
                onSuccessNicknameEdit = onSuccessNicknameEdit,
                Modifier.fillMaxSize()
            )
        }
    }
}


@Composable
fun AuthEditPage(
    viewModel: AuthEditViewModel,
    onSuccessPasswordEdit: () -> Unit,
    onSuccessNicknameEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val isPasswordEdit by viewModel.isPasswordEdit.collectAsStateWithLifecycle()
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
        val nicknameError by viewModel.nicknameError.collectAsStateWithLifecycle()

        // is password edit
        AnimatedVisibility(isPasswordEdit) {
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

        AnimatedVisibility(isPasswordEdit) {
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

        // is nickname and avatar edit
        AnimatedVisibility(!isPasswordEdit) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val curNickname = viewModel.getCurrentNickname()
                if (curNickname == "") {
                    Text(
                        text = "Current name: $curNickname",
                        fontSize = 16.sp,
                    )
                } else {
                    Text(
                        text = "Come you set your nickname!",
                        fontSize = 16.sp,
                    )
                }
            }
        }

        AnimatedVisibility(!isPasswordEdit) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = viewModel.nickname.value,
                    onValueChange = { viewModel.setNickname(it) },
                    isError = (nicknameError != null),
                    label = { Text("New nickName") },
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                    )
                )
            }
        }
        AnimatedVisibility(nicknameError != null) {
            nicknameError?.let {
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
                                if (isPasswordEdit) {
                                    onSuccessPasswordEdit()
                                } else {
                                    onSuccessNicknameEdit()
                                }
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

        val highlightStyle =
            SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        val passwordEditMessage = remember(highlightStyle) {
            buildAnnotatedString {
                pushStyle(highlightStyle)
                append("Change nickname")
                pop()
            }
        }

        val nicknameAvatarEditMessage = remember(highlightStyle) {
            buildAnnotatedString {
                pushStyle(highlightStyle)
                append("Change password")
                pop()
            }
        }

        ClickableText(
            text = if (isPasswordEdit) passwordEditMessage else nicknameAvatarEditMessage,
            onClick = { viewModel.onClickSwitch() },
            style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview
@Preview(device = Devices.TABLET)
@Composable
private fun PreviewNickname() {
    ProvideCompositionalLocalsForPreview {
        AuthEditScene(
            initIsPasswordEdit = false,
            onClickBack = {},
            onSuccessPasswordEdit = {},
            onSuccessNicknameEdit = {},
            Modifier
        )
    }
}

@Preview
@Preview(device = Devices.TABLET)
@Composable
private fun PreviewPassword() {
    ProvideCompositionalLocalsForPreview {
        AuthEditScene(
            initIsPasswordEdit = true,
            onClickBack = {},
            onSuccessPasswordEdit = {},
            onSuccessNicknameEdit = {},
            Modifier
        )
    }
}

