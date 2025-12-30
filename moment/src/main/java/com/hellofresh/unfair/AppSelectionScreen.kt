package com.unfair.moment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.drawable.BitmapDrawable
import android.graphics.Canvas
import androidx.compose.foundation.lazy.items
import androidx.core.graphics.createBitmap
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppSelectionScreen(
    modeType: ModeType,
    viewModel: ModeViewModel = viewModel(),
    onContinue: () -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    val selectedApps by viewModel.selectedApps.collectAsState()
    val availableApps by viewModel.availableApps.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(searchQuery, availableApps) {
        viewModel.searchApps(searchQuery)
    }

    AppSelectionUI(
        selectedApps,
        filteredApps,
        searchQuery,
        onContinue,
        onBack,
        onClose,
        viewModel::removeApp,
        viewModel::toggleAppSelection,
        { query -> searchQuery = query },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionUI(
    selectedApps: List<AppInfo>,
    filteredApps: List<AppInfo>,
    searchQuery: String,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onRemove: (app: AppInfo) -> Unit,
    onToggleAppSelection: (app: AppInfo) -> Unit,
    onSearch: (query: String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose up to 5 apps to show in your Moment.") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Button(
                    onClick = onContinue,
                    enabled = selectedApps.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Continue")
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        ) {
            Text(
                text = "These are the apps you'll see. Phone calls will still come through as normal.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            // Selected apps chips
            if (selectedApps.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp),
                ) {
                    items(selectedApps) { app ->
                        SelectedAppChip(
                            app = app,
                            onRemove = { onRemove(app) },
                        )
                    }
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { onSearch(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(filteredApps) { app ->
                    AppItem(
                        app = app,
                        isSelected = selectedApps.contains(app),
                        onToggle = { onToggleAppSelection(app) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedAppChip(
    app: AppInfo,
    onRemove: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.height(40.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(app = app, size = 24.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = app.name,
                fontSize = 12.sp,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(20.dp),
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
fun AppItem(
    app: AppInfo,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.clickable(onClick = onToggle),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            AppIcon(app = app, size = 64.dp)
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.Green,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(Color.White, CircleShape)
                        .padding(2.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = app.name,
            fontSize = 12.sp,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun AppIcon(
    app: AppInfo,
    size: androidx.compose.ui.unit.Dp,
) {
    val iconBitmap = remember(app.packageName) {
        try {
            app.icon?.let { drawable ->
                when (drawable) {
                    is BitmapDrawable -> drawable.bitmap.asImageBitmap()
                    else -> {
                        val bitmap = createBitmap(size.value.toInt(), size.value.toInt())
                        val canvas = Canvas(bitmap)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)
                        bitmap.asImageBitmap()
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    if (iconBitmap != null) {
        Image(
            bitmap = iconBitmap,
            contentDescription = app.name,
            modifier = Modifier.size(size),
            contentScale = ContentScale.Fit,
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = app.name.take(1).uppercase(),
                fontSize = (size.value / 2).sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

