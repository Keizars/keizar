package org.keizar.android.ui.profile.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

interface ImagePicker {
    val selectedImages: List<Uri?>
    fun launchPhotoPicker()
}

@Composable
fun rememberImagePicker(
    maxSelectionCount: Int = 1,
    onImageSelected: (List<Uri>) -> Unit,
    onDismissed: () -> Unit = {},
): ImagePicker {
    var selectedImages by remember {
        mutableStateOf<List<Uri>>(emptyList())
    }

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            selectedImages = listOfNotNull(uri)
            if (selectedImages.isEmpty()) {
                onDismissed()
            } else {
                onImageSelected(selectedImages)
            }
        }
    )

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(
            maxItems = if (maxSelectionCount > 1) {
                maxSelectionCount
            } else {
                2
            }
        ),
        onResult = { uris ->
            selectedImages = uris
            if (selectedImages.isEmpty()) {
                onDismissed()
            } else {
                onImageSelected(selectedImages)
            }
        }
    )

    fun launchPhotoPicker() {
        if (maxSelectionCount > 1) {
            multiplePhotoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            singlePhotoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }
    return object : ImagePicker {
        override val selectedImages: List<Uri?>
            get() = selectedImages

        override fun launchPhotoPicker() {
            launchPhotoPicker()
        }
    }
}