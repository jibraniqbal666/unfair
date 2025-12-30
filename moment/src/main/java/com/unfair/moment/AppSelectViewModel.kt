package com.unfair.moment

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unfair.moment.database.AppSelectionDatabase
import com.unfair.moment.database.AppSelectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppSelectViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppSelectionDatabase.getDatabase(application)
    private val repository = AppSelectionRepository(
        database.appSelectionDao(),
        database.savedModeDao(),
    )

    private val _modeType = MutableStateFlow<ModeType?>(null)

    private val _availableApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val availableApps: StateFlow<List<AppInfo>> = _availableApps.asStateFlow()

    private val _selectedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val selectedApps: StateFlow<List<AppInfo>> = _selectedApps.asStateFlow()

    init {
        loadInstalledApps()
    }

    fun setMode(modeType: ModeType) {
        _modeType.value = modeType

        viewModelScope.launch {
            val savedMode = repository.loadSavedMode(modeType.id)
            savedMode?.let { saveMode ->
                _selectedApps.value = _availableApps.value.filter { app ->
                    saveMode.selectedApps.any { app.packageName == it.packageName }
                }
            }
        }
    }

    fun toggleAppSelection(app: AppInfo) {
        val current = _selectedApps.value.toMutableList()
        if (current.contains(app)) {
            current.remove(app)
        } else {
            if (current.size < 5) {
                current.add(app)
            }
        }
        _selectedApps.value = current
        _modeType.value?.let { modeType ->
            viewModelScope.launch {
                repository.saveAppSelectionsForMode(modeType.id, current)
            }
        }
    }

    fun removeApp(app: AppInfo) {
        val current = _selectedApps.value.toMutableList()
        current.remove(app)
        _selectedApps.value = current
        _modeType.value?.let { modeType ->
            viewModelScope.launch {
                repository.saveAppSelectionsForMode(modeType.id, current)
            }
        }
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
                                icon = icon,
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


