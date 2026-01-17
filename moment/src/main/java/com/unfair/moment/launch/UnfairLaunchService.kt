package com.unfair.moment.launch

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.unfair.moment.UnfairActivity
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
    private lateinit var vibrator: Vibrator

    private var lastAcceleration = SensorManager.GRAVITY_EARTH
    private var currentAcceleration = SensorManager.GRAVITY_EARTH
    private var acceleration = 0f

    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "UnfairLaunchService started")

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
            val intent = Intent(this, UnfairActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            startActivity(intent)
            Log.d(TAG, "Successfully launched Unfair activity from shake")

            // Provide success haptic feedback
            if (vibrator.hasVibrator()) {
                val pattern = longArrayOf(0, 100, 100, 100) // Short-long-short vibration
                val vibrationEffect = VibrationEffect.createWaveform(pattern, -1)
                vibrator.vibrate(vibrationEffect)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch Unfair activity from shake", e)
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
