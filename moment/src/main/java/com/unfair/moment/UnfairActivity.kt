package com.unfair.moment

import android.app.ActivityOptions
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unfair.moment.theme.MomentTheme
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class UnfairActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MomentTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    UnfairApp {
                        launchApp(it)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Set custom return animation when returning from an app
        // Remove the isTaskRoot check as it might be preventing the animation
        overridePendingTransition(R.anim.launcher_return_enter, R.anim.launcher_return_exit)
    }

    fun launchApp(app: AppInfo) {
        val pm = application.packageManager
        val intent = pm.getLaunchIntentForPackage(app.packageName)
        intent?.let {
            it.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)

            // Use our custom fade + scale animations
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.app_launch_enter,
                R.anim.app_launch_exit
            )

            startActivity(it, options.toBundle())
        }
    }
}

@Composable
fun UnfairApp(onLaunch: (app: AppInfo) -> Unit) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route,
    ) {
        composable(Screen.Main.route) {
            UnfairScreen(navController, onLaunch = onLaunch)
        }

        composable(Screen.ModeSelection.route) {
            ModeSelectionScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNext = {
                    navController.navigate(it.route)
                },
                onAddMoment = {
                    navController.navigate(Screen.AddMoment.route)
                },
            )
        }

        composable("mode_preferences/{modeTypeId}") { backStackEntry ->
            val modeTypeId = backStackEntry.arguments?.getString("modeTypeId") ?: ""

            ModePreferencesScreen(
                modeTypeId = modeTypeId,
                onBack = {
                    navController.popBackStack()
                },
                onAppSelectionClick = {
                    navController.navigate(Screen.AppSelection(modeTypeId).route)
                },
            )
        }

        composable("app_selection/{modeTypeId}") { backStackEntry ->
            val modeTypeId = backStackEntry.arguments?.getString("modeTypeId") ?: ""

            AppSelectionScreen(
                modeTypeId = modeTypeId,
                onContinue = {
                    navController.popBackStack(Screen.Main.route, inclusive = false)
                },
                onBack = {
                    navController.popBackStack()
                },
                onClose = {
                    navController.popBackStack(Screen.Main.route, inclusive = false)
                },
            )
        }

        composable(Screen.AddMoment.route) {
            AddMomentScreen(
                onBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}

@Composable
fun UnfairScreen(
    navController: NavController,
    viewModel: UnfairViewModel = hiltViewModel(),
    onLaunch: (app: AppInfo) -> Unit,
) {
    val currentMode by viewModel.currentMode.collectAsState()
    val dndPermissionState = rememberDNDPermissionState()

    UnfairUi(
        currentMode,
        dndPermissionState,
        onEssentialsClick = {
            navController.navigate(Screen.ModeSelection.route)
        },
        onLaunch,
    )
}

@Composable
fun UnfairUi(
    currentMode: Mode?,
    dndPermissionState: DNDPermissionState,
    onEssentialsClick: () -> Unit,
    onLaunch: (app: AppInfo) -> Unit,
) {
    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val currentDate = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date())
    val selectedApps = currentMode?.selectedApps ?: emptyList()
    LaunchedEffect(currentMode?.isDNDActive) {
        dndPermissionState.setDNDEnabled(currentMode?.isDNDActive ?: false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = currentTime,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = currentDate,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = CircleShape,
                modifier = Modifier,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Text(
                    text = currentMode?.type?.name ?: "Set Mode",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clickable(onClick = onEssentialsClick)
                        .padding(horizontal = 28.dp, vertical = 12.dp),
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (selectedApps.isNotEmpty()) {
                selectedApps.forEach { app ->
                    TextButton(
                        onClick = { onLaunch(app) }
                    ) {
                        Text(
                            text = app.name,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                }
            } else {
                Text(
                    text = "No apps selected",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Preview
@Composable
fun UnfairUiPreview() {
    UnfairUi(
        Mode(
            ModeType.MODES.first(),
            listOf(
                AppInfo("1", "Chrome"),
                AppInfo("2", "Spotify"),
                AppInfo("3", "Netflix"),
                AppInfo("4", "Discord"),
                AppInfo("5", "WhatsApp"),
            ),
        ),
        dndPermissionState = DNDPermissionState(
            hasPermission = true,
            isDNDActive = false,
            requestPermission = {},
            setDNDEnabled = {},
            refreshPermissionState = {},
        ),
        {}, { _ -> },
    )
}

@Preview
@Composable
fun UnfairUi2Preview() {
    UnfairUi(
        Mode(
            ModeType.MODES.first(),
        ),
        dndPermissionState = DNDPermissionState(
            hasPermission = true,
            isDNDActive = false,
            requestPermission = {},
            setDNDEnabled = {},
            refreshPermissionState = {},
        ),
        {}, { _ -> },
    )
}
