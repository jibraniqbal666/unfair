package com.unfair.moment

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unfair.moment.database.AppSelectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AppSelectViewModel @Inject constructor(
    val application: Application,
    val repository: AppSelectionRepository,
) : ViewModel() {
    private val _modeType = MutableStateFlow<ModeType?>(null)

    private val _availableApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val availableApps: StateFlow<List<AppInfo>> = _availableApps.asStateFlow()

    private val _selectedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val selectedApps: StateFlow<List<AppInfo>> = _selectedApps.asStateFlow()

    init {
        loadInstalledApps()
    }

    fun setMode(modeTypeId: String) {
        viewModelScope.launch {
            _modeType.value = repository.getModeType(modeTypeId)
            val savedMode = repository.loadSavedMode(modeTypeId)
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
            val pm = application.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val apps = pm.queryIntentActivities(intent, 0)
                .mapNotNull { packageInfo ->
                    try {
                        val appInfo = packageInfo.activityInfo ?: return@launch
                        // Filter out system apps and launcher itself
                        if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 && appInfo.packageName != application.packageName) {
                            val name = packageInfo.loadLabel(pm).toString()
                            val icon = pm.getApplicationIcon(appInfo.packageName)
                            AppInfo(
                                packageName = appInfo.packageName,
                                name = name,
                                icon = icon,
                            )
                        } else null
                    } catch (_: Exception) {
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


