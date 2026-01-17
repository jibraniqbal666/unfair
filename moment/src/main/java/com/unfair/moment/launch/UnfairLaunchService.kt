package com.unfair.moment.launch

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.sqrt

/**
 * Foreground service that handles shake detection to launch Unfair activity
 */
@AndroidEntryPoint
class UnfairLaunchService : Service(), SensorEventListener {

    companion object {
        private const val TAG = "UnfairLaunchService"
        private const val SHAKE_THRESHOLD = 12.0f // Acceleration threshold for shake detection
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "unfair_shake_service"

        fun start(context: Context) {
            val intent = Intent(context, UnfairLaunchService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, UnfairLaunchService::class.java)
            context.stopService(intent)
        }
    }

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var wakeLock: PowerManager.WakeLock

    private var lastAcceleration = SensorManager.GRAVITY_EARTH
    private var currentAcceleration = SensorManager.GRAVITY_EARTH
    private var acceleration = 0f

    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Acquire wake lock to keep service running
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "UnfairApp:ShakeDetection",
        )

        if (accelerometer == null) {
            Log.e(TAG, "Accelerometer sensor not available")
            stopSelf()
            return
        }

        Log.d(TAG, "UnfairLaunchService created")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Unfair Shake Detection",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Shake detection service for Unfair launcher"
                setShowBadge(false)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        createNotificationChannel()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Unfair Shake Detection")
            .setContentText("Shake your device to launch Unfair")
            .setSmallIcon(android.R.drawable.ic_menu_compass) // Using system icon
            .setOngoing(true)
            .setSilent(true)
            .setShowWhen(false)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "UnfairLaunchService started")

        // Start foreground service with notification to avoid timeout
        startForeground(NOTIFICATION_ID, createNotification())

        // Register sensor listener
        accelerometer?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI,
            )
        }

        if (!wakeLock.isHeld) {
            wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
        }

        return START_STICKY // Restart if killed
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop foreground service and remove notification
        stopForeground(true)

        // Unregister sensor listener
        sensorManager.unregisterListener(this)

        // Release wake lock
        if (wakeLock.isHeld) {
            wakeLock.release()
        }

        Log.d(TAG, "UnfairLaunchService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            handleAccelerometerData(event.values)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for our use case
    }

    private fun handleAccelerometerData(values: FloatArray) {
        val x = values[0]
        val y = values[1]
        val z = values[2]

        // Calculate the magnitude of acceleration
        lastAcceleration = currentAcceleration
        currentAcceleration = sqrt(x * x + y * y + z * z)
        val delta = currentAcceleration - lastAcceleration
        acceleration = acceleration * 0.9f + delta // Apply low-pass filter

        // Check if shake threshold is exceeded
        if (acceleration > SHAKE_THRESHOLD) {
            Log.d(TAG, "Shake detected! Acceleration: $acceleration")
            launchUnfairActivity()
        }
    }

    private fun launchUnfairActivity() {
        // Only launch if current activity is LawnchairLauncher
        if (getCurrentActivity() != CurrentActivity.Home) {
            Log.d(TAG, "Not launching - current activity is not LawnchairLauncher")
            return
        }
        try {

            val intent = Intent("com.unfair.moment.SHOW_OVERLAY").apply {
                setPackage(packageName)
            }
            sendBroadcast(intent)

            Log.d(TAG, "Successfully triggered Unfair overlay from shake")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show Unfair overlay from shake", e)
        }
    }

    /**
     * Check if the current foreground activity is LawnchairLauncher
     */
    private fun getCurrentActivity(): CurrentActivity {
        try {
            val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager

            // Get the current foreground task
            val runningTasks = activityManager.getRunningTasks(1)
            if (runningTasks.isEmpty()) {
                Log.d(TAG, "No running tasks found")
                return CurrentActivity.None
            }

            val topActivity = runningTasks[0].topActivity
            if (topActivity == null) {
                Log.d(TAG, "No top activity found")
                return CurrentActivity.None
            }

            val currentClassName = topActivity.className
            val currentPackageName = topActivity.packageName

            Log.d(TAG, "Current foreground activity: $currentPackageName/$currentClassName")

            // Check if the current activity is specifically LawnchairLauncher
            return when (currentClassName) {
                "app.lawnchair.LawnchairLauncher" -> CurrentActivity.Home
                "app.lawnchair.UnfairActivity" -> CurrentActivity.Moment
                else -> CurrentActivity.None
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error checking current activity", e)
            // If we can't determine, be conservative and don't launch
            return CurrentActivity.None
        }
    }


}

sealed interface CurrentActivity {
    object Home : CurrentActivity
    object Moment : CurrentActivity
    object None : CurrentActivity
}
