package com.unfair.moment.launch

import android.content.Context
import android.util.Log
import javax.inject.Inject

/**
 * Manager for Unfair activity shake launch mechanism
 */
class UnfairLaunchManager @Inject constructor(
    private val context: Context,
) {

    companion object {
        private const val TAG = "UnfairLaunchManager"
    }

    /**
     * Start shake detection service
     */
    fun startLaunchServices() {
        Log.d(TAG, "Starting Unfair shake detection service...")

        try {
            UnfairLaunchService.start(context)
            Log.d(TAG, "Shake detection service started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start shake detection service", e)
        }
    }

    /**
     * Stop shake detection service
     */
    fun stopLaunchServices() {
        Log.d(TAG, "Stopping Unfair shake detection service...")

        try {
            UnfairLaunchService.stop(context)
            Log.d(TAG, "Shake detection service stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop shake detection service", e)
        }
    }
}

