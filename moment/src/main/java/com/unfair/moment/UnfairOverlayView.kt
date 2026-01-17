package com.unfair.moment

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unfair.moment.theme.MomentTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Custom overlay view that displays the Unfair interface on top of the launcher
 * We are doing this due to launcher canceling our activity when user comes back to home or
 * go to recents
 */
@AndroidEntryPoint
class UnfairOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private var onDismissCallback: (() -> Unit)? = null
    private var composeView: ComposeView

    init {
        // Make the overlay take full screen
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        // Don't fit system windows - we want to draw behind status bar and nav bar
        fitsSystemWindows = false

        // Set up compose view
        composeView = ComposeView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            fitsSystemWindows = false
        }

        addView(composeView)

        // Consume window insets so we can draw behind system bars
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            // Don't consume - let the view draw behind system bars
            insets
        }

        // Set up the compose content
        setupComposeContent()

        // Make the view focusable to intercept back button
        isFocusable = true
        isFocusableInTouchMode = true
    }

    fun setOnDismissCallback(callback: () -> Unit) {
        onDismissCallback = callback
    }

    private fun setupComposeContent() {
        composeView.setContent {
            UnfairOverlayContent(
                onClose = {
                    onDismissCallback?.invoke()
                },
                onLaunchApp = { appInfo ->
                    // Launch app through the launcher
                    val intent =
                        context.packageManager.getLaunchIntentForPackage(appInfo.packageName)
                    if (intent != null) {
                        context.startActivity(intent)
                    }
                    // Don't dismiss - overlay will persist when returning home
                },
            )
        }
    }

    // Intercept touch events to prevent them from going to the launcher behind
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true // Consume all touch events
    }

    // Handle back button press - let Compose BackHandler handle it for dialog support
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        // Don't intercept back button here - let Compose BackHandler show the confirmation dialog
        return super.dispatchKeyEvent(event)
    }
}

@Composable
private fun UnfairOverlayContent(
    onClose: () -> Unit,
    onLaunchApp: (AppInfo) -> Unit,
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    var showCloseDialog by remember { mutableStateOf(false) }

    // Function to handle actual close - turns off DND and closes
    val performClose: () -> Unit = {
        // Turn off DND if it's active
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            DNDPermissionHelper.hasDNDPermission(context) &&
            DNDPermissionHelper.isDNDActive(context)
        ) {
            DNDPermissionHelper.setDNDMode(context, false)
        }
        onClose()
    }

    // Function to request close - shows dialog
    val requestClose: () -> Unit = {
        showCloseDialog = true
    }

    // Handle back button to close the overlay
    BackHandler {
        if (!navController.popBackStack()) {
            requestClose()
        }
    }

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
            UnfairNavigation(
                navController = navController,
                onLaunchApp = onLaunchApp,
                onClose = requestClose,
            )
        }

        // Confirmation dialog
        if (showCloseDialog) {
            AlertDialog(
                onDismissRequest = { showCloseDialog = false },
                title = { Text("Exit Moment?") },
                text = { Text("Are you sure you want to close? This was supposed to help you damn it.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showCloseDialog = false
                            performClose()
                        },
                    ) {
                        Text("Yes, Exit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCloseDialog = false }) {
                        Text("Cancel")
                    }
                },
            )
        }
    }
}

@Composable
private fun UnfairNavigation(
    navController: NavHostController,
    onLaunchApp: (AppInfo) -> Unit,
    onClose: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route,
    ) {
        composable(Screen.Main.route) {
            UnfairMainScreen(
                navController = navController,
                onLaunchApp = onLaunchApp,
                onClose = onClose,
            )
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
private fun UnfairMainScreen(
    navController: NavController,
    viewModel: UnfairViewModel = hiltViewModel(),
    onLaunchApp: (AppInfo) -> Unit,
    onClose: () -> Unit,
) {
    val currentMode by viewModel.currentMode.collectAsState()
    val dndPermissionState = rememberDNDPermissionState()

    Box(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(20.dp),
    ) {
        UnfairUi(
            currentMode = currentMode,
            dndPermissionState = dndPermissionState,
            onEssentialsClick = {
                navController.navigate(Screen.ModeSelection.route)
            },
            onLaunch = onLaunchApp,
        )
    }
}
