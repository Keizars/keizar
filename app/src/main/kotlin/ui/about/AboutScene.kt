package org.keizar.android.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
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
        Box(modifier = Modifier.padding(top = 24.dp), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = R.drawable.keizar_icon),
                contentDescription = "Keizar Icon",
                tint = Color.Unspecified
            )
        }

        val height = 24.dp
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
                Text(text = "A GAME BY", style = MaterialTheme.typography.titleMedium)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ClickableText(text = AnnotatedString("ÁKOS ZUBOR"), style = MaterialTheme.typography.bodyMedium) {

                    }
                }

                Spacer(modifier = Modifier.height(height))

                Text(text = "CO-AUTHORS:", style = MaterialTheme.typography.titleMedium)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ClickableText(
                        text = AnnotatedString("MARTIN DURMAN & LUKE O'NEILL"),
                        style = MaterialTheme.typography.bodyMedium
                    ) {
                    }
                }

                Spacer(modifier = Modifier.height(height))

                Text(text = "GRAPHIC DESIGN:", style = MaterialTheme.typography.titleMedium)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ClickableText(
                        text = AnnotatedString("ÁKOS ZUBOR & DÓRA BÁNYAI"),
                        style = MaterialTheme.typography.bodyMedium
                    ) {
                    }
                }

                Spacer(modifier = Modifier.height(height))

                Text(text = "APP DEVELOPMENT:", style = MaterialTheme.typography.titleMedium)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(text = "Holger Pirk", style = MaterialTheme.typography.bodyMedium)

                    val context = LocalContext.current
                    fun browse(url: String) {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(browserIntent)
                    }
                    ClickableText(text = AnnotatedString("Tianyi Guan"), style = MaterialTheme.typography.bodyMedium) {
                        browse("https://github.com/him188")
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

                Spacer(modifier = Modifier.height(height))

                Text(text = "PUBLISHED BY", style = MaterialTheme.typography.titleMedium)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(text = "ZUBOARD GAMES", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(height))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "THIS IS A BETA VERSION\n FOR TESTING PURPOSES\n ALL DATA HANDLING IS\n" +
                                "COVERED BY\n GDPR\n LAWS",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(height))

                Text(text = "KEIZÁR®", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(height))

                Text(text = "AND")

                Spacer(modifier = Modifier.height(height))

                Row {
                    Spacer(modifier = Modifier.width(height))
                    painterResource(id = R.drawable.keizar).let {
                        Icon(
                            painter = it,
                            contentDescription = "Keizar Icon",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(70.dp)
                        )
                    }
                    Text(text = " ®")
                }

                Spacer(modifier = Modifier.height(height))

                Text(text = "ARE REGISTERED TRADEMARKS", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(height))

                Text(text = "ALL GAME SYMBOLS ARE REGISTERED DESIGNS", style = MaterialTheme.typography.bodyMedium)

            }
        }

        Row(Modifier.padding(top = height)) {
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