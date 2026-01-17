package com.unfair.moment

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

object DNDPermissionHelper {

    /**
     * Check if the app has Do Not Disturb access permission
     */
    fun hasDNDPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true // Pre-M devices don't need this permission
        }
    }

    /**
     * Open the DND permission settings page
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun requestDNDPermission(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * Enable or disable Do Not Disturb mode
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun setDNDMode(context: Context, enabled: Boolean) {
        if (!hasDNDPermission(context)) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (enabled) {
            // Enable DND mode - priority only mode
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        } else {
            // Disable DND mode - allow all notifications
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }

    /**
     * Check if DND is currently active
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun isDNDActive(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
    }
}

/**
 * Composable function to handle DND permission state
 */
@Composable
fun rememberDNDPermissionState(): DNDPermissionState {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(DNDPermissionHelper.hasDNDPermission(context)) }
    var isDNDActive by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasPermission) {
                DNDPermissionHelper.isDNDActive(context)
            } else false
        )
    }

    // Check permission status when the composable is first created
    LaunchedEffect(Unit) {
        hasPermission = DNDPermissionHelper.hasDNDPermission(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasPermission) {
            isDNDActive = DNDPermissionHelper.isDNDActive(context)
        }
    }

    return DNDPermissionState(
        hasPermission = hasPermission,
        isDNDActive = isDNDActive,
        requestPermission = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                DNDPermissionHelper.requestDNDPermission(context)
            }
        },
        setDNDEnabled = { enabled ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasPermission) {
                DNDPermissionHelper.setDNDMode(context, enabled)
                isDNDActive = enabled
            }
        },
        refreshPermissionState = {
            hasPermission = DNDPermissionHelper.hasDNDPermission(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasPermission) {
                isDNDActive = DNDPermissionHelper.isDNDActive(context)
            }
        }
    )
}

data class DNDPermissionState(
    val hasPermission: Boolean,
    val isDNDActive: Boolean,
    val requestPermission: () -> Unit,
    val setDNDEnabled: (Boolean) -> Unit,
    val refreshPermissionState: () -> Unit,
)