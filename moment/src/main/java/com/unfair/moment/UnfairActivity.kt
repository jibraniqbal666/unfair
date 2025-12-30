package com.unfair.moment

import android.os.Bundle
import android.util.Log
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
            ModeSelectionScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNext = {
                    navController.navigate(it.route)
                },
            )
        }

        composable("app_selection/{modeTypeId}") { backStackEntry ->
            val modeTypeId = backStackEntry.arguments?.getString("modeTypeId") ?: ""
            Log.i("AppSelection", modeTypeId)
            val modeType = ModeType.MODES.find { it.id == modeTypeId }
                ?: ModeType.MODES.first()

            AppSelectionScreen(
                modeType = modeType,
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
                text = currentMode?.type?.name ?: "Set Mode",
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
