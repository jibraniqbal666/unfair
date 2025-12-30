package com.unfair.moment

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UnfairViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentMode = MutableStateFlow<Mode?>(null)
    val currentMode: StateFlow<Mode?> = _currentMode.asStateFlow()

    private val _availableApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val availableApps: StateFlow<List<AppInfo>> = _availableApps.asStateFlow()

    private val _selectedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val selectedApps: StateFlow<List<AppInfo>> = _selectedApps.asStateFlow()

    init {
        loadInstalledApps()
    }

    fun selectMode(modeType: ModeType) {
        _currentMode.value = Mode(type = modeType, selectedApps = _selectedApps.value)
    }

    fun launchMode() {
        // Mode is launched - the main screen will show the selected apps
        // This could trigger hiding other apps, showing only selected ones, etc.
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val pm = getApplication<Application>().packageManager
            val apps = pm.getInstalledPackages(PackageManager.GET_META_DATA)
                .mapNotNull { packageInfo ->
                    try {
                        val appInfo = packageInfo.applicationInfo ?: return@launch
                        // Filter out system apps and launcher itself
                        if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 ||
                            appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
                        ) {
                            val name = pm.getApplicationLabel(appInfo).toString()
                            val icon = pm.getApplicationIcon(appInfo.packageName)
                            AppInfo(
                                packageName = appInfo.packageName,
                                name = name,
                                icon = icon
                            )
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }
                .sortedBy { it.name }
            _availableApps.value = apps
        }
    }

    fun searchApps(query: String): List<AppInfo> {
        return if (query.isBlank()) {
            _availableApps.value
        } else {
            _availableApps.value.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
            }
        }
    }
}


