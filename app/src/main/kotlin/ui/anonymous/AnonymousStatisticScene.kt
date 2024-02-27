package org.keizar.android.ui.anonymous

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AnonymousStatistics(
    modifier: Modifier = Modifier
) {
    Column(
        Modifier
            .width(1080.dp)
            .height(1920.dp)
    ) {
        // Header
        Row(
            modifier
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Anonymous Statistics",
                modifier = modifier.padding(16.dp)
            )
        }

        // Content
        Row {
            Text(
                text = "This app collects anonymous statistics to help us improve the app.",
                modifier = modifier.padding(16.dp)
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun AnonymousStatisticScene(
    modifier: Modifier = Modifier
) {
    Column(
        Modifier
            .width(1080.dp)
            .height(1920.dp)
    ) {
        AnonymousStatistics(Modifier.fillMaxSize())
    }
}