package org.keizar.android.ui.tutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.keizar.android.tutorial.Tutorials
import org.keizar.android.ui.rules.RuleBookPage

@Composable
fun TutorialSelectionScene(
    onClickBack: () -> Unit,
    onClickRuleBook: (page: RuleBookPage) -> Unit,
    onClickTutorial: (id: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Tutorial") },
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
        TutorialSelectionPage(
            onClickRuleBook,
            onClickTutorial,
            Modifier
                .padding(contentPadding)
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun TutorialSelectionPage(
    onClickRuleBook: (page: RuleBookPage) -> Unit,
    onClickTutorial: (id: String) -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalContentColor providesDefault MaterialTheme.colorScheme.onSurface) {
        Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Rule Book", style = MaterialTheme.typography.titleLarge)

            Column(Modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TutorialCard(
                    title = { Text(text = "Symbols") },
                    onClick = { onClickRuleBook(RuleBookPage.SYMBOLS) },
                    description = {
                        Text("See the meaning of the symbols on the game board.")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                TutorialCard(
                    title = { Text(text = "Rules") },
                    onClick = { onClickRuleBook(RuleBookPage.RULES) },
                    description = {
                        Text("Read detailed game rules.")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text("Demo", style = MaterialTheme.typography.titleLarge)

            Column(Modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TutorialCard(
                    title = { Text(text = "Moves Demo") },
                    onClick = { onClickTutorial(Tutorials.Refresher1.id) },
                    description = {
                        Text("Learn how to move pieces as a pawn, a rook, a knight, a bishop, a queen and a king. ")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                TutorialCard(
                    title = { Text(text = "Win Demo") },
                    onClick = { onClickTutorial(Tutorials.Refresher2.id) },
                    description = {
                        Text("Learn how to end the game by staying on the Keizar tile.")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TutorialCard(
    title: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: @Composable (() -> Unit)? = null,
) {
    ElevatedCard(
        onClick = onClick,
        modifier,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(all = 16.dp)) {
            ProvideTextStyle(value = MaterialTheme.typography.titleMedium) {
                title()
            }

            description?.let { desc ->
                Box(modifier = Modifier.padding(top = 4.dp)) {
                    ProvideTextStyle(value = MaterialTheme.typography.labelMedium) {
                        desc()
                    }
                }
            }
        }
    }
}

@Composable
fun TutorialSelectionDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
    ) {
        val shape = RoundedCornerShape(12.dp)
        Column(
            modifier
                .clip(shape)
                .shadow(4.dp)
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

@Composable
@Preview
private fun PreviewTutorialSelectionPage() {
    TutorialSelectionScene({}, {}, {})
}

@Composable
@Preview(showBackground = true)
private fun PreviewTutorialSelectionDialog() {
    TutorialSelectionDialog(onDismissRequest = { }) {
        TutorialSelectionPage(
            onClickTutorial = {
            },
            onClickRuleBook = {
            },
            modifier = Modifier.padding(all = 16.dp),
        )
    }
}