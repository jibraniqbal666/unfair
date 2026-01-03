package com.unfair.moment

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unfair.moment.database.AppSelectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

@HiltViewModel
class UnfairViewModel @Inject constructor(
    val application: Application,
    val repository: AppSelectionRepository,
) : ViewModel() {
    private val _currentMode = MutableStateFlow<Mode?>(null)
    val currentMode: StateFlow<Mode?> = _currentMode.asStateFlow()

    init {
        launchMode()
    }

    fun launchMode() {
        viewModelScope.launch {
            // Load saved app selections for this mode
            repository.getActiveModeFlow().flowOn(Dispatchers.IO).collect {
                val modeType = it ?: return@collect
                val savedMode = repository.loadSavedMode(modeType.modeTypeId) ?: return@collect
                val selectedApps = savedMode.selectedApps

                // Update the selected apps with icons from package manager
                val appsWithIcons = selectedApps.map { appInfo ->
                    try {
                        val pm = application.packageManager
                        val icon = pm.getApplicationIcon(appInfo.packageName)
                        AppInfo(
                            packageName = appInfo.packageName,
                            name = appInfo.name,
                            icon = icon,
                        )
                    } catch (e: Exception) {
                        appInfo
                    }
                }

                val dndActive = repository.isDNDEnabled(modeType.modeTypeId)

                _currentMode.value = Mode(
                    type = savedMode.type,
                    selectedApps = appsWithIcons,
                    isDNDActive = dndActive,
                )
            }
        }
    }

    fun launchApp(app: AppInfo, context: Context) {
        val pm = application.packageManager
        val intent = pm.getLaunchIntentForPackage(app.packageName)
        context.startActivity(intent)
    }
}


