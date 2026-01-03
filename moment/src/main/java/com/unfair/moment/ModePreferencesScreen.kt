package com.unfair.moment

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.unfair.moment.theme.MomentTheme

@Composable
fun ModePreferencesScreen(
    modeTypeId: String,
    onBack: () -> Unit,
    onAppSelectionClick: () -> Unit,
    viewModel: ModePreferencesViewModel = hiltViewModel(),
) {
    val modeType by viewModel.modeType.collectAsState()
    val dndPermissionState = rememberDNDPermissionState()
    val isDNDEnabled by viewModel.dndEnabled.collectAsState()
    val deleted by viewModel.deleted.collectAsState()

    LaunchedEffect(true) {
        viewModel.setMode(modeTypeId)
    }

    LaunchedEffect(deleted) {
        if (deleted) {
            onBack()
        }
    }

    ModePreferencesUI(
        modeType = modeType,
        dndPermissionState = dndPermissionState,
        isDNDEnabled = isDNDEnabled,
        onBack = onBack,
        onAppSelectionClick = onAppSelectionClick,
        onToggleDND = viewModel::toggleDND,
        onDelete = viewModel::deleteMoment,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModePreferencesUI(
    modeType: ModeType?,
    dndPermissionState: DNDPermissionState,
    isDNDEnabled: Boolean,
    onBack: () -> Unit,
    onAppSelectionClick: () -> Unit,
    onToggleDND: () -> Unit,
    onDelete: () -> Unit = {},
) {
    if (modeType == null) return

    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${modeType.name} Settings",
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape,
                            ),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Mode description
            Text(
                text = modeType.description,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // App selection preference
            PreferenceCard(
                title = "Select Apps",
                subtitle = "Choose which apps to show during this moment",
                icon = Icons.Default.Apps,
                onClick = onAppSelectionClick,
                showArrow = true,
            )

            // Do Not Disturb preference
            PreferenceCard(
                title = "Do Not Disturb",
                subtitle = if (dndPermissionState.hasPermission) {
                    "Silence notifications during this moment"
                } else {
                    "Tap to grant permission for Do Not Disturb access"
                },
                icon = Icons.Default.DoNotDisturb,
                onClick = {
                    if (!dndPermissionState.hasPermission) {
                        dndPermissionState.requestPermission()
                    } else {
                        dndPermissionState.setDNDEnabled(!dndPermissionState.isDNDActive)
                    }
                },
                showArrow = !dndPermissionState.hasPermission,
                trailing = if (dndPermissionState.hasPermission) {
                    {
                        Switch(
                            checked = isDNDEnabled,
                            onCheckedChange = { onToggleDND() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        )
                    }
                } else null,
            )

            // Delete button for custom moments
            if (modeType.isCustom) {
                PreferenceCard(
                    title = "Delete Moment",
                    subtitle = "Remove this custom moment permanently",
                    icon = Icons.Default.Delete,
                    onClick = { showDeleteDialog = true },
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Additional info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "About ${modeType.name} Mode",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Customize your moment by selecting the apps you want to access and choosing whether to enable Do Not Disturb. Your settings will be saved for future use.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text("Delete Moment")
            },
            text = {
                Text("Are you sure you want to delete \"${modeType.name}\"? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
fun PreferenceCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    showArrow: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (trailing != null) {
                trailing()
            } else if (showArrow) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Navigate",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Preview
@Composable
fun ModePreferencesScreenPreview() {
    MomentTheme {
        ModePreferencesUI(
            modeType = ModeType.MODES[0],
            dndPermissionState = DNDPermissionState(
                hasPermission = true,
                isDNDActive = false,
                requestPermission = {},
                setDNDEnabled = {},
                refreshPermissionState = {},
            ),
            onBack = {},
            onAppSelectionClick = {},
            onToggleDND = {},
            onDelete = {},
            isDNDEnabled = true,
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ModePreferencesScreenNightPreview() {
    MomentTheme {
        ModePreferencesUI(
            modeType = ModeType.MODES[1],
            dndPermissionState = DNDPermissionState(
                hasPermission = false,
                isDNDActive = false,
                requestPermission = {},
                setDNDEnabled = {},
                refreshPermissionState = {},
            ),
            onBack = {},
            onAppSelectionClick = {},
            onToggleDND = {},
            onDelete = {},
            isDNDEnabled = false,
        )
    }
}

@Preview
@Composable
fun PreferenceCardPreview() {
    MomentTheme {
        PreferenceCard(
            title = "Select Apps",
            subtitle = "Choose which apps to show during this moment",
            icon = Icons.Default.Apps,
            onClick = {},
            showArrow = true,
        )
    }
}
