package org.keizar.android.ui.profile.component

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.keizar.android.R
import org.keizar.android.ui.external.placeholder.placeholder
import org.keizar.utils.communication.account.User
import java.io.File

@Composable
fun UserInfoRow(
    self: User?,
    onPickAvatar: (Uri) -> Unit,
    onClickEditNickname: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    Row(
        modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(contentPadding)
            .height(IntrinsicSize.Min)
            .heightIn(min = 64.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val imagePicker = rememberImagePicker(onImageSelected = { files ->
            files.firstOrNull()?.let(onPickAvatar)
        })

        Box(
            Modifier
                .clip(CircleShape)
                .clickable {
                    imagePicker.launchPhotoPicker()
                },
            contentAlignment = Alignment.Center,
        ) {
            AvatarImage(
                url = self?.avatarUrlOrDefault(),
                Modifier
                    .placeholder(self == null)
                    .size(64.dp),
            )
        }

        Column(
            Modifier
                .padding(start = 16.dp)
                .fillMaxHeight()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = self?.nickname ?: "Loading...",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.placeholder(self?.nickname == null),
                )

                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(20.dp)
                        .clickable(onClick = onClickEditNickname)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Row(
                Modifier,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                var iconHeight by remember { mutableStateOf(24.dp) }
                Icon(
                    painterResource(id = R.drawable.id_card_fill0_wght400_grad0_opsz24), null,
                    Modifier
                        .padding(end = 4.dp)
                        .size(iconHeight),
                )
                val density = LocalDensity.current
                Text(
                    text = self?.username ?: "Loading...",
                    Modifier
                        .onPlaced {
                            iconHeight = density.run { it.size.height.toDp() }
                        }
                        .placeholder(self?.username == null),
                    style = MaterialTheme.typography.labelLarge,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}


// AvatarImage can be retrieved from a file path or a url
@Composable
fun AvatarImage(
    url: String?, modifier: Modifier = Modifier,
    filePath: String? = null,
    colorFilter: ColorFilter? = null,
) {
    AsyncImage(
        model = if (filePath != null) File(filePath) else url,
        contentDescription = "Avatar",
        modifier,
        placeholder = rememberVectorPainter(Icons.Default.Person),
        error = rememberVectorPainter(Icons.Default.Person),
        contentScale = ContentScale.Crop,
        colorFilter = colorFilter
    )
}


@Preview
@Preview(fontScale = 2f)
@Preview(device = Devices.TABLET)
@Composable
private fun PreviewUserInfoRow() {
    UserInfoRow(
        self = User(
            nickname = "Nickname",
            username = "Username",
            avatarUrl = "",
        ),
        onPickAvatar = {},
        onClickEditNickname = {}
    )
}