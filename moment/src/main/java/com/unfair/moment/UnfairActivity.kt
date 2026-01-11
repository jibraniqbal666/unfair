package com.unfair.moment

import android.app.ActivityOptions
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
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

        val isLightTheme =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES

        enableEdgeToEdge(
            statusBarStyle = if (isLightTheme) {
                SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                )
            } else {
                SystemBarStyle.dark(
                    android.graphics.Color.TRANSPARENT,
                )
            },
        )

        setContent {
            MomentTheme {
                // Subtle radial gradient with theme colors
                val gradientBackground = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        colorResource(R.color.main_500),
                    ),
                    radius = 6000f,
                )
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(gradientBackground),
                    color = Color.Transparent,
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
                R.anim.app_launch_exit,
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
            .padding(20.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = currentTime,
                fontSize = 64.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = currentDate,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.clickable { onEssentialsClick() },
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ) {
                Text(
                    text = currentMode?.type?.name ?: "Set Mode",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(24.dp, 16.dp),
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
                        onClick = { onLaunch(app) },
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    ) {
                        Text(
                            text = app.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 12.dp),
                        )
                    }
                }
            } else {
                Text(
                    text = "No apps selected for this mode",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 16.dp),
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
