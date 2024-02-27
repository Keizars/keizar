package org.keizar.android.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.keizar.android.R
import org.keizar.android.ui.home.CopyrightText

@Composable
fun AboutScene(
    onClickBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "About") },
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
            AboutPage(Modifier.fillMaxSize())
        }
    }
}

@Composable
fun AboutPage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Title or logo can be added here
        Box(modifier = Modifier.padding(top = 48.dp), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = R.drawable.keizar_icon),
                contentDescription = "Keizar Icon",
                tint = Color.Unspecified
            )
        }

        Column(
            modifier = modifier
                .weight(1f)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "Game Designer", style = MaterialTheme.typography.titleMedium)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ClickableText(text = AnnotatedString("Akos Zubor"), style = MaterialTheme.typography.bodyMedium) {

                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(text = "Software Developed by", style = MaterialTheme.typography.titleMedium)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ClickableText(text = AnnotatedString("Tianyi Guan"), style = MaterialTheme.typography.bodyMedium) {

                    }
                    ClickableText(text = AnnotatedString("Shengyue Zhu"), style = MaterialTheme.typography.bodyMedium) {

                    }
                    ClickableText(text = AnnotatedString("Yiqin Li"), style = MaterialTheme.typography.bodyMedium) {

                    }
                    ClickableText(text = AnnotatedString("Fengkai Liu"), style = MaterialTheme.typography.bodyMedium) {

                    }
                    ClickableText(text = AnnotatedString("Jerry Zhu"), style = MaterialTheme.typography.bodyMedium) {

                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(text = "Acknowledgements", style = MaterialTheme.typography.titleMedium)

                    Text(text = "Holger Pirk", style = MaterialTheme.typography.bodyMedium)

                    Text(text = "Imperial College London", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Row(Modifier.padding(top = 16.dp)) {
            CopyrightText()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAboutScene(
    modifier: Modifier = Modifier
) {
    Column(
        Modifier
            .width(1080.dp)
            .height(1920.dp)
    ) {
        AboutScene({}, Modifier.fillMaxSize())
    }
}