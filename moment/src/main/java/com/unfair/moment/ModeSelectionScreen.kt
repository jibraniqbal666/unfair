package com.unfair.moment

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unfair.moment.theme.MomentTheme

@Composable
fun ModeSelectionScreen(
    viewModel: ModeViewModel = viewModel(),
    onBack: () -> Unit,
    onNext: (Screen) -> Unit,
) {
    ModeSelectionUI(
        onModeSetting = { modeType ->
            onNext(Screen.AppSelection(modeType.id))
        },
        onModeSelected = {
            viewModel.setMode(it)
            onBack()
        },
        onAddMoment = {},
        onClose = {
            onBack()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectionUI(
    onModeSelected: (ModeType) -> Unit,
    onModeSetting: (ModeType) -> Unit,
    onAddMoment: () -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .padding(top = 16.dp, start = 10.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape,
                            ),
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
        ) {
            Text(
                text = "What's your next Moment?",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Column {
                ModeType.MODES.forEach { mode ->
                    ModeTypeCard(
                        mode = mode,
                        onSet = { onModeSetting(mode) },
                        onSelect = { onModeSelected(mode) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    )
                }
            }
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .clickable { onAddMoment() },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "content description")
                }
            }
        }
    }
}

@Composable
fun ModeTypeCard(
    mode: ModeType,
    onSelect: () -> Unit,
    onSet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Default.AutoAwesome),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = mode.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                IconButton(onClick = { onSet() }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

        }
    }
}

@Composable
@Preview
fun ModeSelectionScreenPreview() {
    ModeSelectionUI(
        onModeSelected = {},
        onClose = {},
        onAddMoment = {},
        onModeSetting = {},
    )
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
fun ModeSelectionScreenNightPreview() {
    MomentTheme {
        ModeSelectionUI(
            onModeSelected = {},
            onClose = {},
            onAddMoment = {},
            onModeSetting = {},
        )
    }
}

@Composable
@Preview
fun ModeTypeCardPreview() {
    ModeTypeCard(ModeType.MODES[0], {}, {})
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
fun ModeTypeCardNightPreview() {
    MomentTheme {
        ModeTypeCard(ModeType.MODES[0], {}, {})
    }
}
