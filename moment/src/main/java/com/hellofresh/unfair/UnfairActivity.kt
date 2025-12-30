package com.unfair.moment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UnfairActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent,
                ) {
                    UnfairApp()
                }
            }
        }
    }
}

@Composable
fun UnfairApp(
    viewModel: UnfairViewModel = viewModel(),
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route,
    ) {
        composable(Screen.Main.route) {
            UnfairScreen(navController)
        }

        composable(Screen.ModeSelection.route) {
            ModeSelectionScreen(navController)
        }

        composable(
            route = Screen.AppSelection("").route,
            arguments = listOf(navArgument("modeTypeId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val modeTypeId = backStackEntry.arguments?.getString("modeTypeId") ?: ""
            val modeType = ModeType.MODES.find { it.id == modeTypeId }
                ?: ModeType.MODES.first()

            AppSelectionScreen(
                modeType = modeType,
                onContinue = {
                    viewModel.launchMode()
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
    }
}

@Composable
fun UnfairScreen(
    navController: NavController,
    viewModel: UnfairViewModel = viewModel(),
) {
    val currentMode by viewModel.currentMode.collectAsState()

    UnfairUi(currentMode) {
        navController.navigate(Screen.ModeSelection.route)
    }
}

@Composable
fun UnfairUi(
    currentMode: Mode?,
    onEssentialsClick: () -> Unit,
) {
    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val currentDate = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date())
    val selectedApps = currentMode?.selectedApps ?: emptyList()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)) // Semi-transparent black background
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
                color = Color.White,
            )
            Text(
                text = currentDate,
                fontSize = 18.sp,
                color = Color.LightGray,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "✨ Essentials",
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier
                    .clickable(onClick = onEssentialsClick)
                    .background(Color.White, shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (selectedApps.isNotEmpty()) {
                selectedApps.forEach { app ->
                    Text(
                        text = app.name,
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
            } else {
                Text(
                    text = "No apps selected",
                    fontSize = 14.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
